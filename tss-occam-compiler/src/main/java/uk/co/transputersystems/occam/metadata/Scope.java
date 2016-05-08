package uk.co.transputersystems.occam.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * A scope may also be known as an 'environment' or 'context'. It contains metadata about the context in which a particular
 * section of an Occam program should be executed. Each instance of `Scope` represents a distinct scope in an Occam
 * program, and contains various pieces of information:
 *
 * * the channels, variables, ports and timers declared in that scope
 * * the abbreviations declared in that scope for named operands that already exist in the scope (or its ancestors)
 * * a label stack - TODO: currently unused
 * * a construction stack, which is used to pass around temporary metadata needed while the IR tree is being built.
 *   This exists to overcome a limitation in ANTLR4's visitor-generation system: you cannot pass extra arguments into the `visit[*]` methods.
 * * a workspace ID, which links the scope to a single workspace.
 * * the parent scope - whose contents are still accessible in the current scope unless their names are explicitly shadowed
 * * the child scopes - for example, each process in a `PAR` block would have its own scope
 */
public class Scope implements VerifiableData {

    protected transient Scope parent;
    protected List<Scope> children = new ArrayList<>();

    protected List<Channel> channels = new ArrayList<>();
    protected List<Variable> variables = new ArrayList<>();
    protected List<Port> ports = new ArrayList<>();
    protected List<Timer> timers = new ArrayList<>();
    protected List<Abbreviation> abbreviations = new ArrayList<>();

    protected Stack<String> labelStack = new Stack<>();
    protected Stack<Object> constructionStack = new Stack<>();

    protected int scopeId;
    protected Workspace workspace;

    /**
     * Construct a new `Scope`. The workspace ID is set, by default, to the parent's workspace ID.
     * @param parent The parent of the new `Scope`
     */
    public Scope(Scope parent, int scopeId, boolean function) {
        this.parent = parent;
        if (parent != null) {
            setWorkspace(parent.getWorkspace());
        } else {
            this.workspace = new Workspace(null, function);
        }

        this.scopeId = scopeId;
    }

    public Scope getParent() {
        return parent;
    }

    public Workspace getWorkspace() {
        return workspace;
    }
    protected void setWorkspace(Workspace value) {
        // Update child scopes which are part of this scope's workspace, because they will be affected too.
        for (Scope childScope : children) {
            if (childScope.getWorkspace().equals(this.workspace)) {
                childScope.setWorkspace(value);
            }
        }

        // Update this scope
        this.workspace = value;
    }

    public int getScopeId() {
        return scopeId;
    }
    protected Scope getScopeById(int id) {
        if (scopeId == id) {
            return this;
        }

        for (Scope child : children) {
            Scope potentialResult = child.getScopeById(id);
            if (potentialResult != null) {
                return potentialResult;
            }
        }

        return null;
    }

    /**
     * Set the workspace ID of this `Scope` to that of a newly-generated workspace
     * @return The new workspace ID
     */
    public Workspace newWorkspace(boolean functionSpace) {
        setWorkspace(new Workspace(insideFunction() ? parent.getWorkspace() : null, functionSpace));
        return workspace;
    }
    public boolean insideFunction() {
        if (parent != null) {
            if (parent instanceof Function) {
                return true;
            } else {
                return parent.insideFunction();
            }
        }
        return false;
    }

    public List<Scope> getChildren() {
        return new ArrayList<>(children);
    }
    public void addChild(Scope scope) {
        children.add(scope);
    }

    public List<Variable> getVariables() {
        return new ArrayList<>(variables);
    }
    public List<Variable> getVariablesInWorkspaceAndBelow() {
        List<Variable> result = new ArrayList<>(variables);

        for (Scope child : children) {
            if (!(child instanceof Function)) {
                result.addAll(child.getVariablesInWorkspaceAndBelow());
            }
        }

        return result;
    }
    public Variable addVariable(String name, String typeName) {
        Variable variable = new Variable(name, getLocalVariableCount(this)+1, typeName);
        variables.add(variable);
        workspace.allocateVariable(variable);
        return variable;
    }
    public Variable getVariable(String name) {
        for (Variable variable : variables) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }
        return null;
    }
    public Variable getVariable(int index) {
        return getVariable(index, this);
    }
    private Variable getVariable(int index, Scope caller) {
        for (Variable variable : variables) {
            if (variable.getIndex() == index) {
                return variable;
            }
        }

        for (Scope child : children) {
            if (!child.equals(caller) && child.workspace.equals(workspace)) {
                Variable potentialResult = child.getVariable(index, this);
                if (potentialResult != null) {
                    return potentialResult;
                }
            }
        }

        if (!caller.equals(parent) && !(this instanceof Function)) {
            return  parent.getVariable(index, this);
        }

        return null;
    }
    public Scope findScopeOfVariable(Variable variable) {
        if (variables.contains(variable)) {
            return this;
        }

        for (Scope child : children) {
            Scope potentialResult = child.findScopeOfVariable(variable);
            if (potentialResult != null) {
                return potentialResult;
            }
        }

        return null;
    }

    public List<Channel> getChannels() {
        return new ArrayList<>(channels);
    }
    public void addChannel(String name, String typeName) {
        channels.add(new Channel(name, getLocalChannelCount(this)+1, typeName));
    }
    public Channel getChannel(String name) {
        for (Channel channel : channels) {
            if (channel.getName().equals(name)) {
                return channel;
            }
        }
        return null;
    }

    public List<Port> getPorts() {
        return new ArrayList<>(ports);
    }
    public void addPort(String name, String typeName) {
        ports.add(new Port(name, getLocalPortCount(this)+1,typeName));
    }
    public Port getPort(String name) {
        for (Port port : ports) {
            if (port.getName().equals(name)) {
                return port;
            }
        }
        return null;
    }

    public List<Timer> getTimers() {
        return new ArrayList<>(timers);
    }
    public void addTimer(String name) {
        timers.add(new Timer(name));
    }
    public Timer getTimer(String name) {
        for (Timer timer : timers) {
            if (timer.getName().equals(name)) {
                return timer;
            }
        }
        return null;
    }

    public List<Abbreviation> getAbbreviations() {
        return new ArrayList<>(abbreviations);
    }
    public void addAbbreviation(Abbreviation abbreviation) {
        abbreviation.setDeclaringScope(this);
        abbreviations.add(0,abbreviation);
    }
    public Abbreviation getAbbreviation(String name) {
        for (Abbreviation Abbreviation : abbreviations) {
            if (Abbreviation.getName().equals(name)) {
                return Abbreviation;
            }
        }
        return null;
    }

    /**
     * Given the name of an operand, attempt to find it in the scope. Search is performed in the order:
     *
     * * variables
     * * abbreviations
     * * channels
     * * ports
     * * timers
     *
     * Note that the order of the search should **not** make any difference to the result, as names cannot clash in the
     * same scope. The ordering here is simply designed to look for the most-likely-to-exist types first.
     *
     * @param name The name of the operand to attempt to retrieve
     * @return If a matching operand is found, the operand, otherwise `null`
     */
    public NamedOperand searchForNamedOperand(String name) {
        NamedOperand result = getVariable(name);
        if (result != null) {
            return  result;
        }

        result = getAbbreviation(name);
        if (result != null) {
            return  result;
        }

        result = getChannel(name);
        if (result != null) {
            return  result;
        }

        result = getPort(name);
        if (result != null) {
            return  result;
        }

        result = getTimer(name);
        if (result != null) {
            return  result;
        }

        return null;
    }

    public Stack<String> getLabelStack() {
        return labelStack;
    }
    public void pushLabel(String label) {
        labelStack.push(label);
    }
    public String popLabel() {
        return labelStack.pop();
    }
    public String peekLabel() {
        return labelStack.peek();
    }

    public Stack<Object> getConstructionStack() {
        return constructionStack;
    }
    public void pushConstruction(Object construction) {
        constructionStack.push(construction);
    }
    public Object popConstruction() {
        return constructionStack.pop();
    }
    public Object peekConstruction() {
        return constructionStack.peek();
    }

    public int getLocalVariableCount(Scope caller) {
        int result = variables.size();
        if (parent != null && !parent.equals(caller) && !(this instanceof Function)) {
            result += parent.getLocalVariableCount(this);
        }
        for (Scope childScope : children) {
            if (childScope.getWorkspace().equals(workspace)) {
                if (!childScope.equals(caller)) {
                    result += childScope.getLocalVariableCount(this);
                }
            }
        }
        return result;
    }

    public int getLocalChannelCount(Scope caller){
        int result = channels.size();
        if (parent != null && !parent.equals(caller) && !(this instanceof Function)) {
            result += parent.getLocalChannelCount(this);
        }
        for (Scope childScope : children) {
            if (childScope.getWorkspace().equals(workspace)) {
                if (!childScope.equals(caller)) {
                    result += childScope.getLocalChannelCount(this);
                }
            }
        }
        return result;

    }

    public int getLocalPortCount(Scope caller){
        int result = channels.size();
        if (parent != null && !parent.equals(caller) && !(this instanceof Function)) {
            result += parent.getLocalPortCount(this);
        }
        for (Scope childScope : children) {
            if (childScope.getWorkspace().equals(workspace)) {
                if (!childScope.equals(caller)) {
                    result += childScope.getLocalPortCount(this);
                }
            }
        }
        return result;
    }

    public Scope getScopeByWorkspaceId(int id) {
        if (workspace.getId() == id) {
            return this;
        }

        for (Scope child : children) {
            Scope potentialResult = child.getScopeByWorkspaceId(id);
            if (potentialResult != null) {
                return potentialResult;
            }
        }

        return null;
    }
    public Workspace getWorkspaceById(int id) {
        if (workspace.getId() == id) {
            return workspace;
        }

        for (Scope child : children) {
            Workspace potentialResult = child.getWorkspaceById(id);
            if (potentialResult != null) {
                return potentialResult;
            }
        }

        return null;
    }
    public void updateAllWSSizesAndOffsets() {
        updateAllWSSizesAndOffsets(null);
    }
    protected void updateAllWSSizesAndOffsets(Scope caller) {
        if (caller == null || !caller.getWorkspace().equals(workspace)) {
            workspace.updateSizeAndOffsets();
        }

        if (parent != null) {
            if (caller == null || !parent.equals(caller)) {
                parent.updateAllWSSizesAndOffsets(this);
            }
        }

        for (Scope child : children) {
            if (caller == null || !child.equals(caller)) {
                child.updateAllWSSizesAndOffsets(this);
            }
        }
    }

    public void verify(VerificationContext ctx) {
        assert (parent != this);

        assert (children != null);
        for (Scope childScope : children) {
            assert (childScope != null);
            childScope.verify(ctx);
        }

        List<String> usedNames = new ArrayList<>();

        assert (channels != null);
        for (Channel channel : channels) {
            assert (channel != null);
            channel.verify(ctx);

            assert (!usedNames.contains(channel.getName()));
            usedNames.add(channel.getName());
        }

        assert (variables != null);
        for (Variable variable : variables) {
            assert (variable != null);
            variable.verify(ctx);

            assert (!usedNames.contains(variable.getName()));
            usedNames.add(variable.getName());
        }

        assert (ports != null);
        for (Port port : ports) {
            assert (port != null);
            port.verify(ctx);

            assert (!usedNames.contains(port.getName()));
            usedNames.add(port.getName());
        }

        assert (timers != null);
        for (Timer timer : timers) {
            assert (timer != null);
            timer.verify(ctx);

            assert (!usedNames.contains(timer.getName()));
            usedNames.add(timer.getName());
        }

        assert (abbreviations != null);
        for (Abbreviation abbreviation : abbreviations) {
            assert (abbreviation != null);
            abbreviation.verify(ctx);

            assert (!usedNames.contains(abbreviation.getName()));
            usedNames.add(abbreviation.getName());
        }

        assert (labelStack != null);
        assert (labelStack.isEmpty());

        assert (constructionStack != null);
        assert (constructionStack.isEmpty());

        assert (workspace != null);
    }
}
