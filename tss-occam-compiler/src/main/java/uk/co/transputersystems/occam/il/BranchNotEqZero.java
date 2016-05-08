package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class BranchNotEqZero<TIdentifier> extends ILOp<TIdentifier> {

    public final TIdentifier target;

    public BranchNotEqZero(TIdentifier id, TIdentifier branch, String comment) {
        super(id, "BranchIfNotEqualsZero", comment);
        this.target = branch;
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
        return visitor.visitBranchNotEqZero(this, ctx);
    }
}
