package graph.cfg.creator;

/**
 * The enumeration for possible precede node type when creating CFG from JDT ASt.<br>
 * (1) PPR_SEQUENCE: The node is a precede node because it is a sequence precede node of the succeed nodes<br>
 * (2) PPR_BREAK: The node is a break node, and its succeed nodes are after the loop body.<br>
 * (3) PPR_CONTINUE: The node is a continue node, and its succeed nodes are the loop condition.<br>
 * (4) PPR_RETURN: The node is a return node, and its succeed node is the end node of the entire method<br>
 * (5) PPR_THROW: The node is a throw node, and its succeed node is the abnormal end node of the entire method
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 *
 */
public enum PossiblePrecedeReasonType {
	/**
	 * Sequential precede node 
	 */
	PPR_SEQUENCE,
	/**
	 * A break node with or without label
	 */
	PPR_BREAK,
	/**
	 * A continue node with or without label
	 */
	PPR_CONTINUE,
	/**
	 * A return node
	 */
	PPR_RETURN,
	/**
	 * A throw node
	 */
	PPR_THROW;
}
