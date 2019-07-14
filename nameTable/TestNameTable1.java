package nameTable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameReferenceCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.DetailedTypeDefinitionFilter;
import nameTable.filter.NameDefinitionNameFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.NameReference;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê10ÔÂ1ÈÕ
 * @version 1.0
 *
 */
public class TestNameTable1 {

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
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "OpenSource\\ant\\apache-ant-1.10.2\\src\\main\\org\\apache\\tools\\ant\\Target.java",
		}; // "OpenSource\\ant\\apache-ant-1.10.2\\src\\main\\org\\apache\\tools\\ant\\Target.java"
		
		String path = paths[paths.length-1];
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
		
		printAllReferencesToAllMethodsInDetailedTypes(path, writer);

		writer.close();
		output.close();
	}

	public static void printAllReferencesToAllMethodsInDetailedTypes(String path, PrintWriter writer) {
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out));
		Debug.time("End creating.....");
		Debug.flush();
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new DetailedTypeDefinitionFilter(new NameDefinitionNameFilter("Target")));
		manager.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();

		Debug.time("Begin scan....!");
		for (NameDefinition definition : definitionList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			String className = type.getSimpleName();
			System.out.println("Scan class " + className);

			List<MethodDefinition> methodList = type.getMethodList();
			List<SelectedReferenceToName> selectedReferenceToMethodList = findAllReferencesToMethods(manager, methodList);
			printAllReferencesToAllMethodsInADetailedType(type, selectedReferenceToMethodList, writer);
			
			writer.println();
			writer.flush();
		}
		Debug.time("End scan....!");
	}
	
	public static void printAllReferencesToAllMethodsInADetailedType(DetailedTypeDefinition type, List<SelectedReferenceToName> selectedReferenceList, PrintWriter writer) {
		writer.println("In type: " + type.getFullQualifiedName());
		for (SelectedReferenceToName selectedReference : selectedReferenceList) {
			writer.println("\tReferences to method: " + selectedReference.getNameDefinition().getFullQualifiedName());
			List<DetailedTypeSelectedReference> typeSelectedReferenceList = selectedReference.getSelectedReferenceList();
			for (DetailedTypeSelectedReference typeSelectedReference : typeSelectedReferenceList) {
				writer.println("\t\tIn type: " + typeSelectedReference.getType().getFullQualifiedName());
				List<NameReference> referenceList = typeSelectedReference.getOtherReferenceList();
				for (NameReference reference : referenceList) {
					writer.println("\t\t\tReference " + reference.getUniqueId());
				}
				List<MethodSelectedReference> methodReferenceList = typeSelectedReference.getMethodSelectedReferenceList();
				for (MethodSelectedReference methodSelectedReference : methodReferenceList) {
					MethodDefinition enclosingMethod = methodSelectedReference.getMethod();
					writer.println("\t\t\tMethod: " + enclosingMethod.getSimpleName() + "@" + enclosingMethod.getLocation());
					referenceList = methodSelectedReference.getReferenceList();
					for (NameReference reference : referenceList) {
						writer.println("\t\t\t\tReference " + reference.getName() + ", location " + reference.getLocation());
					}
				}
			}
		}
	}
	
	
	public static List<SelectedReferenceToName> findAllReferencesToMethods(NameTableManager manager, List<MethodDefinition> methodList) {
		List<SelectedReferenceToName> selectedReferenceToMethodList = new ArrayList<SelectedReferenceToName>(methodList.size());
		for (MethodDefinition method : methodList) selectedReferenceToMethodList.add(new SelectedReferenceToName(method));
		
		List<DetailedTypeDefinition> typeList = manager.getAllDetailedTypeDefinitions();
		NameReferenceCreator referenceCreator = new NameReferenceCreator(manager);
		
		for (DetailedTypeDefinition type : typeList) {
			System.out.println("\tScan detailed type " + type.getFullQualifiedName());
			
			List<NameReference> referenceList = referenceCreator.createReferences(type);
			
			for (NameReference reference : referenceList) {
				reference.resolveBinding();
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				DetailedTypeDefinition enclosingType = (DetailedTypeDefinition)reference.getEnclosingTypeDefinition();
				if (enclosingType == null) continue;
				
				MethodDefinition enclosingMethod = manager.getEnclosingMethodDefinition(reference);

				for (NameReference leafReference : leafReferenceList) {
					NameDefinition definition = leafReference.getDefinition();
					if (definition == null) continue;
					for (SelectedReferenceToName selectedReferenceToMethod : selectedReferenceToMethodList) {
						if (selectedReferenceToMethod.getNameDefinition() == definition) {
							// Add reference rather than leafReference for more messages on this reference!
							// However, if a group reference refer to the given definition more than one time, the reference will 
							// be add to the selectedReferenceToMethodList more than one time too!
							selectedReferenceToMethod.addSelectedReference(enclosingType, enclosingMethod, reference);
						}
					}
				}
			}
		}
		
		return selectedReferenceToMethodList;
	}
}

class MethodSelectedReference {
	private MethodDefinition method = null;
	private List<NameReference> referenceList = null;
	
	public MethodSelectedReference(MethodDefinition method) {
		this.method = method;
		referenceList = new ArrayList<NameReference>(); 
	}
	
	public MethodDefinition getMethod() {
		return method;
	}
	
	public void addReference(NameReference reference) {
		referenceList.add(reference);
	}
	
	public List<NameReference> getReferenceList() {
		return referenceList;
	}
}

class DetailedTypeSelectedReference {
	private DetailedTypeDefinition type = null;
	private List<MethodSelectedReference> methodReferenceList = null;
	private List<NameReference> otherReferenceList = null;
	
	public DetailedTypeSelectedReference(DetailedTypeDefinition type) {
		this.type = type;
		methodReferenceList = new ArrayList<MethodSelectedReference>();
		otherReferenceList = new ArrayList<NameReference>();
	}
	
	public DetailedTypeDefinition getType() {
		return type;
	}
	
	public void addMethodSelectedReference(MethodDefinition method, NameReference reference) {
		for (MethodSelectedReference methodSelectedReference : methodReferenceList) {
			if (methodSelectedReference.getMethod() == method) {
				methodSelectedReference.addReference(reference);
				return;
			}
		}
		MethodSelectedReference methodSelectedReference = new MethodSelectedReference(method);
		methodSelectedReference.addReference(reference);
		methodReferenceList.add(methodSelectedReference);
	}

	public void addOtherReference(NameReference reference) {
		otherReferenceList.add(reference);
	}
	
	public List<MethodSelectedReference> getMethodSelectedReferenceList() {
		return methodReferenceList;
	}
	
	public List<NameReference> getOtherReferenceList() {
		return otherReferenceList;
	}
}

class SelectedReferenceToName {
	// The definition have been referred!
	NameDefinition definition = null;
	// The information of references referred to the above definition! 
	List<DetailedTypeSelectedReference> selectedReferenceList = null;
	
	public SelectedReferenceToName(NameDefinition definition) {
		this.definition = definition;
		selectedReferenceList = new ArrayList<DetailedTypeSelectedReference>();
	}
	
	public NameDefinition getNameDefinition() {
		return definition;
	}
	
	public List<DetailedTypeSelectedReference> getSelectedReferenceList() {
		return selectedReferenceList;
	}
	
	
	/**
	 * Add a reference which refers to the definition given in the current object!
	 * @param type : the reference is in this type;
	 * @param method : the reference is in this method, if it is null, then the reference maybe in a field initializer expression
	 * 		or a initializer block of the type! 
	 */
	public void addSelectedReference(DetailedTypeDefinition type, MethodDefinition method, NameReference reference) {
		for (DetailedTypeSelectedReference selectedReference : selectedReferenceList) {
			if (selectedReference.getType() == type) {
				if (method != null) selectedReference.addMethodSelectedReference(method, reference);
				else selectedReference.addOtherReference(reference);
				return;
			}
		}
		
		DetailedTypeSelectedReference selectedReference = new DetailedTypeSelectedReference(type);
		if (method != null) selectedReference.addMethodSelectedReference(method, reference);
		else selectedReference.addOtherReference(reference);
		selectedReferenceList.add(selectedReference);
	}

	/**
	 * Add a reference which refers to the definition given in the current object!
	 * @param type : the reference is in this type, which may be in a field initializer expression 
	 * 		or a initializer block of the type! 
	 */
	public void addSelectedReference(DetailedTypeDefinition type, NameReference reference) {
		for (DetailedTypeSelectedReference selectedReference : selectedReferenceList) {
			if (selectedReference.getType() == type) {
				selectedReference.addOtherReference(reference);
				return;
			}
		}
		
		DetailedTypeSelectedReference selectedReference = new DetailedTypeSelectedReference(type);
		selectedReference.addOtherReference(reference);
		selectedReferenceList.add(selectedReference);
	}
	
	
}
