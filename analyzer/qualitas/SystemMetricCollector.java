package analyzer.qualitas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import analyzer.dataTable.DataTableManager;
import measurement.CompilationUnitMeasurement;
import measurement.MethodMeasurement;
import measurement.SystemScopeMeasurement;
import measurement.measure.MeasureObjectKind;
import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import nameTable.NameTableManager;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameDefinitionVisitor;
import softwareStructure.SoftwareStructManager;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ17ÈÕ
 * @version 1.0
 */
public class SystemMetricCollector {
	
	public static void main(String[] args) {
//		collectQualitasUnitFileMeasure(true);
//		collectQualitasMethodMeasure(true);
		collectQualitasRecentSystemsMeasure(true);
//		writeQualitasRecentSystemsMeasureIntoOneFile();
	}

	public static void writeQualitasRecentSystemsMeasureIntoOneFile() {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String summaryFile = QualitasPathsManager.defaultResultPath + "QualitsRecent.txt" ;
		
		DataTableManager resultTable = new DataTableManager("QualitasRecent");
		DataTableManager metricTable = new DataTableManager("metric");

		Debug.setScreenOn();
		Debug.setStart("Begin....");
		String[] resultColumnNames = null;
		for (int i = 0; i < systemNames.length; i++) {
			String resultFile = QualitasPathsManager.getSystemMeasureResultFile(systemNames[i], false);
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[i]);
			// For Qualitas recent package, a system only has a version!
			String infoFile = QualitasPathsManager.getObjectExpressionDebugFile(systemNames[i], versions[0]);
			
			File info = new File(infoFile);
			if (!info.exists()) continue;
			try {
				metricTable.read(resultFile, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			if (resultColumnNames == null) {
				String[] metricColumnNames = metricTable.getColumnNameArray();
				System.out.println("Metric column length: " + metricColumnNames.length + ", for " + resultFile);
				resultColumnNames = new String[metricColumnNames.length + 3];
				for (int j = 0; j < metricColumnNames.length; j++) resultColumnNames[j] = metricColumnNames[j];
				resultColumnNames[metricColumnNames.length] = "Declaration";
				resultColumnNames[metricColumnNames.length+1] = "Expression";
				resultColumnNames[metricColumnNames.length+2] = "Times";
				resultTable.setColumnNames(resultColumnNames);
			}
			String[] line = new String[resultColumnNames.length];
			// System metric result file has only one line!
			String[] metricLine = metricTable.getLineAsStringArray(0);
			System.out.println("Line length " + line.length + ", metric line length " + metricLine.length + ", for " + resultFile);
			for (int j = 0; j < metricLine.length; j++) line[j] = metricLine[j];
			setDeclarationExpressionAndTimeInLine(infoFile, line);
			resultTable.appendLine(line);
		}
		try {
			resultTable.write(summaryFile);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End....");
	}

	static void setDeclarationExpressionAndTimeInLine(String infoFile, String[] line) {
		int declarationNumber = 0;
		int expressionNumber = 0;
		int time = 0;
		try {
			Scanner scanner = new Scanner(new File(infoFile));
			while(scanner.hasNextLine()) {
				String infoLine = scanner.nextLine();
				String[] infoStrings = infoLine.split("\t");
				if (infoStrings[0].equals("Collected")) {
					// InfoLine should be like: Collected	nodes	30735	predicates	4143	declaration	13969	expressions	57764	time	13083
					declarationNumber = Integer.parseInt(infoStrings[6]);
					expressionNumber = Integer.parseInt(infoStrings[8]);
					time = Integer.parseInt(infoStrings[10]);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		line[line.length-3] = "" + declarationNumber;
		line[line.length-2] = "" + expressionNumber;
		line[line.length-1] = "" + time; 
	}
	
	
	public static void collectQualitasRecentSystemsMeasure(boolean recollect) {
		String[] systemNames = QualitasPathsManager.getSystemNames();

		PrintWriter output = null;
		try {
			String info = QualitasPathsManager.getDebugFile();
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Debug.setScreenOn();
		Debug.setStart("Begin....");
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
//			if (systemName.equals("eclipse_SDK") || systemName.equals("jre") || systemName.equals("netbeans")) continue;
//			if (!systemName.equals("JAnalyzer")) continue;
			collectQualitasSystemMeasure(systemName, recollect);
		}
		Debug.time("End....");
		if (output != null) output.close();
	}
	
	public static void collectQualitasSystemMeasure(String systemName, boolean recollect) {
		String result = QualitasPathsManager.getSystemMeasureResultFile(systemName, false);

		File resultFile = new File(result);
		if (!recollect) {
			if (resultFile.exists()) return;
		}
		PrintWriter writer = new PrintWriter(System.out);
		try {
			writer = new PrintWriter(new FileOutputStream(resultFile));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		collectQualitasSystemMeasure(systemName, writer);
		writer.close();
	}
	
	public static void collectQualitasSystemMeasure(String systemName, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getAvailableSystemMeasureList();

		writer.print("System");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.getIdentifier());
		writer.println("\tNotes");

		String[] versionPaths = QualitasPathsManager.getSystemVersions(systemName);
		if (versionPaths.length < 1) return;
		
		for (int index = versionPaths.length-1; index < versionPaths.length; index++) {
			String path = QualitasPathsManager.getSystemPath(systemName, versionPaths[index]);
			
			Debug.setStart("Begin creating system, path = " + path);
			NameTableManager manager = NameTableManager.createNameTableManager(path);
			Debug.time("End creating.....");
			Debug.flush();
			SoftwareStructManager structManager = new SoftwareStructManager(manager);
			SystemScope rootScope = manager.getSystemScope();

			Debug.setStart("Begin calculating measures....!");
			SystemScopeMeasurement measurement = new SystemScopeMeasurement(rootScope, structManager);
			measurement.getMeasureList(measureList);
			writer.print(versionPaths[index]);
			for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.valueString());
			writer.println("\t" + path);
			Debug.time("End calculating....");
		}
		writer.flush();
	}

	public static void collectQualitasMethodMeasure(boolean recollect) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int i = 0; i < systemNames.length; i++) {
			if (!systemNames[i].equals("jena")) continue;
			
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[i]);
			for (int j = 0; j < versions.length; j++) {
				String resultFileName = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_METHOD, systemNames[i], versions[j], false);
				File resultFile = new File(resultFileName);
				if (!recollect) {
					if (resultFile.exists()) return;
				}
				PrintWriter writer = new PrintWriter(System.out);
				try {
					writer = new PrintWriter(new FileOutputStream(resultFile));
				} catch (Exception exc) {
					exc.printStackTrace();
				}
				collectQualitasMethodMeasure(systemNames[i], versions[j], writer);
				writer.close();
			}
		}
	}
	
	public static void collectQualitasMethodMeasure(String systemName, String version, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getAvailableSystemMeasureList();

		writer.print("File\tClass\tMethod");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.getIdentifier());
		writer.println();
		
		String path = QualitasPathsManager.getSystemPath(systemName, version);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = NameTableManager.createNameTableManager(path);
		Debug.time("End creating.....");
		Debug.flush();
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		manager.accept(visitor);
		List<NameDefinition> resultList = visitor.getResult();

		Debug.setStart("Begin calculating measures....!");
		
		for (NameDefinition definition : resultList) {
			MethodDefinition method = (MethodDefinition)definition;
			if (method.isAutoGenerated()) continue;
			CompilationUnitScope unit = manager.getEnclosingCompilationUnitScope(method);
			DetailedTypeDefinition type = manager.getEnclosingDetailedTypeDefinition(method);
			if (type.isInterface()) continue;
			if (method.getBodyScope() == null) continue;
			
			String unitFile = unit.getUnitName();
			String className = type.getSimpleName();
			String methodName = method.getSimpleName() + "@" + method.getLocation();
			if (!unitFile.equals("src\\azureustools1.6.9-src\\org\\azureus\\org\\eclipse\\jdt\\internal\\core\\JavaModelManager.java")) continue;
			
			Debug.println("Calculate method " + method.getFullQualifiedName());
			MethodMeasurement measurement = new MethodMeasurement(method, structManager);
			measurement.getMeasureList(measureList);
			
			writer.print(unitFile + "\t" + className + "\t" + methodName);
			for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.valueString());
			writer.println();
		}
		Debug.time("End calculating....");
	}
	
	public static void collectQualitasUnitFileMeasure(boolean recollect) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int i = 0; i < systemNames.length; i++) {
			if (!systemNames[i].equals("megamek")) continue;
			
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[i]);
			for (int j = 0; j < versions.length; j++) {
				String resultFileName = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_UNIT, systemNames[i], versions[j], false);
				File resultFile = new File(resultFileName);
				if (!recollect) {
					if (resultFile.exists()) return;
				}
				PrintWriter writer = new PrintWriter(System.out);
				try {
					writer = new PrintWriter(new FileOutputStream(resultFile));
				} catch (Exception exc) {
					exc.printStackTrace();
				}
				collectQualitasUnitFileMeasure(systemNames[i], versions[j], writer);
				writer.close();
			}
		}
	}
	
	public static void collectQualitasUnitFileMeasure(String systemName, String version, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getAvailableSystemMeasureList();

		writer.print("File");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.getIdentifier());
		writer.println();
		
		String path = QualitasPathsManager.getSystemPath(systemName, version);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = NameTableManager.createNameTableManager(path);
		Debug.time("End creating.....");
		Debug.flush();
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		
		List<CompilationUnitScope> unitScopeList = manager.getAllCompilationUnitScopes();

		Debug.setStart("Begin calculating measures....!");
		for (CompilationUnitScope unitScope : unitScopeList) {
			
			String unitFile = unitScope.getUnitName();

			Debug.println("Calculate file " + unitFile);
			CompilationUnitMeasurement measurement = new CompilationUnitMeasurement(unitScope, structManager);
			measurement.getMeasureList(measureList);
			
			writer.print(unitFile);
			for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.valueString());
			writer.println();
		}
		Debug.time("End calculating....");
	}

	
	public static List<SoftwareMeasure> getAvailableSystemMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.FILE),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.PKG),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

//				new SoftwareMeasure(SoftwareMeasureIdentifier.NonTopTYPE),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.TopPubTYPE),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.TopNonPubTYPE),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.NonTopCLS),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.TopPubCLS),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.TopNonPubCLS),

				new SoftwareMeasure(SoftwareMeasureIdentifier.ELOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.BLOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.CLOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.NLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.LOPT),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.FLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.PARS),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.LOCV),
				new SoftwareMeasure(SoftwareMeasureIdentifier.STMN),
				new SoftwareMeasure(SoftwareMeasureIdentifier.BRANCHSTMN),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOOPSTMN),

				new SoftwareMeasure(SoftwareMeasureIdentifier.CFGNODE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CFGPREDICATE),

//				new SoftwareMeasure(SoftwareMeasureIdentifier.BYTE),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.WORD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.CHAR),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
}
