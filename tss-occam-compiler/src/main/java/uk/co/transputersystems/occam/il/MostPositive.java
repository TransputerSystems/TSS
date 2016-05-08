package uk.co.transputersystems.occam.il;

import java.util.Collections;


public class MostPositive<TIdentifier> extends ILOp<TIdentifier> {

    public final String type;

    public MostPositive(TIdentifier id, String type, String comment) {
        super(id, "MostPositive", comment + ", " + type );
        this.type = type;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Collections.singleton(type);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitMostPositive(this, ctx);
    }
}
