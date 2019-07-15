package analyzer.method;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import analyzer.cfg.reachDefinition.ConditionDefinitionRecorder;
import analyzer.cfg.reachDefinition.ReachConditionDefinitionAnalyzer;
import analyzer.logic.MLogicValue;
import analyzer.storageModel.IAbstractStorageModel;
import analyzer.storageModel.StorageModelFactory;
import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.ExecutionPointLabel;
import graph.cfg.ExecutionPointType;
import graph.cfg.creator.CFGCreator;
import nameTable.NameTableASTBridge;
import nameTable.NameTableManager;
import nameTable.creator.NameReferenceCreator;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.TypeReference;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameReference.referenceGroup.NameReferenceGroupKind;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeFileSet;
import sourceCodeAST.SourceCodeLocation;
//import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018年7月23日
 * @version 1.0
 *
 */
public class ReturnValueAnalyzer {
	TreeMap<MethodDefinition, ReturnValueRecorderList> map = null;
	NameTableManager manager = null;
	
	public ReturnValueAnalyzer(NameTableManager manager) {
		this.manager = manager;
		map = new TreeMap<MethodDefinition, ReturnValueRecorderList>();
	}
	
	public void reset() {
		map = new TreeMap<MethodDefinition, ReturnValueRecorderList>();
	}
	
	public void analyze() {
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		manager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();
		if (methodList == null) return;
		for (NameDefinition definition : methodList) {
			if (definition.isMethodDefinition()) {
				collectReturnValueRecordFromAllRelatedMethods((MethodDefinition)definition);
			}
		}
		refineReturnNullValueJudgement();
	}
	
	public void analyze(List<NameDefinition> methodList) {
		if (methodList == null) return;
		for (NameDefinition definition : methodList) {
			if (definition.isMethodDefinition()) {
				collectReturnValueRecordFromAllRelatedMethods((MethodDefinition)definition);
			}
		}
		refineReturnNullValueJudgement();
	}

	public void analyze(MethodDefinition method) {
		boolean hasCollected = collectReturnValueRecordFromAllRelatedMethods(method);	
		if (hasCollected) refineReturnNullValueJudgement();
	}

	public static boolean isReturnPrimitiveValue(MethodDefinition method) {
		NameReference returnType = method.getReturnType();
		if (returnType == null) return false;
		if (!returnType.isTypeReference()) {
			throw new AssertionError("The return type reference of method " + method.getUniqueId() + " is not a type reference!");
		}
		TypeReference typeReference = (TypeReference)returnType;
		if (typeReference.isArrayType()) return false;
		if (NameReferenceLabel.isPrimitiveTypeName(typeReference.getName())) return true;
		else return false;
	}
	
	public boolean isPossibleReturnNull(MethodDefinition method) {
		if (!map.containsKey(method)) {
			boolean hasCollected = collectReturnValueRecordFromAllRelatedMethods(method);	
			if (hasCollected) refineReturnNullValueJudgement();
			else return false;
		}
		ReturnValueRecorderList recorder = map.get(method);
		if (recorder == null) return false;
		return recorder.hasNullValue();
	}
	
	public MLogicValue getNullability(MethodDefinition method) {
		if (!map.containsKey(method)) {
			boolean hasCollected = collectReturnValueRecordFromAllRelatedMethods(method);	
			if (hasCollected) refineReturnNullValueJudgement();
			else return MLogicValue.UNKNOWN;
		}
		ReturnValueRecorderList recorder = map.get(method);
		if (recorder == null) return MLogicValue.FALSE;
		return MLogicValue.FALSE;
	}
	
	public ReturnValueRecorderList getReturnValueRecorderList(MethodDefinition method) {
		if (!map.containsKey(method)) {
			boolean hasCollected = collectReturnValueRecordFromAllRelatedMethods(method);	
			if (hasCollected) refineReturnNullValueJudgement();
		}
		return map.get(method);
	}
	
	public String getReturnValueInformation(MethodDefinition method) {
		if (!map.containsKey(method)) {
			boolean hasCollected = collectReturnValueRecordFromAllRelatedMethods(method);	
			if (hasCollected) refineReturnNullValueJudgement();
			else return "Can not collect return value information of " + method.getFullQualifiedName();
		}
		
		ReturnValueRecorderList recorderList = map.get(method);
		if (recorderList == null) return "Can not collect return value information of " + method.getFullQualifiedName();
		
		SourceCodeLocation location = method.getLocation();
		TypeReference returnType = method.getReturnType();
		MLogicValue nullable = recorderList.getNullability();

		StringBuilder builder = new StringBuilder();
		builder.append(method.getSimpleName() + "() : " + returnType.toDeclarationString() + " [" + location.getUniqueId() + "], Nullable: " + nullable + "\r\n");
		for (ReturnValueRecorder recorder : recorderList.returnValueList) {
			if (recorder.expression != null) {
				builder.append("\tExpression: " + recorder.expression.toSimpleString() + " [" + recorder.expression.getLocation() + "], Nullable: " + recorder.getNullability() + "\r\n");
			} else builder.append("\tExpression: Unknown, Nullable: " + recorder.getNullability() + "\r\n");
			builder.append("\tCondition: " + recorder.condition);

			if (recorder.value != null) {
				builder.append("\t\tValue: " + recorder.value.getUniqueId() + " [" + recorder.value.getDefinitionKind() + "]" + "\r\n");
			} else builder.append("\t\tValue: Unknown\r\n");
			
			
			for (ValueNullableRecorder valueRecorder : recorder.valueRecorderList) {
				builder.append("\t\tLast Assignment: " + valueRecorder.getDefinitionListString(location.getFileUnitName()) + "\r\n");
				builder.append("\t\t\tCondition: " + valueRecorder.getConditionChainString() + "\r\n");
				builder.append("\t\t\tNullability: " + valueRecorder.getNullabilityString() + "\r\n");
			}
		}
		
		return builder.toString();
	}
	
	public void printDetailsAsDataTable(PrintWriter writer) {
		String message = "File\tClass\tMethod\tReturnValue\tIsNull\tReturnExpression\tLastAssign\tAssignLocation\tReturnLocation\tMethodLocation";
		writer.println(message);
		
		Set<MethodDefinition> methodSet = map.keySet();
		for (MethodDefinition method : methodSet) {
			ReturnValueRecorderList recorderList = map.get(method);
			SourceCodeLocation location = method.getLocation();
			String unitFileName = location.getFileUnitName();
			DetailedTypeDefinition type = (DetailedTypeDefinition)method.getEnclosingType();
			
			message = unitFileName + "\t" + type.getSimpleName() + "\t" + method.getSimpleName();
			for (ReturnValueRecorder recorder : recorderList.returnValueList) {
				for (ValueNullableRecorder rootAssignmentRecorder : recorder.valueRecorderList) {
					String returnMessage = null;
					if (recorder.value != null) returnMessage = "\t" + recorder.value.getUniqueId();
					else returnMessage = "\t" + "Unknown";
					
					returnMessage += rootAssignmentRecorder.nullable;
					
					NameReference valueReference = rootAssignmentRecorder.getLastReference();
					returnMessage += "\t" + valueReference.toSimpleString();
					returnMessage += "\t" + valueReference.getLocation().getUniqueId();
					returnMessage += "\t" + recorder.expression.toSimpleString() + "\t[" + recorder.expression.getLocation() + "]";
					returnMessage += "\t" + location.getUniqueId();
					
					writer.println(message + returnMessage);
				}
			}
		}
	}
	
	public void printDetailsAsTextFile(PrintWriter writer) {
		Set<MethodDefinition> methodSet = map.keySet();
		for (MethodDefinition method : methodSet) {
			System.out.println("Print method " + method.getFullQualifiedName());
			ReturnValueRecorderList recorderList = map.get(method);
			SourceCodeLocation location = method.getLocation();
			TypeReference returnType = method.getReturnType();
			MLogicValue nullable = recorderList.getNullability();

			writer.println(method.getSimpleName() + "() : " + returnType.toDeclarationString() + " [" + location.getUniqueId() + "], Nullable: " + nullable);
			for (ReturnValueRecorder recorder : recorderList.returnValueList) {
				if (recorder.expression != null) {
					writer.println("\tExpression: " + recorder.expression.toSimpleString() + " [" + recorder.expression.getLocation() + "], Nullable: " + recorder.getNullability());
				} else writer.println("\tExpression: Unknown, Nullable: " + recorder.getNullability());
				writer.println("\tCondition: " + recorder.condition);

				if (recorder.value != null) {
					writer.println("\t\tValue: " + recorder.value.getUniqueId() + " [" + recorder.value.getDefinitionKind() + "]");
				} else writer.println("\t\tValue: Unknown");
				
				
				for (ValueNullableRecorder valueRecorder : recorder.valueRecorderList) {
					writer.println("\t\tLast Assignment: " + valueRecorder.getDefinitionListString(location.getFileUnitName()));
					writer.println("\t\t\tCondition: " + valueRecorder.getConditionChainString());
					writer.println("\t\t\tNullability: " + valueRecorder.getNullabilityString());
				}
			}
		}
		writer.flush();
	}

	public void printSummary(PrintWriter writer) {
		String message = "Method\tReturnType\tReturnNull\tIsAbstract\tLocation\tClass\tIsInterface\tFile";
		writer.println(message);
		
		Set<MethodDefinition> methodSet = map.keySet();
		for (MethodDefinition method : methodSet) {
			ReturnValueRecorderList recorderList = map.get(method);
			SourceCodeLocation location = method.getLocation();
			String unitFileName = location.getFileUnitName();
			DetailedTypeDefinition type = (DetailedTypeDefinition)method.getEnclosingType();
			TypeReference returnType = method.getReturnType();
			
			message = method.getSimpleName() + "\t" + returnType.toDeclarationString();
			message += "\t" + recorderList.getNullability();
			if (method.isAbstract()) message += "\tYes";
			else message += "\tNo";
			message += "\t" + location.getUniqueId();
			message += "\t" + type.getSimpleName(); 
			if (type.isInterface()) message += "\tYes" + "\t" + unitFileName;
			else message += "\tNo" + "\t" + unitFileName;
			writer.println(message);
		}
	}
	
	void refineReturnNullValueJudgement() {
		Set<MethodDefinition> methodSet = map.keySet();
		
		while (true) {
			boolean hasChanged = false;
			for (MethodDefinition method : methodSet) {
				ReturnValueRecorderList recorderList = map.get(method);
				MLogicValue nullable = recorderList.getNullability(); 
				if (nullable != MLogicValue.UNKNOWN) continue;

				for (ReturnValueRecorder recorder : recorderList.returnValueList) {
					if (recorder.getNullability() != MLogicValue.UNKNOWN) continue;
					
					for (ValueNullableRecorder valueRecorder : recorder.valueRecorderList) {
						if (valueRecorder.nullable != MLogicValue.UNKNOWN) continue;
						
						NameReference lastReference = valueRecorder.getLastReference();
						if (lastReference == null) continue;
						NameReference coreReference = lastReference.getCoreReference();
						NameDefinition coreDefinition = null;
						if (coreReference != null) coreDefinition = coreReference.getDefinition();
						if (coreDefinition == null) continue;
						if (coreDefinition.isMethodDefinition()) {
							MethodDefinition coreMethod = (MethodDefinition)coreDefinition;
							boolean needAnalysis = true;
							if (coreMethod.isConstructor()) needAnalysis = false;;
							if (isReturnPrimitiveValue(coreMethod)) needAnalysis = false;
							TypeDefinition coreType = coreMethod.getEnclosingType();
							if (!coreType.isDetailedType() || coreType.isAnonymous()) needAnalysis = false;;

							if (needAnalysis == false) continue;
							
							MethodReference methodReference = (MethodReference)coreReference;
							@SuppressWarnings("null")
							List<MethodDefinition> alternativeList = methodReference.getAlternativeList();
							
							if (alternativeList == null) continue;
							nullable = MLogicValue.FALSE;
							for (int index = 0; index < alternativeList.size(); index++) {
								MethodDefinition callee = alternativeList.get(index);
								if (callee == method) continue;
								ReturnValueRecorderList valueRecorderList = map.get(callee);
								if (valueRecorderList == null) {
									nullable = MLogicValue.UNKNOWN;
									continue;
								}
								MLogicValue calleeNullable = valueRecorderList.getNullability();
								if (calleeNullable == MLogicValue.TRUE) {
									nullable = MLogicValue.TRUE;
									break;
								} else if (calleeNullable == MLogicValue.UNKNOWN) nullable = MLogicValue.UNKNOWN;
							}
							if (nullable != MLogicValue.UNKNOWN) {
								hasChanged = true;
								valueRecorder.nullable = nullable;
							}
						}
					}
				}
			}
			if (!hasChanged) return;
		}
	}
	
	boolean collectReturnValueRecordFromAllRelatedMethods(MethodDefinition method) {
		if (method.isConstructor()) return false;
		if (isReturnPrimitiveValue(method)) return false;
		TypeDefinition type = method.getEnclosingType();
		if (!type.isDetailedType() || type.isAnonymous()) return false;
		
		LinkedList<MethodDefinition> needAnalyzeMethodQueue = new LinkedList<MethodDefinition>();
		needAnalyzeMethodQueue.add(method);
		boolean hasCollected = false;
		while (!needAnalyzeMethodQueue.isEmpty()) {
			MethodDefinition currentMethod = needAnalyzeMethodQueue.removeFirst();
			
			// Collect return value for the currentMethod. If the method call other methods in its return statments, the list of all
			// such callee methods will be returned, and all this callee should be analyzed further.
			List<MethodDefinition> methodReturnCalleeList = collectReturnValueRecord(currentMethod);
			if (methodReturnCalleeList != null) {
				hasCollected = true;
				for (MethodDefinition callee : methodReturnCalleeList) {
					if (!map.containsKey(callee)) {
						// This callee have not been analyzed, so add it to the last of the queue.
						needAnalyzeMethodQueue.addLast(callee);
					}
				}
			}
		}
		return hasCollected;
	}

	List<MethodDefinition> collectReturnValueRecord(MethodDefinition method) {
		if (method.getBodyScope() == null) return null;		// The method have not body, so we do not analyze it!
		
		NameTableASTBridge bridge = new NameTableASTBridge(manager);
		SourceCodeFileSet sourceCodeFileSet = manager.getSouceCodeFileSet();

		CompilationUnitScope unitScope = manager.getEnclosingCompilationUnitScope(method);
		String unitFileName = unitScope.getUnitName();
		CompilationUnit astRoot = manager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitFileName);
		if (astRoot == null) return null;
		CompilationUnitRecorder unitRecorder = new CompilationUnitRecorder(unitFileName, astRoot);
		
		// Create a ControFlowGraph object
		ControlFlowGraph graph = CFGCreator.create(manager, method);
		if (graph == null) return null; 
		
		ReachConditionDefinitionAnalyzer.setReachConditionDefinitionRecorder(graph);
		ReachConditionDefinitionAnalyzer.reachingConditionDefinitionAnalysis(manager, unitRecorder, method, graph);
//		ReachConditionDefinitionAnalyzer.printAllReachingConditionDefinitions(graph, Debug.getWriter());
//		NodePredicateChainAnalyzer.printAllNodePredicatesForDebugging(graph);

		NameReferenceCreator referenceCreator = new NameReferenceCreator(manager, true);
		
		List<GraphNode> nodeList = graph.getAllNodes();

		ReturnValueRecorderList recorderList = new ReturnValueRecorderList();
//		Debug.println("Analyze method " + method.getFullQualifiedName());
		
		List<MethodDefinition> calleeList = new ArrayList<MethodDefinition>();
		for (GraphNode node : nodeList) {
			ExecutionPoint exePoint = (ExecutionPoint)node;
			if (exePoint.getType() != ExecutionPointType.FLOW_CONTROLLER || exePoint.getLabel() != ExecutionPointLabel.RETURN_LABEL) continue;

			ASTNode astNode = exePoint.getAstNode();
			List<NameReference> referenceList = referenceCreator.createReferencesForASTNode(unitFileName, astNode);	

			NameReference reference = referenceList.get(0);
			ReturnValueRecorder recorder = new ReturnValueRecorder();
			recorder.expression = reference;
			recorder.condition = ReachConditionDefinitionAnalyzer.getReachCondition(exePoint);
			reference.resolveBinding();
			IAbstractStorageModel leftStorage = StorageModelFactory.extractLeftStorageModelInReference(reference);
			if (leftStorage != null) recorder.value = leftStorage.getCoreDefinition();

			List<ValueNullableRecorder> preliminaryList = new ArrayList<ValueNullableRecorder>();
			List<ConditionDefinitionRecorder> conditionDefinitionList = ReachConditionDefinitionAnalyzer.getReachConditionDefinitionList(exePoint, reference);
			
//			Debug.println("Return expression " + recorder.expression.toSimpleString() + ", reaching condition definition list size: " + conditionDefinitionList.size());
			if (conditionDefinitionList.size() <= 0) {
				ValueNullableRecorder valueRecorder = new ValueNullableRecorder(recorder.expression, recorder.value);
				valueRecorder.determineLastReferenceNullabilityInNode(exePoint);
				preliminaryList.add(valueRecorder);
			} else {
				for (ConditionDefinitionRecorder definitionRecorder : conditionDefinitionList) {
					ValueNullableRecorder valueRecorder = new ValueNullableRecorder(definitionRecorder);
					preliminaryList.add(valueRecorder);
					valueRecorder.determineNullability();
				}
			}
			
			recorder.valueRecorderList = new ArrayList<ValueNullableRecorder>();
			for (ValueNullableRecorder valueRecorder : preliminaryList) {
				NameReference lastReference = valueRecorder.getLastReference();
				NameDefinition lastDefinition = valueRecorder.getLastDefinition();
//				String lastReferenceString = "?";
				if (lastReference != null) {
//					lastReferenceString = lastReference.toSimpleString();
					lastReference = lastReference.getCoreReference();
				}
//				String lastDefinitionString = "?";
//				if (lastDefinition != null) lastDefinitionString = lastDefinition.getFullQualifiedName();

//				Debug.println("\tLast: " + lastReferenceString + "[" + lastDefinitionString + "]" + ", condition definition " + valueRecorder.assignment);

				if (valueRecorder.nullable != MLogicValue.UNKNOWN) {
					recorder.valueRecorderList.add(valueRecorder);
					continue;
				}
				if (lastDefinition == null) {
					valueRecorder.nullable = MLogicValue.FALSE;
					recorder.valueRecorderList.add(valueRecorder);
					continue;
				}
				
				if (lastDefinition.isMethodDefinition()) {
					recorder.valueRecorderList.add(valueRecorder);

					MethodDefinition coreMethod = (MethodDefinition)lastDefinition;
					boolean needAnalysis = true;
					if (coreMethod.isConstructor()) needAnalysis = false;;
					if (isReturnPrimitiveValue(coreMethod)) needAnalysis = false;
					TypeDefinition coreType = coreMethod.getEnclosingType();
					if (!coreType.isDetailedType() || coreType.isAnonymous()) needAnalysis = false;;

					if (needAnalysis == false) {
						valueRecorder.nullable = MLogicValue.FALSE;
						continue;
					}
					
					MethodReference methodReference = (MethodReference)lastReference;
					@SuppressWarnings("null") // methodReference must not be null since here coreDefinition is not null!
					List<MethodDefinition> alternativeList = methodReference.getAlternativeList();
					if (alternativeList != null) {
						for (int index = 0; index < alternativeList.size(); index++) {
							MethodDefinition callee = alternativeList.get(index);
							if (callee == method) continue;
							if (callee.getBodyScope() == null) {
								valueRecorder.nullable = MLogicValue.FALSE;
								continue;  // The callee has not method body, we do not analyze it!
							}
							
							TypeDefinition enclosingType = callee.getEnclosingType();
							if (enclosingType.isDetailedType() && !enclosingType.isAnonymous()) {
								// Add the callee to the return list for further analysis!
								calleeList.add(callee);
							}
						}
					} else valueRecorder.nullable = MLogicValue.FALSE;
				} else if (lastDefinition.isFieldDefinition()) {
					ConditionDefinitionRecorder definitionRecorder = valueRecorder.assignment;
					FieldDefinition field = (FieldDefinition)lastDefinition;
					List<NameReference> fieldAssignmentList = extractLastAssignmentForField(manager, referenceCreator, bridge, field);
					for (NameReference fieldValue : fieldAssignmentList) {
						// Extend the assignment path with the field as name, value in fieldAssignment as value
						// and add it as a root assignment of the current return expression
						ValueNullableRecorder fieldValueRecorder = new ValueNullableRecorder(definitionRecorder);
						fieldValueRecorder.lastReference = fieldValue;
						fieldValueRecorder.lastDefinition = lastDefinition;
						fieldValueRecorder.lastInDefinitionRecorder = false;
						if (fieldValue == null) fieldValueRecorder.nullable = MLogicValue.TRUE;
						else if (fieldValue.isNullReference()) fieldValueRecorder.nullable = MLogicValue.TRUE;
						else fieldValueRecorder.nullable = MLogicValue.FALSE;
//						fieldValueRecorder.nullable = ValueNullableRecorder.getNullability(fieldValue);
						recorder.valueRecorderList.add(fieldValueRecorder);
					}
				}
			}
			recorderList.returnValueList.add(recorder);
		}

		// Put the method and its recorder list to map
		map.put(method, recorderList);
		
		sourceCodeFileSet.releaseAST(unitFileName);
		sourceCodeFileSet.releaseFileContent(unitFileName);
		
		return calleeList;
	}
	
	
	List<NameReference> extractLastAssignmentForField(NameTableManager manager, NameReferenceCreator referenceCreator, NameTableASTBridge bridge, FieldDefinition field) {
		NameReference initializeExpression = field.getInitializer();
		List<NameReference> resultList = new ArrayList<NameReference>();
		
		TypeDefinition enclosingType = field.getEnclosingType();
		if (!enclosingType.isDetailedType()) {
			resultList.add(initializeExpression);
			return resultList;
		}
		DetailedTypeDefinition type = (DetailedTypeDefinition)enclosingType;
		
		String unitFileName = field.getLocation().getFileUnitName();
		SourceCodeFileSet sourceCodeFileSet = manager.getSouceCodeFileSet();
		CompilationUnit root = sourceCodeFileSet.findSourceCodeFileASTRootByFileUnitName(unitFileName);
		TypeDeclaration typeDeclaration = bridge.findASTNodeForDetailedTypeDefinition(type);
		
		// Find assignment for the field in the initializer block of the type
		// So far, We assume that there is only one initializer, and there is only sequence statements in the initializer, 
		// and then we do not build control flow graph for the initializer block statements
		NameReference lastAssignment = null;
		@SuppressWarnings("unchecked")
		List<BodyDeclaration> bodyList = typeDeclaration.bodyDeclarations();
		for (BodyDeclaration bodyDecl : bodyList) {
			int nodeType = bodyDecl.getNodeType();
			if (nodeType == ASTNode.INITIALIZER) {
				List<NameReference> referenceList = referenceCreator.createReferences(unitFileName, root, type, (Initializer)bodyDecl);
				for (NameReference reference : referenceList) {
					reference.resolveBinding();
					NameReference assignment = extractAssignmentRightExpressionForFieldInReference(field, reference);
					if (assignment != null) {
						if (lastAssignment == null) lastAssignment = assignment;
						else {
							SourceCodeLocation location = assignment.getLocation();
							SourceCodeLocation lastLocation = lastAssignment.getLocation();
							if (lastLocation.compareTo(location) >= 0) lastAssignment = assignment;
						}
					}
				}
			}
		}
		// The assignment to the field in last initializer is the last assignment for the field.
		if (lastAssignment != null) initializeExpression = lastAssignment;
		boolean addInitializer = false;
		
		// Find assignment for the field in the constructor of the type
		// So far, we also assume that there is only sequence statements in all constructors, and then we do not
		// build control flow graph for the constructor
		MethodDeclaration[] methodDeclarationArray = typeDeclaration.getMethods();
		for (int index = 0; index < methodDeclarationArray.length; index++) {
			MethodDeclaration methodDeclaration = methodDeclarationArray[index];
			if (!methodDeclaration.isConstructor()) continue;
			
			lastAssignment = null;
			
			SourceCodeLocation constrcutorLocation = SourceCodeLocation.getStartLocation(methodDeclaration, root, unitFileName);
			MethodDefinition methodDefinition = bridge.findDefinitionForMethodDeclaration(type, constrcutorLocation, methodDeclaration);
			List<NameReference> referenceList = referenceCreator.createReferences(unitFileName, root, methodDefinition, methodDeclaration);
			for (NameReference reference : referenceList) {
				reference.resolveBinding();
				NameReference assignment = extractAssignmentRightExpressionForFieldInReference(field, reference);
				if (assignment != null) {
					if (lastAssignment == null) lastAssignment = assignment;
					else {
						SourceCodeLocation location = assignment.getLocation();
						SourceCodeLocation lastLocation = lastAssignment.getLocation();
						if (lastLocation.compareTo(location) >= 0) lastAssignment = assignment;
					}
				}
			}
			if (lastAssignment != null) {
				resultList.add(lastAssignment);
			} else if (addInitializer == false) {
				// This constructor does not assign value to this field, we need consider the initializer of the field as
				// one of its root assignment
				resultList.add(initializeExpression);
				// Of course, we add the initializer of the field to the result list only one time! 
				addInitializer = true;
			}
		}
		if (resultList.size() <= 0) {
			resultList.add(initializeExpression);
		}
		return resultList;
	}
	
	NameReference extractAssignmentRightExpressionForFieldInReference(FieldDefinition field, NameReference reference) {
		if (!reference.isGroupReference()) return null;

		NameReferenceGroup group = (NameReferenceGroup)reference;
		NameReferenceGroupKind groupKind = group.getGroupKind();

		List<NameReference> sublist = group.getSubReferenceList();

		if(groupKind == NameReferenceGroupKind.NRGK_ASSIGNMENT) {
			NameReference leftReference = sublist.get(0);
			NameReference rightReference = sublist.get(1);
			IAbstractStorageModel leftStorage = StorageModelFactory.extractLeftStorageModelInReference(leftReference);
			if (leftStorage != null) {
				NameDefinition leftValue = leftStorage.getCoreDefinition();
				if (leftValue == field) return rightReference;
			}
		}
		return null;
	}
}
