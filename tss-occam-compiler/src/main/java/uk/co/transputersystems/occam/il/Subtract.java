package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class Subtract<TIdentifier> extends ILOp<TIdentifier> {

    public final boolean ignore_overflow;

    public Subtract(TIdentifier id, boolean ignore_overflow, String comment) {
        super(id, "Subtract", comment + ", " + (ignore_overflow ? "ignore_overflow" : "allow_underflow"));
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
        return visitor.visitSubtract(this, ctx);
    }
}
