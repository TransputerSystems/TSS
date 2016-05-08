package uk.co.transputersystems.occam.il;

import java.util.Arrays;

public class LoadArgument<TIdentifier> extends ILOp<TIdentifier> {
    //TODO: Document this IL op in the specification

    public int index;
    public boolean loadAddress;

    public LoadArgument(TIdentifier id, int index, String comment, boolean loadAddress) {
        super(id, "LoadArgument", comment);
        this.index = index;
        this.loadAddress = loadAddress;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Arrays.asList(String.valueOf(index), String.valueOf(loadAddress));
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitLoadArgument(this, ctx);
    }

}
