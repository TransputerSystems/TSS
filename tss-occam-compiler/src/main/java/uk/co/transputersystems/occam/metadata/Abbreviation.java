package uk.co.transputersystems.occam.metadata;

/**
 * An `Abbreviation` binds a new name to an existing variable. As per the Occam 2.1 reference, §4.5:
 *
 *     An abbreviation simply provides a name to identify an existing variable.
 *
 * @param <ConstantType> This abbreviation may optionally represent a constant, whose type is defined by this type parameter.
 */
public abstract class Abbreviation<ConstantType> extends NamedOperand<ConstantType> {

    protected transient Scope declaringScope;
    protected String hiddenName;

    /**
     * Construct an `Abbreviation` that hides a non-constant variable.
     * @param declaringScope The scope in which this abbreviation is being declared
     * @param name The name of the abbreviation
     * @param typeName The type of the abbreviation. This is because abbreviations can be used to re-type constants and variables.
     * @param hiddenName The name of the variable, array, channel, port or timer being abbreviated.
     */
    public Abbreviation(Scope declaringScope, String name, String typeName, String hiddenName) {
        super(name, typeName);

        this.hiddenName = hiddenName;
        this.declaringScope = declaringScope;
    }


    /**
     * Construct an `Abbreviation` that hides a constant value.
     * @param declaringScope The scope in which this abbreviation is being declared
     * @param name The name of the abbreviation
     * @param typeName The type of the abbreviation. This is because abbreviations can be used to re-type constants and variables.
     * @param hiddenName The name of the variable, array, channel, port or timer being abbreviated.
     * @param constant Whether the abbreviation represents a constant value or not. Abbreviations to constants should be resolved before being created.
     * @param constantValue The constant value the abbreviation is for.
     */
    public Abbreviation(Scope declaringScope, String name, String typeName, String hiddenName, boolean constant, ConstantType constantValue) {
        super(name, typeName, constant, constantValue);

        this.hiddenName = hiddenName;
        this.declaringScope = declaringScope;
    }

    public String getHiddenName() {
        return hiddenName;
    }

    public Scope getDeclaringScope() {
        return declaringScope;
    }
    public void setDeclaringScope(Scope scope) {
        declaringScope = scope;
    }

    /**
     * Resolves to the actual `NamedOperand` that is represented by this `Abbreviation`.
     * Initially searches the scope in which the abbreviation was declared, then recursively
     * searches the parents until a result is found (the result may be null if the root scope
     * is reached).
     *
     * Note that for array abbreviations, this will return the NamedOperand for the underlying array
     *  but the array abbreviation's index must then be taken into account.
     *
     * TODO: Callers of this should check whether "this" is an ArrayAbbreviation. If it is, special processing is required.
     *
     * Said special processing is:
     *  - For arrays which are in fact constants, the index should be used to resolve to the constant element
     *  - For arrays which are not constant, the index should be combined with the returned NamedOperand to create IL
     *    ops to runtime access the element from the array.
     *
     * @return If found, the `NamedOperand` represented by this `Abbreviation`, otherwise null
     */
    public NamedOperand resolveHiddenOperand() {
        // Retrieve the scope where this abbreviation was declared - the search will start
        // from there (rather than the currentScope)
        Scope currentScope = declaringScope;

        // Recursively search the declaringScope and its parents for a `NamedOperand` matching
        // the name that this abbreviation hides
        NamedOperand result = null;
        while (result == null && currentScope != null) {
            result = currentScope.searchForNamedOperand(hiddenName);
            currentScope = currentScope.getParent();
        }

        return result;
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        assert (declaringScope != null);
        assert(resolveHiddenOperand() != null);
    }
}
