package measurement.metric.inheritance;

import measurement.measure.SoftwareMeasure;

/**
 * A class to calculate the metric DIT, which is the length from this class to the root (i.e. the class Object). 
 * The value will be always >= 1. 
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ2ÈÕ
 * @version 1.0
 * @update 2015/10/14, Zhou Xiaocong
 */
public class DITMetric extends SoftwareInheritanceMetric {

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 

		double value = structManager.getDepthOfInheritance(type);
		measure.setValue(value);
		return true;
	}
}
