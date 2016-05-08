package uk.co.transputersystems.transputer.assembler.expression;

import uk.co.transputersystems.transputer.assembler.AssemblerExpressionBaseVisitor;
import uk.co.transputersystems.transputer.assembler.AssemblerExpressionParser;

import java.util.Map;

public class ExpressionEvaluator extends AssemblerExpressionBaseVisitor<Long> {

    private final Map<String, Long> labelMap;

    @Override
    public Long visitNegExp(AssemblerExpressionParser.NegExpContext ctx) {
        return -visit(ctx.expression());
    }

    public ExpressionEvaluator(Map<String, Long> labelMap) {
        this.labelMap = labelMap;
    }

    @Override
    public Long visitNumberExp(AssemblerExpressionParser.NumberExpContext ctx) {
        return Long.parseLong(ctx.NUMBER().getText());
    }

    @Override
    public Long visitParenExp(AssemblerExpressionParser.ParenExpContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Long visitMulExp(AssemblerExpressionParser.MulExpContext ctx) {
        return visit(ctx.expression(0)) * visit(ctx.expression(1));
    }

    @Override
    public Long visitAddSubExp(AssemblerExpressionParser.AddSubExpContext ctx) {
        if (ctx.MINUS() != null) {
            return visit(ctx.expression(0)) - visit(ctx.expression(1));
        } else {
            return visit(ctx.expression(0)) + visit(ctx.expression(1));
        }
    }

    @Override
    public Long visitLabelExp(AssemblerExpressionParser.LabelExpContext ctx) {
        return labelMap.get(ctx.getText());
    }
}
