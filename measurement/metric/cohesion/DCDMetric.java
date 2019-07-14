package measurement.metric.cohesion;

import java.util.List;
import java.util.Set;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import softwareStructure.MethodReferenceMatrix;
import softwareStructure.SoftwareStructManager;

/**
 * A class to calculate the metric DCD
 * 
 * @author Li Jingsheng
 * @since 2015Äê08ÔÂ13
 * @update 2015/10/10, Zhou Xiaocong
 */
public class DCDMetric extends SoftwareCohesionMetric {
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 
		
		List<FieldDefinition> fieldList = structManager.getDeclaredFieldList(type);
		int fieldNumber = 0;
		if (fieldList != null) fieldNumber = fieldList.size();
		
		double value = 0;
		MethodReferenceMatrix methodReferenceMatrix = structManager.createIndirectMethodReferenceMatrix(type);
		methodReferenceMatrix = getDCDMatrix(structManager, methodReferenceMatrix);
		if (methodReferenceMatrix != null) {
			int methodNumber = methodReferenceMatrix.getMethodNumber();
			int edgeNumber = 0;
			// Calculate the edge number in DCDMatrix, i.e. the number of method pair which
			// has the relation (refer to common fields or call common methods)
			// DCDMatrix is a symmetric matrix, we just computing its upper triangle
			for(int i = 0; i < methodNumber - 1; i++){
				for(int j = i + 1; j < methodNumber; j++){
					edgeNumber += methodReferenceMatrix.getValue(i, j);
				}
			}
			
//			Debug.println("DCD: nodeNum = " + nodeNum + ", edgeNum = " + edgeNum);
			
			value = 2.0 * edgeNumber;
			if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.DCD)) {
				// 2016/05/08: Set the special value according to Jehad Al Dalla's 2011 paper published on IST
				if (methodNumber == 0 && fieldNumber == 0) value = 0;
				else if (methodNumber <= 1) value = 1;
				else {
					// Since we just computing the upper triangle of the matrix, so we must
					// multiply the edgeNumber by 2.
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
	 * Calculate the DCDMatrix (i.e. a matrix for calculating DCD metric) based on a method reference matrix.
	 * <p>A method reference matrix gives the relation between two methods which (indirectly) refer to common fields. 
	 * In DCDMatrix, if method i and method j refer to common fields, or method i calls method j, or method j calls 
	 * method i, or both method i and method j call some common methods, then DCDMatrix[i][j] = 1.
	 */
	public static MethodReferenceMatrix getDCDMatrix(SoftwareStructManager manager, MethodReferenceMatrix matrix){
		if (matrix == null) return null;

		List<MethodDefinition> methodList = matrix.getMethodList();
		if (methodList == null) return null;
		
		int methodNumber = methodList.size();
		for (int indexOne = 0; indexOne < methodNumber-1; indexOne++) {
			MethodDefinition methodOne = methodList.get(indexOne);
			Set<MethodDefinition> oneCallSet = manager.getStaticInvocationMethodSet(methodOne);
			for (int indexTwo = indexOne + 1; indexTwo < methodNumber; indexTwo++) {
				MethodDefinition methodTwo = methodList.get(indexTwo);
				Set<MethodDefinition> twoCallSet = manager.getStaticInvocationMethodSet(methodTwo);
				if (oneCallSet.contains(methodTwo) || twoCallSet.contains(methodOne)) {
					// methodOne call methodTwo, or methodTwo call methodOne
					matrix.setReferenceRelation(methodOne, methodTwo);
					matrix.setReferenceRelation(methodTwo, methodOne);
					
//					Debug.println("method " + methodOne.getLabel() + " and method " + methodTwo.getLabel() + " call themself!");
				} else {
					// retainAll will make oneCallSet be the intersection of oneCallSet and twoCallSet
					oneCallSet.retainAll(twoCallSet);
					if (!oneCallSet.isEmpty()) {
						// Both methodOne and methodTwo call the methods in the set oneCallSet. 
						matrix.setReferenceRelation(methodOne, methodTwo);
						matrix.setReferenceRelation(methodTwo, methodOne);

//						Debug.println("Both method " + methodOne.getLabel() + " and method " + methodTwo.getLabel() + " call some methods!");
					}
				}
			}
		}
		return matrix;
	}
}
