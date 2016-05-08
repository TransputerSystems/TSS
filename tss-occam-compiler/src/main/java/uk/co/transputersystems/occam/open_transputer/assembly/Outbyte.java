package uk.co.transputersystems.occam.open_transputer.assembly;

public class Outbyte extends ASMOp {
    public Outbyte(byte data) {
        super(data, "outbyte");
    }
}
