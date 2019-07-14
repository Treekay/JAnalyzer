package measurement.metric.coupling;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.MethodDefinition;
import softwareStructure.MethodWithCallInformation;

/**
 * A class to calculate the metric MPC
 * 
 * @author Li Jingsheng
 * @since 2015Äê09ÔÂ09ÈÕ
 * @update 2015/10/12, Zhou Xiaocong 
 *
 */
public class MPCMetric extends SoftwareCouplingMetric{
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;

		double value = 0;
		List<MethodDefinition> methodList = structManager.getImplementedMethodList(type);
		if (methodList != null) {
			// The MPC metric equals to the sum of the numbers of calling non-implemented methods of the 
			// class by the implemented methods of the class
			for (MethodDefinition implementedMethod : methodList) {
				// We get all direct and static calling and its number for the method implementedMethod.
				List<MethodWithCallInformation> calledMethodList = structManager.getDirectPolymorphicInvocationMethodWithCallInformationList(implementedMethod);
				for (MethodWithCallInformation calledMethod : calledMethodList) {
					MethodDefinition method = calledMethod.getMethod();
					if (!methodList.contains(method)) {
						// This means the calledMethod is not an implemented method of the class, so we add call number to 
						// the metric value
						int callNumber = calledMethod.getCallNumber();
						value = value + callNumber;
					}
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
