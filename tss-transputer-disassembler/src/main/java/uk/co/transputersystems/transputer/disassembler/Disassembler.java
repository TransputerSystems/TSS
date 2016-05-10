package uk.co.transputersystems.transputer.disassembler;

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Disassembler {
    public static Map<Byte, Opcode> directOpcodes = new HashMap<>();
    public static Map<Long, Opcode> indirectOpcodes = new HashMap<>();

    static {
        directOpcodes.put((byte)0x2, new Opcode(OpcodeType.DIRECT, "pfix", 0x2));
        directOpcodes.put((byte)0x6, new Opcode(OpcodeType.DIRECT, "nfix", 0x6));
        directOpcodes.put((byte)0xF, new Opcode(OpcodeType.DIRECT, "opr", 0xF));
        directOpcodes.put((byte)0x4, new Opcode(OpcodeType.DIRECT, "ldc", 0x4));
        directOpcodes.put((byte)0x7, new Opcode(OpcodeType.DIRECT, "ldl", 0x7));
        directOpcodes.put((byte)0xD, new Opcode(OpcodeType.DIRECT, "stl", 0xD));
        directOpcodes.put((byte)0x1, new Opcode(OpcodeType.DIRECT, "ldlp", 0x1));
        directOpcodes.put((byte)0x8, new Opcode(OpcodeType.DIRECT, "adc", 0x8));
        directOpcodes.put((byte)0xC, new Opcode(OpcodeType.DIRECT, "eqc", 0xC));
        directOpcodes.put((byte)0x0, new Opcode(OpcodeType.DIRECT, "j", 0x0));
        directOpcodes.put((byte)0xA, new Opcode(OpcodeType.DIRECT, "cj", 0xA));
        directOpcodes.put((byte)0x3, new Opcode(OpcodeType.DIRECT, "ldnl", 0x3));
        directOpcodes.put((byte)0xE, new Opcode(OpcodeType.DIRECT, "stnl", 0xE));
        directOpcodes.put((byte)0x5, new Opcode(OpcodeType.DIRECT, "ldnlp", 0x5));
        directOpcodes.put((byte)0x9, new Opcode(OpcodeType.DIRECT, "call", 0x9));
        directOpcodes.put((byte)0xB, new Opcode(OpcodeType.DIRECT, "ajw", 0xB));

        indirectOpcodes.put(0x00L, new Opcode(OpcodeType.INDIRECT, "rev", 0x00));
        indirectOpcodes.put(0x05L, new Opcode(OpcodeType.INDIRECT, "add", 0x05));
        indirectOpcodes.put(0x1FL, new Opcode(OpcodeType.INDIRECT, "rem", 0x1F));
        indirectOpcodes.put(0x0CL, new Opcode(OpcodeType.INDIRECT, "sub", 0x0C));
        indirectOpcodes.put(0x53L, new Opcode(OpcodeType.INDIRECT, "mul", 0x53));
        indirectOpcodes.put(0x2CL, new Opcode(OpcodeType.INDIRECT, "div", 0x2C));
        indirectOpcodes.put(0x46L, new Opcode(OpcodeType.INDIRECT, "and", 0x46));
        indirectOpcodes.put(0x4BL, new Opcode(OpcodeType.INDIRECT, "or", 0x4B));
        indirectOpcodes.put(0x33L, new Opcode(OpcodeType.INDIRECT, "xor", 0x33));
        indirectOpcodes.put(0x32L, new Opcode(OpcodeType.INDIRECT, "not", 0x32));
        indirectOpcodes.put(0x41L, new Opcode(OpcodeType.INDIRECT, "shl", 0x41));
        indirectOpcodes.put(0x40L, new Opcode(OpcodeType.INDIRECT, "shr", 0x40));
        indirectOpcodes.put(0x09L, new Opcode(OpcodeType.INDIRECT, "gt", 0x09));
        indirectOpcodes.put(0x21L, new Opcode(OpcodeType.INDIRECT, "lend", 0x21));
        indirectOpcodes.put(0x02L, new Opcode(OpcodeType.INDIRECT, "bsub", 0x02));
        indirectOpcodes.put(0x0AL, new Opcode(OpcodeType.INDIRECT, "wsub", 0x0A));
        indirectOpcodes.put(0x34L, new Opcode(OpcodeType.INDIRECT, "bcnt", 0x34));
        indirectOpcodes.put(0x3FL, new Opcode(OpcodeType.INDIRECT, "wcnt", 0x3F));
        indirectOpcodes.put(0x1BL, new Opcode(OpcodeType.INDIRECT, "ldpi", 0x1B));
        indirectOpcodes.put(0x4AL, new Opcode(OpcodeType.INDIRECT, "move", 0x4A));
        indirectOpcodes.put(0x07L, new Opcode(OpcodeType.INDIRECT, "in", 0x07));
        indirectOpcodes.put(0x0BL, new Opcode(OpcodeType.INDIRECT, "out", 0x0B));
        indirectOpcodes.put(0x0FL, new Opcode(OpcodeType.INDIRECT, "outword", 0x0F));
        indirectOpcodes.put(0x06L, new Opcode(OpcodeType.INDIRECT, "gcall", 0x06));
        indirectOpcodes.put(0x3CL, new Opcode(OpcodeType.INDIRECT, "gajw", 0x3C));
        indirectOpcodes.put(0x20L, new Opcode(OpcodeType.INDIRECT, "ret", 0x20));
        indirectOpcodes.put(0x0DL, new Opcode(OpcodeType.INDIRECT, "startp", 0x0D));
        indirectOpcodes.put(0x03L, new Opcode(OpcodeType.INDIRECT, "endp", 0x03));
        indirectOpcodes.put(0x39L, new Opcode(OpcodeType.INDIRECT, "runp", 0x39));
        indirectOpcodes.put(0x15L, new Opcode(OpcodeType.INDIRECT, "stopp", 0x15));
        indirectOpcodes.put(0x1EL, new Opcode(OpcodeType.INDIRECT, "ldpri", 0x1E));
        indirectOpcodes.put(0x22L, new Opcode(OpcodeType.INDIRECT, "ldtimer", 0x22));
        indirectOpcodes.put(0x2BL, new Opcode(OpcodeType.INDIRECT, "tin", 0x2B));
        indirectOpcodes.put(0x43L, new Opcode(OpcodeType.INDIRECT, "alt", 0x43));
        indirectOpcodes.put(0x44L, new Opcode(OpcodeType.INDIRECT, "altwt", 0x44));
        indirectOpcodes.put(0x45L, new Opcode(OpcodeType.INDIRECT, "altend", 0x45));
        indirectOpcodes.put(0x4EL, new Opcode(OpcodeType.INDIRECT, "talt", 0x4E));
        indirectOpcodes.put(0x51L, new Opcode(OpcodeType.INDIRECT, "taltwt", 0x51));
        indirectOpcodes.put(0x49L, new Opcode(OpcodeType.INDIRECT, "enbs", 0x49));
        indirectOpcodes.put(0x30L, new Opcode(OpcodeType.INDIRECT, "diss", 0x30));
        indirectOpcodes.put(0x48L, new Opcode(OpcodeType.INDIRECT, "enbc", 0x48));
        indirectOpcodes.put(0x2FL, new Opcode(OpcodeType.INDIRECT, "disc", 0x2F));
        indirectOpcodes.put(0x47L, new Opcode(OpcodeType.INDIRECT, "enbt", 0x47));
        indirectOpcodes.put(0x2EL, new Opcode(OpcodeType.INDIRECT, "dist", 0x2E));
        indirectOpcodes.put(0x12L, new Opcode(OpcodeType.INDIRECT, "resetch", 0x12));
        indirectOpcodes.put(0x18L, new Opcode(OpcodeType.INDIRECT, "sthf", 0x18));
        indirectOpcodes.put(0x1CL, new Opcode(OpcodeType.INDIRECT, "stlf", 0x1C));
        indirectOpcodes.put(0x54L, new Opcode(OpcodeType.INDIRECT, "sttimer", 0x54));
        indirectOpcodes.put(0x50L, new Opcode(OpcodeType.INDIRECT, "sthb", 0x50));
        indirectOpcodes.put(0x17L, new Opcode(OpcodeType.INDIRECT, "stlb", 0x17));
        indirectOpcodes.put(0x3EL, new Opcode(OpcodeType.INDIRECT, "saveh", 0x3E));
        indirectOpcodes.put(0x3DL, new Opcode(OpcodeType.INDIRECT, "savel", 0x3D));
        indirectOpcodes.put(0x42L, new Opcode(OpcodeType.INDIRECT, "mint", 0x42));
        indirectOpcodes.put(0x04L, new Opcode(OpcodeType.INDIRECT, "diff", 0x04));
        indirectOpcodes.put(0x52L, new Opcode(OpcodeType.INDIRECT, "sum", 0x52));
        indirectOpcodes.put(0x13L, new Opcode(OpcodeType.INDIRECT, "csub0", 0x13));
        indirectOpcodes.put(0x4DL, new Opcode(OpcodeType.INDIRECT, "ccnt1", 0x4D));
        indirectOpcodes.put(0x29L, new Opcode(OpcodeType.INDIRECT, "testerr", 0x29));
        indirectOpcodes.put(0x10L, new Opcode(OpcodeType.INDIRECT, "seterr", 0x10));
        indirectOpcodes.put(0x55L, new Opcode(OpcodeType.INDIRECT, "stoperr", 0x55));
        indirectOpcodes.put(0x57L, new Opcode(OpcodeType.INDIRECT, "clrhalterr", 0x57));
        indirectOpcodes.put(0x58L, new Opcode(OpcodeType.INDIRECT, "sethalterr", 0x58));
        indirectOpcodes.put(0x59L, new Opcode(OpcodeType.INDIRECT, "testhalterr", 0x59));
        indirectOpcodes.put(0x08L, new Opcode(OpcodeType.INDIRECT, "confio", 0x08));
    }

    public static List<String> disassemble(@Nonnull String binary, @Nonnull OutputStream logger) {
        String startAddress = Arrays.stream(binary.split("\\R")).findFirst().get();
        List<MachineInstruction> machineInstructions = Arrays.stream(binary.split("\\R"))
                .skip(1)
                .map(Disassembler::parseMachineInstruction)
                .collect(Collectors.toList());
        List<AssemblyInstruction> assemblyInstructions = toAssemblyInstructions(machineInstructions);
        return assemblyInstructions.stream()
                .map(Disassembler::showAssemblyInstruction)
                .collect(Collectors.toList());
    }

    public static MachineInstruction parseMachineInstruction(String rawMachineInstruction) {
        int opcode = Character.digit(rawMachineInstruction.charAt(0), 16);
        int operand = Character.digit(rawMachineInstruction.charAt(1), 16);
        if (opcode < 0) {
            throw new IllegalArgumentException("Invalid opcode in " + rawMachineInstruction);
        }
        if (operand < 0) {
            throw new IllegalArgumentException("Invalid operand in " + rawMachineInstruction);
        }
        return new MachineInstruction((byte)opcode, (byte)operand);
    }

    public static List<AssemblyInstruction> toAssemblyInstructions(List<MachineInstruction> machineInstructions) {
        List<AssemblyInstruction> assemblyInstructions = new ArrayList<>();
        long currentOperand = 0;
        long byteNumber = 0;
        long beginningOfCurrentInstruction = 0;
        for (MachineInstruction machineInstruction : machineInstructions) {
            byteNumber++;
            Opcode directOpcode = directOpcodes.get(machineInstruction.opcode);
            if ("pfix".equals(directOpcode.opcode)) {
                currentOperand = (currentOperand | (machineInstruction.operand & 0xF)) << 4;
            } else if ("nfix".equals(directOpcode.opcode)) {
                currentOperand = (~(currentOperand | (machineInstruction.operand & 0xF))) << 4;
            } else {
                currentOperand = (currentOperand | (machineInstruction.operand & 0xF));
                if ("opr".equals(directOpcode.opcode)) {
                    assemblyInstructions.add(new AssemblyInstruction(indirectOpcodes.get(currentOperand), null, beginningOfCurrentInstruction, byteNumber - beginningOfCurrentInstruction));
                    currentOperand = 0;
                    beginningOfCurrentInstruction = byteNumber;
                } else {
                    assemblyInstructions.add(new AssemblyInstruction(directOpcode, currentOperand, beginningOfCurrentInstruction, byteNumber - beginningOfCurrentInstruction));
                    currentOperand = 0;
                    beginningOfCurrentInstruction = byteNumber;
                }
            }
        }

        return assemblyInstructions;
    }

    public static String showAssemblyInstruction(AssemblyInstruction instruction) {
        return String.format(
                "%5d: %6s %6s     (length: %db)",
                instruction.firstByteNumber,
                instruction.opcode.opcode,
                (instruction.constantOperand == null ? "" : " " + instruction.constantOperand),
                instruction.sizeBytes);
    }
}
