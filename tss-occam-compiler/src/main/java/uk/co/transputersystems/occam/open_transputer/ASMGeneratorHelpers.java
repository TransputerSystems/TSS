package uk.co.transputersystems.occam.open_transputer;

import uk.co.transputersystems.occam.il.ILOp;
import uk.co.transputersystems.occam.metadata.Argument;
import uk.co.transputersystems.occam.metadata.Scope;
import uk.co.transputersystems.occam.metadata.Workspace;
import uk.co.transputersystems.occam.open_transputer.assembly.*;

import java.util.*;

public class ASMGeneratorHelpers {

    public static int calculateStaticChainDistance(Workspace ownerWorkspace, Workspace currentWorkspace) {
        // See Compiler Writer's Guide page 32 : 5.10.3 The static chain

        int distance = 0;
        int ownerWorkspaceId = ownerWorkspace.getId();
        int lastWorkspaceId = currentWorkspace.getId();
        while (lastWorkspaceId != ownerWorkspaceId) {
            currentWorkspace = currentWorkspace.getParent();
            if (currentWorkspace == null) {
                throw new IllegalAccessError("Climbed too high in static workspace chain!");
            }

            if (lastWorkspaceId != currentWorkspace.getId()) {
                distance++;
            }
            lastWorkspaceId = currentWorkspace.getId();
        }

        return distance;
    }

    public static boolean climbStaticChain(List<ASMOp> result, Workspace currentWorkspace, Workspace ownerWorkspace, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Calculate distance from current WS to owner WS in terms of WS jumps (i.e. static chain distance)
        int workspacesToClimb = ASMGeneratorHelpers.calculateStaticChainDistance(ownerWorkspace, currentWorkspace);
        // Whether the variable is local to the current WS or not
        boolean isLocal = workspacesToClimb == 0;

        // If not local, climb the static chain
        if (!isLocal) {
            Workspace tempWorkspace = currentWorkspace;
            for (int i = 0; i < workspacesToClimb; i++) {
                // Parent workspace pointer is always held at max offset within the child workspace
                int parentWSPtrOffset = currentWorkspace.getOffset(Integer.MIN_VALUE);

                if (i == 0) {
                    // Load local if starting from actual current workspace
                    context.pushToEvaluationStack(-3, -1);
                    result.add(new Ldl(parentWSPtrOffset));
                } else {
                    // Load non-local if climbing through a remote workspace
                    context.popFromEvaluationStack();
                    context.pushToEvaluationStack(-3, -1);
                    result.add(new Ldnl(parentWSPtrOffset));
                }

                tempWorkspace = tempWorkspace.getParent();
            }
        }
        return isLocal;
    }

    public static int getFullWorkspaceSize(ASMGeneratorContext<Integer, ILOp<Integer>> context, Scope wsScope) {
        //TODO: This size has to take into account the required workspace size of all functions called from within the current workspace
        //  This will require a dependency tree between IL blocks. No circular references can occur due to lack of recursion in Occam
        //  so process tree bottom-up
        return getFullWorkspaceSize(context, wsScope, -1);
    }
    public static int getFullWorkspaceSize(ASMGeneratorContext<Integer, ILOp<Integer>> context, Scope scope, int parentWSId) {
        int result = 0;
        if (scope.getWorkspace().getId() != parentWSId) {
            result += scope.getWorkspace().getFullSize();
        }

        int newParentWSId = scope.getWorkspace().getId();
        for (Scope child : scope.getChildren()) {
            result += getFullWorkspaceSize(context, child, newParentWSId);
        }

        return result;
    }

    public static List<ASMOp> processConditionalBranch_True(Integer targetId, ILOp<Integer> currentOp, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        // Eqc

        List<ASMOp> popOps = processPops(1, currentOp, context, preProcess);
        context.pushToEvaluationStack(currentOp.getId(), -1);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            ILOp<Integer> target = context.getCurrentILBlock().get(targetId);
            target.setRequiresLabel(true);
            target.setRequiresPopAfterLabel(true);

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Eqc(0));
            result.add(new Cj(context.generateILOpLabel(targetId) + "-$0"));
        }

        // Post-op code for both process and preprocess

        // Branch
        context.forkCurrentState(targetId);

        // Continuation
        context.popFromEvaluationStack();

        return result;
    }
    public static List<ASMOp> processConditionalBranch_False(Integer targetId, ILOp<Integer> currentOp, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        List<ASMOp> popOps = processPops(1, currentOp, context, preProcess);
        context.pushToEvaluationStack(currentOp.getId(), -1);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            ILOp<Integer> target = context.getCurrentILBlock().get(targetId);
            target.setRequiresLabel(true);
            target.setRequiresPopAfterLabel(true);

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(new Cj(context.generateILOpLabel(targetId) + "-$0"));
        }

        // Post-op code for both process and preprocess

        // Branch
        context.forkCurrentState(targetId);

        // Continuation
        context.popFromEvaluationStack();

        return result;
    }

    public static List<ASMOp> processStoreArgument(int itemIndex, ILOp<Integer> currentOp, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        Workspace currentWorkspace = context.getCurrentWorkspace();
        Workspace ownerWorkspace = currentWorkspace.getOwner(itemIndex);
        Argument argument = context.getCurrentFunction().getArgument(itemIndex);

        List<ASMOp> result = new ArrayList<>();

        boolean isLocal = ASMGeneratorHelpers.climbStaticChain(result, currentWorkspace, ownerWorkspace, context, preProcess);

        List<ASMOp> popOps;

        /*
         * Combinations:
         *  Store Argument
         *      Local
         *          ArgByRef
         *              [Store value of argument by doing <Ldl X> then <Stnl 0>]
         *
         *          ArgByValue
         *              [Load value of argument by doing <Stl X>]
         *
         *      Non-local
         *          ArgByRef
         *              [Load value of argument by doing <Ldnl X> then <Stnl 0>]
         *
         *          ArgByValue
         *              [Load value of argument by doing <Stnl X>]
         *
         */
        if (isLocal) {
            popOps = processPops(1, currentOp, context, preProcess);
        } else {
            context.popFromEvaluationStack();
            popOps = processPops(1, currentOp, context, preProcess);
        }

        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result.addAll(popOps);

            int varWSOffset = ownerWorkspace.getOffset(itemIndex);

            if (isLocal) {
                if (!argument.getPassByValue()) {
                    result.add(new Ldl(varWSOffset));
                    result.add(new Stnl(0));
                } else {
                    result.add(new Stl(varWSOffset));
                }
            } else {
                if (!argument.getPassByValue()) {
                    result.add(new Ldnl(varWSOffset));
                    result.add(new Stnl(0));
                } else {
                    result.add(new Stnl(varWSOffset));
                }
            }
        }

        // Post-op code for both process and preprocess

        return result;
    }
    public static List<ASMOp> processStoreLocal(int itemIndex, ILOp<Integer> currentOp, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess

        Workspace currentWorkspace = context.getCurrentWorkspace();
        Workspace ownerWorkspace = currentWorkspace.getOwner(itemIndex);

        List<ASMOp> result = new ArrayList<>();

        boolean isLocal = ASMGeneratorHelpers.climbStaticChain(result, currentWorkspace, ownerWorkspace, context, preProcess);

        List<ASMOp> popOps;

        if (isLocal) {
            popOps = processPops(1, currentOp, context, preProcess);
        } else {
            context.popFromEvaluationStack();
            popOps = processPops(1, currentOp, context, preProcess);
        }

        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result.addAll(popOps);

            int varWSOffset = ownerWorkspace.getOffset(itemIndex);
            if (isLocal) {
                result.add(new Stl(varWSOffset));
            } else {
                result.add(new Stnl(varWSOffset));
            }
        }

        // Post-op code for both process and preprocess

        return result;
    }
    public static List<ASMOp> processLoadArgument(int itemIndex, ILOp<Integer> currentOp, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess, boolean loadAddress) {
        // Pre-op code for both process and preprocess

        Workspace currentWorkspace = context.getCurrentWorkspace();
        Workspace ownerWorkspace = currentWorkspace.getOwner(itemIndex);
        Argument argument = context.getCurrentFunction().getArgument(itemIndex);

        List<ASMOp> result = new ArrayList<>();

        boolean isLocal = ASMGeneratorHelpers.climbStaticChain(result, currentWorkspace, ownerWorkspace, context, preProcess);

        List<ASMOp> pushOps;

        /*
         * Combinations:
         *  Load Argument
         *      Local
         *          ArgByRef
         *              [Load value of argument by doing <Ldl X> then <Ldnl 0>]
         *
         *          ArgByValue
         *              [Load value of argument by doing <Ldl X>]
         *
         *      Non-local
         *          ArgByRef
         *              [Load value of argument by doing <Ldnl X> then <Ldnl 0>]
         *
         *          ArgByValue
         *              [Load value of argument by doing <Ldnl X>]
         *
         *  Load Argument by Address
         *      Local
         *          ArgByRef
         *              [Load address of argument by doing <Ldl X>]
         *
         *          ArgByValue
         *              [Load address of argument by doing <Ldlp X>]
         *
         *      Non-local
         *          ArgByRef
         *              [Load address of argument by doing <Ldnl X>]
         *
         *          ArgByValue
         *              [Load address of argument by doing nothing extra]
         *
         */
        if (!loadAddress) {
            if (isLocal) {
                if (!argument.getPassByValue()) {
                    pushOps = processPushes(1, currentOp, context, preProcess);
                } else {
                    pushOps = processPushes(1, currentOp, context, preProcess);
                }
            } else {
                if (!argument.getPassByValue()) {
                    pushOps = processPushes(1, currentOp, context, preProcess);
                } else {
                    pushOps = processPushes(1, currentOp, context, preProcess);
                }
            }
        } else {
            if (isLocal) {
                if (!argument.getPassByValue()) {
                    pushOps = processPushes(1, currentOp, context, preProcess);
                } else {
                    pushOps = processPushes(1, currentOp, context, preProcess);
                }
            } else {
                if (!argument.getPassByValue()) {
                    pushOps = processPushes(1, currentOp, context, preProcess);
                } else {
                    // Nothing extra
                    pushOps = new ArrayList<>();
                }
            }
        }

        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            int varWSOffset = ownerWorkspace.getOffset(itemIndex);

            if (!loadAddress) {
                if (isLocal) {
                    if (!argument.getPassByValue()) {
                        result.add(new Ldl(varWSOffset));
                        result.add(new Ldnl(0));
                    } else {
                        result.add(new Ldl(varWSOffset));
                    }
                } else {
                    if (!argument.getPassByValue()) {
                        result.add(new Ldnl(varWSOffset));
                        result.add(new Ldnl(0));
                    } else {
                        result.add(new Ldnl(varWSOffset));
                    }
                }
            } else {
                if (isLocal) {
                    if (!argument.getPassByValue()) {
                        result.add(new Ldl(varWSOffset));
                    } else {
                        result.add(new Ldlp(varWSOffset));
                    }
                } else {
                    if (!argument.getPassByValue()) {
                        result.add(new Ldnl(varWSOffset));
                    } else {
                        // Nothing extra
                    }
                }
            }

            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }
    public static List<ASMOp> processLoadLocal(int itemIndex, ILOp<Integer> currentOp, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess, boolean loadAddress) {
        // Pre-op code for both process and preprocess

        Workspace currentWorkspace = context.getCurrentWorkspace();
        Workspace ownerWorkspace = currentWorkspace.getOwner(itemIndex);

        List<ASMOp> result = new ArrayList<>();

        boolean isLocal = ASMGeneratorHelpers.climbStaticChain(result, currentWorkspace, ownerWorkspace, context, preProcess);

        List<ASMOp> pushOps;

        if (isLocal) {
            pushOps = processPushes(1, currentOp, context, preProcess);
        } else if (!loadAddress) {
            context.popFromEvaluationStack();
            pushOps = processPushes(1, currentOp, context, preProcess);
        } else {
            context.popFromEvaluationStack();
            pushOps = processPushes(1, currentOp, context, preProcess);
        }

        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            int varWSOffset = ownerWorkspace.getOffset(itemIndex);
            if (isLocal) {
                if (loadAddress) {
                    result.add(new Ldlp(varWSOffset));
                } else {
                    result.add(new Ldl(varWSOffset));
                }
            } else if (!loadAddress) {
                result.add(new Ldnl(varWSOffset));
            }

            result.addAll(pushOps);
        }

        // Post-op code for both process and preprocess

        return result;
    }

    public static List<ASMOp> processPushes(int numPushes, ILOp<Integer> currentOp, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) throws IndexOutOfBoundsException {
        List<ASMOp> result = null;

        if (numPushes > 3) {
            throw new IndexOutOfBoundsException("Cannot push more than three items in one go!");
        }

        if (!preProcess) {
            result = new ArrayList<>();
        }

        for (int i = 0; i < numPushes; i++) {
            context.pushToEvaluationStack(currentOp.getId(), i);

            if (!preProcess) {
                if (currentOp.storeResult(i)) {
                    context.getCurrentWorkspace().allocateTemporary();
                    int offset = context.getCurrentWorkspace().getLastTemporaryOffset(0);

                    result.add(new Stl(offset));
                    context.popFromEvaluationStack();
                }
            } else {
                if (context.getEvaluationStackSize() > 3) {
                    context.getCurrentWorkspace().allocateTemporary();
                }
            }
        }

        return result;
    }
    public static List<ASMOp> processPops(int numPops, ILOp<Integer> currentOp, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) throws IndexOutOfBoundsException {
        List<ASMOp> result = null;

        if (numPops > 3) {
            throw new IndexOutOfBoundsException("Cannot pop more than three items in one go!");
        }

        if (preProcess) {

            for (int i = 0; i < numPops; i++) {
                if (context.getEvaluationStackSize() > 3) {
                    context.getCurrentWorkspace().removeTemporaryFromWorkspace();
                }

                StackItem<Integer> item = context.popFromEvaluationStack();

                setupTempStore(context, item, true);
            }

        } else {

            result = new ArrayList<>();

            int size = context.getEvaluationStackSize();
            if (size < numPops) {

                switch (size) {
                    // Case 0 : Empty register stack
                    case 0:
                        // Just load the items in order
                        for (int i = 0; i < numPops; i++) {
                            int offset = context.getCurrentWorkspace().getLastTemporaryOffset(0);
                            context.getCurrentWorkspace().removeTemporaryFromWorkspace();

                            context.pushToEvaluationStack(-1, -1);
                            result.add(new Ldl(offset));
                        }
                        break;

                    // Case 1 : 1 item on register stack
                    case 1:
                        // Load each item but reverse top 2 items after each in order to keep correct ordering
                        for (int i = 0; i < numPops-1; i++) {
                            int offset = context.getCurrentWorkspace().getLastTemporaryOffset(0);
                            context.getCurrentWorkspace().removeTemporaryFromWorkspace();

                            context.pushToEvaluationStack(-1, -1);
                            result.add(new Ldl(offset));

                            result.add(new Rev());
                        }
                        break;

                    // Case 2 : 2 items on register stack
                    case 2:
                        // Trying to load a single item into CReg
                        //  Store AReg
                        //  Load value for CReg
                        //  Reverse top two items
                        //  Restore AReg

                        {
                            result.add(new Stl(0));

                            int offset = context.getCurrentWorkspace().getLastTemporaryOffset(0);
                            context.getCurrentWorkspace().removeTemporaryFromWorkspace();

                            context.pushToEvaluationStack(-1, -1);
                            result.add(new Ldl(offset));

                            result.add(new Rev());
                            result.add(new Ldl(0));
                        }
                        break;

                    default:
                        throw new IndexOutOfBoundsException("Stack size during processing mustn't go larger than 3!");
                }
            }

            for (int i = 0; i < numPops; i++) {
                context.popFromEvaluationStack();
            }
        }

        return result;
    }
    public static void setupTempStore(ASMGeneratorContext<Integer, ILOp<Integer>> context, StackItem<Integer> item, boolean removeIfAllocated) {
        if (item.getMaxDepth() > 3) {
            context.getCurrentILBlock().get(item.createdAt).setStoreResult(item.index);
            int count = (item.getMaxDepth() - 3) - context.getCurrentWorkspace().getMaxTempsSize();
            for (int j = 0; j < count; j++) {
                context.getCurrentWorkspace().allocateTemporary();
            }
            if (removeIfAllocated) {
                for (int j = 0; j < count; j++) {
                    context.getCurrentWorkspace().removeTemporaryFromWorkspace();
                }
            }
        }
    }

    public static List<ASMOp> processDisable(Integer target, Integer endAlt, ASMOp disableOp, ILOp<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess
        List<ASMOp> pushOps1 = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(disableOp instanceof Diss ? 2 : 3, op, context, preProcess);
        List<ASMOp> pushOps2 = ASMGeneratorHelpers.processPushes(1, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            context.getCurrentILBlock().get(target).setRequiresLabel(true);
            context.getCurrentILBlock().get(endAlt).setRequiresLabel(true);

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            /*Start of guarded process - Byte after altend instruction*/
            result.add(new Ldc(  context.generateILOpLabel(target) + "-" + context.generateILOpLabel(endAlt)));
            result.addAll(pushOps1);
            result.addAll(popOps);
            result.add(disableOp);
            result.addAll(pushOps2);
        }

        // Post-op code for both process and preprocess

        return result;
    }
    public static List<ASMOp> processEnable(ASMOp enableOp, ILOp<Integer> op, ASMGeneratorContext<Integer, ILOp<Integer>> context, boolean preProcess) {
        // Pre-op code for both process and preprocess
        List<ASMOp> popOps = ASMGeneratorHelpers.processPops(enableOp instanceof Enbs ? 1 : 2, op, context, preProcess);

        List<ASMOp> result;
        if (preProcess) {
            // Code for preprocess only

            result = null;
        } else {
            // Code for process only

            result = new ArrayList<>();
            result.addAll(popOps);
            result.add(enableOp);
        }

        // Post-op code for both process and preprocess

        return result;
    }
}
