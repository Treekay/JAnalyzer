package analyzer.method;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import nameTable.NameTableManager;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.filter.NameDefinitionNameFilter;
import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.visitor.NameDefinitionVisitor;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ25ÈÕ
 * @version 1.0
 *
 */
public class TestReturnValueAnalyzer {


	public static void main(String[] args) {
		analyzeAllMethods();
//		getSpecificMethodReturnValueInformation();
	}
	
	public static void analyzeAllMethods() {
		String rootPath = "C:\\";

		
		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\JAnalyzer\\src\\", rootPath + "ZxcTools\\EclipseSource\\org\\", rootPath + "ZxcTemp\\src\\",
							rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\RDExample.java", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
							rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
							rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
							rootPath + "ZxcTools\\ArgoUML-0.34-src\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
							rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};
		
		String path = paths[2];
		String system = "JANew";
		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = new PrintWriter(System.out);
		
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
			Debug.setScreenOn();

			String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\" + system + "ReturnValue.txt";
			String summary = rootPath + "ZxcWork\\ProgramAnalysis\\data\\" + system + "ReturnValueSummary.txt";
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		
			PrintWriter summaryWriter = new PrintWriter(new FileOutputStream(new File(summary)));
			Debug.setStart("Begin collection....");
			NameTableManager manager = NameTableManager.createNameTableManager(path);
			ReturnValueAnalyzer analyzer = new ReturnValueAnalyzer(manager);
			analyzer.analyze();
			analyzer.printDetailsAsTextFile(writer);
			analyzer.printSummary(summaryWriter);
			Debug.time("After collection...");
	
			summaryWriter.close();
			writer.close();
			output.close();
		} catch (Exception exc) {
			writer.close();
			output.close();
			exc.printStackTrace();
		}
	}

	public static void getSpecificMethodReturnValueInformation() {
		String rootPath = "C:\\";

		
		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\JAnalyzer\\src\\", rootPath + "ZxcTools\\EclipseSource\\org\\", rootPath + "ZxcTemp\\src\\",
							rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\HelloWorld.java", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
							rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
							rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
							rootPath + "ZxcTools\\ArgoUML-0.34-src\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
							rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};
		
		String path = paths[4];
		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = new PrintWriter(System.out);
		
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
			Debug.setScreenOn();

			String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		
			Debug.setStart("Begin collection....");
			NameTableManager manager = NameTableManager.createNameTableManager(path);
			String methodName = "ValuedNode.getDescription";
			NameTableFilter filter = new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD);
			NameDefinitionVisitor visitor = new NameDefinitionVisitor(filter);
			manager.accept(visitor);
			List<NameDefinition> resultList = visitor.getResult();
			if (resultList.size() > 0) {
				ReturnValueAnalyzer analyzer = new ReturnValueAnalyzer(manager);
				for (NameDefinition definition : resultList) {
					if (!definition.getFullQualifiedName().contains(methodName)) continue;
					MethodDefinition method = (MethodDefinition)definition;
					System.out.println("Find method " + method.getFullQualifiedName());
					String returnValueMessage = analyzer.getReturnValueInformation(method);
					System.out.println(returnValueMessage);
				}
				analyzer.printDetailsAsTextFile(writer);
			} else System.out.println("Can not find method " + methodName);
			Debug.time("After collection...");
	
			writer.close();
			output.close();
		} catch (Exception exc) {
			writer.close();
			output.close();
			exc.printStackTrace();
		}
	}
}
