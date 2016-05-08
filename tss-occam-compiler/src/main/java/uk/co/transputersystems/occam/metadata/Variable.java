package uk.co.transputersystems.occam.metadata;

public class Variable extends NamedOperand<Object> {
    private int index;

    public Variable(String name, int index, String typeName) {
        super(name, typeName);

        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        assert (index > -1);
    }
}
