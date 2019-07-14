package graph.cfg.creator;

import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.List;

import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Create CFG for Continue statement
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 *
 */
public class ContinueCFGCreator extends StatementCFGCreator {

	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodeLabel) {
		
		// 1. Create an execution point for this continue statement and add it to the current CFG
		ContinueStatement continueStatement = (ContinueStatement)astNode;
		ExecutionPoint continueNode = currentCFG.getExecutionPointFactory().createFlowControlNode(continueStatement);
		currentCFG.addNode(continueNode);
		
		// 2. For the possible precede node in the list precedeNodeList, if its reason type is PPR_SEQUENCE, then add edge <precedeNode, 
		//    continueNode> to the current CFG, and remove the precede node from the list to form a new precedeNodeList.
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, continueNode);	
		
		// 3. Add the continueNode to the possible precede node list.
		String label = null;
		if (continueStatement.getLabel() != null) label = continueStatement.getLabel().getIdentifier();
		PossiblePrecedeNode newPrecedeNode = new PossiblePrecedeNode(continueNode, PossiblePrecedeReasonType.PPR_CONTINUE, label);
		precedeNodeList.add(newPrecedeNode);
		
		return precedeNodeList;
	}

}
