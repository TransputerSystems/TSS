package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class ReadTimer<TIdentifier> extends ILOp<TIdentifier> {

    public ReadTimer(TIdentifier id, String comment) {
        super(id, "ReadTimer", comment);
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
        return visitor.visitReadTimer(this, ctx);
    }

}

