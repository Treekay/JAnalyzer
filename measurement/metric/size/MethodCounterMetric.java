package measurement.metric.size;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import measurement.metric.SoftwareStructMetric;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameScope.NameScope;
import softwareStructure.SoftwareStructManager;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ12ÈÕ
 * @version 1.0
 */
public class MethodCounterMetric implements SoftwareStructMetric {
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
		
		double value = 0;
		List<MethodDefinition> methodList = null;
		if (measure.match(SoftwareMeasureIdentifier.ALLMTHD)) {
			methodList = structManager.getAllMethodList(type);
		} else if (measure.match(SoftwareMeasureIdentifier.IHMTHD)) {
			methodList = structManager.getInheritedMethods(type);
		} else if (measure.match(SoftwareMeasureIdentifier.NEWMTHD)) {
			methodList = structManager.getNewMethods(type);
		} else if (measure.match(SoftwareMeasureIdentifier.OVMTHD)) {
			methodList = structManager.getOverriddenMethods(type);
		} else if (measure.match(SoftwareMeasureIdentifier.IMPMTHD)) {
			methodList = structManager.getImplementedMethodList(type);
		} else return false;

		if (methodList != null) value = methodList.size();
		measure.setValue(value);
		return true;
	}
}
