package nameTable.nameReference;

/**
 * an enum type to distinguish different type references
 *  
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ6ÈÕ
 * @version 1.0
 *
 */
public enum TypeReferenceKind {
	TRK_SIMPLE,
	TRK_QUALIFIED,
	TRK_NAMED,
	TRK_PARAMETERIZED,
	TRK_WILDCARD,
	TRK_INTERSECTION,
	TRK_UNION,
}
