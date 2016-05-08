package uk.co.transputersystems.occam.metadata;

import java.util.List;

public class ArrayDataType extends DataType {

    private String elementTypeName;
    private List<Integer> dimensions;

    public ArrayDataType(String name, String elementTypeName, List<Integer> dimensions) {
        super(name);

        this.elementTypeName = elementTypeName;
        this.dimensions = dimensions;
    }

    public String getElementTypeName() {
        return elementTypeName;
    }

    public List<Integer> getDimensions() {
        return dimensions;
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        assert (elementTypeName != null);
        assert (!elementTypeName.isEmpty());
        assert (ctx.isDataTypeKnown(elementTypeName));

        assert (dimensions.size() > 0);

    }
}
