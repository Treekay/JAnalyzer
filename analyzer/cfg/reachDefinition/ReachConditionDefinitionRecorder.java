package analyzer.cfg.reachDefinition;

import java.util.ArrayList;
import java.util.List;

import analyzer.cfg.predicate.NodePredicateListChain;
import analyzer.cfg.predicate.NodePredicateRecorder;

/**
 * @author Zhou Xiaocong
 * @since 2018年7月14日
 * @version 1.0
 *
 */
public class ReachConditionDefinitionRecorder implements IReachConditionDefinitionRecorder {
	NodePredicateListChain chain = NodePredicateListChain.TRUE_CHAIN;
	NodePredicateRecorder falsePredicate = null;
	NodePredicateRecorder truePredicate = null;
	List<GeneratedConditionDefinitionRecorder> generatedList = new ArrayList<GeneratedConditionDefinitionRecorder>();
	List<ConditionDefinitionRecorder> inList = new ArrayList<ConditionDefinitionRecorder>();
	List<ConditionDefinitionRecorder> outList = new ArrayList<ConditionDefinitionRecorder>();
	
	@Override
	public NodePredicateRecorder getTruePredicate() {
		return truePredicate;
	}

	@Override
	public void setTruePredicate(NodePredicateRecorder truePredicate) {
		this.truePredicate = truePredicate; 
	}

	@Override
	public NodePredicateRecorder getFalsePredicate() {
		return falsePredicate;
	}

	@Override
	public void setFalsePredicate(NodePredicateRecorder falsePredicate) {
		this.falsePredicate = falsePredicate;
	}

	@Override
	public NodePredicateListChain getPredicateChain() {
		return chain;
	}

	@Override
	public void setPredicateChain(NodePredicateListChain chain) {
		this.chain = chain;

	}

	@Override
	public void addGeneratedDefinition(DefinitionRecorder definition) {
		GeneratedConditionDefinitionRecorder recorder = new GeneratedConditionDefinitionRecorder(definition, NodePredicateListChain.TRUE_CHAIN);
		generatedList.add(recorder);
	}

	@Override
	public boolean addReachingDefinition(DefinitionRecorder definition) {
		if (!outListContains(definition)) {
			ConditionDefinitionRecorder recorder = new ConditionDefinitionRecorder(definition, NodePredicateListChain.TRUE_CHAIN);
			outList.add(recorder);
			return true;
		} else return false;
	}

	@Override
	public boolean addInReachingDefinition(DefinitionRecorder definition) {
		if (!inListContains(definition)) {
			ConditionDefinitionRecorder recorder = new ConditionDefinitionRecorder(definition, NodePredicateListChain.TRUE_CHAIN);
			inList.add(recorder);
			return true;
		} else return false;
	}

	@Override
	public List<DefinitionRecorder> getInReachingDefinitionList() {
		List<DefinitionRecorder> result = new ArrayList<DefinitionRecorder>();
		for (ConditionDefinitionRecorder recorder : inList) {
			result.add(recorder.definitionList.getFirst());
		}
		return result;
	}

	@Override
	public List<DefinitionRecorder> getReachingDefinitionList() {
		List<DefinitionRecorder> result = new ArrayList<DefinitionRecorder>();
		for (ConditionDefinitionRecorder recorder : outList) {
			result.add(recorder.definitionList.getFirst());
		}
		return result;
	}

	@Override
	public List<DefinitionRecorder> getGeneratedDefinitionList() {
		List<DefinitionRecorder> result = new ArrayList<DefinitionRecorder>();
		for (GeneratedConditionDefinitionRecorder recorder : generatedList) {
			result.add(recorder.definition);
		}
		return result;
	}

	@Override
	public void addGeneratedConditionDefinition(GeneratedConditionDefinitionRecorder definition) {
		generatedList.add(definition);
	}

	@Override
	public boolean addReachingConditionDefinition(ConditionDefinitionRecorder definition) {
		DefinitionRecorder mainDefinition = definition.definitionList.getFirst();
		if (outListContains(mainDefinition)) return false;
		outList.add(definition);
		return true;
	}

	@Override
	public boolean addInReachingConditionDefinition(ConditionDefinitionRecorder definition) {
		DefinitionRecorder mainDefinition = definition.definitionList.getFirst();
		if (inListContains(mainDefinition)) return false;
		inList.add(definition);
		return true;
	}

	@Override
	public List<ConditionDefinitionRecorder> getInReachingConditionDefinitionList() {
		return inList;
	}

	@Override
	public List<ConditionDefinitionRecorder> getReachingConditionDefinitionList() {
		return outList;
	}

	@Override
	public List<GeneratedConditionDefinitionRecorder> getGeneratedConditionDefinitionList() {
		return generatedList;
	}

	
	/**
	 * If the first definition of the definition chain in a condition definition recorder in the inList equals to the 
	 * the given definition, then return true, otherwise return false;   
	 */
	public boolean inListContains(DefinitionRecorder definition) {
		for (ConditionDefinitionRecorder recorder : inList) {
			if (definition == recorder.definitionList.getFirst()) return true;
		}
		return false;
	}

	/**
	 * If the first definition of the definition chain in a condition definition recorder in the outList equals to the 
	 * the given definition, then return true, otherwise return false;   
	 */
	public boolean outListContains(DefinitionRecorder definition) {
		for (ConditionDefinitionRecorder recorder : outList) {
			if (definition == recorder.definitionList.getFirst()) return true;
		}
		return false;
	}
}
