package uk.co.transputersystems.occam.open_transputer.assembly;

public class Ldc extends ASMOp {

    public String valueExpression = null;

    public Ldc(int data) {
        super(data, "ldc");
    }
    public Ldc(long data) {
        super(data, "ldc");
    }

    public Ldc(String valueExpression) {
        super(valueExpression, "ldc");
    }
}
