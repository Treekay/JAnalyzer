package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import measurement.CompilationUnitMeasurement;
import measurement.measure.MeasureObjectKind;
import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.NameScopeFilter;
import nameTable.filter.NameScopeKindFilter;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameScopeVisitor;
import softwareStructure.SoftwareStructManager;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2015年9月17日
 * @version 1.0
 */
public class CompilationUnitMetricCollector {

	public static void main(String[] args) {
		String systemName = "eclipse_SDK";
		String[] versions = QualitasPathsManager.getSystemVersions(systemName);

		PrintWriter output = null;
		try {
			String info = QualitasPathsManager.getDebugFile();
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		for (int i = 0; i < versions.length; i++) collectQualitasCompilationUnitMeasure(systemName, versions[i]);
		if (output != null) output.close();
	}

	public static void collectQualitasCompilationUnitMeasure(String systemName, String version) {
		String systemPath = QualitasPathsManager.getSystemPath(systemName, version);
		String result = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_UNIT, systemName, version, true);

		PrintWriter writer = new PrintWriter(System.out);
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		collectQualitasCompilationUnitMeasure(systemPath, writer);
		writer.close();
	}
	
	public static void collectQualitasCompilationUnitMeasure(String path, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getAvailableCompilationUnitMeasureList();
		
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
//		List<CompilationUnitScope> unitList = manager.getAllCompilationUnitScopes();
		
		NameScopeVisitor visitor = new NameScopeVisitor(new NameScopeKindFilter(NameScopeKind.NSK_COMPILATION_UNIT));
		manager.accept(visitor);
		List<NameScope> scopeList = visitor.getResult();
		
		writer.print("CompilationUnit");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.getIdentifier());
		writer.println("\tPackage");
		
		Debug.setStart("Begin scan class....");
		for (NameScope scope : scopeList) {
			CompilationUnitScope unit = (CompilationUnitScope)scope;
			
			System.out.println("Scan file: " + unit.getUnitName());
			
			Debug.setStart("Begin calculating measures....!");
			CompilationUnitMeasurement measurement = new CompilationUnitMeasurement(unit, structManager);
			measureList = measurement.getMeasureList(measureList);
			writer.print(unit.getUnitName());
			for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.valueString());
			writer.println("\t" + unit.getEnclosingPackage().getFullQualifiedName());
			Debug.time("End calculating....");
		}
		Debug.time("End scan.....");
	}

	public static List<SoftwareMeasure> getAvailableCompilationUnitMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

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

