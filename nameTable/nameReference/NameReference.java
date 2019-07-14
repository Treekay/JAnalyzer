package nameTable.nameReference;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.ImportedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.TypeParameterDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.nameScope.SystemScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The abstract base class for the class representing a name reference
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NameReference implements Comparable<NameReference> {
	// The name of the reference, it maybe contains '.' as a partial qualified name
	protected String name = null;	
	// The location of the reference in the source code. 
	protected SourceCodeLocation location = null;
	// The definition object which the reference bind to
	protected NameDefinition definition = null;
	protected NameScope scope = null;
	protected NameReferenceKind kind = null;
	
	public NameReference(String name, SourceCodeLocation location, NameScope scope) {
		this.name = name;
		this.location = location;
		this.scope = scope;
	}

	public NameReference(String name, SourceCodeLocation location, NameScope scope, NameReferenceKind kind) {
		this.name = name;
		this.location = location;
		this.scope = scope;
		this.kind = kind;
	}

	/**
	 * Return the name of the reference
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the location in the source code of the reference
	 */
	public SourceCodeLocation getLocation() {
		return location;
	}

	/**
	 * Return the definition object which the reference binded to
	 */
	public NameDefinition getDefinition() {
		return definition;
	}

	/**
	 * Return the scope of the reference 
	 */
	public NameScope getScope() {
		return scope;
	}

	/**
	 * Return the system scope (i.e. root scope) of the reference 
	 */
	public SystemScope getRootScope() {
		NameScope currentScope = scope;
		while (currentScope.getScopeKind() != NameScopeKind.NSK_SYSTEM) {
			currentScope = currentScope.getEnclosingScope();
		}
		return (SystemScope)currentScope;
	}

	/**
	 * Return the kind of the reference
	 */
	public NameReferenceKind getReferenceKind() {
		return kind;
	}

	/**
	 * Bind the reference to the give name definition
	 */
	public void bindTo(NameDefinition definition) {
		this.definition = definition;
	}

	/**
	 * Bind the reference to null for resolving again!
	 */
	public void resetBinding() {
		this.definition = null;
	}

	/**
	 * <p>Resolve the current reference. Provide this method is for redefining in a reference group to resolve
	 * the group according its syntax structure.
	 * 
	 * <p>Important Note: In resolveBinding() method of references, we may call resolve() method provided by 
	 * name scope. So the implementation of resolve() method in name scope can NOT call resolveBinding() method
	 * of reference in order to avoid method calling in self-circulation.
	 */
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		if (scope != null) { 
			return scope.resolve(this);
		} else return false;
	}
	
	/**
	 * Set the kind of the reference
	 */
	public void setReferenceKind(NameReferenceKind kind) {
		this.kind = kind;
	}
	
	/**
	 * Test if the reference has been resolved, i.e. test if the reference is binded to a name definition
	 */
	public boolean isResolved() {
		if (definition == null) return false;
		return true;
	}

	/**
	 * Return the type definition enclosing this name reference. Generally, any reference should occur in
	 * a type definition.
	 */
	public TypeDefinition getEnclosingTypeDefinition() {
		NameScope currentScope = scope;
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_DETAILED_TYPE ||
					currentScope.getScopeKind() == NameScopeKind.NSK_ENUM_TYPE) return (TypeDefinition)currentScope; 
			currentScope = currentScope.getEnclosingScope();
		}
		return null;
	}
	
	/**
	 * Get the result type reference for a reference which is regarded as an expression. 
	 * <OL><LI>If the reference is bind to a variable, field or a parameter definition, then return the type reference in the variable's
	 * declaration
	 * <LI>If the reference is bind to a method definition, then return the return type reference of the method
	 * <LI>Otherwise return null</OL>
	 * 
	 * <p>Note: Before call this method, the current reference should be resolved before;
	 * 
	 * @see NameDefinition getDeclareTypeReference()
	 */
	public TypeReference getResultTypeReference() {
		if (isGroupReference()) return null;
		
		NameDefinition nameDef = getDefinition();
		if (nameDef == null) return null;
		
		TypeReference resultTypeReference = null;
		
		// Determine the result type reference and its bounded type definition
		NameDefinitionKind nameDefKind = nameDef.getDefinitionKind();
		if (nameDefKind == NameDefinitionKind.NDK_TYPE) {
			if (this.isTypeReference()) resultTypeReference = (TypeReference)this; 
		} else if (nameDefKind == NameDefinitionKind.NDK_FIELD) {
			FieldDefinition fieldDef = (FieldDefinition)nameDef;
			resultTypeReference = fieldDef.getType();
			if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
		} else if (nameDefKind == NameDefinitionKind.NDK_VARIABLE || nameDefKind == NameDefinitionKind.NDK_PARAMETER) {
			VariableDefinition varDef = (VariableDefinition)nameDef;
			resultTypeReference = varDef.getType();
			if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
		} else if (nameDefKind == NameDefinitionKind.NDK_METHOD) {
			MethodDefinition methodDef = (MethodDefinition)nameDef;
			if (!methodDef.isConstructor()) {
				resultTypeReference = methodDef.getReturnType();
				if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
			}
		}
		
		return resultTypeReference;
	}

	/**
	 * Get the result type definition for a reference which is regarded as an expression. 
	 * <OL><LI>If the reference is bind to a variable, field or a parameter definition, then return the type definition of the variable
	 * <LI>If the reference is bind to a method definition, then return the return type of the method
	 * <LI>Otherwise return null</OL>
	 * 
	 * <p>Note: Before call this method, the current reference should be resolved before;
	 * 
	 * @see NameDefinition getDeclareTypeDefinition()
	 */
	public TypeDefinition getResultTypeDefinition() {
		NameDefinition nameDef = getDefinition();
		if (nameDef == null) return null;
		
		TypeDefinition resultTypeDefinition = null;
		TypeReference resultTypeReference = null;
		
		// Determine the result type reference and its bounded type definition
		NameDefinitionKind nameDefKind = nameDef.getDefinitionKind();
		if (nameDefKind == NameDefinitionKind.NDK_TYPE) {
			if (this.isTypeReference()) resultTypeReference = (TypeReference)this; 
			resultTypeDefinition = (TypeDefinition)nameDef;
		} else if (nameDefKind == NameDefinitionKind.NDK_FIELD) {
			FieldDefinition fieldDef = (FieldDefinition)nameDef;
			resultTypeReference = fieldDef.getType();
			if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
			resultTypeDefinition = (TypeDefinition)resultTypeReference.getDefinition();
		} else if (nameDefKind == NameDefinitionKind.NDK_VARIABLE || nameDefKind == NameDefinitionKind.NDK_PARAMETER) {
			VariableDefinition varDef = (VariableDefinition)nameDef;
			resultTypeReference = varDef.getType();
			if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
			resultTypeDefinition = (TypeDefinition)resultTypeReference.getDefinition();
		} else if (nameDefKind == NameDefinitionKind.NDK_METHOD) {
			MethodDefinition methodDef = (MethodDefinition)nameDef;
			if (!methodDef.isConstructor()) {
				resultTypeReference = methodDef.getReturnType();
				if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
				resultTypeDefinition = (TypeDefinition)resultTypeReference.getDefinition();
			}
		}
		// If we can not determine result type definition, we return null
		if (resultTypeDefinition == null) return null;
		
		// Get the type parameters if the type definition have
		List<TypeParameterDefinition> typeParameterList = null;
		if (resultTypeDefinition.isDetailedType()) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)resultTypeDefinition;
			typeParameterList = type.getTypeParameterList();
			
			if (typeParameterList != null) {
				type.resetFieldTypeBinding(typeParameterList);
				type.resetMethodReturnTypeAndParameterTypeBinding(typeParameterList);
			}
		} else if (resultTypeDefinition.isImportedType()) {
			ImportedTypeDefinition type = (ImportedTypeDefinition)resultTypeDefinition;
			typeParameterList = type.getTypeParameterList();

			if (typeParameterList != null) {
				type.resetFieldTypeBinding(typeParameterList);
				type.resetMethodReturnTypeAndParameterTypeBinding(typeParameterList);
			}
		}
		// If the type definition have not type parameter, we return it directly
		if (typeParameterList == null) return resultTypeDefinition;
		
		// Check whether there are type arguments when declare the type of the field, variable, method parameter or method return type 
		List<TypeReference> typeArgumentList = null;
		if (resultTypeReference != null) {
			if (resultTypeReference.isParameterizedType()) {
				ParameterizedTypeReference type = (ParameterizedTypeReference)resultTypeReference;
				typeArgumentList = type.getArgumentList();
			}
		}
		
		if (typeArgumentList == null) {
			// There no type argument in the type reference, then all type parameter are instantiated with Object
			// All type parameters instantiate to Object
			SystemScope rootScope = getRootScope();
			NameDefinition rootObject = rootScope.getRootObjectDefinition();
			TypeReference objectReference = new TypeReference(SystemScope.ROOT_OBJECT_NAME, null, rootScope);
			objectReference.bindTo(rootObject);
			for (TypeParameterDefinition typePara : typeParameterList) typePara.setCurrentValue(objectReference);
		} else {
			if (typeParameterList.size() != typeArgumentList.size()) {
				throw new AssertionError("There are different number of type parameter in definition " + resultTypeDefinition.getFullQualifiedName() + ", and reference " + resultTypeReference.getName());
			}
			for (int index = 0; index < typeParameterList.size(); index++) {
				TypeParameterDefinition typePara = typeParameterList.get(index);
				typePara.setCurrentValue(typeArgumentList.get(index));
			}
		}
		
		return resultTypeDefinition;
	}
	
	
	/**
	 * Since a reference maybe include many sub-references (e.g reference group, or complex type reference), we define a core 
	 * reference for each name reference. In general the core reference of a reference is the most important reference in all
	 * sub-references, and the core reference must be a leaf reference!
	 * 
	 * <p>Note: This method must be called after resolved the reference, and the return value of this method never be null!
	 */
	public NameReference getCoreReference() {
		return this;
	}
	
	/**
	 * Return all reference at the leaf in the name reference. Many references indeed include other references as parts of
	 * themselves. This method will return the basic references in the current reference. The basic reference do not include
	 * any other references as parts of itself.   
	 */
	public List<NameReference> getReferencesAtLeaf() {
		List<NameReference> result = new ArrayList<NameReference>();
		result.add(this);
		return result;
	}
	
	/**
	 * Return sub-reference in a name reference
	 */
	public List<NameReference> getSubReferenceList() {
		return new ArrayList<NameReference>();
	}
	
	/**
	 * Test whether the reference is a literal
	 */
	public boolean isLiteralReference() {
		return false;
	}
	
	/**
	 * Test whether the reference is a literal "null"
	 */
	public boolean isNullReference() {
		return false;
	}

	/**
	 * Test whether the reference is a method reference
	 */
	public boolean isMethodReference() {
		return false;
	}

	/**
	 * Test whether the reference is a type reference. 
	 * <p> Note that an object of NameReference be with kind equals to NRK_TYPE, if it occurs as
	 * a qualifier of an object of NRGQualifiedName
	 */
	public boolean isTypeReference() {
		return false;
	}

	/**
	 * Test whether the reference is a name group reference
	 */
	public boolean isGroupReference() {
		return false;
	}
	
	/**
	 * Test whether the reference is represent a left value reference
	 */
	public boolean isLeftValue() {
		return false;
	}

	/**
	 * Test whether the reference is represent a value reference
	 */
	public boolean isValueReference() {
		return false;
	}

	/**
	 * Set the reference is a left value reference
	 */
	public void setLeftValueReference() {
		return;
	}

	public String getUniqueId() {
		if (location != null) return name + "@" + location.getUniqueId();
		else return name;
	}

	
	@Override
	public int hashCode() {
		return getUniqueId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof NameReference)) return false;
		
		NameReference other = (NameReference) obj;
		return getUniqueId().equals(other.getUniqueId());
	}
	
	@Override
	public int compareTo(NameReference other) {
		if (this == other) return 0;

		return getUniqueId().compareTo(other.getUniqueId());
	}
	
	public String toFullString() {
		return kind.id + " Reference [Name = " + name + ", location = " + 
				location.getUniqueId() + ", scope = " + scope.getScopeName() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return kind.id + " Reference [Name = " + name + " @ " + location.getUniqueId() + "]";
	}
	
	/**
	 * Return a better string of the reference for debugging
	 */
	public String toMultilineString(int indent, boolean includeLiteral) {
		if (!includeLiteral && kind == NameReferenceKind.NRK_LITERAL) return "";
		
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		StringBuffer buffer = new StringBuffer(indentString + kind.id + " Reference [Name = " + name);
		if (indent > 0) buffer.append(" @" + location.toString() + "]\n");
		else buffer.append(" @" + location.getUniqueId() + "]\n");
			
		return buffer.toString();
	}
	
	/**
	 * Display the definition binded to the reference
	 */
	public String bindedDefinitionToString() {
		if (definition == null) return kind.id + " Reference [" + name + "] has not been resolved!";
		else return kind.id + " Reference [" + name + "] is binded to: [" + definition.getUniqueId() + "]";
	}
	
	public String toSimpleString() {
		int lineIndex = name.indexOf('\n');
		if (lineIndex < 0 || lineIndex > 64) lineIndex = 64;
		if (lineIndex > name.length()) return name;
		return name.substring(0, lineIndex);
	}

	public String toSimpleString(int length) {
		int lineIndex = name.indexOf('\n');
		if (lineIndex < 0 || lineIndex > length) lineIndex = length;
		if (lineIndex > name.length()) return name;
		return name.substring(0, lineIndex);
	}
}
