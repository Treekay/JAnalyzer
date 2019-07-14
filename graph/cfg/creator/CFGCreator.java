package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import nameTable.NameTableASTBridge;
import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameScope.CompilationUnitScope;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeLocation;
import sourceCodeAST.SourceCodeLocationASTVisitor;

/**
 * Create CFG for java source code file
 * @author Zhou Xiaocong
 * @since 2012/12/29
 * @version 1.0
 * 
 * @update 2013/12/30 Zhou Xiaocong
 * 		Add a method ControlFlowGraph create(MethodDeclaration methodDec, String className) for creating CFG for a given method!
 * 		Add a method ASTNode matchASTNode(ExecutionPoint point) to match the AST node for an execution point.
 * 			Add a class MatchExecutionPointASTVisitor to support matching AST node for an execution point.
 * 
 * @update 2017/1/1 Zhou Xiaocong
 * 		Add a public static method ControlFlowGraph create(NameTableManager nameTable, MethodDefinition method) for creating CFG 
 * 			for a given method definition in the given NameTableManager!
 * 		Add a class MatchExecutionPointASTVisitor to support matching AST node for an execution point by giving method definition and name table.
 *
 */
public class CFGCreator {
	private String sourceFileName = null;
	private CompilationUnit astRoot = null;

	public CFGCreator(String sourceFileName, CompilationUnit astRoot) {
		this.sourceFileName = sourceFileName;
		this.astRoot = astRoot;
	}

	/**
	 * <p>After give the AST root and the file name of the compilation unit, create CFG for the file.
	 * Note that the source file name is used to generate the source code location objects for CFG nodes, and
	 * the AST root node is used to find the row and column number of the AST node corresponding to a CFG node.
	 * 
	 * <p>Moreover, we only create CFG for the methods defined in those class declared in the root of the package, 
	 * that is, we do not create CFG for the methods defined in those member classes, local classes, interfaces, and
	 * anonymous classes 
	 */
	@SuppressWarnings("unchecked")
	public List<ControlFlowGraph> create() {
		List<ControlFlowGraph> resultCFGList = new ArrayList<ControlFlowGraph>();
		if (astRoot == null || sourceFileName == null) return resultCFGList;
		
		List<AbstractTypeDeclaration> typeList = astRoot.types();
		for (AbstractTypeDeclaration type : typeList) {
			if (type.getNodeType() != ASTNode.TYPE_DECLARATION) continue;
			
			TypeDeclaration classDeclaration = (TypeDeclaration)type;
			if (classDeclaration.isInterface()) continue;
			if (!classDeclaration.isPackageMemberTypeDeclaration()) continue;
			
			String className = classDeclaration.getName().getIdentifier();
			// We only create CFG for the methods defined in those class declared in the root of the package!
			MethodDeclaration[] methodDeclarationArray = classDeclaration.getMethods();
			for (MethodDeclaration methodDeclaration : methodDeclarationArray) {
				String methodName = methodDeclaration.getName().getIdentifier();
				String id = className + "." + methodName + "@" + SourceCodeLocation.getStartLocation(methodDeclaration, astRoot, sourceFileName);
				String label = className + "." + methodName;
				String description = label;

				Block methodBody = methodDeclaration.getBody();
				if (methodBody == null) {
					// This method is an abstract method, we can not create CFG for it
					continue;
				}
				
				// Create a ControFlowGraph object
				ControlFlowGraph currentCFG = new ControlFlowGraph(id, label, description);
				currentCFG.setCompilationUnitRecorder(sourceFileName, astRoot);
				ExecutionPointFactory factory = new ExecutionPointFactory(currentCFG.getCompilationUnitRecorder());
				currentCFG.setExecutionPointFactory(factory);
				currentCFG.setMethod(className, methodName, methodDeclaration);
				
				// Create the start node for the CFG of the method
				ExecutionPoint startNode = factory.createStart(methodDeclaration);
				currentCFG.setAndAddStartNode(startNode);
				
				// Create a precede node list precedeNodeList, which only contains the node startNode
				List<PossiblePrecedeNode> precedeNodeList = new LinkedList<PossiblePrecedeNode>();
				precedeNodeList.add(new PossiblePrecedeNode(startNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));

				// Create CFG for the body of the method and get new precedeNodeList
				StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(methodBody);
				precedeNodeList = creator.create(currentCFG, methodBody, precedeNodeList, null);
				
				// Create end and abnormal end node for the entire method
				ExecutionPoint endNode = factory.createEnd(methodDeclaration);
				currentCFG.setAndAddEndNode(endNode);
				
				ExecutionPoint abnormalEndNode = null;
				// Traverse precedeNodeList, for each precedeNode in the list, if it is a PPR_RETURN or PPR_SEQUENCE, add edge <precedeNode, endNode>,
				// if it is a PPR_THROW, create abnormalEndNode, and add edge <precedeNode, abnormalEndNode>
				for (PossiblePrecedeNode precedeNode : precedeNodeList) {
					PossiblePrecedeReasonType reason = precedeNode.getReason();
					String edgeLabel = precedeNode.getLabel();
					if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE || reason == PossiblePrecedeReasonType.PPR_RETURN) {
						currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), endNode, edgeLabel));
					} else if (reason == PossiblePrecedeReasonType.PPR_THROW) {
						if (abnormalEndNode == null) {
							abnormalEndNode = factory.createAbnormalEnd(methodDeclaration);
							currentCFG.setAndAddAbnormalEndNode(abnormalEndNode);
						}
						currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), abnormalEndNode, edgeLabel));
					} else {
						assert false : "After create CFG for the entire method, there are unexpected precede nodes in the precedeNodeList";
					} 
				}
				
				// Add the currentCFG to the resultCFGList
				resultCFGList.add(currentCFG);
			}
		}
		return resultCFGList;
	}
	
	/**
	 * create CFG for a given method! Note that the caller should give the name (i.e. the parameter className) of the class defined the method!
	 */
	public ControlFlowGraph create(MethodDeclaration methodDeclaration, String className) {
		String methodName = methodDeclaration.getName().getIdentifier();
		String id = className + "." + methodName + "@" + SourceCodeLocation.getStartLocation(methodDeclaration, astRoot, sourceFileName);
		String label = className + "." + methodName;
		String description = label;

		Block methodBody = methodDeclaration.getBody();
		if (methodBody == null) return null;
		
		// Create a ControFlowGraph object
		ControlFlowGraph currentCFG = new ControlFlowGraph(id, label, description);
		currentCFG.setCompilationUnitRecorder(sourceFileName, astRoot);
		ExecutionPointFactory factory = new ExecutionPointFactory(currentCFG.getCompilationUnitRecorder());
		currentCFG.setExecutionPointFactory(factory);
		currentCFG.setMethod(className, methodName, methodDeclaration);
		
		// Create the start node for the CFG of the method
		ExecutionPoint startNode = factory.createStart(methodDeclaration);
		currentCFG.setAndAddStartNode(startNode);
		
		// Create a precede node list precedeNodeList, which only contains the node startNode
		List<PossiblePrecedeNode> precedeNodeList = new LinkedList<PossiblePrecedeNode>();
		precedeNodeList.add(new PossiblePrecedeNode(startNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));

		// Create CFG for the body of the method and get new precedeNodeList
		StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(methodBody);
		precedeNodeList = creator.create(currentCFG, methodBody, precedeNodeList, null);
		
		// Create end and abnormal end node for the entire method
		ExecutionPoint endNode = factory.createEnd(methodDeclaration);
		currentCFG.setAndAddEndNode(endNode);
		
		ExecutionPoint abnormalEndNode = null;
		// Traverse precedeNodeList, for each precedeNode in the list, if it is a PPR_RETURN or PPR_SEQUENCE, add edge <precedeNode, endNode>,
		// if it is a PPR_THROW, create abnormalEndNode, and add edge <precedeNode, abnormalEndNode>
		for (PossiblePrecedeNode precedeNode : precedeNodeList) {
			PossiblePrecedeReasonType reason = precedeNode.getReason();
			String edgeLabel = precedeNode.getLabel();
			if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE || reason == PossiblePrecedeReasonType.PPR_RETURN) {
				currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), endNode, edgeLabel));
			} else if (reason == PossiblePrecedeReasonType.PPR_THROW) {
				if (abnormalEndNode == null) {
					abnormalEndNode = factory.createAbnormalEnd(methodDeclaration);
					currentCFG.setAndAddAbnormalEndNode(abnormalEndNode);
				}
				currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), abnormalEndNode, edgeLabel));
			} else {
				throw new AssertionError("After create CFG for the entire method, there are unexpected precede nodes in the precedeNodeList");
			} 
		}
		
		return currentCFG;
	}

	/**
	 * create CFG for a given body of statements! The caller should given the id, label, and description of the created CFG. 
	 * The description is also used as the label of the start and end node of the CFG. 
	 */
	public ControlFlowGraph create(Block body, String id, String label, String description) {
		// Create a ControFlowGraph object
		ControlFlowGraph currentCFG = new ControlFlowGraph(id, label, description);
		currentCFG.setCompilationUnitRecorder(sourceFileName, astRoot);
		ExecutionPointFactory factory = new ExecutionPointFactory(currentCFG.getCompilationUnitRecorder());
		currentCFG.setExecutionPointFactory(factory);
		currentCFG.setMethod(id, label, null);
		
		// Create the start node for the CFG of the method
		ExecutionPoint startNode = factory.createStart(body, description);
		currentCFG.setAndAddStartNode(startNode);
		
		// Create a precede node list precedeNodeList, which only contains the node startNode
		List<PossiblePrecedeNode> precedeNodeList = new LinkedList<PossiblePrecedeNode>();
		precedeNodeList.add(new PossiblePrecedeNode(startNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));

		// Create CFG for the body of the method and get new precedeNodeList
		StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(body);
		precedeNodeList = creator.create(currentCFG, body, precedeNodeList, null);
		
		// Create end and abnormal end node for the entire method
		ExecutionPoint endNode = factory.createEnd(body, description);
		currentCFG.setAndAddEndNode(endNode);
		
		ExecutionPoint abnormalEndNode = null;
		// Traverse precedeNodeList, for each precedeNode in the list, if it is a PPR_RETURN or PPR_SEQUENCE, add edge <precedeNode, endNode>,
		// if it is a PPR_THROW, create abnormalEndNode, and add edge <precedeNode, abnormalEndNode>
		for (PossiblePrecedeNode precedeNode : precedeNodeList) {
			PossiblePrecedeReasonType reason = precedeNode.getReason();
			String edgeLabel = precedeNode.getLabel();
			if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE || reason == PossiblePrecedeReasonType.PPR_RETURN) {
				currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), endNode, edgeLabel));
			} else if (reason == PossiblePrecedeReasonType.PPR_THROW) {
				if (abnormalEndNode == null) {
					abnormalEndNode = factory.createAbnormalEnd(body, description);
					currentCFG.setAndAddAbnormalEndNode(abnormalEndNode);
				}
				currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), abnormalEndNode, edgeLabel));
			} else {
				throw new AssertionError("After create CFG for the entire method, there are unexpected precede nodes in the precedeNodeList");
			} 
		}
		
		return currentCFG;
	}
	
	/**
	 * Match an AST node for a given execution point without using the AST node stored in the point, since we may restore a CFG from 
	 * a file which can not save the AST node of the execution points in the future, and then we may need match the execution point with
	 * an abstract syntax tree build by the same source file.  
	 */
	public ASTNode matchASTNode(ExecutionPoint point) {
		SourceCodeLocation start = point.getStartLocation();
		SourceCodeLocation end = point.getEndLocation();
		
		SourceCodeLocationASTVisitor visitor = new SourceCodeLocationASTVisitor(astRoot, start, end);
		astRoot.accept(visitor);
		ASTNode result = visitor.getMatchedNode();
		
		return result;
	}
	
	/**
	 * Match an AST node for a given execution point without using the AST node stored in the point, since we may restore a CFG from 
	 * a file which can not save the AST node of the execution points in the future, and then we may need match the execution point with
	 * an abstract syntax tree build by the same source file.  
	 */
	public static ASTNode matchASTNode(NameTableManager nameTable, MethodDefinition method, ExecutionPoint point) {
		CompilationUnitScope unitScope = nameTable.getEnclosingCompilationUnitScope(method);
		if (unitScope == null) return null;
		
		String sourceFileName = unitScope.getUnitName();
		CompilationUnit astRoot = nameTable.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(sourceFileName);
		if (astRoot == null) return null;

		SourceCodeLocation start = point.getStartLocation();
		SourceCodeLocation end = point.getEndLocation();
		
		SourceCodeLocationASTVisitor visitor = new SourceCodeLocationASTVisitor(astRoot, start, end);
		astRoot.accept(visitor);
		ASTNode result = visitor.getMatchedNode();
		
		return result;
	}

	/**
	 * Create control flow graph for a method by given an object of MethodDefinition and its NameTableManager
	 */
	public static ControlFlowGraph create(NameTableManager nameTable, MethodDefinition method) {
		CompilationUnitScope unitScope = nameTable.getEnclosingCompilationUnitScope(method);
		if (unitScope == null) return null;
		
		String sourceFileName = unitScope.getUnitName();
		CompilationUnit astRoot = nameTable.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(sourceFileName);
		if (astRoot == null) return null;
		
		CompilationUnitRecorder unitRecorder = new CompilationUnitRecorder(sourceFileName, astRoot);
		
		return create(nameTable, unitRecorder, method);
	}

	/**
	 * Create control flow graph for a method by given an object of MethodDefinition and its NameTableManager
	 */
	public static ControlFlowGraph create(NameTableManager nameTable, CompilationUnitRecorder unitRecorder, MethodDefinition method) {
		NameTableASTBridge bridge = new NameTableASTBridge(nameTable);
		MethodDeclaration methodDeclaration = bridge.findASTNodeForMethodDefinition(method);
		if (methodDeclaration == null) return null;
		
		DetailedTypeDefinition type = nameTable.getEnclosingDetailedTypeDefinition(method);
		
		String id = method.getUniqueId();
		String label = method.getFullQualifiedName();
		String description = label;

		Block methodBody = methodDeclaration.getBody();
		if (methodBody == null) return null;
		
		// Create a ControFlowGraph object
		ControlFlowGraph currentCFG = new ControlFlowGraph(id, label, description);
		currentCFG.setCompilationUnitRecorder(unitRecorder);
		ExecutionPointFactory factory = new ExecutionPointFactory(currentCFG.getCompilationUnitRecorder());
		currentCFG.setExecutionPointFactory(factory);
		currentCFG.setMethod(type.getFullQualifiedName(), method.getSimpleName(), methodDeclaration);
		
		// Create the start node for the CFG of the method
		ExecutionPoint startNode = factory.createStart(methodDeclaration);
		currentCFG.setAndAddStartNode(startNode);
		
		// Create a precede node list precedeNodeList, which only contains the node startNode
		List<PossiblePrecedeNode> precedeNodeList = new LinkedList<PossiblePrecedeNode>();
		precedeNodeList.add(new PossiblePrecedeNode(startNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));

		// Create CFG for the body of the method and get new precedeNodeList
		StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(methodBody);
		precedeNodeList = creator.create(currentCFG, methodBody, precedeNodeList, null);
		
		// Create end and abnormal end node for the entire method
		ExecutionPoint abnormalEndNode = null;
		ExecutionPoint endNode = null;
		// Traverse precedeNodeList, for each precedeNode in the list, if it is a PPR_RETURN or PPR_SEQUENCE, add edge <precedeNode, endNode>,
		// if it is a PPR_THROW, create abnormalEndNode, and add edge <precedeNode, abnormalEndNode>
		for (PossiblePrecedeNode precedeNode : precedeNodeList) {
			PossiblePrecedeReasonType reason = precedeNode.getReason();
			String edgeLabel = precedeNode.getLabel();
			if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE || reason == PossiblePrecedeReasonType.PPR_RETURN) {
				if (endNode == null) {
					endNode = factory.createEnd(methodDeclaration);
					currentCFG.setAndAddEndNode(endNode);
				}
				currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), endNode, edgeLabel));
			} else if (reason == PossiblePrecedeReasonType.PPR_THROW) {
				if (abnormalEndNode == null) {
					abnormalEndNode = factory.createAbnormalEnd(methodDeclaration);
					currentCFG.setAndAddAbnormalEndNode(abnormalEndNode);
				}
				currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), abnormalEndNode, edgeLabel));
			} else {
				System.out.println("Node " + precedeNode.getNode().getId() + ", resaon = " + reason);
				throw new AssertionError("After create CFG for the entire method, there are unexpected precede nodes in the precedeNodeList");
			} 
		}
		
		return currentCFG;
	}
}

