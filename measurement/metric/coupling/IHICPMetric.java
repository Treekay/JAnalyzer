package measurement.metric.coupling;

import measurement.measure.SoftwareMeasure;

/**
 * A class to calculate the metric IHICP 
 * 
 * @author Li Jingsheng
 * @since 2015年09月14日
 * @update 2015/10/12, Zhou Xiaocong 
 *
 */
public class IHICPMetric extends SoftwareCouplingMetric{

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;

		// Use the ICPMetric.getIHICPMetric() to calculate the metric value
		double value = ICPMetric.getIHICPMetric(structManager, type);
		
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
}
