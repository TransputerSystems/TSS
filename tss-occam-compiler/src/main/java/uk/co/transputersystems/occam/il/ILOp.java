package uk.co.transputersystems.occam.il;

import uk.co.transputersystems.occam.metadata.VerifiableData;
import uk.co.transputersystems.occam.metadata.VerificationContext;

import java.util.ArrayList;
import java.util.List;

public abstract class ILOp<TIdentifier> implements VerifiableData {
    protected final TIdentifier id;
    protected final String opName;
    protected String comment;
    protected boolean requiresLabel = false;
    protected boolean requiresPopAfterLabel = false;
    protected List<Integer> storeResultIndices = new ArrayList<>();

    protected ILOp(TIdentifier id, String opName, String comment) {
        this.id = id;
        this.opName = opName;
        this.comment = comment;
    }

    /**
     * A unique identifier for this ILOp
     */
    public final TIdentifier getId() {
        return this.id;
    };

    /**
     * @return A human-readable IL op type
     */
    public final String getOpName() {
        return this.opName;
    };

    /**
     * @return A human-readable comment about this `ILOp`.
     */
    public final String getComment() {
        return this.comment;
    };

    public final void setComment(String value) {
        this.comment = value;
    };

    /**
     * @return A list of static args to this `ILOp`.
     */
    public abstract Iterable<String> getArgs();

    /**
     * @return The size of this `ILOp`, in bytes, when encoded.
     */
    public abstract Integer getEncodedSize();

    public boolean getRequiresLabel() {
        return requiresLabel;
    }
    public void setRequiresLabel(boolean value) {
        requiresLabel = value;
    }

    public boolean getRequiresPopAfterLabel() {
        return requiresPopAfterLabel;
    }
    public void setRequiresPopAfterLabel(boolean value) {
        requiresPopAfterLabel = value;
    }

    public boolean storeResult(int index) {
        return storeResultIndices.contains(index);
    }
    public void setStoreResult(int index) {
        storeResultIndices.add(index);
    }

    public abstract <TReturn,TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx);

    public void verify(VerificationContext ctx) {
        //TODO: This method should be abstract in future
        //TODO: Verify all IL ops - should implement in all extension classes
    }
}
