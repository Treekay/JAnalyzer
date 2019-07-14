package sourceCodeAST;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.eclipse.jdt.core.dom.CompilationUnit;

import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê9ÔÂ28ÈÕ
 * @version 1.0
 *
 */
public class TestSourceCodeFileSet {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String rootPath = "C:\\";
		
		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\ProgramAnalysis\\src\\", rootPath + "ZxcTools\\EclipseSource\\org\\", 
							rootPath + "ZxcWork\\ToolKit\\src\\", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
							rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
							rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
							rootPath + "ZxcTools\\ArgoUml\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
							rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};
		
		String path = paths[2];
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
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		testGetAllUnitName(path, writer);
//		testGetAllContents(path, writer);
//		testGetAllASTs(path, writer);

//		testGetAllASTsByForEach(path, writer);
		
		writer.close();
		output.close();
	}
	
	/**
	 * Demo how to get all unit name by using iterator explicitly and by using for-each loop 
	 */
	public static void testGetAllUnitName(String startPath, PrintWriter writer) {
		SourceCodeFileSet fileSet = new SourceCodeFileSet(startPath);
		SourceCodeFileSetIterator iterator = fileSet.iterator();
		System.out.println("Use iterator explicitly....");
		while (iterator.hasNext()) {
			SourceCodeFile codeFile = iterator.next();
			System.out.println("Source Code File: " + codeFile.getFileFullName());
			writer.println("Current unit name: " + iterator.getCurrentFileUnitName());
		}
		System.out.println("Total files: " + fileSet.getFileNumber());
		System.out.println("Use for each loop ....");
		for (SourceCodeFile codeFile : fileSet) {
			System.out.println("Source Code File: " + codeFile.getFileFullName());
			writer.println("Unit name: " + fileSet.getFileUnitName(codeFile));
		}
	}

	/**
	 * Demo how to get all contents by using iterator explicitly 
	 */
	public static void testGetAllContents(String startPath, PrintWriter writer) {
		SourceCodeFileSet fileSet = new SourceCodeFileSet(startPath);
		SourceCodeFileSetIterator iterator = fileSet.iterator();
		while (iterator.hasNext()) {
			SourceCodeFile codeFile = iterator.next();
			String content = codeFile.getFileContent();
			
			System.out.println("Source Code File: " + codeFile.getFileFullName());
			
			writer.println("Source Code File: " + codeFile.getFileFullName() + ", unit name: " + fileSet.getFileUnitName(codeFile));
			writer.println("\t" + content.substring(0, 60) + ", ...");
			writer.println("\tTotal line: " + codeFile.getTotalLines() + ", total space: " + codeFile.getTotalSpaces());
		}
		
		System.out.println("Total files: " + fileSet.getFileNumber());
		fileSet.releaseAllFileContents();
	}

	/**
	 * Demo how to get all ASTs by using iterator explicitly 
	 */
	public static void testGetAllASTs(String startPath, PrintWriter writer) {
		SourceCodeFileSet fileSet = new SourceCodeFileSet(startPath);
		SourceCodeFileSetIterator iterator = fileSet.iterator();
		
		while (iterator.hasNext()) {
			SourceCodeFile codeFile = iterator.next();
			System.out.println("Source Code File: " + codeFile.getFileFullName());
			writer.println("Source Code File: " + codeFile.getFileFullName() + ", unit name: " + fileSet.getFileUnitName(codeFile));
			if (codeFile.hasCreatedAST()) {
				CompilationUnit root = codeFile.getASTRoot();
				writer.println("\tCreate AST successful, root not type is " + root.getNodeType());
			} else {
				String errorMessage = codeFile.getParsingErrorMessage();
				writer.println("\tError in creating AST: " + errorMessage);
			}
		}
		System.out.println("Total files: " + fileSet.getFileNumber());
		fileSet.releaseAllASTs();
	}
	
	/**
	 * Demo how to get all ASTs by using for-each loop 
	 */
	public static void testGetAllASTsByForEach(String startPath, PrintWriter writer) {
		SourceCodeFileSet fileSet = new SourceCodeFileSet(startPath);
		for (SourceCodeFile codeFile : fileSet) {
			System.out.println("Source Code File: " + codeFile.getFileFullName());
			writer.println("Source Code File: " + codeFile.getFileFullName() + ", unit name: " + fileSet.getFileUnitName(codeFile));
			
			if (codeFile.hasCreatedAST()) {
				CompilationUnit root = codeFile.getASTRoot();
				writer.println("\tCreate AST successful, root not type is " + root.getNodeType());
			} else {
				String errorMessage = codeFile.getParsingErrorMessage();
				writer.println("\tError in creating AST: " + errorMessage);
			}
		}
		System.out.println("Total files: " + fileSet.getFileNumber());
		fileSet.releaseAllASTs();
	}
	
}
