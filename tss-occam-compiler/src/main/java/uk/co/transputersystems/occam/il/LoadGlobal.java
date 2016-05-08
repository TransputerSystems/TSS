package uk.co.transputersystems.occam.il;

import java.util.Arrays;

public class LoadGlobal<TIdentifier> extends ILOp<TIdentifier> {

    //TODO: Document this IL op in the specification

    public String globalName;
    public boolean loadAddress;

    public LoadGlobal(TIdentifier id, String globalName, String comment, boolean loadAddress) {
        super(id, "LoadGlobal", comment);
        this.globalName = globalName;
        this.loadAddress = loadAddress;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Arrays.asList(globalName, String.valueOf(loadAddress));
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitLoadGlobal(this, ctx);
    }

}
