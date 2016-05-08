package uk.co.transputersystems.transputer.assembler;

public class DuplicateLabelException extends Exception {
    public DuplicateLabelException(String message) {
        super(message);
    }
}
