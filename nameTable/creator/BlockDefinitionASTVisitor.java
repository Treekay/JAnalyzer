package nameTable.creator;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NameQualifiedType;
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
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeLocation;

/**
 * A block visitor for creating all name definitions, while ignoring name references as far as possible
 *  
 * @author Zhou Xiaocong
 * @since 2013-4-12
 * @version 1.0
 * 
 * @update 2016/11/11
 * 		Refactor the class according to the design document
 */
public class BlockDefinitionASTVisitor extends BlockASTVisitor {

	public BlockDefinitionASTVisitor(NameTableCreator creator, CompilationUnitRecorder unitFile, NameScope currentScope) {
		super(creator, unitFile, currentScope);
		expressionVisitor = new ExpressionDefinitionASTVisitor(creator, unitFile, currentScope);
	}

	/**
	 * ArrayAccess: Expression[Expression]
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ArrayAccess node) {
		return true;
	}

	/**
	 * ArrayCreation: new Type[Expression] ArrayInitializer
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ArrayCreation node) {
		return true;
	}

	/**
	 * ArrayInitializer: { Expression, Expression, .. }
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ArrayInitializer node) {
		return true;
	}

	/**
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ArrayType node) {
		return false;
	}

	/**
	 * Assignment: Expression AssignmentOperator Expression
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(Assignment node) {
		return true;
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
	 * CastExpression: (Type) Expression
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(CastExpression node) {
		return true;
	}


	/**
	 * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
	 * If there is a anonymous class declaration, we use the creator to scan it! 
	 */
	public boolean visit(ClassInstanceCreation node) {
		NameScope currentScope = scopeStack.getTop();

		// Visit the type of the node
		Type type = node.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();

		// Visit the argument list of the node, since anonymous classes can be declared in the arguments! 
		@SuppressWarnings("unchecked")
		List<Expression> arguments = node.arguments();
		for (Expression arg : arguments) arg.accept(this);

		// Process the anonymous class declaration in the node!
		AnonymousClassDeclaration anonymousClass = node.getAnonymousClassDeclaration();
		if (anonymousClass != null) {
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
			creator.scan(unitFile, qualifier, anonymousClass, currentScope, typeRef);
		}
		
		return false;
	}

	/**
	 * ConditionalExpression: Expression ? Expression : Expression
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ConditionalExpression node) {
		return true;
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

		// Define the parameter declared in the enhanced for statement to the current scope. Note that the current scope 
		// will be the new scope if the new scope have been created!
		String variableName = variable.getName().getIdentifier();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(variable.getName(), unitFile.root, unitFile.unitName);
		VariableDefinition variableDef = new VariableDefinition(variableName, variableName, location, currentScope);
		variableDef.setDefinitionKind(NameDefinitionKind.NDK_VARIABLE);
		variableDef.setType(typeRef);
		currentScope.define(variableDef);
		
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
	 * ExpressionStatement Expression ;
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ExpressionStatement node) {
		NameScope currentScope = scopeStack.getTop();
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		return false;
	}

	/**
	 * FieldAccess: Expression.Identifier ;
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(FieldAccess node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not occur in a block
	 */
	public boolean visit(FieldDeclaration node) {
		return true;
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

		// Visit the initializers of the for statement for possible variable definition in these expressions
		List<Expression> initializers = node.initializers();
		for (Expression expression : initializers) {
			expressionVisitor.reset(currentScope);
			expression.accept(expressionVisitor);
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
			scopeStack.pop();
		}
		
		return false;
	}

	/**
	 * Expression InfixOperator Expression { InfixOperator Expression }
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(InfixExpression node) {
		return true;
	}

	/**
	 * InstanceofExpression: Expression instanceof Type
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(InstanceofExpression node) {
		return true;
	}

	/**
	 * There is no name definition in this kind of AST type node, ignore the node!
	 */
	public boolean visit(IntersectionType node) {
		return false;
	}
	
	/**
	 * [ Expression . ] [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(MethodInvocation node) {
		return true;
	}

	/**
	 * There is no name definition in this kind of AST type node, ignore the node! 
	 */
	public boolean visit(NameQualifiedType node) {
		return false;
	}
	
	/**
	 * ParameterizedType: Type < Type { , Type } >
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ParameterizedType node) {
		return false;
	}

	/**
	 * Use the expression visitor to visit the expression in the node directly
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ParenthesizedExpression node) {
		return true;
	}

	/**
	 * Use the expression visitor to visit the node
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(PostfixExpression node) {
		return true;
	}

	/**
	 * Use the expression visitor to visit the node
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(PrefixExpression node) {
		return true;
	}

	/**
	 * Use the type visitor to visit the node
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(PrimitiveType node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(QualifiedName node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(QualifiedType node) {
		return false;
	}

	/**
	 * ReturnStatement: return [Expression]
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ReturnStatement node) {
		return true;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(SimpleName node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(SimpleType node) {
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
		
		// Define the variable to the current scope
		creator.defineVariable(unitFile, node, typeRef, currentScope);
		
		// Visit the initializer in the variable declaration
		Expression initializer = node.getInitializer();
		if (initializer != null) {
			expressionVisitor.reset(currentScope);
			initializer.accept(expressionVisitor);
		}
		return false;
	}

	/**
	 * SuperFieldAccess: [ ClassName . ] super . Identifier
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(SuperFieldAccess node) {
		return true;
	}

	/**
	 * SuperMethodInvocation: [ ClassName . ] super .  [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(SuperMethodInvocation node) {
		return true;
	}

	/**
	 * ThisExpression : [ClassName.] this
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ThisExpression node) {
		return true;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(TypeLiteral node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST type node, ignore the node! 
	 */
	public boolean visit(UnionType node) {
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
			}
		}
		return false;
	}
	
	
	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(WildcardType node) {
		return false;
	}
}
