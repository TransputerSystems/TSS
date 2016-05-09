package uk.co.transputersystems.transputer.assembler;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

import static uk.co.transputersystems.transputer.assembler.Assembler.assemble;
import static uk.co.transputersystems.transputer.assembler.Assembler.loadConfig;

public class AssemblerApplication extends Application {

    public static void main(String[] args) throws IOException {
        Application.launch(args);
    }

    @Override
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

        OptionSpec<File> configArg = optionParser
                .accepts("config")
                .withRequiredArg()
                .ofType(File.class)
                .describedAs("configuration");

        OptionSpec insertIOConfigurationArg = optionParser.accepts("insert-io-configuration");

        try {
            OptionSet options = optionParser.parse(args);

            File inputFile = options.valueOf(inputArg);
            File outputFile = options.valueOf(outputArg);
            File configFile = options.hasArgument(configArg) ? options.valueOf(configArg) : null;
            FileReader configFileReader = configFile == null ? null : new FileReader(configFile);

            String input = new String(Files.readAllBytes(inputFile.toPath()));

            PrintStream logger = new PrintStream(System.out);

            List<String> output = assemble(input, loadConfig(configFileReader), logger, options.has(insertIOConfigurationArg));

            FileWriter outputWriter = new FileWriter(outputFile);
            for (String line : output) {
                outputWriter.append(line);
                outputWriter.append('\n');
            }
            outputWriter.close();

        } catch (OptionException e) {
            optionParser.printHelpOn(System.out);
        }
        Platform.exit();
    }
}
