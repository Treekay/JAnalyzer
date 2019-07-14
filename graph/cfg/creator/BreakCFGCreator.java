package graph.cfg.creator;

import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.List;

import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Create CFG for Break statement
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 *
 */
public class BreakCFGCreator extends StatementCFGCreator {

	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodeLabel) {
		
		// 1. Create an execution point for this break statement and add it to the current CFG
		BreakStatement breakStatement = (BreakStatement)astNode;
		ExecutionPoint breakNode = currentCFG.getExecutionPointFactory().createFlowControlNode(breakStatement);
		currentCFG.addNode(breakNode);
		
		// 2. For the possible precede node in the list precedeNodeList, if its reason type is PPR_SEQUENCE, then add edge <precedeNode, 
		//    breakNode> to the current CFG, and remove the precede node from the list to form a new precedeNodeList.
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, breakNode);	
		
		// 3. Add the breakNode to the possible precede node list.
		String label = null;
		if (breakStatement.getLabel() != null) label = breakStatement.getLabel().getIdentifier();
		PossiblePrecedeNode newPrecedeNode = new PossiblePrecedeNode(breakNode, PossiblePrecedeReasonType.PPR_BREAK, label);
		precedeNodeList.add(newPrecedeNode);
		
		return precedeNodeList;
	}

}
