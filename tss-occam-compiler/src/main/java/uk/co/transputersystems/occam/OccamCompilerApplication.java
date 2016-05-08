package uk.co.transputersystems.occam;

import uk.co.transputersystems.occam.il.ILBlock;
import uk.co.transputersystems.occam.il.ILOp;
import uk.co.transputersystems.occam.metadata.LibraryInformation;
import uk.co.transputersystems.occam.metadata.VerificationContext;
import uk.co.transputersystems.occam.open_transputer.ASMBlock;
import uk.co.transputersystems.occam.open_transputer.ASMGenerator;
import uk.co.transputersystems.occam.open_transputer.ASMGeneratorContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class OccamCompilerApplication extends Application {

    public static void main(String[] args) throws Exception {
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
                .describedAs("Occam source code");

        OptionSpec<File> outputArg = optionParser
                .accepts("output")
                .withRequiredArg()
                .required()
                .ofType(File.class)
                .describedAs("Transputer assembly");

        OptionSpec<File> outputILArg = optionParser
                .accepts("output-il")
                .withRequiredArg()
                .ofType(File.class)
                .describedAs("Intermediate Language");

        optionParser.accepts("verbose");

        OptionSet options = optionParser.parse(args);

        File inputFile = options.valueOf(inputArg);
        File outputFile = options.valueOf(outputArg);

        OccamCompiler compiler = new OccamCompiler();

        PrintWriter out = new PrintWriter(System.out);

        InputStream inputStream = new FileInputStream(inputFile);

        ErrorListener errorListener = new ErrorListener();
        ParseTree tree = compiler.makeParseTree(inputStream, OccamParser::file_input, errorListener);

        if (options.has("verbose")) {
            compiler.printParseTree(out, tree);
        }

        LibraryInformation libraryInfo = new LibraryInformation("Root Library");
        TSILGenerator tsilGenerator = new TSILGenerator(libraryInfo);
        ReferencedTSILGenerator<UUID> rTsilGenerator = new ReferencedTSILGenerator<>();

        if (options.has("verbose")) {
            out.println(libraryInfo.getName() + ": " + inputFile.getName());
        }

        // Generate IL with UUID identifiers
        List<ILBlock<UUID, ILOp<UUID>>> ilBlocks = compiler.generateTSIL(tsilGenerator, tree, inputFile.getPath(), inputFile.getName());

        // Perform sanity checks
        VerificationContext verificationContext = new VerificationContext(Collections.singletonList(libraryInfo));
        verificationContext.verify(ilBlocks);

        // Convert UUID IL to sequential integer-labelled IL
        List<ILBlock<Integer, ILOp<Integer>>> referencedIlBlocks = compiler.generateReferencedTSIL(rTsilGenerator, ilBlocks);

        try {
            if (options.has("output-il")) {
                File outputILFile = options.valueOf(outputILArg);
                FileWriter outputILWriter = new FileWriter(outputILFile);
                compiler.writeILBlocks(outputILWriter, referencedIlBlocks);
            }

            ASMGeneratorContext asmGeneratorContext = new ASMGeneratorContext(libraryInfo);

            List<ASMBlock> asmBlocks = compiler.generateASM(new ASMGenerator(), referencedIlBlocks, asmGeneratorContext, true);

            if (options.has("verbose")) {
                compiler.writeDivider(out);
                for (ILBlock block : referencedIlBlocks) {
                    out.println(block.toString());
                }
                compiler.writeDivider(out);
                compiler.writeASMBlocks(out, asmBlocks);
                compiler.writeDivider(out);
                out.flush();
            }

            FileWriter outputWriter = new FileWriter(outputFile);
            compiler.writeASMBlocks(outputWriter, asmBlocks);
            outputWriter.flush();
            outputWriter.close();

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        Platform.exit();
    }
}
