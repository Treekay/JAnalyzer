package analyzer.dataTable;

/**
 * A class represents the conditions for filtering the line in a data table
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ24ÈÕ
 * @version 1.0
 */
public class DataLineFilterValueCondition implements DataLineFilterCondition {
	// The current condition is as a disjunction (LOGIC_OR) or a conjunction (LOGIC_AND) branch of the whole condition
	protected ConditionLogicKind logic = ConditionLogicKind.CLK_OR;
	// The type of the column in the current conditions is String (TYPE_STRING) or Numerical (i.e. int or double, TYPE_NUMERICAL)
	protected ConditionDataTypeKind type = ConditionDataTypeKind.CDTK_NUMERIC;
	protected String columnName = null;
	// How to compare the value of the column in the data table with the value in the current condition
	protected ConditionRelationKind relation = ConditionRelationKind.CRK_EQUAL;
	// The value in the current condition
	protected String value = null;

	public DataLineFilterValueCondition(String columnName, String value) {
		this.columnName = columnName;
		this.value = value;
	}

	public DataLineFilterValueCondition(String columnName, String value, ConditionLogicKind logic, ConditionDataTypeKind type, ConditionRelationKind relation) {
		this.logic = logic;
		this.type = type;
		this.columnName = columnName;
		this.relation = relation;
		this.value = value;
	}
	
	public void setLogic(ConditionLogicKind logic) {
		this.logic = logic;
	}
	
	public ConditionLogicKind getLogic() {
		return logic;
	}
	
	public void setType(ConditionDataTypeKind type) {
		this.type = type;
	}
	
	public ConditionDataTypeKind getType() {
		return type;
	}

	public void setRelation(ConditionRelationKind relation) {
		this.relation = relation;
	}
	
	public ConditionRelationKind getRealtion() {
		return relation;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	/**
	 * Test if the column value should be accepted or not
	 * @param currentValue : the value of the other condition branches which have been computed in the whole condition. 
	 * 		If currentValue == true, but the logic in the current condition is OR, then we can return true directly. Similarly, 
	 * 	    If currentValue == false, but the logic in the current condition is AND, then we can return false directly.  
	 * @param columnValue : the value in the column of the data table
	 */
	@Override
	public boolean acceptColumnValue(boolean currentValue, String columnValueString) {
		if (currentValue == true && logic == ConditionLogicKind.CLK_OR) return true;
		if (currentValue == false && logic == ConditionLogicKind.CLK_AND) return false;

		return acceptColumnValue(columnValueString);
	}

	/**
	 * Test if the column value should be accepted or not. This method will ignore the logic in the condition. Generally, this method
	 * should be called by the first condition.
	 * @param columnValue : the value in the column of the data table
	 */
	@Override
	public boolean acceptColumnValue(String columnValueString) {
		boolean result = true;
		
		int compareResult = 0;
		if (type == ConditionDataTypeKind.CDTK_STRING) {
			compareResult = columnValueString.compareTo(value);
		} else if (type == ConditionDataTypeKind.CDTK_NUMERIC) {
			double conditionValue = Double.parseDouble(value);
			double columnValue = Double.parseDouble(columnValueString);
			if (columnValue < conditionValue) compareResult = -1;
			else if (conditionValue == columnValue) compareResult = 0;
			else compareResult = 1;
		} else if (type == ConditionDataTypeKind.CDTK_BOOLEAN) {
			boolean conditionValue = Boolean.parseBoolean(value);
			boolean columnValue = Boolean.parseBoolean(columnValueString);
			if (conditionValue == columnValue) compareResult = 0;
			else if (columnValue == false) compareResult = -1;
			else compareResult = 1;
		}
		if (compareResult == 0) {
			if (relation == ConditionRelationKind.CRK_GREATER || relation == ConditionRelationKind.CRK_LESS || 
					relation == ConditionRelationKind.CRK_NOT_EQUAL) result = false;
			else result = true;
		} else if (compareResult < 0) {
			if (relation == ConditionRelationKind.CRK_GREATER || relation == ConditionRelationKind.CRK_EQUAL || 
					relation == ConditionRelationKind.CRK_GREATER_EQUAL) result = false;
			else result = true;
		} else {
			if (relation == ConditionRelationKind.CRK_LESS || relation == ConditionRelationKind.CRK_EQUAL || 
					relation == ConditionRelationKind.CRK_LESS_EQUAL) result = false;
			else result = true;
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		String logicString = " .??. ";
		if (logic == ConditionLogicKind.CLK_OR) logicString = " .OR. ";
		else if (logic == ConditionLogicKind.CLK_AND) logicString = " .AND. ";
		if (relation == ConditionRelationKind.CRK_GREATER) return logicString + "[" + columnName + "] > " + value;
		else if (relation == ConditionRelationKind.CRK_LESS) return logicString + "[" + columnName + "] < " + value;
		else if (relation == ConditionRelationKind.CRK_EQUAL) return logicString + "[" + columnName + "] == " + value;
		else if (relation == ConditionRelationKind.CRK_NOT_EQUAL) return logicString + "[" + columnName + "] != " + value;
		else if (relation == ConditionRelationKind.CRK_GREATER_EQUAL) return logicString + "[" + columnName + "] >= " + value;
		else if (relation == ConditionRelationKind.CRK_LESS_EQUAL) return logicString + "[" + columnName + "] <= " + value;
		else return logicString + "[" + columnName + "] ?? " + value;
	}
}
