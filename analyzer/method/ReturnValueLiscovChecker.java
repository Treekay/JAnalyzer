package analyzer.method;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nameTable.NameTableManager;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.ImportedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.TypeReference;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.SourceCodeLocation;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2017年12月14日
 * @version 1.0
 *
 */
public class ReturnValueLiscovChecker {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String rootPath = "C:\\";

		
		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\JAnalyzer\\src\\", rootPath + "ZxcTools\\EclipseSource\\org\\", rootPath + "ZxcTemp\\JAnalyzer\\src\\",
							rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\HelloWorld.java", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
							rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
							rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
							rootPath + "ZxcTools\\ArgoUML-0.34-src\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
							rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};
		
		String path = paths[11];
		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = new PrintWriter(System.out);
		
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
			Debug.setScreenOn();

			String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\ArgoUMLLiscov.txt";
			writer = new PrintWriter(new FileOutputStream(new File(result)));

			Debug.setStart("Begin checking....");
			checkLiscovPrinciple(path, writer);
			Debug.time("After checking...");

			writer.close();
			output.close();
		} catch (Exception exc) {
			exc.printStackTrace();
			writer.close();
			output.close();
		}
	}
	
	public static void checkLiscovPrinciple(String path, PrintWriter writer) throws IOException {
		NameTableManager manager = NameTableManager.createNameTableManager(path);
		ReturnValueAnalyzer analyzer = new ReturnValueAnalyzer(manager);

		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		manager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();

		analyzer.analyze(methodList);

		System.out.println("To Check " + methodList.size() + " methods!");
		
		int counter = 0;
		int total = 0;
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
			
			System.out.println("Checking " + method.getFullQualifiedName());
			
			if (method.isConstructor()) continue;
			if (method.isAbstract()) continue;
			if (analyzer.isReturnPrimitiveValue(method)) continue;
			TypeDefinition enclosingType = method.getEnclosingType();
			if (!enclosingType.isDetailedType() || enclosingType.isAnonymous()) continue;
			DetailedTypeDefinition type = (DetailedTypeDefinition)enclosingType;
			
			SourceCodeLocation location = method.getLocation();
			TypeReference returnType = method.getReturnType();
			boolean nullable = analyzer.isPossibleReturnNull(method);
			
			List<MethodDefinition> overrideMethodList = manager.getSystemScope().getAllOverrideMethods((DetailedTypeDefinition)type, method);
			if (overrideMethodList.size() > 0) {
				total++;
			}
			boolean nonLiscov = false;
			StringBuilder message = new StringBuilder();
			for (MethodDefinition overrideMethod : overrideMethodList) {
				boolean overrideNullable = analyzer.isPossibleReturnNull(overrideMethod);
				SourceCodeLocation overrideLocation = overrideMethod.getLocation();
				TypeReference overrideReturnType = overrideMethod.getReturnType();
				
				if (overrideNullable != nullable) {
					message.append("\t**" + overrideMethod.getSimpleName() + "() : " + overrideReturnType.toDeclarationString() + " [" + overrideLocation.getUniqueId() + "], Nullable: " + overrideNullable + "\r\n");
					nonLiscov = true;
				} else {
					message.append("\t" + overrideMethod.getSimpleName() + "() : " + overrideReturnType.toDeclarationString() + " [" + overrideLocation.getUniqueId() + "], Nullable: " + overrideNullable + "\r\n");
				}
			}
			if (nonLiscov) {
				if (nullable) {
					writer.println("-->" + method.getSimpleName() + "() : " + returnType.toDeclarationString() + " [" + location.getUniqueId() + "], Nullable: " + nullable);
				} else {
					if (type.isInterface()) {
						writer.println("***" + method.getSimpleName() + "() : " + returnType.toDeclarationString() + " [" + location.getUniqueId() + "], Nullable: " + nullable);
					} else {
						writer.println("==>" + method.getSimpleName() + "() : " + returnType.toDeclarationString() + " [" + location.getUniqueId() + "], Nullable: " + nullable);
					}
				}
				writer.println(message.toString());
				if (!type.isInterface()) counter++;
			} else {
				if (overrideMethodList.size() > 0) {
					writer.println(method.getSimpleName() + "() : " + returnType.toDeclarationString() + " [" + location.getUniqueId() + "], Nullable: " + nullable);
					writer.println(message.toString());
				}
			}
		}
		
		System.out.println("Total " + methodList.size() + " methods , and " + total + " methods have override methods, and there " + counter + " method(s) do not obey Liscov principle on return null value!");
		writer.println("Total " + methodList.size() + " methods , and " + total + " methods have override methods, and there " + counter + " method(s) do not obey Liscov principle on return null value!");
	}

}
