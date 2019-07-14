package graph.cfg.creator;

import graph.cfg.ExecutionPoint;
import graph.cfg.ExecutionPointLabel;
import graph.cfg.ExecutionPointType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeLocation;

/**
 * The factory class for creating execution point.
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 *
 */
public class ExecutionPointFactory {
	protected CompilationUnitRecorder unitRecorder = null; 
	
	public ExecutionPointFactory(CompilationUnitRecorder recorder) {
		this.unitRecorder = recorder;
	}
	
	/**
	 * Set the compilation unit for this factory class. Before calling any other methods of this class, this method should be call first! 
	 * @param fileName : The name of the file to create this AST tree root (i.e. the compilation unit node root)
	 * @param root : The compilation unit node for creating execution points of current CFG.
	 */
	public void resetCompilationUnitRecorder(CompilationUnitRecorder recorder) {
		this.unitRecorder = recorder;
	}
	
	/**
	 * Call different methods according the type of statement to create an execution point for an AssertStatement
	 */
	public ExecutionPoint create(Statement astNode) {
		if (astNode.getNodeType() == ASTNode.EXPRESSION_STATEMENT) return create((ExpressionStatement)astNode);
		else if (astNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) return create((VariableDeclarationStatement)astNode);
		else if (astNode.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION) return create((ConstructorInvocation)astNode);
		else if (astNode.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION) return create((SuperConstructorInvocation)astNode);
		else if (astNode.getNodeType() == ASTNode.ASSERT_STATEMENT) return create((AssertStatement)astNode);
		else if (astNode.getNodeType() == ASTNode.TYPE_DECLARATION_STATEMENT) return create((TypeDeclarationStatement)astNode);
		else {
			// The statement type should be the above types. So the following assertion should never be executed. 
			throw new AssertionError("Meet unexpected statement type when creating execution point for statement: " + astNode);
		}
	}

	/**
	 * Create an execution point for an AssertStatement
	 */
	public ExecutionPoint create(AssertStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.ASSERTION;
		String description = astNode.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create an execution point for a ConstructorInvocation
	 */
	public ExecutionPoint create(ConstructorInvocation astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.CONSTRUCTOR_INVOCATION;
		String description = astNode.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Call create(Expression) to create an execution point for a ExpressionStatement
	 */
	public ExecutionPoint create(ExpressionStatement astNode) {
		Expression expression = astNode.getExpression();
		return create(expression);
	}
	
	/**
	 * Create an execution point for a SuperConstructorInvocation
	 */
	public ExecutionPoint create(SuperConstructorInvocation astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.SUPER_CONSTRUCTOR_INVOCATION;
		String description = astNode.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create an execution point for a TypeDeclarationStatement. 
	 * We DO NOT create CFG for those methods defined in a TypeDeclarationStatement which is in a method body.
	 */
	public ExecutionPoint create(TypeDeclarationStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.TYPE_DECLARATION;
		String description = astNode.getDeclaration().getName().getIdentifier(); // The description is the type name declared by this statement
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create an execution point for a VariableDeclarationStatement
	 */
	public ExecutionPoint create(VariableDeclarationStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.VARIABLE_DECLARATION;
		String description = StatementCFGCreatorHelper.astNodeToString(astNode);
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create an execution point for an Expression. So far we do not check there are method calls in the expression. Possibly we need
	 * more detailed implementation for creating an execution point for an expression in near future, since an expression may has 
	 * complex structures
	 */
	public ExecutionPoint create(Expression astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.EXPRESSION;
		String description = astNode.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create a predicate execution point for enhanced for statement.
	 */
	public ExecutionPoint createPredicate(EnhancedForStatement node) {
		SingleVariableDeclaration variable = node.getParameter();
		Expression exp = node.getExpression();
		
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(variable, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(exp, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.ENHANCED_FOR_PREDICATE;
		String description = variable.getName().getIdentifier() + " : " + exp.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.LOOP_PREDICATE, node);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a predicate execution point for if statement.
	 */
	public ExecutionPoint createPredicate(IfStatement node) {
		Expression condExp = node.getExpression();
		String label = ExecutionPointLabel.IF_PREDICATE;
		ExecutionPointType type = ExecutionPointType.BRANCH_PREDICATE;
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(condExp, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(condExp, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String description = condExp.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, type, condExp);
		value.setStartLocation(startLocation); 
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a predicate execution point for switch statement.
	 */
	public ExecutionPoint createPredicate(SwitchStatement node) {
		Expression condExp = node.getExpression();
		String label = ExecutionPointLabel.SWITCH_PREDICATE;
		ExecutionPointType type = ExecutionPointType.BRANCH_PREDICATE;
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(condExp, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(condExp, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String description = condExp.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, type, condExp);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create a predicate execution point for do statement.
	 */
	public ExecutionPoint createPredicate(DoStatement node) {
		Expression condExp = node.getExpression();
		String label = ExecutionPointLabel.DO_WHILE_PREDICATE;
		ExecutionPointType type = ExecutionPointType.LOOP_PREDICATE;
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(condExp, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(condExp, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String description = condExp.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, type, condExp);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a predicate execution point for while statement.
	 */
	public ExecutionPoint createPredicate(WhileStatement node) {
		Expression condExp = node.getExpression();
		String label = ExecutionPointLabel.WHILE_PREDICATE;
		ExecutionPointType type = ExecutionPointType.LOOP_PREDICATE;
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(condExp, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(condExp, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String description = condExp.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, type, condExp);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a predicate execution point for for statement.
	 */
	public ExecutionPoint createPredicate(ForStatement node) {
		String label = ExecutionPointLabel.FOR_PREDICATE;
		ExecutionPointType type = ExecutionPointType.LOOP_PREDICATE;
		Expression condExp = node.getExpression();
		String id = null;
		String description = null;
		SourceCodeLocation startLocation = null;
		SourceCodeLocation endLocation = null;
		if (condExp != null) {
			startLocation = SourceCodeLocation.getStartLocation(condExp, unitRecorder.root, unitRecorder.unitName);
			endLocation = SourceCodeLocation.getEndLocation(condExp, unitRecorder.root, unitRecorder.unitName);
			id = startLocation.toString();
			description = condExp.toString();
		} else {
			// The for statement may have not condition expression, such as for (;;) {...}
			// In this case, we use the start position of the for statement as the start position of its condition expression
			startLocation = SourceCodeLocation.getStartLocation(node, unitRecorder.root, unitRecorder.unitName);
			endLocation = SourceCodeLocation.getEndLocation(node, unitRecorder.root, unitRecorder.unitName);
			id = startLocation.toString();
			description = "true";
		}
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, type, condExp);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for do statement
	 */
	public ExecutionPoint createVirtualStart(DoStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.DO_START;
		String description = label;

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.GROUP_START, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create a virtual start node for try block statement
	 */
	public ExecutionPoint createVirtualStart(TryStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.TRY_BLOCK_START;
		String description = label;

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.GROUP_START, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for try block statement
	 */
	public ExecutionPoint createCatchClauseStart(TryStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.CATCH_CLAUSE_START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.CATCH_CLAUSE_START;
		String description = label;

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.GROUP_START, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create a virtual start node for labeled statement
	 */
	public ExecutionPoint createVirtualStart(LabeledStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.LABEL_START + astNode.getLabel().getIdentifier();
		String description = label;

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.GROUP_START, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for synchronize statement
	 */
	public ExecutionPoint createVirtualStart(SynchronizedStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.SYNCHRONIZE_START;
		String description = astNode.getExpression().toString();

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.GROUP_START, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for switch statement
	 */
	public ExecutionPoint createVirtualEnd(SwitchStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.SWITCH_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create a virtual start node for if statement
	 */
	public ExecutionPoint createVirtualEnd(IfStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.IF_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for do statement
	 */
	public ExecutionPoint createVirtualEnd(DoStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.DO_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for while statement
	 */
	public ExecutionPoint createVirtualEnd(WhileStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.WHILE_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for for statement
	 */
	public ExecutionPoint createVirtualEnd(ForStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.FOR_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for enhanced for statement
	 */
	public ExecutionPoint createVirtualEnd(EnhancedForStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.ENHANCED_FOR_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for try block statement
	 */
	public ExecutionPoint createVirtualEnd(TryStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.TRY_BLOCK_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for try block statement
	 */
	public ExecutionPoint createCatchClauseEnd(TryStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.CATCH_CLAUSE_END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.CATCH_CLAUSE_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create a virtual start node for labeled statement
	 */
	public ExecutionPoint createVirtualEnd(LabeledStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.LABEL_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for synchronized statement
	 */
	public ExecutionPoint createVirtualEnd(SynchronizedStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.SYNCHRONIZE_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for the finally block of try statement
	 */
	public ExecutionPoint createFinallyStart(TryStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.FINALLY_START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.FINALLY_START;
		
		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_START, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create a virtual end node for the finally block of try statement, this node is also the end node of the entire try statement!
	 */
	public ExecutionPoint createFinallyEnd(TryStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.TRY_END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.TRY_END;
		
		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create node for break statement
	 */
	public ExecutionPoint createFlowControlNode(BreakStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.BREAK_LABEL;
		if (astNode.getLabel() != null) label = label + astNode.getLabel().getIdentifier();
		String description = label;

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.FLOW_CONTROLLER, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create node for continue statement
	 */
	public ExecutionPoint createFlowControlNode(ContinueStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.CONTINUE_LABEL;
		if (astNode.getLabel() != null) label = label + astNode.getLabel().getIdentifier();
		String description = label;

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.FLOW_CONTROLLER, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create node for return statement
	 */
	public ExecutionPoint createFlowControlNode(ReturnStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.RETURN_LABEL;
		String description = StatementCFGCreatorHelper.astNodeToString(astNode);

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.FLOW_CONTROLLER, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}

	/**
	 * Create node for return statement
	 */
	public ExecutionPoint createFlowControlNode(ThrowStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.THROW_LABEL;
		String description = astNode.toString();

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.FLOW_CONTROLLER, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create the start node for the entire method
	 */
	public ExecutionPoint createStart(MethodDeclaration astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.START;
		String description = astNode.getName().getIdentifier();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.CFG_START, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create the end node for the entire method
	 */
	public ExecutionPoint createEnd(MethodDeclaration astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.END;
		String description = astNode.getName().getIdentifier();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.CFG_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create the abnormal end node for the entire method
	 */
	public ExecutionPoint createAbnormalEnd(MethodDeclaration astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.ABNORMAL_END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.ABNORMAL_END;
		String description = astNode.getName().getIdentifier();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.CFG_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create the start node for the entire method
	 */
	public ExecutionPoint createStart(Block astNode, String description) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.START;
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.CFG_START, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create the end node for the entire method
	 */
	public ExecutionPoint createEnd(Block astNode, String description) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.END;
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.CFG_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
	
	/**
	 * Create the abnormal end node for the entire method
	 */
	public ExecutionPoint createAbnormalEnd(Block astNode, String description) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, unitRecorder.root, unitRecorder.unitName);
		String id = ExecutionPointLabel.ABNORMAL_END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.ABNORMAL_END;
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.CFG_END, astNode);
		value.setStartLocation(startLocation);
		value.setEndLocation(endLocation);
		return value;
	}
}
