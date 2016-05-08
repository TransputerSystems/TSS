package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class WriteChannel<TIdentifier> extends ILOp<TIdentifier> {

    public WriteChannel(TIdentifier id,  String comment) {
        super(id, "WriteChannel", comment);
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
        return visitor.visitWriteChannel(this, ctx);
    }

}
