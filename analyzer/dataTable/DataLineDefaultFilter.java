package analyzer.dataTable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ24ÈÕ
 * @version 1.0
 */
public class DataLineDefaultFilter implements DataLineFilter {

	List<DataLineFilterCondition> conditionList = null;
	StringBuffer reasonBuffer = null;
	boolean needReason = false;
	
	public boolean addCondition(DataLineFilterCondition condition) {
		if (conditionList == null) conditionList = new ArrayList<DataLineFilterCondition>();
		return conditionList.add(condition);
	}
	
	public String getReason() {
		if (needReason && reasonBuffer != null) return reasonBuffer.toString();
		else return "";
	}
	
	public void setNeedReasonOn() {
		needReason = true;
	}

	public void setNeedReasonOff() {
		needReason = false;
	}

	@Override
	public boolean accept(DataTableManager manager, int lineIndex) {
		String[] lineStringArray = manager.getLineAsStringArray(lineIndex);
		if (lineStringArray == null) return false;
		return accept(manager, lineStringArray);
	}

	@Override
	public boolean accept(DataTableManager manager, String lineKey) {
		String[] lineStringArray = manager.getLineAsStringArray(lineKey);
		if (lineStringArray == null) return false;
		return accept(manager, lineStringArray);
	}
	
	protected boolean accept(DataTableManager manager, String[] lineStringArray) {
		if (conditionList == null) return true;
		if (conditionList.size() <= 0) return true;

		DataLineFilterCondition condition = conditionList.get(0);
		int columnIndex = manager.getColumnIndex(condition.getColumnName());
		
		if (needReason == true) reasonBuffer = new StringBuffer();
		
		boolean result = true;
		if (columnIndex >= 0) {
			String columnContent = lineStringArray[columnIndex];
			result = condition.acceptColumnValue(columnContent);
			if (needReason == true && result == true) {
				reasonBuffer.append(condition.getColumnName() + "(" + columnContent + ");");
			}
		}
		for (int conditionIndex = 1; conditionIndex < conditionList.size(); conditionIndex++) {
			condition = conditionList.get(conditionIndex);
			columnIndex = manager.getColumnIndex(condition.getColumnName());
			String columnContent = lineStringArray[columnIndex];
			if (needReason == true) {
				boolean currentResult = condition.acceptColumnValue(columnContent);
				if (condition.getLogic() == ConditionLogicKind.CLK_AND) result = result && currentResult;
				else result = result || currentResult;
				if (result == true && currentResult == true) {
					reasonBuffer.append(condition.getColumnName() + "(" + columnContent + ");");
				}
			} else {
				result = condition.acceptColumnValue(result, columnContent);
			}
		}
		return result;
	}

}
