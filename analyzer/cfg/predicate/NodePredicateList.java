package analyzer.cfg.predicate;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import graph.basic.GraphNode;
//import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018年7月8日
 * @version 1.0
 *
 */
public class NodePredicateList {
	// A list represents empty list, i.e. a tautology!
	public static final NodePredicateList TRUE_LIST = new NodePredicateList();
	// A list represents a contradiction!
	public static final NodePredicateList FALSE_LIST = new NodePredicateList();
	
	protected LinkedList<NodePredicateRecorder> predicateList = null;

	public NodePredicateList() {
		predicateList = new LinkedList<NodePredicateRecorder>();
	}
	
	public NodePredicateList(NodePredicateRecorder predicate) {
		predicateList = new LinkedList<NodePredicateRecorder>();
		predicateList.add(predicate);
	}
	
	public int size() {
		return predicateList.size();
	}
	
	public List<NodePredicateRecorder> getPredicateRecorderList() {
		return predicateList;
	}
	
	public boolean equivalentTo(NodePredicateList other) {
		// Process the special cases
		if (this == other) return true;
		if (this == TRUE_LIST || this == FALSE_LIST) return false;
		
		for (NodePredicateRecorder predicate : predicateList) {
			if (!other.predicateList.contains(predicate)) return false;
		}
		for (NodePredicateRecorder predicate : other.predicateList) {
			if (!predicateList.contains(predicate)) return false;
		}
		return true;
	}
	
	/**
	 * Do conjunction with other predicate list. If there is a contradiction on two branch predicates in these two list, then
	 * return FALSE_LIST to represent such a contradiction, otherwise return a list that equals to logical conjunction of all 
	 * predicates in these two list. 
	 */
	public NodePredicateList conjunctionWith(NodePredicateList other) {
		// Process the special cases!
		if (this == FALSE_LIST || other == FALSE_LIST) return FALSE_LIST;
		if (this == TRUE_LIST && other == TRUE_LIST) return TRUE_LIST;
		NodePredicateList result = new NodePredicateList();
		if (this == TRUE_LIST) {
			result.predicateList.addAll(other.predicateList);
			return result;
		} else if (other == TRUE_LIST) {
			result.predicateList.addAll(predicateList);
			return result;
		}
		
		// First add the predicates in other list, that is, the predicates in other list are the newer predicates!
		for (NodePredicateRecorder otherRecorder : other.predicateList) result.predicateList.add(otherRecorder);
		for (NodePredicateRecorder recorder : predicateList) {
			boolean addable = true;
			for (NodePredicateRecorder otherRecorder : other.predicateList) {
				if (recorder.equals(otherRecorder)) {
					addable = false;
					break;
				} else if (recorder.isNegative(otherRecorder)) {
					if (recorder.getType() != otherRecorder.getType()) {
						throw new AssertionError("Two multi-negative predicates have different type: " + recorder.predicate.getUniqueId());
					}
					if (recorder.getType() == NodePredicateRecorder.BRANCH_PREDICATE) {
						// There is a contradiction in two lists, so we return a special list to represent this contradiction!
						return FALSE_LIST;
					}
				}
			}
			if (addable) result.predicateList.add(recorder);
		}
		
		return result;
	}
	
	/**
	 * Do conjunction with other predicate list. If there is a contradiction on two branch predicates in these two list, then
	 * return FALSE_LIST to represent such a contradiction, otherwise return a list that equals to logical conjunction of all 
	 * predicates in these two list. 
	 */
	public NodePredicateList conjunctionWith(NodePredicateRecorder other) {
		// Process the special cases!
		if (this == FALSE_LIST) return FALSE_LIST;
		NodePredicateList result = new NodePredicateList();
		if (this == TRUE_LIST) {
			result.predicateList.add(other);
			return result;
		}
		
		// First add the predicates in other list, that is, the predicates in other list are the newer predicates!
		result.predicateList.add(other);
		for (NodePredicateRecorder recorder : predicateList) {
			boolean addable = true;
			if (recorder.equals(other)) {
				addable = false;
				break;
			} else if (recorder.isNegative(other)) {
				if (recorder.getType() != other.getType()) {
					throw new AssertionError("Two multi-negative predicates have different type: " + recorder.predicate.getUniqueId());
				}
				if (recorder.getType() == NodePredicateRecorder.BRANCH_PREDICATE) {
					// There is a contradiction in two lists, so we return a special list to represent this contradiction!
					return FALSE_LIST;
				}
			}
			if (addable) result.predicateList.add(recorder);
		}
		
		return result;
	}
	
	/**
	 * Do disjunction with other predicate list. If these two predicate list can be merged to a list according to logical disjunction
	 * operation, the result list will be returned, otherwise FALSE_LIST will be returned.
	 */
	public NodePredicateList disjunctiveMergeWith(NodePredicateList other) {
		// Process the special cases
		if (this == TRUE_LIST || other == TRUE_LIST) return TRUE_LIST;
		if (this == FALSE_LIST && other == FALSE_LIST) return FALSE_LIST;
		NodePredicateList result = new NodePredicateList();
		if (this == FALSE_LIST) {
			result.predicateList.addAll(other.predicateList);
			return result;
		} else if (other == FALSE_LIST) {
			result.predicateList.addAll(predicateList);
			return result;
		}

		LinkedList<NodePredicateRecorder> shortList = predicateList;
		LinkedList<NodePredicateRecorder> longList = other.predicateList;
		if (predicateList.size() > other.predicateList.size()) {
			shortList = other.predicateList;
			longList = predicateList;
		}
		
		// We try to find predicates of the short list in the long list. And then if there a predicate in the short list
		// can not be found in the long list, it means these two predicate lists can not be merged!
		boolean hasNegative = false;
		for (NodePredicateRecorder recorder : shortList) {
			boolean found = false;
			boolean addable = true;
			
			for (NodePredicateRecorder otherRecorder : longList) {
				if (recorder == otherRecorder) {
					// The predicate in recorder is also in otherRecorder. So we find the predicate and this predicate
					// should be added into the result predicate list
					found = true;
					addable = true;
				} else if (recorder.isNegative(otherRecorder) && (hasNegative == false)) {
					// The negation of the predicate in recorder is in otherRecorder. So we also find the predicate (actually, 
					// its negation), but we need no to add them into the result predicate list!
					found = true;
					addable = false;
					hasNegative = true;
					if (shortList.size() != longList.size()) return FALSE_LIST;
				}
			}
			if (!found) {
				// If we do not found the predicates in the long predicate list, then these two lists can not be merged 
				// according to logical disjunctive operation, we return FALSE_LIST to represent that they can not be merged!
				return FALSE_LIST;
			}
			if (addable) result.predicateList.add(recorder);
		}
		
		// We need not find predicates of the long list in the short list, because if all predicates in the short list are in
		// the long list, then the merged result according to logical disjunction operation is just the predicates in the short
		// list whose negation is not in the long list.
		return result;
	}

	/**
	 * Do disjunction with other predicate list, and only consider the predicates in the given nodeList. In general, the give nodeList
	 * is the list of the dominate nodes of the current node. 
	 * If these two predicate list can be merged to a list according to logical disjunction operation, the result list will be 
	 * returned, otherwise FALSE_LIST will be returned.
	 */
	public NodePredicateList disjunctiveMergeWith(NodePredicateList other, TreeSet<GraphNode> nodeList) {
		NodePredicateList result = new NodePredicateList();

		LinkedList<NodePredicateRecorder> shortList = predicateList;
		LinkedList<NodePredicateRecorder> longList = other.predicateList;
		if (predicateList.size() > other.predicateList.size()) {
			shortList = other.predicateList;
			longList = predicateList;
		}
		
//		Debug.println("\t\t\tDisjunction and merge, short " + shortList + ", long " + longList);
		
		// We try to find predicates of the short list in the long list. And then if there a predicate in the short list
		// can not be found in the long list, it means these two predicate lists can not be merged!
		NodePredicateRecorder multiNegativeShortRecorder = null;
		NodePredicateRecorder multiNegativeLongRecorder = null;
		int shortIndex = 0;
		while (shortIndex < shortList.size()) {
			NodePredicateRecorder shortRecorder = shortList.get(shortIndex);
			shortIndex++;
			if (!nodeList.contains(shortRecorder.getNode())) continue;
			
			boolean found = false;
			
			int longIndex = 0;
			while (longIndex < longList.size()) {
				NodePredicateRecorder longRecorder = longList.get(longIndex);
				longIndex++;
				if (shortRecorder == longRecorder) {
					found = true;
					break;
				} else if (shortRecorder.isNegative(longRecorder)) {
					multiNegativeShortRecorder = shortRecorder;
					multiNegativeLongRecorder = longRecorder;
				}
			}
			if (!found) break;
			result.predicateList.add(shortRecorder);
		}
		
		if (shortIndex >= shortList.size()) return result;
		
		if (multiNegativeShortRecorder == null) return FALSE_LIST;
		
		while (shortIndex < shortList.size()) {
			NodePredicateRecorder shortRecorder = shortList.get(shortIndex);
			shortIndex++;
			if (!nodeList.contains(shortRecorder.getNode())) continue;
			if (shortRecorder == multiNegativeShortRecorder) continue;

			boolean found = false;
			int longIndex = 0;
			while (longIndex < longList.size()) {
				NodePredicateRecorder longRecorder = longList.get(longIndex);
				longIndex++;
				if (shortRecorder == longRecorder) {
					found = true;
					break;
				}
			}
			if (!found) return FALSE_LIST;
			result.predicateList.add(shortRecorder);
		}
		
		int longIndex = 0;
		while (longIndex < longList.size()) {
			NodePredicateRecorder longRecorder = longList.get(longIndex);
			longIndex++;
			if (!nodeList.contains(longRecorder.getNode())) continue;
			if (longRecorder == multiNegativeLongRecorder) continue;

			boolean found = false;
			shortIndex = 0;
			while (shortIndex < shortList.size()) {
				NodePredicateRecorder shortRecorder = shortList.get(shortIndex);
				shortIndex++;
				if (shortRecorder == longRecorder) {
					found = true;
					break;
				}
			}
			if (!found) return FALSE_LIST;
		}
		return result;
	}
	
	/**
	 * Select those predicates of the current chain which in the give nodeList! 
	 */
	public NodePredicateList select(TreeSet<GraphNode> nodeList) {
		if (this == FALSE_LIST) return FALSE_LIST;
		if (this == TRUE_LIST) return TRUE_LIST;
		if (predicateList.size() <= 0) return TRUE_LIST;
		
		NodePredicateList result = new NodePredicateList();
		for (NodePredicateRecorder recorder : predicateList) {
			if (nodeList.contains(recorder.getNode())) result.predicateList.add(recorder);
		}
		
		return result;
	}
	
	public boolean dominatedContainsAll(NodePredicateList other, TreeSet<GraphNode> dominateNodeList) {
		for (NodePredicateRecorder otherRecorder : other.predicateList) {
			if (!dominateNodeList.contains(otherRecorder.node)) continue;
			boolean found = false;
			for (NodePredicateRecorder thisRecorder : predicateList) {
				if (thisRecorder == otherRecorder) {
					found = true;
					break;
				}
			}
			if (found == false) return false;
		}
		return true;
	}
	

	@Override
	public String toString() {
		if (this == FALSE_LIST) return "<FALSE>";
		else if (this == TRUE_LIST) return "<TRUE>";
		if (predicateList.size() <= 0) return "<TRUE>";
		StringBuilder message = new StringBuilder();
		message.append(predicateList.get(0));
		for (int i = 1; i < predicateList.size(); i++) {
			message.append("<-");
			message.append(predicateList.get(i));
		}
		return message.toString();
	}
}
