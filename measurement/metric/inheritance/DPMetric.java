package measurement.metric.inheritance;

import measurement.measure.SoftwareMeasure;

/**
 * A class to calculate the metric DP, which is equals to DPD + DPA
 *   
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ14ÈÕ
 * @version 1.0
 */
public class DPMetric extends SoftwareInheritanceMetric {

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 

		double DPAValue = DPAMetric.getDynamicPolymorphismInAncestors(structManager, type);
		double DPDValue = DPDMetric.getDynamicPolymorphismInDescendants(structManager, type);
		
		measure.setValue(DPAValue + DPDValue);
		return true;
	}

}
