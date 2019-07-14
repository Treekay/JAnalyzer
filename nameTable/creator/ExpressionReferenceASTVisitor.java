package nameTable.creator;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.ParameterizedTypeReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameReference.referenceGroup.NRGClassInstanceCreation;
import nameTable.nameReference.referenceGroup.NRGVariableDeclaration;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameScope.NameScope;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeLocation;

/**
 * An expression visitor for creating all name references in an expression or statement. The result will be a name reference
 * group which includes all name reference in the expression or statement. 
 * <p>Note that we will use the same location to generate all name references in the group, so the locations of 
 * the references are not the precise location of the name reference occurring in the source code file.
 * @author Zhou Xiaocong
 * @since 2013-4-13
 * @version 1.0
 *
 * @update 2016/11/6
 * 		Refactor the class according to the design document
 */
public class ExpressionReferenceASTVisitor extends ExpressionASTVisitor {
	protected NameReferenceCreator referenceCreator = null;
	
	public ExpressionReferenceASTVisitor(NameReferenceCreator creator, CompilationUnitRecorder unitFile, NameScope scope) {
		super(null, unitFile, scope);
		this.referenceCreator = creator;
	}

	public ExpressionReferenceASTVisitor(NameReferenceCreator creator, String unitName, CompilationUnit root, NameScope scope) {
		super(null, new CompilationUnitRecorder(unitName, root), scope);
		this.referenceCreator = creator;
	}

	public ExpressionReferenceASTVisitor(NameReferenceCreator creator, CompilationUnitRecorder unitFile, NameScope scope, boolean createLiteralReference) {
		super(null, unitFile, scope, createLiteralReference);
		this.referenceCreator = creator;
	}

	public ExpressionReferenceASTVisitor(NameReferenceCreator creator, String unitName, CompilationUnit root, NameScope scope, boolean createLiteralReference) {
		super(null, new CompilationUnitRecorder(unitName, root), scope, createLiteralReference);
		this.referenceCreator = creator;
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
			argumentRefList.add(lastReference);
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
		// Process the anonymous class declaration in the node!
		AnonymousClassDeclaration anonymousClass = node.getAnonymousClassDeclaration();
		if (anonymousClass != null) {
			List<NameReference> referenceList = referenceCreator.createReferences(unitFile.unitName, unitFile.root, scope, anonymousClass);
			for (NameReference reference : referenceList) {
				referenceGroup.addSubReference(reference);
			}
		}
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * VariableDeclarationExpression: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * <p>VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
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
}
