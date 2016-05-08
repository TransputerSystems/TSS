package uk.co.transputersystems.occam.il;


import java.util.Collections;

public class UnaryMinus<TIdentifier> extends ILOp<TIdentifier> {


    public final boolean ignore_overflow;

    public UnaryMinus(TIdentifier id, boolean ignore_overflow, String comment) {
        super(id, "Unary minus", comment + ", " + (ignore_overflow ? "ignore_overflow" : "allow_underflow"  ));
        this.ignore_overflow = ignore_overflow;
    }

    @Override
    public Iterable<String> getArgs() {
        return Collections.emptyList();
    }

    @Override
    public Integer getEncodedSize() {
        return 1;
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitUnaryMinus(this, ctx);
    }
}
