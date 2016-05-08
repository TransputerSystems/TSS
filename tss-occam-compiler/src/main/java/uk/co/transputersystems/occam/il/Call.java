package uk.co.transputersystems.occam.il;

import java.util.Collections;

/**
 * Created by Ed on 16/04/2016.
 */
public class Call<TIdentifier> extends ILOp<TIdentifier> {

    public final String functionName;

    public Call(TIdentifier id, String functionName,  String comment) {
        super(id, "Call", comment);
        this.functionName = functionName;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Collections.singletonList(functionName);
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitCall(this, ctx);
    }
}