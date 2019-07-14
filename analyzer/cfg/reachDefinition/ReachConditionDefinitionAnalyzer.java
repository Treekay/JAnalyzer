package analyzer.cfg.reachDefinition;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

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

import analyzer.cfg.ExecutionPointWorkingList;
import analyzer.cfg.predicate.NodePredicateChainAnalyzer;
import analyzer.cfg.predicate.NodePredicateListChain;
import analyzer.cfg.predicate.NodePredicateRecorder;
import analyzer.storageModel.IAbstractStorageModel;
import analyzer.storageModel.SimpleStorageModel;
import analyzer.storageModel.StorageModelFactory;
import graph.basic.GraphNode;
import graph.cfg.CFGEdge;
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
//import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ14ÈÕ
 * @version 1.0
 *
 */
public class ReachConditionDefinitionAnalyzer {
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
		
		setReachConditionDefinitionRecorder(currentCFG);
		reachingConditionDefinitionAnalysis(nameTable, unitRecorder, method, currentCFG);
		return currentCFG;
	}
	
	public static void setReachConditionDefinitionRecorder(ControlFlowGraph currentCFG) {
		List<GraphNode> nodeList = currentCFG.getAllNodes();
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				ReachConditionDefinitionRecorder recorder = new ReachConditionDefinitionRecorder();
				node.setFlowInfoRecorder(recorder);
			}
		}
	}
	
	public static void reachingConditionDefinitionAnalysis(NameTableManager manager, CompilationUnitRecorder unitRecorder, 
			MethodDefinition method, ControlFlowGraph currentCFG) {
		NodePredicateChainAnalyzer.nodePredicateChainAnalysis(manager, unitRecorder, method, currentCFG);
		initializeGeneratedConditionDefinitionsForAllNodes(manager, unitRecorder, method, currentCFG);
		
		ExecutionPointWorkingList workingList = new ExecutionPointWorkingList();
		List<GraphNode> allNodeList = currentCFG.getAllNodes();
		for (GraphNode node : allNodeList) {
			if (!(node instanceof ExecutionPoint)) continue;
			workingList.add((ExecutionPoint)node);
		}
//		Debug.println("\t" + workingList);

		while (!workingList.isEmpty()) {
			ExecutionPoint currentNode = workingList.removeFirst();
			SourceCodeLocation currentLocation = currentNode.getStartLocation();

//			Debug.setScreenOn();
//			Debug.println("Begin process current node (" + currentNode.getId() + ")");
//			Debug.setScreenOff();
		
			IReachConditionDefinitionRecorder currentRecorder = (IReachConditionDefinitionRecorder)currentNode.getFlowInfoRecorder();
			List<ConditionDefinitionRecorder> currentInList = currentRecorder.getInReachingConditionDefinitionList();
			List<GraphNode> adjacentToNodeList = currentCFG.adjacentToNode(currentNode);
			
			// If changeStatus == true, then the inList of the current node has been changed. It means that we change
			// some condition chains of condition definition recorders in the inList of the current node, or we add 
			// new condition definition recorders to the inList of the current node.
			boolean changeStatus = false;
			for (GraphNode adjacentToNode : adjacentToNodeList) {
				if (!(adjacentToNode instanceof ExecutionPoint)) continue;

				ExecutionPoint precedeNode = (ExecutionPoint)adjacentToNode;
				// When we do disjunction on the conditionChain in the current node and the precede node, we ignore those
				// precede nodes which are coming back from the loop body!
				boolean isLoopBackEdge = false;
				if (currentNode.isPredicate()) {
					SourceCodeLocation precedeLocation = precedeNode.getStartLocation();
					if (currentLocation.compareTo(precedeLocation) <= 0) isLoopBackEdge = true;
				}
				IReachConditionDefinitionRecorder precedeRecorder = (IReachConditionDefinitionRecorder)precedeNode.getFlowInfoRecorder();
				List<ConditionDefinitionRecorder> precedeDefinitionList = precedeRecorder.getReachingConditionDefinitionList();
				
//				Debug.println("\tPrecede node " + precedeNode.getId() +", precedeDefinitionList size " + precedeDefinitionList.size());
				for (ConditionDefinitionRecorder precedeDefinition : precedeDefinitionList) {
					DefinitionRecorder precedeFirstDefinition = precedeDefinition.definitionList.getFirst();

//					Debug.println("\t\tPrecede condition definition recorder: " + precedeDefinition);
					// Check whether the name scope of the precede definition contains the location of the node. 
					// If not, it means the precede definition can not be accessed at the location of the node, 
					// and then, we ignore it!
					if (!precedeFirstDefinition.getLeftStorage().accessible(currentLocation)) continue;

					NodePredicateListChain precedeChain = precedeDefinition.conditionChain;
					NodePredicateRecorder precedePredicate = precedeRecorder.getTruePredicate(); 
					if (precedePredicate != null) {
						// This means that precede node is a predicate node, we must add this predicate to 
						// the recorder of the current node!
						String edgeLabel = currentCFG.getEdgeLabel(precedeNode, currentNode);
						if (edgeLabel != null) {
							if (edgeLabel.equals(CFGEdge.LABEL_TRUE)) {
								precedeChain = precedeChain.conjunctionWith(precedePredicate);
							} else if (edgeLabel.equals(CFGEdge.LABEL_FALSE)) {
								precedePredicate = precedeRecorder.getFalsePredicate();
								precedeChain = precedeChain.conjunctionWith(precedePredicate);
							}
						}
					}
					
					// Try to combine condition definition recorders of the precede node to the current node!
					boolean found = false;
					for (ConditionDefinitionRecorder currentInRecorder : currentInList) {
						if (precedeFirstDefinition == currentInRecorder.definitionList.getFirst()) {
							found = true;
							// This condition definition recorder has yet been in the inList of the current node, so 
							// we do disjunction operation on their condition chains when the precede node is not coming
							// back from the loop body to its loop predicate node!  
							if (!isLoopBackEdge) {
								NodePredicateListChain currentChain = currentInRecorder.conditionChain;
//								Debug.println("\t\tDo disjunction for current chain " + currentChain + ", and precedeChain " + precedeChain);
								TreeSet<GraphNode> rangeNodeSet = NodePredicateListChain.getCommonCFGNodeList(currentChain, precedeChain);
								NodePredicateListChain newChain = currentChain.disjunctionWith(precedeChain, rangeNodeSet);
								if (!newChain.equivalentTo(currentChain)) {
									changeStatus = true;
									currentInRecorder.conditionChain = newChain;
									
//									Debug.println("\t\tChange current definition " + currentInRecorder.definitionList.getFirst() + " condition chain, old: " + currentChain + ", new: " + newChain);
								}
//								if (!newChain.isSimplified()) {
//									Debug.println("\t\t\tResult chain: " + newChain);
//									throw new AssertionError("Disjunction precede chain: " + precedeChain + ", and \ncurrent chain: " + currentChain + ", \nget a non-simplified chain: " + newChain);
//								}
							}
							break;
						}
					}
					if (!found) {
						// We do not find precedeDefinition in the currentInList, then we use its the definitionList
						// and the changed condition chain of the precede node to construct a condition definition recorder.
						// And the constructed new recorder is added to the inList of the current node. 
						NodePredicateListChain newChain = precedeChain.getACopy();
						ConditionDefinitionRecorder newInRecorder = new ConditionDefinitionRecorder(precedeDefinition.definitionList, newChain);
						currentInList.add(newInRecorder);
						changeStatus = true;

//						Debug.println("\t\tAdd new in recorder : " + newInRecorder);
					}
				}
			}
			
			List<GeneratedConditionDefinitionRecorder> generatedList = currentRecorder.getGeneratedConditionDefinitionList();
			for (GeneratedConditionDefinitionRecorder generatedRecorder : generatedList) {
				boolean found = false;
				for (ConditionDefinitionRecorder currentInRecorder : currentInList) {
					if (currentInRecorder.newDefinitionFlag == false) continue;
					DefinitionRecorder currentInFirstDefinition = currentInRecorder.definitionList.getFirst();
					if (currentInFirstDefinition.leftStorage.referToSameStorage(generatedRecorder.rightModel)) {
						found = true;
						LinkedList<DefinitionRecorder> definitionList = new LinkedList<DefinitionRecorder>();
						definitionList.addFirst(generatedRecorder.definition);
						for (DefinitionRecorder definitionRecorder : currentInRecorder.definitionList) {
							if (!definitionList.contains(definitionRecorder)) definitionList.add(definitionRecorder);
						}
						NodePredicateListChain newPredicateChain = generatedRecorder.conditionChain.conjunctionWith(currentInRecorder.conditionChain);
						ConditionDefinitionRecorder newOutRecorder = new ConditionDefinitionRecorder(definitionList, newPredicateChain);
						boolean added = currentRecorder.addReachingConditionDefinition(newOutRecorder);
						if (added) {
//							Debug.println("\t\tAdd out recorder of generated definition: " + newOutRecorder);
							changeStatus = true;
						}
					}
				}
				if (found == false && generatedRecorder.newDefinitionFlag == true) {
					ConditionDefinitionRecorder newOutRecorder = new ConditionDefinitionRecorder(generatedRecorder.definition, generatedRecorder.conditionChain);
					boolean added = currentRecorder.addReachingConditionDefinition(newOutRecorder);
					if (added) {
//						Debug.println("\t\tAdd out recorder of generated definition: " + newOutRecorder);
						changeStatus = true;
					}
					generatedRecorder.newDefinitionFlag = false;
				}
			}
			
			for (ConditionDefinitionRecorder currentInRecorder : currentInList) {
				// If currentInRecorder.newFlag == true, then the currentInRecorder is a new recorder, i.e. it is 
				// constructed in the above loop for processing the precede nodes. 
				// Note that, newFlag of a currentInRecorder set to be true when it is constructed.
				// We only check if the new recorder in the inList of the current node is killed by the generated 
				// definition in the current node.
				if (currentInRecorder.newDefinitionFlag == false) continue;
				// If we have checked the recorder in the inList, we do not need to check it again!
//				currentInRecorder.newFlag = false;
				DefinitionRecorder currentInFirstDefinition = currentInRecorder.definitionList.getFirst();
				boolean killed = false;
				for (GeneratedConditionDefinitionRecorder generatedRecorder : generatedList) {
					DefinitionRecorder generatedFirstDefinition = generatedRecorder.definition;
					IAbstractStorageModel genLeftStorage = generatedFirstDefinition.getLeftStorage();
					IAbstractStorageModel currentInLeftStorage = currentInFirstDefinition.getLeftStorage();
					killed = currentInLeftStorage.referToSameStorage(genLeftStorage);
					
					if (killed) {
//						Debug.println("\t\t\tIn recorder " + currentInRecorder + " is killed by " + generatedRecorder);
						break;
					}
				}
				// If the recorder in the inList of the current recorder, we can not add it to the outList of 
				// the current node.
				if (killed) continue;
				boolean added = currentRecorder.addReachingConditionDefinition(currentInRecorder);
				if (added) {
//					Debug.println("\t\tAdd out recorder: " + currentInRecorder);
					changeStatus = true;
				}
			}
			
			// If we do not change the inList of the current node, we can not add the succeed nodes to the 
			// working list for the next iteration.  
			if (changeStatus == false) continue;
			List<GraphNode> adjacentFromNodeList = currentCFG.adjacentFromNode(currentNode);
			for (GraphNode adjacentFromNode : adjacentFromNodeList) {
				if (!(adjacentFromNode instanceof ExecutionPoint)) continue;
				workingList.add((ExecutionPoint)adjacentFromNode);
//				Debug.println("\tAdd succeed node (" + adjacentFromNode.getId() + ")");
			}
//			Debug.disable();
		}
	}
	
	static void initializeGeneratedConditionDefinitionsForAllNodes(NameTableManager manager, CompilationUnitRecorder unitRecorder, MethodDefinition method, ControlFlowGraph currentCFG) {
		ExecutionPoint startNode = (ExecutionPoint)currentCFG.getStartNode(); 
		IReachConditionDefinitionRecorder recorder = (IReachConditionDefinitionRecorder)startNode.getFlowInfoRecorder();
//		Debug.println("Initializing....!");
		// Add parameter definition to the defined name list of the start node. Note that its reference for definition is NULL!
		List<VariableDefinition> parameterList = method.getParameterList();
		if (parameterList != null) {
			for (VariableDefinition parameter : parameterList) {
				DefinitionRecorder definitionRecorder = new DefinitionRecorder(startNode, new SimpleStorageModel(parameter), null);
				NodePredicateListChain conditionChain = recorder.getPredicateChain();
				ConditionDefinitionRecorder conditionRecorder = new ConditionDefinitionRecorder(definitionRecorder, conditionChain); 
				recorder.addReachingConditionDefinition(conditionRecorder);
//				Debug.println("\tAdd definition for parameter: " + conditionRecorder);
			}
		}
		
		NameReferenceCreator referenceCreator = new NameReferenceCreator(manager, true);
		ExpressionReferenceASTVisitor referenceVisitor = new ExpressionReferenceASTVisitor(referenceCreator, unitRecorder, null, true);
		// Initialize defined name in node if its ASTNode is assignment, variable declaration, prefix or postfix expression (++, --) 
		List<GraphNode> nodeList = currentCFG.getAllNodes();
		for (GraphNode graphNode : nodeList) {
			ExecutionPoint node = (ExecutionPoint)graphNode;
			if (node.isStart()) continue;
			
			recorder = (IReachConditionDefinitionRecorder)node.getFlowInfoRecorder();
			
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
					DefinitionRecorder definitionRecorder = new DefinitionRecorder(node, leftStorage, rightReference);
					NodePredicateListChain conditionChain = recorder.getPredicateChain();
					IAbstractStorageModel rightStorage = null;
					if (rightReference.isResolved()) {
						rightStorage = StorageModelFactory.extractLeftStorageModelInReference(rightReference); 
					}
					GeneratedConditionDefinitionRecorder conditionRecorder = new GeneratedConditionDefinitionRecorder(definitionRecorder, rightStorage, conditionChain);
					// This execution point generated this defined name, and also kills previous definition of this binded 
					// local variable, parameter or field in the precede nodes.
					recorder.addGeneratedConditionDefinition(conditionRecorder);

//					Debug.println("\tAdd definition from assignment statement: " + conditionRecorder);
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

				for (NameDefinition variable : variableList) {
					if (variable.getSimpleName().equals(parameter.getName().getIdentifier())) {
						// Find the definition of this variable declaration fragment, so this execution point generated a defined name 
						// for this variable, and kill all previous definition of this variable in the precede nodes

						// Create reference for its initializer expression, and add the definition of this variable to definedNameList of 
						// this execution point
						NameScope currentScope = manager.getScopeOfLocation(startLocation);
						referenceVisitor.reset(currentScope);
						expression.accept(referenceVisitor);
						NameReference valueReference = referenceVisitor.getResult();

						valueReference.resolveBinding();
						DefinitionRecorder definitionRecorder = new DefinitionRecorder(node, new SimpleStorageModel(variable), valueReference);
						NodePredicateListChain conditionChain = recorder.getPredicateChain();
						IAbstractStorageModel rightStorage = null;
						if (valueReference.isResolved()) {
							rightStorage = StorageModelFactory.extractLeftStorageModelInReference(valueReference); 
						}
						GeneratedConditionDefinitionRecorder conditionRecorder = new GeneratedConditionDefinitionRecorder(definitionRecorder, rightStorage, conditionChain);
						// Generate this defined name and kill all previous definition in the precede node! 
						recorder.addGeneratedConditionDefinition(conditionRecorder);

//						Debug.println("\tAdd definition from enhanced for statement: " + conditionRecorder);
						break;
					}
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
					
					for (NameDefinition variable : variableList) {
						if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
							// Find the definition of this variable declaration fragment, so this execution point generated a defined name 
							// for this variable, and kill all previous definition of this variable in the precede nodes

							// Create reference for its initializer expression, and add the definition of this variable to definedNameList of 
							// this execution point
							NameScope currentScope = manager.getScopeOfLocation(startLocation);
							referenceVisitor.reset(currentScope);
							initializer.accept(referenceVisitor);
							NameReference valueReference = referenceVisitor.getResult();

							valueReference.resolveBinding();
							DefinitionRecorder definitionRecorder = new DefinitionRecorder(node, new SimpleStorageModel(variable), valueReference);
							NodePredicateListChain conditionChain = recorder.getPredicateChain();
							IAbstractStorageModel rightStorage = null;
							if (valueReference.isResolved()) {
								rightStorage = StorageModelFactory.extractLeftStorageModelInReference(valueReference); 
							}
							GeneratedConditionDefinitionRecorder conditionRecorder = new GeneratedConditionDefinitionRecorder(definitionRecorder, rightStorage, conditionChain);
							// Generate this defined name and kill all previous definition in the precede node! 
							recorder.addGeneratedConditionDefinition(conditionRecorder);
							
//							Debug.println("\tAdd definition from variable declaration expression: " + conditionRecorder);
							break;
						}
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
				
				for (NameDefinition variable : variableList) {
					if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
						// Find the definition of this variable declaration fragment, so this execution point generated a defined name 
						// for this variable, and kill all previous definition of this variable in the precede nodes

						// Create reference for its initializer expression, and add the definition of this variable to definedNameList of 
						// this execution point
						NameScope currentScope = manager.getScopeOfLocation(startLocation);
						referenceVisitor.reset(currentScope);
						initializer.accept(referenceVisitor);
						NameReference valueReference = referenceVisitor.getResult();
						
						valueReference.resolveBinding();
						DefinitionRecorder definitionRecorder = new DefinitionRecorder(node, new SimpleStorageModel(variable), valueReference);
						NodePredicateListChain conditionChain = recorder.getPredicateChain();
						IAbstractStorageModel rightStorage = null;
						if (valueReference.isResolved()) {
							rightStorage = StorageModelFactory.extractLeftStorageModelInReference(valueReference); 
						}
						GeneratedConditionDefinitionRecorder conditionRecorder = new GeneratedConditionDefinitionRecorder(definitionRecorder, rightStorage, conditionChain);
						// Generate this defined name and kill all previous definition in the precede node! 
						recorder.addGeneratedConditionDefinition(conditionRecorder);
						
//						Debug.println("\tAdd definition from variable declaration fragement: " + conditionRecorder);
						break;
					}
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
					
					for (NameDefinition variable : variableList) {
						if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
							// Find the definition of this variable declaration fragment, so this execution point generated a defined name 
							// for this variable, and kill all previous definition of this variable in the precede nodes

							// Create reference for its initializer expression, and add the definition of this variable to definedNameList of 
							// this execution point
							NameScope currentScope = manager.getScopeOfLocation(startLocation);
							referenceVisitor.reset(currentScope);
							initializer.accept(referenceVisitor);
							NameReference valueReference = referenceVisitor.getResult();

							valueReference.resolveBinding();
							DefinitionRecorder definitionRecorder = new DefinitionRecorder(node, new SimpleStorageModel(variable), valueReference);
							NodePredicateListChain conditionChain = recorder.getPredicateChain();
							IAbstractStorageModel rightStorage = null;
							if (valueReference.isResolved()) {
								rightStorage = StorageModelFactory.extractLeftStorageModelInReference(valueReference); 
							}
							GeneratedConditionDefinitionRecorder conditionRecorder = new GeneratedConditionDefinitionRecorder(definitionRecorder, rightStorage, conditionChain);
							// Generate this defined name and kill all previous definition in the precede node! 
							recorder.addGeneratedConditionDefinition(conditionRecorder);
							
//							Debug.println("\tAdd definition from variable declaration statement: " + conditionRecorder);
							break;
						}
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

						rightReference.resolveBinding();
						DefinitionRecorder definitionRecorder = new DefinitionRecorder(node, leftStorage, rightReference);
						NodePredicateListChain conditionChain = recorder.getPredicateChain();
						IAbstractStorageModel rightStorage = null;
						if (rightReference.isResolved()) {
							rightStorage = StorageModelFactory.extractLeftStorageModelInReference(rightReference); 
						}
						GeneratedConditionDefinitionRecorder conditionRecorder = new GeneratedConditionDefinitionRecorder(definitionRecorder, rightStorage, conditionChain);
						// Generate this defined name and kill all previous definition in the precede node! 
						recorder.addGeneratedConditionDefinition(conditionRecorder);

//						Debug.println("\tAdd definition from infix expression: " + conditionRecorder);
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

						rightReference.resolveBinding();
						DefinitionRecorder definitionRecorder = new DefinitionRecorder(node, leftStorage, rightReference);
						NodePredicateListChain conditionChain = recorder.getPredicateChain();
						IAbstractStorageModel rightStorage = null;
						if (rightReference.isResolved()) {
							rightStorage = StorageModelFactory.extractLeftStorageModelInReference(rightReference); 
						}
						GeneratedConditionDefinitionRecorder conditionRecorder = new GeneratedConditionDefinitionRecorder(definitionRecorder, rightStorage, conditionChain);
						// Generate this defined name and kill all previous definition in the precede node! 
						recorder.addGeneratedConditionDefinition(conditionRecorder);

//						Debug.println("\tAdd definition from postfix expression: " + conditionRecorder);
					}
				}
			}
		}
	}
	
	
	/**
	 *  Given all reaching condition definitions for a given reference after the given node (i.e. the definitions in the out list). 
	 *  We assume that the node includes the information of reaching condition definition, i.e. the flow info recorder in the node
	 *  is a IReachConditionDefinitionRecorder.
	 */
	public static List<ConditionDefinitionRecorder> getReachConditionDefinitionList(ExecutionPoint node, NameReference reference) {
		List<ConditionDefinitionRecorder> resultList = new ArrayList<ConditionDefinitionRecorder>();
		
		IAbstractStorageModel storageModel = StorageModelFactory.extractLeftStorageModelInReference(reference);
		if (storageModel == null) return resultList;
		
		IReachConditionDefinitionRecorder recorder = (IReachConditionDefinitionRecorder)node.getFlowInfoRecorder();
		List<ConditionDefinitionRecorder> outList =  recorder.getReachingConditionDefinitionList();
		
		for (ConditionDefinitionRecorder conditionDefinition : outList) {
			DefinitionRecorder definition = conditionDefinition.getMainDefinition();
			IAbstractStorageModel definitionStorageModel = definition.getLeftStorage();
			if (storageModel.referToSameStorage(definitionStorageModel)) resultList.add(conditionDefinition);
		}
		return resultList;
	}

	/**
	 *  Given all reaching condition definitions for a given reference after the given node (i.e. the definitions in the out list). 
	 *  We assume that the node includes the information of reaching condition definition, i.e. the flow info recorder in the node
	 *  is a IReachConditionDefinitionRecorder.
	 */
	public static List<ConditionDefinitionRecorder> getReachConditionDefinitionList(ExecutionPoint node, IAbstractStorageModel storageModel) {
		List<ConditionDefinitionRecorder> resultList = new ArrayList<ConditionDefinitionRecorder>();
		
		IReachConditionDefinitionRecorder recorder = (IReachConditionDefinitionRecorder)node.getFlowInfoRecorder();
		List<ConditionDefinitionRecorder> outList =  recorder.getReachingConditionDefinitionList();
		
		for (ConditionDefinitionRecorder conditionDefinition : outList) {
			DefinitionRecorder definition = conditionDefinition.getMainDefinition();
			IAbstractStorageModel definitionStorageModel = definition.getLeftStorage();
			if (storageModel.referToSameStorage(definitionStorageModel)) resultList.add(conditionDefinition);
		}
		return resultList;
	}
	
	public static NodePredicateListChain getReachCondition(ExecutionPoint node) {
		IReachConditionDefinitionRecorder recorder = (IReachConditionDefinitionRecorder)node.getFlowInfoRecorder();
		return recorder.getPredicateChain();
	}
	
	public static void printAllReachingConditionDefinitions(ControlFlowGraph cfg, PrintWriter output) {
		output.println("ExecutionPointId\tConditionDefinition\tNullability");
		
		List<GraphNode> nodeList = cfg.getAllNodes();
		if (nodeList == null) return;
//		System.out.println("Before write execution point " + nodeList.size() + " nodes!");
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				ReachConditionDefinitionRecorder recorder = (ReachConditionDefinitionRecorder)node.getFlowInfoRecorder();
				List<ConditionDefinitionRecorder> conditionDefinitionList = recorder.getReachingConditionDefinitionList();
				for (ConditionDefinitionRecorder conditionDefinitionRecorder : conditionDefinitionList) {
//					Debug.println("Node: " + graphNode.getId() + ", write current recorder: " + conditionDefinitionRecorder);
					if (conditionDefinitionRecorder.isPrimitiveDefinition()) {
						output.println("(" + node.getId() + ")\t" + conditionDefinitionRecorder + "\t~~");
					} else {
						output.println("(" + node.getId() + ")\t" + conditionDefinitionRecorder + "\t" + conditionDefinitionRecorder.getNullability());
					}
				}
			}
		}
		output.println();
		
		try {
			cfg.simplyWriteToDotFile(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
