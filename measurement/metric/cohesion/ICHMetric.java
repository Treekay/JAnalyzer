package measurement.metric.cohesion;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.VariableDefinition;

/**
 * A class to calculate the metric ICH
 * 
 * @author Li Jingsheng
 * @since 2015Äê08ÔÂ16ÈÕ
 * @update 2015/10/10, Zhou Xiaocong
 *
 */
public class ICHMetric extends SoftwareCohesionMetric {

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 

		double value = 0;
		List<MethodDefinition> implementMethodList = structManager.getImplementedMethodList(type);
		if (implementMethodList != null) {
			// The ICH value of the class is the sum of ICH values of the implemented methods of the class
			for (MethodDefinition method : implementMethodList) {
				value = value + getICHForMethod(method);
			}
		}

		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
	
	/**
	 * Calculate the ICH value of the given method. It equals to the weighted sum of the number of method 
	 * calls which the given method calls another method m' with the parameter number (plus one) as the 
	 * weight, i.e. ICH(m) = sum_{m' in override or new methods of the class} NPI(m, m')*1+|Par(m')|, where
	 * NPI(m, m') is the number of method m polymorphically calling method m', and |Par(m')| is the number
	 * of parameters of method m'.
	 */
	private int getICHForMethod(MethodDefinition method) {
		int result = 0;
		// In our implementation, the override and new methods of a class equals to the methodList
		// stored in the detailed type definition 
		List<MethodDefinition> methodList = type.getMethodList();
		
		if (methodList == null) return 0;
		for (MethodDefinition callMethod : methodList) {
			int callNumber = structManager.getDirectPolymorphicInvocationNumber(method, callMethod);
			if (callNumber > 0) {
				List<VariableDefinition> parameterList = structManager.getParameterList(callMethod);
				int parameterNumber = 0;
				if (parameterList != null) parameterNumber = parameterList.size();
				result = result + callNumber * (1 + parameterNumber);
			}
		}
		
		return result;
	}
	
}
