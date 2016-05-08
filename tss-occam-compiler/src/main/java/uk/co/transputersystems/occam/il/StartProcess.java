package uk.co.transputersystems.occam.il;

import java.util.Arrays;

public class StartProcess<TIdentifier> extends ILOp<TIdentifier> {
    public TIdentifier firstILOpID;
    public ProcessPriorities newPriority;

    public StartProcess(TIdentifier id, TIdentifier creatorILOpID, ProcessPriorities newPriority, String comment) {
        super(id, "StartProcess", comment);

        this.firstILOpID = creatorILOpID;
        this.newPriority = newPriority;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Arrays.asList(firstILOpID.toString(), newPriority.name());
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitStartProcess(this, ctx);
    }
}
