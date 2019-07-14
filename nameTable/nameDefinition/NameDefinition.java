package nameTable.nameDefinition;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.nameScope.SystemScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The abstract base class for the class which represents a name definition
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2015/11/5
 * 		Refactor the class according to the design document
 */
public abstract class NameDefinition implements Comparable<NameDefinition> {
	protected String simpleName = null;
	protected String fullQualifiedName = null;
	protected SourceCodeLocation location = null;
	protected NameScope scope = null;
	
	public static final char LOCATION_ID_BEGINNER = '@';
	
	public NameDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		this.simpleName = simpleName;
		this.fullQualifiedName = fullQualifiedName;
		this.location = location;
		this.scope = scope;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getFullQualifiedName() {
		return fullQualifiedName;
	}

	public SourceCodeLocation getLocation() {
		return location;
	}

	public NameScope getScope() {
		return scope;
	}

	public SystemScope getRootScope() {
		NameScope currentScope = scope;
		while (currentScope.getScopeKind() != NameScopeKind.NSK_SYSTEM) {
			currentScope = currentScope.getEnclosingScope();
		}
		return (SystemScope)currentScope;
	}
	
	public abstract NameDefinitionKind getDefinitionKind();
	
	public boolean isTypeDefinition() {
		return (getDefinitionKind() == NameDefinitionKind.NDK_TYPE || 
				getDefinitionKind() == NameDefinitionKind.NDK_TYPE_PARAMETER);
	}
	
	public boolean isEnumType() {
		return false;
	}

	public boolean isDetailedType() {
		return false;
	}

	public boolean isMethodDefinition() {
		return (getDefinitionKind() == NameDefinitionKind.NDK_METHOD);
	}
	
	public boolean isFieldDefinition() {
		return (getDefinitionKind() == NameDefinitionKind.NDK_FIELD);
	}

	public boolean isVariableDefinition() {
		NameDefinitionKind kind = getDefinitionKind();
		return (kind == NameDefinitionKind.NDK_VARIABLE || kind == NameDefinitionKind.NDK_PARAMETER);
	}

	public boolean isImportedType() {
		return false;
	}
	
	public boolean isImportedStaticMember() {
		return false;
	}
	
	/**
	 * Get the declare type definition for a name definition. 
	 * <OL><LI>If it is a variable, field or a parameter definition, then return the type definition of the variable
	 * <LI>If it is a method definition, then return the return type of the method
	 * <LI>If it is a type definition, then return itself
	 * <LI>Otherwise return null</OL>
	 * 
	 * @see NameDefinition getDeclareTypeDefinition()
	 */
	public TypeDefinition getDeclareTypeDefinition() {
		TypeDefinition resultTypeDefinition = null;
		TypeReference resultTypeReference = null;
		
		// Determine the result type reference and its bounded type definition
		NameDefinitionKind nameDefKind = getDefinitionKind();
		if (nameDefKind == NameDefinitionKind.NDK_TYPE) {
			resultTypeDefinition = (TypeDefinition)this;
		} else if (nameDefKind == NameDefinitionKind.NDK_FIELD) {
			FieldDefinition fieldDef = (FieldDefinition)this;
			resultTypeReference = fieldDef.getType();
			if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
			resultTypeDefinition = (TypeDefinition)resultTypeReference.getDefinition();
		} else if (nameDefKind == NameDefinitionKind.NDK_VARIABLE || nameDefKind == NameDefinitionKind.NDK_PARAMETER) {
			VariableDefinition varDef = (VariableDefinition)this;
			resultTypeReference = varDef.getType();
			if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
			resultTypeDefinition = (TypeDefinition)resultTypeReference.getDefinition();
		} else if (nameDefKind == NameDefinitionKind.NDK_METHOD) {
			MethodDefinition methodDef = (MethodDefinition)this;
			if (!methodDef.isConstructor()) {
				resultTypeReference = methodDef.getReturnType();
				if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
				resultTypeDefinition = (TypeDefinition)resultTypeReference.getDefinition();
			}
		}
		return resultTypeDefinition;
	}

	/**
	 * Get the declare type reference for a name definition. 
	 * <OL><LI>If it is variable, field or a parameter definition, then return the type reference in the variable's
	 * declaration
	 * <LI>If it is a method definition, then return the return type reference of the method
	 * <LI>Otherwise return null</OL>
	 * 
	 * @see NameReference.getResultTypeRefernece()
	 */
	public TypeReference getDeclareTypeReference() {
		TypeReference resultTypeReference = null;
		
		// Determine the result type reference and its bounded type definition
		NameDefinitionKind nameDefKind = getDefinitionKind();
		if (nameDefKind == NameDefinitionKind.NDK_FIELD) {
			FieldDefinition fieldDef = (FieldDefinition)this;
			resultTypeReference = fieldDef.getType();
			if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
		} else if (nameDefKind == NameDefinitionKind.NDK_VARIABLE || nameDefKind == NameDefinitionKind.NDK_PARAMETER) {
			VariableDefinition varDef = (VariableDefinition)this;
			resultTypeReference = varDef.getType();
			if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
		} else if (nameDefKind == NameDefinitionKind.NDK_METHOD) {
			MethodDefinition methodDef = (MethodDefinition)this;
			if (!methodDef.isConstructor()) {
				resultTypeReference = methodDef.getReturnType();
				if (!resultTypeReference.isResolved()) resultTypeReference.resolveBinding();
			}
		}
		return resultTypeReference;
	}
	
	/**
	 * Match a reference name to the current name definition name. If the reference name contains qualifier ("."), 
	 *    then match the reference name to the fuallqualifiedName from the right to left, else match the reference 
	 *    name to the simpleName. 
	 * @param reference
	 * @return If the match is successful return true and bind the reference to the definition, else return false
	 */
	public final boolean match(NameReference reference) {
		String refStr = reference.getName();
		
		if (match(refStr)) {
			reference.bindTo(this);
			return true;
		} else return false;
	}

	/**
	 * Match a reference name to the current name definition name. If the reference name contains qualifier ("."), 
	 *    then match the reference name to the fuallqualifiedName from the right to left, else match the reference 
	 *    name to the simpleName. 
	 * @param reference
	 * @return If the match is successful return true , else return false
	 */
	public final boolean match(String namePostFix) {
		if (namePostFix.contains(NameReferenceLabel.NAME_QUALIFIER)) {
			if (fullQualifiedName == null) return false;
			
			// Update: 2014-1-1 Zhou Xiaocong
			// In JDK, there is a class org.omg.CORBA.ORB and a class com.sun.corba.se.org.omg.CORBA.ORG. If we just use endsWith() to 
			// match, then the latter will match the former, but it is an error! 
			// So we use the precise match method, i.e. equals()!!!
			return fullQualifiedName.equals(namePostFix);
		} else {
			if (simpleName == null) return false;
			return namePostFix.equals(simpleName);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((simpleName == null) ? 0 : simpleName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return false;
	}

	@Override
	public int compareTo(NameDefinition other) {
		if (this == other) return 0;

		if (location == null) {
			if (other.location != null) return -1;
			else return fullQualifiedName.compareTo(other.fullQualifiedName);
		} else {
			if (other.location == null) return 1;
			else {
				int result = location.compareTo(other.location);
				if (result != 0) return result;
			}
		}
		return simpleName.compareTo(other.simpleName);
	}
	
	public String getUniqueId() {
		if (location != null) return simpleName + LOCATION_ID_BEGINNER + location.getUniqueId();
		else return fullQualifiedName;
	}
	
	public static String getDefinitionNameFromId(String id) {
		int indexOfAt = id.indexOf(LOCATION_ID_BEGINNER);
		if (indexOfAt < 0) return id;
		else return id.substring(0, indexOfAt);
	}
	
	public static String getDefinitionLocationStringFromId(String id) {
		int indexOfAt = id.indexOf(LOCATION_ID_BEGINNER);
		if (indexOfAt < 0) return null;
		else return id.substring(indexOfAt+1);
	}

	public String toFullString() {
		StringBuffer buffer = new StringBuffer(getDefinitionKind().id + " Definition [fullQualifiedName = " + fullQualifiedName);
		if (location != null) buffer.append(", location = " + location.getUniqueId());
		if (scope != null) buffer.append(", scope = " + scope.getScopeName());
		buffer.append(", simpleName = " + simpleName + "]");
		return  buffer.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = getDefinitionKind().id + " Definition [" + getUniqueId() + "]";
		return result;
	}
}
