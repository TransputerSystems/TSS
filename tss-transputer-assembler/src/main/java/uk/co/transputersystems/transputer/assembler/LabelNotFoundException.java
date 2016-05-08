package uk.co.transputersystems.transputer.assembler;

import org.antlr.v4.runtime.misc.ParseCancellationException;

public class LabelNotFoundException extends ParseCancellationException {
    public LabelNotFoundException(String message) {
        super(message);
    }
}
