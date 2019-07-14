package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Create CFG for If statement
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 *
 */
public class IfCFGCreator extends StatementCFGCreator {

	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodelLabel) {
		
		ExecutionPointFactory factory = currentCFG.getExecutionPointFactory();

		IfStatement ifStatement = (IfStatement)astNode;
		
		// 1.1 Create an execution point for the condition expression in this if statement, and add it to the current CFG
		ExecutionPoint conditionNode = factory.createPredicate(ifStatement);
		currentCFG.addNode(conditionNode);
		// 1.2 Traverse the list precedeNodeList, if the reason of the precedeNode in the list is PPR_SEQUENCE, 
		//    add an edge <precedeNode, conditionNode> to the current CFG, and remove precedeNode from the list to form a new precedeNodeList
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, conditionNode);

		// 2 Create a new PossiblePrecedeNode list, which just include one node, i.e. conditionNode, since the precede node of the branches of 
		//   if statement must be the conditionNode.
		List<PossiblePrecedeNode> ifPrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
		ifPrecedeNodeList.add(new PossiblePrecedeNode(conditionNode, PossiblePrecedeReasonType.PPR_SEQUENCE, CFGEdge.LABEL_TRUE));
		
		// 3 Create CFG for the then branch of this if statement, and get a new possible precede node list
		Statement thenStatement = ifStatement.getThenStatement();
		StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(thenStatement);
		List<PossiblePrecedeNode> thenPrecedeNodeList = creator.create(currentCFG, thenStatement, ifPrecedeNodeList, null);

		// 4 Traverse thenPrecedeNodeList, if it has PPR_SEQUENCE node thenPrecedeNode, then create virtual end node endNode for the statement, 
		//	 and add the edge <thenPrecedeNode, endNode> to currentCFG. Also, we add non PPR_SEQUENCE node to the precedeNodeList in the above 1.2
		ExecutionPoint endNode = null;
		for (PossiblePrecedeNode thenPrecedeNode : thenPrecedeNodeList) {
			if (thenPrecedeNode.getReason() == PossiblePrecedeReasonType.PPR_SEQUENCE) {
				if (endNode == null) {
					endNode = factory.createVirtualEnd(ifStatement);
					currentCFG.addNode(endNode);
				}
				currentCFG.addEdge(new CFGEdge(thenPrecedeNode.getNode(), endNode));
			} else precedeNodeList.add(thenPrecedeNode);
		}

		// 5 If the statement has else branch, treat the else branch as the then branch. 
		Statement elseStatement = ifStatement.getElseStatement();
		if (elseStatement != null) {
			// 5.1 Since StatementCFGCreator.creat() may change the list pointed by the parameter precedeNodeList, the ifPrecedeNodeList may be 
			//     changed after call creator.create() for thenStatement. Therefore, we need to re-create ifPrecedeNodeList
			ifPrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
			ifPrecedeNodeList.add(new PossiblePrecedeNode(conditionNode, PossiblePrecedeReasonType.PPR_SEQUENCE, CFGEdge.LABEL_FALSE));
			
			// 5.2 Create CFG for the else branch, and get a new possible precede node list elsePrecedeNodeList.
			creator = StatementCFGCreatorFactory.getCreator(elseStatement);
			List<PossiblePrecedeNode> elsePrecedeNodeList = creator.create(currentCFG, elseStatement, ifPrecedeNodeList, null);
			
			// 5.3 traverse elsePrecedNodeList, if it has PPR_SEQUENCE node elsePrecedeNode, then add the edge <elsePrecedeNode, endNode> 
			//     to currentCFG, Also, we add non PPR_SEQUENCE node to the precedeNodeList in the above 1.2
			for (PossiblePrecedeNode elsePrecedeNode : elsePrecedeNodeList) {
				if (elsePrecedeNode.getReason() == PossiblePrecedeReasonType.PPR_SEQUENCE) {
					if (endNode == null) {
						endNode = factory.createVirtualEnd(ifStatement);
						currentCFG.addNode(endNode);
					}
					currentCFG.addEdge(new CFGEdge(elsePrecedeNode.getNode(), endNode));
				} else precedeNodeList.add(elsePrecedeNode);
			}
		} else {
			// There is no else branch of the statement, create endNode (if it is null), and add edge <conditionNode, endNode>
			if (endNode == null) {
				endNode = factory.createVirtualEnd(ifStatement);
				currentCFG.addNode(endNode);
			}
			currentCFG.addEdge(new CFGEdge(conditionNode, endNode, CFGEdge.LABEL_FALSE));
		}
		
		// 6 If we have created the virtual end node endNode, add it to the precedeNodeList
		if (endNode != null) precedeNodeList.add(new PossiblePrecedeNode(endNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		
		return precedeNodeList;
	}

}
