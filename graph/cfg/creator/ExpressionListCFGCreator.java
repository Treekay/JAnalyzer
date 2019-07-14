package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Create CFG for a list of expressions 
 * @author Zhou Xiaocong
 * @since 2012/12/29
 * @version 1.0
 *
 */
public class ExpressionListCFGCreator {

	public static List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			List<Expression> expressionList, List<PossiblePrecedeNode> precedeNodeList) {

		ExecutionPoint precedeNode = null;
		for (Expression exp : expressionList) {
			// 1 Create an execution point for the expression exp, add it to currentCFG
			ExecutionPoint expNode = currentCFG.getExecutionPointFactory().create(exp);
			currentCFG.addNode(expNode);
			
			if (precedeNode == null) {
				// 2 precedeNode == null means this is the first expression,
				//   call generateEdgeForSequentPrecedeNode() to use expNode to substitute PPR_SEQUENCE nodes in
				//   precedeNodeList, and add suitable edges to currentCFG, and get new precedeNodeList
				precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, expNode);
			} else {
				// 3 precedeNode != null means this is not the first expression 
				//   add edge <precedeNode, expNode> to currentCFg
				currentCFG.addEdge(new CFGEdge(precedeNode, expNode, null));
			}
			precedeNode = expNode; 			// expNode will be the precede node of the node of the next expressions
		}
		if (precedeNode != null) {
			// 4 Add precedeNode to precedeNodeList, since it is the new sequential precede node for the next statements
			precedeNodeList.add(new PossiblePrecedeNode(precedeNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		} 
		// precedeNode == null means the expressionList is an empty list

		return precedeNodeList;
	}

}
