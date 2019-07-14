package softwareStructure;

import graph.basic.AbstractGraph;
import graph.basic.GraphEdge;
import graph.basic.GraphNode;
import graph.basic.GraphUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;

/**
 * A graph generated from a MethodInvocatinMatrix 
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ7ÈÕ
 * @version 1.0
 */
public class FieldReferenceGraph extends AbstractGraph {
	private String description = null;
	private FieldReferenceMatrix matrix = null;
	
	public static FieldReferenceGraph createFieldReferenceGraph(SoftwareStructManager structManager, DetailedTypeDefinition type, String description) {
		FieldReferenceMatrix fieldReferenceMatrix = structManager.createFieldReferenceMatrix(type, true);
		return new FieldReferenceGraph(fieldReferenceMatrix, description);
	}
	
	public FieldReferenceGraph(FieldReferenceMatrix matrix, String description) {
		super(matrix.getDetailedTypeDefinition().getSimpleName()+"FRG");
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
		List<FieldDefinition> fieldList = matrix.getFieldList();
		for (FieldDefinition field : fieldList) addNode(new NameDefinitionGraphNode(field));
		
		for (MethodDefinition method : methodList) {
			for (FieldDefinition field : fieldList) {
				NameDefinitionGraphNode methodNode = findByDefinition(method);
				NameDefinitionGraphNode fieldNode = findByDefinition(field);
				if (matrix.hasReferenceRelation(method, field)) addEdge(new MethodReferenceEdge(methodNode, fieldNode));
			}
		}
	}
	
	NameDefinitionGraphNode findByDefinition(NameDefinition nameDef) {
		for (GraphNode node : nodes) {
			NameDefinitionGraphNode methodNode = (NameDefinitionGraphNode)node; 
			NameDefinition definition = methodNode.getNameDefinition();
			if (definition == nameDef) return methodNode;
		}
		return null;
	}

	/**
	 * Write the (directed) graph to a text file, which can be regarded as the description of the graph 
	 * in dot language, and can be used to visualized the graph use Graphviz tools.
	 * @param out : the output text file, which should be opened
	 */
	public void simplyWriteToDotFile(PrintWriter output) throws IOException {
		final int MAX_LABEL_LEN = 16;
		
		String graphId = GraphUtil.getLegalToken(getId());
		output.println("digraph " + graphId + " {");
		for (GraphNode currentNode : nodes) {
			NameDefinitionGraphNode node = (NameDefinitionGraphNode)currentNode;
			
			String label = node.getLabel();
			String nodeId = "node" + GraphUtil.getLegalToken(node.getId());
			NameDefinitionKind kind = node.getNameDefinition().getDefinitionKind();
			String nodeShape = "box";
			// Set the special id and shape for start, normal and abnormal end, predicate and other virtual nodes
			if (kind == NameDefinitionKind.NDK_FIELD) {
				nodeShape = "box";
			} else if (kind == NameDefinitionKind.NDK_METHOD) {
				nodeShape = "ellipse";
			}
			if (label.length() > MAX_LABEL_LEN) {
				label = label.substring(0, MAX_LABEL_LEN) + "...";
			}
			output.println("    " + nodeId + "[label = \"" + label + "\", shape = " + nodeShape + "]");
		}
		if (edges == null) {
			output.println("};");
			output.println();
			output.flush();
			return;
		}
		for (GraphEdge edge : edges) {
			String label = edge.getLabel();
			NameDefinitionGraphNode startNode = (NameDefinitionGraphNode)edge.getStartNode();
			NameDefinitionGraphNode endNode = (NameDefinitionGraphNode)edge.getEndNode();
			
			String startNodeId = "node" + GraphUtil.getLegalToken(startNode.getId());
			String endNodeId = "node" + GraphUtil.getLegalToken(endNode.getId());
			
			if (label != null && label.length() <= 5) {
				output.println("    " + startNodeId + "->" + endNodeId + "[label = \"" + label + "\"]");
			} else {
				output.println("    " + startNodeId + "->" + endNodeId);
			}
		}

		output.println("};");
		output.println();
		output.flush();
	}	
}


class FieldReferenceEdge implements GraphEdge {
	private NameDefinitionGraphNode method = null;
	private NameDefinitionGraphNode field = null;

	public FieldReferenceEdge(NameDefinitionGraphNode method, NameDefinitionGraphNode field) {
		this.method = method;
		this.field = field;
	}
	
	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public GraphNode getStartNode() {
		return method;
	}

	@Override
	public GraphNode getEndNode() {
		return field;
	}

	@Override
	public String getLabel() {
		return "<" + method.getLabel() + ", " + field.getLabel() + ">";
	}

	@Override
	public String getDescription() {
		return method.getDescription() + " uses " + field.getDescription();
	}

	@Override
	public String toFullString() {
		return method.getDescription() + " --> " + field.getDescription();
	}
	
}
