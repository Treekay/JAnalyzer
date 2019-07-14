package nameTable.nameReference.referenceGroup;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to this expression. 
 * <p>		ThisExpression : [ClassName.] this
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGThisExpression extends NameReferenceGroup {

	public NRGThisExpression(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_THIS_EXPRESSION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The array access reference group " + this.toFullString() + " has not sub-references!");
		
		for (NameReference reference : subreferences) reference.resolveBinding();
		// If this expression is in the group, then the first reference is a type reference, 
		// we should bind the group to the type definition of this type reference
		TypeReference typeRef = (TypeReference)subreferences.get(0);
		bindTo(typeRef.getDefinition());
		// We have bind the group to the type definition object of the first reference

		return isResolved();
	}
	
	/**
	 * The core reference of a this expression is the first reference in the expression
	 */
	public NameReference getCoreReference() {
		return subreferences.get(0).getCoreReference();
	}
}
