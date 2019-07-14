package measurement.metric.coupling;

import java.util.Set;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * A class to calculate the metric OCAEC
 * 
 * @author Li Jingsheng
 * @since 2015Äê9ÔÂ12ÈÕ
 * @version 1.0
 * @update 2015/10/12, Zhou Xiaocong
 * 
 */
public class OCAECMetric extends SoftwareCouplingMetric{
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;

		double value = 0;
		if (!type.isInterface()) {
			Set<DetailedTypeDefinition> otherTypeSet = structManager.getUsedOtherDetailedTypeDefinitionSet(type);
			for (DetailedTypeDefinition other : otherTypeSet) {
				value += CouplingCalculationUtil.getNumberOfCA(structManager, other, type);
			}
		}

		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
}
