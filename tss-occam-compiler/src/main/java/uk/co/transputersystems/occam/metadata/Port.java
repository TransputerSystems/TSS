package uk.co.transputersystems.occam.metadata;

public class Port extends NamedOperand {
    public int index;
    public Port(String name, int index,  String typeName) {
        super(name, typeName);
    }


    public int getIndex() {
        return index;
    }
}
