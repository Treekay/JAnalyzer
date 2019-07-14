package nameTable.nameReference;

import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * <p>A class to represent literal reference. A literal is not a reference indeed. However, if we want to infer the 
 * static type of an expression, we have to save the literals to the reference list. 
 * <p>Note that we may create a literal reference for the key word "this" in the expression. However, we do not
 * create any reference for the single key word "this", such as in the method call fun(this)
 * <p>A literal reference will be bind to a type definition correspond to its type, and for "this" literal reference
 * it will be bind to the type definition enclosed the reference
 * 
 * @author Zhou Xiaocong
 * @since 2013-2-27
 * @version 1.0
 * 
 * @update 2015/11/5
 * 		Refactor the class according to the design document
 */
public class LiteralReference extends NameReference {
	private String literal = null;		// Store the literal of the reference
										// while the name of the reference stores the type name of the literal
										// For "this" literal reference, both store the key word "this"
	
	public LiteralReference(String literal, String typeName, SourceCodeLocation location, NameScope scope) {
		super(typeName, location, scope, NameReferenceKind.NRK_LITERAL);
		this.literal = literal;
	}

	/**
	 * Return the literal of the reference, which can not be changed after creating the reference
	 */
	public String getLiteral() {
		return literal;
	}
	
	/**
	 * The core reference of literal reference is itself
	 */
	public NameReference getCoreReference() {
		return this;
	}
	
	/**
	 * Except "this" literal reference, we always resolve the literal reference in the system scope!
	 * For "this" literal reference, we bind it to the enclosing type definition object!
	 */
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		if (isThisReference()) {
			TypeDefinition typeDef = getEnclosingTypeDefinition();
			bindTo(typeDef);
			return isResolved();
		}
		
		NameScope currentScope = scope;
		while (currentScope != null) {
			NameScope parent = currentScope.getEnclosingScope();
			if (parent == null) break;
			currentScope = parent;
		}
		if (currentScope != null) return currentScope.resolve(this);
		else return false;
	}

	/**
	 * Test whether the reference is a literal
	 */
	@Override
	public boolean isLiteralReference() {
		return true;
	}
	
	/**
	 * Test if the reference is "this"
	 */
	public boolean isThisReference() {
		return (name.equals(NameReferenceLabel.KEYWORD_THIS));
	}
	
	/**
	 * Test if the reference is "null"
	 */
	public boolean isNullReference() {
		return (name.equals(NameReferenceLabel.KEYWORD_NULL));
	}
	
	@Override
	public String toString() {
		return kind.id + " Reference [Name = " + name + " @ " + location.getUniqueId() + ", value = " + literal + "]";
	}
	
	@Override
	public String toSimpleString() {
		return literal;
	}
}
