package uk.co.transputersystems.occam;

import uk.co.transputersystems.occam.il.ILBlock;
import uk.co.transputersystems.occam.il.ILOp;
import uk.co.transputersystems.occam.metadata.LibraryInformation;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class TSILGeneratorTest {

    public InputStream getResource(String name) {
        return getClass().getResourceAsStream(name);
    }

    /**
     * Given a file, attempt to parse and compile it to TSIL.
     * @param file The file to be compiled
     * @return true if the compilation appears to have succeeded, otherwise false.
     */
    public boolean testCompilingFile(InputStream file, String filePath, String fileName) {
        return testCompilingFile(file, filePath, fileName, true);
    }
    /**
     * Given a file, attempt to parse and compile it to TSIL. Checks that the
     * compiled code is not null or empty.
     * TODO: find a way to test the output other than checking nullity
     * @param inputStream The file to be compiled
     * @param verbose Whether to print debug output or not.
     * @return true if the compilation appears to have succeeded, otherwise false.
     */
    public boolean testCompilingFile(InputStream inputStream, String filePath, String fileName, boolean verbose) {
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

        List<ILBlock<UUID, ILOp<UUID>>> intermediate = compiler.generateTSIL(new TSILGenerator(new LibraryInformation("TEST")), tree, filePath, fileName);

        assertTrue(intermediate != null);
        assertTrue(intermediate.size() > 0);

        if (verbose) {
            // Convert UUID IL to sequential integer-labelled IL
            List<ILBlock<Integer, ILOp<Integer>>> intermediateRef = compiler.generateReferencedTSIL(new ReferencedTSILGenerator<>(), intermediate);

            try {
                compiler.writeDivider(out);
                for (ILBlock block : intermediateRef) {
                    out.println(block.toString());
                }
                compiler.writeDivider(out);
                out.flush();
            } catch (IOException ex) {
                out.println(ex.toString());
            }
        }

        return true;
    }

    @Ignore("Not yet fully implemented")
    @Test
    public void testArrays() {
        String path = "/feature-tests/array.occ";
		assertTrue(testCompilingFile(getResource(path), path, "array.occ"));
    }

    @Ignore("Not yet fully implemented")
    @Test
    public void testArraySegments() {
        String path = "/feature-tests/array_segment.occ";
		assertTrue(testCompilingFile(getResource(path), path, "array_segment.occ"));
    }

    @Test
    public void testAssignment() {
        String path = "/feature-tests/assignment.occ";
		assertTrue(testCompilingFile(getResource(path), path, "assignment.occ"));
    }

    @Test
    public void testByteLiterals() {
        String path = "/feature-tests/byte_literal.occ";
		assertTrue(testCompilingFile(getResource(path), path, "byte_literal.occ"));
    }

    @Test
    public void testChannels() {
        String path = "/feature-tests/channels.occ";
		assertTrue(testCompilingFile(getResource(path), path, "channels.occ"));
    }

    @Test
    public void testPorts() {
        String path = "/feature-tests/port.occ";
        assertTrue(testCompilingFile(getResource(path), path, "port.occ"));
    }

    @Test
    public void testConditionals() {
        String path = "/feature-tests/conditional.occ";
		assertTrue(testCompilingFile(getResource(path), path, "conditional.occ"));
    }

    @Test
    public void testDeclarations() {
        String path = "/feature-tests/declarations.occ";
		assertTrue(testCompilingFile(getResource(path), path, "declarations.occ"));
    }

    @Test
    public void testDyadicExpressions() {
        String path = "/feature-tests/dyadic_expression.occ";
		assertTrue(testCompilingFile(getResource(path), path, "dyadic_expression.occ"));
    }

    @Test
    public void testNestedExpressionFeatures() {
        String path = "/feature-tests/expression_features_nested.occ";
		assertTrue(testCompilingFile(getResource(path), path, "expression_features_nested.occ"));
    }

    @Test
    public void testFunctionsOfExpressionList() {
        String path = "/function-tests/function_expression_list.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_expression_list.occ"));
    }

    @Test
    public void testFunctionsOfValueProcess() {
        String path = "/function-tests/function_value_process.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_value_process.occ"));
    }

    @Test
    public void testFunctionsVp0In1Out() {
        String path = "/function-tests/function_vp_0_in_1_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_0_in_1_out.occ"));
    }

    @Test
    public void testFunctionsVp0In2Out() {
        String path = "/function-tests/function_vp_0_in_2_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_0_in_2_out.occ"));
    }

    @Test
    public void testFunctionsVp0In3Out() {
        String path = "/function-tests/function_vp_0_in_3_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_0_in_3_out.occ"));
    }

    @Test
    public void testFunctionsVp0In4Out() {
        String path = "/function-tests/function_vp_0_in_4_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_0_in_4_out.occ"));
    }

    @Test
    public void testFunctionsVp1In1Out() {
        String path = "/function-tests/function_vp_1_in_1_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_1_in_1_out.occ"));
    }

    @Test
    public void testFunctionsVp1In2Out() {
        String path = "/function-tests/function_vp_1_in_2_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_1_in_2_out.occ"));
    }

    @Test
    public void testFunctionsVp1In3Out() {
        String path = "/function-tests/function_vp_1_in_3_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_1_in_3_out.occ"));
    }

    @Test
    public void testFunctionsVp1In4Out() {
        String path = "/function-tests/function_vp_1_in_4_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_1_in_4_out.occ"));
    }

    @Test
    public void testFunctionsVp2In1Out() {
        String path = "/function-tests/function_vp_2_in_1_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_2_in_1_out.occ"));
    }

    @Test
    public void testFunctionsVp2In2Out() {
        String path = "/function-tests/function_vp_2_in_2_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_2_in_2_out.occ"));
    }

    @Test
    public void testFunctionsVp2In3Out() {
        String path = "/function-tests/function_vp_2_in_3_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_2_in_3_out.occ"));
    }

    @Test
    public void testFunctionsVp2In4Out() {
        String path = "/function-tests/function_vp_2_in_4_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_2_in_4_out.occ"));
    }

    @Test
    public void testFunctionsVp3In1Out() {
        String path = "/function-tests/function_vp_3_in_1_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_3_in_1_out.occ"));
    }

    @Test
    public void testFunctionsVp3In2Out() {
        String path = "/function-tests/function_vp_3_in_2_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_3_in_2_out.occ"));
    }

    @Test
    public void testFunctionsVp3In3Out() {
        String path = "/function-tests/function_vp_3_in_3_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_3_in_3_out.occ"));
    }

    @Test
    public void testFunctionsVp3In4Out() {
        String path = "/function-tests/function_vp_3_in_4_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_3_in_4_out.occ"));
    }

    @Test
    public void testFunctionsVp4In1Out() {
        String path = "/function-tests/function_vp_4_in_1_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_4_in_1_out.occ"));
    }

    @Test
    public void testFunctionsVp4In2Out() {
        String path = "/function-tests/function_vp_4_in_2_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_4_in_2_out.occ"));
    }

    @Test
    public void testFunctionsVp4In3Out() {
        String path = "/function-tests/function_vp_4_in_3_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_4_in_3_out.occ"));
    }

    @Test
    public void testFunctionsVp4In4Out() {
        String path = "/function-tests/function_vp_4_in_4_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_vp_4_in_4_out.occ"));
    }

    @Test
    public void testFunctionsEl0In1Out() {
        String path = "/function-tests/function_el_0_in_1_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_0_in_1_out.occ"));
    }

    @Test
    public void testFunctionsEl0In2Out() {
        String path = "/function-tests/function_el_0_in_2_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_0_in_2_out.occ"));
    }

    @Test
    public void testFunctionsEl0In3Out() {
        String path = "/function-tests/function_el_0_in_3_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_0_in_3_out.occ"));
    }

    @Test
    public void testFunctionsEl0In4Out() {
        String path = "/function-tests/function_el_0_in_4_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_0_in_4_out.occ"));
    }

    @Test
    public void testFunctionsEl1In1Out() {
        String path = "/function-tests/function_el_1_in_1_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_1_in_1_out.occ"));
    }

    @Test
    public void testFunctionsEl1In2Out() {
        String path = "/function-tests/function_el_1_in_2_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_1_in_2_out.occ"));
    }

    @Test
    public void testFunctionsEl1In3Out() {
        String path = "/function-tests/function_el_1_in_3_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_1_in_3_out.occ"));
    }

    @Test
    public void testFunctionsEl1In4Out() {
        String path = "/function-tests/function_el_1_in_4_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_1_in_4_out.occ"));
    }

    @Test
    public void testFunctionsEl2In1Out() {
        String path = "/function-tests/function_el_2_in_1_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_2_in_1_out.occ"));
    }

    @Test
    public void testFunctionsEl2In2Out() {
        String path = "/function-tests/function_el_2_in_2_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_2_in_2_out.occ"));
    }

    @Test
    public void testFunctionsEl2In3Out() {
        String path = "/function-tests/function_el_2_in_3_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_2_in_3_out.occ"));
    }

    @Test
    public void testFunctionsEl2In4Out() {
        String path = "/function-tests/function_el_2_in_4_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_2_in_4_out.occ"));
    }

    @Test
    public void testFunctionsEl3In1Out() {
        String path = "/function-tests/function_el_3_in_1_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_3_in_1_out.occ"));
    }

    @Test
    public void testFunctionsEl3In2Out() {
        String path = "/function-tests/function_el_3_in_2_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_3_in_2_out.occ"));
    }

    @Test
    public void testFunctionsEl3In3Out() {
        String path = "/function-tests/function_el_3_in_3_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_3_in_3_out.occ"));
    }

    @Test
    public void testFunctionsEl3In4Out() {
        String path = "/function-tests/function_el_3_in_4_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_3_in_4_out.occ"));
    }

    @Test
    public void testFunctionsEl4In1Out() {
        String path = "/function-tests/function_el_4_in_1_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_4_in_1_out.occ"));
    }

    @Test
    public void testFunctionsEl4In2Out() {
        String path = "/function-tests/function_el_4_in_2_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_4_in_2_out.occ"));
    }

    @Test
    public void testFunctionsEl4In3Out() {
        String path = "/function-tests/function_el_4_in_3_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_4_in_3_out.occ"));
    }

    @Test
    public void testFunctionsEl4In4Out() {
        String path = "/function-tests/function_el_4_in_4_out.occ";
		assertTrue(testCompilingFile(getResource(path), path, "function_el_4_in_4_out.occ"));
    }

    @Test
    public void testMonadicExpressions() {
        String path = "/feature-tests/monadic_expressions.occ";
		assertTrue(testCompilingFile(getResource(path), path, "monadic_expressions.occ"));
    }

    @Test
    public void testTypeOperations() {
        String path = "/feature-tests/operations_types.occ";
		assertTrue(testCompilingFile(getResource(path), path, "operations_types.occ"));
    }

    @Test
    public void testPars() {
        String path = "/feature-tests/par.occ";
		assertTrue(testCompilingFile(getResource(path), path, "par.occ"));
    }

    @Test
    public void testParAdvanced() {
        String path = "/feature-tests/par_advanced.occ";
		assertTrue(testCompilingFile(getResource(path), path, "par_advanced.occ"));
    }

    @Test
    public void testProcArguments() {
        String path = "/feature-tests/proc_arguments.occ";
		assertTrue(testCompilingFile(getResource(path), path, "proc_arguments.occ"));
    }

    @Test
    public void testProcedureStaticAssignment() {
        String path = "/feature-tests/procedure_static_assignment.occ";
		assertTrue(testCompilingFile(getResource(path), path, "procedure_static_assignment.occ"));
    }

    @Ignore("Not yet fully implemented")
    @Test
    public void testRecordDeclarations() {
        String path = "/feature-tests/record_declaration.occ";
		assertTrue(testCompilingFile(getResource(path), path, "record_declaration.occ"));
    }

    @Ignore("Not yet fully implemented")
    @Test
    public void testRecordOperations() {
        String path = "/feature-tests/record_operations.occ";
		assertTrue(testCompilingFile(getResource(path), path, "record_operations.occ"));
    }

    @Test
    public void testReplicatedConditionals() {
        String path = "/feature-tests/replicated_conditionals.occ";
		assertTrue(testCompilingFile(getResource(path), path, "replicated_conditionals.occ"));
    }

    @Test
    public void testReplicators() {
        String path = "/feature-tests/replicator.occ";
		assertTrue(testCompilingFile(getResource(path), path, "replicator.occ"));
    }

    @Test
    public void testSequentials() {
        String path = "/feature-tests/sequential.occ";
		assertTrue(testCompilingFile(getResource(path), path, "sequential.occ"));
    }

    @Test
    public void testVariableAbbreviations() {
        String path = "/feature-tests/variable_abbreviations.occ";
		assertTrue(testCompilingFile(getResource(path), path, "variable_abbreviations.occ"));
    }

    @Test
    public void testChannelAbbreviations() {
        String path = "/feature-tests/channel_abbreviations.occ";
        assertTrue(testCompilingFile(getResource(path), path, "channel_abbreviations.occ"));
    }

    @Ignore("Not yet fully implemented")
    @Test
    public void testArrayAbbreviations() {
        String path = "/feature-tests/array.occ";
		assertTrue(testCompilingFile(getResource(path), path, "array.occ"));
    }

    @Test
    public void testWhileLoops() {
        String path = "/feature-tests/while_loops.occ";
		assertTrue(testCompilingFile(getResource(path), path, "while_loops.occ"));
    }
}
