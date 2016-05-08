package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class DelayedTimerInput <TIdentifier> extends ILOp<TIdentifier> {

    public DelayedTimerInput(TIdentifier id, String comment) {
        super(id, "DelayedTimerInput", comment);
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

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitDelayedTimerInput(this, ctx);
    }

}