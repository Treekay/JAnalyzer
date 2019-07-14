package analyzer.dataTable;

public class DataLineFilterIntervalCondition implements DataLineFilterCondition {
	// The current condition is as a disjunction (LOGIC_OR) or a conjunction (LOGIC_AND) branch of the whole condition
	protected ConditionLogicKind logic = ConditionLogicKind.CLK_OR;
	// The type of the column in the current conditions is String (TYPE_STRING) or Numerical (i.e. int or double, TYPE_NUMERICAL)
	protected ConditionDataTypeKind type = ConditionDataTypeKind.CDTK_NUMERIC;
	protected String columnName = null;
	// How to compare the value of the column in the data table with the value in the current condition
	protected ConditionRelationKind relation = ConditionRelationKind.CRK_IN_INTERVAL;
	// The lowerValue and upValue in the current condition, the interval will be (lowValue, upValue) etc.
	protected String lowValue = null;
	protected String upValue = null;
	// The interval type of the current condition
	protected ConditionIntervalKind interval = ConditionIntervalKind.CIK_CLOSE_CLOSE;

	public DataLineFilterIntervalCondition(String columnName, String lowValue, String upValue) {
		this.columnName = columnName;
		this.lowValue = lowValue;
		this.upValue = upValue;
	}

	public DataLineFilterIntervalCondition(String columnName, String lowValue, String upValue, ConditionLogicKind logic, 
			ConditionDataTypeKind type, ConditionRelationKind relation, ConditionIntervalKind interval) {
		this.logic = logic;
		this.type = type;
		this.columnName = columnName;
		this.relation = relation;
		this.interval = interval;
		this.lowValue = lowValue;
		this.upValue = upValue;
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

	public void setInterval(ConditionIntervalKind interval) {
		this.interval = interval;
	}
	
	public ConditionIntervalKind getInterval() {
		return interval;
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
		
		int lowCompareResult = 0;
		int upCompareResult = 0;
		if (type == ConditionDataTypeKind.CDTK_STRING) {
			lowCompareResult = columnValueString.compareTo(lowValue);
			upCompareResult = columnValueString.compareTo(upValue);
		} else if (type == ConditionDataTypeKind.CDTK_NUMERIC) {
			double conditionLowValue = Double.parseDouble(lowValue);
			double conditionUpValue = Double.parseDouble(upValue);
			double columnValue = Double.parseDouble(columnValueString);
			if (columnValue < conditionLowValue) lowCompareResult = -1;
			else if (conditionLowValue == columnValue) lowCompareResult = 0;
			else lowCompareResult = 1;
			if (columnValue < conditionUpValue) upCompareResult = -1;
			else if (conditionUpValue == columnValue) upCompareResult = 0;
			else upCompareResult = 1;
		}
		if (interval == ConditionIntervalKind.CIK_CLOSE_CLOSE) {
			if (lowCompareResult >= 0 && upCompareResult <= 0) result = true;
			else result = false;
		} else if (interval == ConditionIntervalKind.CIK_CLOSE_OPEN) {
			if (lowCompareResult >= 0 && upCompareResult < 0) result = true;
			else result = false;
		} else if (interval == ConditionIntervalKind.CIK_OPEN_CLOSE) {
			if (lowCompareResult > 0 && upCompareResult <= 0) result = true;
			else result = false;
		} else if (interval == ConditionIntervalKind.CIK_OPEN_OPEN) {
			if (lowCompareResult > 0 && upCompareResult < 0) result = true;
			else result = false;
		}
		if (relation == ConditionRelationKind.CRK_IN_INTERVAL) return result;
		else return !result;
	}
	
	@Override
	public String toString() {
		String logicString = " .??. ";
		if (logic == ConditionLogicKind.CLK_OR) logicString = " .OR. ";
		else if (logic == ConditionLogicKind.CLK_AND) logicString = " .AND. ";
		String intervalString = "";
		if (interval == ConditionIntervalKind.CIK_CLOSE_CLOSE) intervalString = "[" + lowValue + ", " + upValue + "]";
		else if (interval == ConditionIntervalKind.CIK_CLOSE_OPEN) intervalString = "[" + lowValue + ", " + upValue + ")";
		else if (interval == ConditionIntervalKind.CIK_OPEN_CLOSE) intervalString = "(" + lowValue + ", " + upValue + "]";
		else if (interval == ConditionIntervalKind.CIK_OPEN_OPEN) intervalString = "(" + lowValue + ", " + upValue + ")";
		if (relation == ConditionRelationKind.CRK_IN_INTERVAL) return logicString + "[" + columnName + "] in " + intervalString;
		else return logicString + "[" + columnName + "] not in " + intervalString;
	}
}
