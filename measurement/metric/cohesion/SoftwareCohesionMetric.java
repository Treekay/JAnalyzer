package measurement.metric.cohesion;

import measurement.metric.SoftwareStructMetric;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameScope.NameScope;
import softwareStructure.SoftwareStructManager;

public abstract class SoftwareCohesionMetric implements SoftwareStructMetric {
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
}
