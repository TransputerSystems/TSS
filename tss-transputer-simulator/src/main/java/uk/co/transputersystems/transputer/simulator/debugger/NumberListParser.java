package uk.co.transputersystems.transputer.simulator.debugger;

import uk.co.transputersystems.transputer.simulator.DebuggerCommandBaseVisitor;
import uk.co.transputersystems.transputer.simulator.DebuggerCommandParser;

import java.util.List;
import java.util.stream.Collectors;

public class NumberListParser extends DebuggerCommandBaseVisitor<List<Integer>> {
    @Override
    public List<Integer> visitTransputer_list(DebuggerCommandParser.Transputer_listContext ctx) {
        return ctx.NUMBER().stream().map(number -> Integer.parseInt(number.getText())).collect(Collectors.toList());
    }
}
