package uk.co.transputersystems.occam.il;

import java.util.Arrays;

public class LoadChannelRef<TIdentifier> extends ILOp<TIdentifier> {
    public int index;
    public LoadChannelRef(TIdentifier id, int index ,String comment) {
        super(id, "LoadChannelRef", comment);
        this.index = index;
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitLoadChannelRef(this, ctx);
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Arrays.asList(String.valueOf(index));
    }
}