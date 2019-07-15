package measurement.metric.cohesion;

import measurement.measure.SoftwareMeasure;
import softwareStructure.FieldReferenceMatrix;

/**
 * A class to calculate the metric Coh
 * 
 * @author Li Jingsheng
 * @since 2015年08月12日
 * @update 2015/10/10, Zhou Xiaocong
 */
public class CohMetric extends SoftwareCohesionMetric{
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 
		
		double value = 0;
		FieldReferenceMatrix fieldReferenceMatrix = structManager.createDirectFieldReferenceMatrix(type);
		if (fieldReferenceMatrix != null) {
			// Note that the rows of the matrix correspond to the methods and the columns of 
			// the matrix correspond to the fields 
			int methodNumber = fieldReferenceMatrix.getMethodNumber();
			int fieldNumber = fieldReferenceMatrix.getFieldNumber();
			// Calculate the sum of values in fieldReferenceMatrix 
			int sum = 0;
			for (int i = 0; i < methodNumber; i++) {
				for (int j = 0; j < fieldNumber; j++) {
					sum += fieldReferenceMatrix.getValue(i,  j);
				}
			}
			
			// 2016/05/08: Set the special value according to Jehad Al Dalla's 2011 paper published on IST
			if (methodNumber > 0 && fieldNumber > 0) value = (double)sum / (methodNumber * fieldNumber);
			else if ((methodNumber == 0 && fieldNumber > 0) || (methodNumber == 1 && fieldNumber == 0)) value = 1;
			else value = 0;   // i.e. methodNumber != 1 && fieldNumber == 0
		}
		
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		// We complete the calculation of the measure successfully. 
		return true;
	}
}
