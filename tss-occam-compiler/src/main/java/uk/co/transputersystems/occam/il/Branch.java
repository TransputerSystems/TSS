package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class Branch<TIdentifier> extends ILOp<TIdentifier> {

    public final TIdentifier target;

    public Branch(TIdentifier id, TIdentifier target, String comment) {
        super(id, "Branch", comment);
        this.target = target;
    }

    @Override
    public Iterable<String> getArgs() {
        return Collections.singletonList(target.toString());
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitBranch(this, ctx);
    }
}
