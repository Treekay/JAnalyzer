package graph.cfg.creator;

import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.List;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;

/**
 * Create CFG for throw statement
 * @author Zhou Xiaocong
 * @since 2012/12/29
 * @version 1.0
 *
 */
public class ThrowCFGCreator extends StatementCFGCreator {

	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodeLabel) {

		// 1. Create an execution point for this throw statement and add it to the current CFG
		ThrowStatement throwStatement = (ThrowStatement)astNode;
		ExecutionPoint throwNode = currentCFG.getExecutionPointFactory().createFlowControlNode(throwStatement);
		currentCFG.addNode(throwNode);
		
		// 2. For the possible precede node in the list precedeNodeList, if its reason type is PPR_SEQUENCE, then add edge <precedeNode, 
		//    throwNode> to the current CFG, and remove the precede node from the list to form a new precedeNodeList.
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, throwNode);	
		
		// 3. Add the throwNode to the possible precede node list.
		PossiblePrecedeNode newPrecedeNode = new PossiblePrecedeNode(throwNode, PossiblePrecedeReasonType.PPR_THROW, null);
		precedeNodeList.add(newPrecedeNode);
		
		return precedeNodeList;
	}

}
