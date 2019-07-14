package measurement.metric.cohesion;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.TypeDefinition;

/**
 * A class to calculate NHD metric. 
 * 
 * @author Li Jingsheng
 * @since 2015Äê08ÔÂ16ÈÕ
 * @update 2015/10/10, Zhou Xiaocong
 *
 */
public class NHDMetric extends SoftwareCohesionMetric {
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 

		double value = 0;
		
		List<MethodDefinition> methodList = structManager.getImplementedMethodList(type);
		List<TypeDefinition> parameterTypeList = structManager.getParameterTypeList(type);
		if (methodList != null && parameterTypeList != null) {
			int[][] POMatrix = CAMCMetric.getPOMatrix(structManager, methodList, parameterTypeList);
			int methodNumber = methodList.size();
			int parameterTypeNumber = parameterTypeList.size();
			
			// Calculate the sum of Hamming distance between two methods using the PO matrix
			int sum = 0;
			for(int j = 0; j < parameterTypeNumber; j++){
				int columnSum = getColumnSum(POMatrix, j);
				sum += columnSum * (methodNumber - columnSum);
			}
			
			// 2016/05/08: Set the special value according to Jehad Al Dalla's 2011 paper published on IST
			if (methodNumber > 1 && parameterTypeNumber == 0) value = 1;
			else if (methodNumber <= 1) value = 0;
			else if (parameterTypeNumber > 0) {
				value = 1 - (double)(2 * sum) / (parameterTypeNumber * methodNumber * (methodNumber - 1));
			}
		}
		
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
	
	/**
	 * Calculate the number of 1 in the given column of the matrix
	 */
	public int getColumnSum(int[][] matrix, int column) {
		int sum = 0;  
		for(int i = 0; i < matrix.length; i++) sum += matrix[i][column];
		return sum;
	}
}
