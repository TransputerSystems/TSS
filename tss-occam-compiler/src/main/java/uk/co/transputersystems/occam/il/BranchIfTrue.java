package uk.co.transputersystems.occam.il;

import java.util.Collections;


public class BranchIfTrue<TIdentifier> extends ILOp<TIdentifier> {

    public final TIdentifier target;

    public BranchIfTrue(TIdentifier id, TIdentifier target, String comment) {
        super(id, "BranchIfTrue", comment);
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
        return visitor.visitBranchIfTrue(this, ctx);
    }
}
