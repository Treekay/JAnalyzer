package measurement.metric.coupling;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.TypeDefinition;

/**
 * A class to calculate the metric OMMIC
 * 
 * @author Li Jingsheng
 * @since 2015年9月12日
 * @version 1.0
 * @update 2015/10/12, Zhou Xiaocong
 * 
 */
public class DACpMetric extends SoftwareCouplingMetric{

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;

		double value = 0;
		// Here we use the implemented fields of the class to calculate DAC, SoftwareStructManager.getImplementedFieldTypes()
		// returns all type definitions occurring in the declarations of the implemented fields in the class
		List<TypeDefinition> fieldTypeList = structManager.getImplementedFieldTypeList(type);
		if (fieldTypeList != null) {
			// Calculate the sum of detailed types occurring in the declarations of the implemented fields. 
			for (TypeDefinition fieldType : fieldTypeList) {
				if (fieldType.isDetailedType() && fieldType != type) value = value + 1;
			}
		}
		
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}
}
