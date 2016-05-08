package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class BitwiseXor<TIdentifier> extends ILOp<TIdentifier> {
    public BitwiseXor(TIdentifier id, String comment) {
        super(id, "BitwiseXor", comment);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitBitwiseXor(this, ctx);
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
