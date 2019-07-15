package measurement;

import nameTable.nameDefinition.MethodDefinition;
import softwareStructure.SoftwareStructManager;

/**
 * @author Zhou Xiaocong
 * @since 2015年7月11日
 * @version 1.0
 */
public class MethodMeasurement extends NameScopeMeasurement {

	public MethodMeasurement(MethodDefinition scope, SoftwareStructManager manager) {
		super(scope, manager);
	}

	public MethodDefinition getMethod() {
		return (MethodDefinition)scope;
	}
}
