package uk.co.transputersystems.occam.open_transputer;

/**
 * Created by Ed on 31/03/2016.
 */
public class StackBranch<TIdentifier, TItem extends StackItem<TIdentifier>> {
    public int from = 0;

    private StackTracker<TIdentifier, TItem> tracker = new StackTracker<>();

    public TItem pushToEvaluationStack(TItem item) {
        return tracker.push(item);
    }
    public TItem popFromEvaluationStack() {
        return tracker.pop();
    }

    public int getStackSize() {
        return tracker.size();
    }

    public StackBranch<TIdentifier, TItem> fork(int from) {
        StackBranch<TIdentifier, TItem> result = new StackBranch<TIdentifier, TItem>();
        result.tracker = new StackTracker<>(tracker.listIterator());
        result.from = from;
        return result;
    }
}
