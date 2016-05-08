package uk.co.transputersystems.occam.metadata;

public abstract class NamedOperand<T> implements VerifiableData {
    protected String name;
    protected String typeName;
    protected boolean constant;
    protected T constantValue;

    public NamedOperand(String name, String typeName) {
        this(name, typeName, false, null);
    }
    public NamedOperand(String name, String typeName, boolean constant, T constantValue) {
        this.name = name;
        this.typeName = typeName;
        this.constant = constant;
        this.constantValue = constantValue;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public boolean isConstant() {
        return constant;
    }

    public T getConstantValue() {
        return constantValue;
    }

    public void verify(VerificationContext ctx) {
        assert(name != null);
        assert(!name.isEmpty());

        assert (typeName != null);
        assert (!typeName.isEmpty());
        assert (ctx.isDataTypeKnown(typeName));

        if (constant) {
            assert (constantValue != null);
        }
    }
}
