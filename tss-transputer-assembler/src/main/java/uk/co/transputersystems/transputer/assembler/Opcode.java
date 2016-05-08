package uk.co.transputersystems.transputer.assembler;

import javax.annotation.Nonnull;

public class Opcode {
    @Nonnull public final OpcodeType type;
    @Nonnull public final String opcode;
    @Nonnull public final int encodesTo;

    public Opcode(@Nonnull OpcodeType type, @Nonnull String opcode, @Nonnull int encodesTo) {
        this.type = type;
        this.opcode = opcode;
        this.encodesTo = encodesTo;
    }

    @Override
    public String toString() {
        return opcode;
    }
}
