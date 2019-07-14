package analyzer.cfg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import analyzer.cfg.reachDefinition.DefinitionRecorder;
import analyzer.cfg.reachDefinition.ReachConditionDefinitionAnalyzer;
import analyzer.cfg.reachDefinition.ReachConditionDefinitionRecorder;
import analyzer.cfg.reachDefinition.ReachDefinitionAnalyzer;
import analyzer.cfg.reachDefinition.ReachDefinitionRecorder;
import analyzer.storageModel.IAbstractStorageModel;
import analyzer.storageModel.SimpleStorageModel;
import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.creator.CFGCreator;
import nameTable.NameTableManager;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.filter.NameDefinitionNameFilter;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameReference.NameReference;
import nameTable.visitor.NameDefinitionVisitor;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê4ÔÂ1ÈÕ
 * @version 1.0
 *
 */
public class TestDataFlowAnalysis {

	public static void main(String[] args) {
		String rootPath = "C:\\";
//		String path = rootPath + "QualitasPacking\\recent\\azureus\\azureus-4.8.1.2\\";
//		String path = rootPath + "ZxcWork\\JAnalyzer\\src\\";
		String path = rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\RDExample.java";
		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = new PrintWriter(System.out);

		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			writer = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(writer);
			Debug.setScreenOn();
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		try {
			output = new PrintWriter(new FileOutputStream(result));
//			testCreateCFG(path, output);
			
			testCreateCFGWithReachDefinition(path, output);
			
//			collectReachDefinition(path);
		} catch (Exception exc) {
			exc.printStackTrace();
			System.exit(-1);
		}
		
		output.close();
		writer.close();
	}
	
	public static void testCreateCFG(String path, PrintWriter output) throws IOException {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(
				new NameDefinitionKindFilter(new NameDefinitionNameFilter("main"), NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();
		
		MethodDefinition method = (MethodDefinition)methodList.get(0);
		
		Debug.setStart("Begin creating CFG for method " + method.getSimpleName() + " ...");
		ControlFlowGraph cfg = CFGCreator.create(tableManager, method);
		cfg.simplyWriteToDotFile(output);
		Debug.time("After Creating CFG ...");
	}

	public static void collectReachDefinition(String path) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();
		
		Debug.setStart("Begin creating CFG...");
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
			ReachDefinitionAnalyzer.create(tableManager, method);
		}
		Debug.time("After Create " + methodList.size() + " CFGs.....");
	}
	
	public static void testCreateCFGWithReachDefinition(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(
				new NameDefinitionKindFilter(new NameDefinitionNameFilter("main"), 
						NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();
		
		MethodDefinition method = (MethodDefinition)methodList.get(0);
		
		Debug.setStart("Begin creating CFG for method " + method.getUniqueId() + "...");
		ControlFlowGraph cfg = ReachConditionDefinitionAnalyzer.create(tableManager, method);
		Debug.time("After Create " + methodList.size() + " CFGs.....");
		if (cfg == null) return;
		
		output.println("ExecutionPointId\tStatement\tReaching Definitions\tReaching In Definitions");
		List<GraphNode> nodeList = cfg.getAllNodes();
		System.out.println("Before write execution point " + nodeList.size() + " nodes!");
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				if (node.isVirtual()) continue;
				String statement = node.getAstNode().toString();
				
				ReachConditionDefinitionRecorder recorder = (ReachConditionDefinitionRecorder)node.getFlowInfoRecorder();
				List<DefinitionRecorder> outList = recorder.getReachingDefinitionList();
				List<DefinitionRecorder> inList = recorder.getInReachingDefinitionList();

				output.println("\\hline (" + graphNode.getId() + ")\t~&~\\tablecell{0.23\\textwidth}{\\pname{" + statement + "}}\t~&~\\tablecell{0.23\\textwidth}{" + getDefinitionListString(outList) + "}\t~&~\\tablecell{0.23\\textwidth}{" + getDefinitionListString(inList) + "}  \\\\");
			} else {
				output.println(graphNode.getId() + "\t~~\t~~\t~~\t~~");
				System.out.println("Found none execution point with defined name node!");
			}
		}
		output.println();
	}
	
	public static String getDefinitionListString(List<DefinitionRecorder> definitionList) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		
		for (DefinitionRecorder definition : definitionList) {
			IAbstractStorageModel leftStorage = definition.getLeftStorage();
			NameReference value = definition.getValueExpression();
			String name = leftStorage.getExpression();
			
			if (name.equals("x") || name.equals("y") || name.equals("z")) {
				if (!first) builder.append(" \\\\");
				first = false;
				builder.append("\\pname{" + leftStorage.getExpression());
				if (value != null) {
					builder.append(" = " + value.toSimpleString() + "} ");
				} else builder.append("} ");
			}
		}
		
		return builder.toString();
	}

}
