package analyzer.cfg.reachDefinition;

import analyzer.storageModel.IAbstractStorageModel;
import graph.cfg.ExecutionPoint;
import nameTable.nameReference.NameReference;

/**
 * @author Zhou Xiaocong
 * @since 2018年4月10日
 * @version 1.0
 *
 */
public class DefinitionRecorder {
	// node can not be null, since all definition must be in a node!
	protected ExecutionPoint node = null;		
	// leftStorage can not be null, since all definition must define a left value!
	protected IAbstractStorageModel leftStorage = null;
	// valueExpression may be null, if the leftStorage has not been initialized yet!
	protected NameReference valueExpression = null;

	public DefinitionRecorder(ExecutionPoint node, IAbstractStorageModel leftStorage, NameReference valueExpression) {
		this.node = node;
		this.leftStorage = leftStorage;
		this.valueExpression = valueExpression;
	}

	public IAbstractStorageModel getLeftStorage() {
		return leftStorage;
	}

	public NameReference getValueExpression() {
		return valueExpression;
	}

	public ExecutionPoint getNode() {
		return node;
	}
	
	/**
	 * A node is in a CFG, but we may use the reach name definition information
	 * after release the CFG. This method provide a way to release memory occupation of nodes in the CFG. 
	 * 
	 * <p>Note: If we clear the information of node, we cannot recover it.  
	 */
	public void clearNode() {
		node = null;
	}
	
	@Override
	public String toString() {
		if (valueExpression != null) {
			return "<(" + node.getId() + "), " + leftStorage.getExpression() + " = " + valueExpression.toSimpleString() + ">"; 
		} else {
			return "<(" + node.getId() + "), " + leftStorage.getExpression() + " = ?>"; 
		}
	}
}
