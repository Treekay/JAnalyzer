package nameTable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.ExecutionPointType;
import graph.cfg.creator.CFGCreator;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameReferenceCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.DetailedTypeDefinitionFilter;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.filter.NameDefinitionNameFilter;
import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameDefinitionFinder;
import nameTable.visitor.NameDefinitionPrinter;
import nameTable.visitor.NameDefinitionVisitor;
import nameTable.visitor.NameScopeVisitor;
import sourceCodeAST.SourceCodeFileSet;
import sourceCodeAST.SourceCodeLocation;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê10ÔÂ13ÈÕ
 * @version 1.0
 *
 */
public class TestNameTable {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String rootPath = "C:\\";

		
		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\JAnalyzer\\src\\", rootPath + "ZxcTools\\EclipseSource\\org\\", rootPath + "ZxcTemp\\testcase\\",
							rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\TestGenericType.java", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
							rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
							rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
							rootPath + "ZxcTools\\ArgoUml\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
							rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};
		
		String path = paths[5];
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
			Debug.setScreenOn();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		try {
//			printAllNames(path, result);
//			printAllDefinitions(path, result);
//			printAllDefinitionsToDataTable(path, result);
//			printAllNameScopesToDataTable(path, result);
			printAllReferencesToTable(path, result);
//			printReferenceBindToDefinition(path, result);
//			printNamesInCFG(path, result);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		writer.close();
		output.close();
	}
	
	/**
	 * Use a name table printer to print all definitions and all references of code file set with the given start path 
	 * to the given resultFile. 
	 */
	public static void printAllNames(String path, String resultFile) throws IOException {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameTableCreator(parser);
		String[] fileNameArray = {"E:\\ZxcWork\\ToolKit\\data\\javalang.txt", "E:\\ZxcWork\\ToolKit\\data\\javautil.txt", "E:\\ZxcWork\\ToolKit\\data\\javaio.txt", }; 

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out), fileNameArray);
		if (creator.hasError()) {
			System.out.println("There are " + creator.getErrorUnitNumber() + " error unit files:");
			creator.printErrorUnitList(new PrintWriter(System.out));
			System.out.println();
		}
		Debug.time("End creating.....");
		Debug.flush();
		
		List<CompilationUnitScope> unitList = manager.getAllCompilationUnitScopes();
		PrintWriter writer = new PrintWriter(new File(resultFile));
	
		for (CompilationUnitScope unit : unitList) {
			List<NameDefinition> resultList = manager.getAllDefinitionsOfScope(unit);
			for (NameDefinition definition : resultList) writer.println(definition.getFullQualifiedName() + " @ " + definition.getLocation());
		}
		
//		NameDefinitionPrinter definitionPrinter = new NameDefinitionPrinter(writer);
//		definitionPrinter.setPrintVariable(true);
//		manager.accept(definitionPrinter);
		
//		writer.println();
		
//		NameReferencePrinter referencePrinter = new NameReferencePrinter(writer);
//		referencePrinter.setPrintBindedDefinition();
//		manager.accept(referencePrinter);
		writer.close();
	}

	/**
	 * Use a name table printer to print all definitions of code file set with the given start path to the given resultFile. 
	 */
	public static void printAllDefinitions(String path, String resultFile) throws IOException {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);
		String[] fileNameArray = {"E:\\ZxcWork\\ToolKit\\data\\javalang.txt", "E:\\ZxcWork\\ToolKit\\data\\javautil.txt", "E:\\ZxcWork\\ToolKit\\data\\javaio.txt", }; 

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out), fileNameArray);
		Debug.time("End creating.....");
		Debug.flush();
		
		PrintWriter writer = new PrintWriter(new File(resultFile));
		NameDefinitionPrinter definitionPrinter = new NameDefinitionPrinter(writer);
		definitionPrinter.setPrintVariable(true);
		manager.accept(definitionPrinter);
		
		writer.close();
	}

	/**
	 * Print all references of code file set with the given start path to the given resultFile to a text file, which can be regarded
	 * as a two-dimension data table. 
	 */
	public static void printAllReferencesToTable(String path, String resultFile) throws IOException {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);
		String[] fileNameArray = {"C:\\ZxcWork\\ToolKit\\data\\javalang.txt", "C:\\ZxcWork\\ToolKit\\data\\javautil.txt", "C:\\ZxcWork\\ToolKit\\data\\javaio.txt", }; 
		NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out), fileNameArray);
		
		PrintWriter writer = new PrintWriter(new File(resultFile));
		writer.println("Type\tName\tLocation\tKind\tScope\tDefinition"); 
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		NameTableFilter filter = new DetailedTypeDefinitionFilter() {
			public boolean accept(NameDefinition definition) {
				if (definition.isDetailedType() && 
						definition.getSimpleName().equals("TestGenericType")) return true;
				return false;
			}
		};
		visitor.setFilter(filter);
		manager.accept(visitor);
		List<NameDefinition> typeList = visitor.getResult();
		NameReferenceCreator referenceCreator = new NameReferenceCreator(manager, true);

		for (NameDefinition definition : typeList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			System.out.println("Write references in type " + type.getFullQualifiedName());
			
			List<NameReference> referenceList = referenceCreator.createReferences(type);
			for (NameReference reference : referenceList) {
				reference.resolveBinding();
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				for (NameReference leafReference : leafReferenceList) {
//					if (leafReference.isLiteralReference()) continue;
					
					String name = leafReference.getName();
					String location = "(" + leafReference.getLocation().toString() + ")";
					String kind = leafReference.getReferenceKind() + "";
					String scopeName = leafReference.getScope().getScopeName();
					NameDefinition bindDef = leafReference.getDefinition();
					String bindString = "~~";
					if (bindDef != null) bindString = bindDef.getUniqueId();
					writer.println(type.getFullQualifiedName() + "\t" + name + "\t" + location + "\t" + 
							kind + "\t" + scopeName + "\t" + bindString); 
				}
			}
		}
		writer.close();
	}

	
	/**
	 * Print all definitions of code file set with the given start path to the given resultFile to a text file, which can be regarded
	 * as a two-dimension data table. 
	 */
	public static void printAllDefinitionsToTable(String path, String resultFile) throws IOException {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);
		String[] fileNameArray = {"C:\\ZxcWork\\ToolKit\\data\\javalang.txt", "C:\\ZxcWork\\ToolKit\\data\\javautil.txt", "C:\\ZxcWork\\ToolKit\\data\\javaio.txt", }; 

		final NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out), fileNameArray);
		
		PrintWriter writer = new PrintWriter(new File(resultFile));
		writer.println("Location\tFullQualifiedName\tSimpleName\tKind\tScope");
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new NameTableFilter() {
			// Exclude those simple definitions defined in system scope!
			public boolean accept(NameDefinition definition) {
				if (definition.isTypeDefinition() && definition.getScope().equals(manager.getSystemScope())) return false;
				return true;
			}
		});
		
		manager.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();

		for (NameDefinition definition : definitionList) {
			System.out.println("Write definition " + definition);
			
			String fullName = definition.getFullQualifiedName();
			String simpleName = definition.getSimpleName();
			SourceCodeLocation location = definition.getLocation();
			String locationString = "~~";
			if (location != null) locationString = "(" + location.toString() + ")";
			String scope = definition.getScope().getScopeName();
			String kind = definition.getDefinitionKind() + "";
			writer.println(locationString + "\t" + fullName + "\t" + simpleName + "\t" + kind + "\t" + scope);
		}
		writer.close();
	}

	/**
	 * Print all scopes of code file set with the given start path to the given resultFile to a text file, which can be regarded
	 * as a two-dimension data table. 
	 */
	public static void printAllNameScopesToTable(String path, String resultFile) throws IOException {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);
		String[] fileNameArray = {"C:\\ZxcWork\\ToolKit\\data\\javalang.txt", "C:\\ZxcWork\\ToolKit\\data\\javautil.txt", "C:\\ZxcWork\\ToolKit\\data\\javaio.txt", }; 

		NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out), fileNameArray);
		
		PrintWriter writer = new PrintWriter(new File(resultFile));
		writer.println("Name\tStart\tEnd\tParent\tKind");
		
		NameScopeVisitor visitor = new NameScopeVisitor();
		SystemScope rootScope = manager.getSystemScope();
		
		rootScope.accept(visitor);
		List<NameScope> scopeList = visitor.getResult();

		for (NameScope scope : scopeList) {
			String name = scope.getScopeName();
			SourceCodeLocation start = scope.getScopeStart();
			String startString = "~~";
			if (start != null) startString = "(" + start.toString() + ")";

			SourceCodeLocation end = scope.getScopeEnd();
			String endString = "~~";
			if (end != null) endString = "(" + end.toString() + ")";
			
			NameScope parentScope = scope.getEnclosingScope();
			String parentString = "~~";
			if (parentScope != null) parentString = parentScope.getScopeName();
			
			String kind = scope.getScopeKind() + "";
			
			writer.println(name + "\t" + startString + "\t" + endString + "\t" + parentString + "\t" + kind);
		}
		writer.close();
	}
	
	/**
	 * Print all references, which binded to the same definitions, of code file set with the given start path to the given resultFile to 
	 * a text file, which can be regarded as a two-dimension data table. 
	 */
	public static void printReferenceBindToDefinition(String path, String resultFile) throws IOException {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);
		String[] fileNameArray = {"E:\\ZxcWork\\ToolKit\\data\\javalang.txt", "E:\\ZxcWork\\ToolKit\\data\\javautil.txt", "E:\\ZxcWork\\ToolKit\\data\\javaio.txt", }; 

		NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out), fileNameArray);
		
		PrintWriter writer = new PrintWriter(new File(resultFile));
		writer.println("CompilationUnit\tType\tMethod\tReference\tLocation\tDefinition"); 
		
		NameDefinitionFinder finder = new NameDefinitionFinder();
		manager.accept(finder);
		NameDefinition definition = finder.getResult();
		if (definition == null) return;
		
		NameReferenceCreator referenceCreator = new NameReferenceCreator(manager);
		List<NameReference> referenceList = referenceCreator.createReferencesBindedToDefinition(definition);
		
		for (NameReference reference : referenceList) {
			CompilationUnitScope unitScope = manager.getEnclosingCompilationUnitScope(reference);
			String unitString = "~~";
			if (unitScope != null) unitString = unitScope.getUnitName();
			
			DetailedTypeDefinition type = manager.getEnclosingDetailedTypeDefinition(reference);
			String typeString = "~~";
			if (type != null) typeString = type.getFullQualifiedName();
			
			MethodDefinition method = manager.getEnclosingMethodDefinition(reference);
			String methodString = "~~";
			if (method != null) methodString = method.getSimpleName();
			
			String name = reference.getName();
			String location = "(" + reference.getLocation().toString() + ")";
			NameDefinition bindDef = reference.getDefinition();
			
			String bindString = "~~";
			if (bindDef != null) bindString = bindDef.getUniqueId();
			System.out.println("Writer reference " + name + ", location  " + location + ", refer to definition " + definition.getUniqueId());
			writer.println(unitString + "\t" + typeString + "\t" + methodString + "\t" + name + "\t" + location + "\t" + bindString); 
		}
		
		finder = new NameDefinitionFinder(new NameDefinitionNameFilter(
				new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD), "main"));
		manager.accept(finder);
		definition = finder.getResult();
		if (definition == null) {
			writer.close();
			return;
		}
		
		referenceList = referenceCreator.createReferencesBindedToDefinition(definition);
		for (NameReference reference : referenceList) {
			CompilationUnitScope unitScope = manager.getEnclosingCompilationUnitScope(reference);
			String unitString = "~~";
			if (unitScope != null) unitString = unitScope.getUnitName();
			
			DetailedTypeDefinition type = manager.getEnclosingDetailedTypeDefinition(reference);
			String typeString = "~~";
			if (type != null) typeString = type.getFullQualifiedName();
			
			MethodDefinition method = manager.getEnclosingMethodDefinition(reference);
			String methodString = "~~";
			if (method != null) methodString = method.getSimpleName();
			
			String name = reference.getName();
			String location = "(" + reference.getLocation().toString() + ")";
			NameDefinition bindDef = reference.getDefinition();
			
			String bindString = "~~";
			if (bindDef != null) bindString = bindDef.getUniqueId();
			System.out.println("Writer reference " + name + ", location  " + location + ", refer to definition " + definition.getUniqueId());
			writer.println(unitString + "\t" + typeString + "\t" + methodString + "\t" + name + "\t" + location + "\t" + bindString); 
		}
		writer.close();
	}
	
	/**
	 * Print all definitions and all references in the node (execution point) of control flow graph of a method 
	 */
	public static void printNamesInCFG(String path, String resultFile) throws IOException {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);
		String[] fileNameArray = {"C:\\ZxcWork\\ToolKit\\data\\javalang.txt", "C:\\ZxcWork\\ToolKit\\data\\javautil.txt", "C:\\ZxcWork\\ToolKit\\data\\javaio.txt", }; 
		NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out), fileNameArray);
		
		// Find the method which has the maximal code lines
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		manager.accept(visitor);
		List<NameDefinition> resultList = visitor.getResult();
		MethodDefinition maxMethod = null;
		int maxLineNumber = 0;
		for (NameDefinition definition : resultList) {
			MethodDefinition method = (MethodDefinition)definition;
			if (maxMethod == null) {
				maxMethod = method;
				maxLineNumber = maxMethod.getEndLocation().getLineNumber() - maxMethod.getLocation().getLineNumber();
			} else {
				int currentLineNumber = method.getEndLocation().getLineNumber() - method.getLocation().getLineNumber();
				if (currentLineNumber > maxLineNumber) {
					maxMethod = method;
					maxLineNumber = currentLineNumber; 
				}
			}
		}
		
		if (maxMethod == null) return;
		
		System.out.println("Find the maximal method " + maxMethod.getFullQualifiedName());
		CompilationUnitScope unitScope = manager.getEnclosingCompilationUnitScope(maxMethod);
		String unitName = unitScope.getUnitName();
		CompilationUnit root = parser.findSourceCodeFileASTRootByFileUnitName(unitName);
		CFGCreator cfgCreator = new CFGCreator(unitName, root);
		
		NameTableASTBridge bridge = new NameTableASTBridge(manager);
		MethodDeclaration methodDeclaration = bridge.findASTNodeForMethodDefinition(maxMethod);
		if (methodDeclaration == null) {
			System.out.println("Can not find AST node for method definition " + maxMethod.getUniqueId());
			return;
		}
		DetailedTypeDefinition type = manager.getEnclosingDetailedTypeDefinition(maxMethod);
		String typeName = type.getSimpleName();
		// Create control flow graph (CFG) for this maximal method!
		ControlFlowGraph cfg = cfgCreator.create(methodDeclaration, typeName);
		if (cfg == null) {
			System.out.println("Can not create control flow graph for method " + maxMethod.getUniqueId());
			return;
		}
		
		NameReferenceCreator referenceCreator = new NameReferenceCreator(manager);
		PrintWriter writer = new PrintWriter(new File(resultFile));
		writer.println("Method: " + maxMethod.getUniqueId());
		
		// Write definitions and references in each execution point (i.e. the node of control flow graph) 
		List<GraphNode> pointList = cfg.getAllNodes();
		for (GraphNode node : pointList) {
			ExecutionPoint point = (ExecutionPoint)node;
			ExecutionPointType pointType = point.getType();
			
			writer.println("\tExecutionPoint: " + point.getStartLocation() + "--" + point.getEndLocation() + " " + point.getLabel() + "[" + point.getDescription() + "]");
			// Do not write definitions and references for virtual node of CFG, since its AST node frequently include all statements of a method, a 
			// branch or a loop statement.
			if (pointType.isVirtual()) continue;
			
			ASTNode astNode = point.getAstNode();
			List<NameDefinition> definitionList = bridge.getAllDefinitionsInASTNode(unitName, astNode);
			if (definitionList != null) {
				if (definitionList.size() > 0) writer.println("\t\tDefinitions: ");
				for (NameDefinition definition : definitionList) {
					System.out.println("Writer definition " + definition.getSimpleName());
					writer.println("\t\t\t" + definition.getLocation() + " " + definition.getDefinitionKind() + " " + definition.getSimpleName());
				}
			}
			
			List<NameReference> referenceList = referenceCreator.createReferencesForASTNode(unitName, astNode);
			if (referenceList.size() > 0) writer.println("\t\tReferences: ");
			for (NameReference reference : referenceList) {
				reference.resolveBinding();
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				for (NameReference leafReference : leafReferenceList) {
					if (leafReference.isResolved()) {
						writer.println("\t\t\t" + leafReference.getLocation() + " " + leafReference.getReferenceKind() + " " + leafReference.getName() + "[" + leafReference.getDefinition().getUniqueId() + "]");
					} else {
						writer.println("\t\t\t" + leafReference.getLocation() + " " + leafReference.getReferenceKind() + " " + leafReference.getName() + "[not resolved]");
					}
					System.out.println("Writer reference " + leafReference.getName());
				}
			}
		}
		writer.close();
	}
	
}
