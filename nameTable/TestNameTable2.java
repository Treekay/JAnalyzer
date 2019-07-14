package nameTable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import analyzer.dataTable.DataTableManager;
import analyzer.dataTable.DataTableUtil;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.DetailedTypeDefinitionFilter;
import nameTable.filter.NameDefinitionNameFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.TypeReference;
import nameTable.visitor.NameDefinitionFinder;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2016年11月26日
 * @version 1.0
 *
 */
public class TestNameTable2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String rootPath = "C:\\";

		
		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\ProgramAnalysis\\src\\", rootPath + "ZxcWork\\JAnalyzer\\src\\", 
							rootPath + "ZxcWork\\ToolKit\\src\\", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
							rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
							rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
							rootPath + "ZxcTools\\ArgoUml\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
							rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};
		
		String path = paths[3];
		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = new PrintWriter(System.out);
		
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);

			printMemberOfDetailedType(path, result);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		writer.close();
		output.close();
	}

	/**
	 * Print fields and methods of top-level types to the given resultFile, which can be used to as a tabular in LaTeX document.
	 */
	public static void printMemberOfDetailedType(String path, String resultFile) throws IOException {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);
		String[] fileNameArray = {"C:\\ZxcWork\\ToolKit\\data\\javalang.txt", "C:\\ZxcWork\\ToolKit\\data\\javautil.txt", "C:\\ZxcWork\\ToolKit\\data\\javaio.txt", }; 

		NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out), fileNameArray);
		
		String typeName = "ControlFlowGraph";
		NameDefinitionFinder finder = new NameDefinitionFinder(new DetailedTypeDefinitionFilter(new NameDefinitionNameFilter(typeName)));
		manager.accept(finder);

		DetailedTypeDefinition type = (DetailedTypeDefinition)finder.getResult();
		if (type == null) {
			System.out.println("Can not find type " + typeName);
			return;
		}
		
		List<DetailedTypeDefinition> ancestorTypeList = getAllAncestorTypeList(type);
		
		PrintWriter writer = new PrintWriter(new File(resultFile));
		
		DataTableManager dataTable = new DataTableManager("type");
		String[] columnNameArray = {"描述"};
		dataTable.setColumnNames(columnNameArray);
		String[] lineArray = null;
		
		FieldComparator fieldComparator = new FieldComparator();
		MethodComparator methodComparator = new MethodComparator();

		// Print the field declared by this type!
		List<FieldDefinition> fieldList = type.getFieldList();
		if (fieldList != null) {
			lineArray = new String[1];
			lineArray[0] = "{\\heiti 类\\pname{" + type.getSimpleName() + "}本身声明的字段}";
			dataTable.appendLine(lineArray);

			fieldList.sort(fieldComparator);
			for (FieldDefinition field : fieldList) {
				lineArray = new String[1];
				lineArray[0] = getFieldSignature(field);
				dataTable.appendLine(lineArray);
			}
		}
		
		// Print the fields inherited from ancestor type
		for (DetailedTypeDefinition ancestorType : ancestorTypeList) {
			fieldList = ancestorType.getFieldList();
			if (fieldList != null) {
				boolean hasInheritedField = false;
				for (FieldDefinition field : fieldList) {
					if (!field.isPrivate() && !field.isStatic()) {
						// Only those non-private and non-static field can be inherited!
						hasInheritedField = true;
						break;
					}
				}
				
				if (hasInheritedField) {
					lineArray = new String[1];
					lineArray[0] = "{\\heiti 继承自类\\pname{" + ancestorType.getSimpleName() + "}的字段}";
					dataTable.appendLine(lineArray);

					fieldList.sort(fieldComparator);
					for (FieldDefinition field : fieldList) {
						if (!field.isPrivate() && !field.isStatic()) {
							// Only those non-private and non-static field can be inherited!

							lineArray = new String[1];
							lineArray[0] = getFieldSignature(field);
							dataTable.appendLine(lineArray);
						}
					}
				}

			}
		}

		List<MethodDefinition> methodList = type.getMethodList();
		if (methodList != null) {
			lineArray = new String[1];
			lineArray[0] = "{\\heiti 类\\pname{" + type.getSimpleName() + "}本身声明的方法}";
			dataTable.appendLine(lineArray);

			methodList.sort(methodComparator);
			for (MethodDefinition method : methodList) {
				// Do not print auto generated constructor!
				if (method.isAutoGenerated()) continue;
				
				lineArray = new String[1];
				String methodString = getMethodSignature(method);
				// Test if this method override a method of ancestor types.
				DetailedTypeDefinition overrideType = getDetailedTypeWithOverridedMethod(method, ancestorTypeList);
				String overrideMessage = "";
			
				if (overrideType != null) {
					if (type.isInterface()) {
						overrideMessage = "~~[重定义接口\\pname{" + overrideType.getSimpleName() + "}的方法]";
					} else {
						if (overrideType.isInterface()) overrideMessage = "~~[实现接口\\pname{" + overrideType.getSimpleName() + "}的方法]";
						else overrideMessage = "~~[重定义类\\pname{" + overrideType.getSimpleName() + "}的方法]";
					}
				}
				lineArray[0] = methodString + overrideMessage;
				dataTable.appendLine(lineArray);
			}
		}

		// For those sub types we have printed.
		List<DetailedTypeDefinition> printedTypeList = new ArrayList<DetailedTypeDefinition>();
		printedTypeList.add(type);
		
		for (DetailedTypeDefinition ancestorType : ancestorTypeList) {
			// If the current type is not an interface while the ancestor type is an interface, we need not 
			// to print any methods in the ancestor type, since all methods of this ancestor type must be 
			// implemented (overridden) by the current type or its an ancestor class. 
			if (!type.isInterface() && ancestorType.isInterface()) continue;
			
			methodList = ancestorType.getMethodList();
			if (methodList != null) {
				boolean hasInheritedMethod = false;
				for (MethodDefinition method : methodList) {
					if (!method.isPrivate() && !method.isStatic() && !method.isConstructor()) {
						// Only those non-private, non-static and non-constructor method can be inherited. 
						hasInheritedMethod = true;
						break;
					}
				}

				if (hasInheritedMethod) {
					lineArray = new String[1];
					if (ancestorType.isInterface()) {
						lineArray[0] = "{\\heiti 继承自接口\\pname{" + ancestorType.getSimpleName() + "}的方法（不含已被重定义方法）}";
					} else lineArray[0] = "{\\heiti 继承自类\\pname{" + ancestorType.getSimpleName() + "}的方法（不含已被重定义方法）}";
					dataTable.appendLine(lineArray);
					
					methodList.sort(methodComparator);
					for (MethodDefinition method : methodList) {
						if (!method.isPrivate() && !method.isStatic() && !method.isConstructor()) {
							// If this method is overridden by a method in the type of printed type list, i.e. a sub type of the current
							// ancestor type, we do not print this method again (since it is overridden by a method of its sub type. 
							if (isOverridedByMethodInType(method, printedTypeList)) continue;
							
							lineArray = new String[1];
							lineArray[0] = getMethodSignature(method);
							dataTable.appendLine(lineArray);
						}
					}
					printedTypeList.add(ancestorType);
				}
			}
		}
		
		DataTableUtil.writeDataLinesAsLatexTableLines(writer, dataTable, columnNameArray, 1);
		writer.close();
	}

	public static String getFieldSignature(FieldDefinition field) {
		return "\\pname{" + field.getSimpleName() + "} : \\pname{" + field.getType().toDeclarationString() + "}";
	}
	
	public static String getMethodSignature(MethodDefinition method) {
		StringBuffer methodString = new StringBuffer("\\pname{" + method.getSimpleName() + "(");
		
		List<VariableDefinition> vars = method.getParameterList();
		if (vars != null) {
			boolean isFirst = true;
			for (VariableDefinition var : vars) {
				if (isFirst) {
					methodString.append(var.getType().toDeclarationString());
					isFirst = false;
				} else methodString.append(", " + var.getType().toDeclarationString());
			}
		}
		TypeReference returnType = method.getReturnType();
		if (returnType != null) {
			methodString.append(") : " + method.getReturnType().toDeclarationString());
		} else {
			if (method.isConstructor()) {
				methodString.append(") : Constructor");
			} else {
				methodString.append(")");
			}
		}
		methodString.append("}");
		return methodString.toString();
	}
	
	/**
	 * Get all ancestor detailed type definition (not include imported type definition) for the given type.
	 */
	public static List<DetailedTypeDefinition> getAllAncestorTypeList(DetailedTypeDefinition type) {
		List<DetailedTypeDefinition> resultList = new ArrayList<DetailedTypeDefinition>();
		
		List<TypeReference> superList = type.getSuperList();
		if (superList == null) return resultList;
		
		for (TypeReference superType : superList) {
			if (superType.resolveBinding()) {
				if (superType.getDefinition().isDetailedType()) {
					DetailedTypeDefinition superTypeDefinition = (DetailedTypeDefinition)superType.getDefinition(); 
					resultList.add(superTypeDefinition);
					List<DetailedTypeDefinition> superResultList = getAllAncestorTypeList(superTypeDefinition);
					resultList.addAll(superResultList);
				}
			}
		}
		return resultList;
	}
	
	
	/**
	 * Given a method definition and super detailed type definition list of the type with this method definition, test if
	 * there a super detailed type definition which has a method overridden by the given method. If so, return this super 
	 * detailed type definition, otherwise return null.
	 */
	public static DetailedTypeDefinition getDetailedTypeWithOverridedMethod(MethodDefinition method, List<DetailedTypeDefinition> superTypeList) {
		for (DetailedTypeDefinition superType : superTypeList) {
			List<MethodDefinition> superMethodList = superType.getMethodList();
			if (superMethodList != null) {
				for (MethodDefinition superMethod : superMethodList) {
					if (method.isOverrideMethod(superMethod)) return superType;
				}
			}
		}
		return null;
	}
	
	/**
	 * Given a method definition and a list of sub type of the type with this method, test if the method is overrode by a method 
	 * in a sub type.
	 */
	public static boolean isOverridedByMethodInType(MethodDefinition method, List<DetailedTypeDefinition> subTypeList) {
		for (DetailedTypeDefinition subType : subTypeList) {
			List<MethodDefinition> subMethodList = subType.getMethodList();
			if (subMethodList != null) {
				for (MethodDefinition subMethod : subMethodList) {
					if (subMethod.isOverrideMethod(method)) return true;
				}
			}
		}
		return false;
	}
}

class FieldComparator implements Comparator<FieldDefinition> {

	@Override
	public int compare(FieldDefinition one, FieldDefinition two) {
		if (one.isStatic() && !two.isStatic()) return -1;
		else if (two.isStatic() && !one.isStatic()) return 1;
		return one.getSimpleName().compareTo(two.getSimpleName());
	}
}

class MethodComparator implements Comparator<MethodDefinition> {

	@Override
	public int compare(MethodDefinition one, MethodDefinition two) {
		if (one.isStatic() && !two.isStatic()) return -1;
		else if (two.isStatic() && !one.isStatic()) return 1;
		
		if (one.isConstructor() && !two.isConstructor()) return -1;
		else if (two.isConstructor() && !one.isConstructor()) return 1;

		return one.getSimpleName().compareTo(two.getSimpleName());
	}
}
