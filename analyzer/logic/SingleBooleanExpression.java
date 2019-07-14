package analyzer.logic;

import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameReference.referenceGroup.NameReferenceGroupKind;

public class SingleBooleanExpression extends AtomicBooleanExpression {
	protected NameReference expression = null;
	
	public SingleBooleanExpression(NameReference reference) {
		expression = reference;
	}
	
	public NameReference getExpression() {
		return expression;
	}
	
	public boolean isSingle() {
		return true;
	}
	
	public static boolean isSingleBooleanExpression(NameReference reference) {
		if (!reference.isResolved()) return false;
		NameDefinition definition = reference.getDefinition();
		if (!definition.getFullQualifiedName().equals(NameReferenceLabel.TYPE_BOOLEAN)) return false;
		if (!reference.isGroupReference()) return true;
		NameReferenceGroup group = (NameReferenceGroup)reference;
		NameReferenceGroupKind kind = group.getGroupKind();
		if (kind != NameReferenceGroupKind.NRGK_INFIX_EXPRESSION || kind != NameReferenceGroupKind.NRGK_PREFIX_EXPRESSION) return true;
		if (group.isLogicOperator() || group.isRelationalOperator()) return false;
		return true;
	}
}
