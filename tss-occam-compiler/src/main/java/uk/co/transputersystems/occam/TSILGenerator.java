package uk.co.transputersystems.occam;

import org.antlr.v4.runtime.tree.TerminalNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.co.transputersystems.occam.il.*;
import uk.co.transputersystems.occam.metadata.*;

import java.util.*;

public class TSILGenerator extends OccamBaseVisitor<List<ILBlock<UUID,ILOp<UUID>>>> {


    private LibraryInformation libraryInfo;

    public TSILGenerator(LibraryInformation libraryInfo) {
        super();

        this.libraryInfo = libraryInfo;
    }

    public LibraryInformation getLibraryInfo() {
        return libraryInfo;
    }

    public void setLibraryInfo(LibraryInformation libraryInfo) {
        this.libraryInfo = libraryInfo;
    }

    /*

    Sample function structure:

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitSmall_stmt_skip(OccamParser.Small_stmt_skipContext ctx) {
        List<ILBlock<UUID,ILOp<UUID>>> result = new ArrayList<>();
        //result.add(new )
        return result;
    }
    */

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitFile_input(OccamParser.File_inputContext ctx) {
        List<ILBlock<UUID,ILOp<UUID>>> result = new ArrayList<>();
        for (OccamParser.SpecificationContext context : ctx.specification()) {
            result.addAll(visit(context));
        }
        return result;
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitSpecificationAbrv(OccamParser.SpecificationAbrvContext ctx) {
        return visit(ctx.abbreviation());
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitSpecificationDef(OccamParser.SpecificationDefContext ctx) {
        return visit(ctx.definition());
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitSpecificationDec(OccamParser.SpecificationDecContext ctx) {
        return visit(ctx.declaration());
    }

    /**
     * Generates the il for a process definition.
     *
     * Syntax:
     *
     * ```
     * PROC name ( formal, formal ... )
     * statements
     * ```
     *
     * @param ctx
     * @return
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitDef_PROC(OccamParser.Def_PROCContext ctx) {
        /* Notes:
         *
         * A process definition has the following grammar:
         *      PROC NAME '(' ( formal (',' formal)* )? ')' NL ( INDENT stmt DEDENT )?  ':'
         *
         * A process is a flat list of il ops for the statements proceeded by a label for the
         * process. The process needs to be added to the global state tracking for later.
         *
         */

        Scope rootScope = libraryInfo.getCurrentScope();

        // Everything inside the `PROC` is in a new scope, so add one to the stack
        String procedureName = ctx.NAME().getText();
        Scope functionScope = libraryInfo.pushNewFunctionScope(procedureName);

        // Update the current scope with information about each of the `PROC`'s `formal`s
        for (OccamParser.FormalContext formal : ctx.formal()) {
            visit(formal);
        }

        ILBlock<UUID,ILOp<UUID>> functionBlock = new ILBlock<>(functionScope.getScopeId(), true);
        functionBlock.add(new Label(UUID.randomUUID(), procedureName, true, ""));
        functionBlock.add(new MethodStart<>(UUID.randomUUID(), "Start of `" + procedureName + "`"));

        List<ILBlock<UUID,ILOp<UUID>>> statementBlocks = visit(ctx.stmt());
        List<ILBlock<UUID,ILOp<UUID>> > result = functionBlock.mergeBlockList(statementBlocks);

        while (libraryInfo.getCurrentScope() != rootScope) {
            libraryInfo.popScope();
        }

        functionBlock.add(new MethodEnd<>(UUID.randomUUID(), "End of `" + procedureName + "`"));

        libraryInfo.getCurrentScope().pushConstruction(true);

        return result;
    }

    /**
     * Add a list of `formal`s (i.e. parameters) to the current function scope.
     * Each formal can have a single specifier, but this may apply to multiple identifiers.
     *
     * Syntax:
     *
     * ```
     * specifier name [name, name ...] | VAL specifier name [name, name ...]
     * ```
     *
     * @param ctx An AST node containing a formal
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitFormal(OccamParser.FormalContext ctx) {

        // Add the `formal`'s `specifier` (i.e. data type) to the construction stack
        visit(ctx.specifier());

        // Retrieve the current scope and the type name that was just pushed to the construction stack
        Function functionScope = (Function) libraryInfo.getCurrentScope();
        String typeName = (String) functionScope.popConstruction();

        // Determine whether the `formal` is to be passed by value
        boolean passByVal = (ctx.VAL() != null);

        // For each identifier, add an argument to the function's scope
        for (TerminalNode nameNode : ctx.NAME()) {
            functionScope.addArgument(new Argument(nameNode.getText(), typeName, passByVal));
        }

        // No need to return IL
        return null;
    }

    /**
     * Wrapper method.
     * Passes through to a method which adds the name of a type to the construction stack
     *
     * @param ctx A `specifier` AST node which is a `data_type`
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitSpecifier_data_type(OccamParser.Specifier_data_typeContext ctx) {
        visit(ctx.data_type());
        return null;
    }

    /**
     * Add the "BOOL" type name to the construction stack.
     *
     * @param ctx A `data_type` AST node which is a `BOOL`
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitData_type_bool(OccamParser.Data_type_boolContext ctx) {
        libraryInfo.getCurrentScope().pushConstruction("BOOL");
        return null;
    }

    /**
     * Add the "BYTE" type name to the construction stack.
     *
     * @param ctx A `data_type` AST node which is a `BYTE`
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitData_type_byte(OccamParser.Data_type_byteContext ctx) {
        libraryInfo.getCurrentScope().pushConstruction("BYTE");
        return null;
    }

    /**
     * Add the "INT" type name to the construction stack.
     *
     * @param ctx A `data_type` AST node which is a `INT`
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitData_type_int(OccamParser.Data_type_intContext ctx) {
        libraryInfo.getCurrentScope().pushConstruction("INT");
        return null;
    }

    /**
     * Add the "INT16" type name to the construction stack.
     *
     * @param ctx A `data_type` AST node which is a `INT16`
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitData_type_int16(OccamParser.Data_type_int16Context ctx) {
        libraryInfo.getCurrentScope().pushConstruction("INT16");
        return null;
    }

    /**
     * Add the "INT32" type name to the construction stack.
     *
     * @param ctx A `data_type` AST node which is a `INT32`
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitData_type_int32(OccamParser.Data_type_int32Context ctx) {
        libraryInfo.getCurrentScope().pushConstruction("INT32");
        return null;
    }

    /**
     * Add the "INT64" type name to the construction stack.
     *
     * @param ctx A `data_type` AST node which is a `INT64`
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitData_type_int64(OccamParser.Data_type_int64Context ctx) {
        libraryInfo.getCurrentScope().pushConstruction("INT64");
        return null;
    }

    /**
     * Add the "REAL32" type name to the construction stack.
     *
     * @param ctx A `data_type` AST node which is a `REAL32`
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitData_type_real32(OccamParser.Data_type_real32Context ctx) {
        libraryInfo.getCurrentScope().pushConstruction("REAL32");
        return null;
    }

    /**
     * Add the "REAL64" type name to the construction stack.
     *
     * @param ctx A `data_type` AST node which is a `REAL64`
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitData_type_real64(OccamParser.Data_type_real64Context ctx) {
        libraryInfo.getCurrentScope().pushConstruction("REAL64");
        return null;
    }

    /**
     * Add the name of a record type to the construction stack.
     *
     * @param ctx A `data_type` AST node which contains a `NAME` - i.e. a custom type
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitData_type_name(OccamParser.Data_type_nameContext ctx) {
        libraryInfo.getCurrentScope().pushConstruction(ctx.NAME().getText());
        return null;
    }

    /**
     * Add the name of an array type to the construction stack.
     *
     * @param ctx A `data_type` AST node which is an array
     * @return null
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitData_type_expr_data_type(OccamParser.Data_type_expr_data_typeContext ctx) {

        // Retrieve the current scope
        Scope currentScope = libraryInfo.getCurrentScope();

        // Get the type of the array
        // Push type name to construction stack
        visit(ctx.data_type());
        // Pop type name from construction stack
        String typeName = (String) currentScope.popConstruction();

        // Evaluate the (constant) expression specifying the array size
        Integer expressionValue = -1;
        if (ctx.expression() != null) {
            // Push the expression value to the construction stack
            visit(ctx.expression());
            // Pop the expression value from the construction stack
            expressionValue = (Integer) currentScope.popConstruction();
        }

        // Push the name of the array type onto the construction stack
        currentScope.pushConstruction("[" + expressionValue.toString() + "]" + typeName);

        return null;
    }

    /**
     * Visit each subexpression in a list of expressions, returning any IL code generated.
     *
     * Syntax:
     *
     * ```
     * expression [expression, expression ...]
     * ```
     *
     * @param ctx An `expression_list` AST node which is just a list of expressions.
     * @return The IL ops for this list.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitExpression_list_expressions(OccamParser.Expression_list_expressionsContext ctx) {
        // For each expression in the list, visit the expression and add any IL returned to the result
        List<ILBlock<UUID, ILOp<UUID>>> ilBlocks = new ArrayList<>();
        for (OccamParser.ExpressionContext expression : ctx.expression()) {
            List<ILBlock<UUID,ILOp<UUID>>> interimResult = visit(expression);
            if (interimResult != null) {
                if (interimResult.size() > 1) {
                    // TODO: can we remove this error?
                    throw new IllegalArgumentException("Cannot currently evaluate expressions in more than one IL Block.");
                }
                ilBlocks.addAll(interimResult);
            }
        }

        // Push number of expressions in this list onto construction stack
        libraryInfo.getCurrentScope().pushConstruction(ctx.expression().size());

        return ilBlocks;
    }

    /**
     * Visit each variable in a list of variables, returning any IL code generated.
     *
     * Syntax:
     *
     * ```
     * variable [variable, variable ...]
     * ```
     *
     * @param ctx A `variable_list` AST node
     * @return The IL ops for this list.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitVariable_list(OccamParser.Variable_listContext ctx) {

        // Retrieve the current scope
        Scope currentScope = libraryInfo.getCurrentScope();

        List<ILBlock<UUID,ILOp<UUID>>> result = new ArrayList<>();
        List<NamedOperand> constructionResult = new ArrayList<>();

        // For each variable in the list, visit it, add the resulting NamedOperand to the
        // construction stack and then add any resulting IL to the return value
        for (OccamParser.Named_operandContext variable : ctx.named_operand()) {
            // TODO: specific action of this visit()
            List<ILBlock<UUID,ILOp<UUID>>> interimResult = visit(variable);
            constructionResult.add((NamedOperand) currentScope.popConstruction());
            if (interimResult != null) {
                if (interimResult.size() > 1) {
                    //TODO: Throw an error
                }
                result.add(interimResult.get(0));
            }
        }

        currentScope.pushConstruction(constructionResult);

        return result;
    }

    /**
     * Pass-through.
     *
     * @param ctx An `expression` AST node containing an `operand`
     * @return The IL ops generated for the `operand`.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitExpression_operand(OccamParser.Expression_operandContext ctx) {
        //TODO: This might need modifying for other parent situations (e.g. returning IL ops)
        return visit(ctx.operand());
    }

    /**
     * Pass-through.
     *
     * @param ctx An `expression` AST node containing a `literal`
     * @return The IL ops generated for the `literal`.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitOperand_literal(OccamParser.Operand_literalContext ctx) {
        //TODO: This might need modifying for other parent situations (e.g. returning IL ops)
        return visit(ctx.literal());
    }

    /**
     * TODO
     *
     * @param ctx An `operand` AST node containing a `variable`
     * @return TODO
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitOperand_variable(OccamParser.Operand_variableContext ctx) {
        //TODO: This might need modifying for other parent situations (e.g. returning IL ops)

        // Visit the `variable`, which will retrieve its information from the library info
        // and push it to the construction stack
        visit(ctx.named_operand());

        // Now retrieve the scope and pop the `variable` information from the construction stack
        Scope currentScope = libraryInfo.getCurrentScope();
        NamedOperand operand = (NamedOperand) currentScope.popConstruction();

        // Create an empty IL block
        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();

        // If the operand is an `Abbreviation`, retrieve the actual operand the abbreviation represents
        operand = TSILGeneratorHelpers.resolveAbbreviations(operand);

        // If the operand is a constant, push its value to the construction stack and add the IL ops for loading it
        // to the result
        if (operand.isConstant()) {
            currentScope.pushConstruction(operand.getConstantValue());
            result.appendBlock(TSILGeneratorHelpers.loadConstant(operand.getConstantValue(), operand.getTypeName()));
        } else {
            // Otherwise, push the `NamedOperand` itself to the construction stack
            currentScope.pushConstruction(operand);

            // Depending on the kind of the operand, add the appropriate Load* instruction to the ILBlock
            if (operand instanceof Variable) {
                Variable variable = (Variable) operand;
                if (libraryInfo.isGlobal(variable)) {
                    result.add(new LoadGlobal<>(UUID.randomUUID(), variable.getName(), "", false));
                } else {
                    result.add(new LoadLocal<>(UUID.randomUUID(), variable.getIndex(), "", false));
                }
            } else if (operand instanceof Argument) {
                Argument argument = (Argument) operand;
                result.add(new LoadArgument<>(UUID.randomUUID(), argument.getIndex(), "", false));
            } else if (operand instanceof ArrayAbbreviation) {
                throw new NotImplementedException();
                //TODO: Load Reference
                //TODO: Load Element
            }
        }

        return Collections.singletonList(result);
    }

    /**
     * Generate the IL required to load a literal integer, and push the value of the literal onto the construction stack.
     * @param ctx A `literal_integer` AST node
     * @return The IL ops required to load the integer.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitLiteral_integer(OccamParser.Literal_integerContext ctx) {
        //TODO: Handle data type specifier
        //TODO: Handle the other literal types

        // Retrieve the current scope
        Scope currentScope = libraryInfo.getCurrentScope();

        // Initialise an empty list of IL ops
        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();

        // Get the source text representing the integer
        String valueStr = ctx.INTEGER().getText();
        Long value;

        if (valueStr.startsWith("#")) {
            // Parse the string as a hex integer
            value = Long.valueOf(valueStr.substring(1), 16);
        } else {
            // Or parse the string as a decimal integer
            value = Long.valueOf(valueStr, 10);
        }

        // Push the parsed value of the literal integer onto the construction stack
        currentScope.pushConstruction(value);

        // Generate the IL ops for loading the literal and add them to the result
        //TODO: Take into account the type specifier
        result.appendBlock(TSILGeneratorHelpers.loadConstant(value, "INT"));

        return Collections.singletonList(result);
    }

    /**
     * Generate the IL required to load a literal byte, and push the value of the byte onto the construction stack.
     * @param ctx A `literal_byte` AST node
     * @return The IL ops required to load the byte.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitLiteral_byte(OccamParser.Literal_byteContext ctx) {
        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();

        // Retrieve the byte literal text from the parse tree
        String byteLiteral = ctx.BYTE_LITERAL().getText();

        // Remove the quote marks and convert the literal to an integer value
        int byteValue = TSILGeneratorHelpers.parseCharacterLiteral(byteLiteral.substring(1, byteLiteral.length() - 1));

        // Generate IL op for loading the literal
        result.add(new LoadConstant<>(UUID.randomUUID(), String.valueOf(byteValue), "Byte literal: " + byteLiteral));

        // Push the parsed value of the literal onto the construction stack
        libraryInfo.getCurrentScope().pushConstruction(byteValue);

        return Collections.singletonList(result);
    }

    /**
     * Generate the IL required to load a `true` boolean literal, and push it onto the construction stack.
     * @param ctx A `literal_true` AST node
     * @return The IL ops required to load the `true` value
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitLiteral_true(OccamParser.Literal_trueContext ctx) {
        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();

        result.add(new LoadConstant<>(UUID.randomUUID(), "1", ""));
        libraryInfo.getCurrentScope().pushConstruction("1");

        return Collections.singletonList(result);
    }

    /**
     * Generate the IL required to load a `false` boolean literal, and push it onto the construction stack.
     * @param ctx A `literal_false` AST node
     * @return The IL ops required to load the `false` value
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitLiteral_false(OccamParser.Literal_falseContext ctx) {
        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();

        result.add(new LoadConstant<>(UUID.randomUUID(), "0", ""));
        libraryInfo.getCurrentScope().pushConstruction("0");

        return Collections.singletonList(result);
    }



    /**
     * Pass-through.
     *
     * @param ctx A `stmt` AST node of type `compound_stmt`
     * @return The result of visiting the `compound_stmt`
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitStmt_compound_stmt(OccamParser.Stmt_compound_stmtContext ctx) {
        return visit(ctx.compound_stmt());
    }

    /**
     * Pass-through.
     *
     * @param ctx A `stmt` AST node of type `simple_stmt`
     * @return The result of visiting the `simple_stmt`
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitStmt_simple_stmt(OccamParser.Stmt_simple_stmtContext ctx) {
        return visit(ctx.simple_stmt());
    }

    /**
     * Generate the IL required for a `PRI` or `PRI PAR` block.
     * TODO: what is pushed to the construction stack?
     *
     * ```
     * PAR
     *      simple_stmt | stmt
     *      stmt?
     *      stmt?
     * ```
     *
     * or
     *
     * ```
     * PRI PAR
     *      simple_stmt | stmt
     *      stmt?
     *      stmt?
     * ```
     *
     * @param ctx A (non-replicated) `parallel` AST node containing a `PAR` or `PRI PAR`
     * @return TODO
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitParallel_pripar_suite(OccamParser.Parallel_pripar_suiteContext ctx) {
        if (ctx.PRI() != null) {
            //TODO: Priority parallels need implementing
            throw new NotImplementedException();
            //return null;
        } else {
            /* Notes:
             * Normal parallel has the following structure:
             *
             * PAR
             *  suite
             *  suite
             *
             * where suite is:
             *
             * stmt
             * stmt
             * ...
             *
             * or:
             *
             * simple_stmt
             *
             * In either case, the statements in the suite constitute independent il blocks that will be executed in
             * parallel.
             *
             * Parallel is implemented as follows in the IL:
             *
             * [IDSx = ID of the StartProcess IL op for the block at index x]
             * [IDRx = ID of the first IL op for the block at index x]
             *
             * IDS0    :    InitProcesses (n, IDEnd)
             * IDS1    :    StartProcess  (IDR1)
             * IDS(x)  :    StartProcess  (IDRx)
             *         ...
             * IDS(n-1):    StartProcess (IDR(n-1))
             *
             *
             *              [Statements for block 0]
             *              EndProcess (IDS0)
             *
             * IDR1    :    [Statements for block 1]
             *              EndProcess (IDS1)
             *
             * IDRx    :    [Statements for block x]
             *              EndProcess (IDSx)
             *
             *         ...
             *
             * IDR(n-1):    Statements for block n-1
             *              EndProcess (IDS(n-1))
             *
             * IDEnd   :    SKIP
             *
             * The final SKIP operation is the end of the parallel block at which
             * the process should continue executing.
             */

            // Push 'true' onto the construction stack of the current scope
            //  Popped by visiting the suite. Indicates that the statements within the suite
            //  will be executed in parallel (i.e. in new processes) as opposed to in sequence
            // (i.e. in the current process)
            libraryInfo.getCurrentScope().pushConstruction(true);

            // Get the current scope
            Scope currentScope = libraryInfo.getCurrentScope();

            // Get the total number of child scopes already in existence, prior to processing the suite
            //  * This acts as the starting index for new scopes created by visiting the suite
            //  * The suite is for a parallel (see pushConstruction(true) above) and thus a new scope
            //      will be created for each statement within the suite
            //  * Each scope created by visiting the suite is also a new workspace because it will be
            //      executing in a new process
            int childScopeIndex = currentScope.getChildren().size();

            // Generate the IL for the statements inside the PAR block
            //  * Visiting the suite also causes a new scope to be created for each statement within the suite
            //      (see above for more details)
            List<ILBlock<UUID,ILOp<UUID>>> statementBlocks = visit(ctx.suite());

            // Initialise an array to store the workspace IDs of each parallel process (i.e. statement)
            List<Integer> workspaceIds = new ArrayList<>(statementBlocks.size());

            // Retrieve child scopes from the current scope
            List<Scope> childScopes = currentScope.getChildren();

            // Register the workspace ID for each process
            //  The new workspaces (i.e. new scopes) for each new process (i.e. each statement within the suite)
            //  were created visiting the suite. They will have been added as children to the current scope.
            //  Thus all scopes from the index before processing the suite (see above) to the last one in the list
            //  are new workspaces for the new processes of the parallel.
            for (int i = 0; i < statementBlocks.size(); i++) {
                Scope theChildScope = childScopes.get(childScopeIndex + i);
                workspaceIds.add(theChildScope.getWorkspace().getId());
            }

            // Now generate the parallelised IL code for the statements (see Javadoc comment)
            ILBlock<UUID,ILOp<UUID>> result = TSILGeneratorHelpers.parallel(statementBlocks, workspaceIds);

            return Collections.singletonList(result);
        }
    }


    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitParallel_pripar_replicator(OccamParser.Parallel_pripar_replicatorContext ctx) {
        if (ctx.PRI() != null) {
            //TODO: Priority parallels need implementing

            return null;
        } else {
            /* Notes:
             *      Normal replicated parallel has the following structure:
             *
             *      PAR x = base FOR count
             *          STMT
             *
             *      Replicated parallel is implemented just like a normal parallel but where the
             *      IL for each parallel process is re-processed each time with the replicator variable
             *      defined as a (constant) value abbreviation.
             */

            //TODO: Allow base and count to be runtime variables instead of requiring constants

            Scope rootScope = libraryInfo.getCurrentScope();

            visit(ctx.replicator());

            List<ValueAbbreviation> replicatorAbbreviations = (List<ValueAbbreviation>) rootScope.popConstruction();
            List<ILBlock<UUID,ILOp<UUID>>> statementBlocks = new ArrayList<>();
            List<Integer> workspaceIds = new ArrayList<>();
            boolean doneFirst = false;
            for (ValueAbbreviation replicatorAbbreviation : replicatorAbbreviations) {
                Scope newScope = libraryInfo.pushNewScope();
                Integer newWorkspaceId;
                // The first new process of a set of parallels actually executes in the current process, so no
                //  new workspace is created
                if (doneFirst) {
                    newWorkspaceId = newScope.newWorkspace(false).getId();
                } else {
                    newWorkspaceId = newScope.getWorkspace().getId();
                }
                workspaceIds.add(newWorkspaceId);
                libraryInfo.getCurrentScope().addAbbreviation(replicatorAbbreviation);
                List<ILBlock<UUID,ILOp<UUID>>> statementBlock = visit(ctx.stmt());
                statementBlocks.add(statementBlock.get(0));
                libraryInfo.popScope();

                doneFirst = true;
            }

            ILBlock<UUID,ILOp<UUID>> result = TSILGeneratorHelpers.parallel(statementBlocks, workspaceIds);
            return Arrays.asList(result);
        }
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitReplicator(OccamParser.ReplicatorContext ctx) {

        //TODO: Allow non-constant (i.e. runtime variable) base
        //TODO: Allow non-constant (i.e. runtime variable) count

        Scope currentScope = libraryInfo.getCurrentScope();

        String name = ctx.NAME().getText();

        visit(ctx.base());
        Long base = (Long) currentScope.popConstruction();
        visit(ctx.count());
        Long count = (Long) currentScope.popConstruction();
        List<ValueAbbreviation> result = new ArrayList<>();
        for (Integer i = 0; i < count; i++) {
            result.add(new ValueAbbreviation<>(null, name, "INT", i + base));
        }
        currentScope.pushConstruction(result);

        return null;
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitBase(OccamParser.BaseContext ctx) {
        visit(ctx.expression());
        return null;
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitCount(OccamParser.CountContext ctx) {
        visit(ctx.expression());
        return null;
    }

    /**
     * Generate the IL for a suite (i.e. a group of statements). Expects a boolean on top of the construction stack
     * where `true` indicates that the statements are going to be executed in parallel (`false` indicates sequential).
     *
     * @param ctx The `suite` AST node
     * @return The IL ops generated for the suite
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitSuite(OccamParser.SuiteContext ctx) {
        List<ILBlock<UUID,ILOp<UUID>>> result;

        // Determine, from the construction stack, whether this suite is inside a parallel block. If so, the management
        // of scopes and workspaces is different
        boolean forParallel = (Boolean) libraryInfo.getCurrentScope().popConstruction();

        // Get the initial scope
        Scope rootScope = libraryInfo.getCurrentScope();

        // If the suite contains a `simple_stmt`, process that
        if (ctx.simple_stmt() != null) {
            // If the statement should be executed in parallel, create a new scope
            //  but let the caller decide whether to associate it with a new workspace or not
            if (forParallel) {
                Scope newScope = libraryInfo.pushNewScope();
            }

            // Generate the IL ops for the statement
            result = visit(ctx.simple_stmt());

            // Pop scopes until we return to the original scope we had on entering this method
            if (forParallel) {
                while (libraryInfo.getCurrentScope() != rootScope) {
                    libraryInfo.popScope();
                }
            }
        } else {
            // Otherwise, process each statement in turn.
            result = new ArrayList<>();
            boolean doneFirst = false;
            for (OccamParser.StmtContext statement : ctx.stmt()) {
                // If the statements should be executed in parallel, create a new scope for this statement and,
                // if it is not the first statement in the parallel, associate it with a new workspace
                if (forParallel) {
                    Scope newScope = libraryInfo.pushNewScope();
                    if (doneFirst) {
                        newScope.newWorkspace(false);
                    }
                    doneFirst = true;
                }

                // Generate the IL blocks for this statement
                List<ILBlock<UUID,ILOp<UUID>>> blocks = visit(statement);

                // Return to the scope we were in on entering this method
                if (forParallel) {
                    while (libraryInfo.getCurrentScope() != rootScope) {
                        libraryInfo.popScope();
                    }
                }

                // If some IL ops were returned, add them to the result
                // TODO: in future, the compiler should throw an error here if nothing is returned. A null result is ignored for now because not many features are implemented
                if (blocks != null) {
                    result.addAll(blocks);
                }
            }
        }

        // If the statements were to be executed sequentially, we will not have returned to the original scope yet -
        // so do it now
        if (!forParallel) {
            while (libraryInfo.getCurrentScope() != rootScope) {
                libraryInfo.popScope();
            }
        }

        return result;
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitSimple_stmt(OccamParser.Simple_stmtContext ctx) {
        return visit(ctx.small_stmt());
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitSmall_stmt_skip(OccamParser.Small_stmt_skipContext ctx) {
        List<ILBlock<UUID,ILOp<UUID>>> result = new ArrayList<>();
        result.add(new ILBlock<>(new Skip<>(UUID.randomUUID(), "")));
        return result;
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitSmall_stmt_assignment(OccamParser.Small_stmt_assignmentContext ctx) {
        return visit(ctx.assignment());
    }

    @Override
    public List<ILBlock<UUID, ILOp<UUID>>> visitSmall_stmt_input(OccamParser.Small_stmt_inputContext ctx) {
        List<ILBlock<UUID, ILOp<UUID>>> ilBlocks = visit(ctx.input());
        //Discard the two values on the construction stack from visiting input
        libraryInfo.getCurrentScope().popConstruction();
        libraryInfo.getCurrentScope().popConstruction();
        return ilBlocks;
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitAssignment(OccamParser.AssignmentContext ctx) {
        /* Notes:
         *
         * Structure:
         *      Statements for expression 0
         *                  ...
         *      Statements for expression n
         *      Store [local/argument/global] n
         *                  ...
         *      Store [local/argument/global] 0
         */

        Scope currentScope = libraryInfo.getCurrentScope();

        visit(ctx.variable_list());
        List<NamedOperand> lvalues = (List<NamedOperand>) currentScope.popConstruction();
        ILBlock<UUID, ILOp<UUID>> opILBlock = new ILBlock<>();
        List<ILBlock<UUID,ILOp<UUID>>> rvalueBlocks = opILBlock.mergeBlockList(visit(ctx.expression_list()));

        Integer numRvalues = (Integer) currentScope.popConstruction();

        if (lvalues.size() != numRvalues) {
            throw new IllegalArgumentException("Assignments must have the same number of l-values as r-values.");
        }

        // Add IL ops to store expression values. Note: Storing happens in reverse order.
        for (int i = numRvalues - 1; i > -1; i--) {
            //NOTE: These could be variables, arguments or abbreviations
            NamedOperand operand = lvalues.get(i);

            operand = TSILGeneratorHelpers.resolveAbbreviations(operand);

            if (operand instanceof Variable) {
                Variable variable = (Variable) operand;
                if (libraryInfo.isGlobal(variable)) {
                    opILBlock.add(new StoreGlobal<>(UUID.randomUUID(), variable.getName(), ""));
                } else {
                    opILBlock.add(new StoreLocal<>(UUID.randomUUID(), variable.getIndex(), ""));
                }
            } else if (operand instanceof Argument) {
                Argument argument = (Argument) operand;
                opILBlock.add(new StoreArgument<>(UUID.randomUUID(), argument.getIndex(), ""));
            } else if (operand instanceof ArrayAbbreviation) {
                //TODO: Load Reference
                //TODO: Store Element
            }
        }

        return rvalueBlocks;
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitDeclaration(OccamParser.DeclarationContext ctx) {
        Scope currentScope = libraryInfo.getCurrentScope();

        ILBlock<UUID, ILOp<UUID>> ilBlock = new ILBlock<>();

        if (ctx.data_type() != null) {
            visit(ctx.data_type());

            String dataTypeName = (String) currentScope.getConstructionStack().pop();

            for (TerminalNode name : ctx.NAME()) {
                currentScope.addVariable(name.getText(), dataTypeName);
                if (!(currentScope instanceof FileInformation)) {
                    currentScope = libraryInfo.pushNewScope();
                }
            }


        } else if (ctx.channel_type() != null) {
            visit(ctx.channel_type());

            String dataTypeName = (String) currentScope.getConstructionStack().pop();

            for (TerminalNode name : ctx.NAME()) {
                currentScope.addChannel(name.getText(), dataTypeName);
                ilBlock.add(new InitChannel<>(UUID.randomUUID(), name.getText(), currentScope.getChannel(name.getText()).getIndex(), dataTypeName , ""));
                if (!(currentScope instanceof FileInformation)) {
                    currentScope = libraryInfo.pushNewScope();
                }
            }


        } else if (ctx.port_type() != null) {

            visit(ctx.port_type());

            String dataTypeName = (String) currentScope.getConstructionStack().pop();

            for (TerminalNode name : ctx.NAME()) {
                currentScope.addPort(name.getText(), dataTypeName);
                if (!(currentScope instanceof FileInformation)) {
                    currentScope = libraryInfo.pushNewScope();
                }
            }


        } else {//Timer type
            //throw new NotImplementedException();
        }

        return Collections.singletonList(ilBlock);
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitSequence_suite(OccamParser.Sequence_suiteContext ctx) {
        /* Notes:
         *      Normal sequential has the following structure:
         *
         *      SEQ
         *          SUITE
         *          SUITE
         *
         *      Sequential is implemented as follows:
         *
         *      Statements for block 0
         *              ...
         *      Statements for block n
         */

        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();
        libraryInfo.getCurrentScope().pushConstruction(false);
        List<ILBlock<UUID,ILOp<UUID>>> statementBlocks = visit(ctx.suite());
        return result.mergeBlockList(statementBlocks);
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitChannel_type_protocol(OccamParser.Channel_type_protocolContext ctx) {
        return visit(ctx.protocol());
    }


    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitProtcol_name(OccamParser.Protcol_nameContext ctx) {
        String name = ctx.NAME().getText();
        libraryInfo.getCurrentScope().pushConstruction(name);
        return null;
    }


    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitOutputitem_single_expression(OccamParser.Outputitem_single_expressionContext ctx) {
        return visit(ctx.expression());
    }


    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitPort_type_data_type(OccamParser.Port_type_data_typeContext ctx) {
        return visit(ctx.data_type());
    }


    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitCase_expression(OccamParser.Case_expressionContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitChannel_name(OccamParser.Channel_nameContext ctx) {
        String name = ctx.NAME().getText();
        NamedOperand namedOperand = libraryInfo.searchForNamedOperand(name);

        if (namedOperand instanceof Channel) {

            libraryInfo.getCurrentScope().pushConstruction(namedOperand);

        } else {

            //Throw error, we should be dealing with channels
        }

        return null;
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitInput_named_operand_input_items(OccamParser.Input_named_operand_input_itemsContext ctx) {

        Scope currentScope = libraryInfo.getCurrentScope();

        visit(ctx.named_operand());

        //We have channel on the scope construction stack
        NamedOperand namedOperand = (NamedOperand) libraryInfo.getCurrentScope().popConstruction();

        namedOperand = TSILGeneratorHelpers.resolveAbbreviations(namedOperand);

        ILBlock<UUID,ILOp<UUID>> ilBlock = new ILBlock<>();

        if (namedOperand instanceof Channel) {

            Iterator<OccamParser.Input_itemContext> itemContextIterator = ctx.input_item().iterator();

            try {

                while (itemContextIterator.hasNext()) {

                    OccamParser.Input_itemContext input_itemContext = itemContextIterator.next();

                    visit(input_itemContext);

                    NamedOperand stackTop = (NamedOperand) libraryInfo.getCurrentScope().popConstruction();

                    //TODO: Resolve abbreviations

                    if (stackTop instanceof Variable) {

                        Variable variable = (Variable) stackTop;

                        if (libraryInfo.isGlobal(variable)) {
                            ilBlock.add(new LoadGlobal<UUID>(UUID.randomUUID(), variable.getName(), "Load address", true));
                        } else {
                            ilBlock.add(new LoadLocal<UUID>(UUID.randomUUID(), variable.getIndex(), "Load address", true));
                        }

                    } else if(stackTop instanceof Argument) {

                        ilBlock.add(new LoadArgument<UUID>(UUID.randomUUID(), ((Argument) stackTop).getIndex(), "", true));

                    } else {
                        throw new NotImplementedException();
                    }

                    ilBlock.add(new LoadChannelRef<>(UUID.randomUUID(), ((Channel) namedOperand).getIndex(), ""));
                    ilBlock.add(new LoadConstant<>(UUID.randomUUID(), String.valueOf(libraryInfo.getTypeSize(namedOperand.getTypeName())), ""));
                    ilBlock.add(new ReadChannel<>(UUID.randomUUID(), ""));

                    currentScope.pushConstruction(namedOperand);
                    currentScope.pushConstruction("CHANNEL");

                }

            }catch (Exception e){
                System.out.println("Error calculating variable size");
                System.exit(0);
            }

        }else if (namedOperand instanceof Port) {

            try {

                visit(ctx.input_item(0));

                Object stackTop = libraryInfo.getCurrentScope().popConstruction();

                if (stackTop instanceof Variable) {

                    Variable variable = (Variable) stackTop;

                    if (libraryInfo.isGlobal(variable)) {
                        ilBlock.add(new LoadGlobal<UUID>(UUID.randomUUID(), variable.getName(), "Load address", true));
                    } else {
                        ilBlock.add(new LoadLocal<UUID>(UUID.randomUUID(), variable.getIndex(), "Load address", true));
                    }

                }else if(stackTop instanceof Argument){

                    ilBlock.add(new LoadArgument<UUID>(UUID.randomUUID(),  ((Argument) stackTop).getIndex(), "", true));

                } else {

                    throw new NotImplementedException();

                }

                ilBlock.add(new LoadPortRef<>(UUID.randomUUID(), ((Port) namedOperand).getIndex(), ((Port) namedOperand).getName(), ((Port) namedOperand).getTypeName(), ""));
                ilBlock.add(new LoadConstant<>(UUID.randomUUID(), String.valueOf(libraryInfo.getTypeSize(namedOperand.getTypeName())), ""));
                ilBlock.add(new ReadPort<>(UUID.randomUUID(), ""));

                currentScope.pushConstruction(namedOperand);
                currentScope.pushConstruction("PORT");

            }catch (Exception e){
                System.out.println("Error calculating variable size");
                System.exit(0);
            }

        }else if (namedOperand instanceof uk.co.transputersystems.occam.metadata.Timer){

            ilBlock.add(new ReadTimer<>(UUID.randomUUID(), ""));

            visit(ctx.input_item(0));

            Object stackTop = libraryInfo.getCurrentScope().popConstruction();

            if (stackTop instanceof Variable) {

                Variable variable = (Variable) stackTop;

                if (libraryInfo.isGlobal(variable)) {
                    ilBlock.add(new StoreGlobal<>(UUID.randomUUID(), variable.getName(), "" ));
                } else {
                    ilBlock.add(new StoreLocal<>(UUID.randomUUID(), variable.getIndex(), ""));
                }

            }else if(stackTop instanceof Argument){

                ilBlock.add(new StoreArgument<UUID>(UUID.randomUUID(),  ((Argument) stackTop).getIndex(), ""));

            } else {

                throw new NotImplementedException();

            }

            currentScope.pushConstruction("0");
            currentScope.pushConstruction("TIMER");


        } else if (namedOperand instanceof ArrayAbbreviation) {
            //todo add ArrayAbbreviation channel implementation
            throw new NotImplementedException();
        } else {

            throw new NotImplementedException();
        }

        return Collections.singletonList(ilBlock);
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitInput_item_variable(OccamParser.Input_item_variableContext ctx) {
        return visit(ctx.named_operand());
    }

    /**
     * First visits the two operands and inserts the relevant ILOPs so
     * the two values will be on the top of the stack
     * The dyadic operator is then visited putting the correct ILOP's to calculate
     * the values on the stack
     * @param ctx An `expression_dyadic_operator` AST node
     * @return The IL ops required to evaluate and load the dyadic expression.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitExpression_dyadic_operator(OccamParser.Expression_dyadic_operatorContext ctx) {

        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();

        // Visiting operand will return a List of ILBlocks
        // An expression must return a single ILBlock
        // Therefore we must manually add each element of the List<ILBlock<UUID,ILOp<UUID>>>
        // to a single ILBlock
        List<ILBlock<UUID,ILOp<UUID>>> lhs = visit(ctx.operand(0));

        List<ILBlock<UUID,ILOp<UUID>>> rhs = visit(ctx.operand(1));

        result.appendBlockList(lhs);
        result.appendBlockList(rhs);

        //Two operands at top of the stack
        //ready for the next instruction
        List<ILBlock<UUID,ILOp<UUID>>> op = visit(ctx.dyadic_operator());

        result.appendBlock(op.get(0));

        return Collections.singletonList(result);
    }

    /**
     * Generate the IL ops to evaluate and load the result of a expression subject to a monadic operator.
     * For example:
     * ```
     * - (a+b)
     * |   +---->  Expression
     * +------->  Monadic operator
     *
     * ```
     * @param ctx An `expression_monadic` AST node
     * @return The IL ops to evaluate and load the result of the monadic expression.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitExpression_monadic(OccamParser.Expression_monadicContext ctx) {
        // Initialise an empty result
        ILBlock<UUID,ILOp<UUID>> result_root = new ILBlock<>();
        List<ILBlock<UUID,ILOp<UUID>>> result_list = new ArrayList<>();

        // Generate the IL ops that will load the monadic operator's operand onto the stack
        List<ILBlock<UUID,ILOp<UUID>>> operandBlock = visit(ctx.operand());

        // Append those IL ops to the result
        result_list = result_root.mergeBlockList(operandBlock);

        // Generate the IL ops that will perform the correct monadic operation to the item currently on top of the stack
        List<ILBlock<UUID,ILOp<UUID>>> monadicBlocks = visit(ctx.monadic_operator());
        result_list.addAll(monadicBlocks);
        result_list = new ILBlock<UUID,ILOp<UUID>>().mergeBlockList(result_list);

        return result_list;
    }

    /**
     * Generate the IL ops to perform the provided monadic (unary) operation.
     * The generated code assumes that the operand is already on the stack.
     * @param ctx A `monadic_operator` AST node
     * @return IL ops to perform the operation
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitMonadic_operator(OccamParser.Monadic_operatorContext ctx) {

        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();

        if (ctx.NOT_KWD() != null) {
            result.add(new BooleanNot<>(UUID.randomUUID(), ""));
        } else if (ctx.BITWISE_NOT_KWD() != null) {
            result.add(new BitwiseNot<>(UUID.randomUUID(), ""));
        } else if (ctx.BITWISE_NOT() != null) {
            result.add(new BitwiseNot<>(UUID.randomUUID(), ""));
        } else if (ctx.MINUS() != null){
            result.add(new UnaryMinus<>(UUID.randomUUID(), false, ""));
        } else if (ctx.MINUS_MOD() != null){
            result.add(new UnaryMinus<>(UUID.randomUUID(), true , ""));
        } else if (ctx.SIZE() != null) {
            throw new NotImplementedException();
        }
        else {
            throw new NotImplementedException();
        }

        return Collections.singletonList(result);
    }

    /**
     * Generate the IL ops to perform the provided dyadic (binary) operation.
     * The generated code assumes that the two operands are already on the stack.
     * @param ctx A `dyadic_operator` AST node.
     * @return IL ops to perform the operation.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitDyadic_operator(OccamParser.Dyadic_operatorContext ctx) {

        ILBlock<UUID,ILOp<UUID>> ilBlock = new ILBlock<>();

        //TODO: rest of operators
        //TODO: document any obscure implementation details - especially for overflow-sensitive operations

        if (ctx.PLUS() != null) {
            ilBlock.add(new Add<>(UUID.randomUUID(), false, ""));
        } else if (ctx.MINUS() != null) {
            ilBlock.add(new Subtract<>(UUID.randomUUID(), false, ""));
        } else if (ctx.TIMES() != null) {
            ilBlock.add(new Multiply<>(UUID.randomUUID(), false, ""));
        } else if (ctx.DIVIDE() != null) {
            ilBlock.add(new Divide<>(UUID.randomUUID(), ""));
        } else if(ctx.PLUS_MOD() != null) {
            ilBlock.add(new Add<>(UUID.randomUUID(), true, ""));
        } else if(ctx.MINUS_MOD() != null){
            ilBlock.add(new Subtract<>(UUID.randomUUID(),true , "Minus modulo"));
        } else if(ctx.TIMES_MOD() != null) {
            ilBlock.add(new Multiply<>(UUID.randomUUID(), true, ""));
        } else if(ctx.REM() != null) {
            ilBlock.add(new Modulo<>(UUID.randomUUID(), ""));
        } else if(ctx.BITWISE_AND() != null){
            ilBlock.add(new BitwiseAnd<>(UUID.randomUUID(), ""));
        }else if(ctx.AND_KWD() != null) {
            ilBlock.add(new BooleanAnd<>(UUID.randomUUID(), ""));
        }else if (ctx.BITWISE_OR() != null) {
            ilBlock.add(new BitwiseOr<>(UUID.randomUUID(), ""));
        }else if(ctx.OR_KWD() != null) {
            ilBlock.add(new BooleanOr<>(UUID.randomUUID(), ""));
        } else if(ctx.XOR() != null) {
            ilBlock.add(new BitwiseXor<>(UUID.randomUUID(), ""));
        } else if(ctx.BITWISE_AND_KWD() != null){
            ilBlock.add(new BitwiseAnd<>(UUID.randomUUID(), ""));
        } else if (ctx.GTHAN() != null) {
            ilBlock.add(new CompareGreaterThan<>(UUID.randomUUID(), ""));
        } else if (ctx.LTHAN() != null) {
            ilBlock.add(new CompareLessThan<>(UUID.randomUUID(), ""));
        } else if (ctx.EQUAL() != null) {
            ilBlock.add(new CompareEqual<>(UUID.randomUUID(), ""));
        } else if(ctx.BITOR() != null) {
            ilBlock.add(new BitwiseOr<>(UUID.randomUUID(), ""));
        } else if(ctx.NOTEQ() != null) {
            ilBlock.add(new CompareNotEqual<>(UUID.randomUUID(), ""));
        } else if(ctx.LTHANEQ() != null) {
            ilBlock.add(new CompareLessThanOrEqual<>(UUID.randomUUID(), ""));
        } else if(ctx.GTHANEQ() != null) {
            ilBlock.add(new CompareGreaterThanOrEqual<>(UUID.randomUUID(), ""));
        } else if(ctx.AFTER() != null) {
            ilBlock.add(new After<>(UUID.randomUUID(),""));
        } else if(ctx.RIGHTSHIFT() != null ) {
            ilBlock.add(new RightShift<>(UUID.randomUUID(),""));
        }else if(ctx.LEFTSHIFT() != null) {
            ilBlock.add(new LeftShift<>(UUID.randomUUID(),""));
        } else {
            throw new NotImplementedException();
        }

        return Collections.singletonList(ilBlock);
    }


    /**
     * Pass-though to `visitExpression`.
     * @param ctx An `operand_expression` AST node - i.e. an expression surround by parentheses.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitOperand_expression(OccamParser.Operand_expressionContext ctx) {
        return visit(ctx.expression());
    }

    /**
     * Pass-though to `visitConditional`.
     * @param ctx A `choice_conditional` AST node.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitChoice_conditional(OccamParser.Choice_conditionalContext ctx) {
        return visit(ctx.conditional());
    }

    /**
     * Pass-though to `visitGuarded_choice`.
     * @param ctx A `choice_guarded` AST node.
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitChoice_guarded(OccamParser.Choice_guardedContext ctx) {
        return visit(ctx.guarded_choice());
    }

    /**
     * Visiting the conditional choices will generate the ILOP's for
     * handling conditional choices
     * It calculates each 'choice' in reverse order so each choice knows where
     * to jump to if it's condition is false
     * @param ctx 'Conditional choice' context
     * @return List of ILBlocks for calculating the structure
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitConditional_choices(OccamParser.Conditional_choicesContext ctx) {

        Scope currentScope = libraryInfo.getCurrentScope();

        ILOp<UUID> endLabel = new Label<>(UUID.randomUUID(), false, "");

        currentScope.pushConstruction(endLabel);

        int choiceCount = ctx.choice().size();


        List<ILBlock<UUID,ILOp<UUID>>> result = new ArrayList<>();

        /*

        Summary of the structure of the ILOp's returned

        Bool ilBlocks 0
        Conditional jump to stmt 0
        ...
        ...
        ...
        Bool ilBlocks n
        Conditional jump to stmt n


        Label stmt 0
        Stmt ILBlock<UUID,ILOp<UUID>> 0
        jmp to end
        ...
        ...
        ...
        Label stmt n
        Stmt ILBlock<UUID,ILOp<UUID>> n
        jmp to end

        Label end


        */


        for (int i = choiceCount - 1; i >= 0; i--) {

            libraryInfo.pushNewScope();

            List<ILBlock<UUID,ILOp<UUID>>> choiceBlocks = visit(ctx.choice(i));

            Label<UUID> stmtLabel = new Label<>(UUID.randomUUID(), false, "");

            ILBlock<UUID,ILOp<UUID>> boolBlock = choiceBlocks.get(0);
            ILBlock<UUID,ILOp<UUID>> stmtBlock = choiceBlocks.get(1);

            //If true, branch to statement label
            boolBlock.add(new BranchIfTrue<>(UUID.randomUUID(), stmtLabel.getId(), ""));

            //Label marking start of statement
            stmtBlock.add(0, stmtLabel);
            //Unconditional target to end of the structure
            stmtBlock.add(new Branch<>(UUID.randomUUID(), endLabel.getId(), ""));

            //We are visiting the choices in reverse
            //So add bool block to start of the list
            result.add(0, boolBlock);

            //Add a statement block to the end of the list
            result.add(stmtBlock);

            libraryInfo.popScope();


        }

        ILBlock<UUID,ILOp<UUID>> endBlock = new ILBlock<>(endLabel);

        result.add(endBlock);

        return result;

    }


    /**
     * Visiting a guarded choice context will calculate
     * the ILBlock<UUID,ILOp<UUID>> for the boolean section and statement section
     * A guarded choice has it's own scope.
     * We push a new scope and pop one at the end of the method
     * @param ctx Guarded choice context
     * @return A list of two ILBlocks. Boolean block and Statement block
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitGuarded_choice(OccamParser.Guarded_choiceContext ctx) {

        libraryInfo.pushNewScope();

        List<ILBlock<UUID,ILOp<UUID>>> result = new ArrayList<>();

        // Create the ILBlocks from the statements
        List<ILBlock<UUID,ILOp<UUID>>> statements = visit(ctx.stmt());
        // Create the ILBlocks from the conditions
        List<ILBlock<UUID,ILOp<UUID>>> conditionBlocks = visit(ctx.bool());

        //Flatten the ILBlocks for calculating the boolean expression
        ILBlock<UUID,ILOp<UUID>> conditionBlock = new ILBlock<>();

        //Add the List of ILBlocks from stmts and bools into a
        //single ILBlock<UUID,ILOp<UUID>> each.
        conditionBlock.appendBlockList(conditionBlocks);
        statements = statements.get(0).mergeBlockList(statements);

        result.add(conditionBlock);
        result.addAll(statements);

        libraryInfo.popScope();

        return result;
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitBool(OccamParser.BoolContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitLoop(OccamParser.LoopContext ctx) {

        //Branch bool-conditons
        //Label stmts
        //Statments
        //...
        //...
        //Label  bool-conditons
        //Bool conditions
        //Branch - stmts


        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();

        ILOp<UUID> boolLabel = new Label<>(UUID.randomUUID(), false, "");
        ILOp<UUID> stmtLabel = new Label<>(UUID.randomUUID(), false, "");

        result.add(new Branch<>(UUID.randomUUID(), boolLabel.getId(), ""));
        result.add(stmtLabel);

        List<ILBlock<UUID,ILOp<UUID>>> stmtBlocks = visit(ctx.stmt());
        stmtBlocks = result.mergeBlockList(stmtBlocks);

        result.add(boolLabel);

        //Visit boolean condition and add the returned ILBlocks to the result
        List<ILBlock<UUID,ILOp<UUID>>> boolBlocks = visit(ctx.bool());
        stmtBlocks.remove(result);
        boolBlocks.addAll(stmtBlocks);
        stmtBlocks = result.mergeBlockList(boolBlocks);

        //If true target to statements
        result.add(new BranchIfTrue<>(UUID.randomUUID(), stmtLabel.getId(), ""));

        return stmtBlocks;
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitCase_input(OccamParser.Case_inputContext ctx) {
        return super.visitCase_input(ctx);
    }

    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitChoice_specification(OccamParser.Choice_specificationContext ctx) {

        //Visit the specification which will adjust the scope
        visit(ctx.specification());

        //Return the code ILBlocks generated from visiting the choice
        return visit(ctx.choice());
    }


    /**
     * When visiting a replicated conditional we first visit the replicator
     * context. Here we calculate the number of replications to make by returning a list of
     * Value Abbreviations
     * Currently this only supports constant number of replications due to the value being
     * calculated at compile time.
     * We then visit each choice, this generates ILBlocks from Bool and GuardedChoice context
     * These ILBlocks are then organised and then added to the output of the method
     * @param ctx
     * @return
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitConditional_replicator(OccamParser.Conditional_replicatorContext ctx) {

        Scope currentScope = libraryInfo.getCurrentScope();

        List<ILBlock<UUID,ILOp<UUID>>> result = new ArrayList<>();

        //Visiting replicator adds a list of ValueAbbreviations to the stack
        //Currently does not support non-static variables
        visit(ctx.replicator());

        List<ValueAbbreviation> replicatorAbbreviations = (List<ValueAbbreviation>) currentScope.popConstruction();

        Label<UUID> exitReplicator = new Label<>(UUID.randomUUID(), false, "");

        //Reverse the list so we can easily handle each value abbreviation in reverse
        Collections.reverse(replicatorAbbreviations);

        /*

        Bool block 0
        Conditional jump to stmt 0
        ...
        ...
        ...
        Bool block n
        Conditional jump to stmt n

        Jump to end -  If no bool returns true
        Label stmt 0
        Stmt block 0
        jmp to end
        ...
        ...
        ...
        Label stmt n
        Stmt block n
        jmp to end

        Label end


        */

        //Jump to the end of the replicator block if all bool exp are false
        result.add(new ILBlock<>(new Branch<>(UUID.randomUUID(), exitReplicator.getId(), "")));

        for  ( ValueAbbreviation valueAbbreviation : replicatorAbbreviations ){

            Scope rootScope = libraryInfo.pushNewScope();

            //Add the value abbreviation to the current scope
            rootScope.addAbbreviation(valueAbbreviation);

            //Visiting the choice block will return a list of two ILBlocks
            List<ILBlock<UUID,ILOp<UUID>>> choiceBlock = visit(ctx.choice());
            ILBlock<UUID,ILOp<UUID>> stmtBlock = choiceBlock.get(1);
            ILBlock<UUID,ILOp<UUID>> boolBlock = choiceBlock.get(0);

            Label<UUID> stmtLabel = new Label<>(UUID.randomUUID(), false, "");

            //Branch to the statement if true
            boolBlock.add(new BranchIfTrue<>(UUID.randomUUID(), stmtLabel.getId(), ""));

            //Add a label to the start of the statement
            stmtBlock.add(0 , stmtLabel );
            //Add a unconditional target to the end of the structure
            stmtBlock.add(new Branch<>(UUID.randomUUID(), exitReplicator.getId(), ""));

            //Add the bool block to the start of the ILBlock<UUID,ILOp<UUID>> list
            result.add(0 , boolBlock);

            //Add the statement block to the end of the list
            //The statement is independent so we don't care about it's position
            result.add(stmtBlock);


            libraryInfo.popScope();

        }
        result.add(new ILBlock<>(exitReplicator));
        return result;
    }



    /*
     * Visiting a selection generates the list of ILBlocks for the selection language feature
     * @param ctx Selection context
     * @return List of ILBlocks for the selection
     */
    @Override
    public List<ILBlock<UUID,ILOp<UUID>>> visitSelection(OccamParser.SelectionContext ctx) {

        List<ILBlock<UUID,ILOp<UUID>>> result = new ArrayList<>();
        // Label indicating the end of the selection block - jump here once one of the options has executed
        Label<UUID> exitLabel = new Label<>(UUID.randomUUID(), false, "");
        // The ops required to load the selector
        List<ILBlock<UUID,ILOp<UUID>>> loadSelector = visit(ctx.selector());

        for (OccamParser.OptionContext option : ctx.option()) {
            libraryInfo.getCurrentScope().pushConstruction(exitLabel);
            libraryInfo.getCurrentScope().pushConstruction(loadSelector);
            result.addAll(visit(option));
        }

        //Exit label marking end of selection
        result.add(new ILBlock(exitLabel));

        return result;
    }

    /*
     * Visiting Option case expression block returns the ILBLock code for each 'option' and a single 'stmt'
     * @param ctx The Option case expression context
     * @return List of ILBlocks in the following format: Position 0:stmtBlock , rest of the list: option blocks
     */
    public List<ILBlock<UUID, ILOp<UUID>>> visitOption_case_expression_stmt(OccamParser.Option_case_expression_stmtContext ctx) {

        List<ILBlock<UUID, ILOp<UUID>>> result = new ArrayList<>();

        Scope currentScope = libraryInfo.getCurrentScope();

        // Retrieve the ops required to load the selector so that we can compare our list of case_expressions against it
        List<ILBlock<UUID, ILOp<UUID>>> loadSelector = (List<ILBlock<UUID, ILOp<UUID>>>)(List<?>) currentScope.popConstruction();

        // Retrieve the exit label for the whole selection block, which we jump to if the statement in this option actually gets executed
        Label<UUID> endLabel =  (Label<UUID>) currentScope.popConstruction();

        libraryInfo.pushNewScope();

        // Inserted just before the statement - jump to this if any of the case_expressions is true
        Label<UUID> stmt = new Label<>(UUID.randomUUID(), false, "");
        Label<UUID> afterStmt = new Label<UUID>(UUID.randomUUID(), false, "");

        for(OccamParser.Case_expressionContext caseExpressionContext : ctx.case_expression()){
            result.addAll(loadSelector);
            result.addAll(visit(caseExpressionContext));
            result.add(new ILBlock<>(new CompareEqual<>(UUID.randomUUID(), "")));
            result.add(new ILBlock<>(new BranchIfTrue<>(UUID.randomUUID(), stmt.getId(),"")));
        }

        result.add(new ILBlock<>(new Branch<>(UUID.randomUUID() , afterStmt.getId(), "")));

        result.add(new ILBlock<>(stmt));
        result.addAll(visit(ctx.stmt()));
        result.add(new ILBlock<>(new Branch<>(UUID.randomUUID(), endLabel.getId(), "")));
        result.add(new ILBlock<>(afterStmt));

        libraryInfo.popScope();

        return result;
    }

    @Override
    public List<ILBlock<UUID, ILOp<UUID>>> visitOption_else(OccamParser.Option_elseContext ctx) {


        Scope currentScope = libraryInfo.getCurrentScope();

        // Retrieve the ops required to load the selector so that we can compare our list of case_expressions against it
        // However we can discard the result as we are in the ELSE section
        currentScope.popConstruction();

        // Retrieve the exit label for the whole selection block, which we jump to if the statement in this option actually gets executed
        Label endLabel = (Label) currentScope.popConstruction();

        libraryInfo.pushNewScope();

        List<ILBlock<UUID, ILOp<UUID>>> result = new ArrayList<>();

        List<ILBlock<UUID, ILOp<UUID>>> stmt = visit(ctx.stmt());

        result.addAll(stmt);
        result.add(new ILBlock<>(new Branch(UUID.randomUUID(), endLabel.getId(), "")));

        libraryInfo.popScope();

        return result;
    }




    /**
     * Visits the sequence that makes up the compound statement.
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitCompound_stmt_sequence(OccamParser.Compound_stmt_sequenceContext ctx) { return visit(ctx.sequence()); }
    /**
     * Visits the conditional that makes up the compound statement.
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitCompound_stmt_conditional(OccamParser.Compound_stmt_conditionalContext ctx) { return visit(ctx.conditional()); }
    /**
     * Visits the selection that makes up the compound statement.
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitCompound_stmt_selection(OccamParser.Compound_stmt_selectionContext ctx) { return visit(ctx.selection()); }
    /**
     * Visits the loop that makes up the compound statement.
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitCompound_stmt_loop(OccamParser.Compound_stmt_loopContext ctx) { return visit(ctx.loop()); }
    /**
     * Visits the parallel that makes up the compound statement.
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitCompound_stmt_parallel(OccamParser.Compound_stmt_parallelContext ctx) { return visit(ctx.parallel()); }
    /**
     * Visits the alternation that makes up the compound statement.
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitCompound_stmt_alternation(OccamParser.Compound_stmt_alternationContext ctx) { return visit(ctx.alternation()); }
    /**
     * Visits the case input that makes up the compound statement.
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitCompound_stmt_case_input(OccamParser.Compound_stmt_case_inputContext ctx) { return visit(ctx.case_input()); }
    /**
     * Implements visiting a compound statement which is made up of either a "specification with statement(s)" or an "allocation with statements".
     *
     * The IL will either continue as expected, or become part of a separate function.
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitCompound_stmt_spec_or_alloc_stmt(OccamParser.Compound_stmt_spec_or_alloc_stmtContext ctx) {
        ILBlock<UUID, ILOp<UUID>> result_root = new ILBlock<>();
        List<ILBlock<UUID, ILOp<UUID>>> result_list = new ArrayList<>();

        boolean popFunctionScope = false;

        if (ctx.allocation() != null) {
            result_list = result_root.mergeBlockList(visit(ctx.allocation()));
        } else if (ctx.specification() != null) {
            result_list = result_root.mergeBlockList(visit(ctx.specification()));
            if (ctx.specification() instanceof OccamParser.SpecificationDefContext) {
                popFunctionScope = (boolean) libraryInfo.getCurrentScope().popConstruction();
            }
        } else {
            //TODO: Throw an error
        }

        result_list.addAll(visit(ctx.stmt()));
        result_list = new ILBlock<UUID, ILOp<UUID>>().mergeBlockList(result_list);

        if (popFunctionScope) {
            libraryInfo.popScope();
        }

        return result_list;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAbbreviation_expression(OccamParser.Abbreviation_expressionContext ctx) {
        Scope currentScope = libraryInfo.getCurrentScope();

        List<ILBlock<UUID, ILOp<UUID>>> expressionOps = visit(ctx.expression());
        String newVarName = ctx.NAME().getText();

        // TODO: Determine the type of the expression. This means tracking the type of the expression when it is visited using the construction stack
        Variable variable = currentScope.addVariable(newVarName, "INT");
        if (!(currentScope instanceof FileInformation)) {
            libraryInfo.pushNewScope();
        }

        expressionOps.get(0).add(new StoreLocal<>(UUID.randomUUID(), variable.getIndex(), ""));

        return expressionOps;
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAbbreviation_spec_expression(OccamParser.Abbreviation_spec_expressionContext ctx) {
        Scope currentScope = libraryInfo.getCurrentScope();

        List<ILBlock<UUID, ILOp<UUID>>> expressionOps = visit(ctx.expression());
        String newVarName = ctx.NAME().getText();
        visit(ctx.specifier());
        String spec = (String) currentScope.popConstruction();

        // TODO: Determine the type of the expression. This means tracking the type of the expression when it is visited using the construction stack
        Variable variable = currentScope.addVariable(newVarName, spec);
        if (!(currentScope instanceof FileInformation)) {
            libraryInfo.pushNewScope();
        }

        expressionOps.get(0).add(new StoreLocal<>(UUID.randomUUID(), variable.getIndex(), ""));

        return expressionOps;
    }

    //TODO: Channel lists to be implemented when Arrays are sorted
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAbbreviation_name_channel_list(OccamParser.Abbreviation_name_channel_listContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAbbreviation_spec_channel_list(OccamParser.Abbreviation_spec_channel_listContext ctx) { return visitChildren(ctx); }


    @Override
    public List<ILBlock<UUID, ILOp<UUID>>> visitAbbreviation_name_operand(OccamParser.Abbreviation_name_operandContext ctx) {

        Scope currentScope = libraryInfo.getCurrentScope();
        visit(ctx.named_operand());

        NamedOperand namedOperand = (NamedOperand)currentScope.popConstruction();

        TSILGeneratorHelpers.abbreviate(currentScope, namedOperand, ctx.NAME().getText(), namedOperand.getTypeName());

        if (!(currentScope instanceof FileInformation)) {
            libraryInfo.pushNewScope();
        }

        return Collections.emptyList();

    }


    @Override
    public List<ILBlock<UUID, ILOp<UUID>>> visitAbbreviation_spec_name_operand(OccamParser.Abbreviation_spec_name_operandContext ctx) {
        Scope currentScope = libraryInfo.getCurrentScope();

        visit(ctx.named_operand());
        NamedOperand operand = (NamedOperand) currentScope.popConstruction();

        visit(ctx.specifier());
        String spec = (String) currentScope.popConstruction();

        TSILGeneratorHelpers.abbreviate(currentScope, operand, ctx.NAME().getText(), spec);

        if (!(currentScope instanceof FileInformation)) {
            libraryInfo.pushNewScope();
        }

        return Collections.emptyList();
    }


    //TODO:
    // Move the todo comment down when a function has been successfully implemented


    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitActual_named_operand(OccamParser.Actual_named_operandContext ctx) {
        //TODO: This might need modifying for other parent situations (e.g. returning IL ops)

        boolean passByValue = (boolean) libraryInfo.getCurrentScope().popConstruction();

        // Visit the `variable`, which will retrieve its information from the library info
        // and push it to the construction stack
        visit(ctx.named_operand());

        // Now retrieve the scope and pop the `variable` information from the construction stack
        Scope currentScope = libraryInfo.getCurrentScope();
        NamedOperand operand = (NamedOperand) currentScope.popConstruction();

        // Create an empty IL block
        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();

        // If the operand is an `Abbreviation`, retrieve the actual operand the abbreviation represents
        operand = TSILGeneratorHelpers.resolveAbbreviations(operand);

        // If the operand is a constant, push its value to the construction stack and add the IL ops for loading it
        // to the result
        if (operand.isConstant() && !passByValue) {
            throw new IllegalArgumentException("Cannot pass a constant by reference.");
        }
        if (operand.isConstant()) {
            currentScope.pushConstruction(operand.getConstantValue());
            result.appendBlock(TSILGeneratorHelpers.loadConstant(operand.getConstantValue(), operand.getTypeName()));
        } else {
            // Otherwise, push the `NamedOperand` itself to the construction stack
            currentScope.pushConstruction(operand);

            // Depending on the kind of the operand, add the appropriate Load* instruction to the ILBlock
            if (operand instanceof Variable) {
                Variable variable = (Variable) operand;
                if (libraryInfo.isGlobal(variable)) {
                    result.add(new LoadGlobal<>(UUID.randomUUID(), variable.getName(), "", !passByValue));
                } else {
                    result.add(new LoadLocal<>(UUID.randomUUID(), variable.getIndex(), "", !passByValue));
                }
            } else if (operand instanceof Argument) {
                Argument argument = (Argument) operand;
                result.add(new LoadArgument<>(UUID.randomUUID(), argument.getIndex(), "", !passByValue));
            } else if (operand instanceof ArrayAbbreviation) {
                throw new NotImplementedException();
                //TODO: Load Reference
                //TODO: Load Element
            }
        }

        return Collections.singletonList(result);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitActual_channel(OccamParser.Actual_channelContext ctx) {
        //TODO: This might need modifying for other parent situations (e.g. returning IL ops)

        boolean passByValue = (boolean) libraryInfo.getCurrentScope().popConstruction();

        // Visit the `channel`, which will retrieve its information from the library info
        // and push it to the construction stack
        visit(ctx.channel());

        // Now retrieve the scope and pop the `variable` information from the construction stack
        Scope currentScope = libraryInfo.getCurrentScope();
        NamedOperand operand = (NamedOperand) currentScope.popConstruction();

        // Create an empty IL block
        ILBlock<UUID,ILOp<UUID>> result = new ILBlock<>();

        // If the operand is an `Abbreviation`, retrieve the actual channel the abbreviation represents
        Channel channel = (Channel) TSILGeneratorHelpers.resolveAbbreviations(operand);

        // Otherwise, push the `NamedOperand` itself to the construction stack
        currentScope.pushConstruction(channel);

        // Depending on the kind of the operand, add the appropriate Load* instruction to the ILBlock
        if (libraryInfo.isGlobal(channel)) {
            result.add(new LoadGlobal<>(UUID.randomUUID(), channel.getName(), "", !passByValue));
        } else {
            result.add(new LoadLocal<>(UUID.randomUUID(), channel.getIndex(), "", !passByValue));
        }

        return Collections.singletonList(result);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitActual_expression(OccamParser.Actual_expressionContext ctx) {
        boolean passByValue = (boolean) libraryInfo.getCurrentScope().popConstruction();
        if (!passByValue) {
            throw new IllegalArgumentException("Cannot pass an expression by reference.");
        }
        return visit(ctx.expression());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAllocation(OccamParser.AllocationContext ctx) { return visitChildren(ctx); }

    /**
     * Calculate the ILBlocks for an Alternative
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAlternation_alternatives(OccamParser.Alternation_alternativesContext ctx) {

        Scope currentScope = libraryInfo.getCurrentScope();

        if(ctx.PRI() != null) {
            //todo PRI
            throw new NotImplementedException();
        }

        List<ILBlock<UUID, ILOp<UUID>>> ilBlocks = new ArrayList<>();


        ILBlock<UUID, ILOp<UUID>> enableBlock = new ILBlock<>();
        ILBlock<UUID, ILOp<UUID>> disableBlock = new ILBlock<>();
        ILBlock<UUID, ILOp<UUID>> stmtBlock = new ILBlock<>();

        ILOp<UUID> waitAlt = new WaitAlt<>(UUID.randomUUID(), "");
        ILOp<UUID> endAlt = new EndAlt<UUID>(UUID.randomUUID(),"");
        ILOp<UUID> afterEndAlt = new Skip<UUID>(UUID.randomUUID(), "After EndAlt Placeholder");

        //See CWG 6.10.3 for the order that items need to be on the construction stack

        for(OccamParser.AlternativeContext alternativeContext : ctx.alternative()){

            visit(alternativeContext);

            List<ILBlock<UUID, ILOp<UUID>>> stmtBlockList = (List<ILBlock<UUID, ILOp<UUID>>>)currentScope.popConstruction();

            //Guard block contains two ILBLOCKS
            //ILBlock 0: Guard ilops
            //ILBlock 1: ILOP's to calculate the input
            List<ILBlock<UUID, ILOp<UUID>>> guardBlockList = (List<ILBlock<UUID, ILOp<UUID>>>)currentScope.popConstruction();


            enableBlock.appendBlock(guardBlockList.get(0));

            //Store the result of calculating the guard in a temporary variable
            //It will be used by both the enable and disable instruction

            String tempVariable = TSILGeneratorHelpers.generateTempVariable();
            currentScope.addVariable(tempVariable, "BOOL");
            int guardTempValueIndex = currentScope.getVariable(tempVariable).getIndex();

            enableBlock.add(new StoreLocal<>(UUID.randomUUID(),guardTempValueIndex,""));

            //Load the type of guarded input from the construction stack
            String inputType = (String)currentScope.popConstruction();

            if (inputType.equals("CHANNEL")) {

                Channel inputChannel = (Channel) currentScope.popConstruction();

                //Load the channel address onto the stack
                enableBlock.add(new LoadChannelRef<>(UUID.randomUUID(),inputChannel.getIndex(),""));
                enableBlock.add(new LoadLocal<UUID>(UUID.randomUUID(),guardTempValueIndex,"",false));
                enableBlock.add(new EnableChannel<>(UUID.randomUUID(),""));


                //Pre-pend the ilblock for reading the channel
                stmtBlockList.add(0, guardBlockList.get(1));

                //Load the channel address onto the stack
                disableBlock.add(new LoadChannelRef<>(UUID.randomUUID(),inputChannel.getIndex(),""));
                //Load the temporary variable which contains the guard value onto the stack
                disableBlock.add(new LoadLocal<UUID>(UUID.randomUUID(),guardTempValueIndex,"",false));

                // From CWG
                // c; e; L; disc
                //
                // Where c is channel address
                // Where e is the alternative guard result
                // Where L is the offset from "altend" instruction to the start of the statement instructins
                // Where disc is the disable instruction

                // DisableChannel contains the target process address which is to be
                // calculated at asm generation

                disableBlock.add(new DisableChannel<>(UUID.randomUUID(), stmtBlockList.get(0).get(0).getId(), afterEndAlt.getId(), "" ));


            } else if(inputType.equals("PORT")) {

                Port inputPort = (Port) currentScope.popConstruction();

                enableBlock.add(new LoadPortRef<>(UUID.randomUUID(),inputPort.getIndex(),inputPort.getName(), inputPort.getTypeName(),""));
                enableBlock.add(new LoadLocal<UUID>(UUID.randomUUID(),guardTempValueIndex,"",false));
                enableBlock.add(new EnablePort<>(UUID.randomUUID(),""));

                stmtBlockList.add(0, guardBlockList.get(1));

                disableBlock.add(new LoadLocal<UUID>(UUID.randomUUID(),guardTempValueIndex,"",false));
                disableBlock.add(new LoadPortRef<>(UUID.randomUUID(),inputPort.getIndex(), inputPort.getName(), inputPort.getTypeName(),""));
                disableBlock.add(new DisablePort<>(UUID.randomUUID(), stmtBlockList.get(0).get(0).getId(), afterEndAlt.getId(), "" ));

            } else if (inputType.equals("DELAYED_INPUT")) {

                //Take the ILBlocks from the evaluation stack
                ILBlock<UUID, ILOp<UUID>> blockCalculateTimer = (ILBlock<UUID, ILOp<UUID>>)currentScope.popConstruction();

                //We need to store the result of the time operations in a temporary variable
                //as the value is used in both enable and disable instructions

                String tempTime = TSILGeneratorHelpers.generateTempVariable();
                currentScope.addVariable(tempTime, "INT");
                int tempTimeIndex = currentScope.getVariable(tempTime).getIndex();

                enableBlock.appendBlock(blockCalculateTimer);
                //Duplicate the value on the stack so we can store the value
                //and use it for the EnableTimer op
                enableBlock.add(new Duplicate<>(UUID.randomUUID(),""));
                enableBlock.add(new StoreLocal<>(UUID.randomUUID(),tempTimeIndex,""));
                enableBlock.add(new LoadLocal<UUID>(UUID.randomUUID(),guardTempValueIndex,"",false));
                enableBlock.add(new EnableTimer<>(UUID.randomUUID(),""));

                disableBlock.add(new LoadLocal<UUID>(UUID.randomUUID(),tempTimeIndex,"",false));
                disableBlock.add(new LoadLocal<UUID>(UUID.randomUUID(),guardTempValueIndex,"",false));
                disableBlock.add(new DisableTimer<>(UUID.randomUUID(), stmtBlockList.get(0).get(0).getId(), afterEndAlt.getId() , "" ));

            } else if(inputType.equals("SKIP")) {

                enableBlock.add(new LoadLocal<UUID>(UUID.randomUUID(),guardTempValueIndex,"",false));
                enableBlock.add(new EnableSkip<>(UUID.randomUUID(), ""));

                disableBlock.add(new LoadLocal<UUID>(UUID.randomUUID(),guardTempValueIndex,"",false));
                disableBlock.add(new DisableSkip<>(UUID.randomUUID(), stmtBlockList.get(0).get(0).getId() , afterEndAlt.getId(), "" ));

            } else {
                throw new NotImplementedException();
            }

            stmtBlock.add(new Branch<>(UUID.randomUUID(),endAlt.getId(), ""));
            ilBlocks.addAll(stmtBlock.mergeBlockList(stmtBlockList));
            ilBlocks.remove(stmtBlock);
        }

        //Add the ILBLocks to the output list
        ilBlocks.add(new ILBlock<>(new InitAlt<UUID>(UUID.randomUUID(),"")));
        ilBlocks.add(enableBlock);
        ilBlocks.add(new ILBlock<>(new WaitAlt<UUID>(UUID.randomUUID(),"")));
        ilBlocks.add(disableBlock);
        ilBlocks.add(new ILBlock<>(waitAlt));
        ilBlocks.add(stmtBlock);
        ilBlocks.add(new ILBlock<>(endAlt));
        ilBlocks.add(new ILBlock<>(afterEndAlt));

        return ilBlocks;

    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAlternation_replicator_alternative(OccamParser.Alternation_replicator_alternativeContext ctx) {
        throw new NotImplementedException();
    }
    /**
     * Visit guarded alternative will trigger visiting it's childed guarded_alternative
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAlternative_guarded(OccamParser.Alternative_guardedContext ctx) {
        return visit(ctx.guarded_alternative());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAlternative_alternation(OccamParser.Alternative_alternationContext ctx) {
        throw new NotImplementedException();
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAlternative_channel(OccamParser.Alternative_channelContext ctx) {
        throw new NotImplementedException();
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAlternative_bool_channel(OccamParser.Alternative_bool_channelContext ctx) {
        throw new NotImplementedException();
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitAlternative_spec(OccamParser.Alternative_specContext ctx) {
        visit(ctx.specification());
        return visit(ctx.alternative());
    };
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitChannel_from_base(OccamParser.Channel_from_baseContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitChannel_channel_expression(OccamParser.Channel_channel_expressionContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitChannel_for_count(OccamParser.Channel_for_countContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitChannel_expression_channel_type(OccamParser.Channel_expression_channel_typeContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitConversion(OccamParser.ConversionContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitDef_DATA_Name(OccamParser.Def_DATA_NameContext ctx) {
        libraryInfo.getCurrentScope().pushConstruction(false);
        return visitChildren(ctx);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitDef_PROTOCOL_NAME_IS(OccamParser.Def_PROTOCOL_NAME_ISContext ctx) {
        libraryInfo.getCurrentScope().pushConstruction(false);
        return visitChildren(ctx);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitDef_PROTOCOL_NAME_INDENT(OccamParser.Def_PROTOCOL_NAME_INDENTContext ctx) {
        libraryInfo.getCurrentScope().pushConstruction(false);
        return visitChildren(ctx);
    }
    /**
     * {@inheritDoc}
     * Visit a function header bound to a value process. Adds a function scope to the metadata and produces the necessary
     * IL ops for the value process.
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitDef_function_value_process(OccamParser.Def_function_value_processContext ctx) {
        /* Notes:
         *
         * A value process function definition has the following grammar:
         *      data_type (',' data_type)* function_header NL INDENT value_process DEDENT ':'
         *
         */

        Scope rootScope = libraryInfo.getCurrentScope();

        // Creates the function scope and populates it with details of the formals
        visit(ctx.function_header());
        // Add the return types to the function definition
        Function functionScope = (Function) libraryInfo.getCurrentScope();
        for (OccamParser.Data_typeContext data_type : ctx.data_type()) {
            visit(data_type);
            functionScope.addReturnType((String)functionScope.popConstruction());
        }

        ILBlock<UUID,ILOp<UUID>> functionBlock = new ILBlock<>(functionScope.getScopeId(), true);
        functionBlock.add(new Label<>(UUID.randomUUID(), functionScope.getName(), true, ""));
        functionBlock.add(new MethodStart<>(UUID.randomUUID(), "Start of `" + functionScope.getName() + "`"));

        List<ILBlock<UUID,ILOp<UUID>>> valueProcess = visit(ctx.value_process());
        List<ILBlock<UUID,ILOp<UUID>>> result = functionBlock.mergeBlockList(valueProcess);

        while (libraryInfo.getCurrentScope() != rootScope) {
            libraryInfo.popScope();
        }

        functionBlock.add(new MethodEnd<>(UUID.randomUUID(), "End of `" + functionScope.getName() + "`"));

        libraryInfo.getCurrentScope().pushConstruction(true);

        return result;
    }
    /**
     * {@inheritDoc}
     * Visit a function header bound to an expression list. Adds a function scope to the metadata and produces the necessary
     * IL ops to evaluate the expressions.
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitDef_function_expression_list(OccamParser.Def_function_expression_listContext ctx) {
        /* Notes:
         *
         * An expression-list function definition has the following grammar:
         *      data_type (',' data_type)* function_header IS expression_list ':'
         */

        Scope rootScope = libraryInfo.getCurrentScope();

        // Creates the function scope and populates it with details of the formals
        visit(ctx.function_header());
        Function functionScope = (Function) libraryInfo.getCurrentScope();
        for (OccamParser.Data_typeContext data_type : ctx.data_type()) {
            visit(data_type);
            functionScope.addReturnType((String)functionScope.popConstruction());
        }

        ILBlock<UUID,ILOp<UUID>> functionBlock = new ILBlock<>(functionScope.getScopeId(), true);
        functionBlock.add(new Label<>(UUID.randomUUID(), functionScope.getName(), true, ""));
        functionBlock.add(new MethodStart<>(UUID.randomUUID(), "Start of `" + functionScope.getName() + "`"));

        List<ILBlock<UUID,ILOp<UUID>>> expressionList = visit(ctx.expression_list());
        List<ILBlock<UUID,ILOp<UUID>>> result = functionBlock.mergeBlockList(expressionList);

        while (libraryInfo.getCurrentScope() != rootScope) {
            libraryInfo.popScope();
        }

        functionBlock.add(new MethodEnd<>(UUID.randomUUID(), "End of `" + functionScope.getName() + "`"));

        libraryInfo.getCurrentScope().pushConstruction(true);

        return result;
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitDef_specifier(OccamParser.Def_specifierContext ctx) {
        libraryInfo.getCurrentScope().pushConstruction(false);
        return visitChildren(ctx);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitDef_specifier2(OccamParser.Def_specifier2Context ctx) {
        libraryInfo.getCurrentScope().pushConstruction(false);
        return visitChildren(ctx);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitDef_val(OccamParser.Def_valContext ctx) {
        libraryInfo.getCurrentScope().pushConstruction(false);
        return visitChildren(ctx);
    }
    /**
     *  Visiting delayed input will push "DELAYED_INPUT" and the ILBlocks for calculating
     *  the delay onto the construction stack
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitDelayed_input(OccamParser.Delayed_inputContext ctx) {


        List<ILBlock<UUID, ILOp<UUID>>> ilBlocks = new ArrayList<>();

        List<ILBlock<UUID, ILOp<UUID>>> expressionILBlocks = visit(ctx.expression());
        ILBlock<UUID, ILOp<UUID>> expressionBlock = new ILBlock<>();

        ilBlocks = expressionBlock.mergeBlockList(expressionILBlocks);

        //Delayed timer input will execute when the current value on the evaluation stack is
        //less than or equal to the timer value

        expressionBlock.add(new DelayedTimerInput<>(UUID.randomUUID(), ""));

        getLibraryInfo().getCurrentScope().pushConstruction(expressionBlock);
        getLibraryInfo().getCurrentScope().pushConstruction("DELAYED_INPUT");

        return ilBlocks;
    }
    /**
     * TODO
     *
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitExpression_most_data_type(OccamParser.Expression_most_data_typeContext ctx) {

        Scope scope = libraryInfo.getCurrentScope();

        visit(ctx.data_type());

        //Data type is now on the construction stack.

        String type = (String)scope.popConstruction();

        ILBlock<UUID, ILOp<UUID>> result = new ILBlock<>();

        if(ctx.MOSTPOS() != null) {

            result.add(new MostPositive<>(UUID.randomUUID(), type, ""));

        }else{

            result.add(new MostNegative<>(UUID.randomUUID(),type, ""));

        }

        return Collections.singletonList(result);

    }
    /**
     * TODO
     *
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitExpression_size_of(OccamParser.Expression_size_ofContext ctx) { return visitChildren(ctx); }
    /**
     * TODO
     *
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitExpression_conversion(OccamParser.Expression_conversionContext ctx) { return visitChildren(ctx); }

    @Override
    public List<ILBlock<UUID, ILOp<UUID>>> visitFunction_call(OccamParser.Function_callContext ctx) {
        List<ILBlock<UUID, ILOp<UUID>>> ilBlocks = new ArrayList<>();
        Function functionDefinition = libraryInfo.getFunction(ctx.NAME().getText());

        libraryInfo.pushNewScope();
        // Load the arguments onto the stack
        for (int i = ctx.expression().size() - 1; i >= 0; i--) {
            ilBlocks.addAll(visit(ctx.expression(i)));
        }
        libraryInfo.popScope();

        // Call the function
        ilBlocks.add(new ILBlock<>(new Call<UUID>(UUID.randomUUID(), ctx.NAME().getText(), "Call " + ctx.getText())));

        // Add return types to construction stack
        for (String returnType : functionDefinition.getReturnTypes()) {
            libraryInfo.getCurrentScope().pushConstruction(returnType);
        }

        libraryInfo.getCurrentScope().pushConstruction(functionDefinition);

        return ilBlocks;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitExpression_list_function_call(OccamParser.Expression_list_function_callContext ctx) {
        List<ILBlock<UUID, ILOp<UUID>>> ilBlocks = new ArrayList<>();

        ilBlocks.addAll(visit(ctx.function_call()));

        Function fn = (Function) libraryInfo.getCurrentScope().popConstruction();
        libraryInfo.getCurrentScope().pushConstruction(fn.getReturnTypes().size());

        return ilBlocks;
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitField_name(OccamParser.Field_nameContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitFunction_header(OccamParser.Function_headerContext ctx) {
        // Everything inside the function is in a new scope, so add one to the stack
        String procedureName = ctx.NAME().getText();
        libraryInfo.pushNewFunctionScope(procedureName);

        // Update the current scope with information about each of the functions's arguments
        for (OccamParser.FormalContext formal : ctx.formal()) {
            visit(formal);
        }

        return null;
    }
    /**
     * Visit a guarded input
     *
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitGuard_input(OccamParser.Guard_inputContext ctx) {

        List<ILBlock<UUID, ILOp<UUID>>> ilBlocks = new ArrayList<>();

        List<ILBlock<UUID, ILOp<UUID>>> inputBlocks =  visit(ctx.input());
        ILBlock<UUID, ILOp<UUID>> inputBlock = new ILBlock<>();

        //Add a TRUE guard
        inputBlocks.add(0, new ILBlock<>(new LoadConstant<> ( UUID.randomUUID() ,"1" , "") ));

        return inputBlock.mergeBlockList(inputBlocks);

    }




    /**
     * Generate the ILBlocks for calculating a guarded input
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitGuard_bool_input_or_skip(OccamParser.Guard_bool_input_or_skipContext ctx) {

        List<ILBlock<UUID, ILOp<UUID>>> ilBlocks = new ArrayList<>();


        //Generate the ILBlocks for calculating the boolean guard
        List<ILBlock<UUID, ILOp<UUID>>> boolBlocks = visit(ctx.bool());

        libraryInfo.getCurrentScope().popConstruction();//We don't need the boolean value on construction stack

        ILBlock<UUID, ILOp<UUID>> boolBlock = new ILBlock<>();
        boolBlocks = boolBlock.mergeBlockList(boolBlocks);
        ilBlocks.addAll(boolBlocks);

        if ( ctx.input() != null ) {


            //Generate the ILBlocks for calculating the input blocks
            List<ILBlock<UUID, ILOp<UUID>>> inputBlockList = visit(ctx.input());
            ILBlock<UUID, ILOp<UUID>> inputBlock = new ILBlock<>();

            inputBlockList = inputBlock.mergeBlockList(inputBlockList);
            ilBlocks.addAll(inputBlockList);

        }else {

            ilBlocks.add(new ILBlock<>(new Skip<UUID>(UUID.randomUUID(), "")));
            libraryInfo.getCurrentScope().pushConstruction("SKIP");
        }

        return new ILBlock<UUID, ILOp<UUID>>().mergeBlockList(ilBlocks);

    }
    /**
     * Visiting the guarded alternative will push two lists onto the construction stack
     * The ILBlock list for calculating the guard
     * THe ILBlock list for executing the stmt
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitGuarded_alternative(OccamParser.Guarded_alternativeContext ctx) {

        List<ILBlock<UUID, ILOp<UUID>>> guardBlockList = visit(ctx.guard());
        List<ILBlock<UUID, ILOp<UUID>>> stmtBlockList = visit(ctx.stmt());

        Scope currentScope = libraryInfo.getCurrentScope();

        currentScope.pushConstruction(guardBlockList);
        currentScope.pushConstruction(stmtBlockList);

        return Collections.emptyList();
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitInput_named_operand_tagged_list(OccamParser.Input_named_operand_tagged_listContext ctx) { return visitChildren(ctx); }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitInput_delayed_input(OccamParser.Input_delayed_inputContext ctx) { return visitChildren(ctx); }


    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitInput_item_multiple_variables(OccamParser.Input_item_multiple_variablesContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitLiteral_real(OccamParser.Literal_realContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitOperand_value_process(OccamParser.Operand_value_processContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitOperand_offsetof(OccamParser.Operand_offsetofContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitOperand_function_call(OccamParser.Operand_function_callContext ctx) {
        return visit(ctx.function_call());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitOperand_bytesin(OccamParser.Operand_bytesinContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitOperand_operand_expression(OccamParser.Operand_operand_expressionContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitOperand_table(OccamParser.Operand_tableContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitOption_spec_option(OccamParser.Option_spec_optionContext ctx) { return visitChildren(ctx); }
    /*
     * Writing input items to a channel
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitOutput_named_operand_outputitems(OccamParser.Output_named_operand_outputitemsContext ctx) {
        Scope currentScope = libraryInfo.getCurrentScope();
        visit(ctx.named_operand());

        //We have channel on the scope construction stack
        NamedOperand namedOperand = (NamedOperand)currentScope.popConstruction();

        namedOperand = TSILGeneratorHelpers.resolveAbbreviations(namedOperand);

        ILBlock<UUID,ILOp<UUID>> ilBlock = new ILBlock<>();
        List<ILBlock<UUID,ILOp<UUID>>> result = new ArrayList<>();

        //Create a temp variable
        String tempVar = TSILGeneratorHelpers.generateTempVariable();

        currentScope.addVariable(tempVar , namedOperand.getTypeName());

        if (namedOperand instanceof Channel) {

            Iterator<OccamParser.OutputitemContext> outputitemContextIterator = ctx.outputitem().iterator();

            while (outputitemContextIterator.hasNext()) {

                try {

                    OccamParser.OutputitemContext outputitemContext = outputitemContextIterator.next();

                    //Load ilBlocks for calculating the expression
                    List<ILBlock<UUID, ILOp<UUID>>> outputBlock = visit(outputitemContext);
                    result.addAll(ilBlock.mergeBlockList(outputBlock));
                    result.remove(ilBlock);
                    ilBlock.add(new StoreLocal<>(UUID.randomUUID(), currentScope.getVariable(tempVar).getIndex(), "Temporary variable"));
                    ilBlock.add(new LoadLocal<UUID>(UUID.randomUUID(), currentScope.getVariable(tempVar).getIndex(), "Load address", true));
                    ilBlock.add(new LoadChannelRef<>(UUID.randomUUID(), ((Channel) namedOperand).getIndex()  , ""));
                    ilBlock.add(new LoadConstant<> (  UUID.randomUUID() , String.valueOf(libraryInfo.getTypeSize(namedOperand.getTypeName())) , "" ));
                    ilBlock.add(new WriteChannel<>(UUID.randomUUID(),  ""));

                    result = ilBlock.mergeBlockList(result);
                }catch (Exception e){
                    System.out.println("Error calculating variable size");
                    System.exit(0);
                }

            }

        } else if(namedOperand instanceof Port) {

            try {


                //Load ilBlocks for calculating the expression
                //Grammar defines that for port we can only write 1 item
                List<ILBlock<UUID, ILOp<UUID>>> outputBlock = visit(ctx.outputitem(0));

                result.addAll(ilBlock.mergeBlockList(outputBlock));
                result.remove(ilBlock);

                ilBlock.add(new StoreLocal<>(UUID.randomUUID(), currentScope.getVariable(tempVar).getIndex(), "Temporary variable"));
                ilBlock.add(new LoadLocal<UUID>(UUID.randomUUID(), currentScope.getVariable(tempVar).getIndex(), "Load address", true));
                ilBlock.add(new LoadPortRef<>(UUID.randomUUID(), ((Port) namedOperand).getIndex(), ((Port) namedOperand).getName(), ((Port) namedOperand).getTypeName(), ""));
                ilBlock.add(new LoadConstant<>(UUID.randomUUID(), String.valueOf(libraryInfo.getTypeSize(namedOperand.getTypeName())), ""));

                ilBlock.add(new WritePort<>(UUID.randomUUID(), ""));

                result = ilBlock.mergeBlockList(result);
            }catch( Exception e){
                System.out.println("Error calculating variable size");
                System.exit(0);
            }

        } else if (namedOperand instanceof ArrayAbbreviation) {
            //todo add ArrayAbbreviation channel implementation
            throw new NotImplementedException();
        } else {

            throw new NotImplementedException();
        }

        return result;


    }

    /**
     * Push information about a variable to the construction stack.
     *
     * @param ctx A `variable` AST node that is just an identifier (`NAME`)
     * @return null
     */
    @Override
    public List<ILBlock<UUID, ILOp<UUID>>> visitNamed_operand_name(OccamParser.Named_operand_nameContext ctx) {
        // Retrieve the variable identifier from the AST node
        String variableName = ctx.NAME().getText();

        // Attempt to retrieve the variable's information from the library info, then
        // push it to the construction stack
        NamedOperand variable = libraryInfo.searchForNamedOperand(variableName);
        libraryInfo.getCurrentScope().pushConstruction(variable);

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitOutputitem_multiple_expression(OccamParser.Outputitem_multiple_expressionContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitParallel_placedpar(OccamParser.Parallel_placedparContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSingle_stmt(OccamParser.Single_stmtContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitPlacedpar_placedpars(OccamParser.Placedpar_placedparsContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitPlacedpar_replicator_placedpar(OccamParser.Placedpar_replicator_placedparContext ctx) { return visitChildren(ctx); }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitPort_type_expression_port_type(OccamParser.Port_type_expression_port_typeContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitProc_instance(OccamParser.Proc_instanceContext ctx) {
        List<ILBlock<UUID,ILOp<UUID>>> ilBlocks = new ArrayList<>(ctx.actual().size());
        Function functionDefinition = libraryInfo.getFunction(ctx.NAME().getText());

        libraryInfo.pushNewScope();

        for (int i = ctx.actual().size() - 1; i >= 0; i--) {
            boolean passByValue = functionDefinition.getArguments().get(i).getPassByValue();
            libraryInfo.getCurrentScope().pushConstruction(passByValue);
            ilBlocks.addAll(visit(ctx.actual(i)));
        }
        libraryInfo.popScope();

        ilBlocks.add(new ILBlock<>(new Call<UUID>(UUID.randomUUID(), ctx.NAME().getText(), "Call " + ctx.getText())));
        return ilBlocks;
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitProtcol_simple_protocol(OccamParser.Protcol_simple_protocolContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSelector(OccamParser.SelectorContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSequence_replicator(OccamParser.Sequence_replicatorContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSequential_protocol(OccamParser.Sequential_protocolContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSimple_protocol_data_type(OccamParser.Simple_protocol_data_typeContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSimple_protocol_any(OccamParser.Simple_protocol_anyContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSimple_protocol_data_type_data_type(OccamParser.Simple_protocol_data_type_data_typeContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSpecifier_channel_type(OccamParser.Specifier_channel_typeContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSpecifier_timer_type(OccamParser.Specifier_timer_typeContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSpecifier_port_type(OccamParser.Specifier_port_typeContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSpecifier_expression_specifier(OccamParser.Specifier_expression_specifierContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitStructured_type(OccamParser.Structured_typeContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitSingle_record_declaration(OccamParser.Single_record_declarationContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitTable_table_base_count(OccamParser.Table_table_base_countContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitTable_table_count(OccamParser.Table_table_countContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitTable_expressions(OccamParser.Table_expressionsContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitTable_string(OccamParser.Table_stringContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitTable_table_expression(OccamParser.Table_table_expressionContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitTag(OccamParser.TagContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitTagged_list(OccamParser.Tagged_listContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitTagged_protocol(OccamParser.Tagged_protocolContext ctx) { return visitChildren(ctx); }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitTimer_type_timer(OccamParser.Timer_type_timerContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitTimer_expression_timer_type(OccamParser.Timer_expression_timer_typeContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitValue_process_stmt(OccamParser.Value_process_stmtContext ctx) {
        List<ILBlock<UUID, ILOp<UUID>>> ilBlocks = new ArrayList<>();
        ilBlocks.addAll(visit(ctx.stmt()));
        ilBlocks.addAll(visit(ctx.expression_list()));
        return ilBlocks;
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitValue_process_specification(OccamParser.Value_process_specificationContext ctx) {
        libraryInfo.pushNewScope();

        // Adjust the scope
        visit(ctx.specification());

        // Then process the actual contents of the value process
        List<ILBlock<UUID, ILOp<UUID>>> ilBlocks = visit(ctx.value_process());

        libraryInfo.popScope();

        return ilBlocks;
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitVariant_tagged_list_stmt(OccamParser.Variant_tagged_list_stmtContext ctx) { return visitChildren(ctx); }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public List<ILBlock<UUID, ILOp<UUID>>> visitVariant_specification_variant(OccamParser.Variant_specification_variantContext ctx) { return visitChildren(ctx); }
}
