package nameTable.nameReference.referenceGroup;

import nameTable.nameReference.LiteralReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to instanceof expression. 
 * <p>		InstanceofExpression: Expression instanceof Type
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGInstanceof extends NameReferenceGroup {

	public NRGInstanceof(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_INSTANCEOF;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The instanceof reference group " + this.toFullString() + " has not sub-references!");
		
		for (NameReference reference : subreferences) reference.resolveBinding();
		// Bind to the type definition object represent boolean type
		LiteralReference booleanLiteral = new LiteralReference(NameReferenceLabel.TYPE_BOOLEAN, NameReferenceLabel.TYPE_BOOLEAN, null, scope);
		booleanLiteral.resolveBinding();
		bindTo(booleanLiteral.getDefinition());

		return isResolved();
	}
	
	/**
	 * The core reference of an instance of expression is the first reference in the expression
	 */
	public NameReference getCoreReference() {
		return subreferences.get(0).getCoreReference();
	}
	
}
