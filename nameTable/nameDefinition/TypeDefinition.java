package nameTable.nameDefinition;

import java.util.List;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.SystemScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * <p>The class represents a type definition, which can be a detailed type definition, an enumeration type definition, 
 * or a simple type definition. A detailed type definition and an enumeration type definition means we can get its 
 * fields, methods, member types or enumeration constants. A simple type definition means we can not get the detailed
 * definition of the type.
 * 
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2015/11/5
 * 		Refactor the class according to the design document
 */
public abstract class TypeDefinition extends NameDefinition {
	protected boolean isInterface = false;
	protected boolean isPackageMember = true;

	public TypeDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		super(simpleName, fullQualifiedName, location, scope);
	}

	/* (non-Javadoc)
	 * @see nameTable.NameDefinition#getNameDefinitionKind()
	 */
	@Override
	public NameDefinitionKind getDefinitionKind() {
		return NameDefinitionKind.NDK_TYPE;
	}
	
	/**
	 * Test if the type is an interface. 
	 */
	public boolean isInterface() {
		return isInterface;
	}

	/**
	 * Test if the type is an interface. 
	 */
	public boolean isAnonymous() {
		return false;
	}

	/**
	 * Set the type to be an interface or not
	 */
	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}

	/**
	 * Test if the type is defined in package, i.e. test if the type is not a member type
	 */
	public boolean isPackageMember() {
		return isPackageMember;
	}

	/**
	 * Set the type to be a package member type or not
	 */
	public void setPackageMember(boolean isTopLevel) {
		this.isPackageMember = isTopLevel;
	}
	
	/**
	 * Test if the class is public according to the modifier flag
	 */
	public boolean isPublic() {
		return true;
	}

	/**
	 * Test if the type is primitive type 
	 */
	public boolean isPrimitive() {
		return false;
	}

	/**
	 * Return the package definition object which this detailed type belongs to 
	 */
	public PackageDefinition getEnclosingPackage() {
		return null;
	}
	
	/**
	 * @return The definition object of the super class. For simple type definition, return null. For enumeration
	 * type or detailed enumeration, return the type definition object binded to the first reference in the super
	 * list.
	 */
	public TypeDefinition getSuperClassDefinition() {
		return null;
	}
	
	/**
	 * Get the list of super type references 
	 */
	public abstract List<TypeReference> getSuperList();

	/**
	 * Test whether the current type is the sub-type of the given type by the parameter
	 */
	public boolean isSubtypeOf(TypeDefinition parent) {
		if (this == parent) return true;

		List<TypeReference> superList = getSuperList();
		if (superList != null) {
			// Match the definition in the super list!
			for (TypeReference superType : superList) {
				if (!superType.isResolved()) superType.resolveBinding();
				if (superType.getDefinition() == parent) return true;
				if (superType.getDefinition() == this) {
					throw new AssertionError("The super type [" + superType.getName() + ", kind = " + superType.getTypeKind() + "] of " + this.fullQualifiedName + " include itself!");
				}
			}
			// If do match the definition in the super list, recursively judge the super type of the current type 
			// is the sub-type of parent
			for (TypeReference superType : superList) {
				TypeDefinition superDef = (TypeDefinition)superType.getDefinition();
				if (superDef != null) {
					if (superDef.isSubtypeOf(parent)) return true;
				}
			}
			return false;
		} else return matchSubtypeRelationsOfPrimitiveTypes(simpleName, parent.getSimpleName());
	}
	
	
	/**
	 * We often need to resolve reference in EnumTypeDefinition and DetailedTypeDefinition, but we only get 
	 * the object of type definition, and we do not want to cast the object to EnumTypeDefinition or DetailedTypeDefinition
	 * frequently. So we provide a resolve method for type definition
	 */
	public boolean resolve(NameReference reference) {
		return false;
	}
		
	protected static boolean matchSubtypeRelationsOfPrimitiveTypes(String subTypeName, String superTypeName) {
		// Each type is a sub-type of ROOT_OBJECT_NAME (i.e. java.lang.Object)
		if (superTypeName.equals(SystemScope.ROOT_OBJECT_NAME)) return true; 
		
		String subtypeRelations[][] = {
				{NameReferenceLabel.TYPE_BYTE, NameReferenceLabel.TYPE_SHORT},
				{NameReferenceLabel.TYPE_BYTE, NameReferenceLabel.TYPE_INT},
				{NameReferenceLabel.TYPE_BYTE, NameReferenceLabel.TYPE_LONG},
				{NameReferenceLabel.TYPE_BYTE, NameReferenceLabel.TYPE_FLOAT},
				{NameReferenceLabel.TYPE_BYTE, NameReferenceLabel.TYPE_DOUBLE},
				{NameReferenceLabel.TYPE_SHORT, NameReferenceLabel.TYPE_INT},
				{NameReferenceLabel.TYPE_SHORT, NameReferenceLabel.TYPE_LONG},
				{NameReferenceLabel.TYPE_SHORT, NameReferenceLabel.TYPE_FLOAT},
				{NameReferenceLabel.TYPE_SHORT, NameReferenceLabel.TYPE_DOUBLE},
				{NameReferenceLabel.TYPE_INT, NameReferenceLabel.TYPE_LONG},
				{NameReferenceLabel.TYPE_INT, NameReferenceLabel.TYPE_FLOAT},
				{NameReferenceLabel.TYPE_INT, NameReferenceLabel.TYPE_DOUBLE},
				{NameReferenceLabel.TYPE_FLOAT, NameReferenceLabel.TYPE_DOUBLE},
		};
		for (int index = 0; index < subtypeRelations.length; index++) {
			if (subTypeName.equals(subtypeRelations[index][0]) && superTypeName.equals(subtypeRelations[index][1])) return true;
		}
		return false;
	}
}
