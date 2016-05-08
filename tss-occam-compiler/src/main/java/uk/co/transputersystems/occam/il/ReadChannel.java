package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class ReadChannel<TIdentifier> extends ILOp<TIdentifier> {

    public ReadChannel(TIdentifier id, String comment) {
        super(id, "ReadChannel", comment);
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
        return visitor.visitReadChannel(this, ctx);
    }

}
