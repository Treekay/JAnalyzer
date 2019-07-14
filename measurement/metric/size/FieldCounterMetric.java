package measurement.metric.size;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import measurement.metric.SoftwareStructMetric;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameScope.NameScope;
import softwareStructure.SoftwareStructManager;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ12ÈÕ
 * @version 1.0
 */
public class FieldCounterMetric implements SoftwareStructMetric {
	protected SoftwareStructManager structManager = null;
	protected DetailedTypeDefinition type = null;

	@Override
	public void setSoftwareStructManager(SoftwareStructManager structManager) {
		this.structManager = structManager;
	}
	
	@Override
	public void setMeasuringObject(NameScope objectScope) {
		if (!(objectScope instanceof DetailedTypeDefinition)) {
			throw new AssertionError("The measure object of cohesion metric must be detailed type, not " + objectScope);
		}
		type = (DetailedTypeDefinition)objectScope;
	}

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (structManager == null || type == null) return false;
		
		if (measure.match(SoftwareMeasureIdentifier.ALLFLD)) {
			double value = 0;
			List<FieldDefinition> fieldList = structManager.getAllFieldList(type);
			if (fieldList != null) value = fieldList.size();
			measure.setValue(value);
		} else if (measure.match(SoftwareMeasureIdentifier.IHFLD)) {
			double value = 0;
			List<FieldDefinition> fieldList = structManager.getAllFieldList(type);
			if (fieldList != null) {
				value = fieldList.size();
				List<FieldDefinition> implementedFieldList = structManager.getImplementedFieldList(type);
				if (implementedFieldList != null) value = value - implementedFieldList.size();
			}
			measure.setValue(value);
		} else return false;
		return true;
	}

}
