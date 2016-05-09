package uk.co.transputersystems.open_transputer.rom;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.*;
public class RomBuilderApplication extends Application {
    public void start(Stage primaryStage) throws Exception {
        String[] args = new String[getParameters().getRaw().size()];
        args = getParameters().getRaw().toArray(args);

        OptionParser optionParser = new OptionParser();
        OptionSpec<File> inputArg = optionParser
                .accepts("input")
                .withRequiredArg()
                .required()
                .ofType(File.class)
                .describedAs("transputer assembly");

        OptionSpec<File> outputArg = optionParser
                .accepts("output")
                .withRequiredArg()
                .required()
                .ofType(File.class)
                .describedAs("transputer object code");

        try {
            OptionSet options = optionParser.parse(args);

            File inputFile = options.valueOf(inputArg);
            File outputFile = options.valueOf(outputArg);

            RomBuilder.run(inputFile, outputFile);

        } catch (OptionException e) {
            optionParser.printHelpOn(System.out);
        }

        Platform.exit();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
