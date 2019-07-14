package nameTable.nameReference.referenceGroup;

/**
 * The enumeration constants represent the kind of the group. Each constant correspond to a kind 
 * of expressions in Java language, or exactly, a kind of expression AST node in JDT AST node.  
 * @author Zhou Xiaocong
 * @since 2013-2-23
 * @version 1.0
 */
public enum NameReferenceGroupKind {
	NRGK_ARRAY_ACCESS,
	NRGK_ARRAY_CREATION,
	NRGK_ARRAY_INITIALIZER,
	NRGK_ASSIGNMENT,
	NRGK_CAST,
	NRGK_CLASS_INSTANCE_CREATION,
	NRGK_CONDITIONAL,
	NRGK_FIELD_ACCESS,
	NRGK_INFIX_EXPRESSION,
	NRGK_INSTANCEOF,
	NRGK_METHOD_INVOCATION,
	NRGK_POSTFIX_EXPRESSION,
	NRGK_PREFIX_EXPRESSION,
	NRGK_SUPER_FIELD_ACCESS,
	NRGK_SUPER_METHOD_INVOCATION,
	NRGK_VARIALBE_DECLARATION,
	NRGK_THIS_EXPRESSION,
	NRGK_TYPE_LITERAL,
	NRGK_QUALIFIED_NAME,

}
