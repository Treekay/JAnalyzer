package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameReferenceCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.visitor.NameDefinitionPrinter;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.SourceCodeFile;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ17ÈÕ
 * @version 1.0
 */
public class NameTableTester {
	public static void main(String[] args) {
		testNameTableCreator();
//		testFileContents();
	}
	
	public static void testNameTableCreator() {
		String result = QualitasPathsManager.defaultRootPath + "QualitasError.txt";
		PrintWriter writer = new PrintWriter(System.out);
		
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
			Debug.setWriter(writer);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		String[] systemNames = QualitasPathsManager.getSystemNames();
		int[] errorUnits = new int[systemNames.length];
		Debug.setScreenOn();
		for (int index = 0; index < systemNames.length-1; index++) {
			errorUnits[index] = testNameTableCreator(systemNames[index]);
		}
		Debug.println("The following are systems which have compiling errors!");
		for (int index = 0; index < errorUnits.length; index++) {
			if (errorUnits[index] > 0) {
				Debug.println("System " + systemNames[index] + ", " + errorUnits[index] + " errors!");
			}
		}
		Debug.flush();
		writer.close();
	}
	
	public static int testNameTableCreator(String systemName) {
		if (systemName.equals("eclipse_SDK") || systemName.equals("jre")) return 0;
		
		String[] fileNameArray = {"C:\\ZxcWork\\ToolKit\\data\\javalang.txt", "C:\\ZxcWork\\ToolKit\\data\\javautil.txt", "C:\\ZxcWork\\ToolKit\\data\\javaio.txt", }; 
		
		String[] versions = QualitasPathsManager.getSystemVersions(systemName);
		int totalErrorUnitNumber = 0;
		
		for (int index = 0; index < versions.length; index++) {
			String path = QualitasPathsManager.getSystemPath(systemName, versions[index]);
			String errorFileName = path + "error.txt";
			String typeListFileName = path + "typelist.txt"; 
			String definitionFileName = path + "definition.txt";
			String referenceFileName = path + "reference.txt";
			
			SourceCodeFileSet parser = new SourceCodeFileSet(path);
			NameTableCreator creator = new NameDefinitionCreator(parser);

			Debug.println("System, path = " + path);
			try {
				NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out), fileNameArray);
				PrintWriter writer = null;
				if (creator.hasError()) {
					writer = new PrintWriter(new File(errorFileName));
					int errorUnitNumber = creator.getErrorUnitNumber();
					if (errorUnitNumber > 1) {
						Debug.println("There are " + errorUnitNumber + " ERROR compilation unit files!");
					} else if (errorUnitNumber > 0) {
						Debug.println("There is " + errorUnitNumber + " ERROR compilation unit file!");
					} 
					totalErrorUnitNumber += errorUnitNumber;
					creator.printErrorUnitList(writer);
					writer.close();
				} else {
					Debug.println("There is no error compilation unit file!");
				}
				writer = new PrintWriter(new File(typeListFileName));
				printTypeDefinition(manager, writer);
				writer.close();
				writer = new PrintWriter(new File(definitionFileName));
				printAllDefinitions(manager, writer);
				writer.close();
				writer = new PrintWriter(new File(referenceFileName));
				printAllReferences(manager, writer);
				writer.close();
			} catch (AssertionError error) {
				String message = error.getMessage();
				Debug.println(message);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			Debug.flush();
		}
		return totalErrorUnitNumber;
	}
	
	public static void printTypeDefinition(NameTableManager manager, PrintWriter writer) {
		NameDefinitionFilter filter = new NameDefinitionFilter();
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(filter);

		manager.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		int size = definitionList.size();
		int counter = 0;
		
		writer.println("Type\tSourceFile\tTopLevel");
		for (NameDefinition definition : definitionList) {
			Debug.println("Total type " + size + ", write type " + counter + ", " + definition.getFullQualifiedName());
			counter++;
			
			TypeDefinition type = (TypeDefinition)definition;
			CompilationUnitScope unitScope = manager.getEnclosingCompilationUnitScope(type);
			String topLevel = "nontop";
			if (type.isPackageMember()) {
				if (type.isPublic()) topLevel = "public";
				else topLevel = "top-nonpublic";
			}
			writer.println(type.getFullQualifiedName() + "\t" + unitScope.getUnitName() + "\t" + topLevel);
		}
	}
	
	public static void printAllDefinitions(NameTableManager manager, PrintWriter writer) throws IOException {
		NameDefinitionPrinter printer = new NameDefinitionPrinter(writer);
		printer.setPrintVariable(true);
		manager.accept(printer);
		printer.close();
	}

	public static void printAllReferences(NameTableManager manager, PrintWriter writer) throws IOException {
		NameTableFilter filter = new NameDefinitionFilter();
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(filter);
		manager.accept(visitor);
		List<NameDefinition> typeList = visitor.getResult();
		NameReferenceCreator referenceCreator = new NameReferenceCreator(manager);

		Debug.time("Begin ....!");
		int size = typeList.size();
		int counter = 0;
		writer.println("Type\tName\tLocation\tKind\tScope\tDefinition"); 
		for (NameDefinition definition : typeList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			Debug.println("Total type " + size + ", scan type " + counter + ", " + type.getFullQualifiedName());
			counter++;
			
			List<NameReference> referenceList = referenceCreator.createReferences(type);
			for (NameReference reference : referenceList) {
				reference.resolveBinding();
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				for (NameReference leafReference : leafReferenceList) {
					if (leafReference.isLiteralReference()) continue;
					
					String name = leafReference.getName();
					String location = leafReference.getLocation().toString();
					String kind = leafReference.getReferenceKind() + "";
					String scopeName = leafReference.getScope().getScopeName();
					NameDefinition bindDef = leafReference.getDefinition();
					
					String bindString = "~~";
					if (bindDef != null) bindDef.getUniqueId();
					writer.println(type.getFullQualifiedName() + "\t" + name + "\t" + location + "\t" + kind + "\t" + scopeName + "\t" + bindString); 
				}
			}
		}
		Debug.time("End....!");
	}
	
	private static int currentLineNumber = 0;
	
	public static void testFileContents() {
		String debugFile = QualitasPathsManager.defaultDebugPath + "debug.txt";
		try {
			PrintWriter output = new PrintWriter(new FileOutputStream(new File(debugFile)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
			String systemPath = QualitasPathsManager.getSystemStartPath(systemName);
			String result = systemPath + "fileInfo.txt";
			PrintWriter writer = new PrintWriter(System.out);
			try {
				writer = new PrintWriter(new FileOutputStream(new File(result)));
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			testFileContents(systemName, writer);
			writer.close();
		}
	}
	
	public static void testFileContents(String systemName, PrintWriter writer) {
		System.out.println("Check system " + systemName);
		String[] versions = QualitasPathsManager.getSystemVersions(systemName);
		for (int index = versions.length-1; index >= versions.length-1; index--) {
			int totalFiles = 0;
			int problemFiles = 0;
			String path = QualitasPathsManager.getSystemPath(systemName, versions[index]);
			System.out.println("Check path " + path);
			Debug.println("Scan files in path = " + path);
			writer.println("Path: " + path);
			
			SourceCodeFileSet parser = new SourceCodeFileSet(path);
			for (SourceCodeFile codeFile : parser) {
				
				String fileName = parser.getFileUnitName(codeFile);
				int lineNumber = codeFile.getTotalLines();
				long spaces = codeFile.getTotalSpaces();
				File file = codeFile.getFileHandle();
				long size = file.length();
				
				totalFiles++;
				
				writer.println("\tFile: " + fileName + ", line: " + lineNumber + ", spaces: " + spaces + ", size: " + size);
				currentLineNumber = 0;
				String contents = loadFile(file);
				System.out.println("Load file " + fileName + " ...");
				writer.println("\t\tScanning file, line: " + currentLineNumber + ", spaces: " + contents.length());
				if (currentLineNumber < lineNumber) {
					Debug.println("\tFile: " + fileName + ", load line: " + lineNumber + ", scan line: " + currentLineNumber);
					problemFiles++;
				}
			}
			Debug.println("Total files: " + totalFiles + ", problem files: " + problemFiles);
			Debug.flush();
			writer.println("Total files: " + totalFiles + ", problem files: " + problemFiles);
			writer.flush();
		}
	}

	public static String loadFile(File file) {
		if (file == null) return null;
		String fileContent = "";
		
		try {
			Scanner scanner = new Scanner(file);
			StringBuffer buffer = new StringBuffer(); 
			while (scanner.hasNextLine()) {
				currentLineNumber++;
				String line = scanner.nextLine();
				buffer.append(line + "\n");
			}
			scanner.close();
			fileContent = buffer.toString();
		} catch (IOException exc) {
			return fileContent;
		}
		return fileContent;
	}
	
	
}

class NameDefinitionFilter extends NameTableFilter {

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isTypeDefinition()) return false;
		TypeDefinition type = (TypeDefinition)definition;
		if (type.isDetailedType() || type.isEnumType()) return true;	
		return false;
	}
}
