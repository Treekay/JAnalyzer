package nameTable.creator;

import java.util.ArrayList;
import java.util.List;

import nameTable.NameTableASTBridge;
import nameTable.NameTableManager;
import nameTable.filter.NameReferenceNameFilter;
import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.AnonymousClassDefinition;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumConstantDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeLocation;

/**
 * A class provided some static methods to create reference for compilation unit, class, method or field
 * @author Zhou Xiaocong
 * @since 2015Äê6ÔÂ24ÈÕ
 * @version 1.0
 *
 * @update 2016/11/13
 * 		Refactor the class according to the design document
 */
public class NameReferenceCreator {
	protected NameTableManager tableManager = null;
	protected boolean createLiteralReference = false;
	
	public NameReferenceCreator(NameTableManager tableManager) {
		this.tableManager = tableManager;
	}

	public NameReferenceCreator(NameTableManager tableManager, boolean createLiteralReference) {
		this.tableManager = tableManager;
		this.createLiteralReference = createLiteralReference;
	}

	/**
	 * Create and return all name references of the given compilation unit scope. All the created references are not 
	 * stored in the name table 
 	 */
	public List<NameReference> createReferences(CompilationUnitScope unitScope) {
		List<NameReference> resultList = new ArrayList<NameReference>();
		
		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		CompilationUnit root = bridge.findASTNodeForCompilationUnitScope(unitScope);
		if (root == null) return resultList;
		String unitName = unitScope.getUnitName();
		
		@SuppressWarnings("unchecked")
		List<AbstractTypeDeclaration> types = root.types();
		for (AbstractTypeDeclaration type : types) {
			if (type.getNodeType() == ASTNode.TYPE_DECLARATION) {
				SourceCodeLocation location = SourceCodeLocation.getStartLocation(type, root, unitName);
				DetailedTypeDefinition detailedType = bridge.findDefinitionForTypeDeclaration(unitScope, location, (TypeDeclaration)type);
				if (detailedType == null) {
					throw new AssertionError("Can not find detailed type definition for " + type.getName() + ", in location " + location + " of unit " + unitName);
				}
				List<NameReference> referenceList = createReferences(unitName, root, detailedType, (TypeDeclaration)type);
				resultList.addAll(referenceList);
			} else if (type.getNodeType() == ASTNode.ENUM_DECLARATION) {
				SourceCodeLocation location = SourceCodeLocation.getStartLocation(type, root, unitName);
				EnumTypeDefinition enumType = bridge.findDefinitionForEnumDeclaration(unitScope, location, (EnumDeclaration)type);
				List<NameReference> referenceList = createReferences(unitName, root, enumType, (EnumDeclaration)type);
				resultList.addAll(referenceList);
			}
		}
		
		return resultList;
	}
	
	/**
	 * Create and return all name references of the given compilation unit scope. All the created references are not 
	 * stored in the name table 
 	 */
	public List<NameReference> createReferences(String unitName) {
		CompilationUnitScope unitScope = tableManager.findCompilationUnitScopeByUnitName(unitName);
		if (unitScope == null) return new ArrayList<NameReference>();
		return createReferences(unitScope);
	}
	
	
	/**
	 * Create and return all name references of the given type declaration node when we know its corresponding detailed type definition.
 	 */
	public List<NameReference> createReferences(String unitName, CompilationUnit root, DetailedTypeDefinition type, TypeDeclaration node) {
		List<NameReference> resultList = new ArrayList<NameReference>();
		// Add all super type reference to the result list
		List<TypeReference> superList = type.getSuperList();
		if (superList != null) resultList.addAll(superList);
		
		// Process the initializers in the node
		@SuppressWarnings("unchecked")
		List<BodyDeclaration> bodyList = node.bodyDeclarations();
		for (BodyDeclaration bodyDecl : bodyList) {
			if (bodyDecl.getNodeType() == ASTNode.INITIALIZER) {
				Initializer initializer = (Initializer)bodyDecl;
				List<NameReference> result = createReferences(unitName, root, type, initializer);
				resultList.addAll(result);
			}
		}
		
		// Process the field declarations in the node
		FieldDeclaration[] fields = node.getFields();
		for (int index = 0; index < fields.length; index++) {
			List<NameReference> result = createReferences(unitName, root, type, fields[index]);
			resultList.addAll(result);
		}
		
		// Process the method declarations in the node
		MethodDeclaration[] methods = node.getMethods();
		for (int index = 0; index < methods.length; index++) {
			List<MethodDefinition> methodList = type.getMethodList();
			if (methodList == null) continue;
			for (MethodDefinition methodInType : methodList) {
				String methodSimpleName = methods[index].getName().getIdentifier();
				SourceCodeLocation location = SourceCodeLocation.getStartLocation(methods[index], root, unitName);
				
				if (methodInType.getSimpleName().equals(methodSimpleName) && methodInType.getLocation().equals(location)) {
					List<NameReference> result = createReferences(unitName, root, methodInType, methods[index]);
					resultList.addAll(result);
				}
			}
		}
		
		// Process the type declarations in the node
		TypeDeclaration[] typeMembers = node.getTypes();
		for (int index = 0; index < typeMembers.length; index++) {
			String declFullName = typeMembers[index].getName().getIdentifier();
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(typeMembers[index], root, unitName);
			
			List<DetailedTypeDefinition> typeList = type.getTypeList();
			if (typeList == null) continue;
			for (DetailedTypeDefinition detailedType : typeList) {
				if (detailedType.getSimpleName().equals(declFullName) && detailedType.getLocation().equals(location)) {
					List<NameReference> result = createReferences(unitName, root, detailedType, typeMembers[index]);
					resultList.addAll(result);
				}
			}
		}
		return resultList;
	}

	/**
	 * Create and return all name references of the given type declaration node when we know its corresponding detailed type definition.
	 * The created references do not include its super types, and all references in its method signature. 
 	 */
	public List<NameReference> createReferencesForTypeBody(String unitName, CompilationUnit root, DetailedTypeDefinition type, TypeDeclaration node) {
		List<NameReference> resultList = new ArrayList<NameReference>();

		// Process the initializers in the node
		@SuppressWarnings("unchecked")
		List<BodyDeclaration> bodyList = node.bodyDeclarations();
		for (BodyDeclaration bodyDecl : bodyList) {
			if (bodyDecl.getNodeType() == ASTNode.INITIALIZER) {
				Initializer initializer = (Initializer)bodyDecl;
				List<NameReference> result = createReferences(unitName, root, type, initializer);
				resultList.addAll(result);
			}
		}
		
		// Process the field declarations in the node
		FieldDeclaration[] fields = node.getFields();
		for (int index = 0; index < fields.length; index++) {
			List<NameReference> result = createReferences(unitName, root, type, fields[index]);
			resultList.addAll(result);
		}
		
		// Process the method declarations in the node
		MethodDeclaration[] methods = node.getMethods();
		for (int index = 0; index < methods.length; index++) {
			List<MethodDefinition> methodList = type.getMethodList();
			if (methodList == null) continue;
			for (MethodDefinition methodInType : methodList) {
				String methodSimpleName = methods[index].getName().getIdentifier();
				SourceCodeLocation location = SourceCodeLocation.getStartLocation(methods[index], root, unitName);
				
				if (methodInType.getSimpleName().equals(methodSimpleName) && methodInType.getLocation().equals(location)) {
					List<NameReference> result = createReferencesForMethodBody(unitName, root, methodInType, methods[index]);
					resultList.addAll(result);
				}
			}
		}

		// Process the type declarations in the node
		TypeDeclaration[] typeMembers = node.getTypes();
		for (int index = 0; index < typeMembers.length; index++) {
			String declFullName = typeMembers[index].getName().getIdentifier();
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(typeMembers[index], root, unitName);
			
			List<DetailedTypeDefinition> typeList = type.getTypeList();
			if (typeList == null) continue;
			
			for (DetailedTypeDefinition detailedType : typeList) {
				if (detailedType.getSimpleName().equals(declFullName) && detailedType.getLocation().equals(location)) {
					List<NameReference> result = createReferences(unitName, root, detailedType, typeMembers[index]);
					resultList.addAll(result);
				}
			}
		}
		return resultList;
	}
	
	/**
	 * Create and return all name references of the given type declaration node.
 	 */
	public List<NameReference> createReferences(DetailedTypeDefinition type) {
		CompilationUnitScope unitScope = tableManager.getEnclosingCompilationUnitScope(type);
		if (unitScope == null) return new ArrayList<NameReference>();

		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		CompilationUnit root = bridge.findASTNodeForCompilationUnitScope(unitScope);
		if (root == null) return new ArrayList<NameReference>();
		
		TypeDeclaration typeDeclaration = bridge.findASTNodeForDetailedTypeDefinition(type);
		if (typeDeclaration == null) return new ArrayList<NameReference>();
		
		return createReferences(unitScope.getUnitName(), root, type, typeDeclaration);
	}

	/**
	 * Create and return all name references of the given type declaration node.
 	 */
	public List<NameReference> createReferencesForTypeBody(DetailedTypeDefinition type) {
		CompilationUnitScope unitScope = tableManager.getEnclosingCompilationUnitScope(type);
		if (unitScope == null) return new ArrayList<NameReference>();

		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		CompilationUnit root = bridge.findASTNodeForCompilationUnitScope(unitScope);
		if (root == null) return new ArrayList<NameReference>();
		
		TypeDeclaration typeDeclaration = bridge.findASTNodeForDetailedTypeDefinition(type);
		if (typeDeclaration == null) return new ArrayList<NameReference>();
		
		return createReferencesForTypeBody(unitScope.getUnitName(), root, type, typeDeclaration);
	}

	/**
	 * Create and return all name reference of an initializer block in the given detailed type definition 
 	 */
	public List<NameReference> createReferences(String unitName, CompilationUnit root, DetailedTypeDefinition type, Initializer node) {
		Block body = node.getBody();
		if (body != null) {
			SourceCodeLocation start = SourceCodeLocation.getStartLocation(body, root, unitName);
			SourceCodeLocation end = SourceCodeLocation.getEndLocation(body, root, unitName);
			LocalScope localScope = (LocalScope)tableManager.getScopeOfStartAndEndLocation(start, end, type);
			
			if (localScope == null) {
				throw new AssertionError("Can not find local scope start " + start + ", end " + end + ", for initializer of type " + type.getFullQualifiedName());
			}
			BlockReferenceASTVisitor blockVisitor = new BlockReferenceASTVisitor(this, unitName, root, localScope, createLiteralReference);
			// Then visit all children of the block
			body.accept(blockVisitor);
			return blockVisitor.getResult();
		} else return new ArrayList<NameReference>();
	}
	

	/**
	 * Create and return all name reference of a enum type definition when we know its corresponding enum declaration node. 
 	 */
	public List<NameReference> createReferences(String unitName, CompilationUnit root, EnumTypeDefinition type, EnumDeclaration node) {
		List<NameReference> resultList = new ArrayList<NameReference>();
		// Add all super type reference to the result list
		List<TypeReference> superList = type.getSuperList(); 
		if (superList != null) resultList.addAll(superList);
		
		// Process the constant declarations in the node. We regard the enum constant as method 
		List<EnumConstantDefinition> constantList = type.getConstantList();
		if (constantList == null) return resultList;
		for (EnumConstantDefinition constant : constantList) {
			List<NameReference> argumentList = constant.getArgumentList();
			if (argumentList != null) resultList.addAll(argumentList);
		}
		return resultList;
	}
	
	/**
	 * Create and return all name reference of an anonymous class declaration node in the given local scope. 
 	 */
	public List<NameReference> createReferences(String unitName, CompilationUnit root, NameScope scope, AnonymousClassDeclaration node) {
		List<NameReference> resultList = new ArrayList<NameReference>();
		
		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		AnonymousClassDefinition anonymousClass = bridge.findDefinitionForAnonymousClassDeclaration(unitName, node); 
		if (anonymousClass == null) return resultList;
		
		@SuppressWarnings("unchecked")
		List<BodyDeclaration> bodyList = node.bodyDeclarations();
		for (BodyDeclaration bodyDecl : bodyList) {
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(bodyDecl, root, unitName);
			int nodeType = bodyDecl.getNodeType();
			if (nodeType == ASTNode.INITIALIZER) {
				List<NameReference> result = createReferences(unitName, root, anonymousClass, (Initializer)bodyDecl);
				resultList.addAll(result);
			} else if (nodeType == ASTNode.FIELD_DECLARATION) {
				List<NameReference> result = createReferences(unitName, root, anonymousClass, (FieldDeclaration)bodyDecl);
				resultList.addAll(result);
			} else if (nodeType == ASTNode.METHOD_DECLARATION) {
				MethodDeclaration methodDeclaration = (MethodDeclaration)bodyDecl;
				MethodDefinition methodDefinition = bridge.findDefinitionForMethodDeclaration(anonymousClass, location, methodDeclaration);
				if (methodDefinition != null) {
					List<NameReference> result = createReferences(unitName, root, methodDefinition, methodDeclaration);
					resultList.addAll(result);
				}
			} else if (nodeType == ASTNode.TYPE_DECLARATION) {
				List<NameReference> result = createReferences(unitName, root, anonymousClass, (TypeDeclaration)bodyDecl);
				resultList.addAll(result);
			} else {
				throw new AssertionError("Incorrect AST node type in anonymous class at " + location.getUniqueId());
			}
		}
		
		return resultList;
	}
	
	/**
	 * Create and return all name references for a field declaration node in the give detailed type definition 
	 */
	public List<NameReference> createReferences(String unitName, CompilationUnit astRoot, DetailedTypeDefinition detailedType, FieldDeclaration node) {
		CompilationUnitRecorder unitFile = new CompilationUnitRecorder(unitName, astRoot);
		ExpressionReferenceASTVisitor expressionVisitor = new ExpressionReferenceASTVisitor(this, unitFile, detailedType, createLiteralReference);
		List<NameReference> resultList = new ArrayList<NameReference>();

		// Visit the variable list defined in the node
		@SuppressWarnings("unchecked")
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				expressionVisitor.reset();
				initializer.accept(expressionVisitor);
				NameReference initExpRef = expressionVisitor.getResult();
				if (initExpRef != null) resultList.add(initExpRef);
			}
		}
		return resultList;
	}

	/**
	 * Create and return all name references for the given field definition 
	 */
	public List<NameReference> createReferences(FieldDefinition field) {
		CompilationUnitScope unitScope = tableManager.getEnclosingCompilationUnitScope(field);
		DetailedTypeDefinition type = tableManager.getEnclosingDetailedTypeDefinition(field);
		if (unitScope == null || type == null) return new ArrayList<NameReference>();

		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		CompilationUnit root = bridge.findASTNodeForCompilationUnitScope(unitScope);
		if (root == null) return new ArrayList<NameReference>();
		
		FieldDeclaration fieldDeclaration = bridge.findASTNodeForFieldDefinition(field);
		if (fieldDeclaration == null) return new ArrayList<NameReference>();
		
		return createReferences(unitScope.getUnitName(), root, type, fieldDeclaration);
		
	}
	
	/**
	 * Create and return all name references for a method declaration node in the give detailed type definition 
	 */
	public List<NameReference> createReferences(String unitName, CompilationUnit root, MethodDefinition method, MethodDeclaration node) {
		List<NameReference> resultList = new ArrayList<NameReference>();
		TypeReference returnType = method.getReturnType();
		if (returnType != null) resultList.add(returnType);
		
		List<VariableDefinition> paraList = method.getParameterList();
		if (paraList != null) {
			for (VariableDefinition parameter : paraList) {
				TypeReference paraType = parameter.getType();
				resultList.add(paraType);
			}
		}
		
		List<TypeReference> throwTypeList = method.getThrowTypeList();
		if (throwTypeList != null) resultList.addAll(throwTypeList);
		
		// Scan the body of the method
		Block body = node.getBody();
		LocalScope localScope = method.getBodyScope();
		if (body != null) {
			BlockReferenceASTVisitor localVisitor = new BlockReferenceASTVisitor(this, unitName, root, localScope, createLiteralReference);
			// Then visit the block
			body.accept(localVisitor);
			resultList.addAll(localVisitor.getResult());
		}
		
		return resultList;
	}

	/**
	 * Create and return all name references for a method declaration node in the give detailed type definition 
	 * The created references do not include all references in its signature (i.e. return type, parameter list and throw list)
	 */
	public List<NameReference> createReferencesForMethodBody(String unitName, CompilationUnit root, MethodDefinition method, MethodDeclaration node) {
		List<NameReference> resultList = new ArrayList<NameReference>();

		// Scan the body of the method
		Block body = node.getBody();
		LocalScope localScope = method.getBodyScope();
		if (body != null) {
			BlockReferenceASTVisitor localVisitor = new BlockReferenceASTVisitor(this, unitName, root, localScope, createLiteralReference);
			// Then visit the block
			body.accept(localVisitor);
			resultList.addAll(localVisitor.getResult());
		}
		return resultList;
	}

	
	/**
	 * Create and return all name references for the given method definition 
	 */
	public List<NameReference> createReferences(MethodDefinition method) {
		CompilationUnitScope unitScope = tableManager.getEnclosingCompilationUnitScope(method);
		DetailedTypeDefinition type = tableManager.getEnclosingDetailedTypeDefinition(method);
		if (unitScope == null || type == null) return new ArrayList<NameReference>();

		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		CompilationUnit root = bridge.findASTNodeForCompilationUnitScope(unitScope);
		if (root == null) return new ArrayList<NameReference>();
		
		MethodDeclaration methodDeclaration = bridge.findASTNodeForMethodDefinition(method);
		if (methodDeclaration == null) return new ArrayList<NameReference>();
		
		return createReferences(unitScope.getUnitName(), root, method, methodDeclaration);
	}

	/**
	 * Create and return all name references for the given method definition 
	 * The created references do not include all references in its signature (i.e. return type, parameter list and throw list)
	 */
	public List<NameReference> createReferencesForMethodBody(MethodDefinition method) {
		CompilationUnitScope unitScope = tableManager.getEnclosingCompilationUnitScope(method);
		DetailedTypeDefinition type = tableManager.getEnclosingDetailedTypeDefinition(method);
		if (unitScope == null || type == null) return new ArrayList<NameReference>();

		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		CompilationUnit root = bridge.findASTNodeForCompilationUnitScope(unitScope);
		if (root == null) return new ArrayList<NameReference>();
		
		MethodDeclaration methodDeclaration = bridge.findASTNodeForMethodDefinition(method);
		if (methodDeclaration == null) return new ArrayList<NameReference>();
		
		return createReferencesForMethodBody(unitScope.getUnitName(), root, method, methodDeclaration);
	}
	
	/**
	 * Create and return all name references binded to a name definition. Not that all references returned are at leaf of name
	 * references, and all resolved since they are binded to the given name definition!
	 */
	public List<NameReference> createReferencesBindedToDefinition(NameDefinition definition) {
		List<NameReference> resultList = new ArrayList<NameReference>();
		List<CompilationUnitScope> unitScopeList = tableManager.getAllCompilationUnitScopes();
		if (unitScopeList == null) return resultList;
		for (CompilationUnitScope unitScope : unitScopeList) {
			List<NameReference> referenceList = createReferences(unitScope);
			for (NameReference reference : referenceList) {
				reference.resolveBinding();
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				for (NameReference leafReference : leafReferenceList) {
					if (leafReference.getDefinition() == definition) resultList.add(leafReference);
					else if (leafReference.getReferenceKind() == NameReferenceKind.NRK_METHOD) {
						MethodReference methodReference = (MethodReference)leafReference;
						List<MethodDefinition> alternativeList = methodReference.getAlternativeList();
						if (alternativeList != null) {
							for (MethodDefinition method : alternativeList) {
								if (method == definition) resultList.add(leafReference);
							}
						}
					}
				}
			}
		}
		return resultList;
	}

	/**
	 * Create and return all name references of the given name 
	 */
	public List<NameReference> createReferencesOfName(String name) {
		List<NameReference> resultList = new ArrayList<NameReference>();
		
		NameReferenceNameFilter filter = new NameReferenceNameFilter(name);
		
		List<CompilationUnitScope> unitScopeList = tableManager.getAllCompilationUnitScopes();
		if (unitScopeList == null) return resultList;
		for (CompilationUnitScope unitScope : unitScopeList) {
			List<NameReference> referenceList = createReferences(unitScope);
			for (NameReference reference : referenceList) {
				if (filter.accept(reference)) resultList.add(reference);
			}
		}
		return resultList;
	}

	/**
	 * Create and return all name references accepted by the given filter 
	 */
	public List<NameReference> createReferencesByFilter(NameTableFilter filter) {
		List<NameReference> resultList = new ArrayList<NameReference>();
		List<CompilationUnitScope> unitScopeList = tableManager.getAllCompilationUnitScopes();
		if (unitScopeList == null) return resultList;
		for (CompilationUnitScope unitScope : unitScopeList) {
			List<NameReference> referenceList = createReferences(unitScope);
			for (NameReference reference : referenceList) {
				if (filter.accept(reference)) resultList.add(reference);
			}
		}
		return resultList;
	}
	
	/**
	 * Create and return a reference (group) for an Expression AST node 
	 */
	public NameReference createReferenceForExpressionASTNode(String unitName, Expression node) {
		CompilationUnit root = tableManager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitName);
		if (root == null) return null;
		
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, root, unitName);
		NameScope scope = tableManager.getScopeOfLocation(start);
		
		ExpressionReferenceASTVisitor visitor = new ExpressionReferenceASTVisitor(this, unitName, root, scope, createLiteralReference);
		node.accept(visitor);
		return visitor.getResult();
	}
	
	/**
	 * Create and return all references for an AST node in a local scope. If the node is not in a local scope 
	 * (e.g. an initialize expression for a field definition), an empty list will be returned.
	 * <p> Note that the method createRefereceForExpressionASTNode() can be used to create references
	 * for expression nodes not in local scope.    
	 */
	public List<NameReference> createReferencesForASTNode(String unitName, ASTNode node) {
		CompilationUnit root = tableManager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitName);
		if (root == null) return new ArrayList<NameReference>();
		
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, root, unitName);
		NameScope scope = tableManager.getScopeOfLocation(start);
		if (scope.getScopeKind() != NameScopeKind.NSK_LOCAL) return new ArrayList<NameReference>(); 
		
		BlockReferenceASTVisitor visitor = new BlockReferenceASTVisitor(this, unitName, root, (LocalScope)scope, createLiteralReference);
		node.accept(visitor);
		return visitor.getResult();
	}

}
