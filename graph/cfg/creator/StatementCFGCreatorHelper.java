package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.CFGNode;
import graph.cfg.ControlFlowGraph;

import java.util.List;
import java.util.ListIterator;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/** 
 * This class collects some useful static method for creating CFG
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 * @
 *
 */
public final class StatementCFGCreatorHelper {
	/**
	 * Traverse the list precedeNodeList, if the reason of the node precedeNode in the list is PPR_SEQUENCE, 
	 * then add an edge from precedeNode to node to the current CFG (currentCFG), and then return the original list after removed all 
	 * PPR_SEQUENCE node<br>
	 * 
	 * @param currentCFG : the CFG (ControlFlowGraph object) is creating
	 * @param precedeNodeList : the list include the nodes may be the precede node of the CFGNode node
	 * @param node : its sequential precede nodes are the precedeNode in the list precedeNodeList which reason is 
	 * 		PPR_SEQUENCE. This node must have been add to the currentCFG
	 * @return The original precede node list with removed all PPR_SEQUENCE nodes. 
	 */
	public static List<PossiblePrecedeNode> generateEdgeForSequentPrecedeNode(ControlFlowGraph currentCFG, 
			List<PossiblePrecedeNode> precedeNodeList, CFGNode node) {

		// Because we may remove the node from the precedeNodeList, we can not use enhanced for statement to traverse the list.
		// We have to use iterator to traverse the list.
		ListIterator<PossiblePrecedeNode> iterator = (ListIterator<PossiblePrecedeNode>)precedeNodeList.iterator();
		while (iterator.hasNext()) {
			PossiblePrecedeNode precedeNode = iterator.next();
			if (precedeNode.getReason() == PossiblePrecedeReasonType.PPR_SEQUENCE) {
				// Generate edge <precedeNode, node> for the currentCFG
				CFGEdge edge = new CFGEdge(precedeNode.getNode(), node, precedeNode.getLabel());
				edge.setLabel(precedeNode.getLabel());
				currentCFG.addEdge(edge);
				
				iterator.remove();		// remove the just returned node (i.e. precedeNode)
			}

		}
		return precedeNodeList;
	}

	/**
	 * Translate an expression to a String. The current implementation just call Expression.toString(). If the return string is not 
	 * so good for use, we can change its implementation.
	 * Note: see the class org.eclipse.jdt.internal.core.dom.NaiveASTFlattener for the implementation of Expression.toString() 
	 */
	public static String astNodeToString(Expression exp) {
		return exp.toString();
	}
	
	/**
	 * Translate a single variable declaration to a String. Here we just call ASTNode.toString()
	 * Note: see the class org.eclipse.jdt.internal.core.dom.NaiveASTFlattener for the implementation of SingleVariableDeclaration.toString() 
	 */
	public static String astNodeToString(SingleVariableDeclaration declaration) {
		return declaration.toString();
	}

	/**
	 * Translate a variable declaration statement to a String. For our purpose, we delete the new line in the 
	 * implementation of VariableDelcarationStatement.toString() in the class 
	 * org.eclipse.jdt.internal.core.dom.NaiveASTFlattener 
	 */
	@SuppressWarnings("unchecked")
	public static String astNodeToString(VariableDeclarationStatement node) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(node.getType().toString());
		buffer.append(" ");
		List<VariableDeclarationFragment> fragmentList = node.fragments();
		for (VariableDeclarationFragment fragment : fragmentList) {
			buffer.append(fragment.toString());
			if (fragmentList.size() > 1) buffer.append(", ");
		}
		return buffer.toString();
	}

	/**
	 * Translate a variable declaration statement to a String. For our purpose, we delete the new line in the 
	 * implementation of ReturnStatement.toString() in the class 
	 * org.eclipse.jdt.internal.core.dom.NaiveASTFlattener 
	 */
	public static String astNodeToString(ReturnStatement node) {
		Expression returnExpression = node.getExpression();
		if (returnExpression != null) return "return " + node.getExpression().toString();
		else return "return";
	}
	
	public static boolean needAddEdgeByMatchLabel(String precedeLabel, String nodeLabel) {
		if (precedeLabel == null) return true;
		if (nodeLabel == null) return false;
		if (precedeLabel.equals(nodeLabel)) return true;
		return false;
	}
}
