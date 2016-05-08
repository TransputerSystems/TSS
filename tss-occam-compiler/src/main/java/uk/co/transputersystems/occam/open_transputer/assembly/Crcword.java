package uk.co.transputersystems.occam.open_transputer.assembly;

public class Crcword extends ASMOp {
    public Crcword(byte data) {
        super(data, "crcword");
    }
}
