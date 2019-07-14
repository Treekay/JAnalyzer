package measurement.metric.coupling;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.VariableDefinition;
import softwareStructure.MethodWithCallInformation;
import softwareStructure.SoftwareStructManager;

/**
 * A class to calculate the metric ICP
 * 
 * @author Li Jingsheng
 * @since 2015Äê09ÔÂ14ÈÕ
 * @update 2015/10/12, Zhou Xiaocong 
 *
 */
public class ICPMetric extends SoftwareCouplingMetric {
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;

		double value = getNIHICPMetric(structManager, type) + getIHICPMetric(structManager, type);
		
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
	
	/**
	 * Get the NIH-ICP metric of the given class. The NIH-ICP metric equals to the sum of the number of direct and 
	 * polymoprhic calling to other methods m' (which is not the method of the class or its ancestor classes) by the 
	 * implemented methods in the class multiplied by the parameter number (plus one) of m'
	 * <p>This method is public and static since it may be used by the class to calculate NIHICP metric   
	 */
	public static double getNIHICPMetric(SoftwareStructManager structManager, DetailedTypeDefinition type){
		List<MethodDefinition> allMethodList = structManager.getAllMethodList(type);
		if (allMethodList == null) return 0;
		List<MethodDefinition> implementedMethodList = structManager.getImplementedMethodList(type);
		if (implementedMethodList == null) return 0;
		
		double result = 0;
		for(MethodDefinition implementedMethod : implementedMethodList){
			// We get all direct and polymorphic calling and its number for the method implementedMethod.
			List<MethodWithCallInformation> calledMethodList = structManager.getDirectPolymorphicInvocationMethodWithCallInformationList(implementedMethod);
			for (MethodWithCallInformation calledMethod : calledMethodList) {
				MethodDefinition method = calledMethod.getMethod();
				if (!allMethodList.contains(method)) {
					// And then, when the method definition in calledMethod is not the method in the class or in the ancestors of the 
					// class, we need add its parameter number (plus 1) (multiplied by call number) to the result.
					List<VariableDefinition> parameterList = structManager.getParameterList(method);
					int parameterNumber = 0;
					if (parameterList != null) parameterNumber = parameterList.size();
					int callNumber = calledMethod.getCallNumber();
					result = result + (1 + parameterNumber) * callNumber;
				}
			}
		}
		return result;
	}
	
	/**
	 * Get the IH-ICP metric of the given class. The IH-ICP metric equals to the sum of the number of direct and 
	 * polymoprhic calling to the methods m' in the ancestor classes of the class by the 
	 * implemented methods in the class multiplied by the parameter number (plus one) of m'   
	 * <p>This method is public and static since it may be used by the class to calculate IHICP metric   
	 */
	public static double getIHICPMetric(SoftwareStructManager structManager, DetailedTypeDefinition type){
		List<MethodDefinition> inheritedMethodList = structManager.getAllInheritedMethodList(type);
		if (inheritedMethodList == null) return 0;
		List<MethodDefinition> implementedMethodList = structManager.getImplementedMethodList(type);
		if (implementedMethodList == null) return 0;
		
		double result = 0;
		for(MethodDefinition implementedMethod : implementedMethodList){
			// We get all direct and polymorphic calling and its number for the method implementedMethod.
			List<MethodWithCallInformation> calledMethodList = structManager.getDirectPolymorphicInvocationMethodWithCallInformationList(implementedMethod);
			for (MethodWithCallInformation calledMethod : calledMethodList) {
				MethodDefinition method = calledMethod.getMethod();
				if (inheritedMethodList.contains(method)) {
					// And then, when the calledMethod is the method in the ancestors of the class, we 
					// only need add its parameter number (plus 1) (multiplied by call number) to the result.
					List<VariableDefinition> parameterList = structManager.getParameterList(method);
					int parameterNumber = 0;
					if (parameterList != null) parameterNumber = parameterList.size();
					int callNumber = calledMethod.getCallNumber();
					result = result + (1 + parameterNumber) * callNumber;
				}
			}
		}
		return result;
	}
}