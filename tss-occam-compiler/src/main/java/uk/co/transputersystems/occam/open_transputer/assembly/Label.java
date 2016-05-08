package uk.co.transputersystems.occam.open_transputer.assembly;

/**
 * Create a label marker
 */
public class Label extends ASMOp {
    public String id;
    public Label ( int id) {
        super("", id+":");
        this.id = "L" + Integer.toString(id);
    }
    public Label ( String id ) {
        super("", id+":");
        this.id = id;
    }
}
