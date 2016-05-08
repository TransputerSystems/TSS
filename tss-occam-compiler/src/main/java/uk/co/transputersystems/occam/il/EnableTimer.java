package uk.co.transputersystems.occam.il;

import java.util.Collections;


public class EnableTimer <TIdentifier> extends ILOp<TIdentifier> {

    public EnableTimer(TIdentifier id, String comment) {
        super(id, "EnableTimer", comment);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitEnableTimer(this, ctx);
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Collections.emptyList();
    }
}