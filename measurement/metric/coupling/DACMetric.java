package measurement.metric.coupling;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.TypeDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2015年10月12日
 * @version 1.0
 */
public class DACMetric extends SoftwareCouplingMetric {

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;

		double value = 0;
		// Here we use the implemented fields of the class to calculate DAC
		List<FieldDefinition> fieldList = structManager.getImplementedFieldList(type);
		if (fieldList != null) {
			// Calculate the sum of field whose type is a detailed type
			for (FieldDefinition field : fieldList) {
				// Here we consider the parameterized type
				List<TypeDefinition> fieldTypeList = field.getTypeDefinition(true);
				if (fieldTypeList != null) {
					for (TypeDefinition fieldType : fieldTypeList) {
						if (fieldType.isDetailedType()) {
							// The declaration of this field has used detailed type 
							value = value + 1;
							break;
						}
					}
				}
			}
		}
		
		// Store the value to the measure
		measure.setValue(value);
		// The value of the measure is usable.
		measure.setUsable();
		return true;
	}

}
