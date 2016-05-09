package uk.co.transputersystems.transputer.assembler;

import uk.co.transputersystems.transputer.assembler.config.AssemblerConfig;
import uk.co.transputersystems.transputer.assembler.config.Connection;
import uk.co.transputersystems.transputer.assembler.config.IOPin;
import uk.co.transputersystems.transputer.assembler.expression.ExpressionEvaluator;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Assembler {

    public final static long maxInt = (1L << 31) - 1;
    public final static long minInt = -(1L << 31);

    public final static Pattern inputComment = Pattern.compile("^\\h*--\\h*(?<comment>\\V*)$");
    // TODO: test underscores, . and ~ in labels
    public final static Pattern instructionPattern = Pattern.compile(
            "^\\h*((?<label>[0-9A-Za-z~\\._]+):)?\\h*((?<code>(?<opcode>[a-z]+)(\\h+?((?<constantoperand>-?[0-9]+)|(?<labeloperand>[0-9A-Za-z~$@\\._\\-][0-9A-Za-z~$\\._\\- ]*?)))?)?(\\h*--\\h*(?<comment>\\V*?))?)?\\h*$");
    // TODO: test direct parsing, check that directives are not accidentally interpreted as instructions
    public final static Pattern directivePattern = Pattern.compile(
            "^\\h*((?<label>[0-9A-Za-z~\\._]+):)?\\h*((?<directive>(#data\\h+[0-9]+)|(#chan\\h+[0-9A-Za-z]+))?(\\h*--\\h*(?<comment>\\V*?))?)?\\h*$");
    // Match patterns $C, $0, $99 etc.
    // Last group ensures that we do not e.g. match $5 if the string is actually $52
    public final static Pattern dollarPattern = Pattern.compile("[$]((?<current>C)|(?<number>[0-9]+))([^\\p{Alnum}~_]|$)");

    public static Map<String, Opcode> opcodes = new HashMap<>();

    static {
        opcodes.put("pfix", new Opcode(OpcodeType.DIRECT, "pfix", 0x2));
        opcodes.put("nfix", new Opcode(OpcodeType.DIRECT, "nfix", 0x6));
        opcodes.put("opr", new Opcode(OpcodeType.DIRECT, "opr", 0xF));
        opcodes.put("ldc", new Opcode(OpcodeType.DIRECT, "ldc", 0x4));
        opcodes.put("ldl", new Opcode(OpcodeType.DIRECT, "ldl", 0x7));
        opcodes.put("stl", new Opcode(OpcodeType.DIRECT, "stl", 0xD));
        opcodes.put("ldlp", new Opcode(OpcodeType.DIRECT, "ldlp", 0x1));
        opcodes.put("adc", new Opcode(OpcodeType.DIRECT, "adc", 0x8));
        opcodes.put("eqc", new Opcode(OpcodeType.DIRECT, "eqc", 0xC));
        opcodes.put("j", new Opcode(OpcodeType.DIRECT, "j", 0x0));
        opcodes.put("cj", new Opcode(OpcodeType.DIRECT, "cj", 0xA));
        opcodes.put("ldnl", new Opcode(OpcodeType.DIRECT, "ldnl", 0x3));
        opcodes.put("stnl", new Opcode(OpcodeType.DIRECT, "stnl", 0xE));
        opcodes.put("ldnlp", new Opcode(OpcodeType.DIRECT, "ldnlp", 0x5));
        opcodes.put("call", new Opcode(OpcodeType.DIRECT, "call", 0x9));
        opcodes.put("ajw", new Opcode(OpcodeType.DIRECT, "ajw", 0xB));
        opcodes.put("#empty", new Opcode(OpcodeType.DIRECT, "#empty", 0x0)); // Pseudo-opcode for inserting zeros
        opcodes.put("rev", new Opcode(OpcodeType.INDIRECT, "rev", 0x00));
        opcodes.put("add", new Opcode(OpcodeType.INDIRECT, "add", 0x05));
        opcodes.put("sub", new Opcode(OpcodeType.INDIRECT, "sub", 0x0C));
        opcodes.put("mul", new Opcode(OpcodeType.INDIRECT, "mul", 0x53));
        opcodes.put("div", new Opcode(OpcodeType.INDIRECT, "div", 0x2C));
        opcodes.put("and", new Opcode(OpcodeType.INDIRECT, "and", 0x46));
        opcodes.put("or", new Opcode(OpcodeType.INDIRECT, "or", 0x4B));
        opcodes.put("xor", new Opcode(OpcodeType.INDIRECT, "xor", 0x33));
        opcodes.put("not", new Opcode(OpcodeType.INDIRECT, "not", 0x32));
        opcodes.put("shl", new Opcode(OpcodeType.INDIRECT, "shl", 0x41));
        opcodes.put("shr", new Opcode(OpcodeType.INDIRECT, "shr", 0x40));
        opcodes.put("gt", new Opcode(OpcodeType.INDIRECT, "gt", 0x09));
        opcodes.put("lend", new Opcode(OpcodeType.INDIRECT, "lend", 0x21));
        opcodes.put("bsub", new Opcode(OpcodeType.INDIRECT, "bsub", 0x02));
        opcodes.put("wsub", new Opcode(OpcodeType.INDIRECT, "wsub", 0x0A));
        opcodes.put("bcnt", new Opcode(OpcodeType.INDIRECT, "bcnt", 0x34));
        opcodes.put("wcnt", new Opcode(OpcodeType.INDIRECT, "wcnt", 0x3F));
        opcodes.put("ldpi", new Opcode(OpcodeType.INDIRECT, "ldpi", 0x1B));
        opcodes.put("move", new Opcode(OpcodeType.INDIRECT, "move", 0x4A));
        opcodes.put("in", new Opcode(OpcodeType.INDIRECT, "in", 0x07));
        opcodes.put("out", new Opcode(OpcodeType.INDIRECT, "out", 0x0B));
        opcodes.put("outword", new Opcode(OpcodeType.INDIRECT, "outword", 0x0F));
        opcodes.put("gcall", new Opcode(OpcodeType.INDIRECT, "gcall", 0x06));
        opcodes.put("gajw", new Opcode(OpcodeType.INDIRECT, "gajw", 0x3C));
        opcodes.put("ret", new Opcode(OpcodeType.INDIRECT, "ret", 0x20));
        opcodes.put("startp", new Opcode(OpcodeType.INDIRECT, "startp", 0x0D));
        opcodes.put("endp", new Opcode(OpcodeType.INDIRECT, "endp", 0x03));
        opcodes.put("runp", new Opcode(OpcodeType.INDIRECT, "runp", 0x39));
        opcodes.put("stopp", new Opcode(OpcodeType.INDIRECT, "stopp", 0x15));
        opcodes.put("ldpri", new Opcode(OpcodeType.INDIRECT, "ldpri", 0x1E));
        opcodes.put("ldtimer", new Opcode(OpcodeType.INDIRECT, "ldtimer", 0x22));
        opcodes.put("tin", new Opcode(OpcodeType.INDIRECT, "tin", 0x2B));
        opcodes.put("alt", new Opcode(OpcodeType.INDIRECT, "alt", 0x43));
        opcodes.put("altwt", new Opcode(OpcodeType.INDIRECT, "altwt", 0x44));
        opcodes.put("altend", new Opcode(OpcodeType.INDIRECT, "altend", 0x45));
        opcodes.put("talt", new Opcode(OpcodeType.INDIRECT, "talt", 0x4E));
        opcodes.put("taltwt", new Opcode(OpcodeType.INDIRECT, "taltwt", 0x51));
        opcodes.put("enbs", new Opcode(OpcodeType.INDIRECT, "enbs", 0x49));
        opcodes.put("diss", new Opcode(OpcodeType.INDIRECT, "diss", 0x30));
        opcodes.put("enbc", new Opcode(OpcodeType.INDIRECT, "enbc", 0x48));
        opcodes.put("disc", new Opcode(OpcodeType.INDIRECT, "disc", 0x2F));
        opcodes.put("enbt", new Opcode(OpcodeType.INDIRECT, "enbt", 0x47));
        opcodes.put("dist", new Opcode(OpcodeType.INDIRECT, "dist", 0x2E));
        opcodes.put("resetch", new Opcode(OpcodeType.INDIRECT, "resetch", 0x12));
        opcodes.put("sthf", new Opcode(OpcodeType.INDIRECT, "sthf", 0x18));
        opcodes.put("stlf", new Opcode(OpcodeType.INDIRECT, "stlf", 0x1C));
        opcodes.put("sttimer", new Opcode(OpcodeType.INDIRECT, "sttimer", 0x54));
        opcodes.put("sthb", new Opcode(OpcodeType.INDIRECT, "sthb", 0x50));
        opcodes.put("stlb", new Opcode(OpcodeType.INDIRECT, "stlb", 0x17));
        opcodes.put("saveh", new Opcode(OpcodeType.INDIRECT, "saveh", 0x3E));
        opcodes.put("savel", new Opcode(OpcodeType.INDIRECT, "savel", 0x3D));
        opcodes.put("mint", new Opcode(OpcodeType.INDIRECT, "mint", 0x42));
        opcodes.put("diff", new Opcode(OpcodeType.INDIRECT, "diff", 0x04));
        opcodes.put("sum", new Opcode(OpcodeType.INDIRECT, "sum", 0x52));
        opcodes.put("csub0", new Opcode(OpcodeType.INDIRECT, "csub0", 0x13));
        opcodes.put("ccnt1", new Opcode(OpcodeType.INDIRECT, "ccnt1", 0x4D));
        opcodes.put("testerr", new Opcode(OpcodeType.INDIRECT, "testerr", 0x29));
        opcodes.put("seterr", new Opcode(OpcodeType.INDIRECT, "seterr", 0x10));
        opcodes.put("stoperr", new Opcode(OpcodeType.INDIRECT, "stoperr", 0x55));
        opcodes.put("clrhalterr", new Opcode(OpcodeType.INDIRECT, "clrhalterr", 0x57));
        opcodes.put("sethalterr", new Opcode(OpcodeType.INDIRECT, "sethalterr", 0x58));
        opcodes.put("testhalterr", new Opcode(OpcodeType.INDIRECT, "testhalterr", 0x59));
        opcodes.put("confio", new Opcode(OpcodeType.INDIRECT, "confio", 0x08));
    }

    /**
     * Given a `File` pointing to a config `yaml` file, attempt to load it as an `AssemblerConfig` object.
     */
    @Nullable
    public static AssemblerConfig loadConfig(@Nullable Reader configReader) {
        if (configReader != null) {
            Yaml yaml = new Yaml();
            return yaml.loadAs(configReader, AssemblerConfig.class);
        }
        return null;
    }

    /**
     * Convert an assembly file into object code.
     */
    public static List<String> assemble(@Nonnull String input, @Nullable AssemblerConfig config, @Nonnull PrintStream logger, boolean insertIOConfiguration) throws DuplicateLabelException {
        List<Instruction> assembly = parseAssembly(input);

        logger.println("== Assembly ==");
        // TODO: build printable representation
        logger.println(assembly);

        if (insertIOConfiguration) {
            assembly = insertIOConfiguration(assembly, config);
        }

        List<Instruction> dollarSubstitutedAssembly = processDollarLabelOperands(assembly);

        if (config != null) {
            dollarSubstitutedAssembly = processChanDirectives(assembly, config);
            logger.println("== Assembly: #chan directives processed ==");
            logger.println(assembly);
        }

        List<Instruction> dataDirectivesProcessedAssembly = processDataDirectives(dollarSubstitutedAssembly);

        Assembly labelMappedAssembly = makeLabelMap(dataDirectivesProcessedAssembly);

        Map<String, Long> absoluteLabelMap = new LinkedHashMap<>();
        if (config != null) {
            absoluteLabelMap = getConfigLabels(config);
        }

        logger.println("== Label map ==");
        for (Map.Entry<String, Long> entry : labelMappedAssembly.labelMap.entrySet()) {
            logger.printf("%s : %s%n", entry.getKey(), entry.getValue());
        }

        logger.println("== Assembly: label map built ==");
        logger.println(labelMappedAssembly.instructions);

        Assembly patchedLabelAssembly = patchLabels(labelMappedAssembly, absoluteLabelMap);

        logger.println("== Label map: patched ==");
        for (Map.Entry<String, Long> entry : patchedLabelAssembly.labelMap.entrySet()) {
            logger.printf("%s : %s%n", entry.getKey(), entry.getValue());
        }

        logger.println("== Assembly: labels patched ==");
        logger.println(patchedLabelAssembly.instructions);

        List<String> machineCode = translateToMachineCode(patchedLabelAssembly);

        logger.println("== Machine code ==");
        logger.println(machineCode);

        return machineCode;
    }

    /**
     * Given some input string representing an assembly source file, parse it into its directive and instructions.
     */
    @Nonnull
    public static List<Instruction> parseAssembly(@Nonnull String input) {
        String[] lines = input.split("\\R");

        List<Instruction> instructions = new ArrayList<>();

        long currentLine = 0;

        for (String line : lines) {
            currentLine++;
            Matcher instructionMatcher = instructionPattern.matcher(line);
            Matcher directiveMatcher = directivePattern.matcher(line);
            if (instructionMatcher.matches()) {
                String opcode = instructionMatcher.group("opcode");
                if (opcode != null && !opcodes.containsKey(opcode)) {
                    throw new IllegalArgumentException("Opcode '" + opcode + "' is not recognised.");
                }

                Long operandInteger = null;
                if (instructionMatcher.group("constantoperand") != null) {
                    operandInteger = tryParse(instructionMatcher.group("constantoperand"));
                }

                instructions.add(new Instruction(InstructionType.INSTRUCTION, instructionMatcher.group("label"), opcodes.get(opcode), operandInteger, instructionMatcher.group("labeloperand"), instructionMatcher.group("comment"), null, currentLine));
            } else if (directiveMatcher.matches()) {
                instructions.add(new Instruction(InstructionType.DIRECTIVE, directiveMatcher.group("label"), null, null, null, directiveMatcher.group("comment"), directiveMatcher.group("directive"), currentLine));
            } else {
                throw new IllegalArgumentException("Line '" + line + "' does not match the expected format.");
            }
        }

        return instructions;
    }

    /**
     * Given a list of instructions and a config, find the init instruction and insert IOPin intialisation just after it
     */
    public static List<Instruction> insertIOConfiguration(List<Instruction> instructions, AssemblerConfig config) {
        List<Instruction> updatedInstructions = new ArrayList<>();

        for (Instruction instruction : instructions) {
            if ("init".equals(instruction.label)) {
                // Insert the init label, by itself
                updatedInstructions.add(new Instruction(InstructionType.INSTRUCTION, "init", null, null, null, null, null, instruction.originalLine));

                // Insert the configuration instructions
                for (IOPin pin : config.getProcessor().getIopins()) {
                    updatedInstructions.add(new Instruction(InstructionType.INSTRUCTION, null, opcodes.get("ldc"), (long) pin.getAddr(), null, null, null, instruction.originalLine));
                    updatedInstructions.add(new Instruction(InstructionType.INSTRUCTION, null, opcodes.get("ldc"), (long) pin.getConfig(), null, null, null, instruction.originalLine));
                    updatedInstructions.add(new Instruction(InstructionType.INSTRUCTION, null, opcodes.get("confio"), null, null, null, null, instruction.originalLine));
                }

                // Re-add non-label contents of the init instruction after the config
                updatedInstructions.add(new Instruction(InstructionType.INSTRUCTION, "postconfig", instruction.opcode, instruction.constantOperand, instruction.labelOperand, instruction.comment, instruction.directive, instruction.originalLine));

            } else {
                updatedInstructions.add(instruction);
            }
        }

        return updatedInstructions;
    }

    /**
     * Process any `$` references in label operands, replacing them with a reference to a new label that then gets inserted
     * after the next instruction.
     */
    public static List<Instruction> processDollarLabelOperands(List<Instruction> instructions) {
        List<Instruction> processedAssembly = new ArrayList<>();

        long nextLabelIndex = 0;

        List<Pair<Long, String>> labelsToInsert = new ArrayList<>();
        // Number of the instruction currently being processed - excludes lines with only comments/labels
        long instructionNumber = 0;

        // Keep track of the last originalLine so we can use it at the end
        long lastOriginalLine = 0;

        for (Instruction instruction : instructions) {

            lastOriginalLine = instruction.originalLine;

            if (!((instruction.type == InstructionType.INSTRUCTION && instruction.opcode == null) || (instruction.type == InstructionType.DIRECTIVE && instruction.directive == null))) {
                instructionNumber++;
            }

            // Insert any labels destined for this line
            List<Pair<Long, String>> labelsLeftToInsert = new ArrayList<>();
            for (Pair<Long, String> labelToInsert : labelsToInsert) {
                if (labelToInsert.a.equals(instructionNumber)) {
                    processedAssembly.add(new Instruction(InstructionType.INSTRUCTION, labelToInsert.b, null, null, null, null, null, instruction.originalLine));
                } else {
                    labelsLeftToInsert.add(labelToInsert);
                }
            }

            // Remove the inserted labels from the list of labels yet to insert
            labelsToInsert = labelsLeftToInsert;

            if (instruction.labelOperand != null) {
                Matcher dollarMatcher = dollarPattern.matcher(instruction.labelOperand);

                Instruction substitutedInstruction = instruction;

                while (dollarMatcher.find()) {
                    nextLabelIndex++;

                    if (dollarMatcher.group("current") != null) {
                        processedAssembly.add(new Instruction(InstructionType.INSTRUCTION, "__dollar" + nextLabelIndex, null, null, null, null, null, instruction.originalLine));
                        substitutedInstruction = new Instruction(
                                substitutedInstruction.type,
                                substitutedInstruction.label,
                                substitutedInstruction.opcode,
                                substitutedInstruction.constantOperand,
                                substitutedInstruction.labelOperand.replace("$C", "__dollar" + nextLabelIndex),
                                substitutedInstruction.comment,
                                substitutedInstruction.directive,
                                substitutedInstruction.originalLine);
                    } else if (dollarMatcher.group("number") != null) {
                        Long maybeNumber = tryParse(dollarMatcher.group("number"));
                        if (maybeNumber != null) {
                            labelsToInsert.add(new Pair<>(instructionNumber + 1 + maybeNumber, "__dollar" + nextLabelIndex));
                            substitutedInstruction = new Instruction(
                                    substitutedInstruction.type,
                                    substitutedInstruction.label,
                                    substitutedInstruction.opcode,
                                    substitutedInstruction.constantOperand,
                                    substitutedInstruction.labelOperand.replace("$" + maybeNumber, "__dollar" + nextLabelIndex),
                                    substitutedInstruction.comment,
                                    substitutedInstruction.directive,
                                    substitutedInstruction.originalLine);
                        } else {
                            throw new IllegalArgumentException("Label operand '$" + dollarMatcher.group("number") + "' could not be parsed");
                        }
                    } else {
                        throw new IllegalArgumentException("Cannot process dollar label '" + dollarMatcher.group(0) + "'");
                    }
                    dollarMatcher = dollarPattern.matcher(substitutedInstruction.labelOperand);
                }

                processedAssembly.add(substitutedInstruction);

            } else {
                processedAssembly.add(instruction);
            }
        }

        instructionNumber++;

        // Insert any labels destined for the end
        List<Pair<Long, String>> labelsLeftToInsert = new ArrayList<>();
        for (Pair<Long, String> labelToInsert : labelsToInsert) {
            if (labelToInsert.a.equals(instructionNumber)) {
                processedAssembly.add(new Instruction(InstructionType.INSTRUCTION, labelToInsert.b, null, null, null, null, null, lastOriginalLine));
            } else {
                labelsLeftToInsert.add(labelToInsert);
            }
        }

        if (labelsLeftToInsert.size() > 0) {
            throw new ArrayIndexOutOfBoundsException("A dollar label wants to be inserted beyond the end of the code.");
        }

        return processedAssembly;
    }

    /**
     * Process any `#chan` directives in the assembly, replacing them with a `ldc <address>` corresponding to the address
     * of the specified channel in the config.
     */
    public static List<Instruction> processChanDirectives(List<Instruction> instructions, AssemblerConfig config) {
        List<Instruction> fixedAssembly = new ArrayList<>();

        Pattern chanDirective = Pattern.compile("^\\h*#chan\\h+(?<channelname>[0-9A-Za-z]+)\\h*$");

        for (Instruction instruction : instructions) {

            if (instruction.type == InstructionType.DIRECTIVE && instruction.directive != null) {
                Matcher chanDirectiveMatcher = chanDirective.matcher(instruction.directive);
                if (chanDirectiveMatcher.matches()) {

                    String channelName = chanDirectiveMatcher.group("channelname");

                    // Find the first IOPin in the config which matches the channel name in the directive
                    Optional<IOPin> ioPinMatchingComment = config.getProcessor().getIopins().stream()
                            .filter(p -> p.getChannel().equals(channelName))
                            .findFirst();

                    // If an IOPin was found,
                    if (ioPinMatchingComment.isPresent()) {
                        fixedAssembly.add(new Instruction(InstructionType.INSTRUCTION, instruction.label, opcodes.get("ldc"), (long) ioPinMatchingComment.get().getAddr(), null, instruction.comment, null, instruction.originalLine));
                    } else {
                        throw new IllegalArgumentException("'" + channelName + "' was not found in the config.");
                    }
                }
            } else {
                fixedAssembly.add(instruction);
            }
        }

        return fixedAssembly;
    }

    /**
     * Process any `#data` directives in the code, but do not word-align them at this point.
     */
    public static List<Instruction> processDataDirectives(List<Instruction> instructions) {
        List<Instruction> processedAssembly = new ArrayList<>();

        Pattern dataDirective = Pattern.compile("^\\h*#data\\h+(?<numbytes>[0-9]+)\\h*$");

        for (Instruction instruction : instructions) {
            if (instruction.type == InstructionType.DIRECTIVE && instruction.directive != null) {
                Matcher dataDirectiveMatcher = dataDirective.matcher(instruction.directive);
                if (dataDirectiveMatcher.matches()) {
                    String numBytes = dataDirectiveMatcher.group("numbytes");
                    Long numBytesInteger = null;
                    if (numBytes != null) {
                        numBytesInteger = tryParse(numBytes);
                    }
                    if (numBytesInteger != null & numBytesInteger > 0) {
                        processedAssembly.add(new Instruction(InstructionType.INSTRUCTION, instruction.label, opcodes.get("#empty"), 0L, null, instruction.comment, null, instruction.originalLine));
                        for (int i = 1; i < numBytesInteger; i++) {
                            processedAssembly.add(new Instruction(InstructionType.INSTRUCTION, null, opcodes.get("#empty"), 0L, null, null, null, instruction.originalLine));
                        }
                    } else {
                        throw new IllegalArgumentException("The number of bytes '" + numBytes + "' could not be parsed.");
                    }
                }
            } else {
                processedAssembly.add(instruction);
            }
        }

        return processedAssembly;
    }

    /**
     * Search for label declarations in the assembly code and create a map from the label names to line numbers.
     * Eliminate lines that do not contain any code (i.e. ones that only consist of labels and/or comments)
     */
    public static Assembly makeLabelMap(List<Instruction> instructions) {
        List<Instruction> updatedAssembly = new ArrayList<>();
        Map<String, Long> labelMap = new LinkedHashMap<>();

        // The actual line of code we have reached - skips lines with only labels and comments
        long lineNumber = 0;

        for (Instruction instruction : instructions) {
            // If there is a label, add it to the label map
            if (instruction.label != null) {
                labelMap.put(instruction.label, lineNumber);
            }

            // If there is an actual instruction on this line, retain it and increment the line number
            if (instruction.opcode != null) {
                updatedAssembly.add(instruction);
                lineNumber++;
            }

        }

        return new Assembly(updatedAssembly, labelMap);
    }

    /**
     * Extract the channel-label pairs from the config.
     */
    public static Map<String, Long> getConfigLabels(@Nonnull AssemblerConfig config) throws DuplicateLabelException {
        Map<String, Long> absoluteLabelMap = new LinkedHashMap<>();

        for (Connection connection : config.getProcessor().getConnections()) {
            if (absoluteLabelMap.containsKey(connection.getChannel())) {
                throw new DuplicateLabelException(connection.getChannel());
            } else {
                absoluteLabelMap.put("@" + connection.getChannel(), (long) connection.getDest_port()); // is this right?
            }
        }

        for (IOPin iopin : config.getProcessor().getIopins()) {
            if (absoluteLabelMap.containsKey(iopin.getChannel())) {
                throw new DuplicateLabelException(iopin.getChannel());
            } else {
                absoluteLabelMap.put("@" + iopin.getChannel(), (long) iopin.getAddr());
            }
        }

        return absoluteLabelMap;
    }

    /**
     * Perform prefixing/nfixing operations, updating the label match to reflect the inserted lines.
     */
    public static Assembly patchLabels(Assembly assembly, Map<String, Long> absoluteLabelMap) {
        List<Instruction> updatedAssembly = new ArrayList<>();
        Map<String, Long> updatedLabelMap = new HashMap<>(assembly.labelMap);

        int lineNumber = 0;

        for (Instruction instruction : assembly.instructions) {
            // How many extra lines we have added to the assembly for this instruction
            int patchOffset = 0;

            // If the current instruction has an indirect opcode, patch in an extra line to compensate for the
            // indirection prefixing that will take place later in `translateToMachineCode`.
            // Indirect operations do not have operands, so there is no need to perform operand prefixing.
            // TODO: deal with indirect ops that are longer than a byte
            if (instruction.opcode.type == OpcodeType.INDIRECT) {
                if (instruction.opcode.encodesTo > 0xFF) {
                    throw new IllegalArgumentException("Indirect instructions with opcodes larger than a byte are not supported.");
                }
                if (instruction.opcode.encodesTo > 0xF) {
                    patchOffset++;
                }
                updatedAssembly.add(instruction);
            } else if (instruction.labelOperand != null) {
                // Otherwise, the opcode is direct. If the operand is a label, patch in 7 extra lines - enough space to
                // store a 32-bit address.
                // TODO: find a way to be more efficient and only patch in the minimum number of ops for the address
                // TODO: support non-32-bit architectures
                patchOffset += 7;
                updatedAssembly.add(instruction);
            } else if (instruction.constantOperand != null) {
                // Otherwise, if the opcode is direct and has a valid operand:
                if (instruction.constantOperand < 0 || instruction.constantOperand > 0xF) {
                    // If the operand is a number that cannot be represented as the lower nibble of the instructionPattern, perform
                    // prefixing and patch in the number of lines added by the prefixing
                    List<Instruction> prefixResult = prefix(instruction);
                    patchOffset += prefixResult.size() - 1; // -1 to exclude the original op
                    updatedAssembly.addAll(prefixResult);
                } else {
                    // Otherwise, the operand is small enough to fit in the lower nibble, so just leave the line as is
                    updatedAssembly.add(instruction);
                }
            } else {
                // Otherwise, the operand is missing. This is not permitted for a direct instruction.
                throw new IllegalArgumentException("Instruction '" + instruction + "' does not have a valid operand.");
            }

            // Update the location of all labels appearing after this point in the code
            for (Map.Entry<String, Long> labelEntry : updatedLabelMap.entrySet()) {
                if (labelEntry.getValue() > lineNumber) {
                    labelEntry.setValue(labelEntry.getValue() + patchOffset);
                }
            }

            // Increment the line number by the number of extra lines added through prefixing, plus 1 for the instructionPattern itself
            lineNumber += patchOffset + 1;

        }

        updatedLabelMap.putAll(absoluteLabelMap);

        return new Assembly(updatedAssembly, updatedLabelMap);
    }

    /**
     * Take some assembly and a label map and translate it to Transputer machine code.
     * NB: any labels in the assembly will be converted to a full 8 instructions - the `labelMap` must already take this
     * into account. TODO: make this more intuitive
     */
    private static List<String> translateToMachineCode(Assembly assembly) {
        List<String> machineCode = new ArrayList<>();

        if (!assembly.labelMap.containsKey("init")) {
            throw new IllegalArgumentException("No init label specified.");
        }

        // The line/byte number from which the code should start executing
        machineCode.add("Start address: " + String.format("%08x", assembly.labelMap.get("init")));

        int actualLine = 0;

        for (Instruction instruction : assembly.instructions) {

            // Convert the opcode to its hex representation
            List<Byte> instructionBytes = new ArrayList<>();

            // Generate the correct bytes for the opcode based on whether it is direct or indirect and what kind of
            // operand it has (integer/label)
            if (instruction.opcode.type == OpcodeType.DIRECT) {
                if (instruction.constantOperand != null) {
                    instructionBytes = Collections.singletonList(processDirectInstructionWithConstantOperand(instruction));
                } else if (instruction.labelOperand != null) {
                    instructionBytes = new ArrayList<>(processDirectInstructionWithLabelOperand(instruction, assembly.labelMap));
                }
            } else if (instruction.opcode.type == OpcodeType.INDIRECT) {
                // Indirect opcodes cannot have operands
                if (instruction.constantOperand != null || instruction.labelOperand != null) {
                    throw new IllegalArgumentException("Indirect instructions cannot have any operand: '" + instruction + "'");
                }

                instructionBytes = new ArrayList<>(processIndirectInstruction(instruction));
            }

            // Map the list of bytes to their two-digit hex representations and append them to the machine code
            machineCode.addAll(instructionBytes
                    .stream()
                    .map(b -> String.format("%02x", b))
                    .collect(Collectors.toList()));

            // Increment the line number by the number of instructionPattern bytes added
            actualLine += instructionBytes.size();
        }

        return machineCode;
    }

    /**
     * Convert an indirect opcode to either one or two machine instructions (depends on whether the opcode is a nibble
     * or a byte)
     * TODO: enable larger prefixes - see `patchLabels`
     */
    public static List<Byte> processIndirectInstruction(Instruction instruction) {
        List<Byte> result = new ArrayList<>();

        if (instruction.opcode.encodesTo > 0xFF) {
            throw new IllegalArgumentException("Indirect instructions >0xFF are not yet supported.");
        }

        if (instruction.opcode.encodesTo > 0xF) {
            result.add((byte) ((opcodes.get("pfix").encodesTo << 4) | (instruction.opcode.encodesTo >>> 4)));
        }
        result.add((byte) ((opcodes.get("opr").encodesTo << 4) | (instruction.opcode.encodesTo & 0xF)));

        return result;
    }

    /**
     * Convert a direct opcode with a label operand to eight machine instructions that load the relative address
     * of the label and execute the operation.
     * TODO: support the minimum number of pfix instructions
     * TODO: support addresses larger than 32 bits
     */
    public static List<Byte> processDirectInstructionWithLabelOperand(Instruction instruction, Map<String, Long> labelMap) {
        if (instruction.labelOperand == null) {
            throw new IllegalArgumentException("Label operand cannot be null");
        }

        Long labelOp = null;
        if (labelMap.containsKey(instruction.labelOperand)) {
            // If the label can be retrieved from the map directly, get its associated line number
            // Calculate the relative address, subtracting 8 to account for the extra pfix ops we will insert
            labelOp = labelMap.get(instruction.labelOperand);
        } else {
            // Otherwise, the label is an arithmetic expression - evaluate it:
            ExpressionEvaluator evaluator = new ExpressionEvaluator(labelMap);
            ParseTree expressionTree = parseExpression(instruction.labelOperand, AssemblerExpressionParser::expression);
            if (expressionTree != null) {
                labelOp = evaluator.visit(expressionTree);
            } else {
                throw new IllegalArgumentException("Invalid label operand '" + instruction.labelOperand + "'");
            }
        }

        if (labelOp < minInt || labelOp > maxInt) {
            throw new IllegalArgumentException("Label '" + instruction.labelOperand + "' (" + labelOp + ") is out of range");
        }

        // Get the prefix that is actually necessary to represent the address
        // NB: this logic really inserts the label in the wrong place, but because any extra pfixs inserted are 0, it
        // doesn't matter
        List<Instruction> prefix = new ArrayList<>(prefix(new Instruction(instruction.type, instruction.label, instruction.opcode, labelOp, null, instruction.comment, instruction.directive, instruction.originalLine)));

        // Pad the list of instructions up to 8, as this is the assumed size from label patching
        while (prefix.size() < 8) {
            prefix.add(0, new Instruction(InstructionType.INSTRUCTION, null, opcodes.get("pfix"), 0L, null, null, null, instruction.originalLine));
        }

        // Now we have converted the instructionPattern+label into several instructionPattern+operands, convert those to their hex
        // representations
        return prefix
                .stream()
                .map(Assembler::processDirectInstructionWithConstantOperand)
                .collect(Collectors.toList());
    }

    /**
     * Convert an opcode and operand pair into their hex representation.
     */
    public static byte processDirectInstructionWithConstantOperand(Instruction instruction) {
        if ("opr".equals(instruction.opcode.opcode)) {
            throw new IllegalArgumentException("Found unexpected 'opr' instructionPattern");
        }
        if (instruction.constantOperand == null) {
            throw new IllegalArgumentException("Operand cannot be null");
        }

        // If there is an operand that fits into the lower nibble, return the single instructionPattern
        if (0x0 <= instruction.constantOperand && instruction.constantOperand <= 0xF) {
            return (byte) ((instruction.opcode.encodesTo << 4) | instruction.constantOperand & 0xF);
        } else {
            throw new IllegalArgumentException("Operand for direct instructionPattern must be between in range 0x0 to 0xF");
        }
    }

    /**
     * Convert an opcode with an operand into the minimum series of prefix instructions.
     *
     * @return A list of assembly instructions - `pfix`s and `nfix`s followed by the original opcode
     */
    public static List<Instruction> prefix(Instruction instruction) {
        if (instruction.constantOperand == null) {
            throw new IllegalArgumentException("Cannot prefix an instruction with a non-integer operand.");
        }
        if (instruction.constantOperand <= 0xF && instruction.constantOperand >= 0) {
            return Collections.singletonList(new Instruction(InstructionType.INSTRUCTION, instruction.label, instruction.opcode, instruction.constantOperand, null, instruction.comment, null, instruction.originalLine));
        } else if (instruction.constantOperand > 0xF) {
            List<Instruction> pfx = new ArrayList<>(prefix(new Instruction(InstructionType.INSTRUCTION, instruction.label, opcodes.get("pfix"), instruction.constantOperand >>> 4, null, instruction.comment, null, instruction.originalLine)));
            pfx.add(new Instruction(InstructionType.INSTRUCTION, null, instruction.opcode, instruction.constantOperand & 0xF, null, null, null, instruction.originalLine));
            return pfx;
        } else if (instruction.constantOperand < 0x0) {
            List<Instruction> pfx = new ArrayList<>(prefix(new Instruction(InstructionType.INSTRUCTION, instruction.label, opcodes.get("nfix"), (~instruction.constantOperand) >>> 4, null, instruction.comment, null, instruction.originalLine)));
            pfx.add(new Instruction(InstructionType.INSTRUCTION, null, instruction.opcode, instruction.constantOperand & 0xF, null, null, null, instruction.originalLine));
            return pfx;
        }
        throw new IllegalArgumentException("Could not prefix '" + instruction.opcode + " " + instruction.constantOperand + "'");
    }

    /**
     * Attempt to parse an integer from a supplied string.
     *
     * @return an `Integer` if the parse is successful, else `null`
     */
    @Nullable
    private static Long tryParse(String str) {
        Long val;
        if (str == null) {
            return null;
        }
        try {
            val = Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
        return val;
    }

    private Assembler() {
    }

    public static ParseTree parseExpression(String expression, Function<AssemblerExpressionParser, ParseTree> startRule) {
        try {
            AssemblerExpressionLexer synLexer = new AssemblerExpressionLexer(new ANTLRInputStream(expression));

            CommonTokenStream tokenStream = new CommonTokenStream(synLexer);
            AssemblerExpressionParser expressionParser = new AssemblerExpressionParser(tokenStream);

            return startRule.apply(expressionParser);

        } catch (Exception ignored) {
            return null;
        }
    }
}
