package measurement.metric.coupling;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.MethodDefinition;

/**
 * A class to calculate the metric RFC
 * 
 * @author Li Jingsheng
 * @since 2015年09月01日
 * @update 2015/10/12, Zhou Xiaocong
 *
 */
public class RFCMetric extends SoftwareCouplingMetric{
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;

		// Calculate the response set of the class, i.e. the set of all methods of the class (including inherited methods), 
		// and the methods directly and polymorphically called by the methods of the class.
		List<MethodDefinition> methods = structManager.getAllMethodList(type);
		Set<MethodDefinition> responseSet = new TreeSet<MethodDefinition>();
		responseSet.addAll(methods);
		for(MethodDefinition method : methods) {
			if (method.isAbstract()) continue;
			List<MethodDefinition> invocationMethods = structManager.getDirectPolymorphicInvocationMethodList(method);
			for(MethodDefinition m : invocationMethods) responseSet.add(m);
		}
		
//		Debug.println("RFC: The response set of type " + type.getLabel() + " is following: ");
//		int counter = 0;
//		for (MethodDefinition method : responseSet) {
//			counter = counter + 1;
//			Debug.println("\t" + method.getLabel() + ", counter = " + counter);
//		}
		
		double value = responseSet.size();
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
}