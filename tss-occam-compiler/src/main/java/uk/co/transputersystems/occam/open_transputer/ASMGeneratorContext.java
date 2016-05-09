package uk.co.transputersystems.occam.open_transputer;

import uk.co.transputersystems.occam.il.ILBlock;
import uk.co.transputersystems.occam.il.ILOp;
import uk.co.transputersystems.occam.metadata.*;
import uk.co.transputersystems.occam.open_transputer.assembly.Data;
import uk.co.transputersystems.occam.open_transputer.assembly.Label;

import javax.annotation.Nonnull;
import java.util.*;


public class ASMGeneratorContext<TIdentifier, TILOp extends ILOp<? extends TIdentifier>> {

    public final LibraryInformation libraryInformation;
    private ILBlock<TIdentifier, TILOp> currentILBlock;
    private ASMBlock currentASMBlock;
    private Function currentFunction;
    private Workspace currentWorkspace;
    @Nonnull private Map<TIdentifier, Integer> workspaceTransitionPoints = new HashMap<>();

    private List<Integer> startingProcessesWorkspaceIds;

    private List<StackBranch<TIdentifier, StackItem<TIdentifier>>> stackBranches;
    private StackBranch<TIdentifier, StackItem<TIdentifier>> currentStackBranch;

    private ASMBlock globalsBlock = new ASMBlock();

    public Stack<Integer> initProcesses_NumProcessesToStart = new Stack<>();
    public Stack<Integer> initProcesses_TotalNumProcessesToEnd = new Stack<>();
    public Stack<Integer> initProcesses_NumProcessesToEnd = new Stack<>();
    public Stack<Integer> initProcesses_ProcCountOffsets = new Stack<>();
    public Stack<Integer> initProcesses_ExpandedSizes = new Stack<>();
    public Stack<Integer> initProcesses_ContinueOpIds = new Stack<>();

    public Map<Function, Map<Integer,ChannelDescriptor>> channelMappings = new HashMap<>();

    public ASMGeneratorContext(LibraryInformation libraryInformation) throws Exception {
        this.libraryInformation = libraryInformation;

        initGlobalVariables();
    }

    public ILBlock<TIdentifier, TILOp> getCurrentILBlock() {
        return currentILBlock;
    }
    public void setCurrentILBlock(ILBlock<TIdentifier, TILOp> value) {
        currentILBlock = value;
        setCurrentFunction((Function)libraryInformation.getScopeById(currentILBlock.getScopeId()));
    }

    public ASMBlock getCurrentASMBlock() {
        return currentASMBlock;
    }
    public void setCurrentASMBlock(ASMBlock block) {
        currentASMBlock = block;
    }

    public Function getCurrentFunction() {
        return currentFunction;
    }
    public void setCurrentFunction(Function value) {
        currentFunction = value;
        currentWorkspace = currentFunction.getWorkspace();
        if (!channelMappings.containsKey(value)) {
            channelMappings.put(value, new HashMap<>());
        }
        stackBranches = new ArrayList<>();
        currentStackBranch = new StackBranch();
        stackBranches.add(currentStackBranch);
    }

    public List<Integer> getStartingProcessesWorkspaceIds() {
        return startingProcessesWorkspaceIds;
    }
    public void setStartingProcessesWorkspaceIds(List<Integer> value) {
        // Shallow clone to prevent accidental corruption of InitProcess IL ops
        startingProcessesWorkspaceIds = new ArrayList<>(value);
    }

    public Workspace getCurrentWorkspace() {
        return currentWorkspace;
    }
    public void addWorkspaceTransition(TIdentifier targetId, Integer newWorkspaceId) {
        workspaceTransitionPoints.put(targetId, newWorkspaceId);
    }

    public void checkForWorkspaceTransition(TILOp op) {
        if (workspaceTransitionPoints.containsKey(op.getId())) {
            Integer newWorkspaceId = workspaceTransitionPoints.remove(op.getId());
            currentWorkspace = currentFunction.getWorkspaceById(newWorkspaceId);
        }
    }

    public void addChannel(Integer index, String name, String typeName) {
        channelMappings.get(currentFunction).put(index, new ChannelDescriptor(index,name,typeName,channelMappings.get(currentFunction).size(),getCurrentWorkspace()));
    }
    public ChannelDescriptor getChannel(Integer index) {
        return channelMappings.get(currentFunction).get(index);
    }

    public void updateCurrentStackBranch(int currentPosition) {
        List<StackBranch> branches = new ArrayList<>(stackBranches);
        branches.removeIf((StackBranch branch) -> branch.from > currentPosition);
        branches.sort((StackBranch b1, StackBranch b2) -> Integer.compare(b1.from, b2.from));
        currentStackBranch = branches.get(branches.size() - 1);
    }
    public void forkCurrentState(int targetPosition) {
        stackBranches.add(currentStackBranch.fork(targetPosition));
    }
    public StackItem<TIdentifier> pushToEvaluationStack(TIdentifier currentOpId, int itemIndex) {
        return currentStackBranch.pushToEvaluationStack(new StackItem<TIdentifier>(currentOpId, itemIndex));
    }
    public StackItem<TIdentifier> pushToEvaluationStack(StackItem<TIdentifier> item) {
        return currentStackBranch.pushToEvaluationStack(item);
    }
    public StackItem<TIdentifier> popFromEvaluationStack() {
        return currentStackBranch.popFromEvaluationStack();
    }
    public int getEvaluationStackSize() {
        return currentStackBranch.getStackSize();
    }

    public String generateILOpLabel(ILOp<TIdentifier> op) {
        return generateILOpLabel(op.getId());
    }
    public String generateILOpLabel(TIdentifier id) {
        return getCurrentFunction().getName() + "~IL_" + id.toString();
    }

    public String generateGlobalRepresentation(String expression) {
        if (expression.startsWith("#")) {
            return expression;
        } else {
            return generateGlobalVariableLabel(expression);
        }
    }
    public String generateGlobalVariableLabel(Variable variable) {
        return generateGlobalVariableLabel(variable.getName());
    }
    public String generateGlobalVariableLabel(String variableName) {
        return "GlobalVariable_" + variableName;
    }

    public ASMBlock getGlobalsBlock() {
        return globalsBlock;
    }
    public void initGlobalVariables() throws Exception {
        for (FileInformation fileInfo : libraryInformation.getFileInfos()) {
            for (Variable variable : fileInfo.getVariablesInWorkspaceAndBelow()) {
                String variableLabel = generateGlobalVariableLabel(variable);
                globalsBlock.addOp(new Label(variableLabel));

                String variableTypeName = variable.getTypeName();
                globalsBlock.addOp(new Data(libraryInformation.getTypeSize(variableTypeName)));
            }
        }
    }
}