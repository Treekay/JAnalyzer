package analyzer.dataTable;

/**
 * A enum type to represent the kind of relation between the column value and the value(s) in a DataLineFilterCondition

 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ31ÈÕ
 * @version 1.0
 */
public enum ConditionRelationKind {
	// The following six constants can be used for Numeric, Boolean, and String data type. 
	// Not that false < true for Boolean data type
	CRK_EQUAL,				// i.e. == 
	CRK_NOT_EQUAL, 			// i.e. != 
	CRK_GREATER,			// i.e. >
	CRK_GREATER_EQUAL, 		// i.e. >=
	CRK_LESS, 				// i.e. <
	CRK_LESS_EQUAL,			// i.e. <=

	// The following two constants can be used for interval condition 
	CRK_IN_INTERVAL,
	CRK_OUT_INTERVAL,
}
