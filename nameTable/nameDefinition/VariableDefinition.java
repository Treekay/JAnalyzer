package nameTable.nameDefinition;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.ParameterizedTypeReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The class represents a variable definition or a method parameter definition
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
public class VariableDefinition extends NameDefinition {
	private NameDefinitionKind kind = NameDefinitionKind.NDK_VARIABLE;
	private TypeReference type = null;
	
	public VariableDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		super(simpleName, fullQualifiedName, location, scope);
	}

	/* (non-Javadoc)
	 * @see nameTable.NameDefinition#getNameDefinitionKind()
	 */
	@Override
	public NameDefinitionKind getDefinitionKind() {
		return kind;
	}

	/**
	 * Set the kind of the variable definition, which can be NDK_PARAMETER or NDK_VARIABLE
	 */
	public void setDefinitionKind(NameDefinitionKind kind) {
		this.kind = kind;
	}

	/**
	 * Return the reference of the type declared for the variable or parameter
	 */
	public TypeReference getType() {
		return type;
	}

	/**
	 * Return the definition of the type declared for the variable or parameter
	 */
	public TypeDefinition getTypeDefinition() {
		if (type == null) return null;
		type.resolveBinding();
		return (TypeDefinition)type.getDefinition();
	}
	
	/**
	 * @param flag: if flag == true, then we return its main type and its parameter types when the type of the variable
	 * is a parameterized type, otherwise we only return its main type
	 * @return the possible list of type definition of the field type. If the type of the variable is a parameterized
	 * type, then we return its main type and its parameter types.
	 */
	public List<TypeDefinition> getTypeDefinition(boolean flag) {
		if (type == null) return new ArrayList<TypeDefinition>();
		type.resolveBinding();
		if (flag == false || !type.isParameterizedType()) {
			List<TypeDefinition> resultList = new ArrayList<TypeDefinition>();
			TypeDefinition variableType = (TypeDefinition)type.getDefinition(); 
			if (variableType != null) resultList.add(variableType);
			return resultList;
		}
		
		ParameterizedTypeReference reference = (ParameterizedTypeReference)type;
		return reference.getDefinition(true);
	}

	/**
	 * Set the type declared of the variable or parameter
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
	 * Display a variable definition as "type[] variableName"
	 */
	public String toDeclarationString() {
		StringBuffer buffer = new StringBuffer();
		String typeString = type.getName();
		for (int count = 0; count < type.getDimension(); count++) typeString += "[]";
		buffer.append(typeString + " " + simpleName);
		return buffer.toString();
	}
}
