package nameTable.nameReference.referenceGroup;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to array creation expression. 
 * <p>		ArrayCreation: new Type[Expression] ArrayInitializer
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGArrayCreation extends NameReferenceGroup {

	public NRGArrayCreation(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_ARRAY_CREATION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The array creation reference group " + this.toFullString() + " has not sub-references!");
		
		// Resolve the group according to the kind of the group
		NameReference firstRef = subreferences.get(0);
		NameReferenceKind firstRefKind = firstRef.getReferenceKind();

		for (NameReference reference : subreferences) reference.resolveBinding();
		// Bind the group to the type definition of the first reference (which is a type reference) of 
		// the array create expression
		if (firstRefKind == NameReferenceKind.NRK_TYPE){
			TypeReference typeRef = (TypeReference)firstRef;
			if (!typeRef.isResolved()) typeRef.resolveBinding();
			bindTo(typeRef.getDefinition());
		} else {
			throw new AssertionError("The first operand in array creation " + this.toFullString() + " is not a type!");
		}

		return isResolved();
	}
	
	/**
	 * The core reference of an array create expression is the core reference of its first expression (i.e. the type of 
	 * the creating array!
	 */
	public NameReference getCoreReference() {
		return subreferences.get(0).getCoreReference();
	}
}
