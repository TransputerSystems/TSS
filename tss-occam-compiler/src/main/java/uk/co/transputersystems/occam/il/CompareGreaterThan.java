package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class CompareGreaterThan<TIdentifier> extends ILOp<TIdentifier> {
    public CompareGreaterThan(TIdentifier id, String comment) {
        super(id, "CompareGreaterThan", comment);
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
        return visitor.visitCompareGreaterThan(this, ctx);
    }
}
