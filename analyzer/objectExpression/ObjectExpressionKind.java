package analyzer.objectExpression;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê6ÔÂ15ÈÕ
 * @version 1.0
 *
 */
public enum ObjectExpressionKind {
	OEK_SIMPLE_NAME,
	OEK_FIELD_ACCESS,
	OEK_METHOD_INVOCATION,
	OEK_ARRAY_ACCESS,
	OEK_INSTANCE_CREATION,
	OEK_ARRAY_CREATION,
	OEK_TYPE_CAST,
	OEK_CONDITIONAL,

	OEK_VARIABLE_DECLARATION,			// Declaration for a variable whose type is non-primitive
	OEK_PRIMITIVE_ARRAY_DECLARATION,	// Declaration for an array whose base type is primitive
	OEK_OBJECTIVE_ARRAY_DECLARATION, 	// Declaration for an array whose base type is non-primitive
	
	OEK_UNKNOWN,						// Has not determine its kind yet, but it is an object expression 
	OEK_NOT_OBJECT_EXPRESSION, 			// Actually it is not an object expression. 
}
