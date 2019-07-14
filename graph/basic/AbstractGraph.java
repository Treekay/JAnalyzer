package graph.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * The abstract class of the graph. It implements some common methods for graph. The class select the 
 * 		data structures for storing a graph, and all implementations depend on these data structures,
 * @author Zhou Xiaocong
 * @since 2012/12/26
 * @version 1.1
 * @update 2013/03/29 Zhou Xiaocong
 * 		Add methods getAllNodes(), getAllEdegs() 
 * @update 2013/06/12 Zhou Xiaocong
 * 		Add methods setNodes(), setEdges(), simplyWriteToDotFile(), getLegalToken()
 * @update 2013/09/13 Zhou Xiaocong
 *      (1) Add method boolean hasEdges(GraphNode, GraphNode);
 * 		(2) Correct many bugs in the methods which can not correctly deal with the case when the fields nodes or edges is null!
 *      (3) Add method List<GraphNode> adjacentNodes(GraphNode); 
 * @update 2014/1/4
 * 		Add methods to calculate the degree of node 
 * @update 2014/1/26
 * 		Modify this abstract class to general class
 */
public class AbstractGraph {
	protected List<GraphNode> nodes = null;
	protected List<GraphEdge> edges = null;
	
	protected String id = null;
	
	public AbstractGraph(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	/**
	 * Set all nodes of the graph. Copy the node in the array list to the graph, so the client can modify the 
	 * parameter (e.g. remove element, or add element) after call this method.
	 * @param nodes: the array list of all nodes
	 */
	public void setAllNodes(ArrayList<GraphNode> allNodes) {
		this.nodes = new ArrayList<GraphNode>(allNodes.size());
		for (GraphNode node: allNodes) this.nodes.add(node);
	}
	
	/**
	 * Set all edges of the graph. Copy the node in the array list to the graph, so the client can modify the 
	 * parameter (e.g. remove element, or add element) after call this method.
	 * @param nodes: the array list of all edges
	 */
	public void setAllEdges(ArrayList<GraphEdge> allEdges) {
		this.edges = new ArrayList<GraphEdge>(allEdges.size());
		for (GraphEdge node: allEdges) this.edges.add(node);
	}

	/**
	 * Add a node to the graph. if the node has been in the graph yet, the method dose not add it to the graph.
	 * That is, if there is a node(graphNode) in the graph such that graphNode.equals(node) == true, the node 
	 * has been in the node.  
	 */
	public void addNode(GraphNode node) {
		if (nodes == null) nodes = new ArrayList<GraphNode>(10);
		if (nodes.contains(node)) return;
		nodes.add(node);
	}

	/**
	 * Add an edge to the graph. The method do not check if the edge has been in the graph.
	 */
	public void addEdge(GraphEdge edge) {
		if (edges == null) edges = new ArrayList<GraphEdge>(10);
		edges.add(edge);
	}
	
	
	/** 
	 * Check if the node is in the graph. Use the method equals() to check two nodes are identity or not.
	 */
	public boolean hasNode(GraphNode node) {
		if (nodes == null) return false;
		if (nodes.contains(node)) return true;
		else return false;
	}

	/** 
	 * Check if the edge is in the graph. Use the method equals() to check two nodes are identity or not.
	 */
	public boolean hasEdge(GraphEdge edge) {
		if (edges == null) return false;
		if (edges.contains(edge)) return true;
		else return false;
	}
	
	/**
	 * Check if the edge between two nodes is in the graph
	 */
	public boolean hasEdge(GraphNode from, GraphNode to) {
		if (edges == null) return false;
		for (GraphEdge edge : edges) {
			if (edge.getStartNode().equals(from) && edge.getEndNode().equals(to)) return true;
		}
		return false;
	}

	/**
	 * Get the label of  the edge between two nodes is in the graph. 
	 */
	public String getEdgeLabel(GraphNode from, GraphNode to) {
		if (edges == null) return null;
		for (GraphEdge edge : edges) {
			if (edge.getStartNode().equals(from) && edge.getEndNode().equals(to)) return edge.getLabel();
		}
		return null;
	}
	
	/**
	 * Find a node by its id
	 */
	public GraphNode findById(String id) {
		if (nodes == null) return null;
		for (GraphNode node: nodes) {
			if (id.equals(node.getId())) return node;
		}
		return null;
	}

	/**
	 * Find a node by its label
	 */
	public GraphNode findByLabel(String label) {
		if (nodes == null) return null;
		for (GraphNode node: nodes) {
			if (label.equals(node.getLabel())) return node;
		}
		return null;
	}
	
	/**
	 * @param node: a fix node may in the graph
	 * @return All nodes that are adjacent to the node, i.e. there is an edge from it to the given node. 
	 */
	public List<GraphNode> adjacentToNode(GraphNode node) {
		ArrayList<GraphNode> result = new ArrayList<GraphNode>();
		if (edges == null) return result;
		for (GraphEdge edge: edges) {
			if (edge.getEndNode().equals(node)) result.add(edge.getStartNode());
		}
		return result;
	}
	
	/**
	 * @param node: a fix node may in the graph
	 * @return All nodes that are adjacent from the node, i.e. there is an edge from the given node to it. 
	 */
	public List<GraphNode> adjacentFromNode(GraphNode node) {
		ArrayList<GraphNode> result = new ArrayList<GraphNode>();
		if (edges == null) return result;
		for (GraphEdge edge: edges) {
			if (edge.getStartNode().equals(node)) result.add(edge.getEndNode());
		}
		return result;
	}
	
	/**
	 * @param node: a fix node may in the graph
	 * @return All nodes that are adjacent from the node or to the node, i.e. there is an edge from or to the given node. 
	 */
	public List<GraphNode> adjacentNodes(GraphNode node) {
		ArrayList<GraphNode> result = new ArrayList<GraphNode>();
		if (edges == null) return result;
		for (GraphEdge edge: edges) {
			if (edge.getStartNode().equals(node)) result.add(edge.getEndNode());
			if (edge.getEndNode().equals(node)) result.add(edge.getStartNode());
		}
		return result;
	}
	
	
	/**
	 * Return the generated sub-graph by given nodes, i.e. the graph composite of the given nodes and all edges associate to these nodes 
	 */
	public AbstractGraph getGeneratedSubGraph(List<GraphNode> nodeList) {
		AbstractGraph result = new AbstractGraph(id);
		
		result.nodes = nodeList;
		result.edges = new ArrayList<GraphEdge>();
		for (GraphEdge edge : edges) {
			GraphNode startNode = edge.getStartNode();
			GraphNode endNode = edge.getEndNode();
			
			if (result.hasNode(startNode) && result.hasNode(endNode)) result.edges.add(edge);
		}
		
		return result;
	}
	
	
	public int getInDegree(GraphNode node) {
		int degree = 0;
		if (edges == null) return degree;
		for (GraphEdge edge: edges) {
			if (edge.getEndNode().equals(node)) degree++;
		}
		return degree;
	}
	
	public int getOutDegree(GraphNode node) {
		int degree = 0;
		if (edges == null) return degree;
		for (GraphEdge edge: edges) {
			if (edge.getStartNode().equals(node)) degree++;
		}
		return degree;
	}
	
	public int getDegree(GraphNode node) {
		int degree = 0;
		if (edges == null) return degree;
		for (GraphEdge edge: edges) {
			if (edge.getStartNode().equals(node) || edge.getEndNode().equals(node)) degree++;
		}
		return degree;
	}

	public int[][] getAdjacentMatrix() {
		int nodeSize = nodes.size();
		if (nodeSize <= 0) return null;
		
		int[][] matrix = new int[nodeSize][nodeSize];
		for (int i = 0; i < nodeSize; i++) 
			for (int j = 0; j < nodeSize; j++) matrix[i][j] = 0;
		
		for (GraphEdge edge : edges) {
			GraphNode startNode = edge.getStartNode();
			GraphNode endNode = edge.getEndNode();
			
			int startIndex = nodes.indexOf(startNode);
			int endIndex = nodes.indexOf(endNode);
			
			matrix[startIndex][endIndex] = 1;
		}
		return matrix;
	}
	
	/**
	 * Print the detailed information (especially for node information) of the graph to a string. 
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Nodes:\n");
		boolean isFirstNode = true;
		for (GraphNode node: nodes) {
			if (isFirstNode) {
				buffer.append(node.toString());
				isFirstNode = false;
			}
			else buffer.append(", " + node.toString());
		}
		
		if (edges == null) return buffer.toString();
		
		for (GraphEdge edge: edges) {
			buffer.append(edge.toString()+"\n");
		}
		buffer.append("\n");
		return buffer.toString();
	}

	/**
	 * Print the detailed information (especially for node information) of the graph to a string. 
	 */
	public String toFullString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Nodes:\n");
		for (GraphNode node: nodes) {
			buffer.append(node.toFullString()+"\n");
		}
		buffer.append("\nEdges: \n");
		for (GraphEdge edge: edges) {
			buffer.append(edge.toString()+"\n");
		}
		buffer.append("\n");
		return buffer.toString();
	}
	
	/**
	 * Return all nodes of the graph
	 */
	public List<GraphNode> getAllNodes() {
		return nodes;
	}

	/**
	 * Set all nodes of the graph
	 */
	public void setNodes(List<GraphNode> nodes) {
		this.nodes = nodes;
	}

	/**
	 * Return all edges of the graph
	 */
	public List<GraphEdge> getEdges() {
		return edges;
	}

	/**
	 * Set all edges of the graph
	 */
	public void setEdges(List<GraphEdge> edges) {
		this.edges = edges;
	}
	
}


