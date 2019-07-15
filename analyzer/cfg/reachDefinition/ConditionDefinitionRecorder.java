package analyzer.cfg.reachDefinition;

import java.util.LinkedList;
import java.util.List;

import analyzer.cfg.predicate.NodePredicateList;
import analyzer.cfg.predicate.NodePredicateListChain;
import analyzer.cfg.predicate.NodePredicateRecorder;
import analyzer.logic.MLogicValue;
import analyzer.logic.NullCheckExpression;
import analyzer.logic.RelationalExpression;
import analyzer.storageModel.IAbstractStorageModel;
import analyzer.storageModel.StorageModelFactory;
import graph.basic.GraphNode;
import graph.cfg.ExecutionPoint;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameReference.referenceGroup.NameReferenceGroupKind;
//import util.Debug;

//import util.Debug;


/**
 * @author Zhou Xiaocong
 * @since 2018年7月14日
 * @version 1.0
 *
 */
public class ConditionDefinitionRecorder {
	protected LinkedList<DefinitionRecorder> definitionList = null;
	protected NodePredicateListChain conditionChain = NodePredicateListChain.TRUE_CHAIN;
	protected boolean newDefinitionFlag = true;
	
	public ConditionDefinitionRecorder(DefinitionRecorder definition, NodePredicateListChain condition) {
		definitionList = new LinkedList<DefinitionRecorder>();
		definitionList.addFirst(definition);
		conditionChain = condition;
	}
	
	/**
	 * Construct a ConditionDefinitionRecorder use the give definitionList and conditionChain. We deeply copy  
	 * definitionList for the result recorder, since we will change the definition list. But we do not deeply 
	 * copy conditionChain for the result recorder, since one NodePredicateListChain is constructed, it can not
	 * be modified by any public methods of NodePredicateListChain, that is, every public operation (conjunction 
	 * or disjunction) on a NodePredicateListChain will produce a new NodePredicateListChain! 
	 */
	public ConditionDefinitionRecorder(LinkedList<DefinitionRecorder> definitionList, NodePredicateListChain conditionChain) {
		this.definitionList = new LinkedList<DefinitionRecorder>();
		// Deeply copy definitionList, but we need not to deeply copy DefinitionRecorder in the list, 
		// since a DefinitionRecorder can not be modified after it is constructed!
		this.definitionList.addAll(definitionList);
		// We do not deeply copy conditionChain, since a NodePredicateListChain can not be modifed by any 
		// public methods after it is constructed!
		this.conditionChain = conditionChain;
	}
	
	public DefinitionRecorder getMainDefinition() {
		return definitionList.getFirst();
	}
	
	public LinkedList<DefinitionRecorder> getDefinitionList() {
		return definitionList;
	}
	
	public NodePredicateListChain getConditionChain() {
		return conditionChain;
	}
	
	public boolean isConflictCondition() {
		List<NodePredicateList> predicateChain = conditionChain.getPredicateList();
		if (predicateChain.size() <= 0) return false;
		
		for (NodePredicateList predicateList : predicateChain) {
			boolean isConflict = false;
			List<NodePredicateRecorder> recorderList = predicateList.getPredicateRecorderList();
			for (NodePredicateRecorder thisRecorder : recorderList) {
				NameReference thisPredicate = thisRecorder.getPredicate();
				thisPredicate.resolveBinding();
//				Debug.println("\tThis recorder " + thisPredicate.toSimpleString());
				if (RelationalExpression.isRelationalBooleanExpression(thisPredicate)) {
//					Debug.println("\tThis recorder is relational expression!");
					for (NodePredicateRecorder otherRecorder : recorderList) {
						NameReference otherPredicate = otherRecorder.getPredicate();
						otherPredicate.resolveBinding();
//						Debug.println("\tOther recorder " + otherPredicate.toSimpleString());
						if (thisRecorder == otherRecorder) continue;
						if (RelationalExpression.isRelationalBooleanExpression(otherPredicate)) {
//							Debug.println("\tOther recorder is relational expression!");
							RelationalExpression thisExpression = new RelationalExpression(thisPredicate, thisRecorder.getValue());
							RelationalExpression otherExpression = new RelationalExpression(otherPredicate, otherRecorder.getValue());
							isConflict = thisExpression.isConflictWith(otherExpression);
//							Debug.println("\t\tThis expression " + thisExpression + "(" + thisRecorder.getValue() + "), other " + otherExpression + "(" + otherRecorder.getValue() +"), conflict " + isConflict);
							break;
						}
					}
				}
				if (isConflict == true) break;
			}
			if (!isConflict) return false;
		}
		return true;
	}
	
	public MLogicValue getNullability() {
		DefinitionRecorder firstRecorder = definitionList.getFirst();
		IAbstractStorageModel firstStorage = firstRecorder.leftStorage;
		if (firstStorage.isPrimitive()) return MLogicValue.FALSE;
		
		DefinitionRecorder lastRecorder = definitionList.getLast();
		NameReference lastAssignment = lastRecorder.valueExpression;
		if (lastAssignment == null) return MLogicValue.UNKNOWN;
		
		if (lastAssignment.isGroupReference()) {
			NameReferenceGroup lastGroup = (NameReferenceGroup)lastAssignment;
			if (lastGroup.getGroupKind() == NameReferenceGroupKind.NRGK_ARRAY_CREATION || 
					lastGroup.getGroupKind() == NameReferenceGroupKind.NRGK_CLASS_INSTANCE_CREATION) {
				return MLogicValue.FALSE;
			}
		}
		
		MLogicValue lastResult = MLogicValue.UNKNOWN;
		for (NodePredicateList predicateList : conditionChain.getPredicateList()) {
			MLogicValue thisResult = MLogicValue.UNKNOWN;
			for (NodePredicateRecorder predicate : predicateList.getPredicateRecorderList()) {
				List<NullCheckExpression> nullCheckerList = NullCheckExpression.extractNullCheckExpression(predicate.getPredicate(), predicate.getValue());
//				Debug.println("\tCheck list size : " + nullCheckerList.size() + " for reference " + predicate.getPredicate().toSimpleString());
				for (NullCheckExpression nullChecker : nullCheckerList) {
					NameReference checkedReference = nullChecker.getCheckedReference();
					IAbstractStorageModel checkStorage = StorageModelFactory.extractLeftStorageModelInReference(checkedReference);
//					Debug.println("\t\tCheck reference : " + checkedReference.toSimpleString() + ", storage " + checkStorage.getExpression());
					
					for (DefinitionRecorder definitionRecorder : definitionList) {
						IAbstractStorageModel definitionStorage = definitionRecorder.leftStorage;
						if (checkStorage.referToSameStorage(definitionStorage)) {
							ExecutionPoint checkNode = (ExecutionPoint)predicate.getNode();
							ExecutionPoint definitionNode = (ExecutionPoint)definitionRecorder.node;
							
							List<ConditionDefinitionRecorder> checkedRecorderList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(checkNode, checkStorage);
							List<ConditionDefinitionRecorder> definitionRecorderList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(definitionNode, definitionStorage);
//							Debug.println("\t\tChecked node " + checkNode.getId() + ", " + checkedRecorderList);
//							Debug.println("\t\tDefinition node " + definitionNode.getId() + ", " + definitionRecorderList);
							
							if (conditionDefinitionListContains(checkedRecorderList, definitionRecorderList)) {
								thisResult = MLogicValue.getMLogicValue(nullChecker.getCheckResult());
//								Debug.println("\t\tCheck result " + thisResult);
								break;
							}
						}
						
					}
					
					if (thisResult != MLogicValue.UNKNOWN) break;
				}
			}
			if (thisResult == MLogicValue.UNKNOWN) return MLogicValue.UNKNOWN;
			if (lastResult == MLogicValue.UNKNOWN) lastResult = thisResult; 
			else if (lastResult != thisResult) return MLogicValue.UNKNOWN;
			
		}
		return lastResult;
	}
	
	public boolean isPrimitiveDefinition() {
		DefinitionRecorder firstRecorder = definitionList.getFirst();
		IAbstractStorageModel firstStorage = firstRecorder.leftStorage;
		return firstStorage.isPrimitive();
	}
	
	@Override
	public String toString() {
		if (definitionList.size() <= 0) return "<>";
		StringBuilder builder = new StringBuilder();
		builder.append("[" + definitionList.get(0));
		for (int i = 1; i < definitionList.size(); i++) {
			builder.append("<-" + definitionList.get(i));
		}
		builder.append("]{" + conditionChain + "}");
		return builder.toString();
	}
	
	public static boolean conditionDefinitionListContains(List<ConditionDefinitionRecorder> superSetList, List<ConditionDefinitionRecorder> subSetList) {
		for (ConditionDefinitionRecorder subRecorder : subSetList) {
			boolean found = false;
			DefinitionRecorder definitionRecorder = subRecorder.getMainDefinition();
			for (ConditionDefinitionRecorder superRecorder : superSetList) {
				if (definitionRecorder == superRecorder.getMainDefinition()) {
					found = true;
					break;
				}
			}
			if (found == false) return false;
		}
		return true;
	}
}
