package uk.co.transputersystems.transputer.assembler;

import uk.co.transputersystems.transputer.assembler.config.AssemblerConfig;
import uk.co.transputersystems.transputer.assembler.config.Connection;
import uk.co.transputersystems.transputer.assembler.config.IOPin;
import uk.co.transputersystems.transputer.assembler.config.Processor;
import org.junit.Test;

import java.io.StringReader;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class AssemblerTest {
    @Test
    public void testLoadConfigIOPins() {
        StringReader configReader = new StringReader(
                "processor:\n" +
                        "    processor_id: 0\n" +
                        "    iopins:\n" +
                        "        - addr: 70\n" +
                        "          channel: pin0\n" +
                        "          config: 1\n");

        AssemblerConfig config = Assembler.loadConfig(configReader);

        assertThat(config.getProcessor().getConnections(), is(equalTo(Collections.emptyList())));
        assertThat(config.getProcessor().getProcessor_id(), is(equalTo(0)));
        assertThat(config.getProcessor().getIopins().get(0).getAddr(), is(equalTo(70)));
        assertThat(config.getProcessor().getIopins().get(0).getChannel(), is(equalTo("pin0")));
        assertThat(config.getProcessor().getIopins().get(0).getConfig(), is(equalTo(1)));
    }

    @Test
    public void testLoadConfigConnections() {
        StringReader configReader = new StringReader(
                "processor:\n" +
                        "    processor_id: 2\n" +
                        "    connections:\n" +
                        "        - target_processor: 1\n" +
                        "          dest_port: 0\n" +
                        "          channel: channel0\n" +
                        "        - target_processor: 0\n" +
                        "          dest_port: 4\n" +
                        "          channel: channel1\n");

        AssemblerConfig config = Assembler.loadConfig(configReader);

        assertThat(config.getProcessor().getIopins(), is(equalTo(Collections.emptyList())));
        assertThat(config.getProcessor().getProcessor_id(), is(equalTo(2)));
        assertThat(config.getProcessor().getConnections().get(0).getTarget_processor(), is(equalTo(1)));
        assertThat(config.getProcessor().getConnections().get(0).getDest_port(), is(equalTo(0)));
        assertThat(config.getProcessor().getConnections().get(0).getChannel(), is(equalTo("channel0")));
        assertThat(config.getProcessor().getConnections().get(1).getTarget_processor(), is(equalTo(0)));
        assertThat(config.getProcessor().getConnections().get(1).getDest_port(), is(equalTo(4)));
        assertThat(config.getProcessor().getConnections().get(1).getChannel(), is(equalTo("channel1")));
    }

    // TODO: test loadConfig with multiple processors?

    @Test
    public void testParseAssembly() {
        String assembly =
                "GlobalVariable_global1:\n" +
                        "#data       4\n" +
                        "init:       \n" +
                        "ajw         -11\n" +
                        "ldc         init~IL_35\n" +
                        "ldc         L0-L_StartP_7\n" +
                        "ldc         L0-$\n" +
                        "ldlp        $\n" +
                        "startp      \n" +
                        "END: -- exit program";

        List<Instruction> instructions = Assembler.parseAssembly(assembly);

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "GlobalVariable_global1", null, null, null, null, null, 1),
                instructions.get(0));

        checkInstructionEquality(
                new Instruction(InstructionType.DIRECTIVE, null, null, null, null, null, "#data       4", 2),
                instructions.get(1));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "init", null, null, null, null, null, 3),
                instructions.get(2));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ajw"), -11L, null, null, null, 4),
                instructions.get(3));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), null, "init~IL_35", null, null, 5),
                instructions.get(4));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), null, "L0-L_StartP_7", null, null, 6),
                instructions.get(5));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), null, "L0-$", null, null, 7),
                instructions.get(6));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldlp"), null, "$", null, null, 8),
                instructions.get(7));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("startp"), null, null, null, null, 9),
                instructions.get(8));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "END", null, null, null, "exit program", null, 10),
                instructions.get(9));
    }

    @Test
    public void testProcessDollarLabelOperandSimple() {
        List<Instruction> instructions = Arrays.asList(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("j"), null, "ALABEL-$1", null, null, 1),
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 5L, null, null, null, 2),
                new Instruction(InstructionType.INSTRUCTION, "ALABEL", Assembler.opcodes.get("ajw"), 1L, null, null, null, 3)
        );

        List<Instruction> processedInstructions = Assembler.processDollarLabelOperands(instructions);

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("j"), null, "ALABEL-__dollar1", null, null, 1),
                processedInstructions.get(0));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 5L, null, null, null, 2),
                processedInstructions.get(1));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "__dollar1", null, null, null, null, null, 3),
                processedInstructions.get(2));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "ALABEL", Assembler.opcodes.get("ajw"), 1L, null, null, null, 3),
                processedInstructions.get(3));
    }

    @Test
    public void testProcessMultipleDollarLabelOperand() {
        List<Instruction> instructions = Arrays.asList(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("j"), null, "$2-$1", null, null, 1),
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("j"), null, "$0-$C", null, null, 2),
                new Instruction(InstructionType.INSTRUCTION, "ALABEL", Assembler.opcodes.get("ajw"), 1L, null, null, null, 3)
        );

        List<Instruction> processedInstructions = Assembler.processDollarLabelOperands(instructions);

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("j"), null, "__dollar1-__dollar2", null, null, 1),
                processedInstructions.get(0));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "__dollar4", null, null, null, null, null, 2),
                processedInstructions.get(1));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("j"), null, "__dollar3-__dollar4", null, null, 2),
                processedInstructions.get(2));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "__dollar2", null, null, null, null, null, 3),
                processedInstructions.get(3));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "__dollar3", null, null, null, null, null, 3),
                processedInstructions.get(4));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "ALABEL", Assembler.opcodes.get("ajw"), 1L, null, null, null, 3),
                processedInstructions.get(5));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "__dollar1", null, null, null, null, null, 3),
                processedInstructions.get(6));
    }

    @Test
    public void testProcessDollarLabelOperandZero() {
        List<Instruction> instructions = Arrays.asList(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("j"), null, "ALABEL-$0", null, null, 1),
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 5L, null, null, null, 2),
                new Instruction(InstructionType.INSTRUCTION, "ALABEL", Assembler.opcodes.get("ajw"), 1L, null, null, null, 3)
        );

        List<Instruction> processedInstructions = Assembler.processDollarLabelOperands(instructions);

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("j"), null, "ALABEL-__dollar1", null, null, 1),
                processedInstructions.get(0));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "__dollar1", null, null, null, null, null, 2),
                processedInstructions.get(1));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 5L, null, null, null, 2),
                processedInstructions.get(2));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "ALABEL", Assembler.opcodes.get("ajw"), 1L, null, null, null, 3),
                processedInstructions.get(3));
    }

    @Test
    public void testProcessDollarLabelOperandCurrent() {
        List<Instruction> instructions = Arrays.asList(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("j"), null, "ALABEL-$C", null, null, 1),
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 5L, null, null, null, 2)
        );

        List<Instruction> processedInstructions = Assembler.processDollarLabelOperands(instructions);

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "__dollar1", null, null, null, null, null, 1),
                processedInstructions.get(0));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("j"), null, "ALABEL-__dollar1", null, null, 1),
                processedInstructions.get(1));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 5L, null, null, null, 2),
                processedInstructions.get(2));
    }

    @Test
    public void testProcessDollarLabelOperandWithInterspersedDirectiveAndLabel() {
        List<Instruction> instructions = Arrays.asList(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("j"), null, "ALABEL-$2", null, null, 1),
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 5L, null, null, null, 2),
                new Instruction(InstructionType.INSTRUCTION, "justalabel", null, null, null, "and comment", null, 3),
                new Instruction(InstructionType.DIRECTIVE, null, null, null, null, null, "#chan chan0", 4),
                new Instruction(InstructionType.INSTRUCTION, "ALABEL", Assembler.opcodes.get("ajw"), 1L, null, null, null, 5)
        );

        List<Instruction> processedInstructions = Assembler.processDollarLabelOperands(instructions);

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("j"), null, "ALABEL-__dollar1", null, null, 1),
                processedInstructions.get(0));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 5L, null, null, null, 2),
                processedInstructions.get(1));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "justalabel", null, null, null, "and comment", null, 3),
                processedInstructions.get(2));
        checkInstructionEquality(
                new Instruction(InstructionType.DIRECTIVE, null, null, null, null, null, "#chan chan0", 4),
                processedInstructions.get(3));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "__dollar1", null, null, null, null, null, 5),
                processedInstructions.get(4));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "ALABEL", Assembler.opcodes.get("ajw"), 1L, null, null, null, 5),
                processedInstructions.get(5));
    }

    @Test
    public void testInsertIOConfiguration() {
        String assembly =
                "init:       ldc 10\najw         -11\n";

        List<Instruction> instructions = Assembler.parseAssembly(assembly);
        AssemblerConfig config = getSampleConfig();

        List<Instruction> configuredInstructions = Assembler.insertIOConfiguration(instructions, config);

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "init", null, null, null, null, null, 1),
                configuredInstructions.get(0));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 70L, null, null, null, 1),
                configuredInstructions.get(1));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 0L, null, null, null, 1),
                configuredInstructions.get(2));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("confio"), null, null, null, null, 1),
                configuredInstructions.get(3));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "postconfig", Assembler.opcodes.get("ldc"), 10L, null, null, null, 1),
                configuredInstructions.get(4));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ajw"), -11L, null, null, null, 2),
                configuredInstructions.get(5));
    }

    @Test
    public void testProcessChanDirectives() {
        List<Instruction> instructions = Arrays.asList(
                new Instruction(InstructionType.DIRECTIVE, null, null, null, null, null, "#chan sensor", 1),
                new Instruction(InstructionType.INSTRUCTION, "alabel", Assembler.opcodes.get("ajw"), -10L, null, null, null, 2),
                new Instruction(InstructionType.DIRECTIVE, null, null, null, null, null, "\t#chan\tsensor", 3),
                new Instruction(InstructionType.DIRECTIVE, "anotherlabel", null, null, null, "a comment", "\t#chan\tsensor", 4)
        );

        List<Instruction> processedInstructions = Assembler.processChanDirectives(instructions, getSampleConfig());

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 70L, null, null, null, 1),
                processedInstructions.get(0));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "alabel", Assembler.opcodes.get("ajw"), -10L, null, null, null, 2),
                processedInstructions.get(1));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 70L, null, null, null, 3),
                processedInstructions.get(2));

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "anotherlabel", Assembler.opcodes.get("ldc"), 70L, null, "a comment", null, 4),
                processedInstructions.get(3));
    }

    @Test
    public void testProcessDataDirectives() {
        List<Instruction> instructions = Arrays.asList(
                new Instruction(InstructionType.DIRECTIVE, null, null, null, null, null, "\t#data\t4", 1),
                new Instruction(InstructionType.INSTRUCTION, "alabel", Assembler.opcodes.get("ajw"), -10L, null, null, null, 2),
                new Instruction(InstructionType.DIRECTIVE, "anotherlabel", null, null, null, "a comment", "\t#data\t3", 3)
        );

        List<Instruction> processedInstructions = Assembler.processDataDirectives(instructions);

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("#empty"), 0L, null, null, null, 1),
                processedInstructions.get(0));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("#empty"), 0L, null, null, null, 1),
                processedInstructions.get(1));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("#empty"), 0L, null, null, null, 1),
                processedInstructions.get(2));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("#empty"), 0L, null, null, null, 1),
                processedInstructions.get(3));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "alabel", Assembler.opcodes.get("ajw"), -10L, null, null, null, 2),
                processedInstructions.get(4));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "anotherlabel", Assembler.opcodes.get("#empty"), 0L, null, "a comment", null, 3),
                processedInstructions.get(5));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("#empty"), 0L, null, null, null, 3),
                processedInstructions.get(6));
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("#empty"), 0L, null, null, null, 3),
                processedInstructions.get(7));
    }

    @Test
    public void testMakeLabelMap() {
        List<Instruction> instructions = Arrays.asList(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("ldc"), 89L, null, null, null, 1),
                new Instruction(InstructionType.INSTRUCTION, "~configuration", Assembler.opcodes.get("confio"), null, null, null, null, 2),
                new Instruction(InstructionType.INSTRUCTION, "alabel", Assembler.opcodes.get("ajw"), -10L, null, null, null, 3),
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldlp"), 0L, null, null, null, 4),
                new Instruction(InstructionType.INSTRUCTION, "justcomment", null, null, null, null, null, 5),
                new Instruction(InstructionType.INSTRUCTION, "jump", Assembler.opcodes.get("j"), null, "~configuration", null, null, 6),
                new Instruction(InstructionType.INSTRUCTION, "~end", null, null, null, "exit program", null, 7)
        );

        Assembly labelMappedAssembly = Assembler.makeLabelMap(instructions);
        assertEquals(new Long(0), labelMappedAssembly.labelMap.get("init"));
        assertEquals(new Long(1), labelMappedAssembly.labelMap.get("~configuration"));
        assertEquals(new Long(2), labelMappedAssembly.labelMap.get("alabel"));
        assertEquals(new Long(4), labelMappedAssembly.labelMap.get("justcomment"));
        assertEquals(new Long(4), labelMappedAssembly.labelMap.get("jump"));
        assertEquals(new Long(5), labelMappedAssembly.labelMap.get("~end"));

        // The label/comment-only instructions should be eliminated
        assertEquals(5, labelMappedAssembly.instructions.size());

    }

    @Test
    public void testPatchLabels() {
        List<Instruction> instructions = Arrays.asList(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("ldc"), 89L, null, null, null, 1),
                new Instruction(InstructionType.INSTRUCTION, "space", Assembler.opcodes.get("#empty"), 0L, null, null, null, 2),
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("#empty"), 0L, null, null, null, 2),
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("#empty"), 0L, null, null, null, 2),
                new Instruction(InstructionType.INSTRUCTION, "~configuration", Assembler.opcodes.get("confio"), null, null, null, null, 3),
                new Instruction(InstructionType.INSTRUCTION, "alabel", Assembler.opcodes.get("ajw"), -10L, null, null, null, 4),
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldlp"), 0L, null, null, null, 5),
                new Instruction(InstructionType.INSTRUCTION, "jump", Assembler.opcodes.get("j"), null, "~configuration", null, null, 7),
                new Instruction(InstructionType.INSTRUCTION, "end", Assembler.opcodes.get("ldc"), 0L, null, null, null, 8)
        );

        Map<String, Long> labelMap = new HashMap<>();
        labelMap.put("init", 0L);
        labelMap.put("space", 1L);
        labelMap.put("~configuration", 4L);
        labelMap.put("alabel", 5L);
        labelMap.put("jump", 7L);
        labelMap.put("end", 8L);

        Assembly patchedAssembly = Assembler.patchLabels(new Assembly(instructions, labelMap), new LinkedHashMap<>());

        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "init", Assembler.opcodes.get("pfix"), 5L, null, null, null, 1),
                patchedAssembly.instructions.get(0)
        );
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 9L, null, null, null, 1),
                patchedAssembly.instructions.get(1)
        );
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "space", Assembler.opcodes.get("#empty"), 0L, null, null, null, 2),
                patchedAssembly.instructions.get(2)
        );
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("#empty"), 0L, null, null, null, 2),
                patchedAssembly.instructions.get(3)
        );
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("#empty"), 0L, null, null, null, 2),
                patchedAssembly.instructions.get(4)
        );
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "~configuration", Assembler.opcodes.get("confio"), null, null, null, null, 3),
                patchedAssembly.instructions.get(5)
        );
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "alabel", Assembler.opcodes.get("nfix"), 0L, null, null, null, 4),
                patchedAssembly.instructions.get(6)
        );
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ajw"), 6L, null, null, null, 4),
                patchedAssembly.instructions.get(7)
        );
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldlp"), 0L, null, null, null, 5),
                patchedAssembly.instructions.get(8)
        );
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "jump", Assembler.opcodes.get("j"), null, "~configuration", null, null, 7),
                patchedAssembly.instructions.get(9)
        );
        checkInstructionEquality(
                new Instruction(InstructionType.INSTRUCTION, "end", Assembler.opcodes.get("ldc"), 0L, null, null, null, 8),
                patchedAssembly.instructions.get(10)
        );

        assertEquals(new Long(0), patchedAssembly.labelMap.get("init"));
        assertEquals(new Long(2), patchedAssembly.labelMap.get("space"));
        assertEquals(new Long(5), patchedAssembly.labelMap.get("~configuration"));
        assertEquals(new Long(6), patchedAssembly.labelMap.get("alabel"));
        assertEquals(new Long(9), patchedAssembly.labelMap.get("jump"));
        assertEquals(new Long(17), patchedAssembly.labelMap.get("end"));

    }

    @Test
    public void testProcessIndirectInstructionSingleByte() {
        List<Byte> result = Assembler.processIndirectInstruction(new Instruction(InstructionType.INSTRUCTION, "alabel", Assembler.opcodes.get("add"), null, null, "a comment", null, 5));
        assertEquals(new Byte((byte) 0xF5), result.get(0));
        assertEquals(1, result.size());
    }

    @Test
    public void testProcessIndirectInstructionDoubleByte() {
        List<Byte> result = Assembler.processIndirectInstruction(new Instruction(InstructionType.INSTRUCTION, "alabel", Assembler.opcodes.get("ldpi"), null, null, "a comment", null, 5));
        assertEquals(new Byte((byte) 0x21), result.get(0));
        assertEquals(new Byte((byte) 0xFB), result.get(1));
        assertEquals(2, result.size());
    }

    @Test
    public void testProcessDirectInstructionWithLabelOperand() {

        Map<String, Long> labelMap = new HashMap<>();
        labelMap.put("somelabel", 20L);

        List<Byte> result = Assembler.processDirectInstructionWithLabelOperand(new Instruction(InstructionType.INSTRUCTION, "alabel", Assembler.opcodes.get("cj"), null, "somelabel", "a comment", null, 1), labelMap);

        assertEquals(new Byte((byte) 0x20), result.get(0));
        assertEquals(new Byte((byte) 0x20), result.get(1));
        assertEquals(new Byte((byte) 0x20), result.get(2));
        assertEquals(new Byte((byte) 0x20), result.get(3));
        assertEquals(new Byte((byte) 0x20), result.get(4));
        assertEquals(new Byte((byte) 0x20), result.get(5));
        assertEquals(new Byte((byte) 0x21), result.get(6));
        assertEquals(new Byte((byte) 0xA4), result.get(7));

        assertEquals(8, result.size());
    }

    @Test
    public void testProcessDirectInstructionWithConstantOperand() {
        assertEquals((byte) 0x4A, Assembler.processDirectInstructionWithConstantOperand(new Instruction(InstructionType.INSTRUCTION, null, Assembler.opcodes.get("ldc"), 10L, null, null, null, 1)));
    }

    private void checkInstructionEquality(Instruction expected, Instruction actual) {
        assertEquals(expected.comment, actual.comment);
        assertEquals(expected.constantOperand, actual.constantOperand);
        assertEquals(expected.directive, actual.directive);
        assertEquals(expected.label, actual.label);
        assertEquals(expected.labelOperand, actual.labelOperand);
        assertEquals(expected.opcode, actual.opcode);
        assertEquals(expected.originalLine, actual.originalLine);
        assertEquals(expected.type, actual.type);
    }

    private AssemblerConfig getSampleConfig() {
        IOPin ioPin = new IOPin();
        ioPin.setAddr(70);
        ioPin.setChannel("sensor");
        ioPin.setConfig(0);
        Connection connection = new Connection();
        connection.setTarget_processor(1);
        connection.setDest_port(0);
        connection.setChannel("channel0");
        Processor processor = new Processor();
        processor.setProcessor_id(0);
        processor.setIopins(Collections.singletonList(ioPin));
        processor.setConnections(Collections.singletonList(connection));
        AssemblerConfig config = new AssemblerConfig();
        config.setProcessor(processor);
        return config;
    }
}
