package analyzer.logic;

import java.util.List;

import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.LiteralReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameReference.referenceGroup.NameReferenceGroupKind;
//import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018年7月23日
 * @version 1.0
 *
 */
public class RelationalExpression extends AtomicBooleanExpression {
	protected NameReference expression = null;
	protected NameReference left = null;
	protected NameReference right = null;
	protected String operator = null;
	
	/**
	 * IMPORTANT NOTE: An instance of RelationExpression should be constructed only when the give reference is a relational expression, 
	 * i.e. isRelationalBooleanExpression(reference) == true!!
	 * @Precondition RelationalExpression.isRelationalBooleanExpression(reference) == true
	 */
	public RelationalExpression(NameReference reference) {
		expression = reference;
		NameReferenceGroup group = (NameReferenceGroup)reference;
		List<NameReference> sublist = group.getSubReferenceList();
		left = sublist.get(0);
		right = sublist.get(1);
		operator = group.getOperator();
	}

	/**
	 * IMPORTANT NOTE: An instance of RelationExpression should be constructed only when the give reference is a relational expression, 
	 * i.e. isRelationalBooleanExpression(reference) == true!!
	 * <p>If the give value is false, the negation of the given reference is constructed as a relational expression, i.e. the opposite 
	 * relation operator of the operator in the given reference will be used to construct the instance. 
	 * @Precondition RelationalExpression.isRelationalBooleanExpression(reference) == true
	 */
	public RelationalExpression(NameReference reference, boolean value) {
		expression = reference;
		NameReferenceGroup group = (NameReferenceGroup)reference;
		List<NameReference> sublist = group.getSubReferenceList();
		left = sublist.get(0);
		right = sublist.get(1);
		operator = group.getOperator();
		if (value == false) {
			if (operator.equals(NameReferenceGroup.OPERATOR_EQUALS)) operator = NameReferenceGroup.OPERATOR_NOT_EQUALS;
			else if (operator.equals(NameReferenceGroup.OPERATOR_LESS)) operator = NameReferenceGroup.OPERATOR_GREATER_EQUALS;
			else if (operator.equals(NameReferenceGroup.OPERATOR_LESS_EQUALS)) operator = NameReferenceGroup.OPERATOR_GREATER;
			else if (operator.equals(NameReferenceGroup.OPERATOR_GREATER_EQUALS)) operator = NameReferenceGroup.OPERATOR_LESS;
			else if (operator.equals(NameReferenceGroup.OPERATOR_GREATER)) operator = NameReferenceGroup.OPERATOR_LESS_EQUALS;
		}
	}

	public static boolean isRelationalBooleanExpression(NameReference reference) {
		if (!reference.isResolved()) {
			return false;
		}
		NameDefinition definition = reference.getDefinition();
		if (!definition.getFullQualifiedName().equals(NameReferenceLabel.TYPE_BOOLEAN)) {
			return false;
		}
		if (!reference.isGroupReference()) {
			return false;
		}
		NameReferenceGroup group = (NameReferenceGroup)reference;
		NameReferenceGroupKind kind = group.getGroupKind();
		if (kind != NameReferenceGroupKind.NRGK_INFIX_EXPRESSION) {
			return false;
		}
		if (group.isRelationalOperator()) return true;
		else {
			return false;
		}
	}


	@Override
	public NameReference getExpression() {
		return expression;
	}
	
	public boolean isNullChecker() {
		return false;
	}
	
	public boolean isConflictWith(RelationalExpression other) {
		if (operator == null || other.operator == null) return false;
		if (!left.isResolved() || !right.isResolved() || !other.left.isResolved() || !other.right.isResolved()) return false;
		if (!AtomicBooleanExpression.isSelectedExpression(left) || !AtomicBooleanExpression.isSelectedExpression(right)) return false;
		if (!AtomicBooleanExpression.isSelectedExpression(other.left) || !AtomicBooleanExpression.isSelectedExpression(other.right)) return false;

		if (right.isLiteralReference() && other.right.isLiteralReference()) {
			if (left.getCoreReference().getDefinition() != other.left.getCoreReference().getDefinition()) return false;
			return isLiteralConflict(operator, (LiteralReference)right, other.operator, (LiteralReference)other.right);
		} else if (right.isLiteralReference() && other.left.isLiteralReference()) {
			if (right.getCoreReference().getDefinition() != other.left.getCoreReference().getDefinition()) return false;
			String otherOperator = other.operator;
			if (otherOperator.equals(NameReferenceGroup.OPERATOR_GREATER)) otherOperator = NameReferenceGroup.OPERATOR_LESS_EQUALS;
			if (otherOperator.equals(NameReferenceGroup.OPERATOR_LESS)) otherOperator = NameReferenceGroup.OPERATOR_GREATER_EQUALS;
			if (otherOperator.equals(NameReferenceGroup.OPERATOR_GREATER_EQUALS)) otherOperator = NameReferenceGroup.OPERATOR_LESS;
			if (otherOperator.equals(NameReferenceGroup.OPERATOR_LESS_EQUALS)) otherOperator = NameReferenceGroup.OPERATOR_GREATER;
			return isLiteralConflict(operator, (LiteralReference)right, otherOperator, (LiteralReference)other.left);
		} else if (left.isLiteralReference() && other.right.isLiteralReference()) {
			if (right.getCoreReference().getDefinition() != other.left.getCoreReference().getDefinition()) return false;
			String thisOperator = operator;
			if (thisOperator.equals(NameReferenceGroup.OPERATOR_GREATER)) thisOperator = NameReferenceGroup.OPERATOR_LESS_EQUALS;
			if (thisOperator.equals(NameReferenceGroup.OPERATOR_LESS)) thisOperator = NameReferenceGroup.OPERATOR_GREATER_EQUALS;
			if (thisOperator.equals(NameReferenceGroup.OPERATOR_GREATER_EQUALS)) thisOperator = NameReferenceGroup.OPERATOR_LESS;
			if (thisOperator.equals(NameReferenceGroup.OPERATOR_LESS_EQUALS)) thisOperator = NameReferenceGroup.OPERATOR_GREATER;
			return isLiteralConflict(thisOperator, (LiteralReference)left, other.operator, (LiteralReference)other.right);
		} else if (left.isLiteralReference() && other.left.isLiteralReference()) {
			if (right.getCoreReference().getDefinition() != other.left.getCoreReference().getDefinition()) return false;
			String otherOperator = other.operator;
			if (otherOperator.equals(NameReferenceGroup.OPERATOR_GREATER)) otherOperator = NameReferenceGroup.OPERATOR_LESS_EQUALS;
			if (otherOperator.equals(NameReferenceGroup.OPERATOR_LESS)) otherOperator = NameReferenceGroup.OPERATOR_GREATER_EQUALS;
			if (otherOperator.equals(NameReferenceGroup.OPERATOR_GREATER_EQUALS)) otherOperator = NameReferenceGroup.OPERATOR_LESS;
			if (otherOperator.equals(NameReferenceGroup.OPERATOR_LESS_EQUALS)) otherOperator = NameReferenceGroup.OPERATOR_GREATER;
			String thisOperator = operator;
			if (thisOperator.equals(NameReferenceGroup.OPERATOR_GREATER)) thisOperator = NameReferenceGroup.OPERATOR_LESS_EQUALS;
			if (thisOperator.equals(NameReferenceGroup.OPERATOR_LESS)) thisOperator = NameReferenceGroup.OPERATOR_GREATER_EQUALS;
			if (thisOperator.equals(NameReferenceGroup.OPERATOR_GREATER_EQUALS)) thisOperator = NameReferenceGroup.OPERATOR_LESS;
			if (thisOperator.equals(NameReferenceGroup.OPERATOR_LESS_EQUALS)) thisOperator = NameReferenceGroup.OPERATOR_GREATER;
			return isLiteralConflict(thisOperator, (LiteralReference)left, otherOperator, (LiteralReference)other.left);
		} 
		NameDefinition thisLeftDefinition = left.getCoreReference().getDefinition();
		NameDefinition thisRightDefinition = right.getCoreReference().getDefinition();
		NameDefinition otherLeftDefinition = other.left.getCoreReference().getDefinition();
		NameDefinition otherRightDefinition = other.right.getCoreReference().getDefinition();
		
		if (thisLeftDefinition == otherLeftDefinition) {
			if (thisRightDefinition == otherRightDefinition) {
				if (!operator.equals(other.operator)) return true;
				else return false;
			} else {
				if (operator.equals(NameReferenceGroup.OPERATOR_EQUALS) && other.operator.equals(NameReferenceGroup.OPERATOR_EQUALS)) return true;
				else return false;
			}
		} else if (thisLeftDefinition == otherRightDefinition) {
			if (thisRightDefinition == otherLeftDefinition) {
				if (!operator.equals(other.operator)) return true;
				else return false;
			} else {
				if (operator.equals(NameReferenceGroup.OPERATOR_EQUALS) && other.operator.equals(NameReferenceGroup.OPERATOR_EQUALS)) return true;
				else return false;
			}
		} return false;
	}
	
	private boolean isLiteralConflict(String thisOperator, LiteralReference thisLiteral, String otherOperator, LiteralReference otherLiteral) {
		if ((!thisOperator.equals(NameReferenceGroup.OPERATOR_EQUALS) && !thisOperator.equals(NameReferenceGroup.OPERATOR_NOT_EQUALS)) ||
				(!otherOperator.equals(NameReferenceGroup.OPERATOR_EQUALS) && !otherOperator.equals(NameReferenceGroup.OPERATOR_NOT_EQUALS))) {
			int compareResult = compareLiteral(thisLiteral, otherLiteral);
			if (compareResult == 0) {
				if (thisOperator.equals(otherOperator)) return false;
				if (thisOperator.equals(NameReferenceGroup.OPERATOR_GREATER) && otherOperator.equals(NameReferenceGroup.OPERATOR_GREATER_EQUALS)) return false;
				if (otherOperator.equals(NameReferenceGroup.OPERATOR_GREATER) && thisOperator.equals(NameReferenceGroup.OPERATOR_GREATER_EQUALS)) return false;
				if (thisOperator.equals(NameReferenceGroup.OPERATOR_LESS) && otherOperator.equals(NameReferenceGroup.OPERATOR_LESS_EQUALS)) return false;
				if (otherOperator.equals(NameReferenceGroup.OPERATOR_LESS) && thisOperator.equals(NameReferenceGroup.OPERATOR_LESS_EQUALS)) return false;
				return true;
			} else if (compareResult < 0) {
				if ((thisOperator.equals(NameReferenceGroup.OPERATOR_LESS) || thisOperator.equals(NameReferenceGroup.OPERATOR_LESS_EQUALS) || thisOperator.equals(NameReferenceGroup.OPERATOR_EQUALS)) && 
						(otherOperator.equals(NameReferenceGroup.OPERATOR_GREATER) || otherOperator.equals(NameReferenceGroup.OPERATOR_GREATER_EQUALS) || otherOperator.equals(NameReferenceGroup.OPERATOR_EQUALS))) return true;
				return false;
			} else {
				if ((otherOperator.equals(NameReferenceGroup.OPERATOR_LESS) || otherOperator.equals(NameReferenceGroup.OPERATOR_LESS_EQUALS) || otherOperator.equals(NameReferenceGroup.OPERATOR_EQUALS)) && 
						(thisOperator.equals(NameReferenceGroup.OPERATOR_GREATER) || thisOperator.equals(NameReferenceGroup.OPERATOR_GREATER_EQUALS) || thisOperator.equals(NameReferenceGroup.OPERATOR_EQUALS))) return true;
				return false;
			}
		} else {
			if (thisLiteral.getLiteral().equals(otherLiteral.getLiteral())) {
				if (!thisOperator.equals(otherOperator)) return true;
			} else {
				if (thisOperator.equals(NameReferenceGroup.OPERATOR_EQUALS) && otherOperator.equals(NameReferenceGroup.OPERATOR_EQUALS)) return true;
			}
		}
		return false;
	}
	
	private int compareLiteral(LiteralReference thisLiteral, LiteralReference otherLiteral) {
		String thisType = thisLiteral.getName();
		if (thisType.equals(NameReferenceLabel.TYPE_BYTE) || thisType.equals(NameReferenceLabel.TYPE_INT)) {
			int thisValue = Integer.parseInt(thisLiteral.getLiteral());
			String otherType = otherLiteral.getName();
			if (otherType.equals(NameReferenceLabel.TYPE_BYTE) || thisType.equals(NameReferenceLabel.TYPE_INT)) {
				int otherValue = Integer.parseInt(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_LONG)) {
				long otherValue = Long.parseLong(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_FLOAT)) {
				float otherValue = Float.parseFloat(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_DOUBLE)) {
				double otherValue = Double.parseDouble(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else {
				throw new AssertionError("Illegal literal type: " + otherType + " in compare two literals");
			}
		} else if (thisType.equals(NameReferenceLabel.TYPE_LONG)) {
			long thisValue = Long.parseLong(thisLiteral.getLiteral());
			String otherType = otherLiteral.getName();
			if (otherType.equals(NameReferenceLabel.TYPE_BYTE) || thisType.equals(NameReferenceLabel.TYPE_INT)) {
				int otherValue = Integer.parseInt(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_LONG)) {
				long otherValue = Long.parseLong(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_FLOAT)) {
				float otherValue = Float.parseFloat(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_DOUBLE)) {
				double otherValue = Double.parseDouble(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else {
				throw new AssertionError("Illegal literal type: " + otherType + " in compare two literals");
			}
		} else if (thisType.equals(NameReferenceLabel.TYPE_FLOAT)) {
			float thisValue = Float.parseFloat(thisLiteral.getLiteral());
			String otherType = otherLiteral.getName();
			if (otherType.equals(NameReferenceLabel.TYPE_BYTE) || thisType.equals(NameReferenceLabel.TYPE_INT)) {
				int otherValue = Integer.parseInt(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_LONG)) {
				long otherValue = Long.parseLong(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_FLOAT)) {
				float otherValue = Float.parseFloat(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_DOUBLE)) {
				double otherValue = Double.parseDouble(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else {
				throw new AssertionError("Illegal literal type: " + otherType + " in compare two literals");
			}
		} else if (thisType.equals(NameReferenceLabel.TYPE_DOUBLE)) {
			double thisValue = Double.parseDouble(thisLiteral.getLiteral());
			String otherType = otherLiteral.getName();
			if (otherType.equals(NameReferenceLabel.TYPE_BYTE) || thisType.equals(NameReferenceLabel.TYPE_INT)) {
				int otherValue = Integer.parseInt(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_LONG)) {
				long otherValue = Long.parseLong(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_FLOAT)) {
				float otherValue = Float.parseFloat(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else if (otherType.equals(NameReferenceLabel.TYPE_DOUBLE)) {
				double otherValue = Double.parseDouble(otherLiteral.getLiteral());
				if (thisValue == otherValue) return 0;
				else if (thisValue < otherValue) return -1;
				else return 1;
			} else {
				throw new AssertionError("Illegal literal type: " + otherType + " in compare two literals");
			}
		} else {
			throw new AssertionError("Illegal literal type: " + thisType + " in compare two literals");
		}
	}
}
