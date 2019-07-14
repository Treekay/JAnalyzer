package nameTable.nameReference.referenceGroup;

import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.LiteralReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to infix expression. 
 * <p>		Expression InfixOperator Expression { InfixOperator Expression }
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NRGInfixExpression extends NameReferenceGroup {

	public NRGInfixExpression(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_INFIX_EXPRESSION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences != null) {
			for (NameReference reference : subreferences) reference.resolveBinding();
		} else return false;
		
		NameReference firstRef = null;
		NameReference secondRef = null;
		if (subreferences.size() > 0) firstRef = subreferences.get(0);
		if (subreferences.size() > 1) secondRef = subreferences.get(1);
		
		if (isArithematicOperator()) {
			if (firstRef != null && secondRef != null) {
				TypeDefinition firstType = firstRef.getResultTypeDefinition();
				TypeDefinition secondType = secondRef.getResultTypeDefinition();
				if (firstType != null && secondType != null) {
					// If the type of one operand is String, then bind to the type String.
					if (firstType.getFullQualifiedName().equals("java.lang.String")) bindTo(firstType);
					else if (secondType.getFullQualifiedName().equals("java.lang.String")) bindTo(secondType);
					else if (firstType.isSubtypeOf(secondType)) bindTo(secondType);
					else bindTo(firstType);
				} else if (firstType != null) {
					bindTo(firstType);
				} else if (secondType != null) {
					bindTo(secondType);
				}
			} else if (firstRef != null) {
				TypeDefinition firstType = firstRef.getResultTypeDefinition();
				bindTo(firstType);
			}
		} else if (isShiftOperator()) {
			if (firstRef != null) {
				TypeDefinition firstType = firstRef.getResultTypeDefinition();
				bindTo(firstType);
			}
		} else {
			// All other operators are relation operators, bind to the type definition object represent boolean type
			LiteralReference booleanLiteral = new LiteralReference(NameReferenceLabel.TYPE_BOOLEAN, NameReferenceLabel.TYPE_BOOLEAN, null, scope);
			booleanLiteral.resolveBinding();
			bindTo(booleanLiteral.getDefinition());
		}

		return isResolved();
	}

	/**
	 * The core reference of an infix expression is the core reference of its first operand
	 */
	public NameReference getCoreReference() {
		if (subreferences == null) return this;
		else if (subreferences.size() <= 0) return this;
		else return subreferences.get(0).getCoreReference();
	}
}
