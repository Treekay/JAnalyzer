package graph.cfg.creator;

import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

/**
 * <p>A CFG creator for those simple statements, which include assert statement, constructor invocation, empty statement, 
 * expression statement, super constructor invocation, type declaration statement, variable declaration statement. 
 * <p>Note that we will only create one CFG node for a type declaration statement
 * @author Zhou Xiaocong
 * @since 2012-12-29
 * @version 1.0
 */
public class SimpleStatementCFGCreator extends StatementCFGCreator {

	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodeLabel) {
		if (astNode.getNodeType() == ASTNode.EMPTY_STATEMENT) return precedeNodeList;
		
		// 1. Create an execution point for astNode, and add it to the current CFG
		ExecutionPoint execPoint = currentCFG.getExecutionPointFactory().create(astNode);
		currentCFG.addNode(execPoint);
		
		// 2. Traverse the list precedeNodeList, if the reason of the precedeNode in the list is PPR_SEQUENCE, 
		//    add an edge <precedeNode, execPoint> to the current CFG, and remove precedeNode from the list to form a new precedeNodeList.
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, execPoint);
		
		// 3. Add the node execPoint to the precedeNodeList
		precedeNodeList.add(new PossiblePrecedeNode(execPoint, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		
		return precedeNodeList;
	}

}
