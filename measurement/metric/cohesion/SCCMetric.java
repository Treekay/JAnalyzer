package measurement.metric.cohesion;

import measurement.measure.SoftwareMeasure;
import softwareStructure.FieldReferenceMatrix;
import softwareStructure.MethodInvocationMatrix;

/**
 * A class to calculate the metric SCC
 * 
 * @author Li Jingsheng
 * @since 2015Äê08ÔÂ17ÈÕ
 * @update 2015/10/10, Zhou Xiaocong
 *
 */
public class SCCMetric extends SoftwareCohesionMetric{

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 

		FieldReferenceMatrix fieldTypeReferenceMatrix = structManager.createIndirectFieldTypeReferenceMatrix(type);
		MethodInvocationMatrix methodInvocationMatrix = structManager.createIndirectStaticMethodInvocationMatrix(type);

		int methodNumber = 0;
		int fieldTypeNumber = 0;
		if (fieldTypeReferenceMatrix != null) {
			fieldTypeNumber = fieldTypeReferenceMatrix.getColLength();
			methodNumber = fieldTypeReferenceMatrix.getRowLength();
		}
		
		double value = 0;
		double mmac = this.getMMACMetric(fieldTypeReferenceMatrix);
		double aac = this.getAACMetric(fieldTypeReferenceMatrix);
		double amc = this.getAMCMetric(fieldTypeReferenceMatrix);
		double mmic = this.getMMICMetric(methodInvocationMatrix);
		
		// Use MMAC, AAC, AMC, MMIC metric to calculate SCC metric as defined in Al-Dallal and 
		// Briand 2010 IST paper
		if (methodNumber == 0) value = 0;
		else if (methodNumber == 1 && fieldTypeNumber == 0) value = 1;
		else if (methodNumber > 1 && fieldTypeNumber == 0) value = mmic;
		else {
			int methodPairs = methodNumber * (methodNumber - 1);
			int fieldPairs = fieldTypeNumber * (fieldTypeNumber - 1);
			int fieldMethodPairs = fieldTypeNumber * methodNumber;
			value = (double)(methodPairs * (mmac + 2 * mmic) + fieldPairs * aac + 2 * fieldMethodPairs * amc);
			value = value / (3 * methodPairs + fieldPairs + 2 * fieldMethodPairs);
		}

//		Debug.println("method number = " + methodNumber + ", field number = " + fieldNumber + ", mmac = " + mmac + ", aac = " + aac + ", amc = " + amc + ", mmic = " + mmic + ", value = " + value);

		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		// We complete the calculation of the measure successfully. 
		return true;
	}
	
	
	/**
	 * Calculate the MMAC metric, i.e. the method-method through attributes cohesion 
	 */
	protected double getMMACMetric(FieldReferenceMatrix fieldTypeReferenceMatrix){
		if (fieldTypeReferenceMatrix == null) return 0;
		
		double value = 0;
		int methodNumber = fieldTypeReferenceMatrix.getRowLength();
		int fieldNumber = fieldTypeReferenceMatrix.getColLength();
		int[][] matrix = fieldTypeReferenceMatrix.getMatrix();
		
		if (methodNumber == 0 || fieldNumber == 0) value = 0;
		else if (methodNumber == 1) value = 1;
		else {
			int sum = 0;
			for (int i = 0; i < fieldNumber; i++) {
				int col1Number = getOneNumberInColumn(matrix, i); 
				sum += col1Number * (col1Number - 1);
			}
			value = (double)sum/(fieldNumber * methodNumber * (methodNumber - 1));
		}
		
		return value;
	}
	
	/**
	 * Calculate the AAC metric, i.e. the attribute-attribute cohesion 
	 */
	protected double getAACMetric(FieldReferenceMatrix fieldTypeReferenceMatrix) {
		if (fieldTypeReferenceMatrix == null) return 0;
		
		double value = 0;
		int methodNumber = fieldTypeReferenceMatrix.getRowLength();
		int fieldNumber = fieldTypeReferenceMatrix.getColLength();
		int[][] matrix = fieldTypeReferenceMatrix.getMatrix();
		
		if (methodNumber == 0 || fieldNumber == 0) value = 0;
		else if (fieldNumber == 1) value = 1;
		else {
			int sum = 0;
			for (int i = 0; i < methodNumber; i++) {
				int row1Number = getOneNumberInRow(matrix, i); 
				sum += row1Number * (row1Number - 1);
			}
			value = (double)sum/(methodNumber * fieldNumber * (fieldNumber - 1));
		}
		return value;
	}

	/**
	 * Calculate the AMC metric, i.e. the attribute-method cohesion 
	 */
	protected double getAMCMetric(FieldReferenceMatrix fieldTypeReferenceMatrix){
		if (fieldTypeReferenceMatrix == null) return 0;
		
		double value = 0;
		int methodNumber = fieldTypeReferenceMatrix.getRowLength();
		int fieldNumber = fieldTypeReferenceMatrix.getColLength();
		int[][] matrix = fieldTypeReferenceMatrix.getMatrix();
		
		if (methodNumber == 0 || fieldNumber == 0) value = 0;
		else {
			int sum = 0;
			for (int i = 0; i < methodNumber; i++) {
				for (int j = 0; j < fieldNumber; j++) sum += matrix[i][j];
			}
			value = (double)sum/(methodNumber * fieldNumber);
		}
		return value;
	}
	
	
	/**
	 * Calculate the MMIC metric, i.e. the method-method invocation cohesion 
	 */
	protected double getMMICMetric(MethodInvocationMatrix methodInvocationMatrix){
		if (methodInvocationMatrix == null) return 0;
		double value = 0;
		
		int methodNumber = methodInvocationMatrix.getMatrixLength();
		int[][] matrix = methodInvocationMatrix.getMatrix();
		if(methodNumber == 0) value = 0;
		else if (methodNumber == 1) value = 1;
		else {
			int sum = 0;
			for(int i = 0; i < methodNumber; i++) {
				for(int j = 0; j < methodNumber; j++) {
					if (i != j) sum += matrix[i][j];
				}
			}
			value = (double)sum/(methodNumber * (methodNumber - 1));
		}
		return value;
	}

	/**
	 * Get the number of 1 in the given column
	 */
	protected int getOneNumberInColumn(int[][] matrix, int col){
		int sum = 0;
		for(int i = 0; i < matrix.length; i++) sum += matrix[i][col];
		return sum;
	}
	
	/**
	 * Get the number of 1 in the given row
	 */
	protected int getOneNumberInRow(int[][] matrix, int row){
		int sum = 0;
		for(int i = 0; i < matrix[row].length; i++) sum += matrix[row][i];
		return sum;
	}
}