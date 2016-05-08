package uk.co.transputersystems.transputer.utils;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InteractivePrefix {
    public static void main(String[] args) {
        long value = 0;
        boolean exit = false;

        System.out.print(String.format("%64s", Long.toBinaryString(value)).replace(" ", "0"));
        System.out.printf("(%d)\n", value);

        Scanner input = new Scanner(System.in);
        String inputPattern = "^(?<exit>exit)|(((?<pfix>pfix)|(?<nfix>nfix)|(?<operand>op))\\h+(?<num>[0-9]+))$";
        Pattern p = Pattern.compile(inputPattern);

        while (!exit) {
            String inputLine = input.nextLine();
            Matcher m = p.matcher(inputLine);
            if (m.matches()) {
                if (m.group("exit") != null) {
                    exit = true;
                    continue;
                }
                Long maybeNumber = tryParse(m.group("num"));
                if (maybeNumber == null) {
                    System.out.println("Number could not be parsed.");
                    continue;
                }
                if (maybeNumber < 0x0 || maybeNumber > 0xF ) {
                    System.out.println("Number must be between 0x0 and 0xF.");
                    continue;
                }
                if (m.group("pfix") != null) {
                    value = (value | (maybeNumber & 0xF)) << 4;
                } else if (m.group("nfix") != null) {
                    value = (~(value | (maybeNumber & 0xF))) << 4;
                } else if (m.group("operand") != null) {
                    value = value | (maybeNumber & 0xF);
                } else {
                    System.out.println("Instruction could not be parsed.");
                    continue;
                }
            }
            System.out.print(String.format("%64s", Long.toBinaryString(value)).replace(" ", "0"));
            System.out.printf("(%d)\n", value);
        }
    }

    /**
     * Attempt to parse an integer from a supplied string.
     *
     * @return a `Long` if the parse is successful, else `null`
     */
    @Nullable
    private static Long tryParse(String str) {
        Long val;
        if (str == null) {
            return null;
        }
        try {
            val = Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
        return val;
    }
}
