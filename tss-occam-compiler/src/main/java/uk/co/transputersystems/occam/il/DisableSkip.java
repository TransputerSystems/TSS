package uk.co.transputersystems.occam.il;

import java.util.Arrays;

public class DisableSkip<TIdentifier> extends ILOp<TIdentifier> {

    public TIdentifier target, endAlt;

    public DisableSkip(TIdentifier id, TIdentifier target , TIdentifier endAlt ,  String comment) {
        super(id, "DisableSkip", comment);
        this.target = target;
        this.endAlt = endAlt;
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitDisableSkip(this, ctx);
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Arrays.asList(String.valueOf(target), String.valueOf(endAlt));
    }
}