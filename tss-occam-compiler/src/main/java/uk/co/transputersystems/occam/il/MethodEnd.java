package uk.co.transputersystems.occam.il;

import java.util.Collections;

/**
 * Created by Ed on 30/03/2016.
 */
public class MethodEnd<TIdentifier>  extends ILOp<TIdentifier> {

    public MethodEnd(TIdentifier id, String comment) {
        super(id, "MethodEnd", comment);
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
        return visitor.visitMethodEnd(this, ctx);
    }
}
