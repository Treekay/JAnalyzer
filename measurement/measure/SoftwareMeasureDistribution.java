package measurement.measure;

import java.io.PrintWriter;
import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

/**
 * A class to store the distribution of a measure. The distribution of a measure is the value of 
 * the measure for each program entity (i.e. a name scope). 
 * <p> For building a distribution, you must set all name scopes at first, and then you can set all values, 
 * or set a value for each name scope.
 * @author Zhou Xiaocong
 * @since 2015年7月3日
 * @version 1.0
 */
public class SoftwareMeasureDistribution {

	protected String identifier = null;
	// To mark if the whole distribution is usable or not
	protected boolean usable = false;

	// The distribution of a measure is the value of the measure for each program entity (i.e. a name scope).
	protected double[] valueArray = null;
	// To mark if the corresponding value is usable or not
	protected boolean[] valueUsableArray = null;
	protected NameScope[] scopeArray = null;
	protected NameScopeKind scopeKind = NameScopeKind.NSK_DETAILED_TYPE;

	public SoftwareMeasureDistribution(String identifier) {
		this.identifier = identifier;
	}

	public SoftwareMeasureDistribution(SoftwareMeasure measure) {
		this.identifier = measure.identifier;
	}
	
	public String getIdentifier() {
		return identifier;
	}


	public double[] getValueArray() {
		return valueArray;
	}

	/**
	 * Test if the whole distribution is usable or not 
	 */
	public boolean isUsable() {
		return usable;
	}


	/**
	 * Set the whole distribution to be unusable (i.e. to be not usable) 
	 */
	public void setUnusable() {
		this.usable = false;
	}

	/**
	 * Set the whole distribution to be usable (Note that this does not mean all of its values are usable) 
	 */
	public void setUsable() {
		this.usable = true;
	}

	/**
	 * Test if the value in the index position is usable or not
	 */
	public boolean isUsable(int index) {
		return valueUsableArray[index];
	}
	
	/**
	 * Set all the usable value to be false. 
	 * <p>If it is successful return true, else (e.g. you do not set name scope array first) return false
	 */
	public boolean setAllUnusable() {
		if (scopeArray == null) return false;
		if (scopeArray.length <= 0) return false;
		
		if (valueUsableArray == null) {
			valueUsableArray = new boolean[scopeArray.length];
		}
		for (int index = 0; index < valueUsableArray.length; index++) valueUsableArray[index] = false;
		return true;
	}
	
	/**
	 * Set the usable value to be false for the value in the index position
	 * <p>If it is successful return true, else (e.g. you do not set name scope array first) return false
	 */
	public boolean setUnusable(int index) {
		if (scopeArray == null) return false;
		if (scopeArray.length <= 0) return false;
		
		if (valueUsableArray == null) {
			valueUsableArray = new boolean[scopeArray.length];
		}
		valueUsableArray[index] = false;
		return true;
	}

	/**
	 * Set all values of all name scope, and set all values to be usable
	 * <p>If it is successful return true, else (e.g. you do not set name scope array first) return false
	 */
	public boolean setValueArray(double[] valueArray) {
		if (scopeArray == null) return false;
		if (valueArray.length != scopeArray.length) return false;
		
		this.valueArray = valueArray;
		if (valueUsableArray == null) valueUsableArray = new boolean[scopeArray.length];
		for (int index = 0; index < valueUsableArray.length; index++) valueUsableArray[index] = true;
		return true;
	}
	
	/**
	 * Set the value for the index position, and set this value is usable
	 * <p>If it is successful return true, else (e.g. you do not set name scope array first) return false
	 */
	public boolean setValue(int index, double value) {
		if (scopeArray == null) return false;
		if (scopeArray.length <= 0) return false;

		if (valueArray == null) {
			valueArray = new double[scopeArray.length];
		}
		if (valueUsableArray == null) {
			valueUsableArray = new boolean[scopeArray.length];
		}

		if (index >= 0 && index < valueArray.length) {
			valueArray[index] = value;
			valueUsableArray[index] = true;
			return true;
		} else return false;
	}

	public NameScope[] getNameScopeArray() {
		return scopeArray;
	}
	
	public NameScope getNameScope(int index) {
		if (scopeArray == null) return null;
		if (index >= 0 && index < scopeArray.length) return scopeArray[index];
		return null;
	}
	
	/**
	 * Get the index of the give name scope. If we can not find the scope, return -1.
	 */
	public int getNameScopeIndex(NameScope scope) {
		if (scopeArray == null) return -1;
		
		for (int index = 0; index < scopeArray.length; index++) {
			// We can use directly == to compare two NameScope objects or two NameDefinition objects, since 
			// a NameScope object or a NameDefinition object do not change at once it is created. 
			if (scopeArray[index] == scope) return index;
		}
		return -1;
	}
	
	/**
	 * Set a detailed type definition array as the name scope array
	 */
	public void setNameScopeArray(DetailedTypeDefinition[] dtArray) {
		scopeArray = dtArray;
		scopeKind = NameScopeKind.NSK_DETAILED_TYPE;
	}

	/**
	 * Set a method definition array as the name scope array
	 */
	public void setNameScopeArray(MethodDefinition[] mArray) {
		scopeArray = mArray;
		scopeKind = NameScopeKind.NSK_METHOD;
	}
	
	/**
	 * Set a compilation unit scope array as the name scope array
	 */
	public void setNameScopeArray(CompilationUnitScope[] cArray) {
		scopeArray = cArray;
		scopeKind = NameScopeKind.NSK_COMPILATION_UNIT;
	}
	
	/**
	 * Set a detailed type definition list as the name scope array
	 */
	public void setDetailedTypeDefinitionList(List<DetailedTypeDefinition> types) {
		scopeArray = new NameScope[types.size()];
		for (int index = 0; index < types.size(); index++) {
			scopeArray[index] = types.get(index);
		}
		scopeKind = NameScopeKind.NSK_DETAILED_TYPE;
	}

	/**
	 * Set a method definition list as the name scope array
	 */
	public void setMethodDefinitionList(List<MethodDefinition> methods) {
		scopeArray = new NameScope[methods.size()];
		for (int index = 0; index < methods.size(); index++) {
			scopeArray[index] = methods.get(index);
		}
		scopeKind = NameScopeKind.NSK_METHOD;
	}
	
	/**
	 * Set a compilation unit scope list as the name scope array
	 */
	public void setCompilationUnitScopList(List<CompilationUnitScope> units) {
		scopeArray = new NameScope[units.size()];
		for (int index = 0; index < units.size(); index++) {
			scopeArray[index] = units.get(index);
		}
		scopeKind = NameScopeKind.NSK_COMPILATION_UNIT;
	}
	
	/**
	 * Get the kind of name scopes in the name scope array
	 */
	public NameScopeKind getNameScopeKind() {
		return scopeKind;
	}
	
	/**
	 * Set the value for the given name scope (i.e. the parameter scope), and set its value to be usable
	 */
	public boolean setValue(NameScope scope, double value) {
		int index = getNameScopeIndex(scope);
		if (index < 0) return false;
		return setValue(index, value);
	}
	
	/**
	 * Print the distribution to one or two rows of table. If the boolean parameter printScope is false, only print the value,
	 * otherwise, print the name scope in a row, and the value in another row. Anyway, the measure identifier will be
	 * in the first column of the value row. The table symbol is used to as the splitter of the columns. 
	 */
	public void printToRow(PrintWriter writer, boolean printScope) {
		if (printScope == true) {
			writer.print("Name Scope\t");
			for (int index = 0; index < scopeArray.length; index++) {
				writer.print(scopeArray[index].getScopeName() + "\t");
			}
			writer.println();
		}
		writer.print(identifier + "\t");
		for (int index = 0; index < scopeArray.length; index++) {
			if (valueUsableArray[index] == true) writer.print(valueArray[index] + "\t");
			else writer.print("N.A.\t");
		}
		writer.println();
	}

	/**
	 * Print the distribution to one or two columns of table. If the boolean parameter printScope is false, only print the value,
	 * otherwise, print the name scope in a column, and the value in another column. Anyway, the measure identifier will be
	 * in the first row of the value column. The table symbol is used to as the splitter of the columns. 
	 */
	public void printToColumn(PrintWriter writer, boolean printScope) {
		if (printScope == true) {
			writer.println("Name Scope\t" + identifier);
		} else writer.println(identifier);
		for (int index = 0; index < scopeArray.length; index++) {
			if (printScope == true) writer.print(scopeArray[index].getScopeName() + "\t");
			if (valueUsableArray[index] == true) writer.println(valueArray[index]);
			else writer.println("N.A.");
		}
	}
}
