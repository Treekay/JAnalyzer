package measurement.metric.inheritance;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * A class to calculate metric NOA, the number of ancestors including classes and interfaces 
 */
public class NOAMetric extends SoftwareInheritanceMetric {

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;
		
		double value = 0;
		List<DetailedTypeDefinition> ancestorList = structManager.getAllAncestorTypeList(type);
		if (ancestorList != null) {
			value = ancestorList.size();
		}
		
		measure.setValue(value);
		return true;
	}

	
}
