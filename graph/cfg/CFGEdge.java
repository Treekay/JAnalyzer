package graph.cfg;

import graph.basic.GraphEdge;
import graph.basic.GraphNode;

/**
 * The class of edges of CFG
 * @author Zhou Xiaocong
 * @since 2013-12-26
 * @version 1.0
 */
public class CFGEdge implements GraphEdge {
	public final static String LABEL_TRUE = "true";
	public final static String LABEL_FALSE = "false";
	
	private CFGNode startNode = null;		// Start node of the edge, it can not be null!
	private CFGNode endNode = null;			// End node of the edge, it can not be null!
	private String label = null;			// The label of the edge, it may be null!
	private String description = null;		// The detailed description of the edge, it may be null!

	/**
	 * @param startNode: Start node of the edge, it can not be null!
	 * @param endNode: End node of the edge, it can not be null!
	 */
	public CFGEdge(CFGNode startNode, CFGNode endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
	}

	public CFGEdge(CFGNode startNode, CFGNode endNode, String label) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.label = label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public GraphNode getEndNode() {
		return endNode;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public GraphNode getStartNode() {
		return startNode;
	}

	@Override
	public boolean isDirected() {
		return true;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof CFGEdge)) return false;
		CFGEdge otherEdge = (CFGEdge)other;

		if (!startNode.equals(otherEdge.getStartNode())) return false;
		if (!endNode.equals(otherEdge.getEndNode())) return false;
		if (label == null && otherEdge.label != null) return false;
		if (label != null && otherEdge.label == null) return false;
		if (label == null && otherEdge.label == null)  return true;
		if (!label.equals(otherEdge.label)) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = startNode.hashCode() + 3 * endNode.hashCode();
		if (label != null) result = result + 5 * label.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		String result = "<" + startNode.toString() + ", " + endNode.toString() + ">";
		if (label != null) result = result + "[" + label + "]";
		return result;
	}
	
	public String toFullString() {
		String result = "CFG Edge: " + "<" + startNode.toString() + ", " + endNode.toString() + ">";
		if (label != null) result = result + "\n\t[" + label + "]";
		if (description != null) result = result + description;
		return result; 
	}
}
