package measurement.metric.coupling;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * A class to calculate the metric DCMEC
 * 
 * @author Li Jingsheng
 * @since 2015Äê9ÔÂ12ÈÕ
 * @version 1.0
 * @update 2015/10/12, Zhou Xiaocong
 * 
 */
public class DCMECMetric extends SoftwareCouplingMetric{

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;

		double value = 0;
		List<DetailedTypeDefinition> descendantList = structManager.getAllDescendantTypeList(type);
		if (descendantList != null) {
			for(DetailedTypeDefinition descendant : descendantList){
//				Debug.println("DCMEC: Number of CM " + type.getLabel() + " used by descendant " + descendant.getLabel() + ", value = " + value);
				value += CouplingCalculationUtil.getNumberOfCM(structManager, descendant, type);
			}
		}
		
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
}
