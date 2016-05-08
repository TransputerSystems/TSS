package uk.co.transputersystems.occam.open_transputer.assembly;

public class Prod extends ASMOp {

    //TODO: OpenTransputer doesn't support unchecked multiplication
    //      so we're just going to override use of PROD to being MUL
    //      and hope that nothing blows up in our faces. Haha.
    public Prod() {
        super(null, "mul");
    }
}
