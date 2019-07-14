package analyzer.logic;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê12ÔÂ16ÈÕ
 * @version 1.0
 *
 */
public enum MLogicValue {
	TRUE,
	FALSE,
	UNKNOWN;

	public static MLogicValue getMLogicValue(boolean value) {
		if (value == true) return TRUE;
		else return FALSE;
	}
	
	public MLogicValue conjunctionWith(MLogicValue other) {
		if (this == UNKNOWN || other == UNKNOWN) return UNKNOWN;
		else if (this == FALSE || other == FALSE) return FALSE;
		else return TRUE;
	}
	
	public MLogicValue disjunctionWith(MLogicValue other) {
		if (this == UNKNOWN || other == UNKNOWN) return UNKNOWN;
		else if (this == TRUE || other == TRUE) return TRUE;
		else return FALSE;
	}
}
