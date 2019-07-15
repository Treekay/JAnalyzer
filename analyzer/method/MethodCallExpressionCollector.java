package analyzer.method;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import analyzer.storageModel.IAbstractStorageModel;
import analyzer.storageModel.SimpleStorageModel;
import analyzer.storageModel.StorageModelFactory;
import graph.basic.GraphNode;
import graph.cfg.CFGNode;
import graph.cfg.CFGNodeType;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.ExecutionPointLabel;
import graph.cfg.creator.CFGCreator;
import nameTable.NameTableManager;
import nameTable.creator.ExpressionReferenceASTVisitor;
import nameTable.creator.NameReferenceCreator;
import nameTable.filter.NameDefinitionLocationFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeFileSet;
import sourceCodeAST.SourceCodeLocation;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018年6月30日
 * @version 1.0
 *
 */
public class MethodCallExpressionCollector {

	public static void collectAllMethodCallStatementByScanningMethods(String path, PrintWriter writer) {
		NameTableManager manager = NameTableManager.createNameTableManager(path);
		SourceCodeFileSet sourceCodeFileSet = manager.getSouceCodeFileSet();

		int counter = 0;
		int fileCounter = 0;
		StringBuilder message = new StringBuilder("No\tCaller\tCallee\tLeftValue\tIsPrimitive\tIsConstructor\tCalleeFile\tCalleeLocation\tCallerFile\tCallerExpression\tCallExpLocation");
		writer.println(message.toString());
		
		List<CompilationUnitScope> unitList = manager.getAllCompilationUnitScopes();
		for (CompilationUnitScope unitScope : unitList) {
			String unitFileName = unitScope.getUnitName();
			CompilationUnit astRoot = manager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitFileName);
			CompilationUnitRecorder unitRecorder = new CompilationUnitRecorder(unitFileName, astRoot);

			Debug.println("Total files " + unitList.size() + ", File " + fileCounter + " " + unitFileName);
			List<DetailedTypeDefinition> typeList = manager.getAllDetailedTypeDefinitions(unitScope);
			for (DetailedTypeDefinition type : typeList) {
				if (type.isInterface()) continue;
				
				List<MethodDefinition> methodList = type.getMethodList();
				
				if (methodList == null) continue;
				for (MethodDefinition method : methodList) {
					ControlFlowGraph cfg = CFGCreator.create(manager, unitRecorder, method);
					if (cfg == null) continue;
					List<CallExpressionRecorder> callExpList = collectCallExpressionRecorder(manager, unitRecorder, cfg);

					for (CallExpressionRecorder info : callExpList) {
						counter++;
						if (info.calleeList == null) {
							message = new StringBuilder();
							message.append(counter + "\t" + method.getSimpleName());
							message.append("\tUnknown");
							if (info.leftValue != null) message.append("\t" + info.leftValue.getExpression());
							else message.append("\tNone");
							message.append("\tUnknown\tUnknown\tUnknown\tUnknown");
							message.append("\t" + unitFileName + "\t" + info.expression + "\t" + info.reference.toSimpleString() + info.reference.getLocation().getUniqueId());
							writer.println(message.toString());
						} else {
							for (MethodDefinition callee : info.calleeList) {
								message = new StringBuilder();
								message.append(counter + "\t" + method.getSimpleName());
								message.append("\t" + callee.getSimpleName());
								if (info.leftValue != null) message.append("\t" + info.leftValue.getExpression());
								else message.append("\tNone");
								message.append("\t" + ReturnValueAnalyzer.isReturnPrimitiveValue(callee));
								message.append("\t" + callee.isConstructor());
								message.append("\t" + callee.getLocation().getFileUnitName());
								message.append("\t" + callee.getUniqueId());
								message.append("\t" + unitFileName + "\t" + info.expression + "\t" + info.reference.toSimpleString() + info.reference.getLocation().getUniqueId());
								writer.println(message.toString());
							}
						}
					}
				}
			}
			sourceCodeFileSet.releaseAST(unitFileName);
			sourceCodeFileSet.releaseFileContent(unitFileName);
		}
		writer.flush();
	}
	
	public static List<CallExpressionRecorder> collectCallExpressionRecorder(NameTableManager manager, CompilationUnitRecorder unitRecorder, ControlFlowGraph graph) {
		List<CallExpressionRecorder> result = new ArrayList<CallExpressionRecorder>();

		// Collect method call expressions at first. 
		List<GraphNode> nodeList = graph.getAllNodes();
		for (GraphNode node : nodeList) {
			CFGNode cfgNode = (CFGNode)node;
			if (cfgNode.getCFGNodeType() != CFGNodeType.N_EXECUTION_POINT) continue;
			ExecutionPoint exePoint = (ExecutionPoint)cfgNode;
			if (exePoint.isVirtual()) continue;
			
			ASTNode astNode = exePoint.getAstNode();
			if (astNode == null) continue;
			
			SourceCodeLocation startLocation = exePoint.getStartLocation();
			SourceCodeLocation endLocation = exePoint.getEndLocation();
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
				IAbstractStorageModel leftValue = null;
				if (leftReference.resolveBinding()) {
					leftValue = StorageModelFactory.extractLeftStorageModelInReference(leftReference);
				}

				Expression rightHandSide = assignment.getRightHandSide();
				referenceVisitor.reset();
				rightHandSide.accept(referenceVisitor);
				NameReference rightReference = referenceVisitor.getResult();
				rightReference.resolveBinding();
				List<CallExpressionRecorder> callExpList = collectCallExpressionRecorderInReference(exePoint, leftValue, rightReference);
				result.addAll(callExpList);
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
						referenceVisitor.reset();
						expression.accept(referenceVisitor);
						NameReference valueReference = referenceVisitor.getResult();
						valueReference.resolveBinding();
						
						List<CallExpressionRecorder> callExpList = collectCallExpressionRecorderInReference(exePoint, new SimpleStorageModel(variable), valueReference);
						result.addAll(callExpList);
						break;
					}
				}
				if (!found) {
					throw new AssertionError("Can not find variable definition for enhanced for parameter: " + parameter.toString() + " at " + startLocation.getUniqueId());
				}
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
					if (initializer == null) continue;
					boolean found = false;
					for (NameDefinition variable : variableList) {
						if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
							found = true;
							referenceVisitor.reset();
							initializer.accept(referenceVisitor);
							NameReference valueReference = referenceVisitor.getResult();
							valueReference.resolveBinding();
							List<CallExpressionRecorder> callExpList = collectCallExpressionRecorderInReference(exePoint, new SimpleStorageModel(variable), valueReference);
							result.addAll(callExpList);
							break;
						}
					}
					if (!found) {
						throw new AssertionError("Can not find variable definition for variable declaration: " + fragment.toString() + " at " + startLocation.getUniqueId());
					}

				}
			} else if (nodeType == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
				VariableDeclarationFragment fragment = (VariableDeclarationFragment)astNode;
				
				NameDefinitionVisitor definitionVisitor = new NameDefinitionVisitor();
				NameDefinitionLocationFilter filter = new NameDefinitionLocationFilter(startLocation, endLocation);
				definitionVisitor.setFilter(filter);
				currentScope.accept(definitionVisitor);
				List<NameDefinition> variableList = definitionVisitor.getResult();
				
				Expression initializer = fragment.getInitializer();
				if (initializer == null) continue;
				boolean found = false;
				for (NameDefinition variable : variableList) {
					if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
						found = true;
						referenceVisitor.reset();
						initializer.accept(referenceVisitor);
						NameReference valueReference = referenceVisitor.getResult();
						valueReference.resolveBinding();
						List<CallExpressionRecorder> callExpList = collectCallExpressionRecorderInReference(exePoint, new SimpleStorageModel(variable), valueReference);
						result.addAll(callExpList);
						break;
					}
				}
				if (!found) {
					throw new AssertionError("Can not find variable definition for variable declaration: " + fragment.toString() + " at " + startLocation.getUniqueId());
				}
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
					if (initializer == null) continue;
					boolean found = false;
					for (NameDefinition variable : variableList) {
						if (variable.getSimpleName().equals(fragment.getName().getIdentifier())) {
							found = true;
							referenceVisitor.reset();
							initializer.accept(referenceVisitor);
							NameReference valueReference = referenceVisitor.getResult();
							valueReference.resolveBinding();
							List<CallExpressionRecorder> callExpList = collectCallExpressionRecorderInReference(exePoint, new SimpleStorageModel(variable), valueReference);
							result.addAll(callExpList);
							break;
						}
					}
					if (!found) {
						throw new AssertionError("Can not find variable definition for variable declaration: " + fragment.toString() + " at " + startLocation.getUniqueId());
					}
				}
			} else {
				List<NameReference> referenceList = referenceCreator.createReferencesForASTNode(unitRecorder.unitName, astNode);
				for (NameReference reference : referenceList) {
					reference.resolveBinding();
					List<CallExpressionRecorder> callExpList = collectCallExpressionRecorderInReference(exePoint, null, reference);
					result.addAll(callExpList);
				}
			}
		}
		
		return result;
	}
	
	public static List<CallExpressionRecorder> collectCallExpressionRecorderInReference(ExecutionPoint node, IAbstractStorageModel leftValue, NameReference reference) {
		List<CallExpressionRecorder> result = new ArrayList<CallExpressionRecorder>(); 
		if (reference == null) return result;

		NameReference coreReference = reference.getCoreReference();
		if (coreReference.getReferenceKind() == NameReferenceKind.NRK_METHOD) {
			MethodReference methodReference = (MethodReference)coreReference;
			List<MethodDefinition> alternativeCalleeList = methodReference.getAlternativeList();
			String expression = reference.toSimpleString();
			ObjectCallExpressionRecorder info = new ObjectCallExpressionRecorder();
			info.node = node;
			info.calleeList = alternativeCalleeList;
			info.expression = expression;
			info.leftValue = leftValue;
			info.reference = methodReference;
			result.add(info);
		}
		return result;
	}
}
