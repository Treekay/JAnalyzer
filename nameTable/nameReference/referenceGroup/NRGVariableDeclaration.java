package nameTable.nameReference.referenceGroup;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to variable declaration expression. 
 * <p> VariableDeclarationExpression: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
 * <p> VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGVariableDeclaration extends NameReferenceGroup {

	public NRGVariableDeclaration(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_VARIALBE_DECLARATION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The variable declaration reference group " + this.toFullString() + " has not sub-references!");
		
		for (NameReference reference : subreferences) reference.resolveBinding();
		TypeReference varTypeRef = (TypeReference)subreferences.get(0);
		bindTo(varTypeRef.getDefinition());

		return isResolved();
	}
	
	/**
	 * The core reference of a variable declaration expression is the first reference (i.e. the declared type) in the expression 
	 */
	public NameReference getCoreReference() {
		return subreferences.get(0).getCoreReference();
	}
}
