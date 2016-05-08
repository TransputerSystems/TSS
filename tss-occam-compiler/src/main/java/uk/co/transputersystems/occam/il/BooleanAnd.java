package uk.co.transputersystems.occam.il;


import java.util.Collections;

public class BooleanAnd<TIdentifier> extends ILOp<TIdentifier> {

    public BooleanAnd(TIdentifier id, String comment) {
        super(id, "BooleanAnd", comment);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitBooleanAnd(this, ctx);
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
