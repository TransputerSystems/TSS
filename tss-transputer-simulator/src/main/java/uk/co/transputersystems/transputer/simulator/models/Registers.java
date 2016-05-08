package uk.co.transputersystems.transputer.simulator.models;

/**
 * Created by Edward on 28/02/2016.
 */
public class Registers {
    public int Iptr;
    public int Wptr;
    public int Areg;
    public int Breg;
    public int Creg;
    public int Oreg;

    public Registers(Registers registers) {
        // Copy constructor
        this.Iptr = registers.Iptr;
        this.Wptr = registers.Wptr;
        this.Areg = registers.Areg;
        this.Breg = registers.Breg;
        this.Creg = registers.Creg;
        this.Oreg = registers.Oreg;
    }

    public Registers() {

    }
}
