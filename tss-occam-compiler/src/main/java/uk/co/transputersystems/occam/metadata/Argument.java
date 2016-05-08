package uk.co.transputersystems.occam.metadata;

public class Argument extends NamedOperand {
    private int index;
    private int workspaceOffset;
    private boolean passByValue;

    public Argument(String name, String typeName, boolean passByValue) {
        super(name, typeName);

        this.index = -1;
        this.passByValue = passByValue;
    }

    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }

    public boolean getPassByValue() {
        return passByValue;
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        assert(index > -1);
    }
}
