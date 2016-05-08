package uk.co.transputersystems.occam.open_transputer.assembly;

public class Gcall extends ASMOp {
    public Gcall(byte data) {
        super(data, "gcall");
    }
}
