package analyzer.cfg.reachDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê4ÔÂ10ÈÕ
 * @version 1.0
 *
 */
public class ReachDefinitionRecorder implements IReachDefinitionRecorder {
	protected List<DefinitionRecorder> generatedList = new ArrayList<DefinitionRecorder>();
	protected List<DefinitionRecorder> inList = new ArrayList<DefinitionRecorder>();
	protected List<DefinitionRecorder> outList = new ArrayList<DefinitionRecorder>();
	
	
	@Override
	public void addGeneratedDefinition(DefinitionRecorder definition) {
		generatedList.add(definition);
	}

	@Override
	public boolean addReachingDefinition(DefinitionRecorder definition) {
		if (!outList.contains(definition)) {
			outList.add(definition);
			return true;
		} else return false;
	}

	@Override
	public List<DefinitionRecorder> getReachingDefinitionList() {
		return outList;
	}

	@Override
	public List<DefinitionRecorder> getGeneratedDefinitionList() {
		return generatedList;
	}

	@Override
	public boolean addInReachingDefinition(DefinitionRecorder definition) {
		if (!inList.contains(definition)) {
			inList.add(definition);
			return true;
		} else return false;
	}

	@Override
	public List<DefinitionRecorder> getInReachingDefinitionList() {
		return inList;
	}
	
	public void clearAllCFGNode() {
		for (DefinitionRecorder recorder : generatedList) recorder.clearNode();
		for (DefinitionRecorder recorder : inList) recorder.clearNode();
		for (DefinitionRecorder recorder : outList) recorder.clearNode();
	}

}
