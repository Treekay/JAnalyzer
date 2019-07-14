package measurement.metric.cohesion;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import softwareStructure.FieldReferenceMatrix;

/**
 * A class to calculate the metric SCOM.
 * 
 * @author Li Jingsheng
 * @since 2015Äê08ÔÂ13ÈÕ
 * @update 2015/10/10, Zhou Xiaocong
 *
 */
public class SCOMMetric extends SoftwareCohesionMetric{
	
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
			// Calculate the sum of the similarities between method i and j with i > j, i = 0, ..., 
			// methodNumber, and j = i + 1, ..., methodNumber
			double sum = 0;
			for(int i = 0; i < methodNumber - 1; i++) {
				for(int j = i + 1; j < methodNumber; j++) {			
					double similarity = getSimilarity(fieldReferenceMatrix, i, j);
					sum = sum + similarity;
				}
			}
			
			value = 2 * sum;
			if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.SCOM)) {
				// 2016/05/08: Set the special value according to Jehad Al Dalla's 2011 paper published on IST
				if (methodNumber != 1 && fieldNumber == 0) value = 0;		
				else if (methodNumber <= 1) value = 1;
				else {
					// Since we only sum the similarities of method i and j with i > j in getSumSimilarity(), so the sumSimilarity 
					// must be multiplied with 2.
					value = value / (methodNumber * (methodNumber - 1));  
				}
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
	 * Calculate the similarity between two methods given by the indexOne and indexTwo.
	 * Here indexOne and indexTwo are the index of methods in the FieldReferenceMatrix matrix.
	 * <p>The similarity between two methods is defined as Fernandez and Pena 2006 paper
	 */
	public double getSimilarity(FieldReferenceMatrix matrix, int indexOne, int indexTwo){
		int totalReferenceNumber = 0;				
		int commonReferenceNumber = 0;
		int oneReferenceNumber = 0;
		int twoReferenceNumber = 0;
		
		int fieldNumber = matrix.getFieldNumber();
		for(int i = 0; i < fieldNumber; i++){
			if(matrix.getValue(indexOne, i) > 0) oneReferenceNumber++;
			if(matrix.getValue(indexTwo, i) > 0) twoReferenceNumber++;
			if(matrix.getValue(indexOne, i) > 0 || matrix.getValue(indexTwo, i) > 0) totalReferenceNumber++;
			if(matrix.getValue(indexOne, i) > 0 && matrix.getValue(indexTwo, i) > 0) commonReferenceNumber++;
		}
		
		int minReferenceNumber = oneReferenceNumber;
		if (twoReferenceNumber < minReferenceNumber) minReferenceNumber = twoReferenceNumber;

		double similarity = 0;
		if (minReferenceNumber > 0 && fieldNumber > 0) {
			similarity =  (double)(commonReferenceNumber * totalReferenceNumber)/(minReferenceNumber * fieldNumber);
		}
		return similarity;
	}
}
