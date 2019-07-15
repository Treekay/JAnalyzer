package measurement.metric.inheritance;

import measurement.measure.SoftwareMeasure;

/**
 * A class to calculate the metric SP, which is equals to SPD + SPA
 *   
 * @author Zhou Xiaocong
 * @since 2015年10月14日
 * @version 1.0
 */
public class SPMetric extends SoftwareInheritanceMetric{

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false; 

		double SPAValue = SPAMetric.getStaticPolymorphismInAncestors(structManager, type);
		double SPDValue = SPDMetric.getStaticPolymorphismInDescendants(structManager, type);
		
		measure.setValue(SPAValue + SPDValue);
		return true;
	}

}
