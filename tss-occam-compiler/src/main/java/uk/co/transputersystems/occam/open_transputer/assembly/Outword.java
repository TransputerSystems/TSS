package uk.co.transputersystems.occam.open_transputer.assembly;

public class Outword extends ASMOp {
    public Outword(byte data) {
        super(data, "outword");
    }
}
