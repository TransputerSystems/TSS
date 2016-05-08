package uk.co.transputersystems.occam.metadata;

public abstract class DataType implements VerifiableData {

    protected String name;

    public DataType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void verify(VerificationContext ctx) {
        assert(name != null);
        assert(!name.isEmpty());
    }
}
