package analyzer.objectExpression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import analyzer.dataTable.DataTableManager;
import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.ExecutionPointLabel;
import graph.cfg.creator.CFGCreator;
import nameTable.NameTableASTBridge;
import nameTable.NameTableManager;
import nameTable.creator.ExpressionReferenceASTVisitor;
import nameTable.creator.NameReferenceCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameReference.referenceGroup.NameReferenceGroupKind;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.visitor.NameDefinitionVisitor;
import nameTable.filter.NameDefinitionLocationFilter;
import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeFileSet;
import sourceCodeAST.SourceCodeLocation;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018年6月2日
 * @version 1.0
 *
 */
public class ObjectExpressionCollector {
	protected int totalExpCounter = 0;
	protected int totalNodeCounter = 0;
	protected int totalPredicateCounter = 0;
	protected int totalDeclaration = 0;
	
	public void collectAllObjectReferenceExpressions(String path, PrintWriter writer) {
		NameTableManager manager = NameTableManager.createNameTableManager(path);
		SourceCodeFileSet sourceCodeFileSet = manager.getSouceCodeFileSet();

		writer.println("File\tLocation\tExpression\tDereference\tPolynomial\tType\tKind\tUsage\tDefinition\tClass\tMethod");
		Debug.println("File\tClass\tMethod\tNodes\tPredicates\tExpressions");
		
		List<CompilationUnitScope> unitList = manager.getAllCompilationUnitScopes();
		for (CompilationUnitScope unitScope : unitList) {
			String unitFileName = unitScope.getUnitName();
			
			List<ObjectExpressionRecorder> recordList = collectAllObjectReferenceExpressions(manager, unitScope);
			totalExpCounter += recordList.size();
			sourceCodeFileSet.releaseAST(unitFileName);
			sourceCodeFileSet.releaseFileContent(unitFileName);
			
			for (ObjectExpressionRecorder expressionRecord : recordList) {
				String locationString = null;
				String expression = null;
				String type = null;
				String definitionString = null;
				if (!expressionRecord.isObjectDefinition()) {
					NameReference reference = ((ObjectReferenceRecorder)expressionRecord).reference;
					SourceCodeLocation location = reference.getLocation(); 
					locationString = "[" + location.getLineNumber() + " : " + location.getColumn() + "]";
					expression = DataTableManager.replaceSpaceWithLaTeXSpace(reference.toSimpleString());
					type = "~";
					TypeReference resultType = reference.getResultTypeReference(); 
					if (resultType != null) type = resultType.toDeclarationString();
					else {
						TypeDefinition resultTypeDef = reference.getResultTypeDefinition();
						if (resultTypeDef != null) type = resultTypeDef.getSimpleName();
					}
					definitionString = reference.getDefinition().getUniqueId();
				} else {
					NameDefinition definition = ((ObjectDefinitionRecorder)expressionRecord).definition;
					SourceCodeLocation location = definition.getLocation(); 
					locationString = "[" + location.getLineNumber() + " : " + location.getColumn() + "]";
					expression = definition.getSimpleName();
					type = definition.getDeclareTypeReference().toDeclarationString();
					definitionString = definition.getUniqueId();
				}
				
				writer.println(unitFileName + "\t" + locationString + "\t" + expression + "\t" + expressionRecord.isDereference + "\t" + expressionRecord.isPolynomial + "\t" + type + "\t" + 
							expressionRecord.kind + "\t" + expressionRecord.usage + "\t" + definitionString + "\t" + 
							expressionRecord.className + "\t" + expressionRecord.methodName);
			}
		}
		writer.flush();
		Debug.println("Collected\tnodes\t" + totalNodeCounter + "\tpredicates\t" + totalPredicateCounter + "\tdeclaration\t" + totalDeclaration + "\texpressions\t" + totalExpCounter + "\ttime\t" + Debug.time());
		totalExpCounter = 0;
		totalNodeCounter = 0;
		totalPredicateCounter = 0;
		totalDeclaration = 0;
	}
	
	@SuppressWarnings("unchecked")
	List<ObjectExpressionRecorder> collectAllObjectReferenceExpressions(NameTableManager manager, CompilationUnitScope unitScope) {
		List<ObjectExpressionRecorder> result = new ArrayList<ObjectExpressionRecorder>();

		String unitFileName = unitScope.getUnitName();
		CompilationUnit astRoot = manager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitFileName);
		CompilationUnitRecorder unitRecorder = new CompilationUnitRecorder(unitFileName, astRoot);
		
		NameTableASTBridge bridge = new NameTableASTBridge(manager);
		
		List<DetailedTypeDefinition> typeList = manager.getAllDetailedTypeDefinitions(unitScope);
		for (DetailedTypeDefinition type : typeList) {
			if (type.isInterface()) continue;
			
			String className = type.getSimpleName();
			// Process the field declaration in the class
			List<FieldDefinition> fieldList = type.getFieldList();
			if (fieldList != null) {
				totalDeclaration += fieldList.size();
				for (FieldDefinition field : fieldList) {
					ObjectExpressionKind kind = getObjectDeclarationKind(field);
					NameReference initializer = field.getInitializer();
					if (kind != ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION) {
						ObjectDefinitionRecorder record = new ObjectDefinitionRecorder();
						record.definition = field;
						record.className = className;
						record.methodName = "~";
						record.isDereference = false;
						record.kind = kind;
						if (initializer != null) record.usage = ObjectExpressionUsageKind.OEUK_LEFT_VALUE;
						else record.usage = ObjectExpressionUsageKind.OEUK_UNKNOWN; 
						result.add(record);
					}
					
					if (initializer != null) {
						List<ObjectExpressionRecorder> objExpList = getObjectExpressionListInReference(initializer, false, false, ObjectExpressionUsageKind.OEUK_RIGHT_VALUE, className, "~");
						result.addAll(objExpList);
					}
				}
			}

			List<BodyDeclaration> bodyList = null;
			
			if (type.isAnonymous()) {
				AnonymousClassDeclaration classDeclaration = bridge.findASTNodeForAnonymousClassDefinition(type);
				if (classDeclaration == null) {
					throw new AssertionError("Can not find declaration for anonymous class " + type.getUniqueId());
				}
				
				bodyList = classDeclaration.bodyDeclarations();
			} else {
				TypeDeclaration classDeclaration = bridge.findASTNodeForDetailedTypeDefinition(type);;
				if (classDeclaration == null) {
					throw new AssertionError("Can not find declaration for class " + type.getUniqueId());
				}
				
				bodyList = classDeclaration.bodyDeclarations();
			}
			if (bodyList == null) continue;
			
			for (BodyDeclaration bodyDecl : bodyList) {
				if (bodyDecl.getNodeType() == ASTNode.INITIALIZER) {
					Initializer initializer = (Initializer)bodyDecl;
					SourceCodeLocation location = SourceCodeLocation.getStartLocation(bodyDecl, astRoot, unitFileName);
					String id = "Initializer@" + location;
					Block body = initializer.getBody();
					CFGCreator cfgCreator = new CFGCreator(unitFileName, astRoot);
					ControlFlowGraph cfg = cfgCreator.create(body, id, id, id);
					
					List<ObjectExpressionRecorder> objExpList = collectAllObjectReferenceExpressions(manager, unitRecorder, className, id, cfg); 
					result.addAll(objExpList);
				} else if (bodyDecl.getNodeType() == ASTNode.METHOD_DECLARATION) {
					MethodDeclaration methodDeclaration = (MethodDeclaration)bodyDecl;
					SourceCodeLocation methodLocation = SourceCodeLocation.getStartLocation(methodDeclaration, astRoot, unitFileName);
					MethodDefinition method = bridge.findDefinitionForMethodDeclaration(type, methodLocation, methodDeclaration);
					String methodName = method.getSimpleName() + "@" + methodLocation;
					
					List<VariableDefinition> parameterList = method.getParameterList();
					if (parameterList != null) {
						totalDeclaration += parameterList.size();
						for (VariableDefinition parameter : parameterList) {
							ObjectExpressionKind kind = getObjectDeclarationKind(parameter);
							if (kind != ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION) {
								ObjectDefinitionRecorder record = new ObjectDefinitionRecorder();
								record.definition = parameter;
								record.className = className;
								record.methodName = methodName;
								record.isDereference = false;
								record.kind = kind;
								record.usage = ObjectExpressionUsageKind.OEUK_UNKNOWN;
								result.add(record);
							}
						}
					}
					
					CFGCreator cfgCreator = new CFGCreator(unitFileName, astRoot);
					ControlFlowGraph cfg = cfgCreator.create(methodDeclaration, className);
					if (cfg == null) continue;
					List<ObjectExpressionRecorder> objExpList = collectAllObjectReferenceExpressions(manager, unitRecorder, className, methodName, cfg);
					result.addAll(objExpList);
				}
			}
			// We do not process types in a type. All types will be processed in the compilation unit scope!
		}
		return result;
	}
	
	List<ObjectExpressionRecorder> collectAllObjectReferenceExpressions(NameTableManager manager, CompilationUnitRecorder unitRecorder, String className, String methodName, ControlFlowGraph cfg) {
		List<ObjectExpressionRecorder> result = new ArrayList<ObjectExpressionRecorder>();
		int nodeCounter = 0;
		int predicateCounter = 0;
		
		List<GraphNode> nodeList = cfg.getAllNodes();
		for (GraphNode graphNode : nodeList) {
			ExecutionPoint node = (ExecutionPoint)graphNode;
			if (node.isVirtual()) continue;
			nodeCounter++;
			if (node.isPredicate()) predicateCounter++;
			
			ASTNode astNode = node.getAstNode();
			if (astNode == null) continue;
			
			SourceCodeLocation startLocation = node.getStartLocation();
			SourceCodeLocation endLocation = node.getEndLocation();
			NameScope currentScope = manager.getScopeOfLocation(startLocation);
			NameReferenceCreator referenceCreator = new NameReferenceCreator(manager, true);
			ExpressionReferenceASTVisitor referenceVisitor = new ExpressionReferenceASTVisitor(referenceCreator, unitRecorder, currentScope, true);

			int nodeType = astNode.getNodeType();
			// We do not process types in a method or an initializer. All types will be processed in the compilation unit scope!
			if (nodeType == ASTNode.ANONYMOUS_CLASS_DECLARATION || nodeType == ASTNode.TYPE_DECLARATION || nodeType == ASTNode.TYPE_DECLARATION_STATEMENT) continue;
			else if (nodeType == ASTNode.ASSIGNMENT) {
				Assignment assignment = (Assignment)astNode;
				Expression leftHandSide = assignment.getLeftHandSide();
				referenceVisitor.reset();
				leftHandSide.accept(referenceVisitor);
				NameReference leftReference = referenceVisitor.getResult();
				leftReference.resolveBinding();
				List<ObjectExpressionRecorder> objExpList = getObjectExpressionListInReference(leftReference, false, false, ObjectExpressionUsageKind.OEUK_LEFT_VALUE, className, methodName);
				result.addAll(objExpList);

				Expression rightHandSide = assignment.getRightHandSide();
				referenceVisitor.reset();
				rightHandSide.accept(referenceVisitor);
				NameReference rightReference = referenceVisitor.getResult();
				rightReference.resolveBinding();
				objExpList = getObjectExpressionListInReference(rightReference, false, false, ObjectExpressionUsageKind.OEUK_RIGHT_VALUE, className, methodName);
				result.addAll(objExpList);
			} else if (nodeType == ASTNode.ENHANCED_FOR_STATEMENT && node.getLabel().equals(ExecutionPointLabel.ENHANCED_FOR_PREDICATE)) {
				EnhancedForStatement enhancedForStatement = (EnhancedForStatement)astNode;
				SingleVariableDeclaration parameter = enhancedForStatement.getParameter();
				Expression expression = enhancedForStatement.getExpression();
				
				NameDefinitionVisitor definitionVisitor = new NameDefinitionVisitor();
				NameDefinitionLocationFilter filter = new NameDefinitionLocationFilter(startLocation, endLocation);
				definitionVisitor.setFilter(filter);
				currentScope.accept(definitionVisitor);
				List<NameDefinition> variableList = definitionVisitor.getResult();

				boolean found = false;
				for (NameDefinition variable : variableList) {
					if (variable.getSimpleName().equals(parameter.getName().getIdentifier())) {
						found = true;
						// Find the definition of this variable declaration fragment, if its type is not primitive, then add it as
						// an object expressions
						ObjectExpressionKind kind = getObjectDeclarationKind(variable);
						if (kind != ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION) {
							ObjectDefinitionRecorder record = new ObjectDefinitionRecorder();
							record.definition = variable;
							record.className = className;
							record.methodName = methodName;
							record.isDereference = false;
							record.kind = kind;
							record.usage = ObjectExpressionUsageKind.OEUK_LEFT_VALUE;
							result.add(record);
						}
						totalDeclaration++;
						break;
					}
				}
				if (!found) {
					throw new AssertionError("Can not find variable definition for enhanced for parameter: " + parameter.toString() + " at " + startLocation.getUniqueId());
				}

				// Create reference for its initializer expression, and add the object reference expressions in this initializer expression
				referenceVisitor.reset();
				expression.accept(referenceVisitor);
				NameReference valueReference = referenceVisitor.getResult();
				valueReference.resolveBinding();
				List<ObjectExpressionRecorder> objExpList = getObjectExpressionListInReference(valueReference, true, false, ObjectExpressionUsageKind.OEUK_RIGHT_VALUE, className, methodName);
				result.addAll(objExpList);
			} else if (nodeType == ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
				VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)astNode;
				
				NameDefinitionVisitor definitionVisitor = new NameDefinitionVisitor();
				NameDefinitionLocationFilter filter = new NameDefinitionLocationFilter(startLocation, endLocation);
				definitionVisitor.setFilter(filter);
				currentScope.accept(definitionVisitor);
				List<NameDefinition> variableList = definitionVisitor.getResult();
				
				@SuppressWarnings("unchecked")
				List<VariableDeclarationFragment> fragmentList = variableDeclarationExpression.fragments();
				for (VariableDeclarationFragment fragment : fragmentList) {
					Expression initializer = fragment.getInitializer();
					boolean found = false;
					for (NameDefinition variable : variableList) {
						if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
							found = true;
							// Find the definition of this variable declaration fragment, if its type is not primitive, then add it as
							// an object expressions
							ObjectExpressionKind kind = getObjectDeclarationKind(variable);
							if (kind != ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION) {
								ObjectDefinitionRecorder record = new ObjectDefinitionRecorder();
								record.definition = variable;
								record.className = className;
								record.methodName = methodName;
								record.isDereference = false;
								record.kind = kind;
								if (initializer != null) record.usage = ObjectExpressionUsageKind.OEUK_LEFT_VALUE;
								else record.usage = ObjectExpressionUsageKind.OEUK_UNKNOWN;
								result.add(record);
							}
							totalDeclaration++;
							break;
						}
					}
					if (!found) {
						throw new AssertionError("Can not find variable definition for variable declaration: " + fragment.toString() + " at " + startLocation.getUniqueId());
					}

					if (initializer == null) continue;
					referenceVisitor.reset();
					initializer.accept(referenceVisitor);
					NameReference valueReference = referenceVisitor.getResult();
					valueReference.resolveBinding();
					List<ObjectExpressionRecorder> objExpList = getObjectExpressionListInReference(valueReference, false, false, ObjectExpressionUsageKind.OEUK_RIGHT_VALUE, className, methodName);
					result.addAll(objExpList);
				}
			} else if (nodeType == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
				VariableDeclarationFragment fragment = (VariableDeclarationFragment)astNode;
				
				NameDefinitionVisitor definitionVisitor = new NameDefinitionVisitor();
				NameDefinitionLocationFilter filter = new NameDefinitionLocationFilter(startLocation, endLocation);
				definitionVisitor.setFilter(filter);
				currentScope.accept(definitionVisitor);
				List<NameDefinition> variableList = definitionVisitor.getResult();
				
				Expression initializer = fragment.getInitializer();
				boolean found = false;
				for (NameDefinition variable : variableList) {
					if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
						found = true;
						// Find the definition of this variable declaration fragment, if its type is not primitive, then add it as
						// an object expressions
						ObjectExpressionKind kind = getObjectDeclarationKind(variable);
						if (kind != ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION) {
							ObjectDefinitionRecorder record = new ObjectDefinitionRecorder();
							record.definition = variable;
							record.className = className;
							record.methodName = methodName;
							record.isDereference = false;
							record.kind = kind;
							if (initializer != null) record.usage = ObjectExpressionUsageKind.OEUK_LEFT_VALUE;
							else record.usage = ObjectExpressionUsageKind.OEUK_UNKNOWN;
							result.add(record);
						}
						totalDeclaration++;
						break;
					}
				}
				if (!found) {
					throw new AssertionError("Can not find variable definition for variable declaration: " + fragment.toString() + " at " + startLocation.getUniqueId());
				}

				if (initializer == null) continue;
				referenceVisitor.reset();
				initializer.accept(referenceVisitor);
				NameReference valueReference = referenceVisitor.getResult();
				valueReference.resolveBinding();
				List<ObjectExpressionRecorder> objExpList = getObjectExpressionListInReference(valueReference, false, false, ObjectExpressionUsageKind.OEUK_RIGHT_VALUE, className, methodName);
				result.addAll(objExpList);
			} else if (nodeType == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
				VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)astNode;
				
				NameDefinitionVisitor definitionVisitor = new NameDefinitionVisitor();
				NameDefinitionLocationFilter filter = new NameDefinitionLocationFilter(startLocation, endLocation);
				definitionVisitor.setFilter(filter);
				currentScope.accept(definitionVisitor);
				List<NameDefinition> variableList = definitionVisitor.getResult();
				
				@SuppressWarnings("unchecked")
				List<VariableDeclarationFragment> fragmentList = variableDeclarationStatement.fragments();
				for (VariableDeclarationFragment fragment : fragmentList) {
					Expression initializer = fragment.getInitializer();
					boolean found = false;
					for (NameDefinition variable : variableList) {
						if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
							found = true;
							// Find the definition of this variable declaration fragment, if its type is not primitive, then add it as
							// an object expressions
							ObjectExpressionKind kind = getObjectDeclarationKind(variable);
							if (kind != ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION) {
								ObjectDefinitionRecorder record = new ObjectDefinitionRecorder();
								record.definition = variable;
								record.className = className;
								record.methodName = methodName;
								record.isDereference = false;
								record.kind = kind;
								if (initializer != null) record.usage = ObjectExpressionUsageKind.OEUK_LEFT_VALUE;
								else record.usage = ObjectExpressionUsageKind.OEUK_UNKNOWN;
								result.add(record);
							}
							totalDeclaration++;
							break;
						}
					}
					if (!found) {
						throw new AssertionError("Can not find variable definition for variable declaration: " + fragment.toString() + " at " + startLocation.getUniqueId());
					}

					if (initializer == null) continue;
					referenceVisitor.reset();
					initializer.accept(referenceVisitor);
					NameReference valueReference = referenceVisitor.getResult();
					valueReference.resolveBinding();
					List<ObjectExpressionRecorder> objExpList = getObjectExpressionListInReference(valueReference, false, false, ObjectExpressionUsageKind.OEUK_RIGHT_VALUE, className, methodName);
					result.addAll(objExpList);
				}
			} else if (nodeType == ASTNode.RETURN_STATEMENT || nodeType == ASTNode.THROW_STATEMENT) {
				List<NameReference> referenceList = referenceCreator.createReferencesForASTNode(unitRecorder.unitName, astNode);
				for (NameReference reference : referenceList) {
					reference.resolveBinding();
					List<ObjectExpressionRecorder> objExpList = getObjectExpressionListInReference(reference, false, false, ObjectExpressionUsageKind.OEUK_RETURN_VALUE, className, methodName);
					result.addAll(objExpList);
				}
			} else if (nodeType == ASTNode.SUPER_CONSTRUCTOR_INVOCATION || nodeType == ASTNode.CONSTRUCTOR_INVOCATION) {
				List<NameReference> referenceList = referenceCreator.createReferencesForASTNode(unitRecorder.unitName, astNode);
				for (NameReference reference : referenceList) {
					reference.resolveBinding();
					List<ObjectExpressionRecorder> objExpList = getObjectExpressionListInReference(reference, false, false, ObjectExpressionUsageKind.OEUK_ARGUMENT, className, methodName);
					result.addAll(objExpList);
				}
			} else {
				List<NameReference> referenceList = referenceCreator.createReferencesForASTNode(unitRecorder.unitName, astNode);
				for (NameReference reference : referenceList) {
					reference.resolveBinding();
					List<ObjectExpressionRecorder> objExpList = getObjectExpressionListInReference(reference, false, false, ObjectExpressionUsageKind.OEUK_UNKNOWN, className, methodName);
					result.addAll(objExpList);
				}
			}
		}
		Debug.println(unitRecorder.unitName + "\t" + className + "\t" + methodName + "\t" + nodeCounter + "\t" + predicateCounter + "\t" + result.size());
		totalNodeCounter += nodeCounter;
		totalPredicateCounter += predicateCounter;
		return result;
	}
	
	List<ObjectExpressionRecorder> getObjectExpressionListInReference(NameReference reference, boolean isDereference, boolean isPolynomial, ObjectExpressionUsageKind usage, String className, String methodName) {
		List<ObjectExpressionRecorder> result = new ArrayList<ObjectExpressionRecorder>();

		// Get the sub access path references in the current reference. Note that we will determine if it is dereference and the usage kind 
		// of the sub access path references in method getSubAccessPathReferenceList()
		List<ObjectExpressionRecorder> subreferenceList = getPossibleSubObjectReferenceList(reference, usage);
		if (subreferenceList != null) {
			for (ObjectExpressionRecorder subreferenceRecorder : subreferenceList) {
				NameReference subreference = ((ObjectReferenceRecorder)subreferenceRecorder).reference;
				if (subreference != null) {
					List<ObjectExpressionRecorder> subresult = getObjectExpressionListInReference(subreference, subreferenceRecorder.isDereference, subreferenceRecorder.isPolynomial, subreferenceRecorder.usage, className, methodName);
					result.addAll(subresult);
				} else {
					System.out.println("Null sub reference in " + reference + ", kind : " + reference.getReferenceKind());
				}
			}
		}

		// Note that getSubAccessPathReferenceList() do not actually determine if a reference is an object reference or add a reference 
		// to the final result list of ObjectExpressionRecorder! All object reference will be added to the final result after
		// call getObjectExpressionKind() to check if it is an object reference and determine its kind
		ObjectExpressionKind objKind = getObjectExpressionKind(reference);
		if (objKind != ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION) {
			ObjectReferenceRecorder record = new ObjectReferenceRecorder();
			record.className = className;
			record.isDereference = isDereference;
			record.isPolynomial = isPolynomial;
			record.kind = objKind;
			record.methodName = methodName;
			record.reference = reference;
			record.usage = usage;
			result.add(record);
		} 
		return result;
	}
	
	List<ObjectExpressionRecorder> getPossibleSubObjectReferenceList(NameReference reference, ObjectExpressionUsageKind usage) {
		List<ObjectExpressionRecorder> result = new ArrayList<ObjectExpressionRecorder>();
		if (!reference.isGroupReference()) {
			List<NameReference> subreferenceList = reference.getSubReferenceList();
			for (NameReference subreference : subreferenceList) {
				ObjectReferenceRecorder record = new ObjectReferenceRecorder();
				record.reference = subreference;
				record.isDereference = false;
				record.usage = usage;
				result.add(record);
			}
			return result;
		}
		
		NameReferenceGroup group = (NameReferenceGroup)reference;
		NameReferenceGroupKind kind = group.getGroupKind();
		List<NameReference> subreferenceList = group.getSubReferenceList();
		if (subreferenceList == null) return null;
		
		if (kind == NameReferenceGroupKind.NRGK_FIELD_ACCESS || kind == NameReferenceGroupKind.NRGK_QUALIFIED_NAME || 
				kind == NameReferenceGroupKind.NRGK_SUPER_FIELD_ACCESS) {
			NameReference subreference = subreferenceList.get(0);
			NameReferenceKind subkind = subreference.getReferenceKind();
			if (subkind != NameReferenceKind.NRK_TYPE && subkind != NameReferenceKind.NRK_LITERAL) {
				ObjectReferenceRecorder record = new ObjectReferenceRecorder();
				record.reference = subreference;
				record.isDereference = true;
				record.usage = ObjectExpressionUsageKind.OEUK_ACCESSING_FIELD;
				result.add(record);
			}
		} else if (kind == NameReferenceGroupKind.NRGK_METHOD_INVOCATION || kind == NameReferenceGroupKind.NRGK_SUPER_METHOD_INVOCATION) {
			boolean isPolynomial = false;
			for (NameReference subreference : subreferenceList) {
				NameReferenceKind subkind = subreference.getReferenceKind();
				if (subkind == NameReferenceKind.NRK_LITERAL || subkind == NameReferenceKind.NRK_TYPE) continue;
				if (subkind == NameReferenceKind.NRK_METHOD) {
					MethodReference methodReference = (MethodReference)subreference;
					List<NameReference> subreferenceInMethodList = methodReference.getSubReferenceList();
					for (NameReference referenceInMethod : subreferenceInMethodList) {
						subkind = referenceInMethod.getReferenceKind();
						if (subkind == NameReferenceKind.NRK_LITERAL || subkind == NameReferenceKind.NRK_TYPE) continue;
						ObjectReferenceRecorder record = new ObjectReferenceRecorder();
						record.reference = referenceInMethod;
						record.isDereference = false;
						record.usage = ObjectExpressionUsageKind.OEUK_ARGUMENT;
						result.add(record);
					}
					List<MethodDefinition> bindMethodList = methodReference.getAlternativeList(); 
					if (bindMethodList != null) {
						if (bindMethodList.size() > 1) isPolynomial = true;
					}
				}
			}
			for (NameReference subreference : subreferenceList) {
				NameReferenceKind subkind = subreference.getReferenceKind();
				if (subkind == NameReferenceKind.NRK_LITERAL || subkind == NameReferenceKind.NRK_TYPE) continue;
				if (subkind == NameReferenceKind.NRK_METHOD) continue;
				ObjectReferenceRecorder record = new ObjectReferenceRecorder();
				record.reference = subreference;
				record.isDereference = true;
				record.isPolynomial = isPolynomial;
				record.usage = ObjectExpressionUsageKind.OEUK_CALLING_METHOD;
				result.add(record);
			}
		} else if (kind == NameReferenceGroupKind.NRGK_CLASS_INSTANCE_CREATION) {
			for (NameReference subreference : subreferenceList) {
				NameReferenceKind subkind = subreference.getReferenceKind();
				if (subkind == NameReferenceKind.NRK_LITERAL || subkind == NameReferenceKind.NRK_TYPE) continue;
				if (subkind == NameReferenceKind.NRK_METHOD) {
					MethodReference methodReference = (MethodReference)subreference;
					List<NameReference> subreferenceInMethodList = methodReference.getSubReferenceList();
					for (NameReference referenceInMethod : subreferenceInMethodList) {
						subkind = referenceInMethod.getReferenceKind();
						if (subkind == NameReferenceKind.NRK_LITERAL || subkind == NameReferenceKind.NRK_TYPE) continue;
						ObjectReferenceRecorder record = new ObjectReferenceRecorder();
						record.reference = referenceInMethod;
						record.isDereference = false;
						record.usage = ObjectExpressionUsageKind.OEUK_ARGUMENT;
						result.add(record);
					}
				}
			}
		} else if (kind == NameReferenceGroupKind.NRGK_CAST){
			NameReference subreference = subreferenceList.get(1);
			NameReferenceKind subkind = subreference.getReferenceKind();
			if (subkind != NameReferenceKind.NRK_LITERAL) {
				ObjectReferenceRecorder record = new ObjectReferenceRecorder();
				record.reference = subreference;
				record.isDereference = false;
				record.usage = ObjectExpressionUsageKind.OEUK_CASTING_TYPE;
				result.add(record);
			}
		} else if (kind == NameReferenceGroupKind.NRGK_ARRAY_ACCESS){
			NameReference subreference = subreferenceList.get(0);
			ObjectReferenceRecorder record = new ObjectReferenceRecorder();
			record.reference = subreference;
			record.isDereference = false;
			record.usage = ObjectExpressionUsageKind.OEUK_ACCESSING_ARRAY;
			result.add(record);
		} else if (kind == NameReferenceGroupKind.NRGK_INFIX_EXPRESSION){
			String operator = group.getOperator();
			NameReference firstOperand = subreferenceList.get(0);
			NameReference secondOperand = subreferenceList.get(1);
			if (operator.equals(NameReferenceGroup.OPERATOR_EQUALS) || operator.equals(NameReferenceGroup.OPERATOR_NOT_EQUALS)) {
				if (firstOperand.isNullReference()) {
					ObjectReferenceRecorder record = new ObjectReferenceRecorder();
					record.reference = secondOperand;
					record.isDereference = false;
					record.usage = ObjectExpressionUsageKind.OEUK_CHECKING_NULL;
					result.add(record);
				} else if (secondOperand.isNullReference()) {
					ObjectReferenceRecorder record = new ObjectReferenceRecorder();
					record.reference = firstOperand;
					record.isDereference = false;
					record.usage = ObjectExpressionUsageKind.OEUK_CHECKING_NULL;
					result.add(record);
				} else {
					ObjectReferenceRecorder record = new ObjectReferenceRecorder();
					record.reference = firstOperand;
					record.isDereference = false;
					record.usage = ObjectExpressionUsageKind.OEUK_CHECKING_ALIAS;
					result.add(record);
					record = new ObjectReferenceRecorder();
					record.reference = secondOperand;
					record.isDereference = false;
					record.usage = ObjectExpressionUsageKind.OEUK_CHECKING_ALIAS;
					result.add(record);
				}
			} else {
				for (NameReference subreference : subreferenceList) {
					ObjectReferenceRecorder record = new ObjectReferenceRecorder();
					record.reference = subreference;
					record.isDereference = false;
					// Only String can be object expressions in an infix expressions!
					record.usage = ObjectExpressionUsageKind.OEUK_STRING_OPERAND;
					result.add(record);
				}
			}
		} else if (kind == NameReferenceGroupKind.NRGK_INSTANCEOF){
			NameReference subreference = subreferenceList.get(0);
			ObjectReferenceRecorder record = new ObjectReferenceRecorder();
			record.reference = subreference;
			record.isDereference = false;
			record.usage = ObjectExpressionUsageKind.OEUK_CHECKING_TYPE;
			result.add(record);
		} else if (kind == NameReferenceGroupKind.NRGK_ARRAY_CREATION){
			for (NameReference subreference : subreferenceList) {
				ObjectReferenceRecorder record = new ObjectReferenceRecorder();
				record.reference = subreference;
				record.isDereference = false;
				record.usage = ObjectExpressionUsageKind.OEUK_RIGHT_VALUE;
				result.add(record);
			}
		} else if (kind == NameReferenceGroupKind.NRGK_ARRAY_INITIALIZER){
			for (NameReference subreference : subreferenceList) {
				ObjectReferenceRecorder record = new ObjectReferenceRecorder();
				record.reference = subreference;
				record.isDereference = false;
				record.usage = ObjectExpressionUsageKind.OEUK_RIGHT_VALUE;
				result.add(record);
			}
		} else if (kind == NameReferenceGroupKind.NRGK_CONDITIONAL){
			for (NameReference subreference : subreferenceList) {
				ObjectReferenceRecorder record = new ObjectReferenceRecorder();
				record.reference = subreference;
				record.isDereference = false;
				record.usage = ObjectExpressionUsageKind.OEUK_RIGHT_VALUE;
				result.add(record);
			}
		} else {
			for (NameReference subreference : subreferenceList) {
				ObjectReferenceRecorder record = new ObjectReferenceRecorder();
				record.reference = subreference;
				record.isDereference = false;
				record.usage = ObjectExpressionUsageKind.OEUK_UNKNOWN;
				result.add(record);
			}
		}
		
		return result;
	}
	
	ObjectExpressionKind getObjectExpressionKind(NameReference reference) {
		if (!reference.isResolved()) return ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION;
		
		NameReferenceKind kind = reference.getReferenceKind();
		if (kind == NameReferenceKind.NRK_LITERAL || kind == NameReferenceKind.NRK_PACKAGE ||
				kind == NameReferenceKind.NRK_TYPE || kind == NameReferenceKind.NRK_UNKNOWN) {
//			System.out.println("\tReference kind is " + kind + ", " + reference.toSimpleString());
			return ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION;
		}
		NameDefinition definition = reference.getDefinition();
		if (definition == null) return ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION;
		if (kind == NameReferenceKind.NRK_GROUP) {
			if (!definition.isTypeDefinition()) return ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION;
			TypeDefinition typeDefinition = (TypeDefinition)definition;
			// Note that, for ary[i], where ary is an array with more than two dimensions and its base type is a primitive type 
			// (e.g. int[][] ary), here will return OEK_NOT_OBJECT_EXPRESSION, since we bind ary[i] to its base type (i.e. int) 
			// rather than its real type (i.e. int[]) 
			if (typeDefinition.isPrimitive() || typeDefinition.isEnumType()) return ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION;

			NameReferenceGroup group = (NameReferenceGroup)reference;
			NameReferenceGroupKind groupKind = group.getGroupKind();
			if (groupKind == NameReferenceGroupKind.NRGK_ARRAY_ACCESS) return ObjectExpressionKind.OEK_ARRAY_ACCESS;
			else if (groupKind == NameReferenceGroupKind.NRGK_ARRAY_CREATION) return ObjectExpressionKind.OEK_ARRAY_CREATION;
			else if (groupKind == NameReferenceGroupKind.NRGK_CLASS_INSTANCE_CREATION) return ObjectExpressionKind.OEK_INSTANCE_CREATION;
			else if (groupKind == NameReferenceGroupKind.NRGK_CONDITIONAL) return ObjectExpressionKind.OEK_CONDITIONAL;
			else if (groupKind == NameReferenceGroupKind.NRGK_FIELD_ACCESS) return ObjectExpressionKind.OEK_FIELD_ACCESS;
			else if (groupKind == NameReferenceGroupKind.NRGK_METHOD_INVOCATION) return ObjectExpressionKind.OEK_METHOD_INVOCATION;
			else if (groupKind == NameReferenceGroupKind.NRGK_QUALIFIED_NAME) return ObjectExpressionKind.OEK_FIELD_ACCESS;
			else if (groupKind == NameReferenceGroupKind.NRGK_CAST) return ObjectExpressionKind.OEK_TYPE_CAST;
			else return ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION;
		}
		TypeReference typeReference = definition.getDeclareTypeReference();
		if (typeReference != null) {
			if (!typeReference.isReferToPrimitiveType()) return ObjectExpressionKind.OEK_SIMPLE_NAME;;
		}
		return ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION;
	}
	
	ObjectExpressionKind getObjectDeclarationKind(NameDefinition definition) {
		TypeReference typeReference = definition.getDeclareTypeReference();
		if (typeReference.isReferToPrimitiveType()) return ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION;
		if (!typeReference.isArrayType()) return ObjectExpressionKind.OEK_VARIABLE_DECLARATION;
		else {
			TypeDefinition typeDefinition = definition.getDeclareTypeDefinition();
			if (typeDefinition != null) {
				if (typeDefinition.isPrimitive()) return ObjectExpressionKind.OEK_PRIMITIVE_ARRAY_DECLARATION;
			}
			return ObjectExpressionKind.OEK_OBJECTIVE_ARRAY_DECLARATION;
		}
	}

	public static String[] getAllObjectExpressionsKind() {
		ObjectExpressionKind[] kindValueArray = ObjectExpressionKind.values();
		String[] result = new String[kindValueArray.length];
		for (int i = 0; i < result.length; i++) result[i] = kindValueArray[i].toString();
		return result;
	}
	
	public static String[] getAllObjectExpressionsUsageKind() {
		ObjectExpressionUsageKind[] kindValueArray = ObjectExpressionUsageKind.values();
		String[] result = new String[kindValueArray.length];
		for (int i = 0; i < result.length; i++) result[i] = kindValueArray[i].toString();
		return result;
	}
	
	public static void main(String[] args) {
		String rootPath = "C:\\";

		
		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\JAnalyzer\\src\\", rootPath + "ZxcTools\\EclipseSource\\org\\", rootPath + "ZxcTemp\\src\\",
							rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\TestGenericType.java", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
							rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
							rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
							rootPath + "ZxcTools\\ArgoUml\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
							rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};
		
		String path = paths[1];
		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = new PrintWriter(System.out);
		
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
			Debug.setScreenOn();
		} catch (Exception exc) {
			exc.printStackTrace();
			output.close();
			return;
		}
		
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
			Debug.setStart("Begin collection....");
			ObjectExpressionCollector collector = new ObjectExpressionCollector();
			collector.collectAllObjectReferenceExpressions(path, writer);
			Debug.time("After collection...");
			writer.close();
			output.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
}






