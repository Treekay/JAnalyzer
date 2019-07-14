package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Create CFG for for statement
 * @author Zhou Xiaocong
 * @since 2012/12/29
 * @version 1.0
 *
 */
public class ForCFGCreator extends StatementCFGCreator {

	@SuppressWarnings("unchecked")
	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodeLabel) {

		ExecutionPointFactory factory = currentCFG.getExecutionPointFactory();
		// 1 Create CFG for the initialize expression of the for statement, and get new precedeNodeList
		ForStatement forStatement = (ForStatement)astNode;
		List<Expression> expressionList = forStatement.initializers();
		precedeNodeList = ExpressionListCFGCreator.create(currentCFG, expressionList, precedeNodeList);
		
		// 2 Create a conditionNode for the condition expression of the for statement, add it to the currentCFG, then call 
		//   StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNodetraverse() to use conditionNode to substitute PPR_SEQUENCE nodes in
		//   precedeNodeList, and add suitable edges to currentCFG, and get new precedeNodeList
		ExecutionPoint conditionNode = factory.createPredicate(forStatement);
		currentCFG.addNode(conditionNode);
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, conditionNode);
		
		// 3 Create a precede node list loopPrecedeNodeList, which only contains the node conditionNode, since the precede node of the loop body is conditionNode
		List<PossiblePrecedeNode> loopPrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
		loopPrecedeNodeList.add(new PossiblePrecedeNode(conditionNode, PossiblePrecedeReasonType.PPR_SEQUENCE, CFGEdge.LABEL_TRUE));
		
		// 4 Create CFG for the body of the loop statement, and get new precede node list loopBodyPrecedeNode
		List<PossiblePrecedeNode> loopBodyPrecedeNodeList;
		Statement loopBody = forStatement.getBody();
		if (loopBody != null) {
			StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(loopBody);
			loopBodyPrecedeNodeList = creator.create(currentCFG, loopBody, loopPrecedeNodeList, null);
		} else {
			// if the body of the loop is null, then the precede node of the succeed node should still be conditionNode
			loopBodyPrecedeNodeList = loopPrecedeNodeList;		
		}
		
		// 5 Create CFG for the update expression of the for statement, and get new loopBodyPrecedeNodeList
		expressionList = forStatement.updaters();
		List<PossiblePrecedeNode> updatePrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
		for (PossiblePrecedeNode loopBodyPrecedeNode : loopBodyPrecedeNodeList) {
			PossiblePrecedeReasonType reason = loopBodyPrecedeNode.getReason();
			String label = loopBodyPrecedeNode.getLabel();
			// All sequence or matched label continue nodes in loop bodyPrecedeNodeList are precede node of the first update expression 
			if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE) {
				updatePrecedeNodeList.add(loopBodyPrecedeNode);
			} else if (reason == PossiblePrecedeReasonType.PPR_CONTINUE && StatementCFGCreatorHelper.needAddEdgeByMatchLabel(label, nodeLabel)) {
				updatePrecedeNodeList.add(new PossiblePrecedeNode(loopBodyPrecedeNode.getNode(), PossiblePrecedeReasonType.PPR_SEQUENCE, null));
			} else updatePrecedeNodeList.add(loopBodyPrecedeNode);
		}
		updatePrecedeNodeList = ExpressionListCFGCreator.create(currentCFG, expressionList, updatePrecedeNodeList);
		
		// 6 Create a virtual end node endNode for the for statement, add it to currentCFG, and add edge <conditionNode, endNode> to currentCFG
		ExecutionPoint endNode = factory.createVirtualEnd(forStatement);
		currentCFG.addNode(endNode);
		currentCFG.addEdge(new CFGEdge(conditionNode, endNode, CFGEdge.LABEL_FALSE));
		
		// 7 Traverse loopBodyPrecedeNodeList, for each node loopBodyPrecedeNode in the list, if it is a PPR_SEQUENCE node, add edge
		//   <loopBodyPrecedeNode, conditionNode> to currentCFG, if it is a PPR_BREAK node without label, add edge <loopBodyPrecedeNode, endNode>
		//   to currentCFG, if it is a PPR_CONTINUE node without label, add edge <loopBodyPrecedeNode, conditionNode> to currentCFG, 
		//   otherwise add it to precedeNodeList in the above 1
		for (PossiblePrecedeNode updatePrecedeNode : updatePrecedeNodeList) {
			PossiblePrecedeReasonType reason = updatePrecedeNode.getReason();
			String label = updatePrecedeNode.getLabel();
			// Note that matched continue node in loop body have been treated before create node for update expression!
			if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE) {
				currentCFG.addEdge(new CFGEdge(updatePrecedeNode.getNode(), conditionNode, label));
			} else if (reason == PossiblePrecedeReasonType.PPR_BREAK && StatementCFGCreatorHelper.needAddEdgeByMatchLabel(label, nodeLabel)) {
				currentCFG.addEdge(new CFGEdge(updatePrecedeNode.getNode(), endNode, null));
			} else precedeNodeList.add(updatePrecedeNode);
		}
		
		// 8 Add endNode to the precedeNodeList
		precedeNodeList.add(new PossiblePrecedeNode(endNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		return precedeNodeList;
	}

}
