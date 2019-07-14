package graph.cfg.creator;

import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.List;

import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Create CFG for return statement
 * @author Zhou Xiaocong
 * @since 2012/12/29
 * @version 1.0
 *
 */
public class ReturnCFGCreator extends StatementCFGCreator {

	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodeLabel) {

		// 1. Create an execution point for this return statement and add it to the current CFG
		ReturnStatement returnStatement = (ReturnStatement)astNode;
		ExecutionPoint returnNode = currentCFG.getExecutionPointFactory().createFlowControlNode(returnStatement);
		currentCFG.addNode(returnNode);
		
		// 2. For the possible precede node in the list precedeNodeList, if its reason type is PPR_SEQUENCE, then add edge <precedeNode, 
		//    returnNode> to the current CFG, and remove the precede node from the list to form a new precedeNodeList.
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, returnNode);	
		
		// 3. Add the returnNode to the possible precede node list.
		PossiblePrecedeNode newPrecedeNode = new PossiblePrecedeNode(returnNode, PossiblePrecedeReasonType.PPR_RETURN, null);
		precedeNodeList.add(newPrecedeNode);
		
		return precedeNodeList;
	}

}
