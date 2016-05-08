package uk.co.transputersystems.occam.open_transputer;

import uk.co.transputersystems.occam.StringUtilities;
import uk.co.transputersystems.occam.open_transputer.assembly.ASMOp;

public class ASMOpFormatter {
    /**
     * Format the details of an ILOp into a human-readable string.
     * @return A human-readable string representing the ILOp.
     */
    public static String formatOp(ASMOp op) {
        return StringUtilities.padRight(op.name, 12) + (op.data != null ? op.data.toString() : "");
    }
}
