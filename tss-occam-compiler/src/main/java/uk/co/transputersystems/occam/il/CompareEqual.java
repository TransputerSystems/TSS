package uk.co.transputersystems.occam.il;


import java.util.Collections;

public class CompareEqual<TIdentifier> extends ILOp<TIdentifier> {

    public CompareEqual(TIdentifier id, String comment) {
        super(id, "CompareEqual", comment);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitCompareEqual(this, ctx);
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
}
