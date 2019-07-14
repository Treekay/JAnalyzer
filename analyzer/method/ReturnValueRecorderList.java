package analyzer.method;

import java.util.ArrayList;
import java.util.List;

import analyzer.logic.MLogicValue;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ25ÈÕ
 * @version 1.0
 *
 */
public class ReturnValueRecorderList {
	List<ReturnValueRecorder> returnValueList = new ArrayList<ReturnValueRecorder>();
	
	boolean hasNullValue() {
		for (ReturnValueRecorder returnValue : returnValueList) {
			if (returnValue.hasNullValue()) return true;
		}
		return false;
	}
	
	/**
	 * If there is at least a MLogicValue.TRUE value of getNullability() in returnValueList, then return MLogicValue.TRUE, 
	 * otherwise, there is at least a MLogicValue.UNKNOW value of getNullability() in returnValueList, then return MLogicValue.UNKNOW,
	 * otherwise, i.e. there all MLogicValue.FALSE, return MLogicValue.FALSE
	 * 
	 */
	MLogicValue getNullability() {
		MLogicValue result = MLogicValue.FALSE;
		for (ReturnValueRecorder returnValue : returnValueList) {
			MLogicValue nullable = returnValue.getNullability();
			if (nullable == MLogicValue.TRUE) return MLogicValue.TRUE;
			else if (nullable == MLogicValue.UNKNOWN) result = MLogicValue.UNKNOWN;
		}
		return result;
	}
}
