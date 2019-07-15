package measurement.metric.coupling;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;

/**
 * A class to calculate the metric CBO
 * 
 * @author Li Jingsheng
 * @since 2015年08月24日
 * @update 2015/10/12, Zhou Xiaocong
 *
 */
public class CBOMetric extends SoftwareCouplingMetric{
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;
		// We use SoftwareStructManager to create a global matrix for detailed type using relations for speeding up
		// the calculation of the metric CBO.
		int value = 0;
		
		if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.CBO)) {
			value = structManager.getUsingDetailedTypeNumber(type);
		} else if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.CBOp)) {
			value = structManager.getUsingOtherDetailedTypeNumber(type);
		} else if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.CBOi)) {
			value = structManager.getImportUsingDetailedTypeNumber(type);
		} else if (measure.getIdentifier().equals(SoftwareMeasureIdentifier.CBOe)) {
			value = structManager.getExportUsingDetailedTypeNumber(type);
		}
		
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
}
