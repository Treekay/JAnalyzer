package nameTable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import nameTable.creator.TypeDeclarationASTVisitor;
import nameTable.filter.NameDefinitionLocationFilter;
import nameTable.nameDefinition.AnonymousClassDefinition;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.SourceCodeLocation;

/**
 * A class to provide bridge between name table and AST nodes, such as finding AST node for name definitions, and find name definitions
 * or creating references for AST nodes.
 * 
 * @author Zhou Xiaocong
 * @since 2016年11月9日
 * @version 1.0
 * 
 * @update 2018/06/05
 * 		Support to finding AnonymousClassDeclaration for DetailedTypeDefinition
 */
public class NameTableASTBridge {
	private NameTableManager tableManager = null;
	
	public NameTableASTBridge(NameTableManager tableManager) {
		this.tableManager = tableManager;
	}

	/**
	 * Find the AST node (type : CompilationUnit) for the given compilation unit scope 
	 */
	public CompilationUnit findASTNodeForCompilationUnitScope(CompilationUnitScope scope) {
		return tableManager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(scope.getUnitName());
	}
	
	/**
	 * Find the AST node (type : CompilationUnit) for the given compilation unit name 
	 */
	public CompilationUnit findASTNodeForCompilationUnitScope(String unitName) {
		return tableManager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitName);
	}
	
	/**
	 * Find the AST node (type : TypeDeclaration) for the given detailed type definition (not a enum type definition) 
	 */
	public TypeDeclaration findASTNodeForDetailedTypeDefinition(DetailedTypeDefinition type) {
		if (type.isAnonymous()) return null;
		
		CompilationUnitScope unitScope = tableManager.getEnclosingCompilationUnitScope(type);
		String unitFullName = unitScope.getUnitName();
		CompilationUnit root = findASTNodeForCompilationUnitScope(unitScope);
		
		if (root == null) return null;
		
		TypeDeclarationASTVisitor visitor = new TypeDeclarationASTVisitor();
		root.accept(visitor);
		List<TypeDeclaration> resultList = visitor.getResultList();
		if (resultList == null) return null;
		for (TypeDeclaration resultType : resultList) {
			TypeDeclaration typeDecl = (TypeDeclaration)resultType;
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(resultType, root, unitFullName);
			String declName = typeDecl.getName().getIdentifier(); 
			if (declName.equals(type.getSimpleName()) && location.equals(type.getLocation())) {
				return typeDecl;
			}
		}
		return null;
	}

	/**
	 * Find the AST node (type : AnonymousClassDeclaration) for the given detailed type definition when it is an anonymous class 
	 */
	public AnonymousClassDeclaration findASTNodeForAnonymousClassDefinition(DetailedTypeDefinition type) {
		if (!type.isAnonymous()) return null;
		
		CompilationUnitScope unitScope = tableManager.getEnclosingCompilationUnitScope(type);
		String unitFullName = unitScope.getUnitName();
		CompilationUnit root = findASTNodeForCompilationUnitScope(unitScope);
		
		if (root == null) return null;
		
		TypeDeclarationASTVisitor visitor = new TypeDeclarationASTVisitor();
		root.accept(visitor);
		List<AnonymousClassDeclaration> resultList = visitor.getAnonyClassList();
		if (resultList == null) return null; 
		for (AnonymousClassDeclaration resultType : resultList) {
			AnonymousClassDeclaration typeDecl = (AnonymousClassDeclaration)resultType;
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(resultType, root, unitFullName);
			if (location.equals(type.getLocation())) return typeDecl;
		}
		return null;
	}
	
	/**
	 * Find the AST node (type : MethodDeclaration) for the given method definition 
	 */
	public MethodDeclaration findASTNodeForMethodDefinition(MethodDefinition method) {
		DetailedTypeDefinition type = tableManager.getEnclosingDetailedTypeDefinition(method);
		CompilationUnitScope unitScope = tableManager.getEnclosingCompilationUnitScope(type);
		String unitName = unitScope.getUnitName();
		CompilationUnit root = findASTNodeForCompilationUnitScope(unitScope);
		if (root == null) return null;

		if (type.isAnonymous()) {
			AnonymousClassDeclaration typeDeclaration = findASTNodeForAnonymousClassDefinition(type);
			if (typeDeclaration == null) return null;

			@SuppressWarnings("unchecked")
			List<BodyDeclaration> bodyList = typeDeclaration.bodyDeclarations();
			for (BodyDeclaration body : bodyList) {
				if (body.getNodeType() == ASTNode.METHOD_DECLARATION) {
					MethodDeclaration methodDecl = (MethodDeclaration)body;
					String methodDeclName = methodDecl.getName().getIdentifier();
					SourceCodeLocation location = SourceCodeLocation.getStartLocation(methodDecl, root, unitName);
					if (methodDeclName.equals(method.getSimpleName()) && location.equals(method.getLocation())) {
						return methodDecl; 
					}
				}
			}
			
		} else {
			TypeDeclaration typeDeclaration = findASTNodeForDetailedTypeDefinition(type);
			if (typeDeclaration == null) return null;

			MethodDeclaration[] methodDeclArray = typeDeclaration.getMethods();
			for (int index = 0; index < methodDeclArray.length; index++) {
				MethodDeclaration methodDecl = methodDeclArray[index];
				String methodDeclName = methodDecl.getName().getIdentifier();
				SourceCodeLocation location = SourceCodeLocation.getStartLocation(methodDecl, root, unitName);
				if (methodDeclName.equals(method.getSimpleName()) && location.equals(method.getLocation())) {
					return methodDecl; 
				}
			}
		}
		return null;
	}
	
	/**
	 * Find the AST node (type : FieldDeclaration) for the given field definition 
	 */
	public FieldDeclaration findASTNodeForFieldDefinition(FieldDefinition field) {
		DetailedTypeDefinition type = tableManager.getEnclosingDetailedTypeDefinition(field);
		CompilationUnitScope unitScope = tableManager.getEnclosingCompilationUnitScope(type);
		String unitName = unitScope.getUnitName();
		CompilationUnit root = findASTNodeForCompilationUnitScope(unitScope);
		if (root == null) return null;

		if (type.isAnonymous()) {
			AnonymousClassDeclaration typeDeclaration = findASTNodeForAnonymousClassDefinition(type);
			if (typeDeclaration == null) return null;

			@SuppressWarnings("unchecked")
			List<BodyDeclaration> bodyList = typeDeclaration.bodyDeclarations();
			for (BodyDeclaration body : bodyList) {
				if (body.getNodeType() == ASTNode.FIELD_DECLARATION) {
					FieldDeclaration fieldDecl = (FieldDeclaration)body;
					// Visit the variable list defined in the node
					@SuppressWarnings("unchecked")
					List<VariableDeclarationFragment> fragments = fieldDecl.fragments();
					for (VariableDeclarationFragment varNode : fragments) {
						String fieldDeclName = varNode.getName().getFullyQualifiedName();
						SourceCodeLocation location = SourceCodeLocation.getStartLocation(varNode, root, unitName);
						if (fieldDeclName.equals(field.getSimpleName()) && location.equals(field.getLocation())) {
							return fieldDecl; 
						}
					}
				}
			}
		} else {
			TypeDeclaration typeDeclaration = findASTNodeForDetailedTypeDefinition(type);
			if (typeDeclaration == null) return null;

			FieldDeclaration[] fieldDeclArray = typeDeclaration.getFields();
			for (int index = 0; index < fieldDeclArray.length; index++) {
				FieldDeclaration fieldDecl = fieldDeclArray[index];
				
				// Visit the variable list defined in the node
				@SuppressWarnings("unchecked")
				List<VariableDeclarationFragment> fragments = fieldDecl.fragments();
				for (VariableDeclarationFragment varNode : fragments) {
					String fieldDeclName = varNode.getName().getFullyQualifiedName();
					SourceCodeLocation location = SourceCodeLocation.getStartLocation(varNode, root, unitName);
					if (fieldDeclName.equals(field.getSimpleName()) && location.equals(field.getLocation())) {
						return fieldDecl; 
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Find the detailed type definition for the given type declaration AST node 
	 */
	public DetailedTypeDefinition findDefinitionForTypeDeclaration(String unitName, TypeDeclaration type) {
		String declFullName = type.getName().getIdentifier();
		CompilationUnit root = tableManager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitName);
		if (root == null) return null;
		
		CompilationUnitScope unit = tableManager.findCompilationUnitScopeByUnitName(unitName);
		if (unit == null) return null;

		SourceCodeLocation location = SourceCodeLocation.getStartLocation(type, root, unitName);

		List<DetailedTypeDefinition> typeList = tableManager.getAllDetailedTypeDefinitions(unit);
		for (DetailedTypeDefinition detailedType : typeList) {
			if (detailedType.getSimpleName().equals(declFullName) && detailedType.getLocation().equals(location)) return detailedType;
		}
		return null;
	}

	/**
	 * Find the detailed type definition for the given type declaration AST node when we know its compilation unit scope and its location 
	 */
	public DetailedTypeDefinition findDefinitionForTypeDeclaration(CompilationUnitScope unit, SourceCodeLocation location, TypeDeclaration type) {
		String declFullName = type.getName().getIdentifier();
		List<DetailedTypeDefinition> typeList = tableManager.getAllDetailedTypeDefinitions(unit);
		for (DetailedTypeDefinition detailedType : typeList) {
			if (detailedType.getSimpleName().equals(declFullName) && detailedType.getLocation().equals(location)) return detailedType;
		}
		return null;
	}
	
	/**
	 * Find the detailed type definition for the given type declaration AST node 
	 */
	public EnumTypeDefinition findDefinitionForEnumDeclaration(String unitName, EnumDeclaration type) {
		String declFullName = type.getName().getIdentifier();
		CompilationUnit root = tableManager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitName);
		if (root == null) return null;
		
		CompilationUnitScope unit = tableManager.findCompilationUnitScopeByUnitName(unitName);
		if (unit == null) return null;

		SourceCodeLocation location = SourceCodeLocation.getStartLocation(type, root, unitName);

		List<NameDefinition> definitionList = tableManager.getAllDefinitionsOfScope(unit);
		for (NameDefinition definition : definitionList) {
			if (definition.getSimpleName().equals(declFullName) && definition.getLocation().equals(location)) return (EnumTypeDefinition)definition;
		}
		return null;
	}

	/**
	 * Find the detailed type definition for the given type declaration AST node when we know its compilation unit scope and its location 
	 */
	public EnumTypeDefinition findDefinitionForEnumDeclaration(CompilationUnitScope unit, SourceCodeLocation location, EnumDeclaration type) {
		String declFullName = type.getName().getIdentifier();
		List<NameDefinition> definitionList = tableManager.getAllDefinitionsOfScope(unit);
		for (NameDefinition definition : definitionList) {
			if (definition.getSimpleName().equals(declFullName) && definition.getLocation().equals(location)) return (EnumTypeDefinition)definition;
		}
		return null;
	}
	

	/**
	 * Find the anonymous class definition the given anonymous class declaration AST node 
	 */
	public AnonymousClassDefinition findDefinitionForAnonymousClassDeclaration(String unitName, AnonymousClassDeclaration type) {
		CompilationUnit root = tableManager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitName);
		if (root == null) return null;
		
		CompilationUnitScope unit = tableManager.findCompilationUnitScopeByUnitName(unitName);
		if (unit == null) return null;

		SourceCodeLocation location = SourceCodeLocation.getStartLocation(type, root, unitName);
		String simpleName = AnonymousClassDefinition.getAnonymousClassSimpleName(location);

		List<DetailedTypeDefinition> typeList = tableManager.getAllDetailedTypeDefinitions(unit);
		for (DetailedTypeDefinition detailedType : typeList) {
			if (detailedType.isAnonymous()) {
				if (detailedType.getSimpleName().equals(simpleName) && detailedType.getLocation().equals(location)) return (AnonymousClassDefinition)detailedType;
			}
		}
		return null;
	}

	/**
	 * Find the anonymous class definition the given anonymous class declaration AST node when we know its compilation unit scope and its location 
	 */
	public AnonymousClassDefinition findDefinitionForAnonymousClassDeclaration(LocalScope scope, SourceCodeLocation location, AnonymousClassDeclaration type) {
		String simpleName = AnonymousClassDefinition.getAnonymousClassSimpleName(location);

		List<DetailedTypeDefinition> typeList = tableManager.getAllDetailedTypeDefinitions(scope);
		for (DetailedTypeDefinition detailedType : typeList) {
			if (detailedType.isAnonymous()) {
				if (detailedType.getSimpleName().equals(simpleName) && detailedType.getLocation().equals(location)) return (AnonymousClassDefinition)detailedType;
			}
		}
		return null;
	}
	
	/**
	 * Find the method definition for the given method declaration AST node 
	 */
	public MethodDefinition findDefinitionForMethodDeclaration(String unitName, MethodDeclaration method) {
		CompilationUnit root = tableManager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitName);
		if (root == null) return null;
		
		CompilationUnitScope unit = tableManager.findCompilationUnitScopeByUnitName(unitName);
		if (unit == null) return null;

		SourceCodeLocation location = SourceCodeLocation.getStartLocation(method, root, unitName);

		List<DetailedTypeDefinition> typeList = tableManager.getAllDetailedTypeDefinitions(unit);
		for (DetailedTypeDefinition type : typeList) {
			List<MethodDefinition> methodList = type.getMethodList();
			if (methodList == null) continue;
			for (MethodDefinition methodInType : methodList) {
				String methodSimpleName = method.getName().getIdentifier();
				if (methodInType.getSimpleName().equals(methodSimpleName) && methodInType.getLocation().equals(location)) return methodInType;
			}
		}
		
		return null;
	}
	
	/**
	 * Find the method definition for the given method declaration AST node when we know its detailed type definition and its location
	 */
	public MethodDefinition findDefinitionForMethodDeclaration(DetailedTypeDefinition type, SourceCodeLocation location, MethodDeclaration method) {
		List<MethodDefinition> methodList = type.getMethodList();
		if (methodList == null) return null;
		for (MethodDefinition methodInType : methodList) {
			String methodSimpleName = method.getName().getIdentifier();
			if (methodInType.getSimpleName().equals(methodSimpleName) && methodInType.getLocation().equals(location)) return methodInType;
		}
		
		return null;
	}

	/**
	 * Find the field definitions for the given field declaration AST node. Note that a field declaration AST node maybe include
	 * many field definitions 
	 */
	public List<FieldDefinition> findDefinitionsForFieldDeclaration(String unitName, FieldDeclaration field) {
		CompilationUnit root = tableManager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitName);
		if (root == null) return new ArrayList<FieldDefinition>();
		
		CompilationUnitScope unit = tableManager.findCompilationUnitScopeByUnitName(unitName);
		if (unit == null) return new ArrayList<FieldDefinition>();

		SourceCodeLocation start = SourceCodeLocation.getStartLocation(field, root, unitName);
		SourceCodeLocation end = SourceCodeLocation.getEndLocation(field, root, unitName);
		@SuppressWarnings("unchecked")
		List<VariableDeclarationFragment> fragments = field.fragments();
		List<FieldDefinition> result = new ArrayList<FieldDefinition>();

		List<DetailedTypeDefinition> typeList = tableManager.getAllDetailedTypeDefinitions(unit);
		for (DetailedTypeDefinition type : typeList) {
			List<FieldDefinition> fieldList = type.getFieldList();
			if (fieldList == null) continue;
			
			for (VariableDeclarationFragment varNode : fragments) {
				String varName = varNode.getName().getIdentifier();
				for (FieldDefinition fieldInType : fieldList) {
					if (fieldInType.getSimpleName().equals(varName) && fieldInType.getLocation().isBetween(start, end)) {
						result.add(fieldInType);
					}
				}
			}
			return result;
		}
		return new ArrayList<FieldDefinition>();
	}
	
	/**
	 * Find the field definitions for the given field declaration AST node when we know its detailed type definition and its start and end location
	 */
	public List<FieldDefinition> findDefinitionsForFieldDeclaration(DetailedTypeDefinition type, SourceCodeLocation startLocation, SourceCodeLocation endLocation, FieldDeclaration field) {
		@SuppressWarnings("unchecked")
		List<VariableDeclarationFragment> fragments = field.fragments();
		List<FieldDefinition> result = new ArrayList<FieldDefinition>();

		List<FieldDefinition> fieldList = type.getFieldList();
		if (fieldList == null) return result;
		
		for (VariableDeclarationFragment varNode : fragments) {
			String varName = varNode.getName().getIdentifier();
			for (FieldDefinition fieldInType : fieldList) {
				if (fieldInType.getSimpleName().equals(varName) && fieldInType.getLocation().isBetween(startLocation, endLocation)) {
					result.add(fieldInType);
				}
			}
		}
		return result;
	}

	/**
	 * Get all definitions in a AST node 
	 */
	public List<NameDefinition> getAllDefinitionsInASTNode(String unitName, ASTNode node) {
		CompilationUnit root = tableManager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitName);
		if (root == null) return null;
		
		CompilationUnitScope unit = tableManager.findCompilationUnitScopeByUnitName(unitName);
		if (unit == null) return null;

		SourceCodeLocation start = SourceCodeLocation.getStartLocation(node, root, unitName);
		SourceCodeLocation end = SourceCodeLocation.getEndLocation(node, root, unitName);

		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionLocationFilter(start, end));
		unit.accept(visitor);
		return visitor.getResult();
	}
}
