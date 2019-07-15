package analyzer.logic;

import java.util.ArrayList;
import java.util.List;

import analyzer.cfg.predicate.NodePredicateRecorder;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameReference.referenceGroup.NameReferenceGroupKind;
//import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018年7月24日
 * @version 1.0
 *
 */
public class NullCheckExpression extends RelationalExpression {

	/**
	 * IMPORTANT NOTE: An instance of NullCheckExpression should be constructed only when the give reference is a null check expression, 
	 * i.e. isNullCheckExpression(reference) == true!! In fact, we should use the static method extractNullCheckExpression() to get 
	 * null checkers from an expression. 
	 * @Precondition NullCheckExpression.isNullCheckExpression(reference) == true
	 */
	public NullCheckExpression(NameReference reference) {
		super(reference);
	}

	/**
	 * IMPORTANT NOTE: An instance of NullCheckExpression should be constructed only when the give reference is a null check expression, 
	 * i.e. isNullCheckExpression(reference) == true!! In fact, we should use the static method extractNullCheckExpression() to get 
	 * null checkers from an expression. 
	 * <p>If the give value is false, the negation of the given reference is constructed as a relational expression, i.e. the opposite 
	 * relation operator of the operator in the given reference will be used to construct the instance. 
	 * @Precondition NullCheckExpression.isNullCheckExpression(reference) == true
	 */
	public NullCheckExpression(NameReference reference, boolean value) {
		super(reference, value);
	}

	public boolean isNullChecker() {
		return true;
	}
	
	public NameReference getCheckedReference() {
		if (left.isNullReference()) return right;
		else return left;
	}
	
	/**
	 * If the expression is true, then the checked reference is null (this method return true) or not (this method return false). 
	 */
	public boolean getCheckResult() {
		if (operator.equals(NameReferenceGroup.OPERATOR_EQUALS)) return true;
		else return false;
	}
	
	public static boolean isNullCheckExpression(NameReference reference) {
		if (!reference.isResolved()) return false;
		NameDefinition definition = reference.getDefinition();
		if (!definition.getFullQualifiedName().equals(NameReferenceLabel.TYPE_BOOLEAN)) return false;
		if (!reference.isGroupReference()) return false;
		NameReferenceGroup group = (NameReferenceGroup)reference;
		NameReferenceGroupKind kind = group.getGroupKind();
		if (kind != NameReferenceGroupKind.NRGK_INFIX_EXPRESSION) return false;
		if (group.getOperator().equals(NameReferenceGroup.OPERATOR_NOT_EQUALS) || group.getOperator().equals(NameReferenceGroup.OPERATOR_EQUALS)) {
			NameReference right = group.getSubReferenceList().get(1);
			if (right.isNullReference()) return true;
			NameReference left = group.getSubReferenceList().get(0);
			if (left.isNullReference()) return true;
			// Here, we assume that (left.isNullReference() == false || right.isNullReference() == false) 
			return false;
		} else return false;
	}
	
	/**
	 * Extract null checkers from the given reference with the give value. Note that there are possibly many null checkers in 
	 * a reference (i.e. expression) 
	 */
	public static List<NullCheckExpression> extractNullCheckExpression(NameReference reference, boolean value) {
		List<NullCheckExpression> resultList = new ArrayList<NullCheckExpression>();
		if (!reference.resolveBinding()) return resultList;
		
		extractNullCheckExpression(resultList, reference, value, null);
		return resultList;
	}
	
	public static List<NullCheckExpression> extractNullCheckExpression(NodePredicateRecorder predicateRecorder, boolean value) {
		return extractNullCheckExpression(predicateRecorder.getPredicate(), value);
	}

	private static void extractNullCheckExpression(List<NullCheckExpression> resultList, NameReference reference, 
			boolean value, String firstOperator) {
		NameDefinition definition = reference.getDefinition();
		if (!definition.getFullQualifiedName().equals(NameReferenceLabel.TYPE_BOOLEAN)) return;
		if (!reference.isGroupReference()) return;
		NameReferenceGroup group = (NameReferenceGroup)reference;
		NameReferenceGroupKind kind = group.getGroupKind();
		if (kind != NameReferenceGroupKind.NRGK_INFIX_EXPRESSION) return;
		
		String operator = group.getOperator();
		
		if (operator.equals(NameReferenceGroup.OPERATOR_NOT_EQUALS) || operator.equals(NameReferenceGroup.OPERATOR_EQUALS)) {
			NameReference right = group.getSubReferenceList().get(1);
			NameReference left = group.getSubReferenceList().get(0);
			if (right.isNullReference() || left.isNullReference()) {
				NullCheckExpression nullChecker = new NullCheckExpression(reference, value);
				resultList.add(nullChecker);
			}
			return;
		} else if (operator.equals(NameReferenceGroup.OPERATOR_AND) || operator.equals(NameReferenceGroup.OPERATOR_OR)){
			if (firstOperator != null) {
				if (!operator.equals(firstOperator)) {
					// The expression (i.e. reference) has different logic operators (e.g. it includes both OR and AND, also
					// we do not consider the logical operator NOT in the expression!), we can not extract meaningful null checker
					// expressions in the give reference!
					resultList.clear();
					return;
				}
			} else firstOperator = operator;
			
			if ((firstOperator == NameReferenceGroup.OPERATOR_AND && value == true) || 
					(firstOperator == NameReferenceGroup.OPERATOR_OR && value == false)) {
				// If the expression is the form of (A && B && C) and the give value is true, we try to extract null checker
				// in its subexpressions, i.e A, B, C. Note that here if the value of the expression is true, we say that 
				// a null checker in those subexpressions is meaningful, but if its value is false, a null checker is not meaningful,
				// since A && B && C(true) implies A(true) and B(true) and C(true) etc. 
				// Or, the expression is the form of (A || B || C) and the give value is false, we also try to extract null checker
				// in its subexpressions. Note that (A || B || C)(false) implies A(false) and B(false) and C(false) etc.
				List<NameReference> sublist = group.getSubReferenceList();
				for (NameReference subreference : sublist) {
					extractNullCheckExpression(resultList, subreference, value, firstOperator);
				}
			}
		}
	}

}
