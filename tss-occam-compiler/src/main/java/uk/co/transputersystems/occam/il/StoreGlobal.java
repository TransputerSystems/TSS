package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class StoreGlobal<TIdentifier> extends ILOp<TIdentifier> {

    //TODO: Document this IL op in the specification

    public String globalName;

    public StoreGlobal(TIdentifier id, String globalName, String comment) {
        super(id, "StoreGlobal", comment);

        this.globalName = globalName;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Collections.singletonList(String.valueOf(globalName));
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitStoreGlobal(this, ctx);
    }

}
