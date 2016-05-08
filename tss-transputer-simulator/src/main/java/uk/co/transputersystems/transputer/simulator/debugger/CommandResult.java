package uk.co.transputersystems.transputer.simulator.debugger;

public enum CommandResult {
    REMAIN,         // Do not step to the next instruction
    STEP,           // Step to the next instruction
    CONTINUE,       // Continue to execute without stopping
    NOT_RECOGNISED  // Command was not recognised
}
