package uk.co.transputersystems.occam.open_transputer.assembly;

public abstract class ASMOp<TData> {
    final public TData data;
    final public String name;

    public ASMOp(TData data, String name) {
        this.data = data;
        this.name = name;
    }
}
