package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class EnableSkip <TIdentifier> extends ILOp<TIdentifier> {

    public EnableSkip(TIdentifier id, String comment) {
        super(id, "EnableSkip", comment);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitEnableSkip(this, ctx);
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