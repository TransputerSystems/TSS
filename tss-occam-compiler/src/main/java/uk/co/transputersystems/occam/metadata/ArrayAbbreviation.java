package uk.co.transputersystems.occam.metadata;

public class ArrayAbbreviation extends Abbreviation {

    //TODO: ARGH! The index of an array abbreviation can be a runtime variable (e.g. counter inside a loop)
    //  So index should resolved to a constant if possible
    //  Otherwise we will need to store a NamedOperand (or expression???) here to act as the index
    protected int index;

    public ArrayAbbreviation(Scope declaringScope, String name, String typeName, String hideName, int index) {
        super(declaringScope, name, typeName, hideName);

        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        //TODO: Array abbreviation verification
    }
}
