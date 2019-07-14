package graph.cfg;

import org.eclipse.jdt.core.dom.ASTNode;

import sourceCodeAST.SourceCodeLocation;

/**
 * The class of the execution point in the source code. 
 * @author Zhou Xiaocong
 * @since 2012/12/26
 * @version 1.02
 * @update 2013/3/29 Zhou Xiaocong
 * 		Add methods to maintain the end position of the execution point
 * @update 2013/06/12 Zhou Xiaocong
 * 		Add the implementations of the methods isVirtual(), isNormalEnd(), isAbnormalEnd(), isStart(), isPredicate() declared in the interface CFGNode
 * 		Modify the method equals() and hashCode() to include label as a critical field to identify an execution point 
 *
 */
public class ExecutionPoint implements CFGNode, Comparable<ExecutionPoint> {
	protected String id = null;
	protected String label = null;
	protected String description = null;
	protected ExecutionPointType type = ExecutionPointType.NORMAL;
	protected ASTNode astNode = null;					// The AST node corresponding to the execution point
	
	protected SourceCodeLocation startLocation = null;	// the start position in the compilation unit of the execution point
	protected SourceCodeLocation endLocation = null;		// the end position in the compilatin unit of the execution point

	protected IFlowInfoRecorder recorder = null;
	
	public ExecutionPoint(String id, String label, String description, ExecutionPointType type, ASTNode astNode) {
		this.id = id;
		this.label = label;
		this.description = description;
		this.type = type;
		this.astNode = astNode;
	}

	@Override
	public CFGNodeType getCFGNodeType() {
		return CFGNodeType.N_EXECUTION_POINT;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public ExecutionPointType getType() {
		return type;
	}

	public ASTNode getAstNode() {

		// 2017/12/28: We need both the formal parameter and expression to analyze reach name definition, so we must to return the entire node even 
		// if it is an enhanced for predicate
//		if (label.equals(ExecutionPointLabel.ENHANCED_FOR_PREDICATE)) {
//			// Note that we set the entire enhanced for statement to such kind of execution point in ExecutionPointFactory!
//			EnhancedForStatement statement = (EnhancedForStatement)astNode;
//			if (statement != null) return statement.getExpression();
//			else return null;
//		} else return astNode;
		
		return astNode;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setType(ExecutionPointType type) {
		this.type = type;
	}

	public void setAstNode(ASTNode astNode) {
		this.astNode = astNode;
	}
	
	public void setFlowInfoRecorder(IFlowInfoRecorder recorder) {
		this.recorder = recorder;
	}
	
	public IFlowInfoRecorder getFlowInfoRecorder() {
		return recorder;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
//		if (!(other instanceof ExecutionPoint)) return false;
//		ExecutionPoint otherEp = (ExecutionPoint)other;
//		if (id.equals(otherEp.id) && label.equals(otherEp.label)) return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode() * 13 + label.hashCode();
	}
	
	@Override 
	public String toString() {
		return id + "[" + label + "]"; 
	}
	
	public String toFullString() {
		return "Execution Point: " + id + "[" + label + "]" + "\n\tDescription: [" + description + "]"; 
	}

	public void setStartLocation(SourceCodeLocation startLocation) {
		this.startLocation = startLocation;
	}

	public SourceCodeLocation getStartLocation() {
		return startLocation;
	}
	
	public void setEndLocation(SourceCodeLocation endLocation) {
		this.endLocation = endLocation;
	}

	public SourceCodeLocation getEndLocation() {
		return endLocation;
	}
	
	/**
	 * If the execution point represents to a virtual start or end node, return true
	 */
	public boolean isVirtual() {
		return type.isVirtual();
	}
	
	/**
	 * If the execution point represents a predicate in a branch or loop statement
	 */
	public boolean isPredicate() {
		return type.isPredicate();
	}

	/**
	 * If the execution point represents a predicate in an enhanced for statement
	 */
	public boolean isEnhancedForPredicate() {
		if (type.isPredicate() && label.equals(ExecutionPointLabel.ENHANCED_FOR_PREDICATE)) return true;
		else return false;
	}
	
	/**
	 * Test if the node is the start node of the entire CFG
	 */
	public boolean isStart() {
		return type == ExecutionPointType.CFG_START;
	}
	
	/**
	 * Test if the node is the end node of the entire CFG
	 */
	public boolean isNormalEnd() {
		return type == ExecutionPointType.CFG_END && label.equals(ExecutionPointLabel.END);
	}
	
	/**
	 * Test if the node is the abnormal end node of the entire CFG
	 */
	public boolean isAbnormalEnd() {
		return type == ExecutionPointType.CFG_END && label.equals(ExecutionPointLabel.ABNORMAL_END);
	}

	@Override
	public int compareTo(ExecutionPoint other) {
		if (this == other) return 0;
		int startLocationCompareResult = this.startLocation.compareTo(other.startLocation);
		if (!this.isVirtual() && !other.isVirtual()) return startLocationCompareResult;

		if (this.isVirtual()) {
			if (this.type == ExecutionPointType.GROUP_START || this.type == ExecutionPointType.CFG_START) {
				if (startLocationCompareResult != 0) return startLocationCompareResult;
				else return -1;
			} else if (this.type == ExecutionPointType.GROUP_END || this.type == ExecutionPointType.CFG_END) {
				int endLocationCompareResult = this.endLocation.compareTo(other.endLocation);
				if (endLocationCompareResult != 0) return endLocationCompareResult;
				else return 1;
			}
		} else if (other.isVirtual()) {
			if (other.type == ExecutionPointType.GROUP_START || other.type == ExecutionPointType.CFG_START) {
				if (startLocationCompareResult != 0) return startLocationCompareResult;
				else return 1;
			} else if (other.type == ExecutionPointType.GROUP_END || other.type == ExecutionPointType.CFG_END) {
				int endLocationCompareResult = this.endLocation.compareTo(other.endLocation);
				if (endLocationCompareResult != 0) return endLocationCompareResult;
				else return -1;
			}
		} else return startLocationCompareResult;
		return 0;
	}
	
}
