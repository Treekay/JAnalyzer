package measurement.metric.cohesion;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import softwareStructure.FieldReferenceMatrix;

/**
 * A class to calculate the metric LCOM1, LCOM2, or LCOM5
 * 
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ2ÈÕ
 * @version 1.0
 */
public class LCOM125Metric extends SoftwareCohesionMetric {

	/* (non-Javadoc)
	 * @see softwareMeasurement.metric.SoftwareStructMetric#calculate(softwareMeasurement.measure.SoftwareMeasure)
	 */
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

			// Calculate the number of method pair which have the common reference. 
			int commonReferencePairNumber = 0;
			for (int i = 0; i < methodNumber; i++) {
				for (int j = i+1; j < methodNumber; j++) {
					for (int k = 0; k < fieldNumber; k++) {
						if (fieldReferenceMatrix.getValue(i,  k) > 0 && fieldReferenceMatrix.getValue(j, k) > 0) {
							// This means the ith method uses the kth field and the jth method uses the kth field too!
							commonReferencePairNumber++;
							break;
						}
					}
				}
			}
			
			// Calculate the sum of the methods with field references
			int numberOfMethodWithFieldReferences = 0;
			if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.LCOM2) || 
					measure.getIdentifier().equals(SoftwareMeasureIdentifier.LCOM2p) ||
					measure.getIdentifier().equals(SoftwareMeasureIdentifier.LCOM5)) {
				for(int i = 0; i < methodNumber; i++){
					for(int j = 0; j < fieldNumber; j++){
						numberOfMethodWithFieldReferences += fieldReferenceMatrix.getValue(i, j);
					}
				}
			}

			// Set the value of the metric LCOM1, LCOM2, or LCOM5
			// 2016/06/01: The special value of these metric is according to Al-Dallal's 2011 IST paper
			int totalMethodPairNumber = (methodNumber * (methodNumber-1))/2; 
			if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.LCOM1)) {
				if (methodNumber <= 1) value = 0;
				else value = totalMethodPairNumber - commonReferencePairNumber;  // The value of LCOM1
			} else if(measure.getIdentifier().equals(SoftwareMeasureIdentifier.LCOM1p)) {
				if (methodNumber <= 1) value = 0;
				else value = (double)(totalMethodPairNumber - commonReferencePairNumber) / totalMethodPairNumber;  // The value of LCOM1p
			} else if(measure.getIdentifier().equals(SoftwareMeasureIdentifier.LCOM2)) {
				if (methodNumber <= 1) value = 0;
				else {
					value = totalMethodPairNumber - 2 * commonReferencePairNumber; // The value of LCOM2
					if (value < 0 || numberOfMethodWithFieldReferences <= 0) value = 0;
				}
			} else if(measure.getIdentifier().equals(SoftwareMeasureIdentifier.LCOM2p)) {
				if (methodNumber <= 1) value = 0;
				else {
					value = totalMethodPairNumber - 2 * commonReferencePairNumber; // The value of LCOM2
					if (value < 0 || numberOfMethodWithFieldReferences <= 0) value = 0;
					value = value / totalMethodPairNumber;
				}
			} else { // The value of LCOM5
				if (methodNumber > 1 && fieldNumber > 0) {
					value = (double)(methodNumber)/(methodNumber-1) - (double)(numberOfMethodWithFieldReferences)/(fieldNumber * (methodNumber - 1));
				} else if (methodNumber > 1 && fieldNumber == 0) value = (double)methodNumber / (methodNumber - 1);
				else if (methodNumber == 0 || (methodNumber == 1 && fieldNumber == 0)) value = 0;
				else {
					value = 2 * (1 - (double)(numberOfMethodWithFieldReferences)/fieldNumber);
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

}
