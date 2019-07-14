package nameTable.nameDefinition;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;

import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.ParameterizedTypeReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameTableVisitor;
import sourceCodeAST.SourceCodeLocation;

/**
 * The class represent a method definition
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2013-12-28
 * 		Add method mathMethod(MethodReference) to match the method definition with a method reference. Now we consider the number of parameter, but we
 * 			do not consider the type conversion between the actual parameters and formal parameters.
 * @update 2013-12-29
 * 		Add method getReturnTypeDefinition()
 * 		Add method isOverrideMethod()
 * 
 * @update 2016/11/5
 * 		Refactor the class according to the design document
 */
public class MethodDefinition extends NameDefinition implements NameScope {
	private TypeReference returnType = null;				// The return type of the method
	private List<VariableDefinition> parameterList = null;	// The parameter list of the method
	private List<TypeReference> throwTypeList = null;		// The throw types declared for the method
	private List<TypeParameterDefinition> typeParameterList = null;

	private LocalScope bodyScope = null;					// The local scope corresponding to the body of the method
	private SourceCodeLocation endLocation = null;
	
	private List<NameReference> referenceList = null;		// The reference defined in the method, i.e. the type references of the parameters of the method
	private int modifier = 0;								// The modifier flag of the method
	private boolean constructorFlag = false;
	
	public MethodDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope, SourceCodeLocation endLocation) {
		super(simpleName, fullQualifiedName, location, scope);
		this.endLocation = endLocation;
	}

	@Override
	public boolean containsLocation(SourceCodeLocation location) {
		return location.isBetween(this.location, endLocation);
	}

	/* (non-Javadoc)
	 * @see nameTable.NameDefinition#getNameDefinitionKind()
	 */
	@Override
	public NameDefinitionKind getDefinitionKind() {
		return NameDefinitionKind.NDK_METHOD;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#define(nameTable.NameDefinition)
	 */
	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		if (nameDef.getDefinitionKind() == NameDefinitionKind.NDK_PARAMETER) {
			if (parameterList == null) parameterList = new ArrayList<VariableDefinition>();
			parameterList.add((VariableDefinition)nameDef);
		} else if (nameDef.getDefinitionKind() == NameDefinitionKind.NDK_TYPE_PARAMETER) {
			if (typeParameterList == null) typeParameterList = new ArrayList<TypeParameterDefinition>();
			typeParameterList.add((TypeParameterDefinition) nameDef);
		} else {
			throw new IllegalNameDefinition("Only parameters or type parameter can be defined in a method definition!");
		}
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getEnclosingScope()
	 */
	@Override
	public NameScope getEnclosingScope() {
		return scope;
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
	

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getNameScopeKind()
	 */
	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_METHOD;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getScopeName()
	 */
	@Override
	public String getScopeName() {
		return simpleName;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getSubScopeList()
	 */
	@Override
	public List<NameScope> getSubScopeList() {
		if (bodyScope == null) return null;
		List<NameScope> result = new ArrayList<NameScope>();
		result.add(bodyScope);
		return result;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#resolve(nameTable.NameReference)
	 */
	@Override
	public boolean resolve(NameReference reference) {
//		if (reference.getName().equals("ORB")) System.out.println("Resolve ORB in method " + this.getScopeName());

		// In a method definitions, we can only resolve the parameters and the type (with type
		// parameter of the method) for local variables and parameters defined in the method or the method itself.
		if (reference.getReferenceKind() == NameReferenceKind.NRK_VARIABLE) {
			if (parameterList != null) {
				for (VariableDefinition var : parameterList) {
					if (var.match(reference)) return true;
				}
			}
		} else if (reference.isTypeReference()) {
			if (typeParameterList != null) {
				for (TypeParameterDefinition typePara : typeParameterList) {
					if (typePara.matchTypeReference((TypeReference)reference)) return true;
				}
			}
		}
		return getEnclosingScope().resolve(reference);
	}

	/**
	 * @return the reference of the return type
	 */
	public TypeReference getReturnType() {
		return returnType;
	}
	
	/**
	 * @return the definition of the return type
	 */
	public TypeDefinition getReturnTypeDefinition() {
		if (returnType == null) return null;
		returnType.resolveBinding();
		return (TypeDefinition)returnType.getDefinition();
	}

	/**
	 * @param flag: if flag == true, then we return its main type and its parameter types when the return type of the method
	 * is a parameterized type, otherwise we only return its main type
	 * @return the possible list of type definition of the return type. If the return type of the method is a parameterized
	 * type, then we return its main type and its parameter types.
	 */
	public List<TypeDefinition> getReturnTypeDefinition(boolean flag) {
		if (returnType == null) return new ArrayList<TypeDefinition>();
		returnType.resolveBinding();
		if (flag == false || !returnType.isParameterizedType()) {
			List<TypeDefinition> resultList = new ArrayList<TypeDefinition>();
			TypeDefinition type = (TypeDefinition)returnType.getDefinition(); 
			if (type != null) resultList.add(type);
			return resultList;
		}
		
		ParameterizedTypeReference reference = (ParameterizedTypeReference)returnType;
		return reference.getDefinition(true);
	}
	
	/**
	 * @param returnType the returnType to set
	 */
	public void setReturnType(TypeReference returnType) {
		this.returnType = returnType;
	}

	/**
	 * @return the parameters
	 */
	public List<VariableDefinition> getParameterList() {
		return parameterList;
	}

	/**
	 * Reset return type reference to null if it matches the type parameter in the list!
	 */
	public void resetReturnTypeBinding(List<TypeParameterDefinition> typeParameterList) {
		if (returnType != null) {
			for (TypeParameterDefinition typeParameter : typeParameterList) {
				if (returnType.getName().equals(typeParameter.getSimpleName())) {
					returnType.resetBinding();
					return;
				}
			}
		}
	}

	/**
	 * Reset parameter type reference to null if it matches the type parameter in the list!
	 */
	public void resetParameterTypeBinding(List<TypeParameterDefinition> typeParameterList) {
		if (parameterList != null) {
			for (VariableDefinition variableDefinition : parameterList) {
				variableDefinition.resetTypeBinding(typeParameterList);
			}
		}
	}
	
	/**
	 * @return the bodyScope
	 */
	public LocalScope getBodyScope() {
		return bodyScope;
	}

	/**
	 * @param bodyScope the bodyScope to set
	 */
	public void setBodyScope(LocalScope bodyScope) {
		this.bodyScope = bodyScope;
	}
	
	/**
	 * Return the throw types declared for the method
	 */
	public List<TypeReference> getThrowTypeList() {
		return throwTypeList;
	}
	
	/**
	 * Add a throw type for the method
	 */
	public void addThrowType(TypeReference type) {
		if (throwTypeList == null) throwTypeList = new ArrayList<TypeReference>();
		throwTypeList.add(type);
	}

	@Override
	public void addReference(NameReference reference) {
		if (reference == null) return;
		if (referenceList == null) referenceList = new ArrayList<NameReference>();
		referenceList.add(reference);
		
	}

	@Override
	public boolean isEnclosedInScope(NameScope ancestorScope) {
		NameScope parent = getEnclosingScope();
		while (parent != null) {
			if (parent == ancestorScope) return true;
			parent = parent.getEnclosingScope();
		}
		return false;
	}
	
	/**
	 * Match the current method definition with a method reference. Since it can not only match the method name, so we must use this method for 
	 * resolve a method reference.
	 * 
	 * @since 2013-12-28
	 * @update 2017-08-15 
	 *   Deal with type parameter and its argument when match method
	 */
	public boolean matchMethod(MethodReference reference) {
		String referenceName = reference.getName();
		
		// At first we should match the method name
		if (!match(referenceName)) return false;
		
		List<NameReference> args = reference.getArgumentList();

		// Test if the number of arguments is equal to the number of parameters of the method
		if (parameterList == null) {
			if (args == null) return true;
			if (args.size() <= 0) return true;
			return false;
		}
		if (args == null) return false;
		if (args.size() != parameterList.size()) return false;
		
		// Instantiate type parameter in method definition
		if (typeParameterList != null) {
			List<TypeReference> typeArgumentList = reference.getTypeArgumentList();
			if (typeArgumentList == null) {
				// All type parameters instantiate to Object
				SystemScope rootScope = getRootScope();
				NameDefinition rootObject = rootScope.getRootObjectDefinition();
				TypeReference objectReference = new TypeReference(SystemScope.ROOT_OBJECT_NAME, null, rootScope);
				objectReference.bindTo(rootObject);
				for (TypeParameterDefinition typePara : typeParameterList) typePara.setCurrentValue(objectReference);
			} else {
				if (typeParameterList.size() != typeArgumentList.size()) return false;
				for (int index = 0; index < typeParameterList.size(); index++) {
					TypeParameterDefinition typePara = typeParameterList.get(index);
					typePara.setCurrentValue(typeArgumentList.get(index));
				}
			}
			// Reset return type and parameter type reference for new resolving!
			resetReturnTypeBinding(typeParameterList);
			resetParameterTypeBinding(typeParameterList);
		}

		// Test if the type of the argument is the sub-type of the type of the corresponding parameter
		for (int index = 0; index < args.size(); index++) {
			NameReference argument = args.get(index);
			VariableDefinition parameter = parameterList.get(index);
			TypeDefinition argumentType = argument.getResultTypeDefinition();
			TypeDefinition paraType = parameter.getTypeDefinition();

			if (argumentType != null && paraType != null) {
				// The type of the argument should be the sub-type of the type of the parameter, otherwise the (actual) argument can not 
				// be assigned to the (formal) parameter!
				if (!argumentType.isSubtypeOf(paraType)) {
					return false;
				}
			} // If we can not resolve the argument type or the parameter type, then we ignore the type compatibility between the argument  and the parameter.
		}
		return true;
	}

	/**
	 * Test if the current method redefine a given method, i.e. the current method and the given method have the same name and 
	 * the same signature (parameter type and return type). Of course, the override relation indeed hold only when the current 
	 * method and the given method in two types has inheritance relations. 
	 * 
	 * @since 2013-12-29
	 */
	public boolean isOverrideMethod(MethodDefinition other) {
		String otherSimpleName = other.getSimpleName();
		
		// At first the two methods must have the same simple name
		if (!simpleName.equals(otherSimpleName)) return false;
		
		List<VariableDefinition> otherParas = other.getParameterList();

		// The return types of the two methods should bind to the same type definition, otherwise they do not have the same signature 
		if (getReturnTypeDefinition() != other.getReturnTypeDefinition()) return false;
		
		// Test if the number of arguments is equal to the number of parameters of the method
		if (otherParas == null && parameterList == null) return true;
		if (otherParas == null && parameterList != null) return false;
		if (otherParas != null && parameterList == null) return false;
		assert(otherParas != null && parameterList != null);
		if (otherParas.size() != parameterList.size()) return false;
		
		for (int index = 0; index < otherParas.size(); index++) {
			VariableDefinition parameter = parameterList.get(index);
			VariableDefinition otherPara = otherParas.get(index);
			
			// The two parameters' type should bind to the same type definition, otherwise they do not have the same signature
			if (parameter.getTypeDefinition() != otherPara.getTypeDefinition()) return false;
		}
		
		return true;
	}
	
	/**
	 * Test if the current method overload a given method, i.e. the current method and the given method have the same name but have  
	 * different parameter types. Of course, the overload relation indeed hold only when the current  method and the given method 
	 * in the same types or in two types has inheritance relations.
	 * 
	 * @since 2015-10-24
	 */
	public boolean isOverloadMethod(MethodDefinition other) {
		String otherSimpleName = other.getSimpleName();
		
		// At first the two methods must have the same simple name
		if (!simpleName.equals(otherSimpleName)) return false;
		
		List<VariableDefinition> otherParas = other.getParameterList();
		
		// Test if the number of arguments is equal to the number of parameters of the method
		if (otherParas == null && parameterList == null) return false;		// This case means two methods overridden rather than overloaded
		if (otherParas == null && parameterList != null) return true;
		if (otherParas != null && parameterList == null) return true;
		if (otherParas.size() != parameterList.size()) return true;
		
		for (int index = 0; index < otherParas.size(); index++) {
			VariableDefinition parameter = parameterList.get(index);
			VariableDefinition otherPara = otherParas.get(index);
			
			// The two parameters' type should bind to the same type definition, otherwise they do not have the same signature
			if (parameter.getTypeDefinition() != otherPara.getTypeDefinition()) return true;
		}
		return false;		// Also, this case means two methods overridden rather than overloaded.
	}
	
	public SourceCodeLocation getEndLocation() {
		return endLocation;
	}

	@Override
	/**
	 * Accept a visitor to visit the current scope
	 */
	public void accept(NameTableVisitor visitor) {
		visitor.preVisit(this);
		
		boolean visitSubscope = visitor.visit(this);
		
		if (visitSubscope == true && bodyScope != null) bodyScope.accept(visitor);
		
		visitor.endVisit(this);
		visitor.postVisit(this);
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

	/**
	 * Test if the class is abstract according to the modifier flag
	 */
	public boolean isAbstract() {
		return Modifier.isAbstract(modifier);
	}
	
	public void setConstructor(boolean isConstruct) {
		constructorFlag = isConstruct;
	}
	
	public void setConstructor() {
		constructorFlag = true;
	}
	
	public boolean isConstructor() {
		return constructorFlag;
	}
	
	public boolean isAutoGenerated() {
		return false;
	}

	@Override
	public SourceCodeLocation getScopeStart() {
		return getLocation();
	}

	@Override
	public SourceCodeLocation getScopeEnd() {
		// TODO Auto-generated method stub
		return endLocation;
	}
	
	@Override
	public List<NameReference> getReferenceList() {
		return referenceList;
	}
	
}
