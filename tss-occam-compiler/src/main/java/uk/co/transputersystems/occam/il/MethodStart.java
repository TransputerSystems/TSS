package uk.co.transputersystems.occam.il;

import java.util.Collections;

/**
 * Created by Ed on 30/03/2016.
 */
public class MethodStart<TIdentifier>  extends ILOp<TIdentifier> {

    public MethodStart(TIdentifier id, String comment) {
        super(id, "MethodStart", comment);
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
        return visitor.visitMethodStart(this, ctx);
    }
}
