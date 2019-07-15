package softwareStructure;

import graph.basic.AbstractGraph;
import graph.basic.GraphEdge;
import graph.basic.GraphNode;

import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;

/**
 * A graph generated from a MethodReferenceGraph
 * @author Zhou Xiaocong
 * @since 2015年9月23日
 * @version 1.0
 */
public class MethodReferenceGraph extends AbstractGraph {
	private MethodReferenceMatrix matrix = null;
	private String description = null;
	
	public static MethodReferenceGraph createMethodReferenceGraph(SoftwareStructManager structManager, DetailedTypeDefinition type, boolean forDirect, String description) {
		MethodReferenceMatrix methodInvocationMatrix = structManager.createMethodReferenceMatrix(type, forDirect);
		return new MethodReferenceGraph(methodInvocationMatrix, description);
	}
	
	public MethodReferenceGraph(MethodReferenceMatrix matrix, String description) {
		super(matrix.getDetailedTypeDefinition().getSimpleName()+"MRG");
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
		for (MethodDefinition rowMethod : methodList) {
			for (MethodDefinition colMethod : methodList) {
				NameDefinitionGraphNode rowNode = findByMethodDefinition(rowMethod);
				NameDefinitionGraphNode colNode = findByMethodDefinition(colMethod);
				if (matrix.hasReferenceRelation(rowMethod, colMethod)) addEdge(new MethodReferenceEdge(rowNode, colNode));
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


class MethodReferenceEdge implements GraphEdge {
	private NameDefinitionGraphNode caller = null;
	private NameDefinitionGraphNode callee = null;

	public MethodReferenceEdge(NameDefinitionGraphNode caller, NameDefinitionGraphNode callee) {
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
