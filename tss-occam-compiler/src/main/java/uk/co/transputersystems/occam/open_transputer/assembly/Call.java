package uk.co.transputersystems.occam.open_transputer.assembly;


public class Call extends ASMOp {
    public Call(String targetName) {
        super(targetName, "call");
    }
}
