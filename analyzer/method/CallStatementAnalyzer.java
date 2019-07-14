package analyzer.method;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import analyzer.cfg.predicate.NodePredicateRecorder;
import analyzer.cfg.reachDefinition.ConditionDefinitionRecorder;
import analyzer.cfg.reachDefinition.DefinitionRecorder;
import analyzer.cfg.reachDefinition.GeneratedConditionDefinitionRecorder;
import analyzer.cfg.reachDefinition.IReachConditionDefinitionRecorder;
import analyzer.cfg.reachDefinition.ReachConditionDefinitionAnalyzer;
import analyzer.dataTable.DataTableManager;
import analyzer.logic.NullCheckExpression;
import analyzer.storageModel.IAbstractStorageModel;
import graph.basic.GraphNode;
import graph.cfg.CFGNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.creator.CFGCreator;
import nameTable.NameTableManager;
import nameTable.creator.NameReferenceCreator;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameReference.referenceGroup.NameReferenceGroupKind;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ26ÈÕ
 * @version 1.0
 *
 */
public class CallStatementAnalyzer {
	private static ReturnValueAnalyzer returnValueAnalyzer = null;

	/**
	 * Note: we assume that the return value information in file are sorted by the column "CalleeLocation" 
	 */
	public static void checkReturnValueNullCheckConsistence(String file, PrintWriter writer) throws IOException {
		DataTableManager manager = new DataTableManager("result");
		manager.read(file, true);
		
		int lineNumber = manager.getLineNumber();
		int index = 0;
		
		while (index < lineNumber) {
			int startCalleeIndex = index;
			String calleeLocation = manager.getCellValueAsString(index, "CalleeLocation");
			String status1 = manager.getCellValueAsString(index, "Status");
			
			System.out.println("Total " + lineNumber + ", Check " + index + ", callee " + calleeLocation);
			boolean same = true;
			int checkIndex = index + 1;
			while (checkIndex < lineNumber) {
				String calleeLocation2 = manager.getCellValueAsString(checkIndex, "CalleeLocation");
				String status2 = manager.getCellValueAsString(checkIndex, "Status");
				
				if (!calleeLocation2.equals(calleeLocation)) break;
				if (!status1.equals(status2) && !status1.equals("Unused") && !status2.equals("Unused")) {
					same = false;
					break;
				} else if (status1.equals("Unused") && !status2.equals("Unused")) status1 = status2;
				checkIndex = checkIndex + 1;
			}
			
			if (!same) {
				checkIndex = startCalleeIndex;
				String method = manager.getCellValueAsString(checkIndex, "Callee");
				calleeLocation = manager.getCellValueAsString(checkIndex, "CalleeLocation");
				writer.println("Method: " + method + "() at " + calleeLocation);
				
				StringBuilder checkReference = new StringBuilder();
				StringBuilder uncheckReference = new StringBuilder();
				StringBuilder warningReference = new StringBuilder();
				StringBuilder redundantReference = new StringBuilder();
				StringBuilder otherReference = new StringBuilder();
				StringBuilder informativeReference = new StringBuilder();
				
				while (checkIndex < lineNumber) {
					String calleeLocation2 = manager.getCellValueAsString(checkIndex, "CalleeLocation");
					String status2 = manager.getCellValueAsString(checkIndex, "Status");
					String reference = manager.getCellValueAsString(checkIndex, "CallRef");
					String refLocation = manager.getCellValueAsString(checkIndex, "CallRefLocation");

					System.out.println("\tCallee " + calleeLocation2 + ", status " + status2);
					if (!calleeLocation2.equals(calleeLocation)) break;
					
					if (status2.equals("NormalCheck")) checkReference.append("\t\t" + reference + "[" + refLocation + "]\r\n");
					else if (status2.equals("NormalUncheck")) uncheckReference.append("\t\t" + reference + "[" + refLocation + "]\r\n");
					else if (status2.equals("Warnning")) warningReference.append("\t\t" + reference + "[" + refLocation + "]\r\n");
					else if (status2.equals("Redundant")) redundantReference.append("\t\t" + reference + "[" + refLocation + "]\r\n");
					else if (status2.equals("OtherCheck")) otherReference.append("\t\t" + reference + "[" + refLocation + "]\r\n");
					else if (status2.equals("Informative")) informativeReference.append("\t\t" + reference + "[" + refLocation + "]\r\n");
					
					checkIndex = checkIndex + 1;
				}
				if (warningReference.length() > 0) {
					writer.println("\tWarning: " );
					writer.println(warningReference.toString());
				}
				if (informativeReference.length() > 0) {
					writer.println("\tInformative: " );
					writer.println(informativeReference.toString());
				}
				if (redundantReference.length() > 0) {
					writer.println("\tRedundant: " );
					writer.println(redundantReference.toString());
				}
				if (checkReference.length() > 0) {
					writer.println("\tNormalCheck: " );
					writer.println(checkReference.toString());
				}
				if (otherReference.length() > 0) {
					writer.println("\tOtherCheck: " );
					writer.println(otherReference.toString());
				}
				if (uncheckReference.length() > 0) {
					writer.println("\tNormalUncheck: " );
					writer.println(uncheckReference.toString());
				}
			}
			index = checkIndex; 
		}
	}
	
	/**
	 * Note: we assume that the return value information in file are sorted by the column "CalleeLocation" 
	 */
	public static void printReturnValueAsText(String file, PrintWriter writer) throws IOException {
		DataTableManager manager = new DataTableManager("result");
		manager.read(file, true);
		
		String lastCalleeLocation = "";
		int lineNumber = manager.getLineNumber();
		int index = 0;
		while (index < lineNumber) {
			String calleeLocation = manager.getCellValueAsString(index, "CalleeLocation");
			String caller = manager.getCellValueAsString(index, "Caller");
			String callee = manager.getCellValueAsString(index, "Callee");
			String leftValue = manager.getCellValueAsString(index, "LeftValue");
			String isChecked = manager.getCellValueAsString(index, "IsChecked");
			String checkReference = manager.getCellValueAsString(index, "CheckReference");
			String exactCheck = manager.getCellValueAsString(index, "ExactCheck");
			String isUsed = manager.getCellValueAsString(index, "IsUsed");
			String useReference = manager.getCellValueAsString(index, "UseReference");
			String leftValueUse = manager.getCellValueAsString(index, "LeftValueUse");
//			String callReference = manager.getCellValueAsString(index, "CallRef");
			String callRefLocation = manager.getCellValueAsString(index, "CallRefLocation");
			String expression = manager.getCellValueAsString(index, "Expression");
			String status = manager.getCellValueAsString(index, "Status");

			if (!calleeLocation.equals(lastCalleeLocation)) {
				String returnNull = manager.getCellValueAsString(index, "ReturnNull");
				String isPrimitive = manager.getCellValueAsString(index, "IsPrimitive");
				String isConstructor = manager.getCellValueAsString(index, "IsConstructor");
				writer.println();
				writer.println(callee + "[" + calleeLocation + "], Return Null: " + returnNull + ", Primitive: " + isPrimitive + ", Constructor: " + isConstructor);
			}
			writer.println("\tExpression " + expression + "[" + callRefLocation + "], in method " + caller);
			writer.println("\t\tLeft value : " + leftValue);
			if (isChecked.equalsIgnoreCase("true")) {
				writer.println("\t\tChecked : " + isChecked + ", Exactly check: " + exactCheck + ", Check Reference: " + checkReference);
			} else {
				writer.println("\t\tChecked : " + isChecked);
			}
			if (isUsed.equalsIgnoreCase("true")) {
				writer.println("\t\tUsed : " + isUsed + ", Left value use: " + leftValueUse + ", Use Reference: " + useReference);
			} else {
				writer.println("\t\tUsed : " + isUsed);
			}
			writer.println("\t\tStatus: " + status);
			lastCalleeLocation = calleeLocation;
			
			index = index+1; 
		}
	}

	public static void collectAllMethodCallStatementByScanningMethods(String path, PrintWriter writer) {
		NameTableManager manager = NameTableManager.createNameTableManager(path);

		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		manager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();
		
		returnValueAnalyzer = new ReturnValueAnalyzer(manager);
		returnValueAnalyzer.analyze();
		
		int counter = 0;
		int methodCounter = 0;
		StringBuilder message = new StringBuilder("No\tCaller\tCallee\tLeftValue\tReturnNull\tIsChecked\tCheckReference\tExactCheck\tIsUsed\tUseReference\tLeftValueUse\tIsPrimitive\tIsConstructor\tCalleeLocation\tCallRef\tCallRefLocation\tExpression\tStatus");
		writer.println(message.toString());
		
		for (NameDefinition nameDefinition : methodList) {
//			if (!nameDefinition.getFullQualifiedName().contains("getSpearmanCoefficient")) continue;
			
			methodCounter++;
			MethodDefinition method = (MethodDefinition)nameDefinition;
			Debug.println("Total method " + methodList.size() + ", Method " + methodCounter + " " + method.getFullQualifiedName());

			if (method.isAutoGenerated()) continue; 
			TypeDefinition enclosingType = method.getEnclosingType();
			if (!enclosingType.isDetailedType()) continue;
			DetailedTypeDefinition type = (DetailedTypeDefinition)enclosingType;
			if (type.isAnonymous()) continue;
			
			List<ObjectCallExpressionRecorder> infoList = collectMethodCallExpressionRecorder(manager, method);
			for (ObjectCallExpressionRecorder info : infoList) {
				counter++;
				Debug.println("\tCall expression " + counter + ": " + info.reference.toSimpleString());
				boolean returnNull = false;
				boolean isPrimitive = false;
				boolean isConstructor = false;

				if (info.calleeList == null) {
					message = new StringBuilder();
					message.append(counter + "\t" + method.getSimpleName());
					message.append("\tUnknown");
					if (info.leftValue != null) message.append("\t" + info.leftValue.getExpression());
					else message.append("\tNone");

					if (returnNull) message.append("\tTRUE");
					else message.append("\tFALSE");
					
					if (info.checkReference != null) message.append("\tTRUE\t[" + info.checkReference.getLocation() + "]" + info.checkReference.toSimpleString());
					else message.append("\tFALSE\t~~");
					message.append("\t" + info.exactlyCheck);
					
					if (info.useReference != null) message.append("\tTRUE\t[" + info.useReference.getLocation() + "]" + info.useReference.toSimpleString());
					else message.append("\tFALSE\t~~");
					message.append("\t" + info.leftValueUse);

					if (isPrimitive) message.append("\tTRUE");
					else message.append("\tFALSE");

					if (isConstructor) message.append("\tTRUE");
					else message.append("\tFALSE");

					message.append("\t~~");
					message.append("\t" + info.reference.toSimpleString() + "\t" + info.reference.getLocation().getUniqueId() + "\t" + info.expression);

					if (returnNull && info.checkReference != null) message.append("\tNormalCheck");
					else if (returnNull && info.checkReference == null) {
						if (info.useReference != null) {
							if (info.leftValueUse == true) message.append("\tWarnning");
							else message.append("\tInformative");
						}
						else message.append("\tUnused");
					} else if (!returnNull && info.checkReference != null) {
						if (info.exactlyCheck) message.append("\tRedundant");
						else message.append("\tOtherCheck");
					}
					else message.append("\tNormalUncheck");
					writer.println(message);
				} else {
					for (MethodDefinition callee : info.calleeList) {
						if (ReturnValueAnalyzer.isReturnPrimitiveValue(callee)) isPrimitive = true;
						if (callee.isConstructor()) continue;
						if (returnValueAnalyzer.isPossibleReturnNull(callee)) returnNull = true;

						message = new StringBuilder();
						message.append(counter + "\t" + method.getSimpleName());
						message.append("\t" + callee.getFullQualifiedName());

						if (info.leftValue != null) message.append("\t" + info.leftValue.getExpression());
						else message.append("\tNone");

						if (returnNull) message.append("\tTRUE");
						else message.append("\tFALSE");
						
						if (info.checkReference != null) message.append("\tTRUE\t[" + info.checkReference.getLocation() + "]" + info.checkReference.toSimpleString());
						else message.append("\tFALSE\t~~");
						message.append("\t" + info.exactlyCheck);
						
						if (info.useReference != null) message.append("\tTRUE\t[" + info.useReference.getLocation() + "]" + info.useReference.toSimpleString());
						else message.append("\tFALSE\t~~");
						message.append("\t" + info.leftValueUse);

						if (isPrimitive) message.append("\tTRUE");
						else message.append("\tFALSE");

						if (isConstructor) message.append("\tTRUE");
						else message.append("\tFALSE");

						message.append("\t" + callee.getUniqueId());
						message.append("\t" + info.reference.toSimpleString() + "\t" + info.reference.getLocation().getUniqueId() + "\t" + info.expression);

						if (returnNull && info.checkReference != null) message.append("\tNormalCheck");
						else if (returnNull && info.checkReference == null) {
							if (info.useReference != null) {
								if (info.leftValueUse == true) message.append("\tWarnning");
								else message.append("\tInformative");
							}
							else message.append("\tUnused");
						} else if (!returnNull && info.checkReference != null) {
							if (info.exactlyCheck) message.append("\tRedundant");
							else message.append("\tOtherCheck");
						}
						else message.append("\tNormalUncheck");

						writer.println(message);
					}
				}
			}
		}
		writer.flush();
	}
	
	public static List<ObjectCallExpressionRecorder> collectMethodCallExpressionRecorder(NameTableManager manager, MethodDefinition method) {
		List<ObjectCallExpressionRecorder> result = new ArrayList<ObjectCallExpressionRecorder>();

		CompilationUnitScope unitScope = manager.getEnclosingCompilationUnitScope(method);
		String unitFileName = unitScope.getUnitName();
		CompilationUnit astRoot = manager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitFileName);
		CompilationUnitRecorder unitRecorder = new CompilationUnitRecorder(unitFileName, astRoot);
		SourceCodeFileSet sourceCodeFileSet = manager.getSouceCodeFileSet();
		
		// Create a ControFlowGraph object
		ControlFlowGraph graph = CFGCreator.create(manager, unitRecorder, method);
		if (graph == null) {
			sourceCodeFileSet.releaseAST(unitFileName);
			sourceCodeFileSet.releaseFileContent(unitFileName);
			return result;
		}
		
		ReachConditionDefinitionAnalyzer.setReachConditionDefinitionRecorder(graph);
		ReachConditionDefinitionAnalyzer.reachingConditionDefinitionAnalysis(manager, unitRecorder, method, graph);
		
		// Collect method call expressions at first. 
		List<GraphNode> nodeList = graph.getAllNodes();
		for (GraphNode node : nodeList) {
			if (!(node instanceof ExecutionPoint)) continue;
			ExecutionPoint exePoint = (ExecutionPoint)node;
			if (exePoint.isVirtual()) continue;
			
			IReachConditionDefinitionRecorder recorder = (IReachConditionDefinitionRecorder)exePoint.getFlowInfoRecorder();
			List<GeneratedConditionDefinitionRecorder> generatedDefinitionList = recorder.getGeneratedConditionDefinitionList();
			// Collect method call expressions in this AST node from the generated name list.
			
			for (GeneratedConditionDefinitionRecorder generatedDefinition : generatedDefinitionList) {
				List<ObjectCallExpressionRecorder> infoList = collectMethodCallExpressionRecorderInReference(generatedDefinition.getNode(), 
						generatedDefinition.getLeftModel(), generatedDefinition.getRightValueExpression());
				for (ObjectCallExpressionRecorder info : infoList) result.add(info);
			}
		}
		
		// Then, collect whether a return value has been checked or used. Note that, when a method invocation is in a loop, 
		// its return value maybe checked before this invocation, e.g. in the loop condition.
		for (GraphNode node : nodeList) {
			if (!(node instanceof ExecutionPoint)) continue;
			ExecutionPoint exePoint = (ExecutionPoint)node;
			if (exePoint.isVirtual()) continue;
			
			IReachConditionDefinitionRecorder recorder = (IReachConditionDefinitionRecorder)exePoint.getFlowInfoRecorder();
			NodePredicateRecorder predicateRecorder = recorder.getTruePredicate();
			if (predicateRecorder == null) continue;
			// Collect check null predicates in this AST node from its checkedReferenceList, and find if a left value in 
			// MethodCallExpressionRecorder list is checked in this AST Node
			List<NullCheckExpression> checkExpressionList = NullCheckExpression.extractNullCheckExpression(predicateRecorder, true);
			for (NullCheckExpression checkExpression : checkExpressionList) {
				NameReference checkedReference = checkExpression.getCheckedReference();
				List<ConditionDefinitionRecorder> definitionRecorderList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(exePoint, checkedReference);
				for (ConditionDefinitionRecorder conditionDefinition : definitionRecorderList) {
					DefinitionRecorder mainDefinition = conditionDefinition.getMainDefinition();
					for (ObjectCallExpressionRecorder info : result) {
						if (mainDefinition.getNode() == info.node && mainDefinition.getLeftStorage() == info.leftValue) {
							info.checkReference = checkedReference;
							if (definitionRecorderList.size() <= 1) info.exactlyCheck = true;
							else info.exactlyCheck = false;
						}
					}
				}
			}
			
			// collect object reference dereference information in this AST node. If the object reference is not dereference, 
			// then it is not need to check it.
			NameReferenceCreator referenceCreator = new NameReferenceCreator(manager, true);
			ASTNode astNode = exePoint.getAstNode();
			if (astNode == null) continue;

			List<NameReference> referenceList = referenceCreator.createReferencesForASTNode(unitFileName, astNode);
			for (NameReference reference : referenceList) {
				reference.resolveBinding();
				extractLeftValueUsingInReference(result, exePoint, reference);
				extractArgumentUsingInReference(result, exePoint, reference);
			}
		}

		sourceCodeFileSet.releaseAST(unitFileName);
		sourceCodeFileSet.releaseFileContent(unitFileName);
		return result;
	}

	public static void extractLeftValueUsingInReference(List<ObjectCallExpressionRecorder> infoList, ExecutionPoint node, NameReference reference) {
		// The expression in enhance for statement should be regarded as a left value using if it is a value reference
		if (node.isEnhancedForPredicate() && reference.isValueReference()) {
			List<ConditionDefinitionRecorder> conditionDefinitionList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(node, reference);
			for (ConditionDefinitionRecorder conditionDefinition : conditionDefinitionList) {
				DefinitionRecorder mainDefinition = conditionDefinition.getMainDefinition();
				for (ObjectCallExpressionRecorder info : infoList) {
					if (mainDefinition.getNode() == info.node && mainDefinition.getLeftStorage() == info.leftValue) {
						info.useReference = reference;
						info.leftValueUse = true;
					}
				}
			}
			return;
		}
		
		if (!reference.isGroupReference()) return;
		
		NameReferenceGroup group = (NameReferenceGroup)reference;
		List<NameReference> sublist = group.getSubReferenceList();
		NameReferenceGroupKind groupKind = group.getGroupKind();

		if (groupKind == NameReferenceGroupKind.NRGK_FIELD_ACCESS || 
				groupKind == NameReferenceGroupKind.NRGK_QUALIFIED_NAME || 
				groupKind == NameReferenceGroupKind.NRGK_ARRAY_ACCESS) {
			NameReference objectExpression = sublist.get(0);
			List<NameReference> objectReferenceList = objectExpression.getReferencesAtLeaf();
			for (NameReference objectReference : objectReferenceList) {
				List<ConditionDefinitionRecorder> conditionDefinitionList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(node, objectReference);
				for (ConditionDefinitionRecorder conditionDefinition : conditionDefinitionList) {
					DefinitionRecorder mainDefinition = conditionDefinition.getMainDefinition();
					for (ObjectCallExpressionRecorder info : infoList) {
						if (mainDefinition.getNode() == info.node && mainDefinition.getLeftStorage() == info.leftValue) {
							info.useReference = objectReference;
							info.leftValueUse = true;
						}
					}
				}
			}
		} else if (group.getGroupKind() == NameReferenceGroupKind.NRGK_METHOD_INVOCATION) {
			NameReference objectExpression = sublist.get(0);
			if (objectExpression.getReferenceKind() != NameReferenceKind.NRK_METHOD) {
				List<NameReference> objectReferenceList = objectExpression.getReferencesAtLeaf();
				for (NameReference objectReference : objectReferenceList) {
					List<ConditionDefinitionRecorder> conditionDefinitionList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(node, objectReference);
					for (ConditionDefinitionRecorder conditionDefinition : conditionDefinitionList) {
						DefinitionRecorder mainDefinition = conditionDefinition.getMainDefinition();
						for (ObjectCallExpressionRecorder info : infoList) {
							if (mainDefinition.getNode() == info.node && mainDefinition.getLeftStorage() == info.leftValue) {
								info.useReference = objectReference;
								info.leftValueUse = true;
							}
						}
					}
				}
			}
			return;
		}
		if (sublist != null) {
			for (NameReference subreference : sublist) extractLeftValueUsingInReference(infoList, node, subreference);
		}
	}
	
	public static void extractArgumentUsingInReference(List<ObjectCallExpressionRecorder> infoList, ExecutionPoint node, NameReference reference) {
		List<NameReference> referenceList = reference.getReferencesAtLeaf();
		for (NameReference leafReference : referenceList) {
			if (leafReference.isMethodReference()) {
				MethodReference methodReference = (MethodReference)leafReference;
				List<NameReference> argumentList = methodReference.getArgumentList();
				if (argumentList != null) {
					for (NameReference argument : argumentList) {
						List<ConditionDefinitionRecorder> conditionDefinitionList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(node, argument);
						for (ConditionDefinitionRecorder conditionDefinition : conditionDefinitionList) {
							DefinitionRecorder mainDefinition = conditionDefinition.getMainDefinition();
							for (ObjectCallExpressionRecorder info : infoList) {
								if (info.useReference != null) continue;
								if (mainDefinition.getNode() == info.node && mainDefinition.getLeftStorage() == info.leftValue) {
									info.useReference = argument;
								}
							}
						}
					}
				}
			}
		}
	}

	public static List<ObjectCallExpressionRecorder> collectMethodCallExpressionRecorderInReference(CFGNode node, IAbstractStorageModel leftModel, NameReference reference) {
		List<ObjectCallExpressionRecorder> result = new ArrayList<ObjectCallExpressionRecorder>(); 
		if (reference == null) return result;

		NameReference coreReference = reference.getCoreReference();
		if (coreReference.getReferenceKind() == NameReferenceKind.NRK_METHOD) {
			MethodReference methodReference = (MethodReference)coreReference;
			List<MethodDefinition> alternativeCalleeList = methodReference.getAlternativeList();
			String expression = reference.toSimpleString();
			ObjectCallExpressionRecorder info = new ObjectCallExpressionRecorder();
			info.node = (ExecutionPoint)node;
			info.calleeList = alternativeCalleeList;
			info.expression = expression;
			info.leftValue = leftModel;	
			info.reference = methodReference;
			result.add(info);
		}
		return result;
	}
}
