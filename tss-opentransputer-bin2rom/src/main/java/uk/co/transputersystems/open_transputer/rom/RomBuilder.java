package uk.co.transputersystems.open_transputer.rom;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RomBuilder {
    public static void run(File input, File output) throws Exception {

        FileReader inputStream = new FileReader(input);
        Scanner inputScanner = new Scanner(inputStream);

        // Skip over 'Start address: XXXXXXXX'
        inputScanner.nextLine();

        // Parse input
        List<Integer> values = new ArrayList<>();
        Integer currentVal = 0;
        int bCount = 0;
        while (inputScanner.hasNextInt(16)) {
            currentVal = currentVal | ((inputScanner.nextInt(16) << (8 * bCount)) & (0xFF << (8 * bCount)));
            bCount++;

            if (bCount == 4) {
                values.add(currentVal);
                bCount = 0;
                currentVal = 0;
            }
        }
        if (bCount != 0) {
            values.add(currentVal);
        }

        if (values.size() > (2592 - 1297 - 2 - 9)) {
            throw new OutOfMemoryError("Program is too big!");
        }

        // Generate output

        FileWriter wr = new FileWriter(output);
        PrintWriter printer = new PrintWriter(wr);

        // Header
        /*
         * memory_initialization_radix=16;
         * memory_initialization_vector=
         */
        printer.println("memory_initialization_radix=16;");
        printer.println("memory_initialization_vector=");

        // 9 lines of 00000000
        for (int i = 0; i < 9; i++) {
            printer.println("00000000,");
        }

        // 2 lines of 80000000
        for (int i = 0; i < 2; i++) {
            printer.println("80000000,");
        }

        // 1,297 lines of 00000000
        for (int i = 0; i < 1297; i++) {
            printer.println("00000000,");
        }

        // Program code padded out to 2592 lines
        for (int i = 0; i < values.size(); i++) {
            printer.printf("%08x,\n", values.get(i));
        }
        for (int i = values.size() + 1297 + 2 + 9; i < 2592; i++) {
            printer.println("00000000,");
        }

        wr.close();
    }
}
