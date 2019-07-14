package analyzer.cfg.dominateNode;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.creator.CFGCreator;
import nameTable.NameTableManager;
import nameTable.nameDefinition.MethodDefinition;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ21ÈÕ
 * @version 1.0
 *
 */
public class DominatePredicateNodeAnalyzer {

	public static ControlFlowGraph create(NameTableManager nameTable, MethodDefinition method) {
		// Create a ControFlowGraph object
		ControlFlowGraph currentCFG = CFGCreator.create(nameTable, method);
		if (currentCFG == null) return null;

		setDominateNodeRecorder(currentCFG);
		dominatePredicateNodeAnalysis(currentCFG, method);
		return currentCFG;
	}
	
	public static void setDominateNodeRecorder(ControlFlowGraph currentCFG) {
		List<GraphNode> nodeList = currentCFG.getAllNodes();
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				DominateNodeRecorder recorder = new DominateNodeRecorder();
				node.setFlowInfoRecorder(recorder);
			}
		}
	}

	/**
	 * Get the list of nodes which dominate the given node.
	 * <p>Note: the given CFG graph must have been done dominate node analysis, and its nodes hold the information of 
	 * dominate nodes 
	 */
	public static List<GraphNode> getDominateNodeList(ControlFlowGraph graph, ExecutionPoint node) {
		IDominateNodeRecorder recorder = (IDominateNodeRecorder)node.getFlowInfoRecorder();
		return recorder.getDominateNodeList();
	}
	
	public static void dominatePredicateNodeAnalysis(ControlFlowGraph currentCFG, MethodDefinition method) {
		List<GraphNode> graphNodeList = currentCFG.getAllNodes();
		
		initializeDominatePredicatesInAllNodes(currentCFG);
		
		// Need an iterative process to deal with the dominate node in a loop
		boolean hasChanged = true;
		while (hasChanged) {
			hasChanged = false;
			
			for (GraphNode graphNode : graphNodeList) {
				if (graphNode instanceof ExecutionPoint) {
					ExecutionPoint currentNode = (ExecutionPoint)graphNode;
					if (currentNode.isStart()) continue;
					
					IDominateNodeRecorder currentRecorder = (IDominateNodeRecorder)currentNode.getFlowInfoRecorder();
					List<GraphNode> currentDominateNodeSet = currentRecorder.getDominateNodeList();
					List<GraphNode> newDominateNodeSet = null;
					
					List<GraphNode> adjacentToNodeList = currentCFG.adjacentToNode(currentNode);
					for (GraphNode adjacentToNode : adjacentToNodeList) {
						if (adjacentToNode instanceof ExecutionPoint) {
							ExecutionPoint precedeNode = (ExecutionPoint)adjacentToNode;
							IDominateNodeRecorder precedeRecorder = (IDominateNodeRecorder)precedeNode.getFlowInfoRecorder();
							List<GraphNode> precedeDominateNodeSet = precedeRecorder.getDominateNodeList();
							// newDominateNodeSet is assigned to be the intersection of newDominateNodeSet and precedeDominateNodeSet
							if (newDominateNodeSet == null) {
								newDominateNodeSet = new ArrayList<GraphNode>();
								newDominateNodeSet.addAll(precedeDominateNodeSet);
							} else newDominateNodeSet.retainAll(precedeDominateNodeSet);
						}
					}
					
					if (newDominateNodeSet == null) {
						Debug.println("\tThe adjacent to node list size " + adjacentToNodeList.size() + " for node [" + currentNode.getId() + "] " + currentNode.getDescription() + " in method " + method.getFullQualifiedName());
					} else {
						// The current node is always in the dominate node of the current node
						if (!newDominateNodeSet.contains(currentNode) && currentNode.isPredicate()) newDominateNodeSet.add(currentNode);
						if (currentDominateNodeSet.size() != newDominateNodeSet.size() || !currentDominateNodeSet.containsAll(newDominateNodeSet)) {
							hasChanged = true;
							currentRecorder.setDominateNodeList(newDominateNodeSet);
						}
					}
				}
			}
		}
	}
	
	static void initializeDominatePredicatesInAllNodes(ControlFlowGraph currentCFG) {
		ExecutionPoint startNode = (ExecutionPoint)currentCFG.getStartNode(); 
		IDominateNodeRecorder recorder = (IDominateNodeRecorder)startNode.getFlowInfoRecorder();
		
		// Add the startNode itself to its dominate node set!
		List<GraphNode> nodeSet = new ArrayList<GraphNode>();
		nodeSet.add(startNode);
		recorder.setDominateNodeList(nodeSet);
		
		// Initialize other node with all node as its dominate node set 
		List<GraphNode> nodeList = currentCFG.getAllNodes();
		List<GraphNode> predicateNodeList = new ArrayList<GraphNode>();
		for (GraphNode graphNode : nodeList) {
			ExecutionPoint node = (ExecutionPoint)graphNode;
			if (node.isPredicate()) predicateNodeList.add(node);
		}
		for (GraphNode graphNode : nodeList) {
			ExecutionPoint node = (ExecutionPoint)graphNode;
			if (node.isStart()) continue;
			
			recorder = (IDominateNodeRecorder)node.getFlowInfoRecorder();
			// We must create a new node set to record the dominate node set of graphNode, because it will be changed
			// in the iterative process
			nodeSet = new ArrayList<GraphNode>();
			// Add all node to this node set
			nodeSet.addAll(predicateNodeList);
			recorder.setDominateNodeList(nodeSet);
			node.setFlowInfoRecorder(recorder);
		}
	}
	
	public static void printDominateNodeInformation(ControlFlowGraph currentCFG, PrintWriter output) {
		List<GraphNode> nodeList = currentCFG.getAllNodes();
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				IDominateNodeRecorder recorder = (IDominateNodeRecorder)node.getFlowInfoRecorder();
				List<GraphNode> dominateNodeSet = recorder.getDominateNodeList();
				
				if (dominateNodeSet.size() <= 0) {
					output.println("[" + graphNode.getId() + "]\t" + getPrettyLine(graphNode.getDescription()) + "\t~~\t~~");
				} else {
					for (GraphNode dominateNode : dominateNodeSet) {
						output.println("[" + graphNode.getId() + "]\t" + getPrettyLine(graphNode.getDescription()) + "\t[" + dominateNode.getId() + "]\t" + getPrettyLine(dominateNode.getDescription()));
					}
				}
			} else {
				output.println(graphNode.getId() + "\t~~\t~~\t~~\t~~");
			}
		}
	}
	
	static void printNodeList(List<GraphNode> nodeList) {
		if (nodeList == null) {
			Debug.println(" null list");
			return;
		}
		for (GraphNode node : nodeList) Debug.print("[" + node.getId() + "]; ");
		Debug.println("");
	}
	
	static String getPrettyLine(String description) {
		int lineIndex = description.indexOf('\n');
		if (lineIndex < 0 || lineIndex > 64) lineIndex = 64;
		if (lineIndex > description.length()) return description;
		return description.substring(0, lineIndex);
	}
}
