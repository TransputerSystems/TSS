package uk.co.transputersystems.transputer.disassembler;

import javax.annotation.Nonnull;

public class AssemblyInstruction {
    public final Opcode opcode;
    public final Long constantOperand;
    public final long firstByteNumber;
    public final long sizeBytes;

    public AssemblyInstruction(@Nonnull Opcode opcode, Long constantOperand, long firstByteNumber, long sizeBytes) {
        this.opcode = opcode;
        this.constantOperand = constantOperand;
        this.firstByteNumber = firstByteNumber;
        this.sizeBytes = sizeBytes;
    }
}
