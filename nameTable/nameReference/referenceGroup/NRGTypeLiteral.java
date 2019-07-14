package nameTable.nameReference.referenceGroup;

import nameTable.nameReference.LiteralReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to type literal expression. 
 * <p>		TypeLiteral: ( Type | void ) . class
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGTypeLiteral extends NameReferenceGroup {

	public NRGTypeLiteral(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_TYPE_LITERAL;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The type literal reference group " + this.toFullString() + " has not sub-references!");
		
		for (NameReference reference : subreferences) reference.resolveBinding();
		// Bind to the type definition object represent Class type
		LiteralReference classLiteral = new LiteralReference(NameReferenceLabel.TYPE_CLASS, NameReferenceLabel.TYPE_CLASS, null, scope);
		classLiteral.resolveBinding();
		bindTo(classLiteral.getDefinition());

		return isResolved();
	}
	
	/**
	 * The core reference of a type literal expression is itself
	 */
	public NameReference getCoreReference() {
		return this;
	}
}
