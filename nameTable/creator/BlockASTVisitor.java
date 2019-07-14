package nameTable.creator;

import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeLocation;
import util.Stack;

/**
 * Visit a block and statements in the block, generating all definitions and references in the block
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2016/11/12
 * 		Refactor the class according to the design document
 */
public class BlockASTVisitor extends ASTVisitor {
	protected NameTableCreator creator = null;
	protected CompilationUnitRecorder unitFile = null;

	protected Stack<NameScope> scopeStack = null;
	
	protected TypeASTVisitor typeVisitor = null;
	protected ExpressionASTVisitor expressionVisitor = null;
	
	public BlockASTVisitor(NameTableCreator creator, CompilationUnitRecorder unitFile, NameScope currentScope) {
		this.creator = creator;
		this.unitFile = unitFile;
		
		scopeStack = new Stack<NameScope>();
		if (currentScope != null) {
			scopeStack.push(currentScope);
		}
		
		typeVisitor = new TypeASTVisitor(unitFile, currentScope);
		expressionVisitor = new ExpressionASTVisitor(creator, unitFile, currentScope);
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(AnnotationTypeDeclaration node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return false;
	}

	/**
	 * We process anonymous class declaration when visit Expression node ClassInstanceCreation
	 * And then we can not see such node here any more!
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}

	/**
	 * ArrayAccess: Expression[Expression]
	 * Use the expression visitor to visit the node, and add the result reference to the creator 
	 */
	public boolean visit(ArrayAccess node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * ArrayCreation: new Type[Expression] ArrayInitializer
	 * Use the expression visitor to visit the node, and add the result reference to the creator
	 */
	public boolean visit(ArrayCreation node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * ArrayInitializer: { Expression, Expression, .. }
	 * Use the expression visitor to visit the node, and add the result reference to the creator
	 */
	public boolean visit(ArrayInitializer node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * Use the TypeASTVisitor to visit the array type node
	 */
	public boolean visit(ArrayType node) {
		NameScope currentScope = scopeStack.getTop();
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		return false;
	}

	/**
	 * AssertStatement: assert Expression: Expression
	 * Only need to visit its children
	 */
	public boolean visit(AssertStatement node) {
		return true;
	}

	/**
	 * Assignment: Expression AssignmentOperator Expression
	 * Use the expression visitor to visit the node, and add the result reference to the creator
	 */
	public boolean visit(Assignment node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * Check if the block need to create a local scope, if it need to create a local scope, then create a new scope and 
	 * push it to the scopeStack, and then visit its children. So the scope of its children is the new scope. After visiting 
	 * all children, we pop the new scope.
	 */
	public boolean visit(Block node) {
		// Check if the block need to create a local scope, if it need to create a local scope, then create a new scope and 
		// push it to the scopeStack
		boolean createScope = creator.needCreateLocalScope(node);
		if (createScope) {
			NameScope currentScope = scopeStack.getTop();
			SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
			SourceCodeLocation end = SourceCodeLocation.getEndLocation(node, unitFile.root, unitFile.unitName);
			NameScope newLocalScope = creator.createLocalScope(start, end, currentScope);
			scopeStack.push(newLocalScope);
		}
		
		// Then visit all children of the block
		@SuppressWarnings("unchecked")
		List<Statement> statementList = node.statements();
		for (Statement statement : statementList) statement.accept(this);
		
		// Pop the new scope created by the block, since the scope of the following AST node is the original current scope
		if (createScope) {
			scopeStack.pop();
		}
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(BlockComment node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(BooleanLiteral node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(BreakStatement node) {
		return false;
	}

	/**
	 * CastExpression: (Type) Expression
	 * Use the expression visitor to visit the node, and add the result reference to the creator
	 */
	public boolean visit(CastExpression node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * CatchClause:  catch ( FormalParameter ) Block
	 * 
	 * Create a new local scope for body block statement of the catch clause, and then define the exception declared in 
	 * the catch clause to the new scope, push the scope to stack, and then visit the block statement of the catch clause, 
	 * after that, pop the scope
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(CatchClause node) {
		if (scopeStack.isEmpty()) throw new AssertionError("Get null scope for node: " + node);
		
		// Create a new local scope for the body of the catch clause
		NameScope currentScope = scopeStack.getTop();
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		SourceCodeLocation end = SourceCodeLocation.getEndLocation(node, unitFile.root, unitFile.unitName);
		NameScope newLocalScope = creator.createLocalScope(start, end, currentScope);
		
		SingleVariableDeclaration exception = node.getException();
		// Get the type reference of the exception
		Type type = exception.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);

		// Define the exception declared in the clause to the new scope
		String exceptionName = exception.getName().getIdentifier();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(exception.getName(), unitFile.root, unitFile.unitName);
		VariableDefinition exceptionDef = new VariableDefinition(exceptionName, exceptionName, location, newLocalScope);
		exceptionDef.setDefinitionKind(NameDefinitionKind.NDK_VARIABLE);
		exceptionDef.setType(typeRef);
		newLocalScope.define(exceptionDef);
		
		// Push the new scope to the scope stack, and visit the body block statement of the catch clause
		// System.out.println("In catch clause " + creator.getStartPosition(node).getLineNumber() + ", Push scope: " + newLocalScope.getScopeName());
		scopeStack.push(newLocalScope);

		// Then visit all statements of the block, we can not visit the block directly, because if do so then we 
		// may create a local scope for the block more than once. 
		Block block = node.getBody();
		List<Statement> statementList = block.statements();
		for (Statement statement : statementList) statement.accept(this);

		scopeStack.pop();
		
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(CharacterLiteral node) {
		return false;
	}

	/**
	 * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
	 * Use the expression visitor to visit the node, and add the result reference to the current scope
	 */
	public boolean visit(ClassInstanceCreation node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not occur in a block node
	 */
	public boolean visit(CompilationUnit node) {
		return false;
	}

	/**
	 * ConditionalExpression: Expression ? Expression : Expression
	 * Use the expression visitor to visit the node, and add the result reference to the creator
	 */
	public boolean visit(ConditionalExpression node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * ConstructorInvocation: [ < Type { , Type } > ] this ( [ Expression { , Expression } ] ) ; 
	 * Only need to visit its children, we do not to match the corresponding constructors
	 */
	public boolean visit(ConstructorInvocation node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ContinueStatement node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(CreationReference node) {
		return false;
	}
	
	/**
	 * Ignore this kind of AST node so far. We use type visitor to proccess the dimension of array type!
	 */
	public boolean visit(Dimension node) {
		return false;
	}

	/**
	 * DoStatement: do Statement while ( Expression ) ;
	 * Only need to visit its children
	 */
	public boolean visit(DoStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(EmptyStatement node) {
		return false;
	}

	/**
	 * EnhancedForStatement: for ( FormalParameter : Expression ) Statement
	 * 
	 * Create a new local scope for body block statement of the statement, and then define the exception declared in 
	 * the catch clause to the new scope, push the scope to stack, and then visit the block statement of the catch clause, 
	 * after that, pop the scope
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(EnhancedForStatement node) {
		NameScope currentScope = scopeStack.getTop();
		boolean createNewScope = false;
		Statement body = node.getBody();
		if (body != null && body.getNodeType() == ASTNode.BLOCK) {
			// Only when the body of the enhanced for statement is a block statement, we create a new scope for this block
			createNewScope = true;
			// Create a new local scope for the body of the statement
			SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
			SourceCodeLocation end = SourceCodeLocation.getEndLocation(node, unitFile.root, unitFile.unitName);
			NameScope newLocalScope = creator.createLocalScope(start, end, currentScope);
			// Push the new scope to the scope stack, and visit the body block statement of the catch clause
			// System.out.println("In enhanced for " + creator.getStartPosition(node).getLineNumber() + ", Push scope: " + newLocalScope.getScopeName());
			scopeStack.push(newLocalScope);
			// The parameter declared in the enhanced for statement should be defined in the new scope
			currentScope = newLocalScope; 
		} // else we define the parameter in the currentScope
		
		SingleVariableDeclaration variable = node.getParameter();
		// Get the type reference of the parameter
		Type type = variable.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);

		// Define the parameter declared in the enhanced for statement to the current scope. Note that the current scope 
		// will be the new scope if the new scope have been created!
		String variableName = variable.getName().getIdentifier();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(variable.getName(), unitFile.root, unitFile.unitName);
		VariableDefinition variableDef = new VariableDefinition(variableName, variableName, location, currentScope);
		variableDef.setDefinitionKind(NameDefinitionKind.NDK_VARIABLE);
		variableDef.setType(typeRef);
		currentScope.define(variableDef);
		
		// Visit the expression of the statement
		Expression expression = node.getExpression();
		expressionVisitor.reset(currentScope);
		expression.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);

		// Visit the body of the statement
		if (body != null && body.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block)body;
			// Then visit all statements of the block, we can not visit the block directly, because if do so then we 
			// may create a local scope for the block more than once. 
			List<Statement> statementList = block.statements();
			for (Statement statement : statementList) statement.accept(this);
		} else if (body != null) {
			// We use the current visitor to visit the body directly!
			body.accept(this);
		}

		if (createNewScope) {
			// System.out.println("In enhanced for " + creator.getStartPosition(node).getLineNumber() + ", pop scope: " + scopeStack.getTop().getScopeName());
			scopeStack.pop();
		}
		
		return false;
	}

	/**
	 * Ignore this kind of AST node, since it can not occur in a block!
	 */
	public boolean visit(EnumConstantDeclaration node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node, since it can not occur in a block (in Java, enumeration can not be defined locally!
	 */
	public boolean visit(EnumDeclaration node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far!
	 */
	public boolean visit(ExpressionMethodReference node) {
		return false;
	}
	
	/**
	 * ExpressionStatement Expression ;
	 * Use the expression visitor to visit the expression in the statement
	 */
	public boolean visit(ExpressionStatement node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * FieldAccess: Expression.Identifier ;
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(FieldAccess node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not occur in a block
	 */
	public boolean visit(FieldDeclaration node) {
		return false;
	}

	/**
	 * Check whether we need to create a local scope for a for statement
	 */
	@SuppressWarnings("unchecked")
	protected boolean createScopeForForStatement(ForStatement node) {
		if (node.getBody() == null) return false;
		if (node.getBody().getNodeType() != ASTNode.BLOCK) return false; 
		Block body = (Block)node.getBody();
		if (creator.needCreateLocalScope(body)) return true;
		
		List<Expression> initializers = node.initializers();
		for (Expression expression : initializers) {
			if (expression.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION) return true;
		}
		return false;
	}
	
	/**
	 * If there are variable declaration in the initialize expression of the statement, and the body is 
	 * a block, then create a local scope for the body, and visit the children of the statement in the appropriate 
	 * name scope
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(ForStatement node) {
		NameScope currentScope = scopeStack.getTop();
		boolean createNewScope = createScopeForForStatement(node);
		Statement body = node.getBody();
		if (createNewScope) {
			// When createNewScope == true, the body of the for statement must be a block statement, 

			// Create a new local scope for the body of the for statement, its scope area is from the start location of the for statement to 
			// the end location of the for statement 
			SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
			SourceCodeLocation end = SourceCodeLocation.getEndLocation(node, unitFile.root, unitFile.unitName);
			NameScope newLocalScope = creator.createLocalScope(start, end, currentScope);
			// Push the new scope to the scope stack, and visit the body block statement of the catch clause
			scopeStack.push(newLocalScope);
			// The variable declared in the for statement should be defined in the new scope
			currentScope = newLocalScope; 
		}

		// Visit the initializers of the for statement
		List<Expression> initializers = node.initializers();
		for (Expression expression : initializers) {
			expressionVisitor.reset(currentScope);
			expression.accept(expressionVisitor);
			currentScope.addReference(expressionVisitor.getResult());
		}
		
		// Visit the condition expression of the for statement
		Expression condExpression = node.getExpression();
		if (condExpression != null) {
			expressionVisitor.reset(currentScope);
			condExpression.accept(expressionVisitor);
			currentScope.addReference(expressionVisitor.getResult());
		}
		
		// Visit the updaters of the for statement
		List<Expression> updaters = node.updaters();
		for (Expression expression : updaters) {
			expressionVisitor.reset(currentScope);
			expression.accept(expressionVisitor);
			currentScope.addReference(expressionVisitor.getResult());
		}
		
		// Visit the body of the statement
		if (body != null && body.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block)body;
			// Then visit all statements of the block, we can not visit the block directly, because if do so then we 
			// may create a local scope for the block more than once. 
			List<Statement> statementList = block.statements();
			for (Statement statement : statementList) statement.accept(this);
		} else if (body != null) {
			// We use the current visitor to visit the body directly!
			body.accept(this);
		}
		
		if (createNewScope) {
			// System.out.println("In for " + creator.getStartPosition(node).getLineNumber() + ", pop scope: " + scopeStack.getTop().getScopeName());
			scopeStack.pop();
		}
		
		return false;
	}

	/**
	 * Only need to visit its children
	 */
	public boolean visit(IfStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not occur in a block
	 */
	public boolean visit(ImportDeclaration node) {
		return false;
	}

	/**
	 * Expression InfixOperator Expression { InfixOperator Expression }
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(InfixExpression node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * InstanceofExpression: Expression instanceof Type
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(InstanceofExpression node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not occur in a block
	 */
	public boolean visit(Initializer node) {
		return true;
	}

	/**
	 * Use the TypeASTVisitor to visit this type node
	 */
	public boolean visit(IntersectionType node) {
		NameScope currentScope = scopeStack.getTop();
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		return false;
	}
	
	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(Javadoc node) {
		// visit tag elements inside doc comments only if requested
		return false;
	}

	/**
	 * We do not create the reference for the label, so we just visit its children
	 */
	public boolean visit(LabeledStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(LambdaExpression node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(LineComment node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(MarkerAnnotation node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(MemberRef node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(MemberValuePair node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(MethodRef node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(MethodRefParameter node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far, since it can not occur in a block
	 */
	public boolean visit(MethodDeclaration node) {
		return false;
	}

	/**
	 * [ Expression . ] [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(MethodInvocation node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(Modifier node) {
		return false;
	}

	/**
	 * Use the TypeASTVisitor to visit this type node
	 */
	public boolean visit(NameQualifiedType node) {
		NameScope currentScope = scopeStack.getTop();
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(NormalAnnotation node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(NullLiteral node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(NumberLiteral node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far. It can not occur in a block
	 */
	public boolean visit(PackageDeclaration node) {
		return false;
	}
	
	/**
	 * ParameterizedType: Type < Type { , Type } >
	 * Use the type visitor to visit the node
	 */
	public boolean visit(ParameterizedType node) {
		NameScope currentScope = scopeStack.getTop();
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		return false;
	}

	/**
	 * Use the expression visitor to visit the expression in the node directly
	 */
	public boolean visit(ParenthesizedExpression node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		
		// We directly visit the expression in the node
		Expression expression = node.getExpression();
		expression.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(PostfixExpression node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(PrefixExpression node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * Use the type visitor to visit the node
	 */
	public boolean visit(PrimitiveType node) {
		NameScope currentScope = scopeStack.getTop();
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		return false;
	}

	/**
	 * If the QualifiedName occurs in the block, we assume it is in an expression, so we use expression visitor 
	 * to visit the node
	 */
	public boolean visit(QualifiedName node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * Use the type visitor to visit the node
	 */
	public boolean visit(QualifiedType node) {
		NameScope currentScope = scopeStack.getTop();
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		return false;
	}

	/**
	 * ReturnStatement: return [Expression]
	 * Use the expression visitor to visit the possible expression
	 */
	public boolean visit(ReturnStatement node) {
		Expression expression = node.getExpression();
		if (expression != null) {
			NameScope currentScope = scopeStack.getTop();
			expressionVisitor.reset(currentScope);
			
			expression.accept(expressionVisitor);
			NameReference result = expressionVisitor.getResult();
			currentScope.addReference(result);
		}
		return false;
	}

	/**
	 * When a simpleName occurs in block directly, we assume it is a variable reference 
	 * and use the expression visitor to visit the node
	 */
	public boolean visit(SimpleName node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * Use the type visitor to visit the node
	 */
	public boolean visit(SimpleType node) {
		NameScope currentScope = scopeStack.getTop();
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SingleMemberAnnotation node) {
		return false;
	}

	/**
	 * SingleVariableDeclaration: { ExtendedModifier } Type [ ... ] Identifier { [] } [ = Expression ]
	 * Define the variable to the scope, and visit the initializer of the declaration
	 */
	public boolean visit(SingleVariableDeclaration node) {
		NameScope currentScope = scopeStack.getTop();
		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		
		// Define the variable to the current scope
		creator.defineVariable(unitFile, node, typeRef, currentScope);
		
		// Visit the initializer in the variable declaration
		Expression initializer = node.getInitializer();
		if (initializer != null) {
			expressionVisitor.reset(currentScope);
			initializer.accept(expressionVisitor);
			NameReference initExpRef = expressionVisitor.getResult();
			currentScope.addReference(initExpRef);
		}
		
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(StringLiteral node) {
		return false;
	}

	/**
	 * SuperConstructorInvocation:  [ Expression . ] [ < Type { , Type } > ]  super ( [ Expression { , Expression } ] ) ;
	 * Only need to visit its children, we do not to match the corresponding constructors
	 */
	public boolean visit(SuperConstructorInvocation node) {
		return true;
	}

	/**
	 * SuperFieldAccess: [ ClassName . ] super . Identifier
	 * Use the expression to visit the node
	 */
	public boolean visit(SuperFieldAccess node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * SuperMethodInvocation: [ ClassName . ] super .  [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * Use the expression to visit the node
	 */
	public boolean visit(SuperMethodInvocation node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SuperMethodReference node) {
		return false;
	}
	
	/**
	 * Only need to visit its children
	 */
	public boolean visit(SwitchCase node) {
		return true;
	}

	/**
	 * Only need to visit its children
	 */
	public boolean visit(SwitchStatement node) {
		return true;
	}

	/**
	 * Only need to visit its children
	 */
	public boolean visit(SynchronizedStatement node) {
		return true;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(TagElement node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(TextElement node) {
		return false;
	}

	/**
	 * ThisExpression : [ClassName.] this
	 * Create a type reference for the class name in the expression
	 */
	public boolean visit(ThisExpression node) {
		NameScope currentScope = scopeStack.getTop();
		// Create a type reference for class name in the node
		Name classNameNode = node.getQualifier();
		if (classNameNode != null) {
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(classNameNode, unitFile.root, unitFile.unitName);
			String className = classNameNode.getFullyQualifiedName();
			TypeReference classRef = new TypeReference(className, location, currentScope);
			currentScope.addReference(classRef);
		}
		return false;
	}

	/**
	 * Only need to visit its children
	 */
	public boolean visit(ThrowStatement node) {
		return true;
	}

	/**
	 * Only need to visit its children
	 */
	public boolean visit(TryStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not occur in a block directly
	 */
	public boolean visit(TypeDeclaration node) {
		return false;
	}

	/**
	 * Use NameTableCreator.scan() to create the name definitions and references in the local type
	 */
	public boolean visit(TypeDeclarationStatement node) {
		NameScope currentScope = scopeStack.getTop();
		String qualifier = currentScope.getScopeName();
		// Find the full qualified name of the enclosing method or type as the name qualifier of this anonymous class.
		NameScope betterScope = currentScope;
		while (betterScope.getEnclosingScope() != null) {
			if (betterScope.getScopeKind() == NameScopeKind.NSK_METHOD) {
				qualifier = ((MethodDefinition)betterScope).getFullQualifiedName();
				break;
			} else if (betterScope.getScopeKind() == NameScopeKind.NSK_DETAILED_TYPE) {
				qualifier = ((DetailedTypeDefinition)betterScope).getFullQualifiedName();
				break;
			}
			betterScope = betterScope.getEnclosingScope();
		}
		int nodeType = node.getDeclaration().getNodeType();
		if (nodeType == ASTNode.TYPE_DECLARATION) {
			TypeDeclaration type = (TypeDeclaration)node.getDeclaration();
			creator.scan(unitFile, qualifier, type, currentScope);
		} else if (nodeType == ASTNode.ENUM_DECLARATION) {
			EnumDeclaration enumType = (EnumDeclaration)node.getDeclaration();
			creator.scan(unitFile, qualifier, enumType, currentScope);
		}
		return false;
	}

	/**
	 * Use the type visitor to visit the type in the node
	 */
	public boolean visit(TypeLiteral node) {
		NameScope currentScope = scopeStack.getTop();
		typeVisitor.reset(currentScope);
		node.getType().accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(TypeMethodReference node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not be accessed from a block directly
	 */
	public boolean visit(TypeParameter node) {
		return false;
	}

	/**
	 * Use the type visitor to visit this node
	 */
	public boolean visit(UnionType node) {
		NameScope currentScope = scopeStack.getTop();
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		return false;
	}
	
	/**
	 * VariableDeclarationExpression: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(VariableDeclarationExpression node) {
		NameScope currentScope = scopeStack.getTop();
		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		
		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Define the variable to the current scope
			creator.defineVariable(unitFile, varNode, typeRef, currentScope);
			
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				expressionVisitor.reset(currentScope);
				initializer.accept(expressionVisitor);
				NameReference initExpRef = expressionVisitor.getResult();
				currentScope.addReference(initExpRef);
			}
		}
		return false;
	}

	/**
	 * VariableDeclarationStatement: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(VariableDeclarationStatement node) {
		NameScope currentScope = scopeStack.getTop();
		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		
		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Define the variable to the current scope
			creator.defineVariable(unitFile, varNode, typeRef, currentScope);
			
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				expressionVisitor.reset(currentScope);
				initializer.accept(expressionVisitor);
				NameReference initExpRef = expressionVisitor.getResult();
				currentScope.addReference(initExpRef);
			}
		}
		return false;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not be accessed from a block directly
	 */
	public boolean visit(VariableDeclarationFragment node) {
		return false;
	}

	/**
	 * Only need to visit its children
	 */
	public boolean visit(WhileStatement node) {
		return true;
	}

	/**
	 * Use the type visitor to visit the node
	 */
	public boolean visit(WildcardType node) {
		NameScope currentScope = scopeStack.getTop();
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		return false;
	}
}
