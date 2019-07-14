package analyzer.cfg.reachDefinition;

import analyzer.cfg.predicate.NodePredicateListChain;
import analyzer.storageModel.IAbstractStorageModel;
import graph.cfg.CFGNode;
import nameTable.nameReference.NameReference;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ21ÈÕ
 * @version 1.0
 *
 */
public class GeneratedConditionDefinitionRecorder {
	protected NodePredicateListChain conditionChain = null;
	protected DefinitionRecorder definition = null;
	protected IAbstractStorageModel rightModel = null;
	protected boolean newDefinitionFlag = true;

	public GeneratedConditionDefinitionRecorder(DefinitionRecorder recorder, NodePredicateListChain chain) {
		definition = recorder;
		conditionChain = chain;
	}

	public GeneratedConditionDefinitionRecorder(DefinitionRecorder recorder, IAbstractStorageModel right, NodePredicateListChain chain) {
		definition = recorder;
		rightModel = right;
		conditionChain = chain;
	}
	
	public DefinitionRecorder getGeneratedDefinition() {
		return definition;
	}
	
	public NodePredicateListChain getConditionChain() {
		return conditionChain;
	}
	
	public IAbstractStorageModel getRightValueModel() {
		return rightModel;
	}
	
	public IAbstractStorageModel getLeftModel() {
		return definition.leftStorage;
	}
	
	public NameReference getRightValueExpression() {
		return definition.valueExpression;
	}
	
	public CFGNode getNode() {
		return definition.node;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[" + definition);
		builder.append("]{" + conditionChain + "}");
		return builder.toString();
	}
}
