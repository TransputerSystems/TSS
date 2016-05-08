package uk.co.transputersystems.occam.metadata;

public class NamedDataType extends DataType {

    private String underlyingTypeName;

    public NamedDataType(String name, String underlyingTypeName) {
        super(name);

        this.underlyingTypeName = underlyingTypeName;
    }

    public String getUnderlyingTypeName() {
        return underlyingTypeName;
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        assert (underlyingTypeName != null);
        assert (!underlyingTypeName.isEmpty());
        assert (ctx.isDataTypeKnown(underlyingTypeName));
    }
}
