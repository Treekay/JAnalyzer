package analyzer.dataTable;

/**
 * A enum type to represent the kind of interval type in DataLineFilterRangeCondition
 * 
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ31ÈÕ
 * @version 1.0
 */
public enum ConditionIntervalKind {
	CIK_OPEN_OPEN, // Open-open interval, i.e. (a, b)
	CIK_OPEN_CLOSE, // Open-close interval, i.e. (a, b]
	CIK_CLOSE_OPEN, // Close-open interval, i.e. [a, b)
	CIK_CLOSE_CLOSE, // Close-close interval, i.e. [a, b]
}
