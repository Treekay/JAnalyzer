package nameTable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import nameTable.creator.NameReferenceCreator;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameReference.ValueReference;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameScope.CompilationUnitScope;
import util.Debug;

/**
 * Check whether the names in the table are consistent 
 * @author Zhou Xiaocong
 * @since 2017年8月16日
 * @version 1.0
 *
 */
public class NameTableChecker {

	public static boolean checkAllNameDefinitions(NameTableManager tableManager, PrintWriter writer) {
		
		return true;
	}

	public static int checkAllNameReferences(NameTableManager tableManager, PrintWriter writer) {
		List<CompilationUnitScope> unitList = tableManager.getAllCompilationUnitScopes();
		NameReferenceCreator creator = new NameReferenceCreator(tableManager, true);
		
		int result = 0;
		for (CompilationUnitScope unit : unitList) {
			List<NameReference> referenceList = creator.createReferences(unit);
			
			for (NameReference reference : referenceList) {
				reference.resolveBinding();
				result += checkReference(reference, writer);
			}
		}
		writer.flush();
		return result;
	}
	
	public static int checkReference(NameReference reference, PrintWriter writer) {
		int result = 0;
		
		String unitFile = reference.getLocation().getFileUnitName();
		int line = reference.getLocation().getLineNumber();
		int column = reference.getLocation().getColumn();
		String referenceMessage = unitFile + "\t" + line + "\t" + column + "\t" + getPrettyReferenceName(reference);
		
		Debug.println("Check reference: " + getPrettyReferenceName(reference));
		if (!reference.isResolved()) {
			result++;
			writer.println(referenceMessage + "\tReference can not be resolved!");
		}
		NameReferenceKind kind = reference.getReferenceKind();
		if (kind == NameReferenceKind.NRK_FIELD) {
			if (!(reference instanceof ValueReference)) {
				result++;
				writer.println(referenceMessage + "\tReference kind is NRK_FILED, but it is not instance of ValueReference!");
			}
		} else if (kind == NameReferenceKind.NRK_GROUP) {
			if (!(reference instanceof NameReferenceGroup)) {
				result++;
				writer.println(referenceMessage + "\tReference kind is NRK_GROUP, but it is not instance of NameReferenceGroup!");
			}
		} else if (kind == NameReferenceKind.NRK_TYPE) {
			if (!(reference instanceof TypeReference)) {
				result++;
				writer.println(referenceMessage + "\tReference kind is NRK_TYPE, but it is not instance of TypeReference!");
			}
			if (!(reference.isTypeReference())) {
				result++;
				writer.println(referenceMessage + "\tReference kind is NRK_TYPE, but its isTypeReference() return false!");
			}
		}
		writer.flush();
		
		List<NameReference> subreferenceList = reference.getSubReferenceList();
		if (subreferenceList == null) {
			result++;
			writer.println(referenceMessage + "\tIts sub-reference list is null!");
		} else {
			for (NameReference subreference : subreferenceList) checkReference(subreference, writer);
		}
		return result;
	}
	
	public static void main(String[] args) {
		String rootPath = "C:\\";
		
		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\JAnalyzer\\src\\", rootPath + "ZxcTools\\EclipseSource\\org\\", rootPath + "ZxcTemp\\JAnalyzer\\src\\",
							rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\TestGenericType.java", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
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
			writer.close();
			return;
		}
		
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
			Debug.setScreenOn();
		} catch (Exception exc) {
			exc.printStackTrace();
			writer.close();
			output.close();
			return;
		}
		
		try {
			Debug.setStart("Begin check....");
			NameTableManager tableManager = NameTableManager.createNameTableManager(path);
			int probs = checkAllNameReferences(tableManager, writer);
			Debug.time("After check, there are " + probs + " problems...");
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		writer.close();
		output.close();
	}

	static String getPrettyReferenceName(NameReference reference) {
		String name = reference.getName();

		int lineIndex = name.indexOf('\n');
		if (lineIndex < 0 || lineIndex > 32) lineIndex = 64;
		if (lineIndex > name.length()) return name;
		return name.substring(0, lineIndex);
	}
}
