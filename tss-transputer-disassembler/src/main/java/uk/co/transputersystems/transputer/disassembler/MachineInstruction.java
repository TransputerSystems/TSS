package uk.co.transputersystems.transputer.disassembler;

public class MachineInstruction {
    public final byte opcode;
    public final byte operand;

    public MachineInstruction(byte opcode, byte operand) {
        this.opcode = opcode;
        this.operand = operand;
    }
}
