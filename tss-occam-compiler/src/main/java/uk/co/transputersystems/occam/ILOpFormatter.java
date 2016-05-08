package uk.co.transputersystems.occam;

import uk.co.transputersystems.occam.il.ILBlock;
import uk.co.transputersystems.occam.il.ILOp;

import java.util.Arrays;
import java.util.StringJoiner;

public class ILOpFormatter {
    /**
     * Format the details of an ILOp into a human-readable string.
     * @return A human-readable string representing the ILOp.
     */
    public static String formatOp(ILOp<?> op, ILBlock<?,? extends ILOp<?>> ctx) {
        StringBuilder result = new StringBuilder();

        result
                .append(op.getId().toString())
                .append(" : ")
                .append(op.getOpName());

        result = padRight(result, 55, 1);

        StringJoiner argJoiner = new StringJoiner(", ", "(", ")");
        for (String arg : op.getArgs()) {
            argJoiner.add(arg);
        }

        result.append(argJoiner.toString());

        result = padRight(result, 120, 1);

        result
                .append("// ")
                .append(op.getComment());

        return result.toString();
    }

    /**
     * Given a `StringBuilder`, pad it with spaces until it has at least `minLength`. If no padding it required, add at
     * least `minPadding` spaces.
     * @return The padded `StringBuilder`.
     */
    private static StringBuilder padRight(StringBuilder sb, int minLength, int minPadding) {
        char[] padding = new char[minLength - sb.length() > 0 ? minLength - sb.length() : minPadding];
        Arrays.fill(padding, ' ');
        return sb.append(padding);
    }
}
