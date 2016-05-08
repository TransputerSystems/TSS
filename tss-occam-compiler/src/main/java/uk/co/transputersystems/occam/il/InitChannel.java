package uk.co.transputersystems.occam.il;

import java.util.Arrays;

public class InitChannel<TIdentifier> extends ILOp<TIdentifier> {
    public String name;
    public String typeName;
    public int index ;
    public InitChannel(TIdentifier id, String name, int index, String typeName,  String comment) {
        super(id, "InitChannel", comment);
        this.name = name;
        this.index = index;
        this.typeName = typeName;
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitInitChannel(this, ctx);
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Arrays.asList(name, String.valueOf(index), typeName);
    }
}