package uk.co.transputersystems.occam.open_transputer;

import java.util.ListIterator;
import java.util.Stack;

/**
 * Created by Ed on 17/04/2016.
 */
public class StackTracker<TIdentifier, E extends StackItem<TIdentifier>> extends Stack<E> {

    public StackTracker() {
        super();
    }
    public StackTracker(ListIterator<E> items){
        super();

        while(items.hasNext()) {
            super.push((E)new StackItem<>(items.next()));
        }
    }

    @Override
    public E push(E item) {
        ListIterator<E> iterator = this.listIterator();
        while(iterator.hasNext()) {
            iterator.next().increaseDepth();
        }
        item.increaseDepth();
        return super.push(item);
    }

    @Override
    public synchronized E pop() throws IndexOutOfBoundsException {
        ListIterator<E> iterator = this.listIterator();
        while(iterator.hasNext()) {
            iterator.next().decreaseDepth();
        }
        return super.pop();
    }
}
