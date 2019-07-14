package measurement.metric.inheritance;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.MethodDefinition;

/**
 * A class to calculate the metric SIX, which is equals to (the number of methods overridden *
 * the depth in inheritance tree) / (the number of methods overridden + the number of new methods + 
 * the number of methods inherited) 
 *   
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ14ÈÕ
 * @version 1.0
 */
public class SIXMetric extends SoftwareInheritanceMetric {

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;
		
		int numberOfOverriddenMethods = 0;
		List<MethodDefinition> methodList = structManager.getOverriddenMethods(type);
		if (methodList != null) numberOfOverriddenMethods = methodList.size();
		
		int depthOfInheritance = structManager.getDepthOfInheritance(type);
		
		int numberOfNewMethods = 0;
		methodList = structManager.getNewMethods(type);
		if (methodList != null) numberOfNewMethods = methodList.size();
		
		int numberOfInheritedMethods = 0;
		methodList = structManager.getInheritedMethods(type);
		if (methodList != null) numberOfInheritedMethods = methodList.size();

		double value = 0;
		if ((numberOfOverriddenMethods + numberOfNewMethods + numberOfInheritedMethods) > 0) {
			value = (double)(numberOfOverriddenMethods * depthOfInheritance) / (numberOfOverriddenMethods + numberOfNewMethods + numberOfInheritedMethods);
		}
		
		measure.setValue(value);
		return true;
	}

}
