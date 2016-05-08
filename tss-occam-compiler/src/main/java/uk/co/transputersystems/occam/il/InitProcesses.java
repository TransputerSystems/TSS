package uk.co.transputersystems.occam.il;

import java.util.Arrays;
import java.util.List;

/**
 * Marks the beginning of a series of `StartProcess` operations, defining the:
 * * number of processes that will exist after all processes are started
 * * a location to continue executing from once the processes are started
 * * the IDs of the workspaces owned by each process
 */
public class InitProcesses<TIdentifier> extends ILOp<TIdentifier> {
    public int numProcesses;
    public TIdentifier continueILOpID;
    public List<Integer> workspaceIds;

    public InitProcesses(TIdentifier id, int numProcesses, TIdentifier continueILOpID, List<Integer> workspaceIds, String comment) {
        super(id, "InitProcesses", comment);

        this.numProcesses = numProcesses;
        this.continueILOpID = continueILOpID;
        this.workspaceIds = workspaceIds;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Arrays.asList(Integer.toString(numProcesses), continueILOpID.toString(), workspaceIds.toString());
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitInitProcesses(this, ctx);
    }
}
