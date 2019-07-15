package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import analyzer.dataTable.DataTableManager;
import analyzer.objectExpression.ObjectExpressionCollector;
import analyzer.objectExpression.ObjectExpressionKind;
import analyzer.objectExpression.ObjectExpressionUsageKind;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2018年6月10日
 * @version 1.0
 *
 */
public class QualitasObjectExpressionCollector {
	public static void main(String[] args) {
		collectQualitasRecentSystemsObjectExpression(true);
		groupQualitasRecentSystemsObjectExpression();
		summaryMethodObjectExpression();
		objectExpressionKindStatistics();
		objectExpressionUsageStatistics();
		objectExpressionNullCheckStatistics();
		objectExpressionPolynomialStatistics();
		objectExpressionAliasStatistics();
		objectExpressionSizeStatistics();
	}


	public static void collectQualitasRecentSystemsObjectExpression(boolean recollect) {
		String[] systemNames = QualitasPathsManager.getSystemNames();

		Debug.setScreenOn();
		Debug.setStart("Begin....");
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
			
//			if (!systemName.equals("eclipse_SDK") && !systemName.equals("jre") && !systemName.equals("netbeans")) continue;
//			if (!systemName.equals("JAnalyzer")) continue;
			
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			for (int j = 0; j < versions.length; j++)
				collectQualitasSystemObjectExpression(systemName, versions[j], recollect);
		}
		Debug.time("End....");
	}
	
	public static void collectQualitasSystemObjectExpression(String systemName, String version, boolean recollect) {
		String result = QualitasPathsManager.getObjectExpressionResultFile(systemName, version);
		String info = QualitasPathsManager.getObjectExpressionDebugFile(systemName, version);

		File resultFile = new File(result);
		if (!recollect) {
			if (resultFile.exists()) return;
		}
		try {
			PrintWriter writer = new PrintWriter(System.out);
			writer = new PrintWriter(new FileOutputStream(resultFile));
			PrintWriter output = new PrintWriter(System.out);
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
			collectQualitasSystemObjectExpression(systemName, version, writer);
			writer.close();
			output.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	public static void collectQualitasSystemObjectExpression(String systemName, String version, PrintWriter writer) {
		String path = QualitasPathsManager.getSystemPath(systemName, version);
		Debug.setStart("Begin collection....");
		ObjectExpressionCollector collector = new ObjectExpressionCollector();
		collector.collectAllObjectReferenceExpressions(path, writer);
		Debug.time("After collection...");
		Debug.flush();
	}
	
	public static void groupQualitasRecentSystemsObjectExpression() {
		String[] systemNames = QualitasPathsManager.getSystemNames();

		Debug.setScreenOn();
		Debug.setStart("Begin....");
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
			
//			if (systemName.equals("eclipse_SDK") || systemName.equals("jre") || systemName.equals("netbeans")) continue;
//			if (!systemName.equals("JAnalyzer")) continue;
			
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			for (int j = 0; j < versions.length; j++)
				groupQualitasSystemObjectExpression(systemName, versions[j]);
		}
		Debug.time("End....");
	}
	
	public static void groupQualitasSystemObjectExpression(String systemName, String version) {
		String resultFile = QualitasPathsManager.getObjectExpressionResultFile(systemName, version);
		String groupFile = QualitasPathsManager.getObjectExpressionGroupFile(systemName, version);

		DataTableManager groupManager = new DataTableManager("group");
		// groupManager structure: "Kind\t...ObjectExpressionUsageKind...\tPolynomial\tNonPolynomial\tDereference\tNonDereference\tTotal"
		initializeGroupDataManager(groupManager);
		
		DataTableManager dataManager = new DataTableManager("data");
		int startLine = 1;
		int length = 1000000;

		try {
			boolean hasLine = true;
			while (hasLine == true) {
				hasLine = dataManager.read(resultFile, 0, startLine, startLine+length);
//				System.out.println("Read file " + resultFile + ", line " + dataManager.getLineNumber() + ", hasLine = " + hasLine);
				startLine = startLine + length;
				// dataManager structure: "File\tLocation\tExpression\tDereference\tPolynomial\tType\tKind\tUsage\tDefinition\tClass\tMethod"
				int lineNumber = dataManager.getLineNumber();
				int value = 0;
				for (int index = 0; index < lineNumber; index++) {
					String line[] = dataManager.getLineAsStringArray(index);
//					int lineNo = startLine + index;
//					System.out.println("Group line " + lineNo + "...");
					
					String kind = line[6];
					String usage = line[7];
					if (line[3].equalsIgnoreCase("true")) {
						// Dereference object expression!
						value = groupManager.getCellValueAsInt(kind, "Dereference");
						value++;
						groupManager.setCellValue(kind, "Dereference", value);
						value = groupManager.getCellValueAsInt("TOTAL", "Dereference");
						value++;
						groupManager.setCellValue("TOTAL", "Dereference", value);
					} else {
						// NonDereference object expression!
						value = groupManager.getCellValueAsInt(kind, "NonDereference");
						value++;
						groupManager.setCellValue(kind, "NonDereference", value);
						value = groupManager.getCellValueAsInt("TOTAL", "NonDereference");
						value++;
						groupManager.setCellValue("TOTAL", "NonDereference", value);
					}
					if (line[4].equalsIgnoreCase("true")) {
						// Polynomial object expression!
						value = groupManager.getCellValueAsInt(kind, "Polynomial");
						value++;
						groupManager.setCellValue(kind, "Polynomial", value);
						value = groupManager.getCellValueAsInt("TOTAL", "Polynomial");
						value++;
						groupManager.setCellValue("TOTAL", "Polynomial", value);
					} else {
						// NonPolynomial object expression!
						value = groupManager.getCellValueAsInt(kind, "NonPolynomial");
						value++;
						groupManager.setCellValue(kind, "NonPolynomial", value);
						value = groupManager.getCellValueAsInt("TOTAL", "NonPolynomial");
						value++;
						groupManager.setCellValue("TOTAL", "NonPolynomial", value);
					}
					value = groupManager.getCellValueAsInt(kind, "Total");
					value++;
					groupManager.setCellValue(kind, "Total", value);
					value = groupManager.getCellValueAsInt("TOTAL", "Total");
					value++;
					groupManager.setCellValue("TOTAL", "Total", value);
					
					value = groupManager.getCellValueAsInt(kind, usage);
					value++;
					groupManager.setCellValue(kind, usage, value);
					value = groupManager.getCellValueAsInt("TOTAL", usage);
					value++;
					groupManager.setCellValue("TOTAL", usage, value);
				}
			}
			System.out.println("Write group data to " + groupFile + ", line " + groupManager.getLineNumber());
			groupManager.write(groupFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void initializeGroupDataManager(DataTableManager manager) {
		String[] usageArray = ObjectExpressionCollector.getAllObjectExpressionsUsageKind();
		String[] columnNameArray = new String[usageArray.length + 6];
		columnNameArray[0] = "Kind";
		for (int i = 0; i < usageArray.length; i++) columnNameArray[i+1] = usageArray[i];
		int index = usageArray.length+1;
		columnNameArray[index] = "Polynomial";
		columnNameArray[index+1] = "NonPolynomial";
		columnNameArray[index+2] = "Dereference";
		columnNameArray[index+3] = "NonDereference";
		columnNameArray[index+4] = "Total";
		
		manager.setColumnNames(columnNameArray);
		manager.setKeyColumnIndex(0);
		
		String[] line = null;
		String[] kindArray = ObjectExpressionCollector.getAllObjectExpressionsKind();
		for (int i = 0; i < kindArray.length; i++) {
			if (ObjectExpressionKind.valueOf(kindArray[i]) == ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION) continue;
			line = new String[columnNameArray.length];
			for (int j = 1; j < line.length; j++) line[j] = "0";
			line[0] = kindArray[i];
			manager.appendLine(line);
		}
		line = new String[columnNameArray.length];
		for (int j = 1; j < line.length; j++) line[j] = "0";
		line[0] = "TOTAL";
		manager.appendLine(line);
	}
	
	public static void summaryMethodObjectExpression() {
		String result = QualitasPathsManager.defaultResultPath + "ExampleSelect.txt";

		DataTableManager summaryManager = new DataTableManager("summary");
		String[] usageArray = ObjectExpressionCollector.getAllObjectExpressionsUsageKind();
		String[] kindArray = ObjectExpressionCollector.getAllObjectExpressionsKind();
		String[] columnNameArray = new String[usageArray.length + kindArray.length + 9];
		columnNameArray[0] = "File";
		columnNameArray[1] = "Class";
		columnNameArray[2] = "Method";
		for (int i = 0; i < usageArray.length; i++) columnNameArray[i+3] = usageArray[i];
		for (int i = 0; i < kindArray.length; i++) columnNameArray[i+3+usageArray.length] = kindArray[i];
		columnNameArray[3+usageArray.length+kindArray.length] = "UsageTotal";
		columnNameArray[3+usageArray.length+kindArray.length+1] = "KindTotal";
		columnNameArray[3+usageArray.length+kindArray.length+2] = "Total";
		columnNameArray[3+usageArray.length+kindArray.length+3] = "Nodes";
		columnNameArray[3+usageArray.length+kindArray.length+4] = "System";
		columnNameArray[3+usageArray.length+kindArray.length+5] = "Version";
		
		summaryManager.setColumnNames(columnNameArray);

		String[] systemNames = QualitasPathsManager.getSystemNames();

		Debug.setScreenOn();
		Debug.setStart("Begin....");
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
			
			if (systemName.equals("eclipse_SDK") || systemName.equals("jre") || systemName.equals("netbeans") || systemName.equals("JAnalyzer")) continue;
			
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			for (int j = 0; j < versions.length; j++) {
				summaryMethodObjectExpression(summaryManager, systemName, versions[j]);
			}
		}
		try {
			System.out.println("Write to " + result + ", lines " + summaryManager.getLineNumber());
			summaryManager.write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Debug.time("End....");
	}
	
	
	public static void summaryMethodObjectExpression(DataTableManager summaryManager, String systemName, String version) {
		String resultFile = QualitasPathsManager.getObjectExpressionResultFile(systemName, version);
		String infoFile = QualitasPathsManager.getObjectExpressionDebugFile(systemName, version);
		String[] columnNameArray = summaryManager.getColumnNameArray();
		String[] usageArray = ObjectExpressionCollector.getAllObjectExpressionsUsageKind();
		String[] kindArray = ObjectExpressionCollector.getAllObjectExpressionsKind();
		
		DataTableManager dataManager = new DataTableManager("data");
		DataTableManager infoManager = new DataTableManager("info");
		int startLine = 1;
		int length = 1000000;
		int minimalUsage = 12;
		int minimalKind = 11;

		try {
			boolean hasLine = true;
			String lastFileName = null;
			String lastClassName = null;
			String lastMethodName = null;
			
			infoManager.read(infoFile, 1);
			while (hasLine == true) {
				hasLine = dataManager.read(resultFile, 0, startLine, startLine+length);
				startLine = startLine + length;
				// dataManager structure: "File\tLocation\tExpression\tDereference\tPolynomial\tType\tKind\tUsage\tDefinition\tClass\tMethod"
				// We assume that the data in dataManager is sorted by File + Class + Method
				int lineNumber = dataManager.getLineNumber();
				String[] summaryInfo = new String[columnNameArray.length];
				for (int index = 0; index < lineNumber; index++) {
					String line[] = dataManager.getLineAsStringArray(index);
//					int lineNo = startLine - length + index;
					
					String currentFileName = line[0];
					String currentClassName = line[9];
					String currentMethodName = line[10];
					if (lastFileName == null || lastClassName == null || lastMethodName == null) {
						lastFileName = currentFileName;
						lastClassName = currentClassName;
						lastMethodName = currentMethodName;
						summaryInfo[0] = currentFileName;
						summaryInfo[1] = currentClassName;
						summaryInfo[2] = currentMethodName;
						for (int i = 3; i < columnNameArray.length; i++) summaryInfo[i] = "0";
					} else if (!lastFileName.equals(currentFileName) || !lastClassName.equals(currentClassName) || !lastMethodName.equals(currentMethodName)) {
						int usageTotal = 0;
						for (int i = 3; i < (3+usageArray.length); i++) {
							if (summaryInfo[i].equals("1")) usageTotal++;
						}
						int kindTotal = 0;
						for (int i = 3+usageArray.length; i < (3+usageArray.length+kindArray.length); i++) {
							if (summaryInfo[i].equals("1")) kindTotal++;
						}
						int total = usageTotal + kindTotal;
						if (usageTotal > minimalUsage || kindTotal > minimalKind) {
							summaryInfo[3+usageArray.length+kindArray.length] = "" + usageTotal;
							summaryInfo[3+usageArray.length+kindArray.length+1] = "" + kindTotal;
							summaryInfo[3+usageArray.length+kindArray.length+2] = "" + total;
							
							int nodes = getMethodCFGNodes(infoManager, lastFileName, lastClassName, lastMethodName);
							summaryInfo[3+usageArray.length+kindArray.length+3] = "" + nodes;
							summaryInfo[3+usageArray.length+kindArray.length+4] = systemName;
							summaryInfo[3+usageArray.length+kindArray.length+5] = version;
							
							summaryManager.appendLine(summaryInfo);
						} else summaryInfo = null;
						
						lastFileName = currentFileName;
						lastClassName = currentClassName;
						lastMethodName = currentMethodName;
						summaryInfo = new String[columnNameArray.length];
						summaryInfo[0] = currentFileName;
						summaryInfo[1] = currentClassName;
						summaryInfo[2] = currentMethodName;
						for (int i = 3; i < columnNameArray.length; i++) summaryInfo[i] = "0";
					} else {
						String kind = line[6];
						String usage = line[7];
						for (int i = 3; i < (3+usageArray.length); i++) {
							if (usage.equals(columnNameArray[i])) {
								summaryInfo[i] = "1";
								break;
							}
						}
						for (int i = 3+usageArray.length; i < (3+usageArray.length+kindArray.length); i++) {
							if (kind.equals(columnNameArray[i])) {
								summaryInfo[i] = "1";
								break;
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static int getMethodCFGNodes(DataTableManager infoManager, String fileName, String className, String methodName) {
		int lineNumber = infoManager.getLineNumber();
		for (int index = 0; index < lineNumber; index++) {
			String[] line = infoManager.getLineAsStringArray(index);
			// infoManager structure: "File\tClass\tMethod\tNodes\tPredicates\tExpressions"
			if (line[0].equals(fileName) && line[1].equals(className) && line[2].equals(methodName)) {
				return infoManager.getCellValueAsInt(index, 3);
			}
		}
		
		return 0;
	}
	
	public static void objectExpressionKindStatistics() {
		String resultFileName = QualitasPathsManager.defaultResultPath + "ObjExpKind.stat";

		DataTableManager resultDataTable = new DataTableManager("result");
		String[] kindArray = ObjectExpressionCollector.getAllObjectExpressionsKind();
		String[] columnNames = new String[2*kindArray.length-2+4];
		columnNames[0] = "System";
		columnNames[1] = "ELOC";
		columnNames[2] = "ObjExp";
		columnNames[3] = "ObjExpDensity";
		int index = 4;
		for (int i = 0; i < kindArray.length; i++) {
			if (ObjectExpressionKind.valueOf(kindArray[i]) == ObjectExpressionKind.OEK_NOT_OBJECT_EXPRESSION) continue;
			columnNames[index] = kindArray[i];
			columnNames[index+1] = kindArray[i] + "_P";
			index = index + 2;
		}
		resultDataTable.setColumnNames(columnNames);
		
		double[] totalData = new double[columnNames.length];
		
		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int i = 0; i < systemNames.length; i++) {
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[i]);
			String sizeMetricFile = QualitasPathsManager.getSystemMeasureResultFile(systemNames[i], false);
			DataTableManager sizeDataTable = new DataTableManager("sizedata");
			sizeDataTable.setKeyColumnIndex("System");
			try {
				sizeDataTable.read(sizeMetricFile, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			for (int j = 0; j < versions.length; j++) {
				System.out.println("Counting system " + versions[j]);
				
				String objExpGroupFile = QualitasPathsManager.getObjectExpressionGroupFile(systemNames[i], versions[j]);
				DataTableManager groupDataTable = new DataTableManager("groupdata");
				try {
					groupDataTable.read(objExpGroupFile, true);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				groupDataTable.setKeyColumnIndex("Kind");
				String[] line = new String[columnNames.length];
				line[0] = versions[j];
				int totalObjExp = groupDataTable.getCellValueAsInt("TOTAL", "Total");
				double eloc = sizeDataTable.getCellValueAsDouble(versions[j], "ELOC");
				double density = 0;
				if (eloc > 0) density = (double)totalObjExp / eloc;
				line[1] = "" + eloc;
				totalData[1] = totalData[1] + eloc;
				line[2] = "" + totalObjExp;
				totalData[2] = totalData[2] + totalObjExp;
				line[3] = "" + density;
				index = 4;
				while (index < columnNames.length) {
					String kind = columnNames[index];
					int totalKindObjExp = groupDataTable.getCellValueAsInt(kind, "Total");
					double proportion = 0;
					if (totalObjExp > 0) proportion = (double)totalKindObjExp / totalObjExp;
					line[index] = "" + totalKindObjExp;
					totalData[index] = totalData[index] + totalKindObjExp;
					line[index+1] = "" + proportion;
					index = index + 2;
				}
				resultDataTable.appendLine(line);
			}
		}
		if (totalData[1] > 0) totalData[3] = totalData[2]/totalData[1];
		else totalData[3] = 0;
		index = 4;
		while (index < columnNames.length) {
			if (totalData[2] > 0) totalData[index+1] = totalData[index]/totalData[2];
			else totalData[index+1] = 0;
			index = index + 2;
		}
		String[] line = new String[columnNames.length];
		line[0] = "Total";
		for (int i = 1; i < columnNames.length; i++) {
			line[i] = "" + totalData[i];
		}
		resultDataTable.appendLine(line);
		
		try {
			resultDataTable.write(resultFileName);
			System.out.println("Write to file " + resultFileName + ", lines " + resultDataTable.getLineNumber());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void objectExpressionUsageStatistics() {
		String resultFileName = QualitasPathsManager.defaultResultPath + "ObjExpUsage.stat";

		DataTableManager resultDataTable = new DataTableManager("result");
		String[] usageArray = ObjectExpressionCollector.getAllObjectExpressionsUsageKind();
		String[] columnNames = new String[2*usageArray.length+4];
		columnNames[0] = "System";
		columnNames[1] = "ELOC";
		columnNames[2] = "ObjExp";
		columnNames[3] = "ObjExpDensity";
		int index = 4;
		for (int i = 0; i < usageArray.length; i++) {
			columnNames[index] = usageArray[i];
			columnNames[index+1] = usageArray[i] + "_P";
			index = index + 2;
		}
		resultDataTable.setColumnNames(columnNames);
		
		double[] totalData = new double[columnNames.length];
		
		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int i = 0; i < systemNames.length; i++) {
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[i]);
			String sizeMetricFile = QualitasPathsManager.getSystemMeasureResultFile(systemNames[i], false);
			DataTableManager sizeDataTable = new DataTableManager("sizedata");
			sizeDataTable.setKeyColumnIndex("System");
			try {
				sizeDataTable.read(sizeMetricFile, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			for (int j = 0; j < versions.length; j++) {
				System.out.println("Counting system " + versions[j]);
				
				String objExpGroupFile = QualitasPathsManager.getObjectExpressionGroupFile(systemNames[i], versions[j]);
				DataTableManager groupDataTable = new DataTableManager("groupdata");
				try {
					groupDataTable.read(objExpGroupFile, true);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				groupDataTable.setKeyColumnIndex("Kind");
				String[] line = new String[columnNames.length];
				line[0] = versions[j];
				int totalObjExp = groupDataTable.getCellValueAsInt("TOTAL", "Total");
				double eloc = sizeDataTable.getCellValueAsDouble(versions[j], "ELOC");
				double density = 0;
				if (eloc > 0) density = (double)totalObjExp / eloc;
				line[1] = "" + eloc;
				totalData[1] = totalData[1] + eloc;
				line[2] = "" + totalObjExp;
				totalData[2] = totalData[2] + totalObjExp;
				line[3] = "" + density;
				index = 4;
				while (index < columnNames.length) {
					String usage = columnNames[index];
					int totalUsageObjExp = groupDataTable.getCellValueAsInt("TOTAL", usage);
					double proportion = 0;
					if (totalObjExp > 0) proportion = (double)totalUsageObjExp / totalObjExp;
					line[index] = "" + totalUsageObjExp;
					totalData[index] = totalData[index] + totalUsageObjExp;
					line[index+1] = "" + proportion;
					index = index + 2;
				}
				resultDataTable.appendLine(line);
			}
		}
		if (totalData[1] > 0) totalData[3] = totalData[2]/totalData[1];
		else totalData[3] = 0;
		index = 4;
		while (index < columnNames.length) {
			if (totalData[2] > 0) totalData[index+1] = totalData[index]/totalData[2];
			else totalData[index+1] = 0;
			index = index + 2;
		}
		String[] line = new String[columnNames.length];
		line[0] = "Total";
		for (int i = 1; i < columnNames.length; i++) {
			line[i] = "" + totalData[i];
		}
		resultDataTable.appendLine(line);
		
		try {
			resultDataTable.write(resultFileName);
			System.out.println("Write to file " + resultFileName + ", lines " + resultDataTable.getLineNumber());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void objectExpressionNullCheckStatistics() {
		String resultFileName = QualitasPathsManager.defaultResultPath + "ObjExpNullCheck.stat";

		DataTableManager resultDataTable = new DataTableManager("result");
		String[] columnNames = new String[20];
		columnNames[0] = "System";
		columnNames[1] = "ELOC";
		columnNames[2] = "ObjExp";
		columnNames[3] = "STMN";
		columnNames[4] = "BRANCHSTMN";
		columnNames[5] = "LOOPSTMN";
		columnNames[6] = "CFGNODE";
		columnNames[7] = "CFGPREDICATE";
		columnNames[8] = "OEUK_CHECKING_NULL";
		columnNames[9] = "OEUK_CHECKING_NULL_P_ELOC";
		columnNames[10] = "OEUK_CHECKING_NULL_P_STMN";
		columnNames[11] = "OEUK_CHECKING_NULL_P_PREDICATE";
		columnNames[12] = ObjectExpressionKind.OEK_SIMPLE_NAME.toString();
		columnNames[13] = ObjectExpressionKind.OEK_SIMPLE_NAME.toString() + "_PROP";
		columnNames[14] = ObjectExpressionKind.OEK_FIELD_ACCESS.toString();
		columnNames[15] = ObjectExpressionKind.OEK_FIELD_ACCESS.toString() + "_PROP";
		columnNames[16] = ObjectExpressionKind.OEK_ARRAY_ACCESS.toString();
		columnNames[17] = ObjectExpressionKind.OEK_ARRAY_ACCESS.toString() + "_PROP";
		columnNames[18] = ObjectExpressionKind.OEK_METHOD_INVOCATION.toString();
		columnNames[19] = ObjectExpressionKind.OEK_METHOD_INVOCATION.toString() + "_PROP";
		resultDataTable.setColumnNames(columnNames);
		
		double[] totalData = new double[columnNames.length];
		
		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int i = 0; i < systemNames.length; i++) {
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[i]);
			String sizeMetricFile = QualitasPathsManager.getSystemMeasureResultFile(systemNames[i], false);
			DataTableManager sizeDataTable = new DataTableManager("sizedata");
			sizeDataTable.setKeyColumnIndex("System");
			try {
				sizeDataTable.read(sizeMetricFile, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			for (int j = 0; j < versions.length; j++) {
				System.out.println("Counting system " + versions[j]);
				
				String objExpGroupFile = QualitasPathsManager.getObjectExpressionGroupFile(systemNames[i], versions[j]);
				DataTableManager groupDataTable = new DataTableManager("groupdata");
				try {
					groupDataTable.read(objExpGroupFile, true);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				groupDataTable.setKeyColumnIndex("Kind");
				String[] line = new String[columnNames.length];
				line[0] = versions[j];
				int totalObjExp = groupDataTable.getCellValueAsInt("TOTAL", "Total");
				double eloc = sizeDataTable.getCellValueAsDouble(versions[j], "ELOC");
				line[1] = "" + eloc;
				totalData[1] = totalData[1] + eloc;
				line[2] = "" + totalObjExp;
				totalData[2] = totalData[2] + totalObjExp;
				
				double statements = sizeDataTable.getCellValueAsDouble(versions[j], "STMN");
				double branches = sizeDataTable.getCellValueAsDouble(versions[j], "BRANCHSTMN");
				double loops = sizeDataTable.getCellValueAsDouble(versions[j], "LOOPSTMN");
				double nodes = sizeDataTable.getCellValueAsDouble(versions[j], "CFGNODE");
				double predicates = sizeDataTable.getCellValueAsDouble(versions[j], "CFGPREDICATE");
				int checkNulls = groupDataTable.getCellValueAsInt("TOTAL", "OEUK_CHECKING_NULL");
				
				line[3] = "" + statements;
				totalData[3] = totalData[3] + statements;
				line[4] = "" + branches;
				totalData[4] = totalData[4] + branches;
				line[5] = "" + loops;
				totalData[5] = totalData[5] + loops;
				line[6] = "" + nodes;
				totalData[6] = totalData[6] + nodes;
				line[7] = "" + predicates;
				totalData[7] = totalData[7] + predicates;
				line[8] = "" + checkNulls;
				totalData[8] = totalData[8] + checkNulls;
				double checkNullPerEloc = checkNulls / eloc;
				double checkNullPerStmn = checkNulls / statements;
				double checkNullPerPred = checkNulls / predicates;
				line[9] = "" + checkNullPerEloc;
				line[10] = "" + checkNullPerStmn;
				line[11] = "" + checkNullPerPred;
				
				int simpleNames = groupDataTable.getCellValueAsInt(columnNames[12], "OEUK_CHECKING_NULL");
				int fieldAccesses = groupDataTable.getCellValueAsInt(columnNames[14], "OEUK_CHECKING_NULL");
				int arrayAccesses = groupDataTable.getCellValueAsInt(columnNames[16], "OEUK_CHECKING_NULL");
				int methodInvocations = groupDataTable.getCellValueAsInt(columnNames[18], "OEUK_CHECKING_NULL");
				double simpleNameProp = 0.0;
				double fieldAccessesProp = 0.0;
				double arrayAccessesProp = 0.0;
				double methodInvocationsProp = 0.0;
				if (checkNulls > 0) {
					simpleNameProp = (double)simpleNames / checkNulls;
					fieldAccessesProp = (double)fieldAccesses / checkNulls;
					arrayAccessesProp = (double)arrayAccesses / checkNulls;
					methodInvocationsProp = (double)methodInvocations / checkNulls;
				}
				line[12] = "" + simpleNames;
				totalData[12] += simpleNames;
				line[13] = "" + simpleNameProp;
				line[14] = "" + fieldAccesses;
				totalData[14] += fieldAccesses;
				line[15] = "" + fieldAccessesProp;
				line[16] = "" + arrayAccesses;
				totalData[16] += arrayAccesses;
				line[17] = "" + arrayAccessesProp;
				line[18] = "" + methodInvocations;
				totalData[18] += methodInvocations;
				line[19] = "" + methodInvocationsProp;
				resultDataTable.appendLine(line);
			}
		}
		totalData[9] = totalData[8] / totalData[1];
		totalData[10] = totalData[8] / totalData[3];
		totalData[11] = totalData[8] / totalData[7];
		totalData[13] = totalData[12] / totalData[8];
		totalData[15] = totalData[14] / totalData[8];
		totalData[17] = totalData[16] / totalData[8];
		totalData[19] = totalData[18] / totalData[8];
		
		String[] line = new String[columnNames.length];
		line[0] = "Total";
		for (int i = 1; i < columnNames.length; i++) {
			line[i] = "" + totalData[i];
		}
		resultDataTable.appendLine(line);
		
		try {
			resultDataTable.write(resultFileName);
			System.out.println("Write to file " + resultFileName + ", lines " + resultDataTable.getLineNumber());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void objectExpressionPolynomialStatistics() {
		String resultFileName = QualitasPathsManager.defaultResultPath + "ObjExpPoly.stat";

		DataTableManager resultDataTable = new DataTableManager("result");
		String[] columnNames = new String[7];
		columnNames[0] = "System";
		columnNames[1] = "ELOC";
		columnNames[2] = "ObjExp";
		columnNames[3] = ObjectExpressionUsageKind.OEUK_CALLING_METHOD.toString();
		columnNames[4] = columnNames[3] + "_PROP";
		columnNames[5] = "Polynomial";
		columnNames[6] = "Polynomial_PROP";
		resultDataTable.setColumnNames(columnNames);
		
		double[] totalData = new double[columnNames.length];
		
		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int i = 0; i < systemNames.length; i++) {
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[i]);
			String sizeMetricFile = QualitasPathsManager.getSystemMeasureResultFile(systemNames[i], false);
			DataTableManager sizeDataTable = new DataTableManager("sizedata");
			sizeDataTable.setKeyColumnIndex("System");
			try {
				sizeDataTable.read(sizeMetricFile, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			for (int j = 0; j < versions.length; j++) {
				System.out.println("Counting system " + versions[j]);
				
				String objExpGroupFile = QualitasPathsManager.getObjectExpressionGroupFile(systemNames[i], versions[j]);
				DataTableManager groupDataTable = new DataTableManager("groupdata");
				try {
					groupDataTable.read(objExpGroupFile, true);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				groupDataTable.setKeyColumnIndex("Kind");
				String[] line = new String[columnNames.length];
				line[0] = versions[j];
				int totalObjExp = groupDataTable.getCellValueAsInt("TOTAL", "Total");
				double eloc = sizeDataTable.getCellValueAsDouble(versions[j], "ELOC");
				line[1] = "" + eloc;
				totalData[1] = totalData[1] + eloc;
				line[2] = "" + totalObjExp;
				totalData[2] = totalData[2] + totalObjExp;
				
				int callMethods = groupDataTable.getCellValueAsInt("TOTAL", columnNames[3]);
				int polynomials = groupDataTable.getCellValueAsInt("TOTAL", columnNames[5]);
				double callMethodsProp = (double)callMethods / totalObjExp;
				double polynomialsProp = 0.0;
				if (callMethods > 0) polynomialsProp = (double)polynomials / callMethods;
				
				line[3] = "" + callMethods;
				totalData[3] = totalData[3] + callMethods;
				line[4] = "" + callMethodsProp;
				line[5] = "" + polynomials;
				totalData[5] = totalData[5] + polynomials;
				line[6] = "" + polynomialsProp;
				resultDataTable.appendLine(line);
			}
		}
		totalData[4] = totalData[3] / totalData[2];
		totalData[6] = totalData[5] / totalData[3];
		
		String[] line = new String[columnNames.length];
		line[0] = "Total";
		for (int i = 1; i < columnNames.length; i++) {
			line[i] = "" + totalData[i];
		}
		resultDataTable.appendLine(line);
		
		try {
			resultDataTable.write(resultFileName);
			System.out.println("Write to file " + resultFileName + ", lines " + resultDataTable.getLineNumber());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void objectExpressionAliasStatistics() {
		String resultFileName = QualitasPathsManager.defaultResultPath + "ObjExpAlias.stat";

		String[] declRowArray = {
				ObjectExpressionKind.OEK_VARIABLE_DECLARATION.toString(),
				ObjectExpressionKind.OEK_OBJECTIVE_ARRAY_DECLARATION.toString(),
		};
		String declColumn = "Total";
		
		String[] aliasRowArray = {
				ObjectExpressionKind.OEK_SIMPLE_NAME.toString(),
				ObjectExpressionKind.OEK_FIELD_ACCESS.toString(),
				ObjectExpressionKind.OEK_ARRAY_ACCESS.toString(),
				ObjectExpressionKind.OEK_TYPE_CAST.toString(),
				ObjectExpressionKind.OEK_CONDITIONAL.toString(),
		};
		String aliasColumn = ObjectExpressionUsageKind.OEUK_RETURN_VALUE.toString();
		
		String sizeMetricFile = QualitasPathsManager.defaultResultPath + "QualitsRecent.txt" ;
		DataTableManager sizeDataTable = new DataTableManager("sizedata");
		sizeDataTable.setKeyColumnIndex("System");

		try {
			sizeDataTable.read(sizeMetricFile, true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		DataTableManager resultDataTable = new DataTableManager("result");
		String[] columnNames = new String[18];
		columnNames[0] = "System";
		columnNames[1] = "ELOC";
		columnNames[2] = "ObjExp";
		columnNames[3] = "Declaration";
		columnNames[4] = "LeftValue";
		columnNames[5] = "MayAlias";
		columnNames[6] = "MayAlias_Per_DECL";
		columnNames[7] = "MayAlias_Per_LEFT";
		columnNames[8] = "ObjectArray";
		columnNames[9] = "ObjectArray_Per_DECL";
		columnNames[10] = "ArrayLeftValue";
		columnNames[11] = "Array_PER_LEFT";
		columnNames[12] = "ArrayRightValue";
		columnNames[13] = "Array_PER_ALIAS";
		columnNames[14] = "AllRightValues";
		columnNames[15] = "MayAlias_Per_RIGHT";
		columnNames[16] = "MethodCallReturns";
		columnNames[17] = "MethodCall_Per_RIGHT";
		resultDataTable.setColumnNames(columnNames);
		
		double[] totalData = new double[columnNames.length];
		
		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int i = 0; i < systemNames.length; i++) {
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[i]);
			
			for (int j = 0; j < versions.length; j++) {
				System.out.println("Counting system " + versions[j]);
				
				String objExpGroupFile = QualitasPathsManager.getObjectExpressionGroupFile(systemNames[i], versions[j]);
				DataTableManager groupDataTable = new DataTableManager("groupdata");
				try {
					groupDataTable.read(objExpGroupFile, true);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				groupDataTable.setKeyColumnIndex("Kind");
				String[] line = new String[columnNames.length];
				line[0] = versions[j];
				int totalObjExp = groupDataTable.getCellValueAsInt("TOTAL", "Total");
				double eloc = sizeDataTable.getCellValueAsDouble(versions[j], "ELOC");
				line[1] = "" + eloc;
				totalData[1] = totalData[1] + eloc;
				line[2] = "" + totalObjExp;
				totalData[2] = totalData[2] + totalObjExp;
				
				int declaration = 0;
				for (int k = 0; k < declRowArray.length; k++) {
					declaration += groupDataTable.getCellValueAsInt(declRowArray[k], declColumn);
				}
				line[3] = "" + declaration;
				totalData[3] += declaration;
				
				int leftValue = groupDataTable.getCellValueAsInt("TOTAL", ObjectExpressionUsageKind.OEUK_LEFT_VALUE.toString());
				line[4] = "" + leftValue;
				totalData[4] += leftValue;
				
				int alias = 0;
				for (int k = 0; k < aliasRowArray.length; k++) {
					alias += groupDataTable.getCellValueAsInt(aliasRowArray[k], aliasColumn);
				}
				line[5] = "" + alias;
				totalData[5] += alias;
				
				double aliasPerDecl = (double)alias / declaration;
				double aliasPerLeft = (double)alias / leftValue;
				line[6] = "" + aliasPerDecl;
				line[7] = "" + aliasPerLeft;

				int objectArray = groupDataTable.getCellValueAsInt(ObjectExpressionKind.OEK_OBJECTIVE_ARRAY_DECLARATION.toString(), "Total");
				int arrayLeft = groupDataTable.getCellValueAsInt(ObjectExpressionKind.OEK_ARRAY_ACCESS.toString(), ObjectExpressionUsageKind.OEUK_LEFT_VALUE.toString());
				int arrayRight = groupDataTable.getCellValueAsInt(ObjectExpressionKind.OEK_ARRAY_ACCESS.toString(), ObjectExpressionUsageKind.OEUK_RIGHT_VALUE.toString());
				double objectArrayPerDecl = (double)objectArray / declaration;
				double arrayPerLeft = (double)arrayLeft / leftValue;
				double arrayPerAlias = (double)arrayRight / alias;
				line[8] = "" + objectArray;
				totalData[8] += objectArray;
				line[9] = "" + objectArrayPerDecl;
				line[10] = "" + arrayLeft;
				totalData[10] += arrayLeft;
				line[11] = "" + arrayPerLeft;
				line[12] = "" + arrayRight;
				totalData[12] += arrayRight;
				line[13] = "" + arrayPerAlias;
				
				int rightValues = groupDataTable.getCellValueAsInt("TOTAL", ObjectExpressionUsageKind.OEUK_RIGHT_VALUE.toString()); 
				double aliasPerRight = (double)alias / rightValues;
				int methodCallReturnValues = groupDataTable.getCellValueAsInt(ObjectExpressionKind.OEK_METHOD_INVOCATION.toString(), ObjectExpressionUsageKind.OEUK_RIGHT_VALUE.toString());
				double methodCallPerRight = (double)methodCallReturnValues / rightValues;
				
				line[14] = "" + rightValues;
				totalData[14] += rightValues;
				line[15] = "" + aliasPerRight;
				line[16] = "" + methodCallReturnValues;
				totalData[16] += methodCallReturnValues;
				line[17] = "" + methodCallPerRight;
				resultDataTable.appendLine(line);
			}
		}
		
		totalData[6] = totalData[5] / totalData[3];
		totalData[7] = totalData[5] / totalData[4];
		totalData[9] = totalData[8] / totalData[3];
		totalData[11] = totalData[10] / totalData[4];
		totalData[13] = totalData[12] / totalData[5];
		totalData[15] = totalData[5] / totalData[14];
		totalData[17] = totalData[16] / totalData[14];
		String[] line = new String[columnNames.length];
		line[0] = "Total";
		for (int i = 1; i < columnNames.length; i++) {
			line[i] = "" + totalData[i];
		}
		resultDataTable.appendLine(line);
		
		try {
			resultDataTable.write(resultFileName);
			System.out.println("Write to file " + resultFileName + ", lines " + resultDataTable.getLineNumber());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void objectExpressionSizeStatistics() {
		String resultFileName = QualitasPathsManager.defaultResultPath + "ObjExpSize.stat";

		String[] declRowArray = {
				ObjectExpressionKind.OEK_VARIABLE_DECLARATION.toString(),
				ObjectExpressionKind.OEK_PRIMITIVE_ARRAY_DECLARATION.toString(),
				ObjectExpressionKind.OEK_OBJECTIVE_ARRAY_DECLARATION.toString(),
		};
		String declColumn = "Total";
		
		String sizeMetricFile = QualitasPathsManager.defaultResultPath + "QualitsRecent.txt" ;
		DataTableManager sizeDataTable = new DataTableManager("sizedata");
		sizeDataTable.setKeyColumnIndex("System");

		try {
			sizeDataTable.read(sizeMetricFile, true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		DataTableManager resultDataTable = new DataTableManager("result");
		String[] columnNames = new String[14];
		columnNames[0] = "System";
		columnNames[1] = "FILe";
		columnNames[2] = "ELOC";
		columnNames[3] = "STMN";
		columnNames[4] = "CFGNODE";
		columnNames[5] = "CollectedExp";
		columnNames[6] = "CollectedDecl";
		columnNames[7] = "GroupedExp";
		columnNames[8] = "Exp_PER_FILE";
		columnNames[9] = "Exp_PER_ELOC";
		columnNames[10] = "Exp_PER_STMN";
		columnNames[11] = "Exp_PER_NODE";
		columnNames[12] = "GroupedDecl";
		columnNames[13] = "GroupedDecl_PROP";
		resultDataTable.setColumnNames(columnNames);
		
		double[] totalData = new double[columnNames.length];
		
		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int i = 0; i < systemNames.length; i++) {
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[i]);
			
			for (int j = 0; j < versions.length; j++) {
				System.out.println("Counting system " + versions[j]);
				
				String objExpGroupFile = QualitasPathsManager.getObjectExpressionGroupFile(systemNames[i], versions[j]);
				DataTableManager groupDataTable = new DataTableManager("groupdata");
				try {
					groupDataTable.read(objExpGroupFile, true);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				groupDataTable.setKeyColumnIndex("Kind");
				String[] line = new String[columnNames.length];
				line[0] = versions[j];
				double eloc = sizeDataTable.getCellValueAsDouble(versions[j], "ELOC");
				double files = sizeDataTable.getCellValueAsDouble(versions[j], "FILE");
				double statements = sizeDataTable.getCellValueAsDouble(versions[j], "STMN");
				double nodes = sizeDataTable.getCellValueAsDouble(versions[j], "CFGNODE");
				double collectedExps = sizeDataTable.getCellValueAsDouble(versions[j], "Expression");
				double collectedDecls = sizeDataTable.getCellValueAsDouble(versions[j], "Declaration");
				line[1] = "" + files;
				totalData[1] += files;
				line[2] = "" + eloc;
				totalData[2] += eloc;
				line[3] = "" + statements;
				totalData[3] += statements;
				line[4] = "" + nodes;
				totalData[4] += nodes;
				line[5] = "" + collectedExps;
				totalData[5] += collectedExps;
				line[6] = "" + collectedDecls;
				totalData[6] += collectedDecls;
				
				int groupedExps = groupDataTable.getCellValueAsInt("TOTAL", "Total");
				int groupedDecls = 0;
				for (int k = 0; k < declRowArray.length; k++) {
					groupedDecls += groupDataTable.getCellValueAsInt(declRowArray[k], declColumn);
				}
				line[7] = "" + groupedExps;
				totalData[7] += groupedExps;
				double expPerFile = (double)groupedExps / files;
				double expPerEloc = (double)groupedExps / eloc;
				double expPerStatement = (double)groupedExps / statements;
				double expPerNode = (double)groupedExps / nodes;
				line[8] = "" + expPerFile;
				line[9] = "" + expPerEloc;
				line[10] = "" + expPerStatement;
				line[11] = "" + expPerNode;
				
				line[12] = "" + groupedDecls;
				totalData[12] += groupedDecls;
				double declsProp = (double)groupedDecls / collectedDecls;
				line[13] = "" + declsProp;
				
				resultDataTable.appendLine(line);
			}
		}
		totalData[8] = totalData[7] / totalData[1];
		totalData[9] = totalData[7] / totalData[2];
		totalData[10] = totalData[7] / totalData[3];
		totalData[11] = totalData[7] / totalData[4];
		totalData[13] = totalData[12] / totalData[6];
		
		String[] line = new String[columnNames.length];
		line[0] = "Total";
		for (int i = 1; i < columnNames.length; i++) {
			line[i] = "" + totalData[i];
		}
		resultDataTable.appendLine(line);
		
		try {
			resultDataTable.write(resultFileName);
			System.out.println("Write to file " + resultFileName + ", lines " + resultDataTable.getLineNumber());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
