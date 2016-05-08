package uk.co.transputersystems.occam.metadata;

public class Channel extends NamedOperand {
    int index;
    public Channel(String name, int index , String typeName ) {
        super(name, typeName);
        this.index = index;
    }

    public int getIndex(){
        return index;
    }
}
