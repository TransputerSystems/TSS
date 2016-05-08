package uk.co.transputersystems.occam;

import uk.co.transputersystems.occam.il.*;
import uk.co.transputersystems.occam.metadata.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TSILGeneratorHelpers {

    static int tempCounter = 0;

    public static String generateTempVariable(){
        return "~temp" + tempCounter++;
    }

    /**
     *
     * @param operand
     * @return
     */
    public static NamedOperand resolveAbbreviations(NamedOperand<?> operand) {
        // TODO: Chase through array abbreviations which lead to constant values
        // (because you can create an array abbreviation for a part of a value abbreviation where the value
        // abbreviation was an abbreviation of an array).

        if(operand.isConstant()) {
            return operand;
        }

        NamedOperand<?> currentOperand = operand;

        while (currentOperand instanceof Abbreviation && !(currentOperand instanceof ArrayAbbreviation)) {
            currentOperand = ((Abbreviation) currentOperand).resolveHiddenOperand();
        }

        return currentOperand;
    }

    /**
     * Given some constant value, generate a series of IL ops that load that constant.
     * @param value The constant value
     * @param typeName The name of the type of the constant value
     * @param <T> The type of the constant
     * @return The generated IL ops
     */
    public static <T> ILBlock<UUID,ILOp<UUID>> loadConstant(T value, String typeName) {
        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();
        switch (typeName) {
            case "INT":
                result.add(new LoadConstant<>(UUID.randomUUID(), value.toString(), ""));
                break;
            case "BYTE":
                result.add(new LoadConstant<>(UUID.randomUUID(), value.toString(), ""));
                break;
            default:
                // TODO: Support loading constants of more types
                System.out.print("Don't know how to load constant of type '" + typeName + "' : " + value.toString());
                break;
        }

        return result;
    }

    public static ILBlock<UUID,ILOp<UUID>> parallel(List<ILBlock<UUID,ILOp<UUID>>> statementBlocks, List<Integer> workspaceIds) {
        if (statementBlocks.size() == 1) {
            return statementBlocks.get(0);
        } else {
            ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();

            ILOp<UUID> endOp = new Skip<>(UUID.randomUUID(), "");
            ILOp<UUID> initOp = new InitProcesses<>(UUID.randomUUID(), statementBlocks.size(), endOp.getId(), workspaceIds, "");

            List<UUID> startOps = new ArrayList<>();
            startOps.add(initOp.getId());

            result.add(initOp);
            for (int i = 1; i < statementBlocks.size(); i++) {
                ILBlock<UUID,ILOp<UUID>> block = statementBlocks.get(i);
                StartProcess<UUID> startOp = new StartProcess<>(UUID.randomUUID(), block.get(0).getId(), ProcessPriorities.Current, "");
                startOps.add(startOp.getId());
                result.add(startOp);
            }

            for (int i = 0; i < statementBlocks.size(); i++) {
                ILBlock<UUID,ILOp<UUID>> block = statementBlocks.get(i);
                block.get(0).setComment("[Process" + Integer.toString(i) + "]");
                result.appendBlock(block);
                result.add(new EndProcess<>(UUID.randomUUID(), startOps.get(i), ""));
            }

            result.add(endOp);

            return result;
        }
    }

    /**
     * Converts a character literal to its decimal integer value. If the literal is invalid, throws an `IllegalArgumentException`.
     *
     * Occam guarantees support for the ASCII characters in the range `#20` through `#7F` inclusive, plus the escape sequences below:
     *
     * * `*c`, `*C`: carriage return
     * * `*n`, `*N`: newline
     * * `*t`, `*T`: tab
     * * `*s`, `*S`: space
     * * `*'`: single quote
     * * `*"`: double quote
     * * `**`: asterisk
     *
     * @param literal A character literal without any enclosing quotes
     * @return The decimal integer representation of that character.
     */
    public static int parseCharacterLiteral(String literal) throws IllegalArgumentException {

        if (literal.length() < 1) {
            throw new IllegalArgumentException("A character literal must have at least one character.");
        }

        if (literal.charAt(0) == '*') {
            // Handle escape sequences
            if (literal.length() == 2) {
                switch (literal.charAt(1)) {
                    case 'c':
                    case 'C':

                        return 13;
                    case 'n':
                    case 'N':
                        return 10;
                    case 't':
                    case 'T':
                        return 9;
                    case 's':
                    case 'S':
                        return 32;
                    case '\'':
                        return 39;
                    case '"':
                        return 34;
                    case '*':
                        return 42;
                    default:
                        break;
                }
            } else if (literal.length() == 4 && literal.charAt(1) == '#') {
                return Integer.parseInt(literal.substring(2, 4), 16);
            } else {
                throw new IllegalArgumentException(String.format("Illegal literal escape sequence %s", literal));
            }
        } else {
            // Otherwise, check that this is a normal character literal and convert it
            if (literal.length() != 1) {
                throw new IllegalArgumentException("A normal character literal must be exactly one character.");
            } else {
                int literalValue = (int)literal.charAt(0);
                if (32 <= literalValue && literalValue <= 127) {
                    return literalValue;
                } else {
                    throw new IllegalArgumentException(String.format("The character %c is not permitted in a character literal.", literal.charAt(0)));
                }
            }
        }

        throw new IllegalArgumentException("The character literal provided is not legal.");
    }

    public static void abbreviate(Scope currentScope, NamedOperand operand, String name, String spec) {
        if (operand instanceof Channel || operand instanceof ChannelAbbreviation) {
            currentScope.addAbbreviation(new ChannelAbbreviation(currentScope, name, spec, operand.getName()));
        } else if (operand instanceof Variable || operand instanceof Argument || operand instanceof VariableAbbreviation) {
            currentScope.addAbbreviation(new VariableAbbreviation(currentScope, name, spec, operand.getName()));
        } else if (operand instanceof Timer || operand instanceof TimerAbbreviation) {
            currentScope.addAbbreviation(new TimerAbbreviation(currentScope, name, operand.getName()));
        } else if (operand instanceof Port || operand instanceof PortAbbreviation) {
            currentScope.addAbbreviation(new PortAbbreviation(currentScope, name, spec, operand.getName()));
        } else if (operand instanceof ArrayAbbreviation || operand instanceof ValueAbbreviation) {
            currentScope.addAbbreviation(new VariableAbbreviation(currentScope, name, spec, operand.getName()));
        } else {
            //TODO: Throw an error
        }
    }
}
