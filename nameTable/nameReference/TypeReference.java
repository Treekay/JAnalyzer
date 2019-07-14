package nameTable.nameReference;

import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The class represents a type reference
 * <p>Note that we store the dimensions of an array type in a type reference not in a type definition, i.e. 
 * there is no type definition for an array type. We just create type definition for the base type of an array 
 * type
 * 
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class TypeReference extends NameReference {
	// If the type reference refers to a array type, we save its dimension
	protected int dimension = 0;
	protected TypeReferenceKind typeKind = TypeReferenceKind.TRK_SIMPLE;

	public TypeReference(String name, SourceCodeLocation location,	NameScope scope) {
		super(name, location, scope, NameReferenceKind.NRK_TYPE);
	}

	public TypeReference(TypeReference other) {
		super(other.name, other.location, other.scope, NameReferenceKind.NRK_TYPE);
		dimension = other.dimension;
		definition = other.definition;
	}
	
	/**
	 * Test if the type reference is an array type reference
	 */
	public boolean isArrayType() {
		if (dimension > 0) return true;
		else return false;
	}

	/**
	 * Get the dimension of an array type
	 */
	public int getDimension() {
		return dimension;
	}
	
	/**
	 * Set the dimension of an array type
	 */
	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	/**
	 * Set the type reference kind
	 */
	public void setTypeKind(TypeReferenceKind typeKind) {
		this.typeKind = typeKind;
	}

	/**
	 * Get the type reference kind
	 */
	public TypeReferenceKind getTypeKind() {
		return typeKind;
	}
	
	/**
	 * Test if the type reference is a named qualified type reference
	 */
	public boolean isNamedQualifiedType() {
		return typeKind == TypeReferenceKind.TRK_QUALIFIED;
	}

	/**
	 * Test if the type reference is a qualified type reference
	 */
	public boolean isQualifiedType() {
		return typeKind == TypeReferenceKind.TRK_QUALIFIED;
	}
	
	/**
	 * Test if the type reference is a parameterized type reference
	 * @update: 2015/07/06
	 */
	public boolean isParameterizedType() {
		return typeKind == TypeReferenceKind.TRK_PARAMETERIZED;
	}
	
	/**
	 * Test whether the reference is a type reference
	 */
	@Override
	public boolean isTypeReference() {
		return true;
	}
	
	/**
	 * Test whether the reference refers to a primitive type!
	 */
	public boolean isReferToPrimitiveType() {
		if (NameReferenceLabel.isPrimitiveTypeName(name) && !isArrayType()) return true;
		else return false;
	}

	/**
	 * The core reference of type reference is itself
	 */
	public NameReference getCoreReference() {
		return this;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		if (NameReferenceLabel.isPrimitiveTypeName(name) || NameReferenceLabel.isAutoImportedTypeName(name)) {
			// For primitive type, we resolve it in the system scope!
			NameScope currentScope = scope;
			while (currentScope != null) {
				if (currentScope.getEnclosingScope() == null) break;
				currentScope = currentScope.getEnclosingScope();
			}
			if (currentScope != null) currentScope.resolve(this);
		} else scope.resolve(this);

		return isResolved();
	}
	
	public String toFullString() {
		String arrayString = "";
		for (int count = 0; count < dimension; count++) {
			arrayString = arrayString + "[]";
		}
		return kind.id + " Reference [Name = " + name + arrayString + ", location = " + 
				location.getUniqueId() + ", scope = " + scope.getScopeName() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String arrayString = "";
		for (int count = 0; count < dimension; count++) {
			arrayString = arrayString + "[]";
		}
		return kind.id + " Reference [Type Name = " + name + arrayString + " @ " + location.getUniqueId() + "]";
	}
	
	public String toDeclarationString() {
		String arrayString = "";
		for (int count = 0; count < dimension; count++) {
			arrayString = arrayString + "[]";
		}
		return name + arrayString;
	}

	/**
	 * Return a better string of the reference for debugging
	 */
	public String toMultilineString(int indent, boolean includeLiteral) {
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		StringBuffer buffer = new StringBuffer(indentString + kind.id + " Reference: " + "[Type Name = " + name);
		for (int count = 0; count < dimension; count++) buffer.append("[]");
		if (indent > 0) buffer.append(" @" + location.toString() + "]\n");
		else buffer.append(" @" + location.getUniqueId() + "]\n");
			
		return buffer.toString();		
	}
}
