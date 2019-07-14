package nameTable.nameReference.referenceGroup;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to type cast expression. 
 * <p>		CastExpression: (Type) Expression
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGCast extends NameReferenceGroup {

	public NRGCast(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
		// TODO Auto-generated constructor stub
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_CAST;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The cast reference group " + this.toFullString() + " has not sub-references!");
		
		for (NameReference reference : subreferences) reference.resolveBinding();
		// Bind the group to the type definition of the first reference (which is a type reference) of 
		// the cast expression
		NameReference firstRef = subreferences.get(0);
		NameReferenceKind firstRefKind = firstRef.getReferenceKind();
		if (firstRefKind == NameReferenceKind.NRK_TYPE){
			TypeReference typeRef = (TypeReference)firstRef;
			if (!typeRef.isResolved()) typeRef.resolveBinding();
			bindTo(typeRef.getDefinition());
		} else {
			throw new AssertionError("The first operand in cast expression " + this.toFullString() + " is not a type!");
		}

		return isResolved();
	}
	
	/**
	 * The core reference of an assignment expression is the core reference of its second expression (i.e. the value is 
	 * to be casted.
	 */
	public NameReference getCoreReference() {
		return subreferences.get(1).getCoreReference();
	}
}
