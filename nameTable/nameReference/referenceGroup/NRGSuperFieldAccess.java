package nameTable.nameReference.referenceGroup;

import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to super field access expression. 
 * <p>		SuperFieldAccess: [ ClassName . ] super . Identifier
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGSuperFieldAccess extends NameReferenceGroup {

	public NRGSuperFieldAccess(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_SUPER_FIELD_ACCESS;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The super field access reference group " + this.toFullString() + " has not sub-references!");

		// For super field access, if the first reference is a type reference, then resolve the reference, 
		// bind it to a type definition, and get the super type of this type definition, and resolve the 
		// second reference in the super type.
		// If the first reference is not a type reference, then find the first type definition enclosing 
		// the reference, and resolve the reference in the super type of the type definition
		NameReference firstRef = subreferences.get(0);
		NameReference fieldRef = null;
		TypeDefinition superTypeDef = null;
		TypeDefinition typeDef = null;
		if (firstRef.getReferenceKind() == NameReferenceKind.NRK_TYPE){
			TypeReference typeRef = (TypeReference)firstRef;
			typeRef.resolveBinding();
			typeDef = (TypeDefinition)typeRef.getDefinition();
			fieldRef = subreferences.get(1);
		} else {
			typeDef = getEnclosingTypeDefinition();
			fieldRef = firstRef;
		}
		if (typeDef != null) superTypeDef = typeDef.getSuperClassDefinition();
		if (superTypeDef != null) {
			superTypeDef.resolve(fieldRef);
		}
		// Bind the group to the type of the field definition
		if (fieldRef.isResolved()) {
			FieldDefinition fieldDef = (FieldDefinition)fieldRef.getDefinition();
			TypeReference typeRef = fieldDef.getType();
			typeRef.resolveBinding();
			bindTo(typeRef.getDefinition());
		}

		return isResolved();
	}
	
	/**
	 * The core reference of a super field access expression is the field reference in the expression
	 */
	public NameReference getCoreReference() {
		NameReference firstRef = subreferences.get(0);
		NameReference fieldRef = null;
		if (firstRef.getReferenceKind() == NameReferenceKind.NRK_TYPE){
			fieldRef = subreferences.get(1);
		} else {
			fieldRef = firstRef;
		}
		return fieldRef.getCoreReference();
	}

}
