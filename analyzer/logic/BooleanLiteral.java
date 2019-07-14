package analyzer.logic;

import nameTable.nameReference.LiteralReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ23ÈÕ
 * @version 1.0
 *
 */
public class BooleanLiteral extends AtomicBooleanExpression {
	protected LiteralReference expression = null;
	protected boolean value = false;
	
	public BooleanLiteral(LiteralReference reference) {
		expression = reference;
		if (reference.getName().equals(NameReferenceLabel.TYPE_BOOLEAN)) {
			if (reference.getLiteral().equals(NameReferenceLabel.KEYWORD_TRUE)) value = true;
		}
	}
	
	public NameReference getExpression() {
		return expression;
	}
	
	public MLogicValue getValue() {
		if (value == true) return MLogicValue.TRUE;
		else return MLogicValue.FALSE;
	}

	public boolean isLiteral() {
		return true;
	}
	
	public static boolean isBooleanLiteral(NameReference reference) {
		if (!reference.isLiteralReference()) return false;
		if (reference.getName().equals(NameReferenceLabel.TYPE_BOOLEAN)) return false;
		return true;
	}
}
