package nameTable.nameReference.referenceGroup;

import nameTable.nameReference.NameReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to array initializer expression. 
 * <p>		ArrayInitializer: { Expression, Expression, .. }
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGArrayInitializer extends NameReferenceGroup {

	public NRGArrayInitializer(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_ARRAY_INITIALIZER;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) return false;
		
		// Resolve the group according to the kind of the group
		NameReference firstRef = subreferences.get(0);

		for (NameReference reference : subreferences) reference.resolveBinding();
		// Bind the group to the binded definition of the first expression of 
		// the array initialize expressions
		firstRef = subreferences.get(0);
		bindTo(firstRef.getDefinition());

		return isResolved();
	}
	
	/**
	 * The core reference of an array initialize expression is the core reference of its first expression
	 */
	public NameReference getCoreReference() {
		if (subreferences == null) return this;
		return subreferences.get(0).getCoreReference();
	}
}
