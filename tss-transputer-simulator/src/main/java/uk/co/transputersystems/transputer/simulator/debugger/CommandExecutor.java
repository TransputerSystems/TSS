package uk.co.transputersystems.transputer.simulator.debugger;

import uk.co.transputersystems.transputer.simulator.*;
import org.antlr.v4.runtime.tree.ParseTree;
import uk.co.transputersystems.transputer.simulator.DebuggerCommandBaseVisitor;
import uk.co.transputersystems.transputer.simulator.DebuggerCommandParser;
import uk.co.transputersystems.transputer.simulator.Transputer;
import uk.co.transputersystems.transputer.simulator.TransputerConstants;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandExecutor extends DebuggerCommandBaseVisitor<CommandResult> {

    public final PrintWriter output;
    public final PrintWriter errOutput;
    public final Transputer[] transputers;
    public final NumberListParser numberListParser = new NumberListParser();

    public CommandExecutor(Transputer[] transputers, PrintWriter output, PrintWriter errOutput) {
        super();
        this.output = output;
        this.errOutput = errOutput;
        this.transputers = transputers;
    }

    @Override
    public CommandResult visit(ParseTree tree) {
        CommandResult result = super.visit(tree);
        errOutput.flush();
        output.flush();
        return result;
    }

    @Override
    public CommandResult visitHelp(DebuggerCommandParser.HelpContext ctx) {
        output.printf("Debugger commands\n");
        output.printf("<trps>\tlist of transputers\n");
        output.printf("\t\te.g. 0,1,3\n");
        output.printf("<brk>\tbreakpoint address\n\n");
        output.printf("\thelp                Display this message\n");
        output.printf("\t<trps> info mem     Print the last written memory locations\n");
        output.printf("\t<trps> info reg     Print all register values\n");
        output.printf("\t<trps> info s-reg   Print the contents of the status register\n");
        output.printf("\t<trps> info c-reg   Print the contents of the control registers\n");
        output.printf("\t<trps> info link    Print the contents of the external links\n");
        output.printf("\t<trps> info break   Print list of breakpoints\n");
        output.printf("\t<trps> break <brk>  Set a breakpoint\n");
        output.printf("\t<trps> delete <brk> Unset a breakpoint\n");
        output.printf("\tstep                Execute the next instruction\n");
        output.printf("\tcontinue            Execute all remaining instructions\n");
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitInfo_memsize(DebuggerCommandParser.Info_memsizeContext ctx) {
        output.print(TransputerConstants.MEMSIZE);
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitInfo_instruction(DebuggerCommandParser.Info_instructionContext ctx) {
        for (int n : numberListParser.visit(ctx.transputer_list())) {
            if (0 <= n && n <= transputers.length) {
                transputers[n].printNextInstruction(output);
            } else {
                output.printf("There is no transputer %d\n", n);
            }
        }
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitExamine(DebuggerCommandParser.ExamineContext ctx) {
        MemorySectionSpecification memorySectionSpecification;
        if (ctx.EXAMINE_FLAG() != null) {
            memorySectionSpecification = interpretExamineFlag(ctx.EXAMINE_FLAG().getText());
        } else {
            memorySectionSpecification = new MemorySectionSpecification(1, 1, "%08x");
        }
        for (int n : numberListParser.visit(ctx.transputer_list())) {
            if (0 <= n && n <= transputers.length) {
                transputers[n].printMemory(memorySectionSpecification.numberUnits, memorySectionSpecification.bytesPerUnit, memorySectionSpecification.format, tryParseAddress(ctx.address().getText(), 0), output);
            } else {
                output.printf("There is no transputer %d\n", n);
            }
        }
        return CommandResult.REMAIN;
    }

    public MemorySectionSpecification interpretExamineFlag(@Nonnull String flag) {
        Pattern flagPattern = Pattern.compile("^/(?<numUnits>[\\d]+)(?<bytesPerUnit>b|h|w|g)(?<format>s|i|x|d|o|a|c|f)$");
        Matcher flagMatcher = flagPattern.matcher(flag);
        if (!flagMatcher.matches()) {
            throw new IllegalArgumentException(String.format("Flag %s is not valid.", flag));
        }

        int bytesPerUnit = unitToNumBytes(flagMatcher.group("bytesPerUnit"));

        return new MemorySectionSpecification(
                tryParseInt(flagMatcher.group("numUnits"), 1, 10),
                bytesPerUnit,
                flagMatcher.group("format"));
    }

    class MemorySectionSpecification {
        public final int numberUnits;
        public final int bytesPerUnit;
        @Nonnull public final String format;
        public MemorySectionSpecification(int numberUnits, int bytesPerUnit, @Nonnull String format) {
            this.numberUnits = numberUnits;
            this.bytesPerUnit = bytesPerUnit;
            this.format = format;
        }
    }

    public static int tryParseInt(String str, int defaultResult, int base) {
        Integer result;
        try {
            result = Integer.parseInt(str, base);
        } catch (Exception ignored) {
            return defaultResult;
        }
        return result;
    }

    public int tryParseAddress(String str, int defaultResult) {
        // An address can be either hexadecimal or decimal
        if ("0x".equals(str.substring(0, 2))) {
            return tryParseInt(str.substring(2), defaultResult, 16);
        } else {
            return tryParseInt(str, defaultResult, 10);
        }
    }

    public int unitToNumBytes(String unit) {
        if (unit == null) {
            return 1;
        }
        switch (unit) {
            case "b":
            case "B":
                return 1;
            case "h":
            case "H":
                return 2;
            case "w":
            case "W":
                return 4;
            case "g":
            case "G":
                return 8;
            default:
                throw new IllegalArgumentException(String.format("%s is not a recognised unit", unit));
        }
    }

    @Override
    public CommandResult visitInfo_mem(DebuggerCommandParser.Info_memContext ctx) {
        for (int n : numberListParser.visit(ctx.transputer_list())) {
            if (0 <= n && n <= transputers.length) {
                transputers[n].printRecentMemory(output);
            } else {
                output.printf("There is no transputer %d\n", n);
            }
        }
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitInfo_reg(DebuggerCommandParser.Info_regContext ctx) {
        for (int n : numberListParser.visit(ctx.transputer_list())) {
            if (0 <= n && n <= transputers.length) {
                transputers[n].printRegisters(output);
            } else {
                output.printf("There is no transputer %d\n", n);
            }
        }
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitInfo_s_reg(DebuggerCommandParser.Info_s_regContext ctx) {
        for (int n : numberListParser.visit(ctx.transputer_list())) {
            if (0 <= n && n <= transputers.length) {
                transputers[n].printSRegisters(output);
            } else {
                output.printf("There is no transputer %d\n", n);
            }
        }
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitInfo_c_reg(DebuggerCommandParser.Info_c_regContext ctx) {
        for (int n : numberListParser.visit(ctx.transputer_list())) {
            if (0 <= n && n <= transputers.length) {
                transputers[n].printCRegisters(output);
            } else {
                output.printf("There is no transputer %d\n", n);
            }
        }
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitTransputers(DebuggerCommandParser.TransputersContext ctx) {
        for (int i = 0; i < transputers.length; i++) {
            output.printf("# Transputer %d\n", i);
            transputers[i].printRegisters(output);
        }
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitInfo_link(DebuggerCommandParser.Info_linkContext ctx) {
        for (int n : numberListParser.visit(ctx.transputer_list())) {
            if (0 <= n && n <= transputers.length) {
                transputers[n].printLinks(output);
            } else {
                output.printf("There is no transputer %d\n", n);
            }
        }
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitInfo_break(DebuggerCommandParser.Info_breakContext ctx) {
        for (int n : numberListParser.visit(ctx.transputer_list())) {
            if (0 <= n && n <= transputers.length) {
                transputers[n].printBreakpoints(output);
            } else {
                output.printf("There is no transputer %d\n", n);
            }
        }
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitInfo_processes(DebuggerCommandParser.Info_processesContext ctx) {
        for (int n : numberListParser.visit(ctx.transputer_list())) {
            if (0 <= n && n <= transputers.length) {
                transputers[n].printProcessList(output);
            } else {
                output.printf("There is no transputer %d\n", n);
            }
        }
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitBreak(DebuggerCommandParser.BreakContext ctx) {
        Integer breakpoint = tryParseAddress(ctx.address().getText(), 0);
        for (int n : numberListParser.visit(ctx.transputer_list())) {
            if (0 <= n && n <= transputers.length) {
                transputers[n].setBreakpoint(breakpoint, output);
            } else {
                output.printf("There is no transputer %d\n", n);
            }
        }
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitDelete(DebuggerCommandParser.DeleteContext ctx) {
        Integer breakpoint = tryParseAddress(ctx.address().getText(), 0);
        for (int n : numberListParser.visit(ctx.transputer_list())) {
            if (0 <= n && n <= transputers.length) {
                transputers[n].unsetBreakpoint(breakpoint, output);
            } else {
                output.printf("There is no transputer %d\n", n);
            }
        }
        return CommandResult.REMAIN;
    }

    @Override
    public CommandResult visitStep(DebuggerCommandParser.StepContext ctx) {
        return CommandResult.STEP;
    }

    @Override
    public CommandResult visitContinue(DebuggerCommandParser.ContinueContext ctx) {
        return CommandResult.CONTINUE;
    }
}
