package measurement.metric.coupling;

import measurement.measure.SoftwareMeasure;

/**
 * @author Zhou Xiaocong
 * @since 2015年10月12日
 * @version 1.0
 */
public class RFCpMetric extends SoftwareCouplingMetric {

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;

		// Surprisingly, some methods indirectly and polymorphically call many, many methods (up to thousands, or ten thousands 
		// methods), so that it is very time-consuming to calculate the metric RFC' (an alternative version of RFC for considering
		// indirectly polymorphically invocation) for many software projects.  
		// For example, we may spend more than 27 hours for calculating RFC for JDK in a computer with 2.9GHz i5-4460S CUP and 8GB memory 

		// Get the response set of the class, i.e. the set of all methods of the class (including inherited methods), 
		// and the methods directly OR INDIRECTLY and polymorphically called by the methods of the class.
		double value = structManager.getResponseSet(type).size();
//		double value = 0;
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
}
