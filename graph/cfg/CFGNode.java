package graph.cfg;

import graph.basic.GraphNode;

/**
 * The interface for the cfg node
 * @author Zhou Xiaocong
 * @since 2012/12/26
 * @version 1.0
 * @update 2012/06/12 Zhou Xiaocong
 * 		Add method isVirtual(), isNormalEnd(), isAbnormalEnd(), isStart(), isPredicate()
 *
 */
public interface CFGNode extends GraphNode {
	
	/**
	 * @return The type of the CFG node
	 */
	public CFGNodeType getCFGNodeType();
	
	/**
	 * Test if the node is a virtual node
	 */
	public boolean isVirtual();

	/**
	 * Test if the node is the start node of the entire CFG
	 */
	public boolean isStart();
	
	/**
	 * Test if the node is the end node of the entire CFG
	 */
	public boolean isNormalEnd();
	
	/**
	 * Test if the node is the abnormal end node of the entire CFG
	 */
	public boolean isAbnormalEnd();
	
	/**
	 * Test if the node is a predicate node 
	 */
	public boolean isPredicate();
}
