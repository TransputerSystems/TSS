package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class LoadConstant<TIdentifier> extends ILOp<TIdentifier> {
    //TODO: Document this IL op in the specification

    public String value;

    public LoadConstant(TIdentifier id, String value, String comment) {
        super(id, "LoadConstant", comment);
        this.value = value;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Collections.singletonList(value);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitLoadConstant(this, ctx);
    }
}
