package uk.co.transputersystems.occam.open_transputer;

/**
 * Created by Ed on 17/04/2016.
 */
public class StackItem<TIdentifier> {
    private int maxDepth;
    private int depth;
    public final int index;

    public final TIdentifier createdAt;

    public StackItem(TIdentifier createdAt, int index) {
        this.createdAt = createdAt;
        this.index = index;
    }
    public StackItem(StackItem<TIdentifier> original) {
        this.createdAt = original.createdAt;
        this.index = original.index;
        this.depth = original.depth;
        this.maxDepth = original.maxDepth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void increaseDepth() {
        depth++;
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }
    public void decreaseDepth() throws IndexOutOfBoundsException {
        depth--;
        if (depth < 0) {
            throw new IndexOutOfBoundsException("Stack item depth < 0!");
        }
    }
}
