package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class Add<TIdentifier> extends ILOp<TIdentifier> {

    public final boolean ignore_overflow;

    public Add(TIdentifier id, boolean ignore_overflow,  String comment) {
        super(id, "Add", ignore_overflow ? "ignore_overflow" : "allow_underflow");
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
        return visitor.visitAdd(this, ctx);
    }
}
