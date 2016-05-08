package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class WritePort<TIdentifier> extends ILOp<TIdentifier> {



    public WritePort(TIdentifier id,  String comment) {
        super(id, "WritePort", comment);
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
        return visitor.visitWritePort(this, ctx);
    }

}

