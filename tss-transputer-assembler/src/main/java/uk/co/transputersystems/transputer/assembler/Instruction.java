package uk.co.transputersystems.transputer.assembler;

public class Instruction {
    public final InstructionType type;
    public final String label;
    public final Opcode opcode;
    public final Long constantOperand;
    public final String labelOperand;
    public final String comment;
    public final String directive;
    public final long originalLine;

    public Instruction(InstructionType type, String label, Opcode opcode, Long constantOperand, String labelOperand, String comment, String directive, long originalLine) {
        this.type = type;
        this.label = label;
        this.opcode = opcode;
        this.constantOperand = constantOperand;
        this.labelOperand = labelOperand;
        this.comment = comment;
        this.directive = directive;
        this.originalLine = originalLine;
    }

    @Override
    public String toString() {
        // TODO: clean up
        if (type == InstructionType.INSTRUCTION) {
            return (label == null ? "" : label) + ": " + (opcode == null ? "" : opcode) + " " + (constantOperand == null ? "" : constantOperand) + (labelOperand == null ? "" : labelOperand) + " -- " + (comment == null ? "" : comment);
        } else if (type == InstructionType.DIRECTIVE) {
            return (label == null ? "" : label) + ": " + directive + " -- " + (comment == null ? "" : comment);
        } else {
            return "[Error converting Instruction to String]";
        }
    }
}
