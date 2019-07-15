package analyzer.cfg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import analyzer.cfg.dominateNode.DominateNodeAnalyzer;
import analyzer.cfg.predicate.NodePredicateChainAnalyzer;
import analyzer.cfg.predicate.NodePredicateChainRecorder;
import analyzer.cfg.reachDefinition.ConditionDefinitionRecorder;
import analyzer.cfg.reachDefinition.DefinitionRecorder;
import analyzer.cfg.reachDefinition.IReachDefinitionRecorder;
import analyzer.cfg.reachDefinition.ReachConditionDefinitionAnalyzer;
import analyzer.cfg.reachDefinition.ReachConditionDefinitionRecorder;
import analyzer.cfg.reachDefinition.ReachDefinitionAnalyzer;
import analyzer.cfg.reachDefinition.ReachDefinitionRecorder;
import nameTable.NameTableManager;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.SourceCodeLocation;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2017年9月3日
 * @version 1.0
 *
 */
public class TestCFGCreator {

	public static void main(String[] args) {
		String rootPath = "C:\\";

		
		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\JAnalyzer\\src\\", rootPath + "ZxcTools\\EclipseSource\\org\\", rootPath + "ZxcTemp\\src\\",
							rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\RDExample.java", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
							rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
							rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
							rootPath + "ZxcTools\\ArgoUml\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
							rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};
		
		String path = paths[4];
		String resultrd = rootPath + "ZxcWork\\ProgramAnalysis\\data\\resultrd.txt";
		String resultcrd = rootPath + "ZxcWork\\ProgramAnalysis\\data\\resultcrd.txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter outputrd = new PrintWriter(System.out);
		PrintWriter outputcrd = new PrintWriter(System.out);
		try {
			outputrd = new PrintWriter(new FileOutputStream(resultrd));
			outputcrd = new PrintWriter(new FileOutputStream(resultcrd));
		} catch (Exception exc) {
			exc.printStackTrace();
			System.exit(-1);
		}

		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			writer = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(writer);
			Debug.setScreenOn();
		} catch (Exception exc) {
			exc.printStackTrace();
		}

//		testCreateAllMethodCFGWithReachDefinition(path, outputrd);
		testCreateAllMethodCFGWithReachConditionDefinition(path, outputcrd);
//		testCreateCFGWithReachDefinition(path, outputrd);
//		testCreateCFGWithReachConditionDefinition(path, outputcrd);
//		testCreateAllMethodCFGWithNodePredicate(path, outputrd);
//		testCreateCFGWithNodePredicate(path, output);
//		testCreateCFGWithDominateNode(path3, output);
//		testCreateCFGWithReachName(path, output);
//		testCreateCFG(path3, output);
		
//		testRootReachName(path, output);
		
		writer.close();
//		outputrd.close();
		outputcrd.close();
	}
	
	public static void testCreateAllMethodCFGWithReachDefinition(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();

		Debug.setStart();
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
//			if (!method.getFullQualifiedName().contains("DataLineFilterValueCondition.acceptColumnValue")) continue;
			if (!method.getFullQualifiedName().contains("example")) continue;
			Debug.time("Before method " + method.getFullQualifiedName());
			ControlFlowGraph cfg = ReachDefinitionAnalyzer.create(tableManager, method);
			Debug.time("\tAfter method " + method.getFullQualifiedName());
			if (cfg == null) continue;
			if (method.getFullQualifiedName().contains("example")) {
//			if (method.getFullQualifiedName().contains("DataLineFilterValueCondition.acceptColumnValue")) {
//			if (method.getFullQualifiedName().contains("test")) {
				output.println("ExecutionPointId\tDefinition");
				List<GraphNode> nodeList = cfg.getAllNodes();
				if (nodeList == null) continue;
				System.out.println("Before write execution point " + nodeList.size() + " nodes!");
				for (GraphNode graphNode : nodeList) {
					if (graphNode instanceof ExecutionPoint) {
						ExecutionPoint node = (ExecutionPoint)graphNode;
						ReachDefinitionRecorder recorder = (ReachDefinitionRecorder)node.getFlowInfoRecorder();
						List<DefinitionRecorder> definitionList = recorder.getReachingDefinitionList();
						for (DefinitionRecorder conditionDefinitionRecorder : definitionList) {
							output.println("(" + node.getId() + ")\t" + conditionDefinitionRecorder);
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
		
		Debug.time("After Create " + methodList.size() + " CFGs.....");
		output.println();
	}
	
	
	public static void testCreateAllMethodCFGWithReachConditionDefinition(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();

		output.println("File\tLocation\tMethod\tExecutionPointId\tConditionDefinition\tNullability");
		
		Debug.setStart();
//		Debug.disable();
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
			SourceCodeLocation location = method.getLocation();
			String fileName = location.getFileUnitName();
			String lineCol = "(" + location.getLineNumber() + ", " + location.getColumn() + ")";
			
//			if (!method.getFullQualifiedName().contains("DataLineFilterValueCondition.acceptColumnValue")) continue;
			if (!method.getFullQualifiedName().contains("ValuedNode.getDescription")) continue;
			Debug.time("Before method " + method.getFullQualifiedName());
//			Debug.disable();
//			Debug.setScreenOff();
			ControlFlowGraph cfg = ReachConditionDefinitionAnalyzer.create(tableManager, method);
//			Debug.enable();
//			Debug.setScreenOn();
			Debug.time("\tAfter method " + method.getFullQualifiedName());
			if (cfg == null) continue;

//			if (method.getFullQualifiedName().contains("example")) {
//			if (method.getFullQualifiedName().contains("DataLineFilterValueCondition.acceptColumnValue")) {
			if (method.getFullQualifiedName().contains("ValuedNode.getDescription")) {
				List<GraphNode> nodeList = cfg.getAllNodes();
				if (nodeList == null) continue;
//				System.out.println("Before write execution point " + nodeList.size() + " nodes!");
				for (GraphNode graphNode : nodeList) {
					if (graphNode instanceof ExecutionPoint) {
						ExecutionPoint node = (ExecutionPoint)graphNode;
						ReachConditionDefinitionRecorder recorder = (ReachConditionDefinitionRecorder)node.getFlowInfoRecorder();
						List<ConditionDefinitionRecorder> conditionDefinitionList = recorder.getReachingConditionDefinitionList();
						for (ConditionDefinitionRecorder conditionDefinitionRecorder : conditionDefinitionList) {
//							Debug.println("Node: " + graphNode.getId() + ", write current recorder: " + conditionDefinitionRecorder);
							if (conditionDefinitionRecorder.isPrimitiveDefinition()) {
								output.println(fileName + "\t" + lineCol + "\t" + method.getSimpleName() + "\t(" + node.getId() + ")\t" + conditionDefinitionRecorder + "\t~~");
							} else {
								output.println(fileName + "\t" + lineCol + "\t" + method.getSimpleName() + "\t(" + node.getId() + ")\t" + conditionDefinitionRecorder + "\t" + conditionDefinitionRecorder.getNullability());
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
		Debug.enable();
		Debug.time("After Create " + methodList.size() + " CFGs.....");
//		output.println();
	}
	
	public static void testCreateCFGWithReachConditionDefinition(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();

		MethodDefinition maxMethod = null;
		int maxLineNumber = 0;
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
			if (maxMethod == null) {
				maxMethod = method;
				maxLineNumber = maxMethod.getEndLocation().getLineNumber() - maxMethod.getLocation().getLineNumber();
			} else {
				int currentLineNumber = method.getEndLocation().getLineNumber() - method.getLocation().getLineNumber();
				if (currentLineNumber > maxLineNumber) {
					maxMethod = method;
					maxLineNumber = currentLineNumber; 
				}
			}
		}
		if (maxMethod == null) return;
		
		Debug.setStart("Begin creating CFG for method " + maxMethod.getUniqueId() + ", " + maxLineNumber + " lines...");
		output.println("ExecutionPointId\tConditionReachDefinition");
		MethodDefinition method = maxMethod;
		
		ControlFlowGraph cfg = ReachConditionDefinitionAnalyzer.create(tableManager, method);
		if (cfg == null) return;
		
		List<GraphNode> nodeList = cfg.getAllNodes();
		System.out.println("Before write execution point " + nodeList.size() + " nodes!");
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				ReachConditionDefinitionRecorder recorder = (ReachConditionDefinitionRecorder)node.getFlowInfoRecorder();
				List<ConditionDefinitionRecorder> conditionDefinitionList = recorder.getReachingConditionDefinitionList();
				for (ConditionDefinitionRecorder conditionDefinitionRecorder : conditionDefinitionList) {
					output.println("(" + node.getId() + ")\t" + conditionDefinitionRecorder);
				}
			}
		}
		
		output.println();
		output.println();
		try {
			cfg.simplyWriteToDotFile(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Debug.time("After Create " + methodList.size() + " CFGs.....");
		output.println();
	}
	
	public static void testCreateCFGWithReachDefinition(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();

		MethodDefinition maxMethod = null;
		int maxLineNumber = 0;
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
			if (maxMethod == null) {
				maxMethod = method;
				maxLineNumber = maxMethod.getEndLocation().getLineNumber() - maxMethod.getLocation().getLineNumber();
			} else {
				int currentLineNumber = method.getEndLocation().getLineNumber() - method.getLocation().getLineNumber();
				if (currentLineNumber > maxLineNumber) {
					maxMethod = method;
					maxLineNumber = currentLineNumber; 
				}
			}
		}
		if (maxMethod == null) return;
		
		Debug.setStart("Begin creating CFG for method " + maxMethod.getUniqueId() + ", " + maxLineNumber + " lines...");
		output.println("ExecutionPointId\tReachDefinition");
		MethodDefinition method = maxMethod;
		
		ControlFlowGraph cfg = ReachDefinitionAnalyzer.create(tableManager, method);
		if (cfg == null) return;
		
		List<GraphNode> nodeList = cfg.getAllNodes();
		System.out.println("Before write execution point " + nodeList.size() + " nodes!");
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				IReachDefinitionRecorder recorder = (IReachDefinitionRecorder)node.getFlowInfoRecorder();
				List<DefinitionRecorder> definitionList = recorder.getReachingDefinitionList();
				for (DefinitionRecorder definitionRecorder : definitionList) {
					output.println("(" + node.getId() + ")\t" + definitionRecorder);
				}
			}
		}
		
		output.println();
		output.println();
		try {
			cfg.simplyWriteToDotFile(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Debug.time("After Create " + methodList.size() + " CFGs.....");
		output.println();
	}

	public static void testCreateAllMethodCFGWithNodePredicate(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();

		Debug.setStart();
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
//			if (!method.getFullQualifiedName().contains("testTypeStructManager")) continue;
//			if (!method.getFullQualifiedName().contains("example")) continue;
			Debug.time("Before method " + method.getFullQualifiedName());
			ControlFlowGraph cfg = NodePredicateChainAnalyzer.create(tableManager, method);
			Debug.time("\tAfter method " + method.getFullQualifiedName());
			if (cfg == null) continue;
//			if (method.getFullQualifiedName().contains("example")) {
			if (method.getFullQualifiedName().contains("test")) {
				output.println("ExecutionPointId\tPredicateChain");
				List<GraphNode> nodeList = cfg.getAllNodes();
				if (nodeList == null) continue;
				System.out.println("Before write execution point " + nodeList.size() + " nodes!");
				for (GraphNode graphNode : nodeList) {
					if (graphNode instanceof ExecutionPoint) {
						ExecutionPoint node = (ExecutionPoint)graphNode;
						NodePredicateChainRecorder recorder = (NodePredicateChainRecorder)node.getFlowInfoRecorder();
						output.println("(" + graphNode.getId() + ")\t" + recorder.getPredicateChain());
					} else {
						output.println("(" + graphNode.getId() + ")\t");
						System.out.println("Found none execution point!");
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
		
		Debug.time("After Create " + methodList.size() + " CFGs.....");
		output.println();
	}
	
	public static void testCreateCFGWithNodePredicate(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();

		MethodDefinition maxMethod = null;
		int maxLineNumber = 0;
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
			if (maxMethod == null) {
				maxMethod = method;
				maxLineNumber = maxMethod.getEndLocation().getLineNumber() - maxMethod.getLocation().getLineNumber();
			} else {
				int currentLineNumber = method.getEndLocation().getLineNumber() - method.getLocation().getLineNumber();
				if (currentLineNumber > maxLineNumber) {
					maxMethod = method;
					maxLineNumber = currentLineNumber; 
				}
			}
		}
		if (maxMethod == null) return;
		
		Debug.setStart("Begin creating CFG for method " + maxMethod.getUniqueId() + ", " + maxLineNumber + " lines...");
		output.println("ExecutionPointId\tPredicateChain");
		MethodDefinition method = maxMethod;
		
		ControlFlowGraph cfg = NodePredicateChainAnalyzer.create(tableManager, method);
		if (cfg == null) return;
		
		List<GraphNode> nodeList = cfg.getAllNodes();
		System.out.println("Before write execution point " + nodeList.size() + " nodes!");
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				NodePredicateChainRecorder recorder = (NodePredicateChainRecorder)node.getFlowInfoRecorder();
				output.println("(" + graphNode.getId() + ")\t" + recorder.getPredicateChain());
			} else {
				output.println("(" + graphNode.getId() + ")\t");
				System.out.println("Found none execution point!");
			}
		}
		output.println();
		try {
			cfg.simplyWriteToDotFile(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Debug.time("After Create " + methodList.size() + " CFGs.....");
		output.println();
	}
	

	public static void testCreateCFGWithDominateNode(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();

		MethodDefinition maxMethod = null;
		int maxLineNumber = 0;
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
			if (!method.getFullQualifiedName().contains("get")) continue;
			if (maxMethod == null) {
				maxMethod = method;
				maxLineNumber = maxMethod.getEndLocation().getLineNumber() - maxMethod.getLocation().getLineNumber();
			} else {
				int currentLineNumber = method.getEndLocation().getLineNumber() - method.getLocation().getLineNumber();
				if (currentLineNumber > maxLineNumber) {
					maxMethod = method;
					maxLineNumber = currentLineNumber; 
				}
			}
		}
		
		if (maxMethod == null) return;
		
		Debug.flush();
		Debug.setStart("Begin creating CFG for method " + maxMethod.getUniqueId() + ", " + maxLineNumber + " lines...");
		output.println("CurrentNodeId\tCurrentNodeLabel\tDominateNodeId\tDominateNodeLabel");
		MethodDefinition method = maxMethod;
		
		ControlFlowGraph cfg = DominateNodeAnalyzer.create(tableManager, method);
		if (cfg == null) return;
		
		DominateNodeAnalyzer.printDominateNodeInformation(cfg, output);
		
		output.println();
		output.println();
		try {
			cfg.simplyWriteToDotFile(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Debug.time("After Create " + methodList.size() + " CFGs.....");
		output.println();
	}
	

	public static void testCreateCFG(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();
		
		Debug.flush();
		int counter = 0;
		Debug.setStart("Begin creating CFG and analysis dominate node...");
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
//			if (!method.getSimpleName().equals("enable")) continue;
			
//			System.out.println("Method " + method.getFullQualifiedName());
			ControlFlowGraph cfg1 = ReachConditionDefinitionAnalyzer.create(tableManager, method);
//			ControlFlowGraph cfg2 = ReachNameAnalyzer.create(tableManager, method);
//			if (compareTwoCFGs(cfg1, cfg2)) {
//				Debug.println("Two CFGs are the same for method " + method.getFullQualifiedName());
//			} else {
//				Debug.println("\tTwo CFGs are different for method " + method.getFullQualifiedName());
//				counter++;
//			}
		}
		Debug.time("After Create " + methodList.size() + " CFGs....., and there are " + counter + " different CFGs....");
		output.println();
		
		
		Debug.setStart("Begin creating CFG and analysis reache name...");
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
//			if (!method.getSimpleName().equals("compareMethodDefinitionSignature")) continue;

//			System.out.println("Method " + method.getSimpleName());
			ControlFlowGraph cfg = ReachDefinitionAnalyzer.create(tableManager, method);
//			try {
//				cfg.simplyWriteToDotFile(output);
//			} catch (Exception exc) {
//				exc.printStackTrace();
//			}
		}
		Debug.time("After Create " + methodList.size() + " CFGs.....");
	}
	
}
