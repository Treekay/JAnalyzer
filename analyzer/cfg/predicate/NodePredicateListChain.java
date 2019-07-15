package analyzer.cfg.predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

//import util.Debug;

import graph.basic.GraphNode;

/**
 * @author Zhou Xiaocong
 * @since 2018年7月8日
 * @version 1.0
 *
 */
public class NodePredicateListChain {
	// The chain represents a tautology
	public static final NodePredicateListChain TRUE_CHAIN = new NodePredicateListChain();
	
	protected List<NodePredicateList> predicateChain = null;
	
	public NodePredicateListChain() {
		predicateChain = new ArrayList<NodePredicateList>();
	}
	
	public List<NodePredicateList> getPredicateList() {
		return predicateChain;
	}
	
	public NodePredicateListChain(NodePredicateList firstPredicateList) {
		predicateChain = new ArrayList<NodePredicateList>();
		predicateChain.add(firstPredicateList);
	}
	
	/**
	 * Test whether every list in predicate chain includes the given predicate
	 */
	public boolean contains(NodePredicateRecorder predicate) {
		if (predicateChain.size() <= 0) return false;
		for (NodePredicateList predicates : predicateChain) {
			if (!predicates.predicateList.contains(predicate)) return false;  
		}
		return true;
	}
	
	public boolean equivalentTo(NodePredicateListChain other) {
		if (predicateChain.size() <= 0 && other.predicateChain.size() <= 0) return true;
		for (NodePredicateList predicateList : predicateChain) {
			boolean hasEquivalence = false;
			for (NodePredicateList otherPredicateList : other.predicateChain) {
				if (predicateList.equivalentTo(otherPredicateList)) {
					hasEquivalence = true;
					break;
				}
			}
			if (!hasEquivalence) return false;
		}
		for (NodePredicateList otherPredicateList : other.predicateChain) {
			boolean hasEquivalence = false;
			for (NodePredicateList predicateList : predicateChain) {
				if (predicateList.equivalentTo(otherPredicateList)) {
					hasEquivalence = true;
					break;
				}
			}
			if (!hasEquivalence) return false;
		}
		return true;
	}
	
	/**
	 * Do conjunction with another predicate list
	 */
	public NodePredicateListChain conjunctionWith(NodePredicateListChain other) {
		NodePredicateListChain result = new NodePredicateListChain();
		if (predicateChain.size() <= 0) {
			for (NodePredicateList otherList : other.predicateChain) {
				result.predicateChain.add(otherList);
			}
			return result;
		}
		
		for (NodePredicateList oldPredicateList : predicateChain) {
			for (NodePredicateList otherPredicateList : other.predicateChain) {
				NodePredicateList newPredicateList = oldPredicateList.conjunctionWith(otherPredicateList);
				if (newPredicateList != NodePredicateList.FALSE_LIST) result.predicateChain.add(newPredicateList); 
			}
		}
		return result;
	}

	/**
	 * Do conjunction with another predicate list
	 */
	public NodePredicateListChain conjunctionWith(NodePredicateList predicateList) {
		NodePredicateListChain result = new NodePredicateListChain();
		if (predicateChain.size() <= 0) {
			result.predicateChain.add(predicateList);
			return result;
		}
		
		for (NodePredicateList oldPredicateList : predicateChain) {
			NodePredicateList newPredicateList = oldPredicateList.conjunctionWith(predicateList);
			if (newPredicateList != NodePredicateList.FALSE_LIST) result.predicateChain.add(newPredicateList); 
		}
		return result;
	}
	
	/**
	 * Do conjunction with another predicate
	 */
	public NodePredicateListChain conjunctionWith(NodePredicateRecorder predicate) {
		NodePredicateListChain result = new NodePredicateListChain();
		if (predicateChain.size() <= 0) {
			NodePredicateList predicateList = new NodePredicateList(predicate);
			result.predicateChain.add(predicateList);
			return result;
		}
		
		for (NodePredicateList oldPredicateList : predicateChain) {
			NodePredicateList newPredicateList = oldPredicateList.conjunctionWith(predicate);
			if (newPredicateList != NodePredicateList.FALSE_LIST) result.predicateChain.add(newPredicateList); 
		}
		return result;
	}

	/**
	 * Do disjunction with another predicate list
	 */
	public NodePredicateListChain disjunctionWith(NodePredicateList predicateList) {
		NodePredicateListChain result = new NodePredicateListChain();
		// This empty predicate chain represents a tautology, and then the result should be also an empty predicate chain, i.e. a tautology 
		if (predicateChain.size() <= 0) return result;
		// When do disjunction with a FALSE_LIST, i.e. a contradiction, the result is just this predicate chain
		if (predicateList == NodePredicateList.FALSE_LIST) {
			for (NodePredicateList oldPredicateList : predicateChain) result.predicateChain.add(oldPredicateList) ;
			return result;
		}
		
		boolean merged = false;
		for (NodePredicateList oldPredicateList : predicateChain) {
			if (oldPredicateList == NodePredicateList.TRUE_LIST) {
				// This predicate chain should be a tautology, and the result is also a tautology!!
				return TRUE_CHAIN;
			}
			// We do not care the FALSE_LIST, i.e. a contradiction in this predicate chain!
			if (oldPredicateList == NodePredicateList.FALSE_LIST) continue;
			
			// Here predicateList != FALSE_LIST && oldPredicateList != FALSE_LIST
			NodePredicateList newPredicateList = oldPredicateList.disjunctiveMergeWith(predicateList);
			if (newPredicateList != NodePredicateList.FALSE_LIST) {
				// We can merge these two predicate list, so we add the merged predicate list to the result chain
				merged = true;
				result.predicateChain.add(newPredicateList); 
			} else {
				// We can not merge these two predicate list, so we add the predicate list in this chain to the result chain
				result.predicateChain.add(oldPredicateList);
			}
		}
		// If we do not merge predicateList to some predicate list in this chain, we should add it to the result chain!
		if (!merged) result.predicateChain.add(predicateList);
		return result;
	}
	
	/**
	 * Do disjunction with another predicate chain
	 */
	public NodePredicateListChain disjunctionWith(NodePredicateListChain other) {
		NodePredicateListChain result = new NodePredicateListChain();
		// This empty predicate chain represents a tautology, and then the result should be also an empty predicate chain, i.e. a tautology 
		if (predicateChain.size() <= 0) return result;
		// The other predicate chain represents a tautology, and then the result should be also an empty predicate chain, i.e. a tautology 
		if (other.predicateChain.size() <= 0) return result;
		
		boolean[] merged = new boolean[predicateChain.size()];
		for (int i = 0; i < merged.length; i++) merged[i] = false;
		
		for (NodePredicateList predicateList : other.predicateChain) {
			// When do disjunction with a FALSE_LIST, i.e. a contradiction, the result is just this predicate chain
			if (predicateList == NodePredicateList.FALSE_LIST) continue;
			if (predicateList == NodePredicateList.TRUE_LIST) return TRUE_CHAIN;
			
			boolean otherMerged = false;
			for (int i = 0; i < predicateChain.size(); i++) {
				NodePredicateList oldPredicateList = predicateChain.get(i);
				if (oldPredicateList == NodePredicateList.TRUE_LIST) {
					// This predicate chain should be a tautology, and the result is also a tautology!!
					return TRUE_CHAIN;
				}
				// We do not care the FALSE_LIST, i.e. a contradiction in this predicate chain!
				if (oldPredicateList == NodePredicateList.FALSE_LIST) {
					merged[i] = true;
					continue;
				}
				
				// Here predicateList != FALSE_LIST && oldPredicateList != FALSE_LIST
				NodePredicateList newPredicateList = oldPredicateList.disjunctiveMergeWith(predicateList);
				if (newPredicateList != NodePredicateList.FALSE_LIST) {
					// We can merge these two predicate list, so we add the merged predicate list to the result chain
					otherMerged = true;
					merged[i] = true;
					result.predicateChain.add(newPredicateList);
					break;
				}
			}
			// If we do not merge predicateList to some predicate list in this chain, we should add it to the result chain!
			if (!otherMerged) result.predicateChain.add(predicateList);
		}
		// Add those predicate lists in the this chain which have not been merged into the result chain
		for (int i = 0; i < predicateChain.size(); i++) {
			if (!merged[i]) result.predicateChain.add(predicateChain.get(i));
		}
		return result;
	}
	
	/**
	 * Do disjunction with another predicate chain, and only consider predicate in the give node list. In general the give node list 
	 * gives the dominate nodes for the current node
	 */
	public NodePredicateListChain disjunctionWith(NodePredicateListChain other, TreeSet<GraphNode> nodeList) {
		NodePredicateListChain result = new NodePredicateListChain();
		// This empty predicate chain represents a tautology, and then the result should be also an empty predicate chain, i.e. a tautology 
		if (predicateChain.size() <= 0) return result;
		// The other predicate chain represents a tautology, and then the result should be also an empty predicate chain, i.e. a tautology 
		if (other.predicateChain.size() <= 0) return result;
		int thisSize = predicateChain.size();
		int otherSize = other.predicateChain.size();
		
		boolean[] thisMergedArray = new boolean[thisSize];
		for (int i = 0; i < thisMergedArray.length; i++) thisMergedArray[i] = false;
		boolean[] otherMergedArray = new boolean[otherSize];
		for (int i = 0; i < otherMergedArray.length; i++) otherMergedArray[i] = false;
		
		for (int thisIndex = 0; thisIndex < thisSize; thisIndex++) {
			NodePredicateList thisPredicateList = predicateChain.get(thisIndex);
			if (thisPredicateList == NodePredicateList.TRUE_LIST) return result;
			if (thisPredicateList == NodePredicateList.FALSE_LIST) {
				thisMergedArray[thisIndex] = true;
				continue;
			}
			
			for (int otherIndex = 0; otherIndex < otherSize; otherIndex++) {
				NodePredicateList otherPredicateList = other.predicateChain.get(otherIndex);
				if (otherPredicateList == NodePredicateList.TRUE_LIST) return result;
				if (otherPredicateList == NodePredicateList.FALSE_LIST) {
					otherMergedArray[otherIndex] = true;
					continue;
				}
				
				if (thisPredicateList.dominatedContainsAll(otherPredicateList, nodeList)) {
					thisMergedArray[thisIndex] = true;
//					Debug.println("\t\t\tThis predicate " + thisPredicateList + " contains other " + otherPredicateList);
				} else if (otherPredicateList.dominatedContainsAll(thisPredicateList, nodeList)) {
					otherMergedArray[otherIndex] = true;
//					Debug.println("\t\t\tOther predicate " + thisPredicateList + " contains this " + otherPredicateList);
				}
			}
		}
		
		
		for (int otherIndex = 0; otherIndex < otherSize; otherIndex++) {
			if (otherMergedArray[otherIndex] == true) continue;
			NodePredicateList otherPredicateList = other.predicateChain.get(otherIndex);
			
			for (int thisIndex = 0; thisIndex < thisSize; thisIndex++) {
				if (thisMergedArray[thisIndex] == true) continue;
				NodePredicateList thisPredicateList = predicateChain.get(thisIndex);
				
				NodePredicateList mergedPredicateList = thisPredicateList.disjunctiveMergeWith(otherPredicateList, nodeList);
				if (mergedPredicateList == NodePredicateList.FALSE_LIST) continue;
				
				thisMergedArray[thisIndex] = true;
				otherMergedArray[otherIndex] = true;
				
				int searchThisIndex = 0;
				int searchOtherIndex = 0;
				while (searchThisIndex < thisSize || searchOtherIndex < otherSize) {
					if (searchThisIndex < thisSize) {
						if (thisMergedArray[searchThisIndex] == true) {
							searchThisIndex++;
							continue;
						}
						NodePredicateList searchPredicateList = predicateChain.get(searchThisIndex);
						NodePredicateList newMergedPredicateList = mergedPredicateList.disjunctiveMergeWith(searchPredicateList, nodeList);
						if (newMergedPredicateList == NodePredicateList.FALSE_LIST) {
							searchThisIndex++;
							continue;
						}
						mergedPredicateList = newMergedPredicateList;
						thisMergedArray[searchThisIndex] = true;
						searchThisIndex = 0;
						searchOtherIndex = 0;
					} else {
						if (otherMergedArray[searchOtherIndex] == true) {
							searchOtherIndex++;
							continue;
						}
						NodePredicateList searchPredicateList = other.predicateChain.get(searchOtherIndex);
						NodePredicateList newMergedPredicateList = mergedPredicateList.disjunctiveMergeWith(searchPredicateList, nodeList);
						if (newMergedPredicateList == NodePredicateList.FALSE_LIST) {
							searchOtherIndex++;
							continue;
						}
						mergedPredicateList = newMergedPredicateList;
						otherMergedArray[searchOtherIndex] = true;
						searchThisIndex = 0;
						searchOtherIndex = 0;
					}
				}
//				Debug.println("\t\t\tAdd merged predicate list: " + mergedPredicateList);
				result.addPredicateList(mergedPredicateList);
			}
		}
		
		for (int thisIndex = 0; thisIndex < thisSize; thisIndex++) {
			if (thisMergedArray[thisIndex] == true) continue;
			NodePredicateList thisPredicateList = predicateChain.get(thisIndex);
//			Debug.println("\t\t\tAdd predicate in this list: " + thisPredicateList);
			result.addPredicateListInNodes(thisPredicateList, nodeList);
		}
		for (int otherIndex = 0; otherIndex < otherSize;otherIndex++) {
			if (otherMergedArray[otherIndex] == true) continue;
			NodePredicateList otherPredicateList = other.predicateChain.get(otherIndex);
//			Debug.println("\t\t\tAdd predicate in other list: " + otherPredicateList);
			result.addPredicateListInNodes(otherPredicateList, nodeList);
		}
		return result;
	}
	
	
	/**
	 * Select those predicates of the current chain which in the give nodeList, and simplify the entire chain! 
	 */
	public NodePredicateListChain selectAndSimplify(TreeSet<GraphNode> nodeList) {
		NodePredicateListChain result = new NodePredicateListChain();
		if (predicateChain.size() <= 0) return result;
		
		if (predicateChain.size() == 1) {
			NodePredicateList oldList = predicateChain.get(0);
			NodePredicateList newList = oldList.select(nodeList);
			result.predicateChain.add(newList);
			return result;
		}
		
		int size = predicateChain.size();
		int oneSize = size / 2;
		for (int i = 0; i < oneSize; i++) result.predicateChain.add(predicateChain.get(i));
		NodePredicateListChain temp = new NodePredicateListChain();
		for (int i = oneSize; i < size; i++) temp.predicateChain.add(predicateChain.get(i));
		
		result = result.disjunctionWith(temp, nodeList);
		return result;
	}
	
	
	protected void addPredicateListInNodes(NodePredicateList predicates, TreeSet<GraphNode> nodeList) {
		NodePredicateList newList = new NodePredicateList();
		for (NodePredicateRecorder recorder : predicates.predicateList) {
			if (nodeList.contains(recorder.node)) newList.predicateList.add(recorder);
		}
		predicateChain.add(newList);
	}

	protected void addPredicateList(NodePredicateList predicates) {
		predicateChain.add(predicates);
	}
	
	public boolean isSimplified() {
		for (NodePredicateList oneList : predicateChain) {
			for (NodePredicateList twoList : predicateChain) {
				if (twoList == oneList) continue;
				if (oneList.predicateList.containsAll(twoList.predicateList)) return false;
				if (twoList.predicateList.containsAll(oneList.predicateList)) return false;
			}
		}
		return true;
	}
	
	public static TreeSet<GraphNode> getCommonCFGNodeList(NodePredicateListChain oneChain, NodePredicateListChain twoChain) {
		TreeSet<GraphNode> resultList = new TreeSet<GraphNode>();
		for (NodePredicateList oneList : oneChain.predicateChain) {
			for (NodePredicateRecorder oneRecorder : oneList.predicateList) {
				GraphNode oneNode = oneRecorder.node;
				
				boolean found = false;
				for (NodePredicateList twoList : twoChain.predicateChain) {
					for (NodePredicateRecorder twoRecorder : twoList.predicateList) {
						if (twoRecorder.node == oneNode) {
							resultList.add(oneNode);
							found = true;
							break;
						}
					}
					if (found) break;
				}
			}
		}
		
		return resultList;
	}

	public NodePredicateListChain getACopy() {
		NodePredicateListChain result = new NodePredicateListChain();
		
		for (NodePredicateList list : predicateChain) {
			NodePredicateList resultList = new NodePredicateList();
			for (NodePredicateRecorder recorder : list.predicateList) {
				resultList.predicateList.add(recorder);
			}
			result.predicateChain.add(resultList);
		}
		return result;
	}
	
	@Override
	public String toString() {
		if (predicateChain.size() <= 0) return "<TRUE>";
		StringBuilder message = new StringBuilder();
		message.append(predicateChain.get(0));
		for (int i = 1; i < predicateChain.size(); i++) {
			message.append("~.OR.~");
			message.append(predicateChain.get(i));
		}
		return message.toString();
	}
}
