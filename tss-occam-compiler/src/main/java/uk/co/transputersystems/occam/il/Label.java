package uk.co.transputersystems.occam.il;

import java.util.Arrays;

public class Label<TIdentifier> extends ILOp<TIdentifier> {

    public String label;
    public boolean isGlobal;

    private static int labelIncrementer = 0;

    //TODO: Only function names should be using this. Convert everything other usage to Skip ops
    //TODO: Refactor this class to be called FunctionName

    public Label(TIdentifier id, String label, boolean isGlobal, String comment) {
        super(id, "Label", comment);
        this.label = label;
        this.isGlobal = isGlobal;
    }

    /*
        When generating a label with no specified name
        One will be automatically generated using an auto incrementer
        E.g L0, L1, L2, ....
    */
    public Label(TIdentifier id, boolean isGlobal, String comment){
        this(id, "L" + labelIncrementer++, isGlobal, comment);
    }

    @Override
    public Integer getEncodedSize() {
        //TODO: Set encoded size properly
        return 1;
    }

    @Override
    public Iterable<String> getArgs() {
        return Arrays.asList(label, Boolean.toString(isGlobal));
    }

    @Override
    public <TReturn, TContext> TReturn accept(ILOpVisitor<? extends TReturn, TIdentifier, TContext> visitor, TContext ctx) {
        return visitor.visitLabel(this, ctx);
    }
}
