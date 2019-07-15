package measurement.metric.size;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameTableVisitor;
import softwareStructure.SoftwareStructManager;

/**
 * A class calculates and buffers the definition numbers of a given systems, i.e. it calculates the following size measures:
 * "FILE", "PKG", "CLS", "INTF", "ENUM", "FLD", "MTHD", "PARS" and "LOCV" 
 * @author Zhou Xiaocong
 * @since 2015年7月7日
 * @version 1.0
 */
public class DefinitionCounterMetric extends SoftwareSizeMetric {

	private NameDefinitionCounter counter = new NameDefinitionCounter();

	@Override
	public void setSoftwareStructManager(SoftwareStructManager structManager) {
		if (structManager != this.structManager) {
			this.structManager = structManager;
			tableManager = structManager.getNameTableManager();
			parser = tableManager.getSouceCodeFileSet();
			
			counter.reset();
		}
	}

	@Override
	public void setMeasuringObject(NameScope objectScope) {
		if (this.objectScope != objectScope) {
			this.objectScope = objectScope;
			
			counter.reset();
		}
	}
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (tableManager == null || objectScope == null) return false;
		
		if (counter.getUnitNumber() == 0 && counter.getClassNumber() == 0 && counter.getEnumNumber() == 0 && counter.getInterfaceNumber() == 0) {
			// We traverse the name table again only when the unit number in the counter is zero!
			counter.reset();
			objectScope.accept(counter);
		}
		
		if (measure.match(SoftwareMeasureIdentifier.FILE)) {
			measure.setValue(counter.getUnitNumber());
		} else if (measure.match(SoftwareMeasureIdentifier.PKG)) {
			measure.setValue(counter.getPackageNumber());
		} else if (measure.match(SoftwareMeasureIdentifier.CLS)) {
			measure.setValue(counter.getClassNumber());
		} else if (measure.match(SoftwareMeasureIdentifier.INTF)) {
			measure.setValue(counter.getInterfaceNumber());
		} else if (measure.match(SoftwareMeasureIdentifier.ENUM)) {
			measure.setValue(counter.getEnumNumber());
		} else if (measure.match(SoftwareMeasureIdentifier.MTHD)) {
			measure.setValue(counter.getMethodNumber());
		} else if (measure.match(SoftwareMeasureIdentifier.FLD)) {
			measure.setValue(counter.getFieldNumber());
		} else if (measure.match(SoftwareMeasureIdentifier.LOCV)) {
			measure.setValue(counter.getVariableNumber());
		} else if (measure.match(SoftwareMeasureIdentifier.PARS)) {
			measure.setValue(counter.getParameterNumber());
		} else return false;
		
		return true;
	}

}

class NameDefinitionCounter extends NameTableVisitor {
	private int unitNumber = 0;
	private int packageNumber = 0;
	private int classNumber = 0;
	private int interfaceNumber = 0;
	private int enumNumber = 0;
	private int fieldNumber = 0;
	private int methodNumber = 0;
	private int parameterNumber = 0;
	private int variableNumber = 0;
	
	public void reset() {
		unitNumber = 0;
		packageNumber = 0;
		classNumber = 0;
		interfaceNumber = 0;
		enumNumber = 0;
		fieldNumber = 0;
		methodNumber = 0;
		parameterNumber = 0;
		variableNumber = 0;
	}
	
	public int getUnitNumber() {
		return unitNumber;
	}
	
	public int getPackageNumber() {
		return packageNumber;
	}

	public int getClassNumber() {
		return classNumber;
	}

	public int getInterfaceNumber() {
		return interfaceNumber;
	}

	public int getEnumNumber() {
		return enumNumber;
	}

	public int getFieldNumber() {
		return fieldNumber;
	}

	public int getMethodNumber() {
		return methodNumber;
	}

	public int getParameterNumber() {
		return parameterNumber;
	}

	public int getVariableNumber() {
		return variableNumber;
	}

	/**
	 * Just go to its children for system scope!
	 */
	public boolean visit(SystemScope scope) {
		return true;
	}
	
	/**
	 * Counter the package number and go to its children for a package definition
	 */
	public boolean visit(PackageDefinition scope) {
		packageNumber++;
		return true;
	}
	
	/**
	 * Counter the unit file
	 */
	public boolean visit(CompilationUnitScope scope) {
		unitNumber++;
		return true;
	}
	
	/**
	 * Counter the detailed type (class or interface), and its fields
	 */
	public boolean visit(DetailedTypeDefinition scope) {
		if (scope.isInterface()) interfaceNumber++;
		else classNumber++;
		
		List<FieldDefinition> fields = scope.getFieldList();
		if (fields != null) fieldNumber += fields.size();
		return true;
	}
	
	/**
	 * Counter the enum type
	 */
	public boolean visit(EnumTypeDefinition scope) {
		enumNumber++;
		return false;
	}
	
	/**
	 * Count the method and its parameters
	 */
	public boolean visit(MethodDefinition scope) {
//		if (scope.isAutoGenerated()) return false;
		methodNumber++;
		List<VariableDefinition> vars = scope.getParameterList();
		if (vars != null) {
			parameterNumber += vars.size();
		}
		return true;
	}

	/**
	 *Counter local variables
	 */
	public boolean visit(LocalScope scope) {
		List<VariableDefinition> vars = scope.getVariableList();
		if (vars != null) {
			variableNumber += vars.size();
		}
		return true;
	}
	
}
