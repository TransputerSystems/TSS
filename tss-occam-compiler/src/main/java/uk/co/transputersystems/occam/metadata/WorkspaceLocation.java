package uk.co.transputersystems.occam.metadata;

/**
 * Created by Ed on 31/03/2016.
 */
public class WorkspaceLocation {
    public int offset;
    /** Special meanings:
     *      Integer.MIN_VALUE = Parent workspace pointer
     *      Integer.MAX_VALUE = Temporary value
     *      Otherwise:
     *          -ve = Argument
     *          +ve = Variable
     */
    public int itemIndex;

    public WorkspaceLocation(int offset) {
        this.offset = offset;
        this.itemIndex = Integer.MAX_VALUE;
    }
    public WorkspaceLocation(int offset, int itemIndex) {
        this.offset = offset;
        this.itemIndex = itemIndex;
    }
}
