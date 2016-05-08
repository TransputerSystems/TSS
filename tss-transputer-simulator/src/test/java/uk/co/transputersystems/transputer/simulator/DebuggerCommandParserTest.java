package uk.co.transputersystems.transputer.simulator;

import uk.co.transputersystems.occam.ErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DebuggerCommandParserTest {

    public ParseTree parseCommand(String command) {
        DebuggerCommandLexer lexer = new DebuggerCommandLexer(new ANTLRInputStream(command));

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ErrorListener errorListener = new ErrorListener();
        DebuggerCommandParser commandParser = new DebuggerCommandParser(tokenStream);
        commandParser.addErrorListener(errorListener);

        ParseTree tree = commandParser.command();

        assertEquals(0, errorListener.errors);
        assertNotNull(tree);

        return tree;
    }

    @Test
    public void testHelpCommand() {
        ParseTree help = parseCommand("help");
        ParseTree HELP = parseCommand("HELP");
        ParseTree hElp = parseCommand("hElp");
    }

    @Test
    public void testInfoMemCommand() {
        ParseTree info1 = parseCommand("1 info mem");
        ParseTree info2 = parseCommand("1,2,3 INFO MEM");
        ParseTree info3 = parseCommand("99,0 i MEm");
    }

    @Test
    public void testInfoRegCommand() {
        ParseTree info1 = parseCommand("1 info reg");
        ParseTree info2 = parseCommand("1,2,3 INFO REG");
        ParseTree info3 = parseCommand("99,0 i reG");
    }

    @Test
    public void testInfoSRegCommand() {
        ParseTree info1 = parseCommand("1 info s-reg");
        ParseTree info2 = parseCommand("1,2,3 INFO s-REG");
        ParseTree info3 = parseCommand("99,0 i S-reG");
    }

    @Test
    public void testInfoCRegCommand() {
        ParseTree info1 = parseCommand("1 info c-reg");
        ParseTree info2 = parseCommand("1,2,3 INFO c-REG");
        ParseTree info3 = parseCommand("99,0 i C-reG");
    }

    @Test
    public void testInfoLinkCommand() {
        ParseTree info1 = parseCommand("1 info link");
        ParseTree info2 = parseCommand("1,2,3 INFO LINK");
        ParseTree info3 = parseCommand("99,0 i Link");
    }

    @Test
    public void testInfoBreakCommand() {
        ParseTree info1 = parseCommand("1 info break");
        ParseTree info2 = parseCommand("1,2,3 INFO BREAk");
        ParseTree info3 = parseCommand("99,0 i bREAk");
    }

    @Test
    public void testBreakCommand() {
        ParseTree info1 = parseCommand("1 break 270");
        ParseTree info2 = parseCommand("1,2,3 b 2738");
        ParseTree info3 = parseCommand("99,0 b 0");
    }

    @Test
    public void testDeleteCommand() {
        ParseTree info1 = parseCommand("1 delete 270");
        ParseTree info2 = parseCommand("1,2,3 DELETE 2738");
        ParseTree info3 = parseCommand("99,0 dELETE 0");
    }

    @Test
    public void testStepCommand() {
        ParseTree info1 = parseCommand("s");
        ParseTree info2 = parseCommand("S");
        ParseTree info3 = parseCommand("STEP");
        ParseTree info4 = parseCommand("step");
    }

    @Test
    public void testContinueCommand() {
        ParseTree info1 = parseCommand("c");
        ParseTree info2 = parseCommand("C");
        ParseTree info3 = parseCommand("CONTINUE");
        ParseTree info4 = parseCommand("continue");
    }
}
