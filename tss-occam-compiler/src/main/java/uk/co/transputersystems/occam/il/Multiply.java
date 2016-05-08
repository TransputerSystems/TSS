package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class Multiply<TIdentifier> extends ILOp<TIdentifier> {

    public final boolean ignore_overflow;

    public Multiply(TIdentifier id, boolean ignore_overflow, String comment) {
        super(id, "Multiply", comment + ", " + (ignore_overflow ? "ignore_overflow" : "don't ignore overflow"));
        this.ignore_overflow = ignore_overflow;
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
        return visitor.visitMultiply(this, ctx);
    }
}
