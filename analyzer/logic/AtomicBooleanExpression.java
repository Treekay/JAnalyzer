package analyzer.logic;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameReference.referenceGroup.NameReferenceGroupKind;

public abstract class AtomicBooleanExpression {
	public boolean isLiteral() {
		return false;
	}
	public boolean isSingle() {
		return false;
	}
	public boolean isRelational() {
		return false;
	}
	
	public abstract NameReference getExpression();
	
	public MLogicValue getValue() {
		return MLogicValue.UNKNOWN;
	}

	public String toString() {
		if (getExpression() != null) return getExpression().toSimpleString();
		else return "null";
	}
	
	protected static boolean isSelectedExpression(NameReference reference) {
		if (reference.isValueReference() || reference.isLiteralReference()) return true;
		if (!reference.isGroupReference()) return false;
		NameReferenceGroup group = (NameReferenceGroup)reference;
		NameReferenceGroupKind kind = group.getGroupKind();
		if (kind == NameReferenceGroupKind.NRGK_FIELD_ACCESS || kind == NameReferenceGroupKind.NRGK_QUALIFIED_NAME ||
				kind == NameReferenceGroupKind.NRGK_SUPER_FIELD_ACCESS) return true;
		return false;
	}
	
}
