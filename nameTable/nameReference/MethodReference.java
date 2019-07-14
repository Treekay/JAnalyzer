package nameTable.nameReference;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The class represent a reference to a method call
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0

 * @update 2015/11/5
 * 		Refactor the class according to the design document. 
 *  	Important notes: The method arguments and type arguments should be setted when we create the method reference.
 */
public class MethodReference extends NameReference {
	private List<NameReference> argumentList = null;		// The arguments in the method call
	private List<TypeReference> typeArgumentList = null;
	// Because there are function overloads, and we can not infer the exact type of the argument, we 
	// can not find the exact method binded to the method call, and then we store all alternative methods
	// for the method call
	private List<MethodDefinition> alternativeList = null;	 

	public MethodReference(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope, NameReferenceKind.NRK_METHOD);
	}

	/**
	 * Return the alternative methods can be binded to the method call
	 */
	public List<MethodDefinition> getAlternativeList() {
		return alternativeList;
	}

	/**
	 * Add an alternative method for this reference
	 */
	public void addAlternative(MethodDefinition method) {
		if (alternativeList == null) alternativeList = new ArrayList<MethodDefinition>();
		alternativeList.add(method);
	}

	/**
	 * Add a list of alternative methods for this reference
	 */
	public void addAlternative(List<MethodDefinition> methods) {
		if (alternativeList == null) alternativeList = new ArrayList<MethodDefinition>();
		alternativeList.addAll(methods);
	}

	public List<NameReference> getArgumentList() {
		return argumentList;
	}

	public void setArgumentList(List<NameReference> arguments) {
		this.argumentList = arguments;
	}

	public List<TypeReference> getTypeArgumentList() {
		return typeArgumentList;
	}

	public void setTypeArgumentList(List<TypeReference> arguments) {
		this.typeArgumentList = arguments;
	}

	/**
	 * Return all reference at the leaf in the method reference, i.e. return all the reference its arguments and type arguments
	 */
	public List<NameReference> getReferencesAtLeaf() {
		List<NameReference> result = new ArrayList<NameReference>();
		result.add(this);
		if (typeArgumentList != null) {
			for (TypeReference typeArgument : typeArgumentList) {
				List<NameReference> referenceList = typeArgument.getReferencesAtLeaf();
				result.addAll(referenceList);
			}
		}
		if (argumentList != null) {
			for (NameReference argument : argumentList) {
				List<NameReference> referenceList = argument.getReferencesAtLeaf();
				result.addAll(referenceList);
			}
		}
		return result;
	}
	
	/**
	 * Return sub-reference in a name reference
	 */
	@Override
	public List<NameReference> getSubReferenceList() {
		List<NameReference> result = new ArrayList<NameReference>();

		if (typeArgumentList != null) {
			for (TypeReference typeArgument : typeArgumentList) result.add(typeArgument);
		}
		if (argumentList != null) {
			for (NameReference argument : argumentList) result.add(argument);
		}
		return result;
	}

	/**
	 * The core reference of method reference is itself
	 */
	public NameReference getCoreReference() {
		return this;
	}
	
	/**
	 * Test whether the reference is a method reference
	 */
	@Override
	public boolean isMethodReference() {
		return true;
	}
}
