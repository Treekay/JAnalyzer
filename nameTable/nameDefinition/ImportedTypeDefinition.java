package nameTable.nameDefinition;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameTableVisitor;
import sourceCodeAST.SourceCodeLocation;

/**
 * The class represents a simple type definition, i.e. we can not get the source code of the type definition, and
 * then we do not its member definitions.
 * 
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2016/11/20
 * 		We rename the class to ImportedTypeDefinition (rather than SimpleTypeDefinition), and support read more information
 * 		on imported types from external files.
 */
public class ImportedTypeDefinition extends TypeDefinition implements NameScope {
	private List<FieldDefinition> fieldList = null;			// The fields of the type
	private List<MethodDefinition> methodList = null;		// The methods of the type
	private List<ImportedTypeDefinition> typeList = null;	// The member types of the type

	private List<TypeReference> superList = null;			// The super types of the type, which include the super class and interfaces of the type. 
	private List<TypeParameterDefinition> typeParameterList = null;
	
	private SourceCodeLocation endLocation = null;

	public ImportedTypeDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope, SourceCodeLocation endLocation) {
		super(simpleName, fullQualifiedName, location, scope);
		this.endLocation = endLocation;
	}

	public ImportedTypeDefinition(String simpleName, String fullQualifiedName, NameScope scope) {
		super(simpleName, fullQualifiedName, null, scope);
	}

	/** 
	 * @see nameTable.TypeDefinition#isDetailedType()
	 */
	@Override
	public boolean isImportedType() {
		return true;
	}

	/** 
	 * @see nameTable.TypeDefinition#isDetailedType()
	 */
	@Override
	public boolean isPrimitive() {
		if (NameReferenceLabel.isPrimitiveTypeName(simpleName)) return true;
		return false;
	}

	@Override
	public String getScopeName() {
		return simpleName;
	}

	@Override
	public SourceCodeLocation getScopeStart() {
		return location;
	}

	@Override
	public SourceCodeLocation getScopeEnd() {
		return endLocation;
	}

	@Override
	public NameScope getEnclosingScope() {
		return scope;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getSubScopeList()
	 */
	@Override
	public List<NameScope> getSubScopeList() {
		if (typeList == null && methodList == null) return null;
		List<NameScope> result = new ArrayList<NameScope>();
		if (typeList != null) {
			for (ImportedTypeDefinition type : typeList) result.add(type);
		}
		if (methodList != null) {
			for (MethodDefinition method : methodList) result.add(method);
		}
		return result;
	}

	@Override
	public boolean containsLocation(SourceCodeLocation location) {
		return false;
	}

	@Override
	public boolean isEnclosedInScope(NameScope scope) {
		if (this.scope == scope) return true;
		return false;
	}

	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		switch (nameDef.getDefinitionKind()) {
		case NDK_TYPE: 
			if (typeList == null) typeList = new ArrayList<ImportedTypeDefinition>();
			TypeDefinition typeNameDef = (TypeDefinition)nameDef;
			if (!typeNameDef.isImportedType()) throw new IllegalNameDefinition("The nested type definition added to a type must to be an imported type definition!");
			typeList.add((ImportedTypeDefinition) nameDef);
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
	
	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_IMPORTED_TYPE;
	}

	@Override
	public void addReference(NameReference reference) {
		
	}

	@Override
	public List<NameReference> getReferenceList() {
		return null;
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
		// the scope of this imported type! 
		// Important Notes: if the reference is not in this imported type, we can not resolve it in the enclosing scope of this imported type 
		// again, since in this case, we intend to test if the reference refers to the member of the imported type, and then we can not resolve
		// it when we can not match the member of the imported type with this reference.
		NameScope referenceScope = reference.getScope();
		if (referenceScope == this || referenceScope.isEnclosedInScope(this)) return getEnclosingScope().resolve(reference);
		else return false;
//		return false;
//		return getEnclosingScope().resolve(reference);
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
	public List<ImportedTypeDefinition> getTypeList() {
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
		if (!superClassRef.isResolved()) superClassRef.resolveBinding();
		
		return (TypeDefinition)superClassRef.getDefinition();
	}

	public List<TypeParameterDefinition> getTypeParameterList() {
		return typeParameterList;
	}
	

	@Override
	public void accept(NameTableVisitor visitor) {
		visitor.preVisit(this);
		
		boolean visitSubscope = visitor.visit(this);

		if (visitSubscope == true && typeList != null) {
			for (ImportedTypeDefinition type : typeList) type.accept(visitor);
		}

		if (visitSubscope == true && methodList != null) {
			for (MethodDefinition method : methodList) method.accept(visitor);
		}
		
		visitor.endVisit(this);
		visitor.postVisit(this);
	}
}
