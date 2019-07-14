package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import analyzer.method.ReturnValueAnalyzer;
import nameTable.NameTableManager;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê6ÔÂ28ÈÕ
 * @version 1.0
 *
 */
public class QualitasReturnValueAnalyzer {

	public static void main(String[] args) {
		analyzeAllMethods();
	}

	public static void analyzeAllMethods() {
		PrintWriter writer = null;
		PrintWriter output = null;
		PrintWriter summaryWriter = null;
		
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
					
					String result = QualitasPathsManager.getReturnValueAnalyzeResultFile(systems[i], versions[j]);
					writer = new PrintWriter(new FileOutputStream(new File(result)));
					String summary = QualitasPathsManager.getReturnValueAnalyzeSummaryFile(systems[i], versions[j]);
					summaryWriter = new PrintWriter(new FileOutputStream(new File(summary)));

					Debug.setStart("Begin collection....");
					NameTableManager manager = NameTableManager.createNameTableManager(path);
					ReturnValueAnalyzer analyzer = new ReturnValueAnalyzer(manager);
					analyzer.analyze();
					analyzer.printDetailsAsTextFile(writer);
					analyzer.printSummary(summaryWriter);
					Debug.time("After collection...");
			
					summaryWriter.close();
					writer.close();
				}
			}
			output.close();
		} catch (Exception exc) {
			if (writer != null) writer.close();
			if (output != null) output.close();
			if (summaryWriter != null) summaryWriter.close();
			exc.printStackTrace();
		}
	}
}
