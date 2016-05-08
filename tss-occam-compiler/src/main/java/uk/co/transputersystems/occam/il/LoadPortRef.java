package uk.co.transputersystems.occam.il;

import java.util.Arrays;

public class LoadPortRef <TIdentifier> extends ILOp<TIdentifier> {


    public int index;

    public LoadPortRef(TIdentifier id, int index, String comment) {
        super(id, "LoadPortRef", comment);
        this.index = index;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Arrays.asList(String.valueOf(index));
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitLoadPortRef(this, ctx);
    }

}
