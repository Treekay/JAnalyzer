package softwareStructure;

import graph.basic.AbstractGraph;
import graph.basic.GraphEdge;
import graph.basic.GraphNode;

import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;

/**
 * A graph generated from a MethodInvocatinMatrix 
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ7ÈÕ
 * @version 1.0
 */
public class MethodInvocationGraph extends AbstractGraph {
	private MethodInvocationMatrix matrix = null;
	private String description = null;
	
	public static MethodInvocationGraph createMethodInvocationGraph(SoftwareStructManager structManager, DetailedTypeDefinition type, boolean forDirect, boolean forStatic, String description) {
		MethodInvocationMatrix methodInvocationMatrix = structManager.createMethodInvocationMatrix(type, forDirect, forStatic);
		return new MethodInvocationGraph(methodInvocationMatrix, description);
	}
	
	public MethodInvocationGraph(MethodInvocationMatrix matrix, String description) {
		super(matrix.getDetailedTypeDefinition().getSimpleName()+"MIG");
		this.matrix = matrix;
		this.description = description;
		
		transform();
	}
	
	public String getDescription() {
		return description;
	}
	
	private void transform() {
		List<MethodDefinition> methodList = matrix.getMethodList();
		for (MethodDefinition method : methodList) addNode(new NameDefinitionGraphNode(method));
		for (MethodDefinition caller : methodList) {
			for (MethodDefinition callee : methodList) {
				NameDefinitionGraphNode callerNode = findByMethodDefinition(caller);
				NameDefinitionGraphNode calleeNode = findByMethodDefinition(callee);
				if (matrix.hasInvocationRelation(caller, callee)) addEdge(new MethodReferenceEdge(callerNode, calleeNode));
			}
		}
	}
	
	NameDefinitionGraphNode findByMethodDefinition(MethodDefinition method) {
		for (GraphNode node : nodes) {
			NameDefinitionGraphNode methodNode = (NameDefinitionGraphNode)node; 
			MethodDefinition definition = (MethodDefinition)methodNode.getNameDefinition();
			if (definition == method) return methodNode;
		}
		return null;
	}

}


class MethodInvocationEdge implements GraphEdge {
	private NameDefinitionGraphNode caller = null;
	private NameDefinitionGraphNode callee = null;

	public MethodInvocationEdge(NameDefinitionGraphNode caller, NameDefinitionGraphNode callee) {
		this.caller = caller;
		this.callee = callee;
	}
	
	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public GraphNode getStartNode() {
		return caller;
	}

	@Override
	public GraphNode getEndNode() {
		return callee;
	}

	@Override
	public String getLabel() {
		return "<" + caller.getLabel() + ", " + callee.getLabel() + ">";
	}

	@Override
	public String getDescription() {
		return caller.getDescription() + " calling " + callee.getDescription();
	}

	@Override
	public String toFullString() {
		return caller.getDescription() + " --> " + callee.getDescription();
	}
	
}
