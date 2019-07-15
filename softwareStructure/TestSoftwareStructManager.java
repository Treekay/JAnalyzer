package softwareStructure;

import graph.basic.GraphUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.DetailedTypeDefinitionFilter;
import nameTable.filter.NameDefinitionNameFilter;
import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameDefinitionVisitor;
import util.Debug;
import sourceCodeAST.SourceCodeFileSet;

/**
 * @author Zhou Xiaocong
 * @since 2015年7月3日
 * @version 1.0
 */
public class TestSoftwareStructManager {

	public static void main(String[] args) {
		String rootPath = "C:\\";

		String[] paths = {"C:\\QualitasPacking\\recent\\azureus\\azureus-4.8.1.2\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\ProgramAnalysis\\src\\", "C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", 
							rootPath + "ZxcWork\\ToolKit\\src\\", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
							rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
							rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
							rootPath + "ZxcTools\\ArgoUml\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
							rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};
		
		String path = paths[4];
		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = null;
		
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Debug.setScreenOn();
//		Debug.disable();
		try {
//			testInheritanceInformation(path, writer);
//			testMemberInformation(path, writer);
//			testMethodInvocations(path, writer);
//			testDetailedTypeMatrix(path, writer);
//			testDetailedTypeUsingMatrix(path, writer);
			testTypeStructManager(path, writer);
//			findLargePolyCallMethod(path, writer);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		writer.close();
		output.close();
	}

	public static void findLargePolyCallMethod(String path, PrintWriter writer) {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator nameTablecreator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating name table, path = " + path);
		Debug.disable();
		NameTableManager nameTableManager = nameTablecreator.createNameTableManager();
		Debug.enable();
		Debug.time("End creating.....");
		
		Debug.time("Begin creating structure....");
		Debug.disable();
		SoftwareStructManager structManager = new SoftwareStructManager(nameTableManager);
		if (path.contains("ProgramAnalysis")) structManager.createSoftwareStructure();
		else structManager.readOrCreateSoftwareStructure();
		Debug.enable();
		Debug.time("End creating.....");

		class LargeMethodFilter extends NameTableFilter {

			@Override
			public boolean accept(NameDefinition definition) {
				if (!definition.isMethodDefinition()) return false;
//				return true;
				if (definition.getFullQualifiedName().equals("analyzer.qualitas.ClassMetricCollector.collectQualitasDetailedTypeMeasure")) return true;
//				if (definition.getSimpleName().equals("Axis") || definition.getSimpleName().equals("JFreeChart") || definition.getSimpleName().equals("Plot")) return true;
				else return false;
			}

		}
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new LargeMethodFilter());
		SystemScope rootScope = nameTableManager.getSystemScope();
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		LinkedList<MethodDefinition> largeMethodQueue = new LinkedList<MethodDefinition>();
		TreeSet<MethodDefinition> methodSet = new TreeSet<MethodDefinition>();
		
		for (NameDefinition definition : definitionList) {
			MethodDefinition method = (MethodDefinition)definition;
			largeMethodQueue.add(method);
			methodSet.add(method);
		}
		
		while (!largeMethodQueue.isEmpty()) {
			MethodDefinition method = largeMethodQueue.removeFirst();
			
			Debug.time("Scan method " + method.getFullQualifiedName());
			List<MethodDefinition> calledMethodList = structManager.getDirectPolymorphicInvocationMethodList(method);
			writer.println("Method " + method.getFullQualifiedName() + " direct polymorphically calls: ");
			for (MethodDefinition calledMethod : calledMethodList) {
				Set<MethodDefinition> calledMethodSet = structManager.getPolymorphicInvocationMethodSet(calledMethod);
				int size = calledMethodSet.size();
				writer.println("\tMethod " + calledMethod.getFullQualifiedName() + ", and its polymorphic calling set size " + size);
				if (size > 300) {
//					writer.println("\tMethod " + calledMethod.getFullQualifiedName() + ", and its polymorphic calling set size " + size);
					if (!methodSet.contains(calledMethod)) {
						largeMethodQueue.addLast(calledMethod);
						methodSet.add(calledMethod);
					}
				}
			}
		}
		
		writer.close();
	}
	
	public static void testTypeStructManager(String path, PrintWriter writer) {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator nameTablecreator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating name table, path = " + path);
		Debug.disable();
		NameTableManager nameTableManager = nameTablecreator.createNameTableManager();
		Debug.enable();
		Debug.time("End creating.....");
		
		Debug.time("Begin creating structure....");
		Debug.disable();
		SoftwareStructManager structManager = new SoftwareStructManager(nameTableManager);
		structManager.createSoftwareStructure();
		
		Debug.enable();
		Debug.time("End creating.....");
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new DetailedTypeDefinitionFilter(new NameDefinitionNameFilter("HelloWorld")));
		nameTableManager.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
//		int totalCounter = 0;
		
		Debug.time("Begin scan class....");
		Debug.disable();
		
		for (NameDefinition definition : definitionList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;

			Debug.time("Scan class: " + type.getFullQualifiedName());
			
			writer.println("Type: " + type.getFullQualifiedName());
			List<DetailedTypeDefinition> typeList = structManager.getAllParentTypeList(type);
			writer.print("\tAll Parent type: ");
			if (typeList != null) {
				for (DetailedTypeDefinition typeInList : typeList) {
					writer.print(typeInList.getSimpleName() + "  ");
				}
			}
			writer.println();

			List<DetailedTypeDefinition> detailedTypeList = structManager.getAncestorClassList(type);
			writer.print("\tDetailed Ancestor class: ");
			if (detailedTypeList != null) {
				for (TypeDefinition typeInList : detailedTypeList) {
					writer.print(typeInList.getSimpleName() + "  ");
				}
			}
			
			writer.println();
			typeList = structManager.getAllAncestorTypeList(type);
			writer.print("\tAll Ancestor type: ");
			if (typeList != null) {
				for (TypeDefinition typeInList : typeList) {
					writer.print(typeInList.getSimpleName() + "  ");
				}
			}
			writer.println();

			List<DetailedTypeDefinition> classList = structManager.getChildrenClassList(type);
			writer.print("\tChildren class: ");
			if (classList != null) {
				for (TypeDefinition classInList : classList) {
					writer.print(classInList.getSimpleName() + "  ");
				}
			}
			writer.println();
			
			classList = structManager.getAllDescendantTypeList(type);
			writer.print("\tDescendant type: ");
			if (classList != null) {
				for (TypeDefinition classInList : classList) {
					writer.print(classInList.getSimpleName() + "  ");
				}
			}
			writer.println();

			classList = structManager.getDescendantClassList(type);
			writer.print("\tDescendant class: ");
			if (classList != null) {
				for (TypeDefinition classInList : classList) {
					writer.print(classInList.getSimpleName() + "  ");
				}
			}
			writer.println();
			
			writer.println("\tInheritance summary: DIT = " +  structManager.getDepthOfInheritance(type) + ", AID = " + structManager.getAverageInheritanceDepth(type) + ", CLD = " + structManager.getTypeToLeafDepth(type));
			
			Set<DetailedTypeDefinition> classSet = structManager.getUsedOtherDetailedTypeDefinitionSet(type);
			writer.println("\tUsed other type set: ");
			if (classList != null) {
				for (TypeDefinition classInSet : classSet) {
					writer.println("\t\t" + classInSet.getFullQualifiedName());
				}
			}
			writer.println("\tTotal used other types: " + classSet.size());

//			Set<MethodDefinition> responseSet = structManager.getResponseSet(type);
//			writer.println("\tResponse set: ");
//			if (classList != null) {
//				for (MethodDefinition methodInSet : responseSet) {
//					writer.println("\t\t" + methodInSet.getUniqueId());
//				}
//			}
//			writer.println("\tResponse set size: " + responseSet.size());

			List<MethodDefinition> implementedMethodList = structManager.getImplementedMethodList(type);
			for (MethodDefinition method : implementedMethodList) {
				
				Debug.time("\tScan method " + method.getSimpleName());
				writer.println("\tMethod: " + method.getSimpleName());
				
				List<TypeDefinition> parameterTypeList = structManager.getParameterTypeList(method);
				writer.print("\t\tParameter type: ");
				if (parameterTypeList != null) {
					for (TypeDefinition typeInList : parameterTypeList) {
						writer.print(typeInList.getSimpleName() + "  ");
					}
				}
				writer.println(); 

				List<MethodWithCallInformation> calledMethodList = structManager.getDirectStaticInvocationMethodWithCallInformationList(method);
				writer.println("\t\tDirectly static call method: ");
				if (calledMethodList != null) {
					for (MethodWithCallInformation methodInList : calledMethodList) {
						writer.println("\t\t\t" + methodInList.getMethod().getFullQualifiedName() + "(" + methodInList.getCallNumber() + ")");
					}
					writer.println("\t\tTotal methods: " + calledMethodList.size());
				} else writer.println();

				calledMethodList = structManager.getDirectPolymorphicInvocationMethodWithCallInformationList(method);
				writer.println("\t\tDirectly polymorphic call method: ");
				if (calledMethodList != null) {
					for (MethodWithCallInformation methodInList : calledMethodList) {
						writer.println("\t\t\t" + methodInList.getMethod().getFullQualifiedName() + "(" + methodInList.getCallNumber() + ")");
					}
					writer.println("\t\tTotal methods: " + calledMethodList.size());
				} else writer.println();

				List<FieldDefinition> fieldList = structManager.getDirectFieldUsingList(method);
				writer.println("\t\tField using: ");
				if (fieldList != null) {
					for (FieldDefinition field : fieldList) {
						writer.println("\t\t\t" + field.getFullQualifiedName());
					}
					writer.println("\t\tTotal fields: " + fieldList.size());
				} else writer.println();
				
				Set<MethodDefinition> calledMethodSet = structManager.getStaticInvocationMethodSet(method);
				writer.println("\t\tIndirectly or directly static call method: ");
				if (calledMethodSet != null) {
					for (MethodDefinition methodInList : calledMethodSet) {
						writer.println("\t\t\t" + methodInList.getFullQualifiedName());
					}
					writer.println("\t\tTotal methods: " + calledMethodSet.size());
				} else writer.println();

				calledMethodSet = structManager.getPolymorphicInvocationMethodSet(method);
				writer.println("\t\tIndirectly or directly polymorphic call method: ");
				if (calledMethodSet != null) {
					for (MethodDefinition methodInList : calledMethodSet) {
						writer.println("\t\t\t" + methodInList.getFullQualifiedName());
					}
					writer.println("\t\tTotal methods: " + calledMethodSet.size());
				} 
				
			}
		}
		
		Debug.enable();
		Debug.time("End scan.....");
	}
	
	public static void testDetailedTypeUsingMatrix(String path, PrintWriter writer) {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		
		Debug.time("Begin creating detailed type using matrix....");
		int[][] detailedTypeUsingMatrix = structManager.getDetailedTypeUsingMatrix();
		List<DetailedTypeDefinition> classList = structManager.getAllDetailedTypeDefinition();
		Debug.time("Begin writing detailed type using matrix....");
		int totalCounter = 0;
		for (int i = 0; i < classList.size(); i++) {
			int counter = 0;
			for (int j = 0; j < detailedTypeUsingMatrix[i].length; j++) {
				counter = counter + detailedTypeUsingMatrix[i][j];
			}
			totalCounter = totalCounter + counter;
			writer.print(classList.get(i).getSimpleName() + "\t" + counter);
			for (int j = 0; j < detailedTypeUsingMatrix[i].length; j++) {
				if (detailedTypeUsingMatrix[i][j] == 1) {
					writer.print("\t" + classList.get(j).getSimpleName());
				}
			}
			writer.println();
		}
		Debug.time("End...., total class " + classList.size() + ", total counter " + totalCounter);
	}
	
	public static void testDetailedTypeMatrix(String path, PrintWriter writer) throws IOException {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new DetailedTypeDefinitionFilter());
		SystemScope rootScope = manager.getSystemScope();
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		Debug.setStart("Begin scan class....");
		for (NameDefinition definition : definitionList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			
			System.out.println("Scan class: " + type.getFullQualifiedName());
			
			Debug.println("Class: " + type.getFullQualifiedName());
			
			MethodInvocationMatrix invocationMatrix = structManager.createMethodInvocationMatrix(type, true, true);
			if (invocationMatrix != null) {
				Debug.println("Direct static method invocation matrix: ");
				invocationMatrix.print(Debug.getWriter());
				
				MethodInvocationGraph invocationGraph = new MethodInvocationGraph(invocationMatrix, "Direct static method invocation graph");
				GraphUtil.simplyWriteToDotFile(invocationGraph, writer);
			}
			
			FieldReferenceMatrix referenceMatrix = structManager.createFieldReferenceMatrix(type, true);
			if (referenceMatrix != null) {
				Debug.println("Direct field reference matrix: ");
				referenceMatrix.print(Debug.getWriter());
				FieldReferenceGraph referenceGraph = new FieldReferenceGraph(referenceMatrix, "Direct field reference graph");
				referenceGraph.simplyWriteToDotFile(writer);
			}
			
		}
		Debug.time("End scan.....");
	}
	
	public static void testMethodInvocations(String path, PrintWriter writer) {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new DetailedTypeDefinitionFilter());
		SystemScope rootScope = manager.getSystemScope();
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		Debug.setStart("Begin scan class....");
		for (NameDefinition definition : definitionList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			
			System.out.println("Scan class: " + type.getFullQualifiedName());
			
			writer.println("Class: " + type.getFullQualifiedName());
			
			List<TypeDefinition> fieldTypeList = structManager.getFieldTypeList(type);
			writer.print("Field types: ");
			for (TypeDefinition fieldType : fieldTypeList) writer.print(fieldType.getSimpleName() + "\t"); 
			writer.println();

			fieldTypeList = structManager.getImplementedFieldTypeList(type);
			writer.print("Implemented field types: ");
			for (TypeDefinition fieldType : fieldTypeList) writer.print(fieldType.getSimpleName() + "\t"); 
			writer.println();
			
			List<MethodDefinition> methodList = structManager.getImplementedMethodList(type);
			for (MethodDefinition method : methodList) {
				writer.print("\tMethod: " + method.getSimpleName() + "(");
				List<VariableDefinition> parameterList = structManager.getParameterList(method);
				if (parameterList.size() > 0) {
					VariableDefinition parameter = parameterList.get(0); 
					TypeDefinition parameterType = parameter.getTypeDefinition();
					String parameterTypeString = "";
					if (parameterType != null) parameterTypeString = parameterType.getSimpleName(); 
					writer.print(parameterTypeString + " " + parameter.getSimpleName());
					for (int index = 1; index < parameterList.size(); index++) {
						parameter = parameterList.get(index);
						parameterType = parameter.getTypeDefinition();
						parameterTypeString = "";
						if (parameterType != null) parameterTypeString = parameterType.getSimpleName();  
						writer.print(", " + parameterTypeString + " " + parameter.getSimpleName());
					}
				}
				writer.println(")");

				List<TypeDefinition> parameterTypeList = structManager.getParameterTypeList(method);
				writer.print("\t\tParameter types: ");
				for (TypeDefinition parameterType : parameterTypeList) writer.print(parameterType.getSimpleName() + "\t");
				writer.println();
				
				List<MethodDefinition> calleeList = structManager.getDirectStaticInvocationMethodList(method);
				writer.println("\t\tDirect static invocation methods: ");
				for (MethodDefinition callee : calleeList) {
					int callNumber = structManager.getDirectStaticInvocationNumber(method, callee);
					writer.println("\t\t\t" + callee.getFullQualifiedName() + "(" + callNumber + ")\t");
				}
				writer.println("\t\tTotal " + calleeList.size());
				
				calleeList = structManager.getDirectPolymorphicInvocationMethodList(method);
				writer.println("\t\tDirect polymorphic invocation methods: ");
				for (MethodDefinition callee : calleeList) {
					int callNumber = structManager.getDirectPolymorphicInvocationNumber(method, callee);
					writer.println("\t\t\t" + callee.getFullQualifiedName() + "(" + callNumber + ")\t");
				}
				writer.println("\t\tTotal " + calleeList.size());
				
				Set<MethodDefinition> calleeSet = structManager.getStaticInvocationMethodSet(method);
				writer.println("\t\tDirect or indirect static invocation methods: ");
				for (MethodDefinition callee : calleeSet) {
					writer.println("\t\t\t" + callee.getFullQualifiedName() + "\t");
				}
				writer.println("\t\tTotal " + calleeSet.size());
				
				calleeSet = structManager.getPolymorphicInvocationMethodSet(method);
				writer.println("\t\tDirect or indirect polymorphic invocation methods: ");
				for (MethodDefinition callee : calleeSet) {
					writer.println("\t\t\t" + callee.getFullQualifiedName() + "\t");
				}
				writer.println("\t\tTotal " + calleeSet.size());
				
				List<FieldDefinition> fieldList = structManager.getDirectFieldUsingList(method);
				writer.print("\t\tUse fields: ");
				for (FieldDefinition field : fieldList) writer.print(field.getSimpleName() + "\t");
				writer.println();
				
				fieldList = structManager.getDirectImplementedFieldReferencesInMethod(method);
				writer.print("\t\tUse implemented fields: ");
				for (FieldDefinition field : fieldList) writer.print(field.getSimpleName() + "\t");
				writer.println();
				
			}
		}
		Debug.time("End scan.....");
	}
	
	public static void testInheritanceInformation(String path, PrintWriter writer) {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new DetailedTypeDefinitionFilter());
		SystemScope rootScope = manager.getSystemScope();
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		Debug.setStart("Begin scan class....");
		for (NameDefinition definition : definitionList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			
			System.out.println("Scan class: " + type.getFullQualifiedName());
			
			writer.println("Class: " + type.getFullQualifiedName());
			
			DetailedTypeDefinition parentClass = structManager.getParentClass(type);
			if (parentClass == null) writer.println("\tNo parent class!");
			else writer.println("\tParent class: " + parentClass.getSimpleName());
			
			List<DetailedTypeDefinition> parentList = structManager.getAllParentTypeList(type);
			if (parentList.size() <= 0) writer.println("\tNo parent class or interface!");
			else {
				writer.print("\tParent class or interface: ");
				for (TypeDefinition parentType : parentList) {
					writer.print("\t" + parentType.getSimpleName());
				}
				writer.println();
			}
			
			List<DetailedTypeDefinition> ancestorClassList = structManager.getAncestorClassList(type);
			if (ancestorClassList.size() <= 0) writer.println("\tNo ancestor class!");
			else {
				writer.print("\tAncestor class: ");
				for (DetailedTypeDefinition ancestor : ancestorClassList) {
					writer.print("\t" + ancestor.getSimpleName());
				}
				writer.println();
			}

			List<DetailedTypeDefinition> ancestorList = structManager.getAllAncestorTypeList(type);
			if (ancestorList.size() <= 0) writer.println("\tNo ancestor class or interface!");
			else {
				writer.print("\tAncestor class or interface: ");
				for (DetailedTypeDefinition ancestor : ancestorList) {
					writer.print("\t" + ancestor.getSimpleName());
				}
				writer.println();
			}

			List<DetailedTypeDefinition> childrenClassList = structManager.getChildrenClassList(type);
			if (childrenClassList.size() <= 0) writer.println("\tNo children class!");
			else {
				writer.print("\tChildren class: ");
				for (DetailedTypeDefinition children : childrenClassList) {
					writer.print("\t" + children.getSimpleName());
				}
				writer.println();
			}

			List<DetailedTypeDefinition> childrenList = structManager.getAllChildrenTypeList(type);
			if (childrenList.size() <= 0) writer.println("\tNo children class or interface!");
			else {
				writer.print("\tChildren class or interface: ");
				for (DetailedTypeDefinition children : childrenList) {
					writer.print("\t" + children.getSimpleName());
				}
				writer.println();
			}

			List<DetailedTypeDefinition> descendantClassList = structManager.getDescendantClassList(type);
			if (descendantClassList.size() <= 0) writer.println("\tNo descendant class!");
			else {
				writer.print("\tDescendant class: ");
				for (DetailedTypeDefinition desendant : descendantClassList) {
					writer.print("\t" + desendant.getSimpleName());
				}
				writer.println();
			}
			
			List<DetailedTypeDefinition> descendantList = structManager.getAllDescendantTypeList(type);
			if (descendantList.size() <= 0) writer.println("\tNo descendant class or interface!");
			else {
				writer.print("\tDescendant class or interface: ");
				for (DetailedTypeDefinition desendant : descendantList) {
					writer.print("\t" + desendant.getSimpleName());
				}
				writer.println();
			}
		}
		Debug.time("End scan.....");
	}
	
	public static void testMemberInformation(String path, PrintWriter writer) {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new DetailedTypeDefinitionFilter());
		SystemScope rootScope = manager.getSystemScope();
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		Debug.setStart("Begin scan class....");
		for (NameDefinition definition : definitionList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			
			System.out.println("Scan class: " + type.getFullQualifiedName());
			
			writer.println("Class: " + type.getFullQualifiedName());
			
			List<MethodDefinition> methodList = structManager.getImplementedMethodList(type);
			if (methodList != null) {
				writer.print("\tImplemented methods: ");
				for (MethodDefinition method : methodList) writer.print(method.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo implemented methods!");
			
			methodList = structManager.getDeclaredMethodList(type);
			if (methodList != null) {
				writer.print("\tDeclared methods: ");
				for (MethodDefinition method : methodList) writer.print(method.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo declared methods!");
			
			methodList = structManager.getAllInheritedMethodList(type);
			if (methodList != null) {
				writer.print("\tAll inherited methods: ");
				for (MethodDefinition method : methodList) writer.print(method.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo all inherited methods!");

			methodList = structManager.getInheritedMethods(type);
			if (methodList != null) {
				writer.print("\tInherited methods: ");
				for (MethodDefinition method : methodList) writer.print(method.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo inherited methods!");

			methodList = structManager.getOverriddenMethods(type);
			if (methodList != null) {
				writer.print("\tOverrided methods: ");
				for (MethodDefinition method : methodList) writer.print(method.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo overrided methods!");

			methodList = structManager.getNewMethods(type);
			if (methodList != null) {
				writer.print("\tNew methods: ");
				for (MethodDefinition method : methodList) writer.print(method.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo new methods!");

			methodList = structManager.getImplementedPublicMethodList(type);
			if (methodList != null) {
				writer.print("\tImplemented public methods: ");
				for (MethodDefinition method : methodList) writer.print(method.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo implmented public methods!");

			methodList = structManager.getDeclaredPublicMethodList(type);
			if (methodList != null) {
				writer.print("\tDeclared public methods: ");
				for (MethodDefinition method : methodList) writer.print(method.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo delcared public methods!");

			methodList = structManager.getPublicMethodList(type);
			if (methodList != null) {
				writer.print("\tPublic methods: ");
				for (MethodDefinition method : methodList) writer.print(method.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo public methods!");

			methodList = structManager.getNonPublicMethodList(type);
			if (methodList != null) {
				writer.print("\tNon-Public methods: ");
				for (MethodDefinition method : methodList) writer.print(method.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo non-public methods!");
			
			List<FieldDefinition> fieldList = structManager.getImplementedFieldList(type);
			if (fieldList != null) {
				writer.print("\tImplemented fields: ");
				for (FieldDefinition field : fieldList) writer.print(field.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo implemented fields!");

			fieldList = structManager.getDeclaredFieldList(type);
			if (fieldList != null) {
				writer.print("\tDeclared fields: ");
				for (FieldDefinition field : fieldList) writer.print(field.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo delcared fields!");

			fieldList = structManager.getAllFieldList(type);
			if (fieldList != null) {
				writer.print("\tAll fields: ");
				for (FieldDefinition field : fieldList) writer.print(field.getSimpleName() + "\t");
				writer.println();
			} else writer.println("\tNo all fields!");
		}
		Debug.time("End scan.....");
	}
}



