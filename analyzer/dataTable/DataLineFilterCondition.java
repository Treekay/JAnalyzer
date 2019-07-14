package analyzer.dataTable;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ31ÈÕ
 * @version 1.0
 */
public interface DataLineFilterCondition {
	/**
	 * Test if the column value should be accepted or not
	 * @param currentValue : the value of the other condition branches which have been computed in the whole condition. 
	 * 		If currentValue == true, but the logic in the current condition is OR, then we can return true directly. Similarly, 
	 * 	    If currentValue == false, but the logic in the current condition is AND, then we can return false directly.  
	 * @param columnValue : the value in the column of the data table
	 */
	public boolean acceptColumnValue(boolean currentValue, String columnValueString);
	
	/**
	 * Test if the column value should be accepted or not. This method will ignore the logic in the condition. Generally, this method
	 * should be called by the first condition.
	 * @param columnValue : the value in the column of the data table
	 */
	public boolean acceptColumnValue(String columnValueString);
	
	public ConditionLogicKind getLogic();
	
	public String getColumnName();
}
