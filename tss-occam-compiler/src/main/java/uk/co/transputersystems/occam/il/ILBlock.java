package uk.co.transputersystems.occam.il;

import uk.co.transputersystems.occam.ILOpFormatter;
import uk.co.transputersystems.occam.metadata.VerificationContext;

import java.util.ArrayList;
import java.util.List;

public class ILBlock<TIdentifier, TILOp extends ILOp<? extends TIdentifier>> {
    private List<TILOp> ilOps = new ArrayList<>();
    /**
     * The Id of the scope to which this ILBlock belongs or -1 to indicate that it doesn't matter.
     *
     * This value is only relevant for the top-most ILBlock which represents the container of all the ILOps for a particular procedure.
     */
    private int scopeId = -1;

    private boolean functionBlock = false;

    public ILBlock() {
    }

    public ILBlock(TILOp op) {
        ilOps.add(op);
    }

    public ILBlock(int scopeId, boolean functionBlock) {
        this.functionBlock = functionBlock;
        this.scopeId = scopeId;
    }

    public int getScopeId() {
        return scopeId;
    }
    public boolean isFunctionBlock() {
        return functionBlock;
    }

    public TILOp get(int index) {
        return ilOps.get(index);
    }
    public TILOp get(TIdentifier id) {
        for (TILOp op : ilOps) {
            if (op.getId().equals(id)) {
                return op;
            }
        }
        return null;
    }

    public void add(int position, TILOp op){
        ilOps.add(position, op);
    }

    public void add(TILOp op) {
        ilOps.add(op);
    }
    public void addAll(List<TILOp> ops) {
        ilOps.addAll(ops);
    }
    public void appendBlock(ILBlock<TIdentifier,TILOp> block) {
        addAll(block.getAll());
    }
    public void appendBlockList(Iterable<ILBlock<TIdentifier,TILOp>> blocks){
        for(ILBlock<TIdentifier,TILOp> ilBlock : blocks){
            this.appendBlock(ilBlock);
        }
    }
    public List<TILOp> getAll() {
        return new ArrayList<>(ilOps);
    }

    public List<ILBlock<TIdentifier,TILOp>> mergeBlockList(Iterable<ILBlock<TIdentifier,TILOp>> blocks) {
        List<ILBlock<TIdentifier,TILOp>> result = new ArrayList<>();
        for(ILBlock<TIdentifier,TILOp> ilBlock : blocks){
            if (ilBlock.isFunctionBlock()) {
                result.add(ilBlock);
            } else {
                this.appendBlock(ilBlock);
            }
        }
        result.add(this);
        return result;
    }

    /**
     * Gets the offset, in bytes, of the specified IL op from the start of the block.
     * @param opId The Id of the operation to get the offset of.
     * @return The offset, in bytes, from the start of the block.
     */
    public Integer getOffset(TIdentifier opId) {
        int offset = 0;
        for (TILOp op : ilOps) {
            if (op.getId().equals(opId)) {
                return offset;
            }
            offset += op.getEncodedSize();
        }
        return null;
    }

    public Integer getOffset(ILOp searchOp) {
        int offset = 0;
        for (TILOp op : ilOps) {
            if (op.equals(searchOp)) {
                return offset;
            }
            offset += op.getEncodedSize();
        }
        return null;
    }

    @Override
    public String toString() {
        String result = " [ILBlock scope: " + (scopeId == -1 ? "[PARENT]" : Integer.toString(scopeId)) + "]\n";
        for (TILOp ilOp : ilOps) {
            result += ILOpFormatter.formatOp(ilOp, this) + "\n";
        }
        return result;
    }

    public void verify(VerificationContext ctx) {
        int methodStartOps = 0;

        for (ILOp ilOp : ilOps) {
            if (ilOp instanceof MethodStart) {
                methodStartOps++;
            }
            ilOp.verify(ctx);
        }

        assert (methodStartOps == (isFunctionBlock() ? 1 : 0));
    }
}
