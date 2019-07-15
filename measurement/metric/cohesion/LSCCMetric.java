package measurement.metric.cohesion;

import measurement.measure.SoftwareMeasure;
import softwareStructure.FieldReferenceMatrix;

/**
 * A class to calculate the metric LSCC
 * 
 * @author Li Jingsheng
 * @since 2015年08月14日
 * @update 2015/10/10 Zhou Xiaocong
 *
 */
public class LSCCMetric extends SoftwareCohesionMetric{

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 

		double value = 0;
		FieldReferenceMatrix fieldReferenceMatrix = structManager.createIndirectFieldReferenceMatrix(type);
		if (fieldReferenceMatrix != null) {
			// Note that the rows of the matrix correspond to the methods and the columns of 
			// the matrix correspond to the fields 
			int methodNumber = fieldReferenceMatrix.getMethodNumber();
			int fieldNumber = fieldReferenceMatrix.getFieldNumber();

			// Calculate the sum of similarity between two methods in referring fields
			int sum = 0; 
			for(int i = 0; i < fieldNumber; i++) {
				int numberOfMethod = getNumberOfMethodReferField(fieldReferenceMatrix, i); 
				sum +=  numberOfMethod * (numberOfMethod - 1);
			}
			
			// 2016/05/08: Set the special value according to Jehad Al Dalla's 2011 paper published on IST
			if (methodNumber != 1 && fieldNumber == 0) value = 0;		
			else if (methodNumber <= 1) value = 1;
			else {
				value = (double)sum / (fieldNumber * methodNumber *(methodNumber - 1)); 
			}
		}
		
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		// We complete the calculation of the measure successfully. 
		return true;
	}


	/**
	 * Get the number of method which refer to the field given by the fieldIndex
	 */
	public int getNumberOfMethodReferField(FieldReferenceMatrix matrix, int fieldIndex) {
		int result = 0;
		int methodNumber = matrix.getMethodNumber();
		for (int i=0; i < methodNumber; i++) {
			result += matrix.getValue(i, fieldIndex);
		}
		return result;
	}
}
