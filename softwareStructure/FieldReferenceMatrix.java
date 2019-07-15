package softwareStructure;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;

/**
 * A matrix to store the relation of method-field reference. 
 * 
 * @author Zhou Xiaocong
 * @since 2015年6月30日
 * @version 1.0
 */
public class FieldReferenceMatrix {
	private DetailedTypeDefinition type;		// The methods are of this type
	private List<MethodDefinition> methodList;  // The methods in the matrix
	private List<FieldDefinition> fieldList;	// The fields in the matrix
	private int[][] matrix = null;				// The matrix value
	private boolean isDirectReference = false; // Is only consider direct invocation?

	public FieldReferenceMatrix(DetailedTypeDefinition type, boolean isDirect) {
		this.type = type;
		this.isDirectReference = isDirect;
	}

	public DetailedTypeDefinition getDetailedTypeDefinition() {
		return type;
	}

	public List<MethodDefinition> getMethodList() {
		return methodList;
	}

	public List<FieldDefinition> getFieldList() {
		return fieldList;
	}

	public int[][] getMatrix() {
		if (matrix == null) {
			int methodLength = methodList.size();
			int fieldLength = fieldList.size();
			
			if (methodLength <= 0 || fieldLength <= 0) return null;
			
			matrix = new int[methodLength][fieldLength];
		}
		return matrix;
	}

	public int getValue(int rowIndex, int colIndex) {
		if (matrix == null) return 0;
		else return matrix[rowIndex][colIndex];
	}
	
	public boolean isDirectReference() {
		return isDirectReference;
	}


	public void setMethodList(List<MethodDefinition> methodList) {
		this.methodList = methodList;
	}

	/**
	 * Set the default method list as all the methods implemented in the detailed type
	 * == SoftwareStructManager.getImplementedMethods(type)
	 */
	public void setDefaultMethodList() {
		this.methodList = new ArrayList<MethodDefinition>();
		List<MethodDefinition> typeMethods = type.getMethodList();
		
		// Add the non-abstract method in the types to the result method list
		for (MethodDefinition method : typeMethods) {
			if (!method.isAbstract()) this.methodList.add(method);
		}
	}
	
	public void setFieldList(List<FieldDefinition> fieldList) {
		this.fieldList = fieldList;
	}

	/**
	 * Set the default field list as all the fields implemented in the detailed type
	 * == SoftwareStructManager.getImplementedFields(type)
	 */
	public void setDefaultFieldList() {
		this.fieldList = type.getFieldList();
	}

	/**
	 * The lengths of the row and column of the matrix must be consistent with the size of methodList and fieldList. 
	 */
	public void setMatrix(int[][] matrix) {
		this.matrix = matrix;
	}
	
	public void setReferenceRelation(MethodDefinition method, FieldDefinition field) {
		if (matrix == null) {
			int methodLength = methodList.size();
			int fieldLength = fieldList.size();
			
			if (methodLength <= 0 || fieldLength <= 0) return;
			
			matrix = new int[methodLength][fieldLength];
		}
		
		int rowIndex = -1;
		int colIndex = -1;
		
		for (int index = 0; index < methodList.size(); index++) {
			if (method == methodList.get(index)) {
				rowIndex = index;
				break;
			}
		}
		for (int index = 0; index < fieldList.size(); index++) {
			if (field == fieldList.get(index)) {
				colIndex = index;
				break;
			}
		}
		if (rowIndex >= 0 && colIndex >= 0) {
			matrix[rowIndex][colIndex] = 1;
			return;
		}
	}

	public int getReferenceRelation(MethodDefinition method, FieldDefinition field) {
		if (matrix == null) return 0;
		
		int rowIndex = -1;
		int colIndex = -1;
		
		for (int index = 0; index < methodList.size(); index++) {
			if (method == methodList.get(index)) {
				rowIndex = index;
				break;
			}
		}
		for (int index = 0; index < fieldList.size(); index++) {
			if (field == fieldList.get(index)) {
				colIndex = index;
				break;
			}
		}
		if (rowIndex >= 0 && colIndex >= 0) return matrix[rowIndex][colIndex];
		return 0;
	}

	public boolean hasReferenceRelation(MethodDefinition method, FieldDefinition field) {
		return (getReferenceRelation(method, field) == 1);
	}

	public int getRowLength() {
		return methodList.size();
	}

	public int getMethodNumber() {
		return methodList.size();
	}

	public int getColLength() {
		return fieldList.size();
	}
	
	public int getFieldNumber() {
		return fieldList.size();
	}

	/**
	 * Print the matrix to the given writer
	 */
	public void print(PrintWriter writer) {
		writer.print("\t");
		for (FieldDefinition field : fieldList) {
			writer.print(field.getSimpleName() + "\t");
		}
		writer.println();
		for (int rowIndex = 0; rowIndex < methodList.size(); rowIndex++) {
			writer.print(methodList.get(rowIndex).getSimpleName() + "\t");
			for (int colIndex = 0; colIndex < fieldList.size(); colIndex++) {
				if (matrix == null) writer.print("0\t");
				else writer.print(matrix[rowIndex][colIndex] + "\t");
			}
			writer.println();
		}
	}
}
