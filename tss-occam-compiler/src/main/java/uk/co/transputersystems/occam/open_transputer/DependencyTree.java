package uk.co.transputersystems.occam.open_transputer;

import uk.co.transputersystems.occam.il.*;

import java.util.*;

/**
 * Created by Ed on 16/04/2016.
 */
public class DependencyTree<TIdentifier> {
    private class Node {
        public final String functionName;
        public final ILBlock<TIdentifier, ILOp<TIdentifier>> block;
        public List<String> calledFunctions = new ArrayList<>();
        public List<Node> children = new ArrayList<>();
        public boolean called = false;
        public boolean visited = false;

        public Node(String functionName, ILBlock<TIdentifier, ILOp<TIdentifier>> block, List<String> calledFunctions) {
            this.functionName = functionName;
            this.block = block;
            this.calledFunctions = calledFunctions;
        }

        public List<ILBlock<TIdentifier, ILOp<TIdentifier>>> flatten() {
            if (visited) {
                return Collections.emptyList();
            }

            visited = true;

            List<ILBlock<TIdentifier, ILOp<TIdentifier>>> result = new ArrayList<>();

            for (Node child : children) {
                List<ILBlock<TIdentifier, ILOp<TIdentifier>>> childResult = child.flatten();
                for (ILBlock<TIdentifier, ILOp<TIdentifier>> block : childResult) {
                    if (!result.contains(block)) {
                        result.add(block);
                    }
                }
            }

            result.add(block);

            return result;
        }
    }

    private Map<String, Node> nodes = new HashMap<>();

    public DependencyTree(List<ILBlock<TIdentifier, ILOp<TIdentifier>>> ilBlocks) {
        load(ilBlocks);
    }
    private void load(List<ILBlock<TIdentifier, ILOp<TIdentifier>>> ilBlocks) {
        for (ILBlock<TIdentifier, ILOp<TIdentifier>> ilBlock : ilBlocks) {
            String functionName = getFunctionName(ilBlock);
            List<String> calledFunctions = getCalledFunctionNames(ilBlock);
            nodes.put(functionName, new Node(functionName, ilBlock, calledFunctions));
        }
    }
    public List<ILBlock<TIdentifier, ILOp<TIdentifier>>> flatten() {
        convert();

        List<ILBlock<TIdentifier, ILOp<TIdentifier>>> result = new ArrayList<>();

        for (Node child : nodes.values()) {
            if (!child.called) {
                List<ILBlock<TIdentifier, ILOp<TIdentifier>>> childResult = child.flatten();
                for (ILBlock<TIdentifier, ILOp<TIdentifier>> block : childResult) {
                    if (!result.contains(block)) {
                        result.add(block);
                    }
                }
            }
        }

        return result;
    }
    private void convert() {
        for (Node node : nodes.values()) {
            node.children = convertFunctionNames(node.calledFunctions);
        }
    }
    private List<Node> convertFunctionNames(List<String> functionNames) {
        List<Node> result = new ArrayList<>();

        for (String functionName : functionNames) {
            Node node = nodes.get(functionName);
            node.called = true;
            result.add(node);
        }

        return result;
    }

    private String getFunctionName(ILBlock<TIdentifier, ILOp<TIdentifier>> ilBlock) {
        Label lastLabel = null;
        for (ILOp<TIdentifier> op : ilBlock.getAll()) {
            if (op instanceof Label) {
                lastLabel = (Label)op;
            } else if (op instanceof MethodStart) {
                return lastLabel.label;
            }
        }

        return null;
    }
    private List<String> getCalledFunctionNames(ILBlock<TIdentifier, ILOp<TIdentifier>> ilBlock) {
        List<String> result = new ArrayList<>();

        for (ILOp<TIdentifier> op : ilBlock.getAll()) {
            if (op instanceof Call) {
                Call<TIdentifier> callOp = (Call<TIdentifier>)op;
                result.add(callOp.functionName);
            }
        }

        return result;
    }
}
