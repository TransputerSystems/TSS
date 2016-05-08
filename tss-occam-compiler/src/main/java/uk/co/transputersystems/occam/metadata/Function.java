package uk.co.transputersystems.occam.metadata;

import java.util.ArrayList;
import java.util.List;

public class Function extends Scope {

    private String name;
    private List<String> returnTypes;
    private List<Argument> arguments;

    public Function(Scope parent, int scopeId, String name, List<String> returnTypes, List<Argument> arguments) {
        super(parent, scopeId, true);

        this.name = name;
        this.returnTypes = returnTypes;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public List<String> getReturnTypes() {
        return new ArrayList<>(returnTypes);
    }
    public void addReturnType(String typeName) {
        returnTypes.add(typeName);
    }

    public List<Argument> getArguments() {
        return new ArrayList<>(arguments);
    }
    public void addArgument(Argument argument) {
        argument.setIndex(arguments.size()+1);
        arguments.add(argument);
        workspace.allocateArgument(argument);
    }
    public Argument getArgument(String name) {
        for (Argument argument : arguments) {
            if (argument.getName().equals(name)) {
                return argument;
            }
        }
        return null;
    }
    public Argument getArgument(Integer index) {
        index = Math.abs(index);
        for (Argument argument : arguments) {
            if (argument.getIndex() == index) {
                return argument;
            }
        }
        return null;
    }

    @Override
    public NamedOperand searchForNamedOperand(String name) {
        NamedOperand result = super.searchForNamedOperand(name);
        if (result != null) {
            return result;
        }

        result = getArgument(name);
        if (result != null) {
            return result;
        }

        return null;
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        assert (name != null);
        assert (!name.isEmpty());

        assert (returnTypes != null);
        for (String returnTypeName : returnTypes) {
            assert (returnTypeName != null);
            assert (!returnTypeName.isEmpty());
            assert (ctx.isDataTypeKnown(returnTypeName));
        }

        assert (arguments != null);
        for (Argument argument : arguments) {
            assert (argument != null);
            argument.verify(ctx);
        }
    }
}

