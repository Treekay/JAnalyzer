package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import analyzer.dataTable.DataTableManager;
import analyzer.method.CallStatementAnalyzer;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê6ÔÂ28ÈÕ
 * @version 1.0
 *
 */
public class QualitasMethodCallAnalyzer {

	public static void main(String[] args) {
		collectAndCheck();
	}

	public static void collectAndCheck() {
		PrintWriter writer = null;
		PrintWriter output = null;
		
		try {
			String info = QualitasPathsManager.getDebugFile();
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
			Debug.setScreenOn();

			String[] systems = QualitasPathsManager.getSystemNames();
			for (int i = 0; i < systems.length; i++) {
				if (!systems[i].equals("ant")) continue;
				String[] versions = QualitasPathsManager.getSystemVersions(systems[i]);
				for (int j = 0; j < versions.length; j++) {
					String path = QualitasPathsManager.getSystemPath(systems[i], versions[j]);
					
					String result = QualitasPathsManager.getCallAnalyzeResultTableFile(systems[i], versions[j]);
					writer = new PrintWriter(new FileOutputStream(new File(result)));

					Debug.setStart("Begin collection....");
					CallStatementAnalyzer.collectAllMethodCallStatementByScanningMethods(path, writer);
					Debug.time("After collection...");
					writer.close();
					
					String checkResult = QualitasPathsManager.getCallAnalyzeCheckFile(systems[i], versions[j]);
					writer = new PrintWriter(new FileOutputStream(new File(checkResult)));
					Debug.setStart("Begin check....");
					DataTableManager manager = new DataTableManager("result");
					manager.read(result, true);
					manager.writeWithSortedStringColumn(result, "CalleeLocation", false);
					CallStatementAnalyzer.checkReturnValueNullCheckConsistence(result, writer);
					writer.close();
					Debug.time("After check...");
					
					String callResult = QualitasPathsManager.getCallAnalyzeResultTextFile(systems[i], versions[j]);
					writer = new PrintWriter(new FileOutputStream(new File(callResult)));
					CallStatementAnalyzer.printReturnValueAsText(result, writer);
					writer.close();
				}
			}
			output.close();
		} catch (Exception exc) {
			if (writer != null) writer.close();
			if (output != null) output.close();
			exc.printStackTrace();
		}
	}
}
