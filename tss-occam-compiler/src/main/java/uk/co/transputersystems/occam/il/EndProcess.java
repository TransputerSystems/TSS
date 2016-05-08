package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class EndProcess<TIdentifier> extends ILOp<TIdentifier> {
    public TIdentifier creatorILOpID;

    public EndProcess(TIdentifier id, TIdentifier creatorILOpID, String comment) {
        super(id, "EndProcess", comment);
        this.creatorILOpID = creatorILOpID;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Collections.singletonList(creatorILOpID.toString());
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitEndProcess(this, ctx);
    }
}
