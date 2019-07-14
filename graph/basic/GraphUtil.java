package graph.basic;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/29
 * @version 1.0
 */
public class GraphUtil {

	public static int getConnectedComponentNumber(int[][] adjacentMatrix) {
		int length = adjacentMatrix.length;
		if (length != adjacentMatrix[0].length) return 0;
		
		int result = 0;
		boolean[] visited = new boolean[length]; 
		for (int i = 0; i < length; i++) visited[i] = false;
		
		for (int i = 0; i < length; i++) {
			if (visited[i] == false) {
				result = result + 1;
				deepthSearchFirstTravel(i, adjacentMatrix, visited);
			}
		}
		return result;
	}
	
	private static void deepthSearchFirstTravel(int startNode, int[][] adjacentMatrix, boolean[] visited) {
		visited[startNode] = true;
		for (int i = 0; i < visited.length; i++) {
			if (adjacentMatrix[startNode][i] == 1 && visited[i] == false) {
				deepthSearchFirstTravel(i, adjacentMatrix, visited);
			}
		}
	}

	/**
	 * Return the generated sub-graph by the node center and those node with less than or equal to the given distance from or to the node center 
	 */
	public static AbstractGraph extractSubGraph(AbstractGraph graph, GraphNode center, int distance) {
		List<GraphNode> nodes = graph.getAllNodes();
		if (nodes == null) return null;
		if (!nodes.contains(center)) return null;
		
		List<GraphNode> nodeList = new ArrayList<GraphNode>();
		LinkedList<GraphNodeWithDistance> nodeQueue = new LinkedList<GraphNodeWithDistance>();
		
		nodeList.add(center);
		nodeQueue.add(new GraphNodeWithDistance(center, 0));
		while (!nodeQueue.isEmpty()) {
			GraphNodeWithDistance first = nodeQueue.pollFirst();
			boolean include = true;
			if (distance > 0 && first.distance >= distance) include = false;
			
			if (include == true) {
				List<GraphNode> adjacents = graph.adjacentNodes(first.node);
				for (GraphNode adjacentNode : adjacents) {
					if (!nodeList.contains(adjacentNode)) {
						nodeList.add(adjacentNode);
						nodeQueue.addLast(new GraphNodeWithDistance(adjacentNode, first.distance+1));
					}
				}
			}
		}
		AbstractGraph result = graph.getGeneratedSubGraph(nodeList);
		return result;
	}

	/**
	 * Return the generated sub-graph by the node center and those node with less than or equal to the given distance from the node center 
	 */
	public static AbstractGraph extractSubGraphFromCenter(AbstractGraph graph, GraphNode center, int distance) {
		List<GraphNode> nodes = graph.getAllNodes();
		if (nodes == null) return null;
		if (!nodes.contains(center)) return null;
		
		List<GraphNode> nodeList = new ArrayList<GraphNode>();
		LinkedList<GraphNodeWithDistance> nodeQueue = new LinkedList<GraphNodeWithDistance>();
		
		nodeList.add(center);
		nodeQueue.add(new GraphNodeWithDistance(center, 0));
		while (!nodeQueue.isEmpty()) {
			GraphNodeWithDistance first = nodeQueue.pollFirst();
			boolean include = true;
			if (distance > 0 && first.distance >= distance) include = false;
			
			if (include == true) {
				List<GraphNode> adjacents = graph.adjacentFromNode(first.node);
				for (GraphNode adjacentNode : adjacents) {
					if (!nodeList.contains(adjacentNode)) {
						nodeList.add(adjacentNode);
						nodeQueue.addLast(new GraphNodeWithDistance(adjacentNode, first.distance+1));
					}
				}
			}
		}
		AbstractGraph result = graph.getGeneratedSubGraph(nodeList);
		return result;
	}

	/**
	 * Return the generated sub-graph by the node center and those node with less than or equal to the given distance to the node center 
	 */
	public static AbstractGraph extractSubGraphToCenter(AbstractGraph graph, GraphNode center, int distance) {
		List<GraphNode> nodes = graph.getAllNodes();
		if (nodes == null) return null;
		if (!nodes.contains(center)) return null;
		
		List<GraphNode> nodeList = new ArrayList<GraphNode>();
		LinkedList<GraphNodeWithDistance> nodeQueue = new LinkedList<GraphNodeWithDistance>();
		
		nodeList.add(center);
		nodeQueue.add(new GraphNodeWithDistance(center, 0));
		while (!nodeQueue.isEmpty()) {
			GraphNodeWithDistance first = nodeQueue.pollFirst();
			boolean include = true;
			if (distance > 0 && first.distance >= distance) include = false;
			
			if (include == true) {
				List<GraphNode> adjacents = graph.adjacentToNode(first.node);
				for (GraphNode adjacentNode : adjacents) {
					if (!nodeList.contains(adjacentNode)) {
						nodeList.add(adjacentNode);
						nodeQueue.addLast(new GraphNodeWithDistance(adjacentNode, first.distance+1));
					}
				}
			}
		}
		AbstractGraph result = graph.getGeneratedSubGraph(nodeList);
		return result;
	}

	/**
	 * Write the (directed) graph to a text file, which can be regarded as the description of the graph 
	 * in dot language, and can be used to visualized the graph use Graphviz tools.
	 * @param out : the output text file, which should be opened
	 */
	public static void simplyWriteToDotFile(AbstractGraph graph, PrintWriter output) throws IOException {
		simplyWriteToDotFile(graph, output, true);
	}

	/**
	 * Write the (directed) graph to a text file, which can be regarded as the description of the graph 
	 * in dot language, and can be used to visualized the graph use Graphviz tools.
	 * @param out : the output text file, which should be opened
	 */
	public static void simplyWriteToDotFile(AbstractGraph graph, PrintWriter output, boolean writeZeroDegreeNode) throws IOException {
		List<GraphNode> nodes = graph.getAllNodes();
		if (nodes == null) return;
		
		String id = getLegalToken(graph.getId());
		output.println("digraph " + id + " {");
		for (GraphNode node : nodes) {
			if (!writeZeroDegreeNode) {
				if (graph.getDegree(node) < 1) continue;
			}
			String label = node.getLabel();
			id = "node_" + getLegalToken(node.getId());
			if (label.length() > 20) label = label.substring(0, 20) + "...";
			label = label.replace("\\", "/");
			output.println("    " + id + "[label = \"" + label + "\"]");
		}
		
		List<GraphEdge> edges = graph.getEdges();
		if (edges != null) {
			for (GraphEdge edge : edges) {
				String label = edge.getLabel();
	
				String startNodeId = "node_" + getLegalToken(edge.getStartNode().getId());
				String endNodeId = "node_" + getLegalToken(edge.getEndNode().getId());
				
				if (label != null && label.length() < 5) {
					output.println("    " + startNodeId + "->" + endNodeId + "[label = \"" + label + "\"]");
				} else {
					output.println("    " + startNodeId + "->" + endNodeId);
				}
			}
		}
	
		output.println("};");
		output.println();
		output.flush();
	}

	/**
	 * Write the (directed) graph to a text file, which can be regarded as the description of the graph 
	 * in the format defined by complex network analysis tool Pajek.
	 * @param out : the output text file, which should be opened
	 */
	public static void simplyWriteToNetFile(AbstractGraph graph, PrintWriter output) throws IOException {
		List<GraphNode> nodes = graph.getAllNodes();
		if (nodes == null) return;
		
		output.println("*Vertices " + nodes.size());
		for (GraphNode node : nodes) {
			String label = node.getLabel();
			int index = nodes.indexOf(node) + 1;
			output.println(index + " \"" + label + "\"");
		}

		List<GraphEdge> edges = graph.getEdges();
		if (edges != null) {
			output.println("*Arcs");
			
			for (GraphEdge edge : edges) {
				int startIndex = nodes.indexOf(edge.getStartNode()) + 1;
				int endIndex = nodes.indexOf(edge.getEndNode()) + 1;
				output.println(startIndex + " " + endIndex);
			}
		}
	
		output.println();
		output.flush();
	}
	

	/**
	 * Get a legal identifier (in dot language) from a identifier of graph, node, or edge 
	 */
	public static String getLegalToken(String id) {
		StringBuilder token = new StringBuilder("");
		for (int index = 0; index < id.length(); index++) {
			char ch = id.charAt(index);
			if (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ('0' <= ch && ch <= '9')) token.append(ch);
			else token.append('_');
		}
		return token.toString();
	}
}

class GraphNodeWithDistance {
	GraphNode node = null;
	int distance = 0;
	
	protected GraphNodeWithDistance(GraphNode node, int distance) {
		this.node = node;
		this.distance = distance;
	}
	
	public boolean equals(Object otherNode) {
		if (this == otherNode) return true;
		
		if (otherNode instanceof GraphNodeWithDistance) {
			GraphNodeWithDistance temp = (GraphNodeWithDistance)otherNode;
			return temp.node.equals(this.node);
		} else if (otherNode instanceof GraphNode) {
			GraphNode temp = (GraphNode)otherNode;
			return temp.equals(this.node);
		} else return false;
	}
}
