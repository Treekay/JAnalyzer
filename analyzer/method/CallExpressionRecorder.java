package analyzer.method;

import java.util.List;

import analyzer.storageModel.IAbstractStorageModel;
import graph.cfg.ExecutionPoint;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameReference.MethodReference;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ26ÈÕ
 * @version 1.0
 *
 */
public class CallExpressionRecorder {
	protected ExecutionPoint node = null;   				// node may be null, if the method calling is in a field initializer
	protected String expression = null;						// expression should always not be null 
	protected MethodReference reference = null;				// reference should always not be null
	protected List<MethodDefinition> calleeList = null;		// calleeList may be null, if the reference can not be resolved!
	protected IAbstractStorageModel leftValue = null;		// leftValue may be null, if the method return value is not received by a variable
}
