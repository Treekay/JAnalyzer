package graph.cfg;

public enum CFGNodeType {
	/**
	 * The node is an execution point
	 */
	N_EXECUTION_POINT, 
	/**
	 * The node is a basic block
	 */
	N_BASIC_BLOCK,
	/**
	 * The node is a control flow graph of another method
	 */
	N_SUB_CFG,
}
