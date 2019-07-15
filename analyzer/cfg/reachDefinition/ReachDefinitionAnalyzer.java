package analyzer.cfg.reachDefinition;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import analyzer.storageModel.ArrayStorageModel;
import analyzer.storageModel.FieldStorageModel;
import analyzer.storageModel.IAbstractStorageModel;
import analyzer.storageModel.SimpleStorageModel;
import analyzer.storageModel.StorageModelFactory;
import util.Debug;

import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.ExecutionPointLabel;
import graph.cfg.creator.CFGCreator;
import nameTable.NameTableManager;
import nameTable.creator.ExpressionReferenceASTVisitor;
import nameTable.creator.NameReferenceCreator;
import nameTable.filter.NameDefinitionLocationFilter;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeLocation;

/**
 * @author Zhou Xiaocong
 * @since 2018年4月10日
 * @version 1.0
 *
 */
public class ReachDefinitionAnalyzer {

	public static ControlFlowGraph create(NameTableManager nameTable, MethodDefinition method) {
		CompilationUnitScope unitScope = nameTable.getEnclosingCompilationUnitScope(method);
		if (unitScope == null) return null;
		String sourceFileName = unitScope.getUnitName();
		CompilationUnit astRoot = nameTable.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(sourceFileName);
		if (astRoot == null) return null;
		CompilationUnitRecorder unitRecorder = new CompilationUnitRecorder(sourceFileName, astRoot);
		
		// Create a ControFlowGraph object
		ControlFlowGraph currentCFG = CFGCreator.create(nameTable, unitRecorder, method);
		if (currentCFG == null) return null;
		
		setReachDefinitionRecorder(currentCFG);
		reachingDefinitionAnalysis(nameTable, unitRecorder, method, currentCFG);
		return currentCFG;
	}
	
	public static void setReachDefinitionRecorder(ControlFlowGraph currentCFG) {
		List<GraphNode> nodeList = currentCFG.getAllNodes();
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				ReachDefinitionRecorder recorder = new ReachDefinitionRecorder();
				node.setFlowInfoRecorder(recorder);
			}
		}
	}
	
	public static void reachingDefinitionAnalysis(NameTableManager manager, CompilationUnitRecorder unitRecorder, 
			MethodDefinition method, ControlFlowGraph currentCFG) {
		initializeGeneratedDefinitionsForAllNodes(manager, unitRecorder, method, currentCFG);
		
		List<GraphNode> graphNodeList = currentCFG.getAllNodes();
		boolean hasChanged = true;
		while (hasChanged) {
			hasChanged = false;
			for (GraphNode graphNode : graphNodeList) {
				ExecutionPoint currentNode = (ExecutionPoint)graphNode;
				IReachDefinitionRecorder currentRecorder = (IReachDefinitionRecorder)currentNode.getFlowInfoRecorder();
				List<DefinitionRecorder> generatedList = currentRecorder.getGeneratedDefinitionList();
				List<GraphNode> adjacentToNodeList = currentCFG.adjacentToNode(graphNode);
				for (GraphNode adjacentToNode : adjacentToNodeList) {
					if (adjacentToNode instanceof ExecutionPoint) {
						ExecutionPoint precedeNode = (ExecutionPoint)adjacentToNode;
						IReachDefinitionRecorder precedeRecorder = (IReachDefinitionRecorder)precedeNode.getFlowInfoRecorder();
						List<DefinitionRecorder> precedeDefinitionList = precedeRecorder.getReachingDefinitionList();
						// Note that all generated definitions by this node also kill the definitions reached the precede nodes.
						for (DefinitionRecorder precedeDefinition : precedeDefinitionList) {
							// Note that the fact that addInReachingDefinition(precedeDefinition) returns false implies that
							// precedeDefinition has been in the in reaching definition list of the current node.
							if (!currentRecorder.addInReachingDefinition(precedeDefinition)) continue;
							boolean killed = false;
							for (DefinitionRecorder generatedDefinition : generatedList) {
								IAbstractStorageModel genLeftStorage = generatedDefinition.getLeftStorage();
								IAbstractStorageModel precedeLeftStorage = precedeDefinition.getLeftStorage();
								killed = precedeLeftStorage.referToSameStorage(genLeftStorage);
								
								if ((genLeftStorage instanceof FieldStorageModel) && (precedeLeftStorage instanceof FieldStorageModel)) {
									FieldStorageModel genField = (FieldStorageModel)genLeftStorage;
									FieldStorageModel precedeField = (FieldStorageModel)precedeLeftStorage;
									
									if (killed) {
										Debug.println("Field Assignment [" + genField.getExpression() + "] at " + generatedDefinition.getNode().getId() + " kills [" + precedeField.getExpression() + "] at " + precedeDefinition.getNode().getId() + " in " + generatedDefinition.getValueExpression().getLocation().getFileUnitName());
									} else {
										if (genField.getFieldDefinition() == precedeField.getFieldDefinition()) {
											Debug.println("Field Assignment [" + genField.getExpression() + "] at " + generatedDefinition.getNode().getId() + " DOES NOT kill [" + precedeField.getExpression() + "] at " + precedeDefinition.getNode().getId() + " in " + generatedDefinition.getValueExpression().getLocation().getFileUnitName());
										}
									}
								} else if ((genLeftStorage instanceof ArrayStorageModel) && (precedeLeftStorage instanceof ArrayStorageModel)) {
									ArrayStorageModel genArray = (ArrayStorageModel)genLeftStorage;
									ArrayStorageModel precedeArray = (ArrayStorageModel)precedeLeftStorage;
									if (killed) {
										if (!genArray.getExpression().equals(precedeArray.getExpression())) {
											Debug.println("Array Assignment [" + genArray.getExpression() + "] at " + generatedDefinition.getNode().getId() + " kills [" + precedeArray.getExpression() + "] at " + precedeDefinition.getNode().getId() + " in " + generatedDefinition.getValueExpression().getLocation().getFileUnitName());
										}
									}
								} else {
									if (killed) {
//										Debug.println("Assignment [" + genLeftStorage.getExpression() + "] at " + generatedDefinition.getNode().getId() + " kills [" + precedeLeftStorage.getExpression() + "] at " + precedeDefinition.getNode().getId() + " in " + generatedDefinition.getValueExpression().getLocation().getFileUnitName());
									}
								}
								if (killed) break;
							}
							if (!killed) {
								SourceCodeLocation currentLocation = currentNode.getStartLocation(); 
								// Check whether the name scope of the precede definition contains the location of the node. 
								// If not, it means the precede definition can not be accessed at the location of the node.
								if (precedeDefinition.getLeftStorage().accessible(currentLocation)) {
									// There is at least a node changing its reaching definition.
									if (currentRecorder.addReachingDefinition(precedeDefinition)) hasChanged = true;  
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	static void initializeGeneratedDefinitionsForAllNodes(NameTableManager manager, CompilationUnitRecorder unitRecorder, MethodDefinition method, ControlFlowGraph currentCFG) {
		ExecutionPoint startNode = (ExecutionPoint)currentCFG.getStartNode(); 
		IReachDefinitionRecorder recorder = (IReachDefinitionRecorder)startNode.getFlowInfoRecorder();
		// Add parameter definition to the defined name list of the start node. Note that its reference for definition is NULL!
		List<VariableDefinition> parameterList = method.getParameterList();
		if (parameterList != null) {
			for (VariableDefinition parameter : parameterList) {
				recorder.addReachingDefinition(new DefinitionRecorder(startNode, new SimpleStorageModel(parameter), null));
			}
		}
		
		NameReferenceCreator referenceCreator = new NameReferenceCreator(manager, true);
		ExpressionReferenceASTVisitor referenceVisitor = new ExpressionReferenceASTVisitor(referenceCreator, unitRecorder, null, true);
		// Initialize defined name in node if its ASTNode is assignment, variable declaration, prefix or postfix expression (++, --) 
		List<GraphNode> nodeList = currentCFG.getAllNodes();
		for (GraphNode graphNode : nodeList) {
			ExecutionPoint node = (ExecutionPoint)graphNode;
			if (node.isStart()) continue;
			
			recorder = (IReachDefinitionRecorder)node.getFlowInfoRecorder();
			
			ASTNode astNode = node.getAstNode();
			if (astNode == null) continue;
			
			SourceCodeLocation startLocation = node.getStartLocation();
			SourceCodeLocation endLocation = node.getEndLocation();
			int nodeType = astNode.getNodeType();
			if (nodeType == ASTNode.ASSIGNMENT) {
				Assignment assignment = (Assignment)astNode;
				Expression leftHandSide = assignment.getLeftHandSide();
				
				NameScope currentScope = manager.getScopeOfLocation(startLocation);
				referenceVisitor.reset(currentScope);
				leftHandSide.accept(referenceVisitor);
				NameReference leftReference = referenceVisitor.getResult();
				if (leftReference.resolveBinding()) {
					IAbstractStorageModel leftStorage = StorageModelFactory.extractLeftStorageModelInReference(leftReference);
					
					Expression rightHandSide = assignment.getRightHandSide();
					referenceVisitor.reset();
					rightHandSide.accept(referenceVisitor);
					NameReference rightReference = referenceVisitor.getResult();
					
					rightReference.resolveBinding();
					DefinitionRecorder definedName = new DefinitionRecorder(node, leftStorage, rightReference); 
					recorder.addReachingDefinition(definedName);
					// This execution point generated this defined name, and also kills previous definition of this binded 
					// local variable, parameter or field in the precede nodes.
					recorder.addGeneratedDefinition(definedName);
				}
			} else if (nodeType == ASTNode.ENHANCED_FOR_STATEMENT && node.getLabel().equals(ExecutionPointLabel.ENHANCED_FOR_PREDICATE)) {
				EnhancedForStatement enhancedForStatement = (EnhancedForStatement)astNode;
				SingleVariableDeclaration parameter = enhancedForStatement.getParameter();
				Expression expression = enhancedForStatement.getExpression();
				
				NameDefinitionVisitor definitionVisitor = new NameDefinitionVisitor();
				NameDefinitionLocationFilter filter = new NameDefinitionLocationFilter(startLocation, endLocation);
				definitionVisitor.setFilter(filter);
				method.accept(definitionVisitor);
				List<NameDefinition> variableList = definitionVisitor.getResult();

				boolean found = false;
				for (NameDefinition variable : variableList) {
					if (variable.getSimpleName().equals(parameter.getName().getIdentifier())) {
						found = true;
						// Find the definition of this variable declaration fragment, so this execution point generated a defined name 
						// for this variable, and kill all previous definition of this variable in the precede nodes

						// Create reference for its initializer expression, and add the definition of this variable to definedNameList of 
						// this execution point
						NameScope currentScope = manager.getScopeOfLocation(startLocation);
						referenceVisitor.reset(currentScope);
						expression.accept(referenceVisitor);
						NameReference valueReference = referenceVisitor.getResult();
/*
						SourceCodeLocation location = variable.getLocation();
						String file = location.getFileUnitName();
						int lineNumber = location.getLineNumber();
						int column = location.getColumn();
						String name = variable.getSimpleName();
						Debug.println(file + "\t(" + lineNumber + ":" + column + ")\tEnhancedFor\t" + "VariableDeclaration" + "\t" + name + "\t" + variable.getUniqueId() + "\t" + variable.getDefinitionKind());
*/						
						valueReference.resolveBinding();
						DefinitionRecorder definedName = new DefinitionRecorder(node, new SimpleStorageModel(variable), valueReference); 
						recorder.addReachingDefinition(definedName);
						// Generate this defined name and kill all previous definition in the precede node! 
						recorder.addGeneratedDefinition(definedName);
						break;
					}
				}
				if (!found) {
					throw new AssertionError("Can not find variable definition for enhanced for parameter: " + parameter.toString() + " at " + startLocation.getUniqueId());
				}
			} else if (nodeType == ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
				VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)astNode;
				
				NameDefinitionVisitor visitor = new NameDefinitionVisitor();
				NameDefinitionLocationFilter filter = new NameDefinitionLocationFilter(startLocation, endLocation);
				visitor.setFilter(filter);
				method.accept(visitor);
				List<NameDefinition> variableList = visitor.getResult();
				
				@SuppressWarnings("unchecked")
				List<VariableDeclarationFragment> fragmentList = variableDeclarationExpression.fragments();
				for (VariableDeclarationFragment fragment : fragmentList) {
					Expression initializer = fragment.getInitializer();
					// This variable has not been initialized, that is, it is not defined!
					if (initializer == null) continue;
					
					boolean found = false;
					for (NameDefinition variable : variableList) {
						if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
							found = true;
							// Find the definition of this variable declaration fragment, so this execution point generated a defined name 
							// for this variable, and kill all previous definition of this variable in the precede nodes

							// Create reference for its initializer expression, and add the definition of this variable to definedNameList of 
							// this execution point
							NameScope currentScope = manager.getScopeOfLocation(startLocation);
							referenceVisitor.reset(currentScope);
							initializer.accept(referenceVisitor);
							NameReference valueReference = referenceVisitor.getResult();
/*							
							SourceCodeLocation location = variable.getLocation();
							String file = location.getFileUnitName();
							int lineNumber = location.getLineNumber();
							int column = location.getColumn();
							String name = variable.getSimpleName();
							Debug.println(file + "\t(" + lineNumber + ":" + column + ")\tVariableDeclExpression\t" + "VariableDeclaration" + "\t" + name + "\t" + variable.getUniqueId() + "\t" + variable.getDefinitionKind());
*/
							valueReference.resolveBinding();
							DefinitionRecorder definedName = new DefinitionRecorder(node, new SimpleStorageModel(variable), valueReference); 
							recorder.addReachingDefinition(definedName);
							// Generate this defined name and kill all previous definition in the precede node! 
							recorder.addGeneratedDefinition(definedName);
							
							break;
						}
					}
					if (!found) {
						throw new AssertionError("Can not find variable definition for variable declaration: " + fragment.toString() + " at " + startLocation.getUniqueId());
					}
				}
			} else if (nodeType == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
				VariableDeclarationFragment fragment = (VariableDeclarationFragment)astNode;
				
				NameDefinitionVisitor visitor = new NameDefinitionVisitor();
				NameDefinitionLocationFilter filter = new NameDefinitionLocationFilter(startLocation, endLocation);
				visitor.setFilter(filter);
				method.accept(visitor);
				List<NameDefinition> variableList = visitor.getResult();
				
				Expression initializer = fragment.getInitializer();
				// This variable has not been initialized, that is, it is not defined!
				if (initializer == null) continue;
				
				boolean found = false;
				for (NameDefinition variable : variableList) {
					if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
						found = true;
						// Find the definition of this variable declaration fragment, so this execution point generated a defined name 
						// for this variable, and kill all previous definition of this variable in the precede nodes

						// Create reference for its initializer expression, and add the definition of this variable to definedNameList of 
						// this execution point
						NameScope currentScope = manager.getScopeOfLocation(startLocation);
						referenceVisitor.reset(currentScope);
						initializer.accept(referenceVisitor);
						NameReference valueReference = referenceVisitor.getResult();
						
/*						SourceCodeLocation location = variable.getLocation();
						String file = location.getFileUnitName();
						int lineNumber = location.getLineNumber();
						int column = location.getColumn();
						String name = variable.getSimpleName();
						Debug.println(file + "\t(" + lineNumber + ":" + column + ")\tVariableDeclFragement\t" + "VariableDeclaration" + "\t" + name + "\t" + variable.getUniqueId() + "\t" + variable.getDefinitionKind());
*/
						valueReference.resolveBinding();
						DefinitionRecorder definedName = new DefinitionRecorder(node, new SimpleStorageModel(variable), valueReference); 
						recorder.addReachingDefinition(definedName);
						// Generate this defined name and kill all previous definition in the precede node! 
						recorder.addGeneratedDefinition(definedName);
						
						break;
					}
				}
				if (!found) {
					throw new AssertionError("Can not find variable definition for variable declaration: " + fragment.toString() + " at " + startLocation.getUniqueId());
				}
			} else if (nodeType == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
				VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)astNode;
				
				NameDefinitionVisitor visitor = new NameDefinitionVisitor();
				NameDefinitionLocationFilter filter = new NameDefinitionLocationFilter(startLocation, endLocation);
				visitor.setFilter(filter);
				method.accept(visitor);
				List<NameDefinition> variableList = visitor.getResult();
				
				@SuppressWarnings("unchecked")
				List<VariableDeclarationFragment> fragmentList = variableDeclarationStatement.fragments();
				for (VariableDeclarationFragment fragment : fragmentList) {
					Expression initializer = fragment.getInitializer();
					// This variable has not been initialized, that is, it is not defined!
					if (initializer == null) continue;
					
					boolean found = false;
					for (NameDefinition variable : variableList) {
						if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
							found = true;
							// Find the definition of this variable declaration fragment, so this execution point generated a defined name 
							// for this variable, and kill all previous definition of this variable in the precede nodes

							// Create reference for its initializer expression, and add the definition of this variable to definedNameList of 
							// this execution point
							NameScope currentScope = manager.getScopeOfLocation(startLocation);
							referenceVisitor.reset(currentScope);
							initializer.accept(referenceVisitor);
							NameReference valueReference = referenceVisitor.getResult();
/*							
							SourceCodeLocation location = variable.getLocation();
							String file = location.getFileUnitName();
							int lineNumber = location.getLineNumber();
							int column = location.getColumn();
							String name = variable.getSimpleName();
							Debug.println(file + "\t(" + lineNumber + ":" + column + ")\tVariableDeclStatement\t" + "VariableDeclaration" + "\t" + name + "\t" + variable.getUniqueId() + "\t" + variable.getDefinitionKind());
*/
							valueReference.resolveBinding();
							DefinitionRecorder definedName = new DefinitionRecorder(node, new SimpleStorageModel(variable), valueReference); 
							recorder.addReachingDefinition(definedName);
							// Generate this defined name and kill all previous definition in the precede node! 
							recorder.addGeneratedDefinition(definedName);
							
							break;
						}
					}
					if (!found) {
						throw new AssertionError("Can not find variable definition for variable declaration: " + fragment.toString() + " at " + startLocation.getUniqueId());
					}
				}
			} else if (nodeType == ASTNode.PREFIX_EXPRESSION) {
				PrefixExpression prefix = (PrefixExpression)astNode;
				if (prefix.getOperator() == PrefixExpression.Operator.DECREMENT || prefix.getOperator() == PrefixExpression.Operator.INCREMENT) {
					Expression leftHandSide = prefix.getOperand();
					
					NameScope currentScope = manager.getScopeOfLocation(startLocation);
					referenceVisitor.reset(currentScope);
					leftHandSide.accept(referenceVisitor);
					NameReference leftReference = referenceVisitor.getResult();
					if (leftReference.resolveBinding()) {
						IAbstractStorageModel leftStorage = StorageModelFactory.extractLeftStorageModelInReference(leftReference);
						
						referenceVisitor.reset();
						prefix.accept(referenceVisitor);
						NameReference rightReference = referenceVisitor.getResult();
/*						
						SourceCodeLocation location = definition.getLocation();
						String file = location.getFileUnitName();
						int lineNumber = location.getLineNumber();
						int column = location.getColumn();
						String name = definition.getSimpleName();
						Debug.println(file + "\t(" + lineNumber + ":" + column + ")\tPrefixExpression\t" + "PrefixExpression" + "\t" + name + "\t" + definition.getUniqueId() + "\t" + definition.getDefinitionKind());
*/
						rightReference.resolveBinding();
						DefinitionRecorder definedName = new DefinitionRecorder(node, leftStorage, rightReference); 
						recorder.addReachingDefinition(definedName);
						// Generate this defined name and kill all previous definition in the precede node! 
						recorder.addGeneratedDefinition(definedName);
					}
				}
			} else if (nodeType == ASTNode.POSTFIX_EXPRESSION) {
				PostfixExpression postfix = (PostfixExpression)astNode;
				if (postfix.getOperator() == PostfixExpression.Operator.DECREMENT || postfix.getOperator() == PostfixExpression.Operator.INCREMENT) {
					Expression leftHandSide = postfix.getOperand();
					
					NameScope currentScope = manager.getScopeOfLocation(startLocation);
					referenceVisitor.reset(currentScope);
					leftHandSide.accept(referenceVisitor);
					NameReference leftReference = referenceVisitor.getResult();
					if (leftReference.resolveBinding()) {
						IAbstractStorageModel leftStorage = StorageModelFactory.extractLeftStorageModelInReference(leftReference);
						
						referenceVisitor.reset();
						postfix.accept(referenceVisitor);
						NameReference rightReference = referenceVisitor.getResult();
/*
						SourceCodeLocation location = definition.getLocation();
						String file = location.getFileUnitName();
						int lineNumber = location.getLineNumber();
						int column = location.getColumn();
						String name = definition.getSimpleName();
						Debug.println(file + "\t(" + lineNumber + ":" + column + ")\tPrefixExpression\t" + "PrefixExpression" + "\t" + name + "\t" + definition.getUniqueId() + "\t" + definition.getDefinitionKind());
*/
						rightReference.resolveBinding();
						DefinitionRecorder definedName = new DefinitionRecorder(node, leftStorage, rightReference); 
						recorder.addReachingDefinition(definedName);
						// Generate this defined name and kill all previous definition in the precede node! 
						recorder.addGeneratedDefinition(definedName);
					}
				}
			}
		}
	}
}
