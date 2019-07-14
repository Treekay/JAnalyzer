package graph.cfg.creator;

import graph.cfg.CFGNode;

/**
 * The class of the possible precede node when creating CFG from JDT AST. 
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 *
 */
public class PossiblePrecedeNode {
	private CFGNode node;
	private PossiblePrecedeReasonType reason;
	// The label of break or continue nodes. If the node is a sequence precede node, the label is used to 
	// labeled the edge from the precede node to its successor. 
	private String label;
	
	public PossiblePrecedeNode(CFGNode node, PossiblePrecedeReasonType reason, String label) {
		this.node = node;
		this.reason = reason;
		this.label = label;
	}

	public PossiblePrecedeNode(CFGNode node, PossiblePrecedeReasonType reason) {
		this.node = node;
		this.reason = reason;
		this.label = null;
	}

	public CFGNode getNode() {
		return node;
	}

	public PossiblePrecedeReasonType getReason() {
		return reason;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	// The node itself and its type can not be modified after initialized.
}
