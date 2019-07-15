package analyzer.cfg.predicate;

import graph.cfg.CFGNode;
import nameTable.nameReference.NameReference;

/**
 * @author Zhou Xiaocong
 * @since 2018年7月8日
 * @version 1.0
 *
 */
public class NodePredicateRecorder {
	public static int BRANCH_PREDICATE = 0;
	public static int LOOP_PREDICATE = 1;
	
	protected int type = BRANCH_PREDICATE;
	protected CFGNode node = null;
	protected NameReference predicate = null;
	protected boolean value = true;

	public NodePredicateRecorder(int type, CFGNode node, NameReference predicate, boolean value) {
		this.type = type;
		this.node = node;
		this.predicate = predicate;
		this.value = value;
	}

	public int getType() {
		return type;
	}

	public CFGNode getNode() {
		return node;
	}

	public NameReference getPredicate() {
		return predicate;
	}

	public boolean getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0;
		if (value == false) result = predicate.hashCode();
		else result = prime + predicate.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		else return false;
//		if (obj == null) return false;
//		if (!(obj instanceof NodePredicateRecorder)) return false;
		
//		NodePredicateRecorder other = (NodePredicateRecorder) obj;
//		return predicate.equals(other.predicate) && value == other.value;
	}

	public boolean isNegative(NodePredicateRecorder other) {
		return predicate.equals(other.predicate) && value != other.value;
	}
	
	@Override
	public String toString() {
		if (type == LOOP_PREDICATE) {
			return "<(" + predicate.getLocation() + "), " + predicate.toSimpleString(20) + ", " + value + ">*"; 
		} else {
			return "<(" + predicate.getLocation() + "), " + predicate.toSimpleString(20) + ", " + value + ">"; 
		}
	}
}
