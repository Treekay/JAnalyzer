package measurement;

import nameTable.nameScope.SystemScope;
import softwareStructure.SoftwareStructManager;

/**
 * @author Zhou Xiaocong
 * @since 2015年7月11日
 * @version 1.0
 */
public class SystemScopeMeasurement extends NameScopeMeasurement {

	public SystemScopeMeasurement(SystemScope scope, SoftwareStructManager manager) {
		super(scope, manager);
	}
	
	public SystemScope getSystemScope() {
		return (SystemScope)scope;
	}
}
