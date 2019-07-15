package analyzer.cfg.predicate;

import java.util.List;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;

import analyzer.cfg.ExecutionPointWorkingList;
import graph.basic.GraphNode;
import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.ExecutionPointLabel;
import graph.cfg.ExecutionPointType;
import graph.cfg.creator.CFGCreator;
import nameTable.NameTableManager;
import nameTable.creator.NameReferenceCreator;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeLocation;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018年7月10日
 * @version 1.0
 *
 */
public class NodePredicateChainAnalyzer {

	public static ControlFlowGraph create(NameTableManager nameTable, MethodDefinition method) {
		CompilationUnitScope unitScope = nameTable.getEnclosingCompilationUnitScope(method);
		if (unitScope == null) return null;
		String sourceFileName = unitScope.getUnitName();
		CompilationUnit astRoot = nameTable.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(sourceFileName);
		if (astRoot == null) return null;
		CompilationUnitRecorder unitRecorder = new CompilationUnitRecorder(sourceFileName, astRoot);
		
		// Create a ControFlowGraph object
		ControlFlowGraph currentCFG = CFGCreator.create(nameTable, unitRecorder, method);
		if (currentCFG == null) return null;
		
		setNodePredicateChainRecorder(currentCFG);
		nodePredicateChainAnalysis(nameTable, unitRecorder, method, currentCFG);
		return currentCFG;
	}
	
	public static void setNodePredicateChainRecorder(ControlFlowGraph currentCFG) {
		List<GraphNode> nodeList = currentCFG.getAllNodes();
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				NodePredicateChainRecorder recorder = new NodePredicateChainRecorder();
				node.setFlowInfoRecorder(recorder);
			}
		}
	}
	
	public static void nodePredicateChainAnalysis(NameTableManager nameTable, CompilationUnitRecorder unitRecorder, MethodDefinition method, ControlFlowGraph currentCFG) {
		initializeNodePredicateChainForAllNodes(nameTable, unitRecorder, method, currentCFG);
		GraphNode startNode = currentCFG.getStartNode();
		if (startNode == null) return;
		ExecutionPointWorkingList workingList = new ExecutionPointWorkingList();
		if (!(startNode instanceof ExecutionPoint)) return; 
		List<GraphNode> adjacentFromNodeList = currentCFG.adjacentFromNode(startNode);
		for (GraphNode adjacentFromNode : adjacentFromNodeList) {
			if (!(adjacentFromNode instanceof ExecutionPoint)) continue;
			workingList.add((ExecutionPoint)adjacentFromNode);
		}
		
		while (!workingList.isEmpty()) {
			ExecutionPoint currentNode = workingList.removeFirst();
			INodePredicateChainRecorder currentRecorder = (INodePredicateChainRecorder)currentNode.getFlowInfoRecorder();
			SourceCodeLocation currentLocation = currentNode.getStartLocation();

//			Debug.println("Begin process current node (" + currentNode.getId() + ")");
			
			// Do disjunction operation with all predicate chain in the precede node
			NodePredicateListChain disjunctiveResult = null;
			List<GraphNode> adjacentToNodeList = currentCFG.adjacentToNode(currentNode);
			for (GraphNode adjacentToNode : adjacentToNodeList) {
				if (!(adjacentToNode instanceof ExecutionPoint)) continue;
				ExecutionPoint precedeNode = (ExecutionPoint)adjacentToNode;
				SourceCodeLocation precedeLocation = precedeNode.getStartLocation();
				// Precede node to current node is a loop back edge, if both them are not virtual nodes and the location of 
				// the current node is before the location of the precede node!
				if (currentLocation.compareTo(precedeLocation) <= 0 && currentNode.isPredicate()) continue;
				
				INodePredicateChainRecorder precedeRecorder = (INodePredicateChainRecorder)precedeNode.getFlowInfoRecorder();
				NodePredicateListChain precedeChain = precedeRecorder.getPredicateChain();
				NodePredicateRecorder precedePredicate = precedeRecorder.getTruePredicate(); 
				if (precedePredicate != null) {
					// This means that precede node is a predicate node!
					String edgeLabel = currentCFG.getEdgeLabel(precedeNode, currentNode);
					if (edgeLabel != null) {
						if (edgeLabel.equals(CFGEdge.LABEL_TRUE)) {
//							System.out.println("\tBefore do conjunction: [" + precedeChain + "], [" + precedePredicate + "]");
							precedeChain = precedeChain.conjunctionWith(precedePredicate);
//							System.out.println("\tAfter do conjunction");
						} else if (edgeLabel.equals(CFGEdge.LABEL_FALSE)) {
							precedePredicate = precedeRecorder.getFalsePredicate();
//							System.out.println("\tBefore do conjunction: [" + precedeChain + "], [" + precedePredicate + "]");
							precedeChain = precedeChain.conjunctionWith(precedePredicate);
//							System.out.println("\tAfter do conjunction");
						}
					}
				}
				

//				Debug.println("\tPrecede node (" + precedeNode.getId() + "), chain " + precedeChain);
				if (disjunctiveResult == null) disjunctiveResult = precedeChain;
				else {
//					System.out.println("\tBefore do disjunction: [" + disjunctiveResult + "], [" + precedeChain + "]");
					TreeSet<GraphNode> rangeNodeSet = NodePredicateListChain.getCommonCFGNodeList(precedeChain, disjunctiveResult);
					disjunctiveResult = disjunctiveResult.disjunctionWith(precedeChain, rangeNodeSet);
//					System.out.println("\tAfter do disjunction....");
				}
			}
			
			if (disjunctiveResult == null) continue;	// This means that this node has not precede nodes, i.e. it is the start node!
			NodePredicateListChain currentChain = currentRecorder.getPredicateChain();
			// If currentChain.equivalentTo(disjunctiveResult), the chain in the current node does not need to be modified
//			System.out.println("\tBefore test equivalence: [" + disjunctiveResult + "], [" + currentChain + "]");
			boolean isEquivalent = currentChain.equivalentTo(disjunctiveResult);
//			System.out.println("\tAfter test equivalence");
			if (isEquivalent) {
//				Debug.println("\tNO Change after process node (" + currentNode.getId() + ")");
//				Debug.println("\t\told chain " + currentChain);
//				Debug.println("\t\tnew chain " + disjunctiveResult);
				continue;
			}
			// Otherwise, the chain in the current node should be the disjunctive result of the chains in its precede nodes!
			currentRecorder.setPredicateChain(disjunctiveResult);
//			Debug.println("\tChange after process node (" + currentNode.getId() + ")");
//			Debug.println("\t\told chain " + currentChain);
//			Debug.println("\t\tnew chain " + disjunctiveResult);
			
			// And then, its succeed nodes may be modified in the latter iterations, so them should be added to the working list!
			adjacentFromNodeList = currentCFG.adjacentFromNode(currentNode);
			for (GraphNode adjacentFromNode : adjacentFromNodeList) {
				if (!(adjacentFromNode instanceof ExecutionPoint)) continue;
				ExecutionPoint succeedNode = (ExecutionPoint)adjacentFromNode;
				SourceCodeLocation succeedLocation = succeedNode.getStartLocation();
				if (succeedLocation.compareTo(currentLocation) <= 0 && succeedNode.isPredicate()) continue; 
//				System.out.println("\tAdd node " + adjacentFromNode.getId() + " to working list!");
				workingList.add((ExecutionPoint)adjacentFromNode);
//				Debug.println("\tAdd succeed node (" + adjacentFromNode.getId() + ")");
			}
//			printAllNodePredicatesForDebugging(currentCFG);
//			Debug.println("");
		}
	}
	
	
	public static void initializeNodePredicateChainForAllNodes(NameTableManager manager, CompilationUnitRecorder unitRecorder, MethodDefinition method, ControlFlowGraph currentCFG) {
		List<GraphNode> nodeList = currentCFG.getAllNodes();
		if (nodeList == null) return;
		
		NameReferenceCreator referenceCreator = new NameReferenceCreator(manager, true);
		for (GraphNode graphNode : nodeList) {
			if (!(graphNode instanceof ExecutionPoint)) continue;
			ExecutionPoint node = (ExecutionPoint)graphNode;
			INodePredicateChainRecorder recorder = (INodePredicateChainRecorder)node.getFlowInfoRecorder();
			if (node.isStart()) {
				NodePredicateListChain chain = new NodePredicateListChain();
				recorder.setPredicateChain(chain);
			} else {
				NodePredicateListChain chain = new NodePredicateListChain(NodePredicateList.FALSE_LIST);
				recorder.setPredicateChain(chain);
			}
			
			if (!node.isPredicate()) continue;
			String label = node.getLabel();
			if (label.equals(ExecutionPointLabel.SWITCH_PREDICATE)) continue;
			ExecutionPointType type = node.getType();
			Expression expression = null;
			if (label.equals(ExecutionPointLabel.ENHANCED_FOR_PREDICATE)) {
				EnhancedForStatement statement = (EnhancedForStatement)node.getAstNode();
				expression = statement.getExpression();
			} else expression = (Expression)node.getAstNode();
			if (expression == null) continue;
			NameReference predicate = referenceCreator.createReferenceForExpressionASTNode(unitRecorder.unitName, expression);
			// Since we do not create reference for literal, so if predicate == null, it means the branch or loop condition is a 
			// boolean literal, i.e. "true" (e.g forever loops), or "false" (in general, it can not be "false" for a reasonable software) 
			if (predicate == null) continue;
			
			if (type == ExecutionPointType.BRANCH_PREDICATE) {
				NodePredicateRecorder truePredicate = new NodePredicateRecorder(NodePredicateRecorder.BRANCH_PREDICATE, node, predicate, true);
				NodePredicateRecorder falsePredicate = new NodePredicateRecorder(NodePredicateRecorder.BRANCH_PREDICATE, node, predicate, false);
				recorder.setTruePredicate(truePredicate);
				recorder.setFalsePredicate(falsePredicate);
			} else if (type == ExecutionPointType.LOOP_PREDICATE) {
				NodePredicateRecorder truePredicate = new NodePredicateRecorder(NodePredicateRecorder.LOOP_PREDICATE, node, predicate, true);
				NodePredicateRecorder falsePredicate = new NodePredicateRecorder(NodePredicateRecorder.LOOP_PREDICATE, node, predicate, false);
				recorder.setTruePredicate(truePredicate);
				recorder.setFalsePredicate(falsePredicate);
			}
		}
	}
	
	public static void printAllNodePredicatesForDebugging(ControlFlowGraph currentCFG) {
		List<GraphNode> nodeList = currentCFG.getAllNodes();
		if (nodeList == null) return;
		Debug.println("ExecutionPointId\tPredicateChain");
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				INodePredicateChainRecorder recorder = (INodePredicateChainRecorder)node.getFlowInfoRecorder();
				Debug.println("(" + graphNode.getId() + ")\t" + recorder.getPredicateChain());
			}
		}
	}
}
