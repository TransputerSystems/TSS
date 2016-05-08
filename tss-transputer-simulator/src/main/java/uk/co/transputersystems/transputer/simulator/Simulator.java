package uk.co.transputersystems.transputer.simulator;

import uk.co.transputersystems.transputer.simulator.debugger.CommandExecutor;
import uk.co.transputersystems.transputer.simulator.debugger.CommandResult;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;

import static java.io.File.pathSeparatorChar;

public class Simulator {

    private static CommandResult executeCommand(String command, Transputer[] transputers, PrintWriter output, PrintWriter errOutput) {
        DebuggerCommandLexer commandLexer = new DebuggerCommandLexer(new ANTLRInputStream(command));
        commandLexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

        CommonTokenStream tokenStream = new CommonTokenStream(commandLexer);
        ErrorListener errorListener = new ErrorListener();
        DebuggerCommandParser commandParser = new DebuggerCommandParser(tokenStream);
        commandParser.addErrorListener(errorListener);
        commandParser.removeErrorListener(ConsoleErrorListener.INSTANCE);

        ParseTree commandTree = commandParser.command();

        if (errorListener.errors != 0) {
            output.println("Command not recognised.");
            output.flush();
            return CommandResult.NOT_RECOGNISED;
        }

        CommandExecutor executor = new CommandExecutor(transputers, output, errOutput);
        return executor.visit(commandTree);
    }

    private static boolean interact(Transputer[] transputers, Scanner input, PrintWriter output, PrintWriter errOutput) {
        String command;
        CommandResult result;
        while (true) {
            System.out.printf("> ");
            command = input.nextLine();
            output.printf("\n");
            switch (executeCommand(command, transputers, output, errOutput)) {
                case CONTINUE:
                    return false;
                case STEP:
                    return true;
                default:
            }
        }
    }

    public static SimulatorConfig parseOptions(String[] args) {
        OptionParser optionParser = new OptionParser();

        OptionSpec interactiveArg = optionParser.accepts("interactive");

        OptionSpec printWorkspaceMemArg = optionParser.accepts("print-workspace-mem");

        OptionSpec<File> verilogTestbenchArg = optionParser
                .accepts("verilog-testbench-gen")
                .withRequiredArg()
                .ofType(File.class)
                .describedAs("generate test checking files for verilog testbench");

        OptionSpec<File> schedulerArg = optionParser
                .accepts("scheduler-test")
                .withRequiredArg()
                .ofType(File.class)
                .describedAs("generate test checking including scheduler registers");

        OptionSpec<File> timerArg = optionParser
                .accepts("timer-test")
                .withRequiredArg()
                .ofType(File.class)
                .describedAs("generate test checking including timer registers");

        OptionSpec<File> binariesArg = optionParser
                .accepts("binaries")
                .withRequiredArg()
                .required()
                .ofType(File.class)
                .withValuesSeparatedBy(pathSeparatorChar);

        OptionSet options = optionParser.parse(args);

        SimulatorConfig config = new SimulatorConfig(options.has(interactiveArg), options.valueOf(verilogTestbenchArg), options.valueOf(schedulerArg), options.valueOf(timerArg), options.valuesOf(binariesArg), options.has(printWorkspaceMemArg));

        if (config.binaries.size() == 0) {
            throw new IllegalArgumentException("At least one binary must be supplied.");
        } else if (config.binaries.size() > 4) {
            // TODO: lift this restriction?
            throw new IllegalArgumentException("At most four binaries can be supplied.");
        }

        return config;
    }

    public static void run(String[] args) throws Exception {
        SimulatorConfig config = parseOptions(args);

        Transputer[] transputers;
        HashSet<Transputer> activeTransputers;
        boolean anyTransputerActive = true;
        boolean hitBreak;
        boolean currentlyInteractive = config.interactive;
        long loopCount = 0;
        PrintWriter stdout = new PrintWriter(System.out);
        PrintWriter stderr = new PrintWriter(System.err);
        Scanner stdin = new Scanner(System.in);

        FileWriter testCheckerFileWriter = null;
        PrintWriter testCheckerPrintWriter = null;
        FileWriter schedCheckerFileWriter = null;
        PrintWriter schedCheckerPrintWriter = null;
        FileWriter timerCheckerFileWriter = null;
        PrintWriter timerCheckerPrintWriter = null;
        //int i, j;
        boolean worked;

        if (config.testChecker != null) {
            // Open output file and write initial state
            testCheckerFileWriter = new FileWriter(config.testChecker, false);
            testCheckerPrintWriter = new PrintWriter(testCheckerFileWriter);
        }
        if (config.schedChecker != null) {
            // Open output file and write initial state
            schedCheckerFileWriter = new FileWriter(config.schedChecker, false);
            schedCheckerPrintWriter = new PrintWriter(schedCheckerFileWriter);
        }
        if (config.timerChecker != null) {
            timerCheckerFileWriter = new FileWriter(config.timerChecker, false);
            timerCheckerPrintWriter = new PrintWriter(timerCheckerFileWriter);
        }


        stdout.printf("# Loading\n");
        transputers = new Transputer[config.binaries.size()];
        activeTransputers = new HashSet<>();
        for (int i = 0; i < config.binaries.size(); i++) {
            transputers[i] = new Transputer((byte) i, stdout, stderr);
            transputers[i].loadProgram(config.binaries.get(i));
            transputers[i].printRecentMemory(stdout);
            transputers[i].printRegisters(stdout);
            if (config.testChecker != null) {
                transputers[i].logState(0, testCheckerPrintWriter);
            }
            if (config.schedChecker != null) {
                transputers[i].logSched(0, schedCheckerPrintWriter);
            }
            if (config.timerChecker != null) {
                transputers[i].logTimer(0, timerCheckerPrintWriter);
            }
            activeTransputers.add(transputers[i]);
        }

        stdout.printf("# Starting\n");
        stdout.flush();
        stderr.flush();

        while (anyTransputerActive) {
            // Check if we hit any breakpoint
            hitBreak = false;
            for (Transputer transputer : transputers) {
                if (activeTransputers.contains(transputer)) {
                    hitBreak = hitBreak || transputer.debuggerState.breakpoints.contains(transputer.registers.Iptr);
                }
            }

            if (currentlyInteractive || hitBreak) {
                currentlyInteractive = interact(transputers, stdin, stdout, stderr);
            }

            anyTransputerActive = false;
            loopCount += 1;
            for (Transputer transputer : transputers) {
                if (activeTransputers.contains(transputer)) {
                    worked = transputer.performStep();
                    transputer.printRegisters(stdout);
                    transputer.incrementClock(loopCount);
                    if (transputer.programEndPtr < transputer.registers.Iptr ||
                            transputer.registers.Iptr < TransputerConstants.CODESTART) {
                        activeTransputers.remove(transputer);
                    } else {
                        anyTransputerActive = true;
                        // Check LinkIn
                        for (int j = 0; j < TransputerConstants.IN_PORTS; j++) {
                            transputer.processInputLink(transputer.inputLinks[j]);
                        }
                        // Check LinkOut
                        transputer.processOutputLink();
                    }
                    if (config.testChecker != null) {
                        transputer.logState(loopCount - 1, testCheckerPrintWriter);
                    }
                    if (config.schedChecker != null) {
                        transputer.logSched(loopCount - 1, schedCheckerPrintWriter);
                    }
                    if (worked && config.timerChecker != null) {
                        transputer.logTimer(loopCount - 1, timerCheckerPrintWriter);
                    }

                    if (!worked) {
                        activeTransputers.remove(transputer);
                    }
                }
            }
            Transputer.switchStep(transputers, stdout);

            stdout.flush();
            stderr.flush();
        }

        for (Transputer transputer : transputers) {
            transputer.printRegisters(stdout);
            transputer.printRecentMemory(stdout);
        }

        if (config.testChecker != null) {
            testCheckerFileWriter.close();
            stdout.printf("# Closed log file for testing\n");
        }
        if (config.schedChecker != null) {
            schedCheckerFileWriter.close();
            stdout.printf("# Closed log file for scheduler checking\n");
        }
        if (config.timerChecker != null) {
            timerCheckerFileWriter.close();
            stdout.printf("# Closed log file for timer checking\n");
        }

        if (config.printWorkspaceMemory) {
            stdout.printf("# Workspace memory usage\n");
            for (Transputer transputer : transputers) {
                transputer.printWorkspaceMemory(stdout);
            }
        }

        stdout.println();
        stdout.printf("# Total steps: %d\n", loopCount);
        stdout.printf("\n==DONE==\n");

        stdout.flush();
        stderr.flush();
    }

}
