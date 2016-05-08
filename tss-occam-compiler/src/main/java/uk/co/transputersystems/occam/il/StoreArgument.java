package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class StoreArgument<TIdentifier> extends ILOp<TIdentifier> {

    //TODO: Document this IL op in the specification

    public int index;

    public StoreArgument(TIdentifier id, int index, String comment) {
        super(id, "StoreArgument", comment);

        this.index = index;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Collections.singletonList(String.valueOf(index));
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitStoreArgument(this, ctx);
    }

}
