package nameTable.nameReference.referenceGroup;

import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to assignment expression. 
 * <p>		Assignment: Expression AssignmentOperator Expression
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGAssignment extends NameReferenceGroup {

	public NRGAssignment(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_ASSIGNMENT;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The assignment reference group " + this.toFullString() + " has not sub-references!");
		
		for (NameReference reference : subreferences) reference.resolveBinding();
		// Bind the group to the binded definition of the first expression of 
		// the assignment expressions
		NameReference firstRef = subreferences.get(0);
		TypeDefinition resultTypeDef = firstRef.getResultTypeDefinition();
		bindTo(resultTypeDef);

		return isResolved();
	}
	
	/**
	 * The core reference of an assignment expression is the core reference of its first expression (i.e. the left value of
	 * the assignment).
	 */
	public NameReference getCoreReference() {
		return subreferences.get(0).getCoreReference();
	}
}
