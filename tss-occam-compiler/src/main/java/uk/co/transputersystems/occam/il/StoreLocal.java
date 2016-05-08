package uk.co.transputersystems.occam.il;

import java.util.Collections;

public class StoreLocal<TIdentifier> extends ILOp<TIdentifier> {

    //TODO: Document this IL op in the specification

    /*
     * Notes:
     *  - Local index refers to index within the current scope and parent scopes. However, Scopes will need to
     *    be translated into different workspaces by the ASMGenerator. This means that just because a local index
     *    from the current scope is the same as a local index from a horizontally level scope, they will not
     *    necessarily point to the same memory. They might point to the same offset from the workspace pointer.
     *    It may also be necessary to travel up the workspace linked list to reach the memory for a particular local.
     *    Scopes do not directly translate to new workspaces.
     */

    public int index;

    public StoreLocal(TIdentifier id, int index, String comment) {
        super(id, "StoreLocal", comment);

        this.index = index;
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Collections.singletonList(String.valueOf(index));
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitStoreLocal(this, ctx);
    }

}
