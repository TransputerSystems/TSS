package uk.co.transputersystems.occam.metadata;

public class ValueAbbreviation<T> extends Abbreviation<T> {

    public ValueAbbreviation(Scope declaringScope, String name, String typeName, T constantValue) {
        super(declaringScope, name, typeName, null, true, constantValue);
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        assert (hiddenName == null);
        assert (constant == true);
    }
}
