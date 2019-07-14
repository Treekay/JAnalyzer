package nameTable.nameDefinition;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.ParameterizedTypeReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import sourceCodeAST.SourceCodeLocation;

/**
 * The class represents a field definition
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2013-12-29
 * 		Add method getTypeDefinition()
 * 
 * @update 2015/11/5
 * 		Refactor the class according to the design document
 */
public class FieldDefinition extends NameDefinition {
	private TypeReference type = null;			// The type of the field
	private int modifier = 0;					// The modifier flag of the field
	private NameReference initializer = null;	// The reference of initialize expression 

	public FieldDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		super(simpleName, fullQualifiedName, location, scope);
	}

	/* (non-Javadoc)
	 * @see nameTable.NameDefinition#getNameDefinitionKind()
	 */
	@Override
	public NameDefinitionKind getDefinitionKind() {
		return NameDefinitionKind.NDK_FIELD;
	}

	/**
	 * @return the reference of the field type
	 */
	public TypeReference getType() {
		return type;
	}
	
	/**
	 * @return the definition of the field type
	 */
	public TypeDefinition getTypeDefinition() {
		if (type == null) return null;
		
		type.resolveBinding();
		return (TypeDefinition)type.getDefinition();
	}
	
	/**
	 * @param flag: if flag == true, then we return its main type and its parameter types when the type of the field
	 * is a parameterized type, otherwise we only return its main type
	 * @return the possible list of type definition of the field type. If the type of the field is a parameterized
	 * type, then we return its main type and its parameter types.
	 */
	public List<TypeDefinition> getTypeDefinition(boolean flag) {
		if (type == null) return new ArrayList<TypeDefinition>();
		type.resolveBinding();
		if (flag == false || !type.isParameterizedType()) {
			List<TypeDefinition> resultList = new ArrayList<TypeDefinition>();
			TypeDefinition fieldType = (TypeDefinition)type.getDefinition(); 
			if (fieldType != null) resultList.add(fieldType);
			return resultList;
		}
		
		ParameterizedTypeReference reference = (ParameterizedTypeReference)type;
		return reference.getDefinition(true);
	}

	/**
	 * @param type the type to set
	 */
	public void setType(TypeReference type) {
		this.type = type;
	}
	
	/**
	 * Reset type reference to null if it matches the type parameter in the list!
	 */
	public void resetTypeBinding(List<TypeParameterDefinition> typeParameterList) {
		if (type != null) {
			for (TypeParameterDefinition typeParameter : typeParameterList) {
				if (type.getName().equals(typeParameter.getSimpleName())) {
					type.resetBinding();
					return;
				}
			}
		}
	}

	
	/**
	 * Set reference of initialize expression of the field 
	 */
	public void setInitializer(NameReference initializer) {
		this.initializer = initializer;
	}
	
	/**
	 * Get reference of initialize expression of the field 
	 */
	public NameReference getInitializer() {
		return initializer;
	}

	/**
	 * Return the package definition object which this detailed type belongs to 
	 */
	public TypeDefinition getEnclosingType() {
		NameScope currentScope = scope;
		while (currentScope.getScopeKind() != NameScopeKind.NSK_DETAILED_TYPE && currentScope.getScopeKind() != NameScopeKind.NSK_IMPORTED_TYPE) 
			currentScope = currentScope.getEnclosingScope();
		return (TypeDefinition)currentScope;
	}
	
	/**
	 * Display a field definition as "type[] fieldName"
	 */
	public String toDeclarationString() {
		StringBuffer buffer = new StringBuffer();
		String typeString = type.getName();
		for (int count = 0; count < type.getDimension(); count++) typeString += "[]";
		buffer.append(typeString + " " + simpleName);
		return buffer.toString();
	}
		
	/**
	 * Set the modifier flag 
	 */
	public void setModifierFlag(int flag) {
		this.modifier = flag;
	}
	
	/**
	 * Test if the class is public according to the modifier flag
	 */
	public boolean isPublic() {
		return Modifier.isPublic(modifier);
	}

	/**
	 * Test if the class is private according to the modifier flag
	 */
	public boolean isPrivate() {
		return Modifier.isPrivate(modifier);
	}

	/**
	 * Test if the class is protected according to the modifier flag
	 */
	public boolean isProtected() {
		return Modifier.isProtected(modifier);
	}

	/**
	 * Test if the class is static according to the modifier flag
	 */
	public boolean isStatic() {
		return Modifier.isStatic(modifier);
	}

	/**
	 * Test if the class is protected according to the modifier flag
	 */
	public boolean isFinal() {
		return Modifier.isFinal(modifier);
	}
}
