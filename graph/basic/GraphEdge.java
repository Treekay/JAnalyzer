package graph.basic;

/**
 * The interface for the graph edge.
 * @author Zhou Xiaocong
 * @since 2012/12/26
 * @version 1.0
 *
 */
public interface GraphEdge {
	/**
	 * @return If the edge is a directed edge return true, else return false
	 */
	public boolean isDirected();
	
	/**
	 * @return The start node of the edge
	 */
	public GraphNode getStartNode();
	
	/**
	 * @return The end node of the edge
	 */
	public GraphNode getEndNode();
	
	/**
	 * @return The label of the edge
	 */
	public String getLabel();
	
	/**
	 * @return The detailed description of the edge
	 */
	public String getDescription();
	
	/**
	 * @return The detailed information for displaying the edge
	 */
	public String toFullString();
}
