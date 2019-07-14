package nameTable.nameReference;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A reference for a parameterized type, the information of the main type (for example, the container type List in a parameterized 
 * type List<NameDefinition>) is stored in the inherited fields (including name, location, scope, definition, kind), and the information
 * of parameter types (i.e. the type reference NameDefinition in List<NameDefinition>) is stored in a list of this class. 
 * 
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ6ÈÕ
 * @version 1.0
 * 
 * @update 2015/11/5
 * 		Refactor the class according to the design document.
 * 		Important notes: The primary type is a type reference and no longer a simple name as before. Then the name
 * 		    should be the entire string of this parameterized type reference.
 */
public class ParameterizedTypeReference extends TypeReference {
	private TypeReference primaryType = null;
	private List<TypeReference> argumentList = null;
	
	public ParameterizedTypeReference(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
		typeKind = TypeReferenceKind.TRK_PARAMETERIZED;
	}

	public ParameterizedTypeReference(ParameterizedTypeReference other) {
		super(other);
		primaryType = other.primaryType;
		argumentList = new ArrayList<TypeReference>();
		typeKind = other.typeKind;
		for (TypeReference otherParameter : other.argumentList) argumentList.add(otherParameter);
	}
	
	public void setPrimaryType(TypeReference type) {
		primaryType = type;
	}
	
	public TypeReference getPrimaryType() {
		return primaryType;
	}
	
	public TypeDefinition getPrimaryTypeDefinition() {
		primaryType.resolveBinding();
		return (TypeDefinition)primaryType.getDefinition();
	}
	
	public void addArgument(TypeReference parameterType) {
		if (argumentList == null) argumentList = new ArrayList<TypeReference>();
		argumentList.add(parameterType);
	}
	
	public void setArgumentList(List<TypeReference> argumentList) {
		this.argumentList = argumentList;
	}
	
	public List<TypeReference> getArgumentList() {
		return argumentList;
	}
	
	public List<TypeDefinition> getDefinition(boolean flag) {
		List<TypeDefinition> resultList = new ArrayList<TypeDefinition>();
		if (definition != null) resultList.add((TypeDefinition)definition);
		if (flag == false || argumentList == null) {
			return resultList;
		}
		for (TypeReference argument : argumentList) {
			if (!argument.isParameterizedType()) {
				TypeDefinition parameterTypeDefinition = (TypeDefinition)argument.getDefinition();
				if (parameterTypeDefinition != null && !resultList.contains(parameterTypeDefinition)) 
					resultList.add(parameterTypeDefinition);
			} else {
				ParameterizedTypeReference reference = (ParameterizedTypeReference)argument;
				List<TypeDefinition> parameterTypeDefinitionList = reference.getDefinition(true);
				for (TypeDefinition type : parameterTypeDefinitionList) {
					if (!resultList.contains(type)) resultList.add(type);
				}
			}
		}
		return resultList;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		// Resolve the primary type
		primaryType.resolveBinding();
		// Resolve the parameter types
		if (argumentList != null) {
			for (TypeReference parameterType : argumentList) parameterType.resolveBinding();
		}
		
		if (primaryType.isResolved()) bindTo(primaryType.getDefinition());
		return isResolved();
	}
	
	/**
	 * Return all reference at the leaf in the group, i.e. return all non-group-reference in 
	 * this reference group
	 */
	public List<NameReference> getReferencesAtLeaf() {
		List<NameReference> result = new ArrayList<NameReference>();
		result.addAll(primaryType.getReferencesAtLeaf());
		if (argumentList != null) {
			for (TypeReference argument : argumentList) result.addAll(argument.getReferencesAtLeaf());
		}
		return result;
	}
	
	/**
	 * Return sub-reference in a name reference
	 */
	@Override
	public List<NameReference> getSubReferenceList() {
		List<NameReference> result = new ArrayList<NameReference>();
		result.add(primaryType);
		if (argumentList != null) {
			for (TypeReference argument : argumentList) result.add(argument);
		}
		return result;
	}

	/**
	 * The core reference of parameterized type reference is the core reference of the primary type reference
	 */
	public NameReference getCoreReference() {
		if (primaryType == null) return this;
		else return primaryType.getCoreReference();
	}
	
	public String toFullString() {
		StringBuffer parameterString = new StringBuffer("");
		if (argumentList != null && argumentList.size() > 0) {
			parameterString.append("<" + argumentList.get(0).name);
			for (int index = 1; index < argumentList.size(); index++) {
				parameterString.append(", " + argumentList.get(index).name);
			}
			parameterString.append(">");
		}
		return "Type Reference [Name = " + name + parameterString.toString() + ", location = " + 
				location.getUniqueId() + ", scope = " + scope.getScopeName() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer parameterString = new StringBuffer("");
		if (argumentList != null && argumentList.size() > 0) {
			parameterString.append("<" + argumentList.get(0).name);
			for (int index = 1; index < argumentList.size(); index++) {
				parameterString.append(", " + argumentList.get(index).name);
			}
			parameterString.append(">");
		}
		return "Type Reference [Name = " + primaryType.getName() + parameterString.toString() + " @ " + location.getUniqueId() + "]";
	}
	
	public String toDeclarationString() {
		StringBuffer parameterString = new StringBuffer("");
		if (argumentList != null && argumentList.size() > 0) {
			parameterString.append("<" + argumentList.get(0).name);
			for (int index = 1; index < argumentList.size(); index++) {
				parameterString.append(", " + argumentList.get(index).name);
			}
			parameterString.append(">");
		}
		return primaryType.getName() + parameterString;
	}

	/**
	 * Return a better string of the reference for debugging
	 */
	public String toMultilineString(int indent, boolean includeLiteral) {
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		StringBuffer buffer = new StringBuffer(indentString + "Type Reference " + "[Type Name = " + primaryType.getName());
		if (argumentList != null && argumentList.size() > 0) {
			buffer.append("<" + argumentList.get(0).name);
			for (int index = 1; index < argumentList.size(); index++) {
				buffer.append(", " + argumentList.get(index).name);
			}
			buffer.append(">");
		}
		if (indent > 0) buffer.append(" @" + location.toString() + "]\n");
		else buffer.append(" @" + location.getUniqueId() + "]\n");
			
		return buffer.toString();		
	}
}
