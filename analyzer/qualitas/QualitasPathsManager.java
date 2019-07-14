package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import measurement.measure.MeasureObjectKind;
import util.Debug;

/**
 * A class to find systems and versions in Qualitas Corpus, and to manager all kinds of paths for 
 * analyze systems in Qualitas. This class holds the rules for storing the data of analyzing results.
 * 
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ14ÈÕ
 * @version 1.0
 */
public class QualitasPathsManager {
	public static final String pathSeparator = "\\";
	public static final String defaultOriginalPath = "C:\\Qualitas\\QualitasCorpus-20130901r\\Systems\\";
	public static final String defaultRootPath = "C:\\QualitasPacking\\recent\\";
	public static final String defaultResultPath = "C:\\QualitasPacking\\result\\recent\\";
	public static final String defaultDebugPath = "C:\\ZxcWork\\ProgramAnalysis\\data\\";
	
	public static String getSystemPath(String systemName, String version, String rootPath) {
		String mainDirectory = getSystemVersionMainDirectory(systemName, version);
		if (mainDirectory.equals("")) {
			return rootPath + systemName + pathSeparator + version + pathSeparator;
		} else return rootPath + systemName + pathSeparator + version + pathSeparator + mainDirectory + pathSeparator;
	}
	
	public static String getSystemPath(String systemName, String version) {
		return getSystemPath(systemName, version, defaultRootPath);
	}
	
	public static String getSystemStartPath(String systemName, String rootPath) {
		return rootPath + systemName + pathSeparator;
	}

	public static String getSystemStartPath(String systemName) {
		return getSystemStartPath(systemName, defaultRootPath);
	}
	
	public static String getDebugFile() {
		return defaultDebugPath + "debug.txt";
	}

	public static String getTestingResultFile() {
		return defaultDebugPath + "result.txt";
	}

	/**
	 * Given a root directory (rootDirectory), return the sub-directories as the system names. We assume 
	 * the sub-directories in this root directory are the names of the systems which we want to analyzed.
	 */
	public static String[] getSystemNames(String rootDirectory) {
		ArrayList<String> result = new ArrayList<String>();
		
		File root = new File(rootDirectory);
		if (!root.isDirectory()) return null;
		
		File[] files = root.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) result.add(files[i].getName());
		}
	
		String[] resultArray = new String[result.size()];
		for (int i = 0; i < result.size(); i++) resultArray[i] = result.get(i);
		return resultArray;
	}
	
	public static String[] getSystemNames() {
		return getSystemNames(defaultRootPath);
	}
	
	public static String[] getSystemNamesInOriginalPath(String originalPath) {
		return getSystemNames(originalPath);
	}

	public static String[] getSystemNamesInOriginalPath() {
		return getSystemNames(defaultOriginalPath);
	}
	
	/**
	 * Given a root directory (rootDirectory) and a system name (systemName), return the sub-directory of 
	 * systemName as the versions of the system. We assume the sub-directories in the root directory are the
	 * name of the systems and the sub-directories of a system (i.e. a sub-directory of rootDirectory) are the 
	 * versions of this system. 
	 */
	public static String[] getSystemVersions(String systemName, String rootDirectory) {
		ArrayList<String> result = new ArrayList<String>();
		String startPath = getSystemStartPath(systemName, rootDirectory);
		
		// Note that, in this software, we always use pathSeparator ("\\") to end a directory!
		File system = new File(startPath);
		if (!system.isDirectory()) return null;
		
		File[] files = system.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) result.add(files[i].getName());
		}
	
		String[] resultArray = new String[result.size()];
		for (int i = 0; i < result.size(); i++) resultArray[i] = result.get(i);
		return resultArray;
	}
	
	public static String[] getSystemVersions(String systemName) {
		return getSystemVersions(systemName, defaultRootPath);
	}

	public static String[] getSystemVersionsInOriginalPath(String systemName, String originalPath) {
		return getSystemVersions(systemName, originalPath);
	}

	public static String[] getSystemVersionsInOriginalPath(String systemName) {
		return getSystemVersions(systemName, defaultOriginalPath);
	}
	
	/**
	 * Get the file which describes the basic properties of the system version 
	 */
	public static String getSystemQualitasPropertyFile(String systemName, String version, String originalPath) {
		return originalPath + systemName + pathSeparator + version + pathSeparator + ".properties";
	}
	
	/**
	 * Get the .properties file which describes the basic properties of the system version 
	 */
	public static String getSystemQualitasPropertyFile(String systemName, String version) {
		return defaultOriginalPath + systemName + pathSeparator + version + pathSeparator + ".properties";
	}

	/**
	 * Get the contents.csv file which describes the contents of classes of the system version 
	 */
	public static String getSystemQualitasMetadataFile(String systemName, String version, String originalPath) {
		return originalPath + systemName + pathSeparator + version + pathSeparator + "metadata" + pathSeparator + "contents.csv";
	}
	
	/**
	 * Get the contents.csv file which describes the contents of classes of the system version 
	 */
	public static String getSystemQualitasMetadataFile(String systemName, String version) {
		return defaultOriginalPath + systemName + pathSeparator + version + pathSeparator + "metadata" + pathSeparator + "contents.csv";
	}

	
	public static String getMeasureResultPath(MeasureObjectKind kind, String systemName, String resultPath, boolean groupBySystem) {
		String result = null;
		if (groupBySystem) result = resultPath + systemName + pathSeparator;
		else result = resultPath;

		if (kind == MeasureObjectKind.MOK_SYSTEM) {
			if (!groupBySystem) result = result + "system" + pathSeparator; 
		} else if (kind == MeasureObjectKind.MOK_PACKAGE) {
			result = result + "package" + pathSeparator; 
		} else if (kind == MeasureObjectKind.MOK_UNIT) {
			result = result + "unit" + pathSeparator; 
		} else if (kind == MeasureObjectKind.MOK_CLASS) {
			result = result + "class" + pathSeparator; 
		} else if (kind == MeasureObjectKind.MOK_METHOD) {
			result = result + "method" + pathSeparator; 
		} 
		
		File path = new File(result);
		if (!path.exists()) path.mkdirs();
		return result;
	}

	public static String getMeasureResultFile(MeasureObjectKind kind, String systemName, String version, String resultPath, boolean groupBySystem) {
		return getMeasureResultPath(kind, systemName, resultPath, groupBySystem) + version + ".txt";
	}

	public static String getMeasureResultFile(MeasureObjectKind kind, String systemName, String version, boolean groupBySystem) {
		return getMeasureResultFile(kind, systemName, version, defaultResultPath, groupBySystem);
	}
	
	public static String getSystemMeasureResultFile(String systemName, String resultPath, boolean groupBySystem) {
		return getMeasureResultPath(MeasureObjectKind.MOK_SYSTEM, systemName, resultPath, groupBySystem) + systemName + ".txt";
	}

	public static String getSystemMeasureResultFile(String systemName, boolean groupBySystem) {
		return getSystemMeasureResultFile(systemName, defaultResultPath, groupBySystem);
	}

	public static String getObjectExpressionResultPath(String systemName, String resultPath) {
		String result = resultPath + "objexp" + pathSeparator;

		File path = new File(result);
		if (!path.exists()) path.mkdirs();
		return result;
	}

	public static String getObjectExpressionResultFile(String systemName, String version, String resultPath) {
		return getObjectExpressionResultPath(systemName, resultPath) + version + ".txt";
	}

	public static String getObjectExpressionResultFile(String systemName, String version) {
		return getObjectExpressionResultPath(systemName, defaultResultPath) + version + ".txt";
	}

	public static String getObjectExpressionDebugFile(String systemName, String version, String resultPath) {
		return getObjectExpressionResultPath(systemName, resultPath) + version + "-info.txt";
	}

	public static String getObjectExpressionDebugFile(String systemName, String version) {
		return getObjectExpressionResultPath(systemName, defaultResultPath) + version + "-info.txt";
	}
	
	public static String getObjectExpressionGroupFile(String systemName, String version, String resultPath) {
		return getObjectExpressionResultPath(systemName, resultPath) + version + "-group.txt";
	}

	public static String getObjectExpressionGroupFile(String systemName, String version) {
		return getObjectExpressionResultPath(systemName, defaultResultPath) + version + "-group.txt";
	}

	public static String getNullCheckAnalyzeResultPath(String systemName, String resultPath) {
		String result = resultPath + "nullcheck" + pathSeparator;

		File path = new File(result);
		if (!path.exists()) path.mkdirs();
		return result;
	}
	
	public static String getReturnValueAnalyzeResultFile(String systemName, String version) {
		return getNullCheckAnalyzeResultPath(systemName, defaultResultPath) + version + "-return-value.txt";
	}

	public static String getReturnValueAnalyzeSummaryFile(String systemName, String version) {
		return getNullCheckAnalyzeResultPath(systemName, defaultResultPath) + version + "-return-summary.txt";
	}

	public static String getCallAnalyzeResultTableFile(String systemName, String version) {
		return getNullCheckAnalyzeResultPath(systemName, defaultResultPath) + version + "-call-table.txt";
	}

	public static String getCallAnalyzeResultTextFile(String systemName, String version) {
		return getNullCheckAnalyzeResultPath(systemName, defaultResultPath) + version + "-call-text.txt";
	}
	
	public static String getCallAnalyzeCheckFile(String systemName, String version) {
		return getNullCheckAnalyzeResultPath(systemName, defaultResultPath) + version + "-call-check.txt";
	}

	/**
	 * The source codes of some version of some system are under a sub-directory of that version. In other words, 
	 * the sub-directory of the version of the system does not only include the source codes which we want to analyzed, 
	 * but also include some examples, test cases and other java files which are not essentially contribute to the 
	 * systems.
	 * 
	 * We return the main directory according to investigate the systems and versions individually.     
	 */
	public static String getSystemVersionMainDirectory(String systemName, String version) {
//		if (systemName.equals("ant")) return "main";
//		if (systemName.equals("antlr")) {
//			if (version.contains("antlr-2.")) return "antlr";
//			else if (version.contains("antlr-3.1.3") || version.contains("antlr-3.2") || 
//					version.contains("antlr-3.3") || version.contains("antlr-3.4") || 
//					version.contains("antlr-3.5")) return "tool\\src\\main";
//			else if (version.contains("antlr-4.0")) return "tool\\src";
//			else return "src";
//		}
		
		return "";
	}
	
	public static void main(String[] args) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String result = QualitasPathsManager.defaultDebugPath + "QualitsVersions.txt" ;
		
		PrintWriter writer = new PrintWriter(System.out);
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		StringBuffer systemNameString = new StringBuffer();
		StringBuffer systemVersionString = new StringBuffer();
		
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			
			for (int index = 0; index < versions.length; index++) {
				systemNameString.append("\"" + systemName);
				systemVersionString.append("\"" + versions[index]);
				if (i == (systemNames.length-1) && index == (versions.length-1)) {
					systemNameString.append("\"");
					systemVersionString.append("\"");
				} else {
					systemNameString.append("\", ");
					systemVersionString.append("\", ");
				}
			}
		}
		
		writer.println(systemNameString);
		writer.println(systemVersionString);
		writer.close();
	}
	
	public static void writeSingleSystemVersions() {
		String systemName = "azureus";
		String systemPath = QualitasPathsManager.getSystemStartPath(systemName);
		
		String result = systemPath + "versionNames.txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = null;
		
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		try {
			String info = QualitasPathsManager.getDebugFile();
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		String[] versions = QualitasPathsManager.getSystemVersions(systemName);
		
		for (int index = 0; index < versions.length; index++) {
			writer.print("\"" + versions[index]);
			if (index == (versions.length-1)) writer.print("\"");
			else writer.print("\", ");
		}

		writer.println();
		writer.close();
	}
}


