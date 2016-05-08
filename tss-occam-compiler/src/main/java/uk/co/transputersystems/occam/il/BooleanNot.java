package uk.co.transputersystems.occam.il;


import java.util.Collections;

public class BooleanNot<TIdentifier> extends ILOp<TIdentifier> {

    public BooleanNot(TIdentifier id, String comment) {
        super(id, "BooleanNot", comment);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitBooleanNot(this, ctx);
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
