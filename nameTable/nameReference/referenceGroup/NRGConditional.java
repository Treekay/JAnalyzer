package nameTable.nameReference.referenceGroup;

import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to conditional expression. 
 * <p>		ConditionalExpression: Expression ? Expression : Expression
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGConditional extends NameReferenceGroup {

	public NRGConditional(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_CONDITIONAL;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The conditional reference group " + this.toFullString() + " has not sub-references!");
		
		for (NameReference reference : subreferences) reference.resolveBinding();
		TypeDefinition secondType = null;
		TypeDefinition thirdType = null;
		if (subreferences.size() > 1) {
			NameReference secondRef = subreferences.get(1);
			secondType = secondRef.getResultTypeDefinition();
			
		}
		if (subreferences.size() > 2) {
			NameReference thirdRef = subreferences.get(2);
			thirdType = thirdRef.getResultTypeDefinition();
		}
		if (secondType != null && thirdType != null) {
			if (secondType.isSubtypeOf(thirdType)) bindTo(thirdType);
			else bindTo(secondType);
		} else if (secondType != null) bindTo(secondType);
		else if (thirdType != null) bindTo(thirdType);

		return isResolved();
	}
	
	/**
	 * The core reference of a conditional expression is the first value reference in the expression
	 */
	public NameReference getCoreReference() {
		return subreferences.get(1).getCoreReference();
	}
}
