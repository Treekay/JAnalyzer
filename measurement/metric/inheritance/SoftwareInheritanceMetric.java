package measurement.metric.inheritance;

import measurement.metric.SoftwareStructMetric;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameScope.NameScope;
import softwareStructure.SoftwareStructManager;

/**
 * The abstract base class for calculating inheritance metric. 
 * Note that inheritance metric only for detailed type definition.
 * 
 * @author Wu Zhangshen
 * @since 2015/9/13
 * @update 2015/10/13
 */
public abstract class SoftwareInheritanceMetric implements SoftwareStructMetric{

	protected SoftwareStructManager structManager = null;
	protected DetailedTypeDefinition type = null;

	@Override
	public void setSoftwareStructManager(SoftwareStructManager structManager) {
		this.structManager = structManager;
	}

	@Override
	public void setMeasuringObject(NameScope objectScope) {
		if (!(objectScope instanceof DetailedTypeDefinition)) {
			throw new AssertionError("The measure object of inheritance metric must be detailed type, not " + objectScope);
		}
		type = (DetailedTypeDefinition)objectScope;
	}

}
