package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class BitwiseNot<TIdentifier> extends ILOp<TIdentifier> {
    public BitwiseNot(TIdentifier id, String comment) {
        super(id, "BitwiseNot", comment);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitBitwiseNot(this, ctx);
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
