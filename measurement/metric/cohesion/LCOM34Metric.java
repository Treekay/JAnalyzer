package measurement.metric.cohesion;

import java.util.List;

import nameTable.nameDefinition.FieldDefinition;
import graph.basic.GraphUtil;
import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import softwareStructure.MethodInvocationMatrix;
import softwareStructure.MethodReferenceMatrix;

/**
 * @author Zhou Xiaocong
 * @since 2016年6月12日
 * @version 1.0
 */
public class LCOM34Metric extends SoftwareCohesionMetric {

	/* (non-Javadoc)
	 * @see softwareMeasurement.metric.SoftwareStructMetric#calculate(softwareMeasurement.measure.SoftwareMeasure)
	 */
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 
			
		double value = 0;
		MethodReferenceMatrix methodReferenceMatrix = structManager.createDirectMethodReferenceMatrix(type);
		if (methodReferenceMatrix != null) {
			// Note that the rows and columns of the matrix correspond to the methods
			int methodNumber = methodReferenceMatrix.getMethodNumber();
			if (methodNumber > 1) {
				int[][] matrix = methodReferenceMatrix.getMatrix();
				
				if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.LCOM4) || 
						measure.getIdentifier().equals(SoftwareMeasureIdentifier.CoPrim)) {
					MethodInvocationMatrix methodInvocationMatrix = structManager.createDirectStaticMethodInvocationMatrix(type);
					if (methodInvocationMatrix.getMethodNumber() != methodNumber) {
						throw new AssertionError("The method number of method invocation matrix is not equal to method reference matrix!");
					}
					for (int i = 0; i < methodNumber; i++) {
						for (int j = 0; j < methodNumber; j++) {
							if (methodInvocationMatrix.getValue(i, j) == 1 || methodInvocationMatrix.getValue(j, i) == 1) matrix[i][j] = 1;
						}
					}
				}
				
				int componentNumber = GraphUtil.getConnectedComponentNumber(matrix);
				
				int edgeNumber = 0;
				if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.CoPrim)) {
					for (int i = 0; i < methodNumber; i++) {
						for (int j = 0; j < methodNumber; j++) {
							if (i != j) edgeNumber = edgeNumber + matrix[i][j];
						}
					}
				}

				if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.LCOM3) || 
						measure.getIdentifier().equals(SoftwareMeasureIdentifier.LCOM4)) {
					value = componentNumber;
				} else if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.CoPrim)) {
					value = (double)edgeNumber / (methodNumber * (methodNumber -1));
				}
			} else {
				if (methodNumber == 1) value = 1;
				else {
					List<FieldDefinition> fieldList = structManager.getAllFieldList(type);
					if (fieldList != null && fieldList.size() > 0) value = 1;
					else value = 0;
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
