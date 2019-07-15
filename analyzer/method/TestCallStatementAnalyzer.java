package analyzer.method;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import analyzer.dataTable.DataTableManager;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018年7月27日
 * @version 1.0
 *
 */
public class TestCallStatementAnalyzer {

	public static void main(String[] args) {
		collectAndCheck();
	}
	
	public static void collectAndCheck() {
		String rootPath = "C:\\";

		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\JAnalyzer\\src\\", rootPath + "ZxcTools\\EclipseSource\\org\\", rootPath + "ZxcTemp\\JAnalyzer\\src\\",
							rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\CNExample.java", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
							rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
							rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
							rootPath + "ZxcTools\\ArgoUml\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
							rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};
		
		String path = paths[2];
		String system = "JANew";
		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\" + system + "CallValueTable.txt";
		String callResult = rootPath + "ZxcWork\\ProgramAnalysis\\data\\" + system + "CallValue.txt";
		String checkResult = rootPath + "ZxcWork\\ProgramAnalysis\\data\\" + system + "CheckResult.txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = new PrintWriter(System.out);
		
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
			Debug.setScreenOn();
		} catch (Exception exc) {
			exc.printStackTrace();
			writer.close();
			output.close();
			return;
		}
		
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
			Debug.setStart("Begin collection....");
			CallStatementAnalyzer.collectAllMethodCallStatementByScanningMethods(path, writer);
			Debug.time("After collection...");
			writer.close();

			writer = new PrintWriter(new FileOutputStream(new File(checkResult)));
			Debug.setStart("Begin check....");
			DataTableManager manager = new DataTableManager("result");
			manager.read(result, true);
			manager.writeWithSortedStringColumn(result, "CalleeLocation", false);
			CallStatementAnalyzer.checkReturnValueNullCheckConsistence(result, writer);
			writer.close();
			Debug.time("After check...");
			
			writer = new PrintWriter(new FileOutputStream(new File(callResult)));
			CallStatementAnalyzer.printReturnValueAsText(result, writer);
			writer.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		output.close();
	}
}
