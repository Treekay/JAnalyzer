package analyzer.method;

import java.util.LinkedList;
import java.util.List;

import analyzer.cfg.predicate.INodePredicateChainRecorder;
import analyzer.cfg.predicate.NodePredicateList;
import analyzer.cfg.predicate.NodePredicateListChain;
import analyzer.cfg.predicate.NodePredicateRecorder;
import analyzer.cfg.reachDefinition.ConditionDefinitionRecorder;
import analyzer.cfg.reachDefinition.DefinitionRecorder;
import analyzer.cfg.reachDefinition.ReachConditionDefinitionAnalyzer;
import analyzer.logic.MLogicValue;
import analyzer.logic.NullCheckExpression;
import analyzer.storageModel.IAbstractStorageModel;
import analyzer.storageModel.StorageModelFactory;
import graph.basic.GraphNode;
import graph.cfg.ExecutionPoint;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameReference.referenceGroup.NameReferenceGroupKind;
import sourceCodeAST.SourceCodeLocation;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ25ÈÕ
 * @version 1.0
 *
 */
public class ValueNullableRecorder {
	public static int REFERENCE_DISPLAY_LENGTH = 30;
	protected ConditionDefinitionRecorder assignment = null;
	protected MLogicValue nullable = MLogicValue.UNKNOWN;
	protected NullCheckExpression keyChecker = null;
	protected ExecutionPoint keyCheckNode = null;
	protected NameDefinition lastDefinition = null;			// The last definition of the value
	protected NameReference lastReference = null;			// The last assignment reference of the value
	// The last reference and definition may not be in the ConditionDefinitionRecorder assignment, when the last definition is
	// a field definition, we will look for its last assignment in constructor or field declaration!
	protected boolean lastInDefinitionRecorder = true;		

	public ValueNullableRecorder(NameReference reference, NameDefinition definition) {
		this.lastReference = reference;
		this.lastDefinition = definition;
	}
	
	public ValueNullableRecorder(ConditionDefinitionRecorder assignment) {
		this.assignment = assignment;
		if (assignment == null) return;

		DefinitionRecorder lastRecorder = assignment.getDefinitionList().getLast();
		lastReference = lastRecorder.getValueExpression();
		if (lastReference != null) {
			NameReference coreReference = lastReference.getCoreReference();
			if (coreReference != null) lastDefinition = coreReference.getDefinition();
		}
	}
	
	public NameReference getLastReference() {
		return lastReference;
	}
	
	public NameDefinition getLastDefinition() {
		return lastDefinition;
	}
	
	public void determineNullability() {
		if (assignment == null) return;
		
		LinkedList<DefinitionRecorder> definitionList = assignment.getDefinitionList();
		DefinitionRecorder firstRecorder = definitionList.getFirst();
		IAbstractStorageModel firstStorage = firstRecorder.getLeftStorage();
		if (firstStorage.isPrimitive()) {
			nullable = MLogicValue.FALSE;
			return;
		}
		
		for (DefinitionRecorder definitionRecorder : definitionList) {
			GraphNode node = definitionRecorder.getNode();
			if (node instanceof ExecutionPoint) {
				ExecutionPoint point = (ExecutionPoint)node;
//				Debug.println("Execution point: " + point.getId() + ", " + point.getType());
				if (point.isEnhancedForPredicate()) {
					nullable = MLogicValue.FALSE;
					return;
				}
			}
			NameReference reference = definitionRecorder.getValueExpression();
			if (reference == null) {
				nullable = MLogicValue.FALSE;		// This means the reach definition is a parameter!
				return;
			}
			if (reference.isGroupReference()) {
				NameReferenceGroup lastGroup = (NameReferenceGroup)reference;
				if (lastGroup.getGroupKind() == NameReferenceGroupKind.NRGK_ARRAY_CREATION || 
						lastGroup.getGroupKind() == NameReferenceGroupKind.NRGK_CLASS_INSTANCE_CREATION) {
					nullable = MLogicValue.FALSE;
					return;
				}
			}
		}
		
		DefinitionRecorder lastRecorder = definitionList.getLast();
		NameReference lastAssignment = lastRecorder.getValueExpression();
		
		NodePredicateListChain conditionChain = assignment.getConditionChain();
		
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
						IAbstractStorageModel definitionStorage = definitionRecorder.getLeftStorage();
						if (checkStorage.referToSameStorage(definitionStorage)) {
							ExecutionPoint checkNode = (ExecutionPoint)predicate.getNode();
							ExecutionPoint definitionNode = (ExecutionPoint)definitionRecorder.getNode();
							
							List<ConditionDefinitionRecorder> checkedRecorderList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(checkNode, checkStorage);
							List<ConditionDefinitionRecorder> definitionRecorderList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(definitionNode, definitionStorage);
//							Debug.println("\t\tChecked node " + checkNode.getId() + ", " + checkedRecorderList);
//							Debug.println("\t\tDefinition node " + definitionNode.getId() + ", " + definitionRecorderList);
							
							if (ConditionDefinitionRecorder.conditionDefinitionListContains(checkedRecorderList, definitionRecorderList)) {
								thisResult = MLogicValue.getMLogicValue(nullChecker.getCheckResult());
								keyChecker = nullChecker;
								keyCheckNode = checkNode;
//								Debug.println("\t\tCheck result " + thisResult);
								break;
							}
						}
					}
					if (thisResult != MLogicValue.UNKNOWN) break;
				}
			}
			if (thisResult == MLogicValue.UNKNOWN) break;
			if (lastResult == MLogicValue.UNKNOWN) lastResult = thisResult; 
			else if (lastResult != thisResult) break;
		}
		if (lastResult != MLogicValue.UNKNOWN) nullable = lastResult;
		else nullable = getNullability(lastAssignment);
	}
	
	public void determineLastReferenceNullabilityInNode(ExecutionPoint node) {
		if (lastReference == null) {
			nullable = MLogicValue.TRUE;
			return;
		}
		if (lastReference.isNullReference()) {
			nullable = MLogicValue.TRUE;
			return;
		}
		if (!lastReference.isResolved()) {
			nullable = MLogicValue.FALSE;
			return;
		}
		TypeReference type = lastReference.getResultTypeReference();
		if (type != null) {
			if (type.isReferToPrimitiveType()) {
				nullable = MLogicValue.FALSE;
				return;
			}
			TypeDefinition typeDefinition = (TypeDefinition)type.getDefinition();
			if (typeDefinition != null) {
				if (typeDefinition.isEnumType()) {
					nullable = MLogicValue.FALSE;
					return;
				}
			}
		}
		NameDefinition definition = lastReference.getDefinition();
		if (definition.isVariableDefinition()) {
			nullable = MLogicValue.FALSE;
			return;
		}
		
		if (lastReference.isGroupReference()) {
			NameReferenceGroup group = (NameReferenceGroup)lastReference;
			NameReferenceGroupKind kind = group.getGroupKind();
			if (kind == NameReferenceGroupKind.NRGK_ARRAY_ACCESS || kind == NameReferenceGroupKind.NRGK_ARRAY_CREATION ||
					kind == NameReferenceGroupKind.NRGK_ARRAY_INITIALIZER || kind == NameReferenceGroupKind.NRGK_CLASS_INSTANCE_CREATION ||
					kind == NameReferenceGroupKind.NRGK_INFIX_EXPRESSION) {
				nullable = MLogicValue.FALSE;
				return;
			}
		}

		if (!(node instanceof ExecutionPoint)) {
			nullable = MLogicValue.UNKNOWN;
			return;
		}
		ExecutionPoint point = (ExecutionPoint)node;
		
		IAbstractStorageModel definitionStorage = StorageModelFactory.extractLeftStorageModelInReference(lastReference);
		if (definitionStorage == null) {
			nullable = MLogicValue.UNKNOWN;
			return;
		}

		INodePredicateChainRecorder recorder = (INodePredicateChainRecorder)point.getFlowInfoRecorder();

		NodePredicateListChain conditionChain = recorder.getPredicateChain();
//		Debug.println("\tNode " + node.getId() + " condition chain: " + conditionChain);
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
					
					if (checkStorage.referToSameStorage(definitionStorage)) {
						ExecutionPoint checkNode = (ExecutionPoint)predicate.getNode();
						
						List<ConditionDefinitionRecorder> checkedRecorderList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(checkNode, checkStorage);
						List<ConditionDefinitionRecorder> definitionRecorderList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(node, definitionStorage);
//						Debug.println("\t\tChecked node " + checkNode.getId() + ", " + checkedRecorderList);
//						Debug.println("\t\tDefinition node " + definitionNode.getId() + ", " + definitionRecorderList);
						
						if (ConditionDefinitionRecorder.conditionDefinitionListContains(checkedRecorderList, definitionRecorderList)) {
							thisResult = MLogicValue.getMLogicValue(nullChecker.getCheckResult());
							keyChecker = nullChecker;
							keyCheckNode = checkNode;
//							Debug.println("\t\tCheck result " + thisResult);
							break;
						}
					}
					if (thisResult != MLogicValue.UNKNOWN) break;
				}
			}
			if (thisResult == MLogicValue.UNKNOWN) break;
			if (lastResult == MLogicValue.UNKNOWN) lastResult = thisResult; 
			else if (lastResult != thisResult) break;
		}
		nullable = lastResult;
	}
	
	public String getDefinitionListString(String currentFileName) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		List<DefinitionRecorder> definitionList = null;
		if (assignment != null) definitionList = assignment.getDefinitionList();
		if (definitionList != null) {
			for (DefinitionRecorder recorder : definitionList) {
				String valueString = "?";
				NameReference value = recorder.getValueExpression();
				if (value != null) {
					valueString = value.toSimpleString(REFERENCE_DISPLAY_LENGTH);
					SourceCodeLocation location = value.getLocation();
					if (location.getFileUnitName().equals(currentFileName)) valueString += "(" + location + ")";
					else valueString += "(" + location.getUniqueId() + ")";
				}
				if (first) {
					IAbstractStorageModel storage = recorder.getLeftStorage();
					builder.append(storage.getExpression() + "<--" + valueString);
					first = false;
				} else builder.append("<--" + valueString);
			}
		}
		if (lastInDefinitionRecorder == false) {
			String valueString = "?";
			if (lastReference != null) {
				valueString = lastReference.toSimpleString(REFERENCE_DISPLAY_LENGTH);
				SourceCodeLocation location = lastReference.getLocation();
				if (location.getFileUnitName().equals(currentFileName)) valueString += "(" + location + ")";
				else valueString += "(" + location.getUniqueId() + ")";
			}
			if (first) {
				builder.append(valueString);
				first = false;
			} else builder.append("<--" + valueString);
		}
		if (lastDefinition != null) {
			if (lastDefinition.isMethodDefinition()) {
				MethodDefinition callee = (MethodDefinition)lastDefinition;
				SourceCodeLocation location = callee.getLocation();
				if (location.getFileUnitName().equals(currentFileName)) {
					builder.append("[Call " + callee.getSimpleName() + "()@" + location + "]");
				} else {
					builder.append("[Call " + callee.getSimpleName() + "()@" + location.getUniqueId() + "]");
				}
			}
		}
		
		return builder.toString();
	}
	
	public String getConditionChainString() {
		if (assignment == null) return "NONE";
		NodePredicateListChain conditionChain = assignment.getConditionChain();
		if (conditionChain == null) return "NONE";
		else return conditionChain.toString();
	}
	
	public String getNullabilityString() {
		String result = "" + nullable;
		if (keyChecker != null) result += "[Checker: (" + keyCheckNode.getId() + ")" + keyChecker + "]";
		return result;
	}
	
	public static MLogicValue getNullability(NameReference reference) {
		if (reference == null) return MLogicValue.TRUE;
		if (reference.isNullReference()) return MLogicValue.TRUE;
		if (!reference.isResolved()) return MLogicValue.FALSE;
		TypeReference type = reference.getResultTypeReference();
		if (type != null) {
			if (type.isReferToPrimitiveType()) return MLogicValue.FALSE;
			TypeDefinition typeDefinition = (TypeDefinition)type.getDefinition();
			if (typeDefinition.isEnumType()) return MLogicValue.FALSE;
		}
		NameDefinition definition = reference.getDefinition();
		if (definition.isVariableDefinition()) return MLogicValue.FALSE;
		
		if (!reference.isGroupReference()) return MLogicValue.UNKNOWN;
		NameReferenceGroup group = (NameReferenceGroup)reference;
		NameReferenceGroupKind kind = group.getGroupKind();
		if (kind == NameReferenceGroupKind.NRGK_ARRAY_ACCESS || kind == NameReferenceGroupKind.NRGK_ARRAY_CREATION ||
				kind == NameReferenceGroupKind.NRGK_ARRAY_INITIALIZER || kind == NameReferenceGroupKind.NRGK_CLASS_INSTANCE_CREATION ||
				kind == NameReferenceGroupKind.NRGK_INFIX_EXPRESSION) return MLogicValue.FALSE;
		return MLogicValue.UNKNOWN;
	}
	
}
