package measurement.metric.cohesion;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import nameTable.nameDefinition.FieldDefinition;
import softwareStructure.MethodReferenceMatrix;

/**
 * A class to calulate the metric LCC
 * 
 * @author Li Jingsheng
 * @since 2015年08月12日
 * @update 2015/10/10, Zhou Xiaocong
 * @version 1.0
 */
public class LCCMetric extends SoftwareCohesionMetric {
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 

		// A matrix to calculate the TCC metric is indeed a indirect method reference matrix (i.e. two methods have 
		// the relation iff they indirectly refer to common fields
		MethodReferenceMatrix methodReferenceMatrix = structManager.createIndirectMethodReferenceMatrix(type);
		// The LCC matrix is the transitive closure of the TCC matrix
		MethodReferenceMatrix LCCMatrix = getLCCMatrix(methodReferenceMatrix);
		
//		List<MethodDefinition> methodList = methodReferenceMatrix.getMethodList();
		
		double value = 0;
		if (LCCMatrix != null) {
			int methodNumber = LCCMatrix.getMethodNumber();
			int edgeNumber = 0;
			// Calculate the edge number in LCCMatrix, i.e. the number of method pair which
			// directly or indirectly has the relation (indirectly refer to common fields)
			// LCCMatrix is a symmetric matrix, we just computing its upper triangle
			for(int i = 0; i < methodNumber - 1; i++){
				for(int j = i + 1; j < methodNumber; j++) {
					edgeNumber += LCCMatrix.getValue(i,  j);

//					if (LCCMatrix.getValue(i,  j) == 1) {
//						Debug.println("LCC: method " + methodList.get(i).getLabel() + " and method " + methodList.get(j).getLabel());
//					}
				}
			}								
//			Debug.println("LCC: methodNumber = " + methodNumber + ", edgesNumber = " + edgeNumber);
			
			int fieldNumber = 0;
			List<FieldDefinition> fieldList = structManager.getAllFieldList(type);
			if (fieldList != null) fieldNumber = fieldList.size();
			
			value = 2 * edgeNumber;
			if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.LCC)) {
				// 2016/05/08: Set the special value according to Jehad Al Dalla's 2011 paper published on IST
				if (methodNumber != 1 && fieldNumber == 0) value = 0;		
				else if (methodNumber <= 1) value = 1;
				else {
					// Since we just computing the upper triangle of the matrix, so we must
					// multiply the edgeNumber by 2.
					value = value / ((methodNumber - 1) * methodNumber);
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
	 * Note that a matrix used to calculate the metric TCC is indeed the method reference matrix (i.e. two methods
	 * has the relation iff they refer to common fields). And a matrix used to calculate the metric LCC is the 
	 * transitive closure of a TCC matrix. 
	 */
	private MethodReferenceMatrix getLCCMatrix(MethodReferenceMatrix methodReferenceMatrix) {
		if (methodReferenceMatrix == null) return null;
		int[][] TCCMatrix = methodReferenceMatrix.getMatrix();
		int[][] LCCMatrix = structManager.getTransitiveClosure(TCCMatrix, TCCMatrix.length);
		methodReferenceMatrix.setMatrix(LCCMatrix);
		return methodReferenceMatrix;
	}
}
