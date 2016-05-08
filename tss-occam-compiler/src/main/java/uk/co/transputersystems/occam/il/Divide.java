package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class Divide<TIdentifier> extends ILOp<TIdentifier> {

    public Divide(TIdentifier id, String comment) {
        super(id, "Divide", comment);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitDivide(this, ctx);
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
