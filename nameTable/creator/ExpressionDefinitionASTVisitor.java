package nameTable.creator;

import java.util.List;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import sourceCodeAST.CompilationUnitRecorder;

/**
 * The expression visitor for creating all name definitions, while ignoring name references as far as possible
 * @author Zhou Xiaocong
 * @since 2013-4-12
 * @version 1.0
 *
 * @update 2016/11/12
 * 		Refactor the class according to the design document
 */
public class ExpressionDefinitionASTVisitor extends ExpressionASTVisitor {

	public ExpressionDefinitionASTVisitor(NameTableCreator creator, CompilationUnitRecorder unitFile, NameScope scope) {
		super(creator, unitFile, scope);
	}

	public NameReference getResult() {
		return null;
	}

	/**
	 * ArrayAccess: Expression[Expression]
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	@Override
	public boolean visit(ArrayAccess node) {
		return true;
	}
	
	/**
	 * ArrayCreation: new Type[Expression] ArrayInitializer
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	public boolean visit(ArrayCreation node) {
		return true;
	}
	
	/**
	 * ArrayInitializer: { Expression, Expression, .. }
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	public boolean visit(ArrayInitializer node) {
		return true;
	}

	/**
	 * Assignment: Expression AssignmentOperator Expression
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	public boolean visit(Assignment node) {
		return true;
	}
	
	/**
	 * CastExpression: (Type) Expression
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	public boolean visit(CastExpression node) {
		return true;
	}
	
	/**
	 * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
	 * Process the anonymous class and its member!
	 */
	public boolean visit(ClassInstanceCreation node) {
		// Visit the type of the node
		Type type = node.getType();
		typeVisitor.reset(scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();

		// Visit the argument list of the node, since anonymous classes can be declared in the arguments! 
		@SuppressWarnings("unchecked")
		List<Expression> arguments = node.arguments();
		for (Expression arg : arguments) arg.accept(this);
		
		// Process the anonymous class declaration in the node!
		AnonymousClassDeclaration anonymousClass = node.getAnonymousClassDeclaration();
		if (anonymousClass != null) {
			String qualifier = scope.getScopeName();
			// Find the full qualified name of the enclosing method or type as the name qualifier of this anonymous class.
			NameScope betterScope = scope;
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
			creator.scan(unitFile, qualifier, anonymousClass, scope, typeRef);
		}
		return false;
	}

	/**
	 * ConditionalExpression: Expression ? Expression : Expression
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	public boolean visit(ConditionalExpression node) {
		return true;
	}

	/**
	 * FieldAccess: Expression.Identifier ;
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	public boolean visit(FieldAccess node) {
		return true;
	}
	
	/**
	 * Expression InfixOperator Expression { InfixOperator Expression }
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	public boolean visit(InfixExpression node) {
		return true;
	}
	
	/**
	 * InstanceofExpression: Expression instanceof Type
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	public boolean visit(InstanceofExpression node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(LambdaExpression node) {
		return false;
	}

	/**
	 * [ Expression . ] [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	public boolean visit(MethodInvocation node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(org.eclipse.jdt.core.dom.MethodReference node) {
		return false;
	}
	
	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(CreationReference node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ExpressionMethodReference node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SuperMethodReference node) {
		return false;
	}
	
	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(TypeMethodReference node) {
		return false;
	}
	
	/**
	 * PostfixExpression: Expression PostfixOperator
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(PostfixExpression node) {
		return false;
	}

	/**
	 * PrefixExpression: PrefixOperator Expression 
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(PrefixExpression node) {
		return false;
	}
	
	/**
	 * Create a variable reference for the fully qualified name of the node
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(QualifiedName node) {
		return false;
	}
	
	/**
	 * Create a variable reference for the fully qualified name of the node
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(SimpleName node) {
		return false;
	}
	
	/**
	 * SuperFieldAccess: [ ClassName . ] super . Identifier
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	public boolean visit(SuperFieldAccess node) {
		return true;
	}

	/**
	 * SuperMethodInvocation: [ ClassName . ] super .  [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * Since an anonymous class can be declared in this node, so we must visit its subnode!  
	 */
	public boolean visit(SuperMethodInvocation node) {
		return true;
	}

	/**
	 * ThisExpression : [ClassName.] this
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ThisExpression node) {
		return false;
	}
	
	/**
	 * VariableDeclarationExpression: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * <p>VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(VariableDeclarationExpression node) {
		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		
		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Define the variable to the current scope
			creator.defineVariable(unitFile, varNode, typeRef, scope);

			// Visit the initializer in the variable declaration, since anonymous classes can be declared in an initializer expressions
			Expression initializer = varNode.getInitializer();
			if (initializer != null) initializer.accept(this);
		}

		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(BooleanLiteral node) {
		return false;
	}

	
	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(CharacterLiteral node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(NullLiteral node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(NumberLiteral node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(StringLiteral node) {
		return false;
	}

	/**
	 * TypeLiteral: ( Type | void ) . class
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(TypeLiteral node) {
		return false;
	}
}
