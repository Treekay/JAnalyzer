package nameTable.nameDefinition;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;

import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameTableVisitor;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name definition represent a type with detailed field, method and member type definitions, i.e. we can
 * access the source code of the type definition
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2016/11/5
 * 		Refactor the class according to the design document
 */
public class DetailedTypeDefinition extends TypeDefinition implements NameScope {
	private List<FieldDefinition> fieldList = null;			// The fields of the type
	private List<MethodDefinition> methodList = null;		// The methods of the type
	private List<DetailedTypeDefinition> typeList = null;	// The member types of the type

	private List<TypeReference> superList = null;			// The super types of the type, which include the super class and interfaces of the type. 
	private List<TypeParameterDefinition> typeParameterList = null;
	private List<LocalScope> initializerList = null;
	
	private SourceCodeLocation endLocation = null;
	private int modifier = 0; 									// The modifier flag of the detailed type
															// The first super type must be the super class of the type
	private List<NameReference> referenceList = null;
	
	public DetailedTypeDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope, SourceCodeLocation endLocation) {
		super(simpleName, fullQualifiedName, location, scope);
		this.endLocation = endLocation;
	}
	
	@Override
	public boolean containsLocation(SourceCodeLocation location) {
		return location.isBetween(this.location, endLocation);
	}
	
	/* (non-Javadoc)
	 * @see nameTable.TypeDefinition#isDetailedType()
	 */
	@Override
	public boolean isDetailedType() {
		return true;
	}

	@Override
	public boolean isEnumType() {
		return false;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#define(nameTable.NameDefinition)
	 */
	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		switch (nameDef.getDefinitionKind()) {
		case NDK_TYPE: 
			if (typeList == null) typeList = new ArrayList<DetailedTypeDefinition>();
			TypeDefinition typeNameDef = (TypeDefinition)nameDef;
			if (!typeNameDef.isDetailedType()) throw new IllegalNameDefinition("The nested type definition added to a type must to be a detailed type definition!");
			typeList.add((DetailedTypeDefinition) nameDef);
			break;
		case NDK_METHOD: 
			if (methodList == null) methodList = new ArrayList<MethodDefinition>();
			methodList.add((MethodDefinition) nameDef);
			break;
		case NDK_FIELD: 
			if (fieldList == null) fieldList = new ArrayList<FieldDefinition>();
			fieldList.add((FieldDefinition) nameDef);
			break;
		case NDK_TYPE_PARAMETER: 
			if (typeParameterList == null) typeParameterList = new ArrayList<TypeParameterDefinition>();
			typeParameterList.add((TypeParameterDefinition) nameDef);
			break;
		default:
			throw new IllegalNameDefinition("The kind of name definition in a type have to be NDK_TYPE, NDK_METHOD or NDK_FIELD!");
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
	@Override
	public PackageDefinition getEnclosingPackage() {
		NameScope currentScope = scope;
		while (currentScope.getScopeKind() != NameScopeKind.NSK_PACKAGE) currentScope = currentScope.getEnclosingScope();
		return (PackageDefinition)currentScope;
	}
	
	/* (non-Javadoc)
	 * @see nameTable.NameScope#getNameScopeKind()
	 */
	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_DETAILED_TYPE;
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
		if (typeList == null && methodList == null) return null;
		List<NameScope> result = new ArrayList<NameScope>();
		if (typeList != null) {
			for (DetailedTypeDefinition type : typeList) result.add(type);
		}
		if (methodList != null) {
			for (MethodDefinition method : methodList) result.add(method);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#resolve(nameTable.NameReference)
	 */
	@Override
	public boolean resolve(NameReference reference) {
		NameReferenceKind refKind = reference.getReferenceKind();
		if (refKind == NameReferenceKind.NRK_FIELD || refKind == NameReferenceKind.NRK_VARIABLE) {
			if (fieldList != null) {
				for (FieldDefinition field : fieldList) {
					if (field.match(reference)) return true;
				}
			}
		} else if (refKind == NameReferenceKind.NRK_METHOD) {
			if (methodList != null) {
				MethodReference methodRef = (MethodReference)reference;
				for (MethodDefinition method : methodList) {
					if (method.matchMethod(methodRef)) {
						methodRef.addAlternative(method);
						
						// Find all methods defined in the sub-type of the current detailed type definition and redefine (i.e. override) the matched method
						SystemScope root = SystemScope.getRootScope(scope);		// We do this in the root scope (i.e. the system scope)!
						List<MethodDefinition> redefinedMethods = root.getAllOverrideMethods(this, method);
						methodRef.addAlternative(redefinedMethods);  // Add all alternatives to the method reference!
					} else {
						// Do nothing if we do not match any method!
					}
				}
				List<MethodDefinition> methodDefList = methodRef.getAlternativeList();
				if (methodDefList != null) {
					if (methodDefList.size() > 0) {
						MethodDefinition firstMethodDef = methodDefList.get(0);
						methodRef.bindTo(firstMethodDef);
						return true;
					}
				}
			}
		} else if (refKind == NameReferenceKind.NRK_TYPE) {
			if (this.match(reference)) return true;
			if (typeList != null) {
				for (TypeDefinition type : typeList) {
					if (type.match(reference)) return true;
				}
			}
			if (reference.isTypeReference()) {
				if (typeParameterList != null) {
					for (TypeParameterDefinition typePara : typeParameterList) {
						if (typePara.matchTypeReference((TypeReference)reference)) return true;
					}
				}
			} // else if a reference with kind == NRK_TYPE, but it is not type reference, then it is a qualifier of a qualified name, a type parameter can not be a qualifier! 
		}

		// If we can not match the name in the fields, methods and types of the type, we resolve the  
		// reference in the super class and super interface of the type. However, if the reference is a type reference, we should not resolve it 
		// in super class or super interface, since a class can not inherent a type, and an inner class name must be accessed by given its outter class name!
		if (refKind != NameReferenceKind.NRK_TYPE && superList != null) {
			for (TypeReference superTypeRef : superList) {
				if (!superTypeRef.isResolved()) superTypeRef.resolveBinding();
				if (superTypeRef.isResolved()) {
					TypeDefinition superTypeDef = (TypeDefinition)superTypeRef.getDefinition();
					// Resolve the reference in the super class or super interface of the type 
					if (superTypeDef.resolve(reference)) return true;
				}
			}
		}
		
		// If we can resolve the name reference in the super class, we resolve the reference in the enclosing scope when the reference is in 
		// the scope of this detailed type! 
		// Important Notes: if the reference is not in this detailed type, we can not resolve it in the enclosing scope of this detailed type 
		// again, since in this case, we intend to test if the reference refers to the member of the detailed type, and then we can not resolve
		// it when we can not match the member of the detailed type with this reference.
		NameScope referenceScope = reference.getScope();
		if (referenceScope == this || referenceScope.isEnclosedInScope(this)) return getEnclosingScope().resolve(reference);
		return false;
	}

	/**
	 * Get the list of fields defined in this type
	 */
	public List<FieldDefinition> getFieldList() {
		return fieldList;
	}
	
	/**
	 * Get the list of methods defined in this type
	 */
	public List<MethodDefinition> getMethodList() {
		return methodList;
	}

	/**
	 * Get the list of member types defined in this type
	 */
	public List<DetailedTypeDefinition> getTypeList() {
		return typeList;
	}

	/**
	 * Reset field type reference to null if it matches the type parameter in the list!
	 */
	public void resetFieldTypeBinding(List<TypeParameterDefinition> typeParameterList) {
		if (fieldList != null) {
			for (FieldDefinition field : fieldList) field.resetTypeBinding(typeParameterList);
		}
	}
	
	/**
	 * Reset field type reference to null if it matches the type parameter in the list!
	 */
	public void resetMethodReturnTypeAndParameterTypeBinding(List<TypeParameterDefinition> typeParameterList) {
		if (methodList != null) {
			for (MethodDefinition method : methodList) {
				method.resetReturnTypeBinding(typeParameterList);
				method.resetParameterTypeBinding(typeParameterList);
			}
		}
	}
	
	
	/**
	 * Get the list of super type, which include super class and super interfaces of the current type
	 */
	public List<TypeReference> getSuperList() {
		return superList;
	}

	public void addSuperType(TypeReference superType) {
		if (superList == null) superList = new ArrayList<TypeReference>();
		superList.add(superType);
	}

	@Override
	public TypeDefinition getSuperClassDefinition() {
		if (superList == null) return null;
		if (superList.size() < 1) return null;
		TypeReference superClassRef = superList.get(0);
		if (superClassRef.resolveBinding()) {
			TypeDefinition superTypeDefinition = (TypeDefinition)superClassRef.getDefinition();
			if (superTypeDefinition.isInterface()) return null;
			else return superTypeDefinition;
		}
		
		return null;
	}

	public List<TypeParameterDefinition> getTypeParameterList() {
		return typeParameterList;
	}
	
	public boolean addInitializer(LocalScope initializer) {
		if (initializerList == null) initializerList = new ArrayList<LocalScope>();
		return initializerList.add(initializer);
	}
	
	public List<LocalScope> getInitializerList() {
		return initializerList;
	}

	@Override
	public void addReference(NameReference reference) {
		if (reference == null) return;
		if (referenceList == null) referenceList = new ArrayList<NameReference>();
		referenceList.add(reference);
		
	}
	
	/**
	 * Get the end location of the type definition
	 */
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

		if (visitSubscope == true && initializerList != null) {
			for (LocalScope initializer : initializerList) initializer.accept(visitor);
		}
		
		if (visitSubscope == true && typeList != null) {
			for (DetailedTypeDefinition type : typeList) type.accept(visitor);
		}

		if (visitSubscope == true && methodList != null) {
			for (MethodDefinition method : methodList) method.accept(visitor);
		}
		
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
	@Override
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

	/**
	 * Test if the class is anonymous
	 */
	public boolean isAnonymous() {
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
	public boolean isEnclosedInScope(NameScope ancestorScope) {
		NameScope parent = getEnclosingScope();
		while (parent != null) {
			if (parent == ancestorScope) return true;
			parent = parent.getEnclosingScope();
		}
		return false;
	}
	
	@Override
	public List<NameReference> getReferenceList() {
		return referenceList;
	}
}
