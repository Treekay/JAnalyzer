package measurement;

import nameTable.nameDefinition.DetailedTypeDefinition;
import softwareStructure.SoftwareStructManager;

/**
 * @author Zhou Xiaocong
 * @since 2015年7月11日
 * @version 1.0
 */
public class ClassMeasurement extends NameScopeMeasurement {

	public ClassMeasurement(DetailedTypeDefinition scope, SoftwareStructManager manager) {
		super(scope, manager);
	}
	
	public DetailedTypeDefinition getClassType() {
		return (DetailedTypeDefinition)scope;
	}
}
