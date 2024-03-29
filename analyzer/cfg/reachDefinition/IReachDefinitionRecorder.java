package analyzer.cfg.reachDefinition;

import java.util.List;

import graph.cfg.IFlowInfoRecorder;

/**
 * @author Zhou Xiaocong
 * @since 2018年4月10日
 * @version 1.0
 *
 */
public interface IReachDefinitionRecorder extends IFlowInfoRecorder {

	/**
	 * Add a generated definition and its value (reference).
	 * Note: a generated definition in a node also kills all definition defined in the precede nodes 
	 */
	public void addGeneratedDefinition(DefinitionRecorder definition);

	/**
	 * Add a definition to the current node. All added definition can reach this node. 
	 * In fact, those definitions include generated definitions in this node and all definitions
	 * which reach the precede nodes and not killed by this node. 
	 */
	public boolean addReachingDefinition(DefinitionRecorder definition);
	
	/**
	 * Add a definition to the reaching definition list of the program point in (i.e. before) this node. 
	 */
	public boolean addInReachingDefinition(DefinitionRecorder definition);

	/**
	 * Get the definition list of the program point in (i.e. before) this node.
	 */
	public List<DefinitionRecorder> getInReachingDefinitionList();
	
	/**
	 * Get all defined names reach this node
	 */
	public List<DefinitionRecorder> getReachingDefinitionList();
	
	/**
	 * Get all defined names generated by this node
	 * Note: we can also get all killed names by this node from its generated names
	 */
	public List<DefinitionRecorder> getGeneratedDefinitionList();

}
