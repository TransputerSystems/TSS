package uk.co.transputersystems.occam.open_transputer;

import uk.co.transputersystems.occam.open_transputer.assembly.ASMOp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ed on 31/03/2016.
 */
public class ASMBlock {
    private List<ASMOp> ops = new ArrayList<>();

    public ASMBlock() {

    }

    public List<ASMOp> getOps() {
        return ops;
    }
    public void addOp(ASMOp op) {
        ops.add(op);
    }
    public void addOps(List<ASMOp> opsToAdd) {
        ops.addAll(opsToAdd);
    }
}
