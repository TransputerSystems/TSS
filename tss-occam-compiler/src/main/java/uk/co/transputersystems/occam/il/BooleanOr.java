package uk.co.transputersystems.occam.il;


import java.util.Collections;

public class BooleanOr<TIdentifier> extends ILOp<TIdentifier> {

    public BooleanOr(TIdentifier id, String comment) {
        super(id, "BooleanOr", comment);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitBooleanOr(this, ctx);
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
