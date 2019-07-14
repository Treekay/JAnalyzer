package nameTable.creator;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameReference.LiteralReference;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.TypeReference;
import nameTable.nameReference.ParameterizedTypeReference;
import nameTable.nameReference.ValueReference;
import nameTable.nameReference.referenceGroup.NRGArrayAccess;
import nameTable.nameReference.referenceGroup.NRGArrayCreation;
import nameTable.nameReference.referenceGroup.NRGArrayInitializer;
import nameTable.nameReference.referenceGroup.NRGAssignment;
import nameTable.nameReference.referenceGroup.NRGCast;
import nameTable.nameReference.referenceGroup.NRGClassInstanceCreation;
import nameTable.nameReference.referenceGroup.NRGConditional;
import nameTable.nameReference.referenceGroup.NRGFieldAccess;
import nameTable.nameReference.referenceGroup.NRGInfixExpression;
import nameTable.nameReference.referenceGroup.NRGInstanceof;
import nameTable.nameReference.referenceGroup.NRGMethodInvocation;
import nameTable.nameReference.referenceGroup.NRGPostfixExpression;
import nameTable.nameReference.referenceGroup.NRGPrefixExpression;
import nameTable.nameReference.referenceGroup.NRGQualifiedName;
import nameTable.nameReference.referenceGroup.NRGSuperFieldAccess;
import nameTable.nameReference.referenceGroup.NRGSuperMethodInvocation;
import nameTable.nameReference.referenceGroup.NRGThisExpression;
import nameTable.nameReference.referenceGroup.NRGTypeLiteral;
import nameTable.nameReference.referenceGroup.NRGVariableDeclaration;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

import org.eclipse.jdt.core.dom.ASTVisitor;
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
import org.eclipse.jdt.core.dom.Name;
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

import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeLocation;

/**
 * Visit an expression, create name reference for the expression. Note that the name reference created by 
 * the class may be a name reference group. We DO NOT add the result reference to the creator, since we do not 
 * want to add sub-references of the group to the creator directly!
 * @author Zhou Xiaocong
 * @since 2013-2-23
 * @version 1.0
 * 
 * @update 2016/11/11
 * 		Refactor the class according to the design document
 */
public class ExpressionASTVisitor extends ASTVisitor {
	// The result reference corresponding to the expression is the last reference in the travel.
	// Note that the result reference may be a reference group!
	protected NameReference lastReference = null;
	protected boolean createReferenceForLiteral = false;
	
	protected NameScope scope = null;
	protected TypeASTVisitor typeVisitor = null;

	protected CompilationUnitRecorder unitFile = null;
	protected NameTableCreator creator = null;
	
	public ExpressionASTVisitor(NameTableCreator creator, CompilationUnitRecorder unitFile, NameScope scope) {
		this.creator = creator;
		this.unitFile = unitFile;
		this.scope = scope;
		typeVisitor = new TypeASTVisitor(unitFile, scope);
	}
	
	public ExpressionASTVisitor(NameTableCreator creator, CompilationUnitRecorder unitFile, NameScope scope, boolean createReferenceForLiteral) {
		this.creator = creator;
		this.unitFile = unitFile;
		this.scope = scope;
		this.createReferenceForLiteral = createReferenceForLiteral;
		typeVisitor = new TypeASTVisitor(unitFile, scope);
	}
	
	public void setCreateReferenceForLiteral(boolean flag) {
		this.createReferenceForLiteral = flag;
	}
	
	public NameReference getResult() {
		return lastReference;
	}
	
	public void reset(CompilationUnitRecorder unitFile, NameScope scope) {
		this.unitFile = unitFile;
		this.scope = scope;
		lastReference = null;
		typeVisitor.reset(unitFile, scope);
	}

	public void reset(NameScope scope) {
		lastReference = null;
		this.scope = scope;
	}

	public void reset() {
		lastReference = null;
	}
	
	/**
	 * ArrayAccess: Expression[Expression]
	 */
	@Override
	public boolean visit(ArrayAccess node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGArrayAccess(name, location, scope);

		// Visit the array expression of the node
		Expression arrayExpression = node.getArray();
		arrayExpression.accept(this);
		// Add the references corresponding to array expressions to the reference group
		referenceGroup.addSubReference(lastReference);

		// Visit the index expression of the node
		Expression indexExpression = node.getIndex();
		indexExpression.accept(this);
		// Add the references corresponding to index expressions to the reference group
		referenceGroup.addSubReference(lastReference);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * ArrayCreation: new Type[Expression] ArrayInitializer
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(ArrayCreation node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGArrayCreation(name, location, scope);

		// Visit the type of the node
		Type type = node.getType();
		typeVisitor.reset(scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		
		// Add type reference to the group
		referenceGroup.addSubReference(typeRef);

		// Visit the dimension expressions of the node
		List<Expression> dimensionExpressions = node.dimensions();
		for (Expression dimensionExp : dimensionExpressions) {
			dimensionExp.accept(this);
			referenceGroup.addSubReference(lastReference);
		}
		
		// Visit the initializer of the node
		ArrayInitializer initializer = node.getInitializer();
		if (initializer != null) {
			initializer.accept(this);
			// Get the name reference corresponding to array initializer
			referenceGroup.addSubReference(lastReference);
		}
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * ArrayInitializer: { Expression, Expression, .. }
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(ArrayInitializer node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGArrayInitializer(name, location, scope);
		
		// Visit the initial expressions of the node
		List<Expression> initExpressions = node.expressions();
		for (Expression initExp : initExpressions) {
			initExp.accept(this);
			referenceGroup.addSubReference(lastReference);
		}

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Assignment: Expression AssignmentOperator Expression
	 */
	public boolean visit(Assignment node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGAssignment(name, location, scope);
		
		// Set the operator of the reference group
		String operator = node.getOperator().toString();
		referenceGroup.setOperator(operator);
		
		// Visit the left expressions of the node
		Expression leftExp = node.getLeftHandSide();
		leftExp.accept(this);
		lastReference.setLeftValueReference();
		referenceGroup.addSubReference(lastReference);

		// Visit the right expressions of the node
		Expression rightExp = node.getRightHandSide();
		rightExp.accept(this);
		referenceGroup.addSubReference(lastReference);

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * CastExpression: (Type) Expression
	 */
	public boolean visit(CastExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGCast(name, location, scope);

		// Visit the type of the node
		Type type = node.getType();
		typeVisitor.reset(scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		// Add type reference to the group
		referenceGroup.addSubReference(typeRef);

		// Visit the expressions of the node
		Expression exp = node.getExpression();
		exp.accept(this);
		referenceGroup.addSubReference(lastReference);

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(ClassInstanceCreation node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGClassInstanceCreation(name, location, scope);

		// Visit the possible expressions of the node
		Expression exp = node.getExpression();
		if (exp != null) {
			exp.accept(this);
			referenceGroup.addSubReference(lastReference);
		}

		// Visit the type of the node
		Type type = node.getType();
		typeVisitor.reset(scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		// Add type reference to the group as the return type of constructor invocation
		referenceGroup.addSubReference(typeRef);

		String methodName = typeRef.getName();
		// For parameterized type, the name of its constructor is the private type reference name
		if (typeRef.isParameterizedType()) {
			ParameterizedTypeReference paraType = (ParameterizedTypeReference)typeRef;
			methodName = paraType.getPrimaryType().getName();
		}
		location = SourceCodeLocation.getStartLocation(type, unitFile.root, unitFile.unitName);
		MethodReference methodRef = new MethodReference(methodName, location, scope);
		
		// Add method (constructor invocation) reference to the group
		referenceGroup.addSubReference(methodRef);

		boolean oldCreateReferenceForLiteral = createReferenceForLiteral;
		createReferenceForLiteral = true;
		// Visit the argument list of the node
		List<Expression> arguments = node.arguments();
		List<NameReference> argumentRefList = new ArrayList<NameReference>();
		for (Expression arg : arguments) {
			arg.accept(this);
			if (lastReference != null) argumentRefList.add(lastReference);
		}
		methodRef.setArgumentList(argumentRefList);
		createReferenceForLiteral = oldCreateReferenceForLiteral;
		
		List<Type> typeArguments = node.typeArguments();
		List<TypeReference> typeArgumentRefList = new ArrayList<TypeReference>();
		for (Type typeArg : typeArguments) {
			typeVisitor.reset(scope);
			typeArg.accept(typeVisitor);
			TypeReference typeArgRef = typeVisitor.getResult();
			typeArgumentRefList.add(typeArgRef);
		}
		methodRef.setTypeArgumentList(typeArgumentRefList);

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
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * ConditionalExpression: Expression ? Expression : Expression
	 */
	public boolean visit(ConditionalExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGConditional(name, location, scope);

		// Visit the condition expressions of the node
		Expression condition = node.getExpression();
		condition.accept(this);
		referenceGroup.addSubReference(lastReference);
		
		// Visit the condition expressions of the node
		Expression thenExp = node.getThenExpression();
		thenExp.accept(this);
		referenceGroup.addSubReference(lastReference);
	
		// Visit the condition expressions of the node
		Expression elseExp = node.getElseExpression();
		elseExp.accept(this);
		referenceGroup.addSubReference(lastReference);

		// The reference group is the result reference of the node, save it to the lastReference
		// If there is no sub reference in the referenceGroup, then it represent a constant expression, we do not add it 
		// to the reference list!
		if (referenceGroup.getSubReferenceList() != null) lastReference = referenceGroup;
		else lastReference = null;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * FieldAccess: Expression.Identifier ;
	 */
	public boolean visit(FieldAccess node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGFieldAccess(name, location, scope);

		// Visit the expressions of the node
		Expression expression = node.getExpression();
		expression.accept(this);
		referenceGroup.addSubReference(lastReference);
		
		NameReference fieldNameRef = createReferenceForName(node.getName(), NameReferenceKind.NRK_FIELD);
		referenceGroup.addSubReference(fieldNameRef);

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * Expression InfixOperator Expression { InfixOperator Expression }
	 * Use the expression visitor to visit the node
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(InfixExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGInfixExpression(name, location, scope);

		// Set the operator of the reference group
		String operator = node.getOperator().toString();
		referenceGroup.setOperator(operator);
		
		// Visit the left operand expressions of the node
		Expression leftExp = node.getLeftOperand();
		leftExp.accept(this);
		referenceGroup.addSubReference(lastReference);
		
		// Visit the left operand expressions of the node
		Expression rightExp = node.getRightOperand();
		rightExp.accept(this);
		referenceGroup.addSubReference(lastReference);
		
		// Visit the extended operand of the node
		if (node.hasExtendedOperands()) {
			List<Expression> extendedOperands = node.extendedOperands();
			for (Expression operand : extendedOperands) {
				operand.accept(this);
				referenceGroup.addSubReference(lastReference);
			}
		}

		// The reference group is the result reference of the node, save it to the lastReference
		// If there is no sub reference in the referenceGroup, then it represent a constant expression, we do not add it 
		// to the reference list!
		if (referenceGroup.getSubReferenceList() != null) lastReference = referenceGroup;
		else lastReference = null;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * InstanceofExpression: Expression instanceof Type
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(InstanceofExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGInstanceof(name, location, scope);

		// Visit the left operand expressions of the node
		Expression leftExp = node.getLeftOperand();
		leftExp.accept(this);
		referenceGroup.addSubReference(lastReference);

		// Visit the type (i.e. the right operand) of the node
		Type type = node.getRightOperand();
		typeVisitor.reset(scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		// Add type reference to the group
		referenceGroup.addSubReference(typeRef);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(LambdaExpression node) {
		return false;
	}

	
	/**
	 * [ Expression . ] [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * Use the expression visitor to visit the node
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(MethodInvocation node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGMethodInvocation(name, location, scope);

		// Visit the expressions of the node
		Expression expression = node.getExpression();
		if (expression != null) {
			expression.accept(this);
			referenceGroup.addSubReference(lastReference);
		}
		
		String methodName = node.getName().getFullyQualifiedName();
		MethodReference methodRef = new MethodReference(methodName, location, scope);
		referenceGroup.addSubReference(methodRef);
	
		// Visit the arguments of the method invocation
		List<Expression> arguments = node.arguments();
		List<NameReference> argumentReferenceList = new ArrayList<NameReference>();
		boolean oldCreateREferenceForLiteral = createReferenceForLiteral;
		createReferenceForLiteral = true;
		for (Expression arg : arguments) {
			arg.accept(this);
			if (lastReference != null) argumentReferenceList.add(lastReference);
		}
		methodRef.setArgumentList(argumentReferenceList);
		createReferenceForLiteral = oldCreateREferenceForLiteral;

		List<Type> typeArguments = node.typeArguments();
		List<TypeReference> typeArgumentRefList = new ArrayList<TypeReference>();
		for (Type typeArg : typeArguments) {
			typeVisitor.reset(scope);
			typeArg.accept(typeVisitor);
			TypeReference typeArgRef = typeVisitor.getResult();
			typeArgumentRefList.add(typeArgRef);
		}
		methodRef.setTypeArgumentList(typeArgumentRefList);

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
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
	 */
	public boolean visit(PostfixExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGPostfixExpression(name, location, scope);

		// Set the operator of the reference group
		String operator = node.getOperator().toString();
		referenceGroup.setOperator(operator);

		// Visit the expressions of the node
		Expression expression = node.getOperand();
		expression.accept(this);
		lastReference.setLeftValueReference();
		referenceGroup.addSubReference(lastReference);

		// The reference group is the result reference of the node, save it to the lastReference
		// If there is no sub reference in the referenceGroup, then it represent a constant expression, we do not add it 
		// to the reference list!
		if (referenceGroup.getSubReferenceList() != null) lastReference = referenceGroup;
		else lastReference = null;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * PrefixExpression: PrefixOperator Expression 
	 */
	public boolean visit(PrefixExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGPrefixExpression(name, location, scope);

		// Set the operator of the reference group
		String operator = node.getOperator().toString();
		referenceGroup.setOperator(operator);

		// Visit the expressions of the node
		Expression expression = node.getOperand();
		expression.accept(this);
		if (operator.equals(NameReferenceGroup.OPERATOR_INCREMENT) || operator.equals(NameReferenceGroup.OPERATOR_DECREMENT))
			lastReference.setLeftValueReference();
		referenceGroup.addSubReference(lastReference);

		// The reference group is the result reference of the node, save it to the lastReference
		// If there is no sub reference in the referenceGroup, then it represent a constant expression, we do not add it 
		// to the reference list!
		if (referenceGroup.getSubReferenceList() != null) lastReference = referenceGroup;
		else lastReference = null;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * Create a variable reference for the fully qualified name of the node
	 */
	public boolean visit(QualifiedName node) {
		// Create a reference group for the node
		String name = node.getFullyQualifiedName();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGQualifiedName(name, location, scope);

		// Visit the expressions of the node
		Name qualifier = node.getQualifier();
		qualifier.accept(this);
		referenceGroup.addSubReference(lastReference);
		
		NameReference simpleNameRef = createReferenceForName(node.getName(), NameReferenceKind.NRK_VARIABLE);
		referenceGroup.addSubReference(simpleNameRef);

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * Create a variable reference for the fully qualified name of the node
	 */
	public boolean visit(SimpleName node) {
		// Create a reference for the node
		String name = node.getFullyQualifiedName();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReference reference = new ValueReference(name, location, scope, NameReferenceKind.NRK_VARIABLE);

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * SuperFieldAccess: [ ClassName . ] super . Identifier
	 * Use the expression to visit the node
	 */
	public boolean visit(SuperFieldAccess node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGSuperFieldAccess(name, location, scope);
		
		// Create a type reference for class name in the node
		Name classNameNode = node.getQualifier();
		if (classNameNode != null) {
			location = SourceCodeLocation.getStartLocation(classNameNode, unitFile.root, unitFile.unitName);
			String className = classNameNode.getFullyQualifiedName();
			TypeReference classRef = new TypeReference(className, location, scope);
			referenceGroup.addSubReference(classRef);
		}
		
		// Create a field reference for field name in the node
		Name fieldNameNode = node.getName();
		location = SourceCodeLocation.getStartLocation(fieldNameNode, unitFile.root, unitFile.unitName);
		String fieldName = fieldNameNode.getFullyQualifiedName();
		NameReference fieldRef = new ValueReference(fieldName, location, scope, NameReferenceKind.NRK_FIELD);
		referenceGroup.addSubReference(fieldRef);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * SuperMethodInvocation: [ ClassName . ] super .  [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * Use the expression to visit the node
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(SuperMethodInvocation node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGSuperMethodInvocation(name, location, scope);
		
		// Create a type reference for class name in the node
		Name classNameNode = node.getQualifier();
		if (classNameNode != null) {
			location = SourceCodeLocation.getStartLocation(classNameNode, unitFile.root, unitFile.unitName);
			String className = classNameNode.getFullyQualifiedName();
			TypeReference classRef = new TypeReference(className, location, scope);
			referenceGroup.addSubReference(classRef);
		}
		
		// Create a method reference for field name in the node
		Name methodNameNode = node.getName();
		location = SourceCodeLocation.getStartLocation(methodNameNode, unitFile.root, unitFile.unitName);
		String methodName = methodNameNode.getFullyQualifiedName();
		MethodReference methodRef = new MethodReference(methodName, location, scope);
		referenceGroup.addSubReference(methodRef);
		
		// Visit the argument expressions of the node
		List<Expression> arguments = node.arguments();
		List<NameReference> argumentReferenceList = new ArrayList<NameReference>();
		boolean oldCreateREferenceForLiteral = createReferenceForLiteral;
		createReferenceForLiteral = true;
		for (Expression arg : arguments) {
			arg.accept(this);
			if (lastReference != null) argumentReferenceList.add(lastReference);
		}
		methodRef.setArgumentList(argumentReferenceList);
		createReferenceForLiteral = oldCreateREferenceForLiteral;

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * ThisExpression : [ClassName.] this
	 * Create a literal reference for the keyword "this" and a type reference for the class name 
	 * in the expression
	 */
	public boolean visit(ThisExpression node) {
		// Create a literal reference for the key word 
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		LiteralReference thisReference = new LiteralReference(NameReferenceLabel.KEYWORD_THIS, NameReferenceLabel.KEYWORD_THIS, location, scope);
		
		// Create a type reference for class name in the node
		Name classNameNode = node.getQualifier();
		if (classNameNode != null) {
			// Create a reference group for the node
			String name = node.toString();
			NameReferenceGroup referenceGroup = new NRGThisExpression(name, location, scope);
			
			location = SourceCodeLocation.getStartLocation(classNameNode, unitFile.root, unitFile.unitName);
			String className = classNameNode.getFullyQualifiedName();
			TypeReference classRef = new TypeReference(className, location, scope);
			referenceGroup.addSubReference(classRef);

			referenceGroup.addSubReference(thisReference);

			// The reference group is the result reference of the node, save it to the lastReference
			lastReference = referenceGroup;
		} else {
			// The reference group is the result reference of the node, save it to the lastReference
			lastReference = thisReference;
		}
		
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * VariableDeclarationExpression: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * <p>VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
	 * Important notes: The expression visitor just create references in the node with ignoring any variable definitions.
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(VariableDeclarationExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGVariableDeclaration(name, location, scope);

		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		referenceGroup.addSubReference(typeRef);
		
		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Define the variable to the current scope
			creator.defineVariable(unitFile, varNode, typeRef, scope);
			
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				initializer.accept(this);
				referenceGroup.addSubReference(lastReference);
			}
		}

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Create a literal reference for the node
	 */
	public boolean visit(BooleanLiteral node) {
		if (!createReferenceForLiteral) {
			// We do not create reference for literal, then set the lastReference to null!
			lastReference = null;
			return false;
		}
		// Create a reference for the node
		String literal = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		LiteralReference reference = new LiteralReference(literal, NameReferenceLabel.TYPE_BOOLEAN, location, scope);
		
		// The reference is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	
	/**
	 * Create a literal reference for the node
	 */
	public boolean visit(CharacterLiteral node) {
		if (!createReferenceForLiteral) {
			// We do not create reference for literal, then set the lastReference to null!
			lastReference = null;
			return false;
		}
		// Create a reference for the node
		String literal = node.getEscapedValue();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		LiteralReference reference = new LiteralReference(literal, NameReferenceLabel.TYPE_CHAR, location, scope);
		
		// The reference is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Create a literal reference for the node
	 */
	public boolean visit(NullLiteral node) {
		if (!createReferenceForLiteral) {
			// We do not create reference for literal, then set the lastReference to null!
			lastReference = null;
			return false;
		}
		// Create a reference for the node
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		LiteralReference reference = new LiteralReference(NameReferenceLabel.KEYWORD_NULL, NameReferenceLabel.KEYWORD_NULL, location, scope);
		
		// The reference is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Create a literal reference for the node
	 */
	public boolean visit(NumberLiteral node) {
		if (!createReferenceForLiteral) {
			// We do not create reference for literal, then set the lastReference to null!
			lastReference = null;
			return false;
		}
		// Create a reference for the node
		String literal = node.getToken();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		
		// Judge the type of the literal, we use the simplest way to get it!
		String typeName = NameReferenceLabel.TYPE_INT;
		if (literal.contains("l") || literal.contains("L")) typeName = NameReferenceLabel.TYPE_LONG;
		else if (literal.contains(".") || literal.contains("e") || literal.contains("E")) typeName = NameReferenceLabel.TYPE_DOUBLE;
		else if (literal.contains("f") || literal.contains("F")) typeName = NameReferenceLabel.TYPE_FLOAT;

		LiteralReference reference = new LiteralReference(literal, typeName, location, scope);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Create a literal reference for the node
	 */
	public boolean visit(StringLiteral node) {
		if (!createReferenceForLiteral) {
			// We do not create reference for literal, then set the lastReference to null!
			lastReference = null;
			return false;
		}
		// Create a reference for the node
		String name = node.getEscapedValue();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		LiteralReference reference = new LiteralReference(name, NameReferenceLabel.TYPE_STRING, location, scope);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * TypeLiteral: ( Type | void ) . class
	 */
	public boolean visit(TypeLiteral node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReferenceGroup referenceGroup = new NRGTypeLiteral(name, location, scope);

		Type type = node.getType();
		typeVisitor.reset(scope);
		type.accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		referenceGroup.addSubReference(typeRef);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Create a name reference for a name node
	 */
	private NameReference createReferenceForName(Name node, NameReferenceKind kind) {
		String name = node.getFullyQualifiedName();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NameReference result = new ValueReference(name, location, scope, kind);
		return result;
	}
}
