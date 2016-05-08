package uk.co.transputersystems.occam.metadata;

public class PrimitiveDataType extends DataType {

    private int size;

    public PrimitiveDataType(String name, int size) {
        super(name);

        this.size = size;
    }

    public int getSize() {
        return size;
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        assert (size >= 0);
    }
}
