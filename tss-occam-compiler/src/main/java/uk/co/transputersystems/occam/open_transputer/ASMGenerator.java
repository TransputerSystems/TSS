package uk.co.transputersystems.occam.open_transputer;

import uk.co.transputersystems.occam.ILOpFormatter;
import uk.co.transputersystems.occam.il.Add;
import uk.co.transputersystems.occam.il.Call;
import uk.co.transputersystems.occam.metadata.Function;
import uk.co.transputersystems.occam.il.*;
import uk.co.transputersystems.occam.open_transputer.assembly.*;
import uk.co.transputersystems.occam.open_transputer.assembly.Label;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ASMGenerator {

    public List<ASMBlock> generateASM(List<ILBlock<Integer, ILOp<Integer>>> ilBlocks, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean suppressErrors) throws Exception {

        DependencyTree<Integer> dependencyTree = new DependencyTree<>(ilBlocks);
        ilBlocks = dependencyTree.flatten();

        List<ASMBlock> result = new ArrayList<>();

        /**
         * Preprocessing means going through all the IL ops, performing all the relevant stack tracking stuff
         * but not actually producing any ASM ops.
         *
         * Processing means going through all the IL ops and actually creating the relevant ASM ops.
         *
         * Preprocessing is required for calculating the maximum workspace size and thus at what offsets
         * things which overflow from the ABC register stack will go into. Also, what offsets variables
         * and arguments will be at and how big the workspace adjustments have to be.
         */

        // Preprocess all blocks
        for (ILBlock<Integer, ILOp<Integer>> block : ilBlocks) {
            context.setCurrentILBlock(block);
            context.setCurrentASMBlock(null);

            for (ILOp<Integer> ilOp : block.getAll()) {
                try {
                    process(ilOp, context, true);

                    context.getCurrentFunction().updateAllWSSizesAndOffsets();
                } catch (InvalidObjectException e) {
                    System.err.println(e.toString());
                    System.err.flush();

                    if (!suppressErrors) {
                        throw e;
                    }
                } catch (Exception e) {
                    System.err.println(e.toString());
                    System.err.flush();

                    if (!suppressErrors) {
                        throw e;
                    }
                }
            }
        }

        // Process all blocks
        for (ILBlock<Integer, ILOp<Integer>> block : ilBlocks) {
            ASMBlock asmBlock = new ASMBlock();

            context.setCurrentILBlock(block);
            context.setCurrentASMBlock(asmBlock);

            for (ILOp<Integer> ilOp : block.getAll()) {
                try {
                    process(ilOp, context, false);
                } catch (InvalidObjectException e) {
                    System.err.println(e.toString());
                    System.err.flush();
                    asmBlock.addOp(new Label("ERROR! Couldn't process: " + ILOpFormatter.formatOp(ilOp, context.getCurrentILBlock())));

                    if (!suppressErrors) {
                        throw e;
                    }
                } catch (Exception e) {
                    System.err.println(e.toString());
                    System.err.flush();
                    asmBlock.addOp(new Label("ERROR! Couldn't process: " + ILOpFormatter.formatOp(ilOp, context.getCurrentILBlock())));

                    if (!suppressErrors) {
                        throw e;
                    }
                }
            }

            result.add(asmBlock);
        }

        return result;
    }

    private void process(ILOp<Integer> ilOp, ASMGeneratorContext context, boolean preProcess) throws Exception {

        // Pre-op processing that occurs for every op
        context.checkForWorkspaceTransition(ilOp);
        context.updateCurrentStackBranch(ilOp.getId());

        boolean prefixWithJump = false;
        if (ilOp.getRequiresLabel()) {
            if (ilOp.getRequiresPopAfterLabel()) {
                if (context.getEvaluationStackSize() == 0) {
                    prefixWithJump = true;
                } else {
                    context.popFromEvaluationStack();
                }
            }
        }

        // Preprocess or process the IL op

        // Holds the result of processing. Should remain null for preprocessing
        List<ASMOp> asmOps = null;
        if (ilOp instanceof Add) {
            asmOps = processAdd((Add) ilOp, context, preProcess);
        } else if (ilOp instanceof After) {
            asmOps = processAfter((After) ilOp, context, preProcess);
        } else if (ilOp instanceof BitwiseAnd) {
            asmOps = processBitwiseAnd((BitwiseAnd) ilOp, context, preProcess);
        } else if (ilOp instanceof BitwiseNot) {
            asmOps = processBitwiseNot((BitwiseNot) ilOp, context, preProcess);
        } else if (ilOp instanceof BitwiseOr) {
            asmOps = processBitwiseOr((BitwiseOr) ilOp, context, preProcess);
        } else if (ilOp instanceof BitwiseXor) {
            asmOps = processBitwiseXor((BitwiseXor) ilOp, context, preProcess);
        } else if (ilOp instanceof BooleanAnd) {
            asmOps = processBooleanAnd((BooleanAnd) ilOp, context, preProcess);
        } else if (ilOp instanceof BooleanNot) {
            asmOps = processBooleanNot((BooleanNot) ilOp, context, preProcess);
        } else if (ilOp instanceof BooleanOr) {
            asmOps = processBooleanOr((BooleanOr) ilOp, context, preProcess);
        } else if (ilOp instanceof Branch) {
            asmOps = processBranch((Branch) ilOp, context, preProcess);
        } else if (ilOp instanceof BranchEqZero) {
            asmOps = processBranchEqZero((BranchEqZero) ilOp, context, preProcess);
        } else if (ilOp instanceof BranchIfFalse) {
            asmOps = processBranchIfFalse((BranchIfFalse) ilOp, context, preProcess);
        } else if (ilOp instanceof BranchIfTrue) {
            asmOps = processBranchIfTrue((BranchIfTrue) ilOp, context, preProcess);
        } else if (ilOp instanceof BranchNotEqZero) {
            asmOps = processBranchNotEqZero((BranchNotEqZero) ilOp, context, preProcess);
        } else if (ilOp instanceof Call) {
            asmOps = processCall((Call) ilOp, context, preProcess);
        } else if (ilOp instanceof CompareEqual) {
            asmOps = processCompareEqual((CompareEqual) ilOp, context, preProcess);
        } else if (ilOp instanceof CompareGreaterThan) {
            asmOps = processCompareGreaterThan((CompareGreaterThan) ilOp, context, preProcess);
        } else if (ilOp instanceof CompareGreaterThanOrEqual) {
            asmOps = processCompareGreaterThanOrEqual((CompareGreaterThanOrEqual) ilOp, context, preProcess);
        } else if (ilOp instanceof CompareLessThan) {
            asmOps = processCompareLessThan((CompareLessThan) ilOp, context, preProcess);
        } else if (ilOp instanceof CompareLessThanOrEqual) {
            asmOps = processCompareLessThanOrEqual((CompareLessThanOrEqual) ilOp, context, preProcess);
        } else if (ilOp instanceof CompareNotEqual) {
            asmOps = processCompareNotEqual((CompareNotEqual) ilOp, context, preProcess);
        } else if (ilOp instanceof DelayedTimerInput) {
            asmOps = processDelayedTimerInput((DelayedTimerInput) ilOp, context, preProcess);
        } else if (ilOp instanceof DisableChannel) {
            asmOps = processDisableChannel((DisableChannel) ilOp, context, preProcess);
        } else if (ilOp instanceof DisablePort) {
            asmOps = processDisablePort((DisablePort) ilOp, context, preProcess);
        } else if (ilOp instanceof DisableSkip) {
            asmOps = processDisableSkip((DisableSkip) ilOp, context, preProcess);
        } else if (ilOp instanceof DisableTimer) {
            asmOps = processDisableTimer((DisableTimer) ilOp, context, preProcess);
        } else if (ilOp instanceof Divide) {
            asmOps = processDivide((Divide) ilOp, context, preProcess);
        } else if (ilOp instanceof Duplicate) {
            asmOps = processDuplicate((Duplicate) ilOp, context, preProcess);
        } else if (ilOp instanceof EnableChannel) {
            asmOps = processEnableChannel((EnableChannel) ilOp, context, preProcess);
        } else if (ilOp instanceof EnablePort) {
            asmOps = processEnablePort((EnablePort) ilOp, context, preProcess);
        } else if (ilOp instanceof EnableSkip) {
            asmOps = processEnableSkip((EnableSkip) ilOp, context, preProcess);
        } else if (ilOp instanceof EnableTimer) {
            asmOps = processEnableTimer((EnableTimer) ilOp, context, preProcess);
        } else if (ilOp instanceof EndAlt) {
            asmOps = processEndAlt((EndAlt) ilOp, context, preProcess);
        } else if (ilOp instanceof EndProcess) {
            asmOps = processEndProcess((EndProcess) ilOp, context, preProcess);
        } else if (ilOp instanceof InitAlt) {
            asmOps = processInitAlt((InitAlt) ilOp, context, preProcess);
        } else if (ilOp instanceof InitChannel) {
            asmOps = processInitChannel((InitChannel) ilOp, context, preProcess);
        } else if (ilOp instanceof InitProcesses) {
            asmOps = processInitProcesses((InitProcesses) ilOp, context, preProcess);
        } else if (ilOp instanceof uk.co.transputersystems.occam.il.Label) {
            asmOps = processLabel((uk.co.transputersystems.occam.il.Label) ilOp, context, preProcess);
        } else if (ilOp instanceof LeftShift) {
            asmOps = processLeftShift((LeftShift) ilOp, context, preProcess);
        } else if (ilOp instanceof LoadArgument) {
            asmOps = processLoadArgument((LoadArgument) ilOp, context, preProcess);
        } else if (ilOp instanceof LoadChannelRef) {
            asmOps = processLoadChannelRef((LoadChannelRef) ilOp, context, preProcess);
        } else if (ilOp instanceof LoadConstant) {
            asmOps = processLoadConstant((LoadConstant) ilOp, context, preProcess);
        } else if (ilOp instanceof LoadGlobal) {
            asmOps = processLoadGlobal((LoadGlobal) ilOp, context, preProcess);
        } else if (ilOp instanceof LoadLocal) {
            asmOps = processLoadLocal((LoadLocal) ilOp, context, preProcess);
        } else if (ilOp instanceof LoadPortRef) {
            asmOps = processLoadPortRef((LoadPortRef) ilOp, context, preProcess);
        } else if (ilOp instanceof MethodEnd) {
            asmOps = processMethodEnd((MethodEnd) ilOp, context, preProcess);
        } else if (ilOp instanceof MethodStart) {
            asmOps = processMethodStart((MethodStart) ilOp, context, preProcess);
        } else if (ilOp instanceof Modulo) {
            asmOps = processModulo((Modulo) ilOp, context, preProcess);
        }else if (ilOp instanceof MostPositive) {
            asmOps = processMostPositive((MostPositive) ilOp, context, preProcess);
        }else if (ilOp instanceof MostNegative){
            asmOps = processMostNegative((MostNegative) ilOp, context, preProcess);
        } else if (ilOp instanceof Multiply) {
            asmOps = processMultiply((Multiply) ilOp, context, preProcess);
        } else if (ilOp instanceof ReadChannel) {
            asmOps = processReadChannel((ReadChannel) ilOp, context, preProcess);
        } else if (ilOp instanceof ReadPort) {
            asmOps = processReadPort((ReadPort) ilOp, context, preProcess);
        } else if (ilOp instanceof ReadTimer) {
            asmOps = processReadTimer((ReadTimer) ilOp, context, preProcess);
        } else if (ilOp instanceof RightShift) {
            asmOps = processRightShift((RightShift) ilOp, context, preProcess);
        } else if (ilOp instanceof Skip) {
            asmOps = processSkip((Skip) ilOp, context, preProcess);
        } else if (ilOp instanceof StartProcess) {
            asmOps = processStartProcess((StartProcess) ilOp, context, preProcess);
        } else if (ilOp instanceof StoreArgument) {
            asmOps = processStoreArgument((StoreArgument) ilOp, context, preProcess);
        } else if (ilOp instanceof StoreGlobal) {
            asmOps = processStoreGlobal((StoreGlobal) ilOp, context, preProcess);
        } else if (ilOp instanceof StoreLocal) {
            asmOps = processStoreLocal((StoreLocal) ilOp, context, preProcess);
        } else if (ilOp instanceof Subtract) {
            asmOps = processSubtract((Subtract) ilOp, context, preProcess);
        } else if (ilOp instanceof UnaryMinus) {
            asmOps = processUnaryMinus((UnaryMinus) ilOp, context, preProcess);
        } else if (ilOp instanceof WaitAlt) {
            asmOps = processWaitAlt((WaitAlt) ilOp, context, preProcess);
        } else if (ilOp instanceof WriteChannel) {
            asmOps = processWriteChannel((WriteChannel) ilOp, context, preProcess);
        } else if (ilOp instanceof WritePort) {
            asmOps = processWritePort((WritePort) ilOp, context, preProcess);
        } else {
            throw new InvalidObjectException("Unrecognised IL op type in the ASMGenerator! IL op: " + ILOpFormatter.formatOp(ilOp, context.getCurrentILBlock()));
        }

        if (!preProcess && asmOps != null) {

            // Post-op processing

            if (context.initProcesses_ContinueOpIds.size() > 0) {
                if (context.initProcesses_ContinueOpIds.peek().equals(ilOp.getId())) {
                    context.initProcesses_ContinueOpIds.pop();

                    asmOps.add(0, new Ajw(-(int)context.initProcesses_ProcCountOffsets.pop()));
                }
            }

            if (ilOp.getRequiresLabel()) {
                String labelStr = context.generateILOpLabel(ilOp);
                Label label = new Label(labelStr);
                if (asmOps.size() > 0) {
                    asmOps = new ArrayList<>(asmOps);
                    asmOps.add(0, label);
                } else {
                    asmOps = new ArrayList<>();
                    asmOps.add(label);
                }

                if (prefixWithJump) {
                    asmOps.add(0, new Ldc(0));
                    asmOps.add(1, new Cj(labelStr + "-$0"));
                }

                if (ilOp.getRequiresPopAfterLabel()) {
                    asmOps.add(new Diff());
                }
            }

            // Save the ops
            context.getCurrentASMBlock().addOps(asmOps);
        } else if (preProcess && asmOps != null) {
            System.out.println("Pre-processing mistake. The op's process method returned a non-null list for a preprocess call. Il op: " + ILOpFormatter.formatOp(ilOp, context.getCurrentILBlock()));
        }
    }


    private List<ASMOp> processCall(Call<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess
        Function functionToCall = context.libraryInformation.getFunction(op.functionName);


        // Push parent WS pointer as last argument
        List<ASMOp> parentWSPtrOps = preProcess ? null : new ArrayList<>();
        int argsSize = functionToCall.getArguments().size();
        if (functionToCall.getWorkspace().getParent() != null) {
            argsSize += 1;

            List<ASMOp> pushPtrOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

            if (!preProcess) {
                parentWSPtrOps.add(new Ldlp(0));
                parentWSPtrOps.addAll(pushPtrOps);
            }
        }



        int expansionSize = ASMGeneratorHelpers.getFullWorkspaceSize(context, functionToCall);
        expansionSize -= Math.max(argsSize - 3, 0);
        context.getCurrentWorkspace().growExpansionArea(expansionSize);



       /* int extraSpace = functionToCall.getArguments().size() >= functionToCall.getReturnTypes().size() ? 0 :
                            functionToCall.getReturnTypes().size() - functionToCall.getArguments().size();
        for (int i = 0; i < extraSpace; i++) {
            context.getCurrentWorkspace().allocateTemporary();
        }*/

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(Math.min(argsSize, 3), op, context, preProcess);
        for (int i = 0; i < argsSize - 3; i++) {
            if (preProcess) {
                StackItem<Integer> item = context.popFromEvaluationStack();

                ASMGeneratorHelpers.setupTempStore(context, item, true);
            } else {
                context.getCurrentWorkspace().removeTemporaryFromWorkspace();
            }
        }

        for (int i = 0; i < functionToCall.getReturnTypes().size() - 3; i++) {
            if (preProcess) {
                context.pushToEvaluationStack(op.getId(), i+3);
            } else {
                context.getCurrentWorkspace().allocateTemporary();
            }
        }
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(Math.min(functionToCall.getReturnTypes().size(), 3), op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(parentWSPtrOps);
            result.addAll(popOps);

            for (int i = argsSize; i < 3; i++) {
                result.add(new Ldc(0));
            }

            int offset = context.getCurrentWorkspace().getLastTemporaryOffset(Math.max(functionToCall.getReturnTypes().size()-3,0) - Math.max(argsSize-3,0));
            result.add(new Ajw(offset));
            result.add(new uk.co.transputersystems.occam.open_transputer.assembly.Call(op.functionName + "-$0"    ));

            offset = context.getCurrentWorkspace().getLastTemporaryOffset(0);
            result.add(new Ajw(-offset));

            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess
        context.getCurrentWorkspace().shrinkExpansionArea(expansionSize);

        return result;
    }


    private List<ASMOp> processMethodStart(MethodStart<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = Collections.singletonList(new Ajw(-context.getCurrentWorkspace().getInitSize()));
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processMethodEnd(MethodEnd<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> popOps = preProcess ? null : new ArrayList<>();
        int retValsToPop = context.getCurrentFunction().getReturnTypes().size();
        if (retValsToPop > 3) {
            if (preProcess) {
                StackItem<Integer> item1 = context.popFromEvaluationStack();
                StackItem<Integer> item2 = context.popFromEvaluationStack();
                StackItem<Integer> item3 = context.popFromEvaluationStack();


                for (int i = 3; i < retValsToPop; i++) {
                    StackItem<Integer> item = context.popFromEvaluationStack();
                    ASMGeneratorHelpers.setupTempStore(context, item, false);
                }

                context.pushToEvaluationStack(item3);
                context.pushToEvaluationStack(item2);
                context.pushToEvaluationStack(item1);
            }

            retValsToPop = 3;
        }
        if (retValsToPop > 0) {
            popOps = ASMGeneratorHelpers.processPops(retValsToPop, op, context, preProcess);
        }

        int iPtrStoreLocation = context.getCurrentWorkspace().getNextTemporaryOffset();
        context.getCurrentWorkspace().allocateTemporary();
        int r2StoreLocation = Integer.MIN_VALUE;
        if (context.getCurrentFunction().getReturnTypes().size() >= 3) {
            r2StoreLocation = context.getCurrentWorkspace().getNextTemporaryOffset();
            context.getCurrentWorkspace().allocateTemporary();
        }
        int r1StoreLocation = Integer.MIN_VALUE;
        if (context.getCurrentFunction().getReturnTypes().size() >= 2) {
            r1StoreLocation = context.getCurrentWorkspace().getNextTemporaryOffset();
            context.getCurrentWorkspace().allocateTemporary();
        }
        int r0StoreLocation = Integer.MIN_VALUE;
        if (context.getCurrentFunction().getReturnTypes().size() >= 1) {
            r0StoreLocation = context.getCurrentWorkspace().getNextTemporaryOffset();
            context.getCurrentWorkspace().allocateTemporary();
        }


        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);

            //Store r0, r1, r2
            if (context.getCurrentFunction().getReturnTypes().size() >= 1) {

                result.add(new Stl(r0StoreLocation));

                if (context.getCurrentFunction().getReturnTypes().size() >= 2) {
                    result.add(new Stl(r1StoreLocation));
                }
                if (context.getCurrentFunction().getReturnTypes().size() >= 3) {
                    result.add(new Stl(r2StoreLocation));
                }
                //Load iPtr
                result.add(new Ldl(context.getCurrentWorkspace().getInitSize()));
                //Store iPtr
                result.add(new Stl(iPtrStoreLocation));
                //Copy rN...r3 to `size`
                int rNDstOffset = context.getCurrentWorkspace().getSize() - 1;
                if (context.getCurrentFunction().getReturnTypes().size() > 3) {
                    int rNSrcOffset = context.getCurrentWorkspace().getLastTemporaryOffset(4 + context.getCurrentFunction().getReturnTypes().size() - 3 - 1);
                    for (int i = 0; i < context.getCurrentFunction().getReturnTypes().size()-3; i++, rNSrcOffset--, rNDstOffset--) {
                        result.add(new Ldl(rNSrcOffset));
                        result.add(new Stl(rNDstOffset));
                    }
                }
                rNDstOffset -= 3;
                //
                //Copy iPtr to `size` - ((rets - 3) downto 0) - 1 - 1
                //                                                     cntToIdx
                result.add(new Ldl(iPtrStoreLocation));
                result.add(new Stl(rNDstOffset));
                //Restore r0 to r2 (size-args)
                if (context.getCurrentFunction().getReturnTypes().size() >= 3) {
                    result.add(new Ldl(r2StoreLocation));
                }
                if (context.getCurrentFunction().getReturnTypes().size() >= 2) {
                    result.add(new Ldl(r1StoreLocation));
                }

                result.add(new Ldl(r0StoreLocation));

                //Ajw
                result.add(new Ajw(rNDstOffset));
            } else {
                //Ajw
                result.add(new Ajw(context.getCurrentWorkspace().getSize() - 4));
            }
            //Ret
            result.add(new Ret());
        }

        // Post-op code for both process and preprocess

        // Remove r0, r1, r2
        if (context.getCurrentFunction().getReturnTypes().size() >= 1) {
            context.getCurrentWorkspace().removeTemporaryFromWorkspace();
        }
        if (context.getCurrentFunction().getReturnTypes().size() >= 2) {
            context.getCurrentWorkspace().removeTemporaryFromWorkspace();
        }
        if (context.getCurrentFunction().getReturnTypes().size() >= 3) {
            context.getCurrentWorkspace().removeTemporaryFromWorkspace();
        }

        // Remove iPtr
        context.getCurrentWorkspace().removeTemporaryFromWorkspace();

        retValsToPop = context.getCurrentFunction().getReturnTypes().size();
        if (retValsToPop > 3) {
            if (preProcess) {
                for (int i = 3; i < retValsToPop; i++) {
                    context.getCurrentWorkspace().removeTemporaryFromWorkspace();
                }
            }
        }

        return result;
    }



    private List<ASMOp> processInitProcesses(InitProcesses<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<Integer> wsIds = new ArrayList<>(op.workspaceIds);
        wsIds.remove(0);
        context.setStartingProcessesWorkspaceIds(wsIds);
        context.addWorkspaceTransition(op.continueILOpID, context.getCurrentWorkspace().getId());

        context.getCurrentWorkspace().allocateTemporary();
        context.getCurrentWorkspace().allocateTemporary();

        context.initProcesses_NumProcessesToStart.push(op.numProcesses - 1);
        context.initProcesses_NumProcessesToEnd.push(op.numProcesses);
        context.initProcesses_TotalNumProcessesToEnd.push(op.numProcesses);
        context.initProcesses_ExpandedSizes.push(0);

        context.getCurrentWorkspace().allocateTemporary();
        int processesCountOffset = context.getCurrentWorkspace().getLastTemporaryOffset(0);
        context.getCurrentWorkspace().allocateTemporary();
        int continuePtrOffset = context.getCurrentWorkspace().getLastTemporaryOffset(0);

        context.initProcesses_ProcCountOffsets.push(continuePtrOffset);
        context.initProcesses_ContinueOpIds.push(op.continueILOpID);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            context.getCurrentILBlock().get(op.continueILOpID).setRequiresLabel(true);

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.add(new Ldc(op.numProcesses));
            result.add(new Stl(processesCountOffset));
            result.add(new Ldc(context.generateILOpLabel(op.continueILOpID) + "-$1"));
            result.add(new Ldpi());
            result.add(new Stl(continuePtrOffset));
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processStartProcess(StartProcess<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<Integer> workspaceIds = context.getStartingProcessesWorkspaceIds();
        Integer newWorkspaceId = workspaceIds.remove(0);
        context.addWorkspaceTransition(op.firstILOpID, newWorkspaceId);

        int numProcessesToStart = context.initProcesses_NumProcessesToStart.pop();
        numProcessesToStart--;
        if (numProcessesToStart > 0) {
            context.initProcesses_NumProcessesToStart.push(numProcessesToStart);
        }

        ILOp<Integer> processStartOp = context.getCurrentILBlock().get(op.firstILOpID);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            processStartOp.setRequiresLabel(true);

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();

            result.add(new Ldlp(context.initProcesses_ProcCountOffsets.peek()));
            result.add(new Stl(-context.getCurrentWorkspace().getExpansionAreaSize() - 1));

            String processLabelStr = context.generateILOpLabel(processStartOp);
            result.add(new Ldc(processLabelStr + "-$2"));
            result.add(new Ldlp(-context.getCurrentWorkspace().getExpansionAreaSize() - context.getCurrentFunction().getWorkspaceById(newWorkspaceId).getInitSize()));
            result.add(new Startp());
        }

        // Post-op code for both process and preprocess
        int newWorkspaceFullSize = ASMGeneratorHelpers.getFullWorkspaceSize(context, context.getCurrentFunction().getScopeByWorkspaceId(newWorkspaceId));
        context.initProcesses_ExpandedSizes.push(context.initProcesses_ExpandedSizes.pop() + newWorkspaceFullSize);
        context.getCurrentWorkspace().growExpansionArea(newWorkspaceFullSize);

        return result;
    }

    private List<ASMOp> processEndProcess(EndProcess<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        int totalNumProcessesToEnd = context.initProcesses_TotalNumProcessesToEnd.peek();
        int numProcessesToEnd = context.initProcesses_NumProcessesToEnd.pop();
        numProcessesToEnd--;
        if (numProcessesToEnd > 0) {
            context.initProcesses_NumProcessesToEnd.push(numProcessesToEnd);
        } else {
            context.initProcesses_TotalNumProcessesToEnd.pop();
            context.getCurrentWorkspace().getParent().removeTemporaryFromWorkspace();
            context.getCurrentWorkspace().getParent().removeTemporaryFromWorkspace();
        }

        if (numProcessesToEnd == totalNumProcessesToEnd - 1) {
            context.getCurrentWorkspace().shrinkExpansionArea(context.initProcesses_ExpandedSizes.pop());

            context.getCurrentWorkspace().removeTemporaryFromWorkspace();
            context.getCurrentWorkspace().removeTemporaryFromWorkspace();
        }

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            if (numProcessesToEnd == totalNumProcessesToEnd - 1) {
                result.add(new Ldlp(context.initProcesses_ProcCountOffsets.peek()));
            } else {
                result.add(new Ldl(context.getCurrentWorkspace().getOffset(Integer.MIN_VALUE)));
            }
            result.add(new Endp());
        }

        // Post-op code for both process and preprocess

        return result;
    }



    private List<ASMOp> processSkip(Skip<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processLabel(uk.co.transputersystems.occam.il.Label<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            //TODO: Handle is global
            result = Collections.singletonList(new uk.co.transputersystems.occam.open_transputer.assembly.Label(op.label));
        }

        // Post-op code for both process and preprocess

        return result;
    }



    private List<ASMOp> processMostPositive(MostPositive<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);
        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            Long value = null;
            switch(op.type) {
                case "BOOL":
                    value = 1l;
                    break;
                case "BYTE":
                    value = (long)Byte.MAX_VALUE;
                    break;
                case "INT":
                    value = (long)Integer.MAX_VALUE; // TODO: Load byte size from config
                    break;
                case "INT16":
                    value = (long)Short.MAX_VALUE;
                    break;
                case "INT32":
                    value = (long)Integer.MAX_VALUE;
                    break;
                case "INT64":
                    value = Long.MAX_VALUE;
                    break;
            }
            result.add(new Ldc(value));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processMostNegative(MostNegative<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);
        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            Long value = null;
            switch(op.type) {
                case "BOOL":
                    value = 0l;
                    break;
                case "BYTE":
                    value = (long)Byte.MIN_VALUE;
                    break;
                case "INT":
                    value = (long)Integer.MIN_VALUE; // TODO: Load byte size from config
                    break;
                case "INT16":
                    value = (long)Short.MIN_VALUE;
                    break;
                case "INT32":
                    value = (long)Integer.MIN_VALUE;
                    break;
                case "INT64":
                    value = Long.MIN_VALUE;
                    break;
            }
            result.add(new Ldc(value));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processLoadConstant(LoadConstant<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);
        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            long value = Long.parseLong(op.value); // TODO: may throw exception
            result.add(new Ldc(value));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processLoadArgument(LoadArgument<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return  ASMGeneratorHelpers.processLoadArgument(-op.index, op, context, preProcess, op.loadAddress);
    }

    private List<ASMOp> processLoadLocal(LoadLocal<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return  ASMGeneratorHelpers.processLoadLocal(op.index, op, context, preProcess, op.loadAddress);
    }

    private List<ASMOp> processLoadGlobal(LoadGlobal<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess
        // Ldc
        if (!op.loadAddress) {
            context.pushToEvaluationStack(op.getId(), -1);
            context.popFromEvaluationStack();
        }
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.add(new Ldc(context.generateGlobalRepresentation(op.globalName)));
            if (!op.loadAddress) {
                //TODO: Handle globals which are record types i.e. reference types
                //TODO: Handle globals which are > word size
                result.add(new Ldnl(0));
            }
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processStoreArgument(StoreArgument<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processStoreArgument(-op.index, op, context, preProcess);
    }

    private List<ASMOp> processStoreLocal(StoreLocal<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processStoreLocal(op.index, op, context, preProcess);
    }

    private List<ASMOp> processStoreGlobal(StoreGlobal<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        context.pushToEvaluationStack(op.getId(), -1);
        context.popFromEvaluationStack();

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            //TODO: Handle globals which are record types i.e. reference types
            //TODO: Handle globals which are > word size
            result.add(new Ldc(context.generateGlobalRepresentation(op.globalName)));
            result.addAll(popOps);
            result.add(new Stnl(0));
        }

        // Post-op code for both process and preprocess

        return result;
    }



    private List<ASMOp> processReadChannel(ReadChannel<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        //TODO

        // Pre-op code for both process and preprocess

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(3, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new In());
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processReadPort(ReadPort<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        //TODO

        // Pre-op code for both process and preprocess

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(3, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new In());
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processReadTimer(ReadTimer<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        //TODO

        // Pre-op code for both process and preprocess

        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.add(new Ldtimer());
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }


    private List<ASMOp> processWriteChannel(WriteChannel<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        //TODO

        // Pre-op code for both process and preprocess

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(3, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Out());
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processWritePort(WritePort<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        //TODO

        // Pre-op code for both process and preprocess

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(3, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Out());
        }

        // Post-op code for both process and preprocess

        return result;
    }


    private List<ASMOp> processAdd(Add<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);

            //Sum assembly op ignores overflow check
            if (op.ignore_overflow) {
                result.add(new Sum());
            } else {
                result.add(new uk.co.transputersystems.occam.open_transputer.assembly.Add());
            }

            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;

    }

    private List<ASMOp> processSubtract(Subtract<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);

            //Diff instruction ignores overflow
            if (op.ignore_overflow) {
                result.add(new Diff());
            } else {
                result.add(new Sub());
            }

            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;


    }

    private List<ASMOp> processMultiply(Multiply<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);

            if (op.ignore_overflow) { //Prod instruction ignores overflow
                result.add(new Prod());
            } else { //Mul doesn't ignore overflow
                result.add(new Mul());
            }

            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;

    }

    private List<ASMOp> processDuplicate(Duplicate<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        // Dup
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(1, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(2, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Dup());
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }



    private List<ASMOp> processBranch(Branch<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) throws Exception {
        // Currently we store the Id of the destination label in a separate destination field for all branch instructions

        // Pre-op code for both process and preprocess

        // Ldc
        if (context.getEvaluationStackSize() != 0) {
            throw new Exception("Stack should be empty before branching!");
        }

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            ILOp<Integer> target = context.getCurrentILBlock().get(op.target);
            target.setRequiresLabel(true);
            target.setRequiresPopAfterLabel(true);

            result = null;
        } else {
            // Code for process only

            // Avoid being time sliced by using conditional jump
            result = new ArrayList<>();
            result.add(new Ldc(0));
            result.add(new Cj(context.generateILOpLabel(op.target) + "-$0"));
        }

        // Post-op code for both process and preprocess

        // Branch
        context.pushToEvaluationStack(op.getId(), -1);
        context.forkCurrentState(op.target);
        context.popFromEvaluationStack();

        return result;
    }

    private List<ASMOp> processBranchEqZero(BranchEqZero<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processConditionalBranch_False(op.target, op, context, preProcess);
    }

    private List<ASMOp> processBranchNotEqZero(BranchNotEqZero<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processConditionalBranch_True(op.target, op, context, preProcess);
    }

    private List<ASMOp> processBranchIfFalse(BranchIfFalse<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processConditionalBranch_False(op.target, op, context, preProcess);
    }

    private List<ASMOp> processBranchIfTrue(BranchIfTrue<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processConditionalBranch_True(op.target, op, context, preProcess);
    }



    private List<ASMOp> processCompareGreaterThan(CompareGreaterThan<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        // Gt
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = Collections.singletonList(new Gt());
        }

        // Post-op code for both process and preprocess

        return result;
    }

    /**
     * See *Transputer Instruction Set: a compiler writer's guide*, p23
     * (X >= Y) ≡ ¬(Y > X)
     */
    private List<ASMOp> processCompareGreaterThanOrEqual(CompareGreaterThanOrEqual<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        // Rev

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);

        context.pushToEvaluationStack(op.getId(), -1);
        context.pushToEvaluationStack(op.getId(), -1);
        // Gt
        context.popFromEvaluationStack();
        context.popFromEvaluationStack();
        context.pushToEvaluationStack(op.getId(), -1);
        // Eqc
        context.popFromEvaluationStack();

        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();

            result.addAll(popOps);
            result.add(new Rev());
            result.add(new Gt());
            result.add(new Eqc(0));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    /**
     * See *Transputer Instruction Set: a compiler writer's guide*, p23
     * (X - Y) = 0 ≡ X = Y
     */
    private List<ASMOp> processCompareEqual(CompareEqual<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);

        // Diff
        context.pushToEvaluationStack(op.getId(), -1);
        // Eqc
        context.popFromEvaluationStack();

        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Diff());
            result.add(new Eqc(0));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    /**
     * See *Transputer Instruction Set: a compiler writer's guide*, p23
     * ¬((X - Y) = 0) ≡ X ≠ Y
     */
    private List<ASMOp> processCompareNotEqual(CompareNotEqual<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);

        // Diff
        context.pushToEvaluationStack(op.getId(), -1);
        // Eqc
        context.popFromEvaluationStack();
        context.pushToEvaluationStack(op.getId(), -1);
        // Eqc
        context.popFromEvaluationStack();

        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Diff());
            result.add(new Eqc(0));
            result.add(new Eqc(0));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    /**
     * See *Transputer Instruction Set: a compiler writer's guide*, p23
     * (X < Y) ≡ (Y > X)
     */
    private List<ASMOp> processCompareLessThan(CompareLessThan<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);

        // Rev
        context.pushToEvaluationStack(op.getId(), -1);
        context.pushToEvaluationStack(op.getId(), -1);
        // Gt
        context.popFromEvaluationStack();
        context.popFromEvaluationStack();

        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Rev());
            result.add(new Gt());
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    /**
     * See *Transputer Instruction Set: a compiler writer's guide*, p23
     * (X <= Y) ≡ ¬(X > Y)
     */
    private List<ASMOp> processCompareLessThanOrEqual(CompareLessThanOrEqual<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);

        // Gt
        context.pushToEvaluationStack(op.getId(), -1);
        // Eqc
        context.popFromEvaluationStack();

        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Gt());
            result.add(new Eqc(0));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }


    private List<ASMOp> processBooleanAnd(BooleanAnd<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        // And
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Eqc(0));
            result.add(new Rev());
            result.add(new Eqc(0));
            result.add(new Or());
            result.add(new Eqc(0));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processBooleanOr(BooleanOr<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        // And
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Or());
            result.add(new Eqc(0));
            result.add(new Eqc(0));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processBitwiseAnd(BitwiseAnd<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        // And
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new And());
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processBitwiseOr(BitwiseOr<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        // Or
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Or());
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processBooleanNot(BooleanNot<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(1, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Eqc(0));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processBitwiseNot(BitwiseNot<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(1, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Not());
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processBitwiseXor(BitwiseXor<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Xor());
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processDivide(Divide<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Div());
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processUnaryMinus(UnaryMinus<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // See page 5.3.5 of Compilers Writers Guide

        List<ASMOp> prePushOps = null;
        List<ASMOp> popOps;
        List<ASMOp> pushOps;

        // Pre-op code for both process and preprocess
        if (op.ignore_overflow) {

            // Ldc
            prePushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);
            // Prod
            popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
            pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);


        } else {
            // Ldc
            prePushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);
            // Mul
            popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
            pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);
        }


        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.add(new Ldc(-1));
            result.addAll(prePushOps);
            result.addAll(popOps);
            if (op.ignore_overflow) {
                result.add(new Prod());
            } else {
                result.add(new Mul());
            }
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processModulo(Modulo<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {

        // Pre-op code for both process and preprocess
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Rem());
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processLeftShift(LeftShift<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        // And
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            //TODO: Handle long data types
            result.add(new Shl());
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processRightShift(RightShift<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        // And
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            //TODO: Handle long data types
            result.add(new Shr());
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processAfter(After<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {

        // Pre-op code for both process and preprocess
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.add(new Ldtimer());
            result.addAll(pushOps);
            result.addAll(popOps);
            result.add(new Sum());
            result.add(new Tin());
        }

        // Post-op code for both process and preprocess

        return result;
    }


    private List<ASMOp> processDelayedTimerInput(DelayedTimerInput<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {

        // Pre-op code for both process and preprocess
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(2, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.add(new Ldtimer());
            result.addAll(pushOps);
            result.addAll(popOps);
            result.add(new Sum());
            result.add(new Tin());
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processDisableChannel(DisableChannel<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processDisable(op.target, op.endAlt, new Disc(), op, context, preProcess);
    }

    private List<ASMOp> processDisablePort(DisablePort<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processDisable(op.target, op.endAlt, new Disc(), op, context, preProcess);
    }

    private List<ASMOp> processDisableSkip(DisableSkip<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processDisable(op.target, op.endAlt, new Diss(), op, context, preProcess);
    }

    private List<ASMOp> processDisableTimer(DisableTimer<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processDisable(op.target, op.endAlt, new Dist(), op, context, preProcess);
    }


    private List<ASMOp> processEnableChannel(EnableChannel<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processEnable(new Enbc(), op, context, preProcess);
    }

    private List<ASMOp> processEnablePort(EnablePort<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processEnable(new Enbc(), op, context, preProcess);
    }

    private List<ASMOp> processEnableSkip(EnableSkip<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processEnable(new Enbs(), op, context, preProcess);
    }

    private List<ASMOp> processEnableTimer(EnableTimer<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        return ASMGeneratorHelpers.processEnable(new Enbt(), op, context, preProcess);
    }


    private List<ASMOp> processInitAlt(InitAlt<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            //TODO: Talt?
            result = new ArrayList<>();
            result.add(new Alt());
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processWaitAlt(WaitAlt<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            //TODO: Taltwt?
            result = new ArrayList<>();
            result.add(new Altwt());
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processEndAlt(EndAlt<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.add(new Altend());
        }

        // Post-op code for both process and preprocess

        return result;
    }


    private List<ASMOp> processInitChannel(InitChannel<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            context.addChannel(op.index, op.name, op.typeName);
            context.getCurrentWorkspace().allocateTemporary();

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
        }

        // Post-op code for both process and preprocess

        return result;
    }

    private List<ASMOp> processLoadChannelRef(LoadChannelRef<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {

        // Pre-op code for both process and preprocess
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            //TODO: Handle channel data type
            //TODO: External channels? result.add(new Channel(context.getChannel(op.index).getKey()));
            result.add(new Ldlp(context.getCurrentWorkspace().getNthTemporaryOffset(context.getChannelOffset(op.index))));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }
    private List<ASMOp> processLoadPortRef(LoadPortRef<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {

        // Pre-op code for both process and preprocess
        List<ASMOp> pushOps = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            //TODO: Handle port data type
            result.add(new Ldc(op.name));
            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }
}