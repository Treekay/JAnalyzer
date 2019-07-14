package nameTable.nameReference.referenceGroup;

import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to field access expression. 
 * <p>		FieldAccess: Expression.Identifier 
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGFieldAccess extends NameReferenceGroup {

	public NRGFieldAccess(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_FIELD_ACCESS;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The field access reference group " + this.toFullString() + " has not sub-references!");
		
		// For field access expression, we should resolve the second reference in the scope defined by 
		// the first expression, which should be binded to a type definition!
		NameReference firstRef = subreferences.get(0);
		firstRef.resolveBinding();
		TypeDefinition typeDef = firstRef.getResultTypeDefinition();
		NameReference fieldRef = subreferences.get(1);
		if (typeDef !=  null) typeDef.resolve(fieldRef);

		// Bind the group to the type of the field definition of the second reference (i.e. the field reference)
		if (fieldRef.isResolved()) {
			FieldDefinition fieldDef = (FieldDefinition)fieldRef.getDefinition();
			TypeReference typeRef = fieldDef.getType();
			typeRef.resolveBinding();
			bindTo(typeRef.getDefinition());
		}

		return isResolved();
	}
	
	/**
	 * The core reference of a field access expression is the field reference in the expression
	 */
	public NameReference getCoreReference() {
		return subreferences.get(1).getCoreReference();
	}
}
