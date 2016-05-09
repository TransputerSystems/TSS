package uk.co.transputersystems.transputer.disassembler;

import javax.annotation.Nonnull;

public class AssemblyInstruction {
    public final Opcode opcode;
    public final Long constantOperand;
    public final long originalLine;

    public AssemblyInstruction(@Nonnull Opcode opcode, Long constantOperand, long originalLine) {
        this.opcode = opcode;
        this.constantOperand = constantOperand;
        this.originalLine = originalLine;
    }
}
