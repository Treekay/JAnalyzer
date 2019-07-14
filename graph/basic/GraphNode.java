package graph.basic;

/**
 * The interface for the graph node
 * @author Zhou Xiaocong
 * @since 2012/12/26
 * @version 1.0
 *
 */
public interface GraphNode {
	/**
	 * @return The identifiers of the graph node. The identifiers must be unique in the graph.
	 */
	public String getId();
	
	/**
	 * @return The label of the graph node. The label may be shown in the screen.
	 */
	public String getLabel();
	
	/**
	 * @return The detailed description of the graph node.
	 */
	public String getDescription();

	/**
	 * @return The detailed information for displaying the edge
	 */
	public String toFullString();
}
