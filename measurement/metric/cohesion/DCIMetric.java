package measurement.metric.cohesion;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import nameTable.nameDefinition.FieldDefinition;
import softwareStructure.MethodReferenceMatrix;
import softwareStructure.SoftwareStructManager;

/**
 * A class to calculate the metric DCD
 * 
 * @author Li Jingsheng
 * @since 2015Äê08ÔÂ13ÈÕ
 * @update 2015/10/10, Zhou Xiaocong
 *
 */
public class DCIMetric extends SoftwareCohesionMetric{
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 

		List<FieldDefinition> fieldList = structManager.getDeclaredFieldList(type);
		int fieldNumber = 0;
		if (fieldList != null) fieldNumber = fieldList.size();
		
		double value = 0;
		MethodReferenceMatrix methodReferenceMatrix = structManager.createIndirectMethodReferenceMatrix(type);
		MethodReferenceMatrix DCIMatrix = getDCIMatrix(structManager, methodReferenceMatrix);
		if (DCIMatrix != null) {
			int methodNumber = DCIMatrix.getMethodNumber();
			int edgeNumber = 0;
			// Calculate the edge number in DCIMatrix, i.e. the number of method pair which
			// directly or indirectly has the relation (refer to common fields or call common methods).
			// Note that DCIMatrix is the transitive closure of DCDMatrix 
			// DCIMatrix is a symmetric matrix, we just computing its upper triangle
			for(int i = 0; i < methodNumber - 1; i++) {
				for(int j = i + 1; j < methodNumber; j++) {
					edgeNumber += DCIMatrix.getValue(i,  j);
				}
			}

			value = 2 * edgeNumber;
			if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.DCI)) {
				// 2016/05/08: Set the special value according to Jehad Al Dalla's 2011 paper published on IST
				if (methodNumber == 0 && fieldNumber == 0) value = 0;
				else if (methodNumber <= 1) value = 1;
				else {
					value = value / (methodNumber * (methodNumber - 1));
				}
			}
		}

		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
	
	/**
	 * Calculate the DCIMatrix (i.e. a matrix for calculating DCI metric) based on a method reference matrix.
	 * <p>A DCIMatrix is the transitive closure of a DCDMatrix.
	 * @see DCDMetric.getDCDMatrix() 
	 */
	public MethodReferenceMatrix getDCIMatrix(SoftwareStructManager structManager, MethodReferenceMatrix methodReferenceMatrix){
		methodReferenceMatrix = DCDMetric.getDCDMatrix(structManager, methodReferenceMatrix);
		if (methodReferenceMatrix == null) return null;
		int[][] matrix = methodReferenceMatrix.getMatrix();
		matrix = structManager.getTransitiveClosure(matrix, matrix.length);
		methodReferenceMatrix.setMatrix(matrix);
		return methodReferenceMatrix;
	}
}
