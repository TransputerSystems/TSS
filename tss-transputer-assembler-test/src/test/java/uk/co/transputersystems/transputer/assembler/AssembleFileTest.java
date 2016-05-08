package uk.co.transputersystems.transputer.assembler;

import uk.co.transputersystems.occam.*;
import uk.co.transputersystems.occam.il.ILBlock;
import uk.co.transputersystems.occam.il.ILOp;
import uk.co.transputersystems.occam.metadata.LibraryInformation;
import uk.co.transputersystems.occam.open_transputer.ASMBlock;
import uk.co.transputersystems.occam.open_transputer.ASMGenerator;
import uk.co.transputersystems.occam.open_transputer.ASMGeneratorContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * Created by Edward on 01/05/2016.
 */
public class AssembleFileTest {

    @Before
    public void before() throws Exception {
        File dir = new File("target/test-output/assemble-file-test/function-tests");
        File dir2 = new File("target/test-output/assemble-file-test/feature-tests");
        File dir3 = new File("target/test-output/assemble-file-test/basic-tests");
        dir.mkdirs();
        dir2.mkdirs();
        dir3.mkdirs();
    }

    public InputStream getResource(String name) {
        return getClass().getResourceAsStream(name);
    }

    /**
     * Given a file, attempt to parse and compile it to TSIL.
     * @param file The file to be compiled
     * @return true if the compilation appears to have succeeded, otherwise false.
     */
    public boolean testAssemblingFile(File file) throws Exception {
        return testAssemblingFile(file, true);
    }
    /**
     * Given a file, attempt to parse and compile it to TSIL. Checks that the
     * compiled code is not null or empty.
     * TODO: find a way to test the output other than checking nullity
     * @param file The file to be compiled
     * @param verbose Whether to print debug output or not.
     * @return true if the compilation appears to have succeeded, otherwise false.
     */
    public boolean testAssemblingFile(File file, boolean verbose) throws Exception {
        PrintStream out = new PrintStream(System.out);

        if (verbose) {
            out.println("TEST: " + file);
            out.flush();
        }

        String input = new String(Files.readAllBytes(file.toPath()));
        List<String> output = Assembler.assemble(input, Assembler.loadConfig(null), out);

        assertTrue(output != null);
        assertTrue(output.size() > 0);

        FileWriter outputWriter = new FileWriter(new File(file.getPath().replace(".auto.", ".").replace(".s",".auto.o")));
        for (String line : output) {
            outputWriter.append(line);
            outputWriter.append('\n');
        }
        outputWriter.close();

        return true;
    }

    /**
     * Given a file, attempt to parse and compile it to TSIL.
     *
     * @param file The file to be compiled
     * @return true if the compilation appears to have succeeded, otherwise false.
     */
    public File testCompilingFile(InputStream file, String filePath, String fileName) throws Exception {
        return testCompilingFile(file, filePath, fileName, true);
    }

    /**
     * Given a file, attempt to parse and compile it to TSIL. Checks that the
     * compiled code is not null or empty.
     * TODO: find a way to test the output other than checking nullity
     *
     * @param inputStream The file to be compiled
     * @param verbose     Whether to print debug output or not.
     * @return true if the compilation appears to have succeeded, otherwise false.
     */
    public File testCompilingFile(InputStream inputStream, String filePath, String fileName, boolean verbose) throws Exception {
        ErrorListener errorListener = new ErrorListener();
        OccamCompiler compiler = new OccamCompiler();

        PrintWriter out = new PrintWriter(System.out);

        if (verbose) {
            out.println("TEST: " + inputStream);
        }

        ParseTree tree = compiler.makeParseTree(inputStream, OccamParser::file_input, errorListener);

        if (verbose) {
            compiler.printParseTree(out, tree);
        }

        LibraryInformation libraryInfo = new LibraryInformation("TEST");
        List<ILBlock<UUID, ILOp<UUID>>> intermediate = compiler.generateTSIL(new TSILGenerator(libraryInfo), tree, filePath, fileName);

        assertTrue(intermediate != null);
        assertTrue(intermediate.size() > 0);

        // Convert UUID IL to sequential integer-labelled IL
        List<ILBlock<Integer, ILOp<Integer>>> intermediateRef = compiler.generateReferencedTSIL(new ReferencedTSILGenerator<>(), intermediate);

        ASMGeneratorContext asmGeneratorContext = new ASMGeneratorContext(libraryInfo);

        List<ASMBlock> asmBlocks = compiler.generateASM(new ASMGenerator(), intermediateRef, asmGeneratorContext, false);

        assertTrue(asmBlocks != null);
        assertTrue(asmBlocks.size() > 0);

        if (verbose) {
            compiler.writeDivider(out);
            compiler.writeASMBlocks(out, asmBlocks);
            compiler.writeDivider(out);
            out.flush();
        }

        File outputFile = new File("target/test-output/assemble-file-test" + filePath.replace(".occ", ".auto.s"));
        FileWriter outputWriter = new FileWriter(outputFile);
        compiler.writeASMBlocks(outputWriter, asmBlocks);
        outputWriter.flush();
        outputWriter.close();

        return outputFile;
    }

    @Ignore("Not yet fully implemented")
    @Test
    public void testArrays() throws Exception {
        String path = "/feature-tests/array.occ";
		File asmFile = testCompilingFile(getResource(path), path, "array.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Ignore("Not yet fully implemented")
    @Test
    public void testArraySegments() throws Exception {
        String path = "/feature-tests/array_segment.occ";
		File asmFile = testCompilingFile(getResource(path), path, "array_segment.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testAssignment() throws Exception {
        String path = "/feature-tests/assignment.occ";
		File asmFile = testCompilingFile(getResource(path), path, "assignment.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testByteLiterals() throws Exception {
        String path = "/feature-tests/byte_literal.occ";
		File asmFile = testCompilingFile(getResource(path), path, "byte_literal.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testChannels() throws Exception {
        String path = "/feature-tests/channels.occ";
		File asmFile = testCompilingFile(getResource(path), path, "channels.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testConditionals() throws Exception {
        String path = "/feature-tests/conditional.occ";
		File asmFile = testCompilingFile(getResource(path), path, "conditional.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testDeclarations() throws Exception {
        String path = "/feature-tests/declarations.occ";
		File asmFile = testCompilingFile(getResource(path), path, "declarations.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testDyadicExpressions() throws Exception {
        String path = "/feature-tests/dyadic_expression.occ";
		File asmFile = testCompilingFile(getResource(path), path, "dyadic_expression.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testNestedExpressionFeatures() throws Exception {
        String path = "/feature-tests/expression_features_nested.occ";
		File asmFile = testCompilingFile(getResource(path), path, "expression_features_nested.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsOfExpressionList() throws Exception {
        String path = "/function-tests/function_expression_list.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_expression_list.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsOfValueProcess() throws Exception {
        String path = "/function-tests/function_value_process.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_value_process.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp0In1Out() throws Exception {
        String path = "/function-tests/function_vp_0_in_1_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_0_in_1_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp0In2Out() throws Exception {
        String path = "/function-tests/function_vp_0_in_2_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_0_in_2_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp0In3Out() throws Exception {
        String path = "/function-tests/function_vp_0_in_3_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_0_in_3_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp0In4Out() throws Exception {
        String path = "/function-tests/function_vp_0_in_4_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_0_in_4_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp1In1Out() throws Exception {
        String path = "/function-tests/function_vp_1_in_1_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_1_in_1_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp1In2Out() throws Exception {
        String path = "/function-tests/function_vp_1_in_2_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_1_in_2_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp1In3Out() throws Exception {
        String path = "/function-tests/function_vp_1_in_3_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_1_in_3_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp1In4Out() throws Exception {
        String path = "/function-tests/function_vp_1_in_4_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_1_in_4_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp2In1Out() throws Exception {
        String path = "/function-tests/function_vp_2_in_1_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_2_in_1_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp2In2Out() throws Exception {
        String path = "/function-tests/function_vp_2_in_2_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_2_in_2_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp2In3Out() throws Exception {
        String path = "/function-tests/function_vp_2_in_3_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_2_in_3_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp2In4Out() throws Exception {
        String path = "/function-tests/function_vp_2_in_4_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_2_in_4_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp3In1Out() throws Exception {
        String path = "/function-tests/function_vp_3_in_1_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_3_in_1_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp3In2Out() throws Exception {
        String path = "/function-tests/function_vp_3_in_2_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_3_in_2_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp3In3Out() throws Exception {
        String path = "/function-tests/function_vp_3_in_3_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_3_in_3_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp3In4Out() throws Exception {
        String path = "/function-tests/function_vp_3_in_4_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_3_in_4_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp4In1Out() throws Exception {
        String path = "/function-tests/function_vp_4_in_1_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_4_in_1_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp4In2Out() throws Exception {
        String path = "/function-tests/function_vp_4_in_2_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_4_in_2_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp4In3Out() throws Exception {
        String path = "/function-tests/function_vp_4_in_3_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_4_in_3_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsVp4In4Out() throws Exception {
        String path = "/function-tests/function_vp_4_in_4_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_vp_4_in_4_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl0In1Out() throws Exception {
        String path = "/function-tests/function_el_0_in_1_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_0_in_1_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl0In2Out() throws Exception {
        String path = "/function-tests/function_el_0_in_2_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_0_in_2_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl0In3Out() throws Exception {
        String path = "/function-tests/function_el_0_in_3_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_0_in_3_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl0In4Out() throws Exception {
        String path = "/function-tests/function_el_0_in_4_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_0_in_4_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl1In1Out() throws Exception {
        String path = "/function-tests/function_el_1_in_1_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_1_in_1_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl1In2Out() throws Exception {
        String path = "/function-tests/function_el_1_in_2_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_1_in_2_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl1In3Out() throws Exception {
        String path = "/function-tests/function_el_1_in_3_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_1_in_3_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl1In4Out() throws Exception {
        String path = "/function-tests/function_el_1_in_4_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_1_in_4_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl2In1Out() throws Exception {
        String path = "/function-tests/function_el_2_in_1_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_2_in_1_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl2In2Out() throws Exception {
        String path = "/function-tests/function_el_2_in_2_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_2_in_2_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl2In3Out() throws Exception {
        String path = "/function-tests/function_el_2_in_3_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_2_in_3_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl2In4Out() throws Exception {
        String path = "/function-tests/function_el_2_in_4_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_2_in_4_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl3In1Out() throws Exception {
        String path = "/function-tests/function_el_3_in_1_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_3_in_1_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl3In2Out() throws Exception {
        String path = "/function-tests/function_el_3_in_2_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_3_in_2_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl3In3Out() throws Exception {
        String path = "/function-tests/function_el_3_in_3_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_3_in_3_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl3In4Out() throws Exception {
        String path = "/function-tests/function_el_3_in_4_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_3_in_4_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl4In1Out() throws Exception {
        String path = "/function-tests/function_el_4_in_1_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_4_in_1_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl4In2Out() throws Exception {
        String path = "/function-tests/function_el_4_in_2_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_4_in_2_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl4In3Out() throws Exception {
        String path = "/function-tests/function_el_4_in_3_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_4_in_3_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testFunctionsEl4In4Out() throws Exception {
        String path = "/function-tests/function_el_4_in_4_out.occ";
		File asmFile = testCompilingFile(getResource(path), path, "function_el_4_in_4_out.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testMonadicExpressions() throws Exception {
        String path = "/feature-tests/monadic_expressions.occ";
		File asmFile = testCompilingFile(getResource(path), path, "monadic_expressions.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testTypeOperations() throws Exception {
        String path = "/feature-tests/operations_types.occ";
		File asmFile = testCompilingFile(getResource(path), path, "operations_types.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testPars() throws Exception {
        String path = "/feature-tests/par.occ";
		File asmFile = testCompilingFile(getResource(path), path, "par.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testParAdvanced() throws Exception {
        String path = "/feature-tests/par_advanced.occ";
		File asmFile = testCompilingFile(getResource(path), path, "par_advanced.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testProcArguments() throws Exception {
        String path = "/feature-tests/proc_arguments.occ";
		File asmFile = testCompilingFile(getResource(path), path, "proc_arguments.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testProcedureStaticAssignment() throws Exception {
        String path = "/feature-tests/procedure_static_assignment.occ";
		File asmFile = testCompilingFile(getResource(path), path, "procedure_static_assignment.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Ignore("Not yet fully implemented")
    @Test
    public void testRecordDeclarations() throws Exception {
        String path = "/feature-tests/record_declaration.occ";
		File asmFile = testCompilingFile(getResource(path), path, "record_declaration.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Ignore("Not yet fully implemented")
    @Test
    public void testRecordOperations() throws Exception {
        String path = "/feature-tests/record_operations.occ";
		File asmFile = testCompilingFile(getResource(path), path, "record_operations.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testReplicatedConditionals() throws Exception {
        String path = "/feature-tests/replicated_conditionals.occ";
		File asmFile = testCompilingFile(getResource(path), path, "replicated_conditionals.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testReplicators() throws Exception {
        String path = "/feature-tests/replicator.occ";
		File asmFile = testCompilingFile(getResource(path), path, "replicator.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testSequentials() throws Exception {
        String path = "/feature-tests/sequential.occ";
		File asmFile = testCompilingFile(getResource(path), path, "sequential.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testVariableAbbreviations() throws Exception {
        String path = "/feature-tests/variable_abbreviations.occ";
		File asmFile = testCompilingFile(getResource(path), path, "variable_abbreviations.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testChannelAbbreviations() throws Exception {
        String path = "/feature-tests/channel_abbreviations.occ";
        File asmFile = testCompilingFile(getResource(path), path, "channel_abbreviations.occ");
        assertTrue(testAssemblingFile(asmFile));
    }

    @Ignore("Not yet fully implemented")
    @Test
    public void testArrayAbbreviations() throws Exception {
        String path = "/feature-tests/array.occ";
		File asmFile = testCompilingFile(getResource(path), path, "array.occ");
		assertTrue(testAssemblingFile(asmFile));
    }

    @Test
    public void testWhileLoops() throws Exception {
        String path = "/feature-tests/while_loops.occ";
		File asmFile = testCompilingFile(getResource(path), path, "while_loops.occ");
		assertTrue(testAssemblingFile(asmFile));
    }
}
