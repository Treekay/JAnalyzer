package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Create CFG for do while statement
 * @author Zhou Xiaocong
 * @since 2012/12/29
 * @version 1.0
 *
 */
public class DoWhileCFGCreator extends StatementCFGCreator {

	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodeLabel) {

		ExecutionPointFactory factory = currentCFG.getExecutionPointFactory();
		// 1 Create a startNode for the entry of do statement, and add it to the currentCFG, then call 
		//   StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNodetraverse() to traverse the list precedeNodeList, and if the reason of 
		//   the precedeNode in the list is PPR_SEQUENCE, add an edge <precedeNode, startNode> to the current CFG, and remove precedeNode from the 
		//   list to form a new precedeNodeList
		DoStatement doStatement = (DoStatement)astNode;
		ExecutionPoint startNode = factory.createVirtualStart(doStatement);
		currentCFG.addNode(startNode);
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, startNode);
		
		// 2 Create a precede node list loopPrecedeNodeList, which only contains the node startNode, since the precede node of the loop body is startNode
		List<PossiblePrecedeNode> loopPrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
		loopPrecedeNodeList.add(new PossiblePrecedeNode(startNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		
		// 3 Create CFG for the body of the loop statement, and get new precede node list loopBodyPrecedeNode
		List<PossiblePrecedeNode> loopBodyPrecedeNodeList;
		Statement loopBody = doStatement.getBody();
		if (loopBody != null) {
			StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(loopBody);
			loopBodyPrecedeNodeList = creator.create(currentCFG, loopBody, loopPrecedeNodeList, null);
		} else {
			// if the body of the loop is null, then the precede node of the succeed node should still be startNode
			loopBodyPrecedeNodeList = loopPrecedeNodeList;		
		}
		
		// 4 Create node for the condition of the do statement, add it to currentCFG, and add edge <conditionNode, startNode> to currentCFG 
		ExecutionPoint conditionNode = factory.createPredicate(doStatement);
		currentCFG.addNode(conditionNode);
		currentCFG.addEdge(new CFGEdge(conditionNode, startNode, CFGEdge.LABEL_TRUE));
		
		// 5 Create a virtual end node endNode for the do statement, add it to currentCFG, and add edge <conditionNode, endNode> to currentCFG
		ExecutionPoint endNode = factory.createVirtualEnd(doStatement);
		currentCFG.addNode(endNode);
		currentCFG.addEdge(new CFGEdge(conditionNode, endNode, CFGEdge.LABEL_FALSE));
		
		// 6 Traverse loopBodyPrecedeNodeList, for each node loopBodyPrecedeNode in the list, if it is a PPR_SEQUENCE node, add edge
		//   <loopBodyPrecedeNode, conditionNode> to currentCFG, if it is a PPR_BREAK node without label, add edge <loopBodyPrecedeNode, endNode>
		//   to currentCFG, if it is a PPR_CONTINUE node without label, add edge <loopBodyPrecedeNode, conditionNode> to currentCFG, 
		//   otherwise add it to precedeNodeList in the above 1
		for (PossiblePrecedeNode loopBodyPrecedeNode : loopBodyPrecedeNodeList) {
			PossiblePrecedeReasonType reason = loopBodyPrecedeNode.getReason();
			String label = loopBodyPrecedeNode.getLabel();
			if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE) {
				currentCFG.addEdge(new CFGEdge(loopBodyPrecedeNode.getNode(), conditionNode, label));
			} else if (reason == PossiblePrecedeReasonType.PPR_BREAK && StatementCFGCreatorHelper.needAddEdgeByMatchLabel(label, nodeLabel)) {
				currentCFG.addEdge(new CFGEdge(loopBodyPrecedeNode.getNode(), endNode, null));
			} else if (reason == PossiblePrecedeReasonType.PPR_CONTINUE && StatementCFGCreatorHelper.needAddEdgeByMatchLabel(label, nodeLabel)) {
				currentCFG.addEdge(new CFGEdge(loopBodyPrecedeNode.getNode(), conditionNode, null));
			} else precedeNodeList.add(loopBodyPrecedeNode);
		}

		// 7 Add endNode to the precedeNodeList
		precedeNodeList.add(new PossiblePrecedeNode(endNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		return precedeNodeList;
	}

}
