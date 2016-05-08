package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class BranchEqZero<TIdentifier> extends ILOp<TIdentifier> {

    public final TIdentifier target;

    public BranchEqZero(TIdentifier id, TIdentifier target, String comment) {
        super(id, "BranchIfEqualsZero", comment);
        this.target = target;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Collections.singletonList(target.toString());
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitBranchEqZero(this, ctx);
    }
}
