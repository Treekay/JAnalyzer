package measurement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import measurement.measure.ClassMeasureDistribution;
import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import measurement.metric.SoftwareStructMetric;
import measurement.metric.SoftwareStructMetricFactory;
import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameDefinitionVisitor;
import softwareStructure.SoftwareStructManager;
import util.Debug;
import sourceCodeAST.SourceCodeFileSet;

/**
 * @author Zhou Xiaocong
 * @since 2015年7月3日
 * @version 1.0
 */
public class TestSystemMeasurementManager {

	public static void main(String[] args) {
		testSingleSystemMeasure();
	}
	

	public static void testSingleSystemMeasure() {
		String[] paths = {"C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
				"C:\\ZxcTemp\\src\\",
				"C:\\ZxcWork\\ProgramAnalysis\\src\\",
				"C:\\ZxcTools\\JDKSource\\"
		};
		
		String path = paths[2];
		
		String dataPath = "C:\\ZxcWork\\ProgramAnalysis\\data\\";
		String result = dataPath + "PA(metric).txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = null;
		
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		try {
			String info = dataPath + "debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		Debug.setScreenOn();
		Debug.setStart("Begin....");
		Debug.disable();
//		testMethodMeasure(path, writer);
//		testDetailedTypeMeasure(path, writer);
		testCompilationUnitMeasure(path, writer);
//		testPackageMeasure(path, writer);
//		testMeasureDistribution(path, writer);
		Debug.enable();
		Debug.time("End....");
		
		writer.close();
		output.close();
	}

	
	public static void testCompilationUnitMeasure(String path, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getAvailableSoftwareSizeMeasureList();

		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		List<CompilationUnitScope> unitList = manager.getAllCompilationUnitScopes();

		writer.print("CompilationUnit");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.getIdentifier());
		writer.println("Package");
		
		Debug.setStart("Begin scan unit....");
		for (CompilationUnitScope unit : unitList) {
			System.out.println("Scan unit file: " + unit.getScopeName());
			
			CompilationUnitMeasurement measurement = new CompilationUnitMeasurement(unit, structManager);
			measureList = measurement.getMeasureList(measureList);
			writer.print(unit.getUnitName());
			for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.valueString());
			writer.println("\t" + unit.getEnclosingPackage().getFullQualifiedName());
		}
		Debug.time("End scan.....");
		
		writer.flush();
	}
	

	public static void testDetailedTypeMeasure(String path, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getTestingMeasureList();
		
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.time("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
//		Debug.enable();
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		Debug.time("Begin creating structure....");
		if (path.contains("ProgramAnalysis")) structManager.createSoftwareStructure();
		else structManager.readOrCreateSoftwareStructure();
		Debug.time("End creating structure....");
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new DetailedTypeFilter());
		SystemScope rootScope = manager.getSystemScope();
		
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		writer.print("Class");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.getIdentifier());
		writer.println("\tPackage\tLabel");
		
		Debug.time("Begin scan class....");
		int counter = 1;
		int totalCounter = definitionList.size();
		for (NameDefinition definition : definitionList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			
			Debug.time("Scan class " + counter + " : " + type.getFullQualifiedName() + ", total " + totalCounter);
			
//			Debug.time("Begin calculating measures....!");
			ClassMeasurement measurement = new ClassMeasurement(type, structManager);
			measurement.getMeasureList(measureList);
			writer.print(type.getSimpleName());
			for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.valueString());
			writer.println("\t" + type.getEnclosingPackage().getFullQualifiedName() + "\t" + type.getUniqueId());
			Debug.time("\tEnd calculating, remain " + (totalCounter - counter) + " classes ....");
			counter++;
		}
		Debug.time("End scan.....");
	}
	
	public static void testPackageMeasure(String path, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getTestingMeasureList();
		
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new PackageFilter());
		SystemScope rootScope = manager.getSystemScope();
		
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		writer.print("Package");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.getIdentifier());
		writer.println("\tLabel");
		
		Debug.setStart("Begin scan class....");
		for (NameDefinition definition : definitionList) {
			PackageDefinition packageDef = (PackageDefinition)definition;
			
			System.out.println("Scan package: " + packageDef.getFullQualifiedName());
			
			Debug.setStart("Begin calculating measures....!");
			PackageMeasurement measurement = new PackageMeasurement(packageDef, structManager);
			measurement.getMeasureList(measureList);
			
			writer.print(packageDef.getFullQualifiedName());
			for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.valueString());
			writer.println(packageDef.getUniqueId());
			
			Debug.time("End calculating....");
		}
		Debug.time("End scan.....");
	}
	
	public static void testMethodMeasure(String path, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getTestingMeasureList();
		
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new MethodDefinitionFilter());
		SystemScope rootScope = manager.getSystemScope();
		
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		writer.print("Method");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + SoftwareMeasureIdentifier.getDescription(measure));
		writer.println("\tClass\tPackage\tLabel");
		
		Debug.setStart("Begin scan method....");
		for (NameDefinition definition : definitionList) {
			MethodDefinition method = (MethodDefinition)definition;
			
			System.out.println("Scan method: " + method.getFullQualifiedName());
			
			Debug.setStart("Begin calculating measures....!");
			MethodMeasurement measurement = new MethodMeasurement(method, structManager);
			measurement.getMeasureList(measureList);

			writer.print(method.getSimpleName());
			for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.valueString());
			writer.println("\t" + method.getEnclosingType().getSimpleName() + "\t" + method.getEnclosingType().getEnclosingPackage().getSimpleName() + "\t" + method.getUniqueId());

			Debug.time("End calculating....");
		}
		Debug.time("End scan.....");
	}

	
	public static void testSystemSizeMeasure(String systemPath, String[] versionPaths, String mainDir, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getAvailableSoftwareSizeMeasureList();

		writer.print("System");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + SoftwareMeasureIdentifier.getDescription(measure));
		writer.println("\tNotes");
		
		for (int index = 0; index < versionPaths.length; index++) {
			String path = systemPath + versionPaths[index] + "\\" + mainDir + "\\";
			
			SourceCodeFileSet parser = new SourceCodeFileSet(path);
			NameTableCreator creator = new NameDefinitionCreator(parser);

			Debug.setStart("Begin creating system, path = " + path);
			NameTableManager manager = creator.createNameTableManager();
			Debug.time("End creating.....");
			Debug.flush();
			SoftwareStructManager structManager = new SoftwareStructManager(manager);
			SystemScope rootScope = manager.getSystemScope();

			Debug.setStart("Begin calculating measures....!");
			SystemScopeMeasurement measurement = new SystemScopeMeasurement(rootScope, structManager);
			measurement.getMeasureList(measureList);
//			measurement.printToRow(writer, false, versionPaths[index], path);
			Debug.time("End calculating....");
		}

		
		writer.flush();
	}
	

	public static void testMeasureDistribution(String path, PrintWriter writer) {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		
		SystemMeasurementManager systemMeasurement = new SystemMeasurementManager(manager, structManager);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new DetailedTypeFilter());
		SystemScope rootScope = manager.getSystemScope();

		SoftwareMeasure measure = new SoftwareMeasure(SoftwareMeasureIdentifier.STMN);
		SoftwareStructMetric metric = SoftwareStructMetricFactory.getMetricInstance(measure);
		metric.setMeasuringObject(rootScope);
		metric.setSoftwareStructManager(structManager);
		metric.calculate(measure);
		if (measure.isUsable()) {
			writer.println("System statements: " + measure.getValue());
		}
		
		
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		List<DetailedTypeDefinition> typeList = new ArrayList<DetailedTypeDefinition>();
		for (NameDefinition definition : definitionList) typeList.add((DetailedTypeDefinition)definition);
		
		Debug.setStart("Begin scan class....");
		
		ClassMeasureDistribution distribution = systemMeasurement.getMeasureDistributionOfClassList(typeList, measure);
		distribution.printToColumn(writer, true);
		
//		measure = new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM2);
//		distribution = systemMeasurement.getMeasureDistributionOfClassList(typeList, measure);
//		distribution.printToColumn(writer, false);
		
		Debug.time("End scan.....");
	}
	
	public static List<SoftwareMeasure> getAvailableSoftwareSizeMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.FILE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PKG),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

				new SoftwareMeasure(SoftwareMeasureIdentifier.ELOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.BLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOPT),
				
//				new SoftwareMeasure(SoftwareMeasureIdentifier.ALLMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.IHMTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.NEWMTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.OVMTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.IMPMTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.ALLFLD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.IHFLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PARS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOCV),
				new SoftwareMeasure(SoftwareMeasureIdentifier.STMN),

				new SoftwareMeasure(SoftwareMeasureIdentifier.BYTE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.WORD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CHAR),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}

	public static List<SoftwareMeasure> getAvailableSoftwareCohesionMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM1),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM2),

				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM5),
				new SoftwareMeasure(SoftwareMeasureIdentifier.Coh),

				new SoftwareMeasure(SoftwareMeasureIdentifier.TCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCI),

				new SoftwareMeasure(SoftwareMeasureIdentifier.CC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LSCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCOM),

				new SoftwareMeasure(SoftwareMeasureIdentifier.CAMC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICH),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCC),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}

	public static List<SoftwareMeasure> getAvailableSoftwareCouplingMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBO),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOi),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOe),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.RFC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.RFCp),

				new SoftwareMeasure(SoftwareMeasureIdentifier.DAC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DACp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.IHICP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MPC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.ACAIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ACMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.AMMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCAEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DMMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCAIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCMIC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.OMMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCAEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OMMEC),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}

	public static List<SoftwareMeasure> getAvailableSoftwareInheritanceMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.AID),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DIT),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SIX),

				new SoftwareMeasure(SoftwareMeasureIdentifier.NOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOA),

				new SoftwareMeasure(SoftwareMeasureIdentifier.SPA),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SPD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SP),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.DPA),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DPD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DP),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
	
	public static List<SoftwareMeasure> getTestingMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.FLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ELOC),

				// Cohesion metrics
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM1),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM1p),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM2),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM2p),

				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM5),
				new SoftwareMeasure(SoftwareMeasureIdentifier.Coh),

				new SoftwareMeasure(SoftwareMeasureIdentifier.TCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCI),

				new SoftwareMeasure(SoftwareMeasureIdentifier.CC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LSCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCOM),

				new SoftwareMeasure(SoftwareMeasureIdentifier.CAMC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICH),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCC),
				
				// Coupling metrics
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBO),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOi),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOe),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.RFC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.RFCp),

				new SoftwareMeasure(SoftwareMeasureIdentifier.DAC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DACp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.IHICP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MPC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.ACAIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ACMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.AMMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCAEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DMMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCAIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCMIC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.OMMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCAEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OMMEC),

				// Inheritance metrics
				new SoftwareMeasure(SoftwareMeasureIdentifier.AID),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DIT),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SIX),

				new SoftwareMeasure(SoftwareMeasureIdentifier.NOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOA),

				new SoftwareMeasure(SoftwareMeasureIdentifier.SPA),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SPD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SP),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.DPA),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DPD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DP),

				// Size metrics
				new SoftwareMeasure(SoftwareMeasureIdentifier.ALLMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.IHMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NEWMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OVMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.IMPMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ALLFLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.IHFLD),

				new SoftwareMeasure(SoftwareMeasureIdentifier.STMN),
				new SoftwareMeasure(SoftwareMeasureIdentifier.BLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOPT),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

				new SoftwareMeasure(SoftwareMeasureIdentifier.PARS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOCV),

				new SoftwareMeasure(SoftwareMeasureIdentifier.BYTE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.WORD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CHAR),
		};
		
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
	
	
}

class DetailedTypeFilter extends NameTableFilter {

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isDetailedType()) return false;	
		DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
		if (!type.isPublic() || !type.isPackageMember()) return false;
		return true;
//		if (definition.getSimpleName().equals("DefinitionCounterMetric")) return true;
//		else return false;
	}
}


class MethodDefinitionFilter extends NameTableFilter {

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isMethodDefinition()) return false;		
//		return true;
		if (definition.getSimpleName().contains("ConciseASTVisitor")) return true;
		else return false;
	}
}

class PackageFilter extends NameTableFilter {

	@Override
	public boolean accept(NameDefinition definition) {
		if (definition.getDefinitionKind() != NameDefinitionKind.NDK_PACKAGE) return false;	
		return true;
//		if (definition.getSimpleName().contains("ast")) return true;
//		else return false;
	}
}

