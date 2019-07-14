package softwareStructure;

import graph.basic.GraphNode;
import nameTable.nameDefinition.NameDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ7ÈÕ
 * @version 1.0
 */
public class NameDefinitionGraphNode implements GraphNode {
	private NameDefinition definition = null;
	
	public NameDefinitionGraphNode(NameDefinition method) {
		this.definition = method;
	}

	@Override
	public String getId() {
		return definition.getSimpleName() + definition.getLocation();
	}

	@Override
	public String getLabel() {
		return definition.getSimpleName();
	}

	@Override
	public String getDescription() {
		return definition.toFullString();
	}

	@Override
	public String toFullString() {
		return definition.toFullString();
	}
	
	public NameDefinition getNameDefinition() {
		return definition;
	}
}
