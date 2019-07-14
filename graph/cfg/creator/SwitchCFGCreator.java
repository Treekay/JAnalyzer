package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;

/**
 * Create CFG for Switch statement
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 *
 */
public class SwitchCFGCreator extends StatementCFGCreator {
	public final static String CASE_LABEL_DEFAULT = "default";
	public final static String CASE_LABEL_SEPERATOR = ":";

	@SuppressWarnings("unchecked")
	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodeLabel) {
		
		ExecutionPointFactory factory = currentCFG.getExecutionPointFactory();

		SwitchStatement switchStatement = (SwitchStatement)astNode;
		
		// 1.1 Create an execution point for the condition expression in this switch statement, and add it to the current CFG
		ExecutionPoint conditionNode = factory.createPredicate(switchStatement);
		currentCFG.addNode(conditionNode);
		// 1.2 Traverse the list precedeNodeList, if the reason of the precedeNode in the list is PPR_SEQUENCE, 
		//    add an edge <precedeNode, conditionNode> to the current CFG, and remove precedeNode from the list to form a new precedeNodeList
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, conditionNode);

		// 2 Create a virtual end node for the statement and add it to currentCFG
		ExecutionPoint endNode = null;

		// 3 Get the case branches
		List<Statement> caseStatementList = switchStatement.statements();
		if (caseStatementList == null) {
			// 4 caseStatementList == null, this switch statement is an empty statement actually, add an edge from conditionNode to endNode directly
			endNode = factory.createVirtualEnd(switchStatement);
			currentCFG.addNode(endNode);
			currentCFG.addEdge(new CFGEdge(conditionNode, endNode));
			
			// And add endNode to the precedeNodeList, and return precedeNodeList
			precedeNodeList.add(new PossiblePrecedeNode(endNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
			return precedeNodeList;
		
		}

		// 5 if caseStatementList != null, traverse the list and create CFG for all case branches. 
		//   Note that each case branch is begin with an ASTNode SwitchCase
		List<PossiblePrecedeNode> casePrecedeNodeList = null;
		boolean hasDefault = false;
		for (Statement caseStatement : caseStatementList) {
			if (caseStatement.getNodeType() == ASTNode.SWITCH_CASE) {
				// The statement is a case label, that is, the beginning of a case branch

				// 5.1 Set the precede node list casePrecedeNodeList for the next case branch
				Expression caseExpression = ((SwitchCase)caseStatement).getExpression();
				String caseLabel;
				if (caseExpression == null) {
					caseLabel = CASE_LABEL_DEFAULT;
					hasDefault = true;
				} else caseLabel = StatementCFGCreatorHelper.astNodeToString(caseExpression);

				if (casePrecedeNodeList == null) {
					// 5.1.1 Create a new PossiblePrecedeNode list, which only include one node, i.e. conditionNode, since the precede node of the 
					//       first case branch of switch statement must be the conditionNode.
					casePrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
					casePrecedeNodeList.add(new PossiblePrecedeNode(conditionNode, PossiblePrecedeReasonType.PPR_SEQUENCE, caseLabel));
				} else {
					// 5.1.2 Reset the casePrecedeNodeList, if the conditionNode remain in the casePrecedeNodeList, contact its label with
					//       the label of this SwitchCase, if the conditionNode is not in the casePrecedeNodeList yet, add it to casePrecedeNodeList 
					//       with the label as the same as the label of this SwitchCase
					boolean hasConditionNode = false;
					for (PossiblePrecedeNode casePrecedeNode : casePrecedeNodeList) {
						if (casePrecedeNode.getNode() == conditionNode) {
							// We use == to check if the conditionNode remain in the casePrecedeNodeList, since here checking the pointer value
							// is valid
							casePrecedeNode.setLabel(casePrecedeNode.getLabel() + CASE_LABEL_SEPERATOR + caseLabel);
							hasConditionNode = true;
						} 
					}
					if (! hasConditionNode) {
						casePrecedeNodeList.add(new PossiblePrecedeNode(conditionNode, PossiblePrecedeReasonType.PPR_SEQUENCE, caseLabel));
					}
				} 
			} else {
				// 5.2 The statement is in a case branch, create CFG for this statement, and get new casePrecedeNodeList
				StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(caseStatement);
				// Since each case branch starts with a SwitchCase ASTNode, thus casePrecedeNodeList should not be null!
				if (casePrecedeNodeList == null) throw new AssertionError("The casePrecedeNodeList is null in SwitchCFGCreator!");		
				casePrecedeNodeList = creator.create(currentCFG, caseStatement, casePrecedeNodeList, null);
			}
		}

		// 6 Traverse the casePrecedeNodeList, if the node casePrecedeNode in the list is a PPR_BREAK node with no label or a PPR_SEQUENCE node, then 
		//     add edge <casePrecedeNode, endNode> to currentCFG, otherwise add casePrecedeNode to the precedeNodeList in the above 1.2
		// In general casePrecedeNodeList can not be null, but we fined in com/sun/java/swin/plaf/nimbus/InternalFrameInternalFrameTitlePanePainter.java, there is an empty switch statement!
		if (casePrecedeNodeList != null) {
			for (PossiblePrecedeNode casePrecedeNode : casePrecedeNodeList) {
				PossiblePrecedeReasonType reason = casePrecedeNode.getReason();
				if ((reason == PossiblePrecedeReasonType.PPR_BREAK && casePrecedeNode.getLabel() == null) || 
						reason == PossiblePrecedeReasonType.PPR_SEQUENCE) {
					if (endNode == null) {
						endNode = factory.createVirtualEnd(switchStatement);
						currentCFG.addNode(endNode);
					}
					currentCFG.addEdge(new CFGEdge(casePrecedeNode.getNode(), endNode, casePrecedeNode.getLabel()));
				} else precedeNodeList.add(casePrecedeNode);
			}
			if (!hasDefault) {
				// 2017/9/9: There are no default label, we also need add an edge from condition node to end node!
				if (endNode == null) {
					endNode = factory.createVirtualEnd(switchStatement);
					currentCFG.addNode(endNode);
				}
				currentCFG.addEdge(new CFGEdge(conditionNode, endNode));
			}
		} else {
			// As the same as the above 4, when casePrecedeNodeList == null, this switch statement is an empty statement actually, add an edge 
			// from conditionNode to endNode directly
			endNode = factory.createVirtualEnd(switchStatement);
			currentCFG.addNode(endNode);
			currentCFG.addEdge(new CFGEdge(conditionNode, endNode));
		}
		
		// 7 Add endNode to the precedeNodeList
		if (endNode != null) precedeNodeList.add(new PossiblePrecedeNode(endNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		return precedeNodeList;
	}

}
