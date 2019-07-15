package analyzer.method;

import java.util.List;

import analyzer.cfg.predicate.NodePredicateListChain;
import analyzer.logic.MLogicValue;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.NameReference;

/**
 * @author Zhou Xiaocong
 * @since 2018年7月25日
 * @version 1.0
 *
 */
public class ReturnValueRecorder {

	// The expression after the keyword "return"
	protected NameReference expression = null;		
	// The impossible value which the expression binded to. 
	// For statement "return variable", the value will be the definition of the variable (reference) binded to;
	// For statement "return methodCallExpression", the value with be the definition of the method definition.
	protected NameDefinition value = null;
	
	// The root assignment references of the return expression. Note that the node and the name in an instance of 
	// ReachNameDefinition in the rootAssignment probably is null, and also the reference may be null when the name
	// is a parameter!
	protected List<ValueNullableRecorder> valueRecorderList = null;
	
	protected NodePredicateListChain condition = null;
	
	boolean hasNullValue() {
		for (ValueNullableRecorder rootAssignment : valueRecorderList) {
			if (rootAssignment.nullable == MLogicValue.TRUE) return true;
		}
		return false;
	}

	/**
	 * If there is at least a MLogicValue.TRUE value of nullable in rootAssignment, then return MLogicValue.TRUE, 
	 * otherwise, there is at least a MLogicValue.UNKNOW value of nullable in rootAssignment, then return MLogicValue.UNKNOW,
	 * otherwise, i.e. there all MLogicValue.FALSE, return MLogicValue.FALSE
	 * 
	 */
	MLogicValue getNullability() {
		MLogicValue result = MLogicValue.FALSE;
		for (ValueNullableRecorder rootAssignment : valueRecorderList) {
			if (rootAssignment.nullable == MLogicValue.TRUE) return MLogicValue.TRUE;
			else if (rootAssignment.nullable == MLogicValue.UNKNOWN) result = MLogicValue.UNKNOWN;
		}
		return result;
	}

}
