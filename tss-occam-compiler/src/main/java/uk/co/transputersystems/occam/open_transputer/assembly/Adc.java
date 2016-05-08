package uk.co.transputersystems.occam.open_transputer.assembly;

/**
 * Add a constant to `Areg`. Checked overflow.
 */
public class Adc extends ASMOp {
    public Adc(int data) {
        super(data, "adc");
    }
}
