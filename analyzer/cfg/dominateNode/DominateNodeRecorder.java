package analyzer.cfg.dominateNode;

import java.util.List;

import graph.basic.GraphNode;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê9ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class DominateNodeRecorder implements IDominateNodeRecorder {
	List<GraphNode> dominateNodeList = null; // Record those nodes which dominate the current node!

	public DominateNodeRecorder() {
	}

	public void setDominateNodeList(List<GraphNode> nodeSet) {
		dominateNodeList = nodeSet;
	}
	
	public List<GraphNode> getDominateNodeList() {
		return dominateNodeList;
	}
}
