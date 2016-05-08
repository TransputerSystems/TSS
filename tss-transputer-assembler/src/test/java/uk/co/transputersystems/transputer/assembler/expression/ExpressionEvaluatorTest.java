package uk.co.transputersystems.transputer.assembler.expression;

import uk.co.transputersystems.transputer.assembler.AssemblerExpressionLexer;
import uk.co.transputersystems.transputer.assembler.AssemblerExpressionParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ExpressionEvaluatorTest {

    public final static Map<String, Long> labelMap;
    static {
        labelMap = new HashMap<>();
        labelMap.put("label1", 0L);
        labelMap.put("~label2", 3L);
        labelMap.put("_~label_3", 7L);
        labelMap.put("4label~", 13L);

    }

    @Test
    public void testExpressionEvaluatorWithNumber() {
        assertEquals(new Long(5), evaluateExpression("5", labelMap));
    }

    @Test
    public void testExpressionEvaluatorWithNegativeNumber() {
        assertEquals(new Long(-5), evaluateExpression("-5", labelMap));
    }

    @Test
    public void testExpressionEvaluatorWithNumberExpression() {
        assertEquals(new Long(-3), evaluateExpression("-5 + 2", labelMap));
    }

    @Test
    public void testExpressionEvaluatorWithLabel() {
        assertEquals(new Long(3), evaluateExpression("~label2", labelMap));
    }

    @Test
    public void testExpressionEvaluatorWithLabelSubtraction() {
        assertEquals(new Long(7), evaluateExpression("_~label_3-label1", labelMap));
    }

    @Test
    public void testExpressionEvaluatorWithLabelAddition() {
        assertEquals(new Long(20), evaluateExpression("_~label_3+4label~", labelMap));
    }

    @Test
    public void testExpressionEvaluatorWithLabelAndNumber() {
        assertEquals(new Long(2), evaluateExpression("_~label_3-5", labelMap));
    }

    @Test
    public void testExpressionEvaluatorWithNegativeLabel() {
        assertEquals(new Long(-7), evaluateExpression("-_~label_3", labelMap));
    }

    @Test
    public void testExpressionEvaluatorWithComplexExpression() {
        assertEquals(new Long(-10), evaluateExpression("(-_~label_3 - 4label~) + 10", labelMap));
    }

    public static Long evaluateExpression(String expression, Map<String, Long> labelMap) {
        AssemblerExpressionLexer synLexer = new AssemblerExpressionLexer(new ANTLRInputStream(expression));

        CommonTokenStream tokenStream = new CommonTokenStream(synLexer);
        AssemblerExpressionParser expressionParser = new AssemblerExpressionParser(tokenStream);

        ParseTree tree = expressionParser.expression();
        ExpressionEvaluator evaluator = new ExpressionEvaluator(labelMap);

        return evaluator.visit(tree);
    }
}
