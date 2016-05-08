package uk.co.transputersystems.occam.metadata;

public class Field implements VerifiableData {
    private String name;
    private String typeName;
    private int index;

    public Field(String name, String typeName, int index) {
        this.name = name;
        this.typeName = typeName;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getIndex() {
        return index;
    }

    public void verify(VerificationContext ctx) {
        assert(name != null);
        assert(!name.isEmpty());

        assert(typeName != null);
        assert(!typeName.isEmpty());
        assert (ctx.isDataTypeKnown(typeName));

        assert(index > -1);
    }
}
