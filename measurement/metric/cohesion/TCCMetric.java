package measurement.metric.cohesion;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import nameTable.nameDefinition.FieldDefinition;
import softwareStructure.MethodReferenceMatrix;

/**
 * A class to calculate the metric TCC
 * 
 * @author Li Jingsheng
 * @since 2015年08月12日
 * @update 2015/10/10, Zhou Xiaocong
 * @version 1.0
 * 
 */

public class TCCMetric extends SoftwareCohesionMetric {
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 

		double value = 0;
		// A matrix to calculate the TCC metric is indeed a indirect method reference matrix (i.e. two methods have 
		// the relation iff they indirectly refer to common fields
		MethodReferenceMatrix TCCMatrix = structManager.createIndirectMethodReferenceMatrix(type);

//		List<MethodDefinition> methodList = referenceMatrix.getMethodList();
		
		if (TCCMatrix != null) {
			int methodNumber = TCCMatrix.getMethodNumber();
			int edgeNumber = 0;
			// Calculate the edge number in TCCMatrix, i.e. the number of method pair which
			// has the relation (indirectly refer to common fields)
			// TCCMatrix is a symmetric matrix, we just computing its upper triangle
			for(int i = 0; i < methodNumber - 1; i++) {
				for(int j = i + 1; j < methodNumber; j++) {
					edgeNumber += TCCMatrix.getValue(i,  j);
//					if (matrix[i][j] == 1) {
//						Debug.println("TCC: method " + methodList.get(i).getLabel() + " and method " + methodList.get(j).getLabel());
//					}
				}
			}

//			Debug.println("TCC: methodNumber = " + methodNumber + ", edgesNumber = " + edgeNumber);
			int fieldNumber = 0;
			List<FieldDefinition> fieldList = structManager.getAllFieldList(type);
			if (fieldList != null) fieldNumber = fieldList.size();

			value = 2 * edgeNumber;
			if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.TCC)) {
				// 2016/05/08: Set the special value according to Jehad Al Dalla's 2011 paper published on IST
				if (methodNumber != 1 && fieldNumber == 0) value = 0;		
				else if (methodNumber <= 1) value = 1;
				else {
					// Since we just computing the upper triangle of the matrix, so we must
					// multiply the edgeNumber by 2.
					value = value / (methodNumber*(methodNumber-1));
				}
			}
		}
		
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
}