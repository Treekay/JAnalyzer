package measurement.metric.cohesion;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.TypeDefinition;
import softwareStructure.SoftwareStructManager;

/**
 * A class to calculate CAMC metric. 
 *  
 * @author Li Jingsheng
 * @since 2015年08月16日
 * @update 2015/10/10, Zhou Xiaocong
 *
 */
public class CAMCMetric extends SoftwareCohesionMetric {
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 

		double value = 0;
		
		List<MethodDefinition> methodList = structManager.getImplementedMethodList(type);
		List<TypeDefinition> parameterTypeList = structManager.getParameterTypeList(type);
		if (methodList != null && parameterTypeList != null) {
			int[][] POMatrix = getPOMatrix(structManager, methodList, parameterTypeList);
			int methodNumber = methodList.size();
			int parameterTypeNumber = parameterTypeList.size();
			
			// Get the summation of the values in POMatrix
			int sum = 0; 
			for (int i = 0; i < methodNumber; i++) {
				for (int j=0; j < parameterTypeNumber; j++) sum += POMatrix[i][j];
			}
			
			// 2016/05/08: Set the special value according to Jehad Al Dalla's 2011 paper published on IST
			if (methodNumber > 1 && parameterTypeNumber > 0) {
				// In this case, CAMC metric is equal to the sum of the POMatrix divide (methodNumber * parameterTypeNumber)
				value = (double)sum / (methodNumber * parameterTypeNumber);
			} else if (methodNumber == 1 || (methodNumber == 0 && parameterTypeNumber > 0)) value = 1;
			else value = 0; // i.e. (methodNumber != 1 && parameterTypeNumber == 0)
		}
		
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
	
	
	/**
	 * Create a POMatrix for a method list and parameter type list. The parameter type list gets all types of parameters occurring in 
	 * the methods in the method list. For method i, and parameter type j, POMatrix[i][j] == 1 iff method i has a parameter whose type is
	 *  type j.  
	 *  
	 *  <p>This method is not private and static, because it is also used by NHDMetric. 
	 */
	static int[][] getPOMatrix(SoftwareStructManager manager, List<MethodDefinition> methodList, List<TypeDefinition> parameterTypeList) {
		if (methodList == null || parameterTypeList == null) return null;
		int methodNumber = methodList.size();
		int parameterTypeNumber = parameterTypeList.size();

		int[][] POMatrix = new int[methodNumber][parameterTypeNumber];
		for (int i = 0; i < methodNumber; i++) {
			MethodDefinition method = methodList.get(i);
			for (int j = 0; j < parameterTypeNumber; j++) {
				List<TypeDefinition> methodParaTypeList = manager.getParameterTypeList(method);
				if (methodParaTypeList.contains(parameterTypeList.get(j))) {
					POMatrix[i][j] = 1;
				} else {
					POMatrix[i][j] = 0;
				}
			}
		}
		return POMatrix;
	}
}
