package nameTable.creator;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.NameScope;

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
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
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

/**
 * An ASTVisitor for creating reference in a method, and all the created references are stored in the name table.
 * @author Zhou Xiaocong
 * @since 2015Äê6ÔÂ25ÈÕ
 * @version 1.0
 * 
 * @update 2016/11/11
 * 		Refactor the class according to the design document
 */
public class BlockReferenceASTVisitor extends ASTVisitor {
	protected CompilationUnitRecorder unitFile = null;
	protected NameReferenceCreator creator = null;

	protected LocalScope bodyScope = null;
	protected List<NameReference> resultList = null;
	
	protected TypeASTVisitor typeVisitor = null;
	protected ExpressionReferenceASTVisitor expressionVisitor = null;
	protected boolean createLiteralReference = false;
	
	public BlockReferenceASTVisitor(NameReferenceCreator creator, CompilationUnitRecorder unitFile, LocalScope bodyScope) {
		this.unitFile = unitFile;
		this.creator = creator;
		this.bodyScope = bodyScope;
		
		resultList = new ArrayList<NameReference>();
		typeVisitor = new TypeASTVisitor(unitFile, bodyScope);
		expressionVisitor = new ExpressionReferenceASTVisitor(creator, unitFile, bodyScope);
	}

	public BlockReferenceASTVisitor(NameReferenceCreator creator, String unitName, CompilationUnit root, LocalScope bodyScope) {
		this.unitFile = new CompilationUnitRecorder(unitName, root);
		this.creator = creator;
		this.bodyScope = bodyScope;
		
		resultList = new ArrayList<NameReference>();
		typeVisitor = new TypeASTVisitor(unitFile, bodyScope);
		expressionVisitor = new ExpressionReferenceASTVisitor(creator, unitFile, bodyScope);
	}

	public BlockReferenceASTVisitor(NameReferenceCreator creator, CompilationUnitRecorder unitFile, LocalScope bodyScope, boolean createLiteralReference) {
		this.unitFile = unitFile;
		this.creator = creator;
		this.bodyScope = bodyScope;
		this.createLiteralReference = createLiteralReference;
		
		resultList = new ArrayList<NameReference>();
		typeVisitor = new TypeASTVisitor(unitFile, bodyScope);
		expressionVisitor = new ExpressionReferenceASTVisitor(creator, unitFile, bodyScope, createLiteralReference);
	}

	public BlockReferenceASTVisitor(NameReferenceCreator creator, String unitName, CompilationUnit root, LocalScope bodyScope, boolean createLiteralReference) {
		this.unitFile = new CompilationUnitRecorder(unitName, root);
		this.creator = creator;
		this.bodyScope = bodyScope;
		this.createLiteralReference = createLiteralReference;
		
		resultList = new ArrayList<NameReference>();
		typeVisitor = new TypeASTVisitor(unitFile, bodyScope);
		expressionVisitor = new ExpressionReferenceASTVisitor(creator, unitFile, bodyScope, createLiteralReference);
	}
	
	public void setCreateLiteralReference(boolean flag) {
		this.createLiteralReference = flag;
	}

	public void reset(LocalScope bodyScope) {
		this.bodyScope = bodyScope;

		resultList = new ArrayList<NameReference>();
		typeVisitor.reset(unitFile, bodyScope);
		expressionVisitor.reset(unitFile, bodyScope);
	}
	
	public List<NameReference> getResult() {
		return resultList;
	}

	/**
	 * Get a name scope enclosing a source code location exactly, i.e. the location is in the scope, and 
	 * there is no other scope enclosing the location and is enclosed in the returned scope! 
	 * We search the scope beginning with the startScope, i.e. we search the scope in this scope and its sub-scope
	 */
	protected NameScope getNameScopeOfLocation(SourceCodeLocation location, NameScope startScope) {
		NameScope result = startScope;
		if (!result.containsLocation(location)) return null;
		
		List<NameScope> subscopes = result.getSubScopeList();
		while (subscopes != null) {
			boolean findScope = false;
			// Test if there is a sub-scope enclosing the given location
			for (NameScope scope : subscopes) {
				if (scope.containsLocation(location)) {
					findScope = true; 
					result = scope;
					
					subscopes = result.getSubScopeList();
					
					break;
				} else {
					// Check the next scope!
				}
			}
			if (!findScope) {
				// There is no sub-scope enclosing the given location. 
				return result;
			} // else we continue to test if there is a sub-scope enclosing the given location
		}
		
		// The result scope enclosing the given location and it has not sub-scopes, so return it!
		return result;
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
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}

	/**
	 * ArrayAccess: Expression[Expression]
	 * Use the expression visitor to visit the node, and add the result reference to the creator 
	 */
	public boolean visit(ArrayAccess node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		
		resultList.add(result);
		return false;
	}

	/**
	 * ArrayCreation: new Type[Expression] ArrayInitializer
	 * Use the expression visitor to visit the node, and add the result reference to the creator
	 */
	public boolean visit(ArrayCreation node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		resultList.add(result);
		return false;
	}

	/**
	 * ArrayInitializer: { Expression, Expression, .. }
	 * Use the expression visitor to visit the node, and add the result reference to the creator
	 */
	public boolean visit(ArrayInitializer node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		resultList.add(result);
		return false;
	}

	/**
	 * Use the TypeASTVisitor to visit the array type node
	 */
	public boolean visit(ArrayType node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();

		resultList.add(typeRef);
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
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		resultList.add(result);
		return false;
	}

	/**
	 * Directly go to its children
	 */
	public boolean visit(Block node) {
		return true;
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
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		resultList.add(result);
		return false;
	}

	/**
	 * CatchClause:  catch ( FormalParameter ) Block
	 */
	public boolean visit(CatchClause node) {
		// Goto its children, i.e. visit all statements of the block
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(CharacterLiteral node) {
		return false;
	}

	/**
	 * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
	 * Use the expression visitor to visit the node, and add the result reference to the creator
	 */
	public boolean visit(ClassInstanceCreation node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		LocalScope currentScope = (LocalScope)getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		resultList.add(result);

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
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		if (result != null) resultList.add(result);
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
	public boolean visit(EnhancedForStatement node) {
		// It is OK to goto its children for create reference
		return true;
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
	 * ExpressionStatement Expression ;
	 * Use the expression visitor to visit the expression in the statement
	 */
	public boolean visit(ExpressionStatement node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		if (result != null) resultList.add(result);
		return false;
	}

	/**
	 * FieldAccess: Expression.Identifier ;
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(FieldAccess node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		resultList.add(result);
		return false;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not occur in a block
	 */
	public boolean visit(FieldDeclaration node) {
		return false;
	}

	
	/**
	 */
	public boolean visit(ForStatement node) {
		// It is OK to goto its children for create reference
		return true;
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
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		if (result != null) resultList.add(result);
		return false;
	}

	/**
	 * InstanceofExpression: Expression instanceof Type
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(InstanceofExpression node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		resultList.add(result);
		return false;
	}

	/**
	 * Goto its children directly
	 */
	public boolean visit(Initializer node) {
		return true;
	}

	/**
	 * Use the TypeASTVisitor to visit this type node
	 */
	public boolean visit(IntersectionType node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference result = typeVisitor.getResult();

		resultList.add(result);
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
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		resultList.add(result);
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
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference result = typeVisitor.getResult();
		
		resultList.add(result);
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
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();

		resultList.add(typeRef);
		return false;
	}

	/**
	 * Use the expression visitor to visit the expression in the node directly
	 */
	public boolean visit(ParenthesizedExpression node) {
		// We directly visit the expression in the node
		Expression expression = node.getExpression();
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(expression, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		expression.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		if (result != null) resultList.add(result);
		return false;
	}

	/**
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(PostfixExpression node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		if (result != null) resultList.add(result);
		return false;
	}

	/**
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(PrefixExpression node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);

		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		if (result != null) resultList.add(result);
		return false;
	}

	/**
	 * Use the type visitor to visit the node
	 */
	public boolean visit(PrimitiveType node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();

		resultList.add(typeRef);
		return false;
	}

	/**
	 * If the QualifiedName occurs in the block, we assume it is in an expression, so we use expression visitor 
	 * to visit the node
	 */
	public boolean visit(QualifiedName node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		resultList.add(result);
		return false;
	}

	/**
	 * Use the type visitor to visit the node
	 */
	public boolean visit(QualifiedType node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);

		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();

		resultList.add(typeRef);
		return false;
	}

	/**
	 * ReturnStatement: return [Expression]
	 * Use the expression visitor to visit the possible expression
	 */
	public boolean visit(ReturnStatement node) {
		Expression expression = node.getExpression();
		if (expression != null) {
			SourceCodeLocation start = SourceCodeLocation.getStartLocation(expression, unitFile.root, unitFile.unitName);
			NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
			expressionVisitor.reset(currentScope);
			
			expression.accept(expressionVisitor);
			NameReference result = expressionVisitor.getResult();
			if (result != null) resultList.add(result);
		}
		return false;
	}

	/**
	 * When a simpleName occurs in block directly, we assume it is a variable reference 
	 * and use the expression visitor to visit the node
	 */
	public boolean visit(SimpleName node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		resultList.add(result);
		return false;
	}

	/**
	 * Use the type visitor to visit the node
	 */
	public boolean visit(SimpleType node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);

		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();

		resultList.add(typeRef);
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
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);

		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		resultList.add(typeRef);
		
		// Visit the initializer in the variable declaration
		Expression initializer = node.getInitializer();
		if (initializer != null) {
			expressionVisitor.reset(currentScope);
			initializer.accept(expressionVisitor);
			NameReference initExpRef = expressionVisitor.getResult();
			if (initExpRef != null) resultList.add(initExpRef);
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
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		resultList.add(result);
		return false;
	}

	/**
	 * SuperMethodInvocation: [ ClassName . ] super .  [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * Use the expression to visit the node
	 */
	public boolean visit(SuperMethodInvocation node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();

		resultList.add(result);
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
		// Create a type reference for class name in the node
		Name classNameNode = node.getQualifier();
		if (classNameNode != null) {
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(classNameNode, unitFile.root, unitFile.unitName);
			NameScope currentScope = getNameScopeOfLocation(location, bodyScope);
			
			String className = classNameNode.getFullyQualifiedName();
			TypeReference classRef = new TypeReference(className, location, currentScope);

			resultList.add(classRef);
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
	 * Use creator.scan() to create the name definitions and references in the local type
	 */
	public boolean visit(TypeDeclarationStatement node) {
		if (node.getDeclaration().getNodeType() == ASTNode.TYPE_DECLARATION) {
			TypeDeclaration type = (TypeDeclaration)node.getDeclaration();
			
			String declFullName = type.getName().getIdentifier();
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(type, unitFile.root, unitFile.unitName);
			
			List<DetailedTypeDefinition> typeList = bodyScope.getLocalTypeList();
			if (typeList != null) {
				for (DetailedTypeDefinition detailedType : typeList) {
					if (detailedType.getSimpleName().equals(declFullName) && detailedType.getLocation().equals(location)) {
						List<NameReference> referenceList = creator.createReferences(unitFile.unitName, unitFile.root, detailedType, type);
						resultList.addAll(referenceList);
					}
				}
			}
		}
		return false;
	}

	/**
	 * Use the type visitor to visit the type in the node
	 */
	public boolean visit(TypeLiteral node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);

		typeVisitor.reset(currentScope);
		node.getType().accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();

		resultList.add(typeRef);
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
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		resultList.add(typeRef);
		return false;
	}
	
	/**
	 * VariableDeclarationExpression: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
	 */
	public boolean visit(VariableDeclarationExpression node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		expressionVisitor.reset(currentScope);
		
		node.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		if (result != null) resultList.add(result);
		return false;
	}

	/**
	 * VariableDeclarationStatement: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
	 */
	public boolean visit(VariableDeclarationStatement node) {
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);

		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		resultList.add(typeRef);
		
		@SuppressWarnings("unchecked")
		List<VariableDeclarationFragment> fragmentList = node.fragments();
		for (VariableDeclarationFragment fragment : fragmentList) {
			Expression expression = fragment.getInitializer();
			if (expression != null) {
				expressionVisitor.reset(currentScope);
				expression.accept(expressionVisitor);
				NameReference result = expressionVisitor.getResult();
				if (result != null) resultList.add(result);
			}
		}
		return false;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not be accessed from a block directly
	 */
	public boolean visit(VariableDeclarationFragment node) {
		Expression expression = node.getInitializer();
		
		if (expression != null) {
			SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
			NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
			expressionVisitor.reset(currentScope);

			expression.accept(expressionVisitor);
			NameReference result = expressionVisitor.getResult();
			if (result != null) resultList.add(result);
		}
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
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameScope currentScope = getNameScopeOfLocation(start, bodyScope);
		typeVisitor.reset(currentScope);
		node.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		
		resultList.add(typeRef);
		return false;
	}
	
}
