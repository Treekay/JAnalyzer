package graph.cfg;

/**
 * The type of the execution point. It has the following type:
 *	(1) BRANCH_PREDICATE: the predicate of the branch statement (if, switch);
 *	(2) LOOP_PREDICATE: the predicate of the loop statement (do..while, while, for)
 *  (3) FLOW_CONTROLLER: the node is break, continue, return, or throw. Those statement control the program executing flow.
 *	(4) GROUP_START: the virtual node for the entry of the some statements (if,	switch, while, do..while, 
 *		for, enhanced for, label, synchronize, try);
 *	(5) GROUP_END£ºthe virtual node for the exit of the some statements (if,	switch, while, do..while, 
 *		for, enhanced for, label, synchronize, try);
 *	(6) CFG_START: The virtual node for the entry of the cfg of the entire method;
 *	(7) CFG_END: The virtual node for the exit of the cfg of the entire method;
 *	(8) METHOD_CALL£ºThe node includes a method call
 *	(9) NORMAL£ºThe node is a normal execution point
 * @author Zhou Xiaocong
 * @since 2012/12/26
 * @version 1.0
 *
 */
public enum ExecutionPointType {
	/**
	 * Predicate (i.e. condition expression) of if or switch statement
	 */
	BRANCH_PREDICATE,
	/**
	 * Predicate (i.e. condition expression) of do, while, for, enhanced for statement
	 */
	LOOP_PREDICATE,
	/**
	 * Break, continue, return or throw
	 */
	FLOW_CONTROLLER,
	/**
	 * Virtual start node of a composite statement
	 */
	GROUP_START,
	/**
	 * Virtual end node of a composite statement
	 */
	GROUP_END,
	/**
	 * Virtual start node of the entire method
	 */
	CFG_START,
	/**
	 * Virtual end node of the entire method
	 */
	CFG_END,
	/**
	 * The node includes method call
	 */
	METHOD_CALL,
	/**
	 * Other execution point types
	 */
	NORMAL;
	
	public boolean isVirtual() {
		if (this.equals(GROUP_START) || this.equals(GROUP_END) || this.equals(CFG_START) || this.equals(CFG_END)) return true;
		else return false;
	}
	
	public boolean isPredicate() {
		if (this.equals(BRANCH_PREDICATE) || this.equals(LOOP_PREDICATE)) return true;
		else return false;
	}

}
