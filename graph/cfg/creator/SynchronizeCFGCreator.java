package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;

/**
 * Create CFG for synchronized statement
 * @author Zhou Xiaocong
 * @since 2012/12/29
 * @version 1.0
 *
 */
public class SynchronizeCFGCreator extends StatementCFGCreator {

	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodeLabel) {

		ExecutionPointFactory factory = currentCFG.getExecutionPointFactory();

		SynchronizedStatement synStatement = (SynchronizedStatement)astNode;
		// 1 Create a virtual start node for the statement, 
		ExecutionPoint startNode = factory.createVirtualStart(synStatement);
		currentCFG.addNode(startNode);
		
		// 2. For the possible precede node in the list precedeNodeList, if its reason type is PPR_SEQUENCE, then add edge <precedeNode, 
		//    startNode> to the current CFG, and remove the precede node from the list to form a new precedeNodeList.
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, startNode);	
		
		// 3 Create a precede node list synPrecedeNodeList, which only contains the node startNode, since the precede node of the synchronize 
		//   body is startNode
		List<PossiblePrecedeNode> synPrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
		synPrecedeNodeList.add(new PossiblePrecedeNode(startNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		
		// 4 Create CFG for the body of the synchronize statement, and get new precede node list synBodyPrecedeNodeList
		Block synBody = synStatement.getBody();
		StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(synBody);
		List<PossiblePrecedeNode> synBodyPrecedeNodeList = creator.create(currentCFG, synBody, synPrecedeNodeList, null);
		
		// 5 Create a virtual end node for the statement, 
		ExecutionPoint endNode = null;
		
		// 6 Traverse synBodyPrecedeNodeList, for each node synBodyPrecedeNode in the list, if it is a PPR_SEQUENCE node, add edge
		//   <synBodyPrecedeNode, endNode> to currentCFG, otherwise add it to precedeNodeList in the above 2
		for (PossiblePrecedeNode synBodyPrecedeNode : synBodyPrecedeNodeList) {
			PossiblePrecedeReasonType reason = synBodyPrecedeNode.getReason();
			String label = synBodyPrecedeNode.getLabel();
			if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE) {
				if (endNode == null) {
					endNode = factory.createVirtualEnd(synStatement);
					currentCFG.addNode(endNode);
				}
				currentCFG.addEdge(new CFGEdge(synBodyPrecedeNode.getNode(), endNode, label));
			} else precedeNodeList.add(synBodyPrecedeNode);
		}

		// 7 Add endNode to the precedeNodeList
		if (endNode != null) precedeNodeList.add(new PossiblePrecedeNode(endNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		return precedeNodeList;
	}

}
