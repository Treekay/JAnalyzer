package nameTable.nameReference.referenceGroup;

import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.SystemScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A class represents a reference of qualified name. Because a field access such as test.data may be parsed 
 * as a qualified name instead of a field access, we should resolve a qualified name as a field access, and then
 * we need a class to represent a reference of qualified name.
 * @author Zhou Xiaocong
 * @since 2013-3-22
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGQualifiedName extends NameReferenceGroup {

	public NRGQualifiedName(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
		// TODO Auto-generated constructor stub
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_QUALIFIED_NAME;
	}

	/* (non-Javadoc)
	 * @see nameTable.nameReference.referenceGroup.NameReferenceGroup#resolveBinding()
	 */
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The qualified name reference group " + this.toFullString() + " has not sub-references!");
		if (subreferences.size() != 2) throw new AssertionError("The qualified name reference group " + this.toFullString() + " does not have 2 sub-references!");

		// For qualified name, we first resolve it as a type, if its prefix is a package name!
		NameDefinition definition = tryToResolveQualifiedNameAsType(this);
		if (definition != null) {
			// This group is bind to the definition
			bindTo(definition);
			return true;
		}
		
		// And then we first resolve it as a field access expression. We should resolve the second reference in the scope defined by 
		// the first expression, which should be binded to a type definition!
		NameReference firstRef = subreferences.get(0);
		TypeDefinition typeDef = null;

		firstRef.resolveBinding(); 
		if (firstRef.isResolved()) typeDef = firstRef.getResultTypeDefinition();
		else {
			// The first expression may be a class name (i.e. use a class name to call its static data!
			if (firstRef.getReferenceKind() == NameReferenceKind.NRK_VARIABLE) {
				// This means the expression is a simple name, and when we create the reference, we set its kind to be NRK_VARIABLE
				// However, a simple name may be a class name! we set the kind to be NRK_TYPE, and try to resolve it!
				firstRef.setReferenceKind(NameReferenceKind.NRK_TYPE);
				firstRef.resolveBinding();
				if (firstRef.isResolved()) {
					typeDef = firstRef.getResultTypeDefinition();
//					if (typeDef == null) firstRef.setReferenceKind(NameReferenceKind.NRK_VARIABLE);
				} else firstRef.setReferenceKind(NameReferenceKind.NRK_VARIABLE);   // We can not resolve it as a class name, restore its kind to NRK_VARIABLE! 
			}
		}

		NameReference fieldRef = subreferences.get(1);
		if (typeDef !=  null) typeDef.resolve(fieldRef);

		// Bind the group to the type of the field definition or enum constant of the second reference (i.e. the field or enum constant reference)
		if (fieldRef.isResolved()) {
			// If we bind the fieldRef successfully, then it is indeed a field reference, so we set it reference kind to NRK_FIELD
			fieldRef.setReferenceKind(NameReferenceKind.NRK_FIELD);

			// And then we bind the whole qualified name to the type of this field!
			NameDefinition nameDef = fieldRef.getDefinition();
			NameDefinitionKind nameDefKind = nameDef.getDefinitionKind();
			if (nameDefKind == NameDefinitionKind.NDK_ENUM_CONSTANT) {
				// The field is an enum constant indeed, then bind it to its enum type!
				EnumTypeDefinition enumType = (EnumTypeDefinition)nameDef.getScope();
				bindTo(enumType);
			} else if (nameDefKind == NameDefinitionKind.NDK_FIELD) {
				// Bind the field to its declared type
				FieldDefinition fieldDef = (FieldDefinition)nameDef;
				bindTo(fieldDef.getTypeDefinition());
			} else {
				throw new AssertionError("Unexpected name definition kind[" + nameDefKind + "] of [" + nameDef.toString() + 
						" for [" + fieldRef.toString() + "] in qualified name [" + this.toString() + "]!");
			}
		}

		if (isResolved()) return true;
		else {
			// If we can not resolve the qualified name as a field access expression, then we try to resolve it as a qualified type name again
			boolean result = scope.resolve(this);
			return result;
		}
	}
	
	/**
	 * The core reference of a qualified name is the last reference
	 */
	public NameReference getCoreReference() {
		return subreferences.get(1).getCoreReference();
	}
	
	private NameDefinition tryToResolveQualifiedNameAsType(NRGQualifiedName reference) {
		NameDefinition result = null;
		
		NameReference qualifiedRef = reference.subreferences.get(0);
		String prefixName = qualifiedRef.getName();
		SystemScope systemScope = SystemScope.getRootScope(scope);
		PackageDefinition packageDef = systemScope.findPackageByName(prefixName); 
		if (packageDef!= null) {
			// The prefix of the qualified name is a package, then resolve this type in the package
			String typeName = reference.getName();
			SourceCodeLocation location = reference.getLocation();
			TypeReference typeRef = new TypeReference(typeName, location, scope);
			packageDef.resolve(typeRef);
			if (typeRef.isResolved()) {
				// Resolve the qualified name (corresponding to the first reference) as a type, and then bind the firstRef to its definition!
				result = typeRef.getDefinition();
			} 
		}
		return result;
	}
}
