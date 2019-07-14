package nameTable.nameReference;

/**
 * Define some constant strings to mark the name reference
 * @author Zhou Xiaocong
 * @since 2013-2-23
 * @version 1.0

 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public final class NameReferenceLabel {

	public static final String TYPE_BYTE = "byte";
	public static final String TYPE_CHAR = "char";
	public static final String TYPE_SHORT = "short";
	public static final String TYPE_INT = "int";
	public static final String TYPE_LONG = "long";
	public static final String TYPE_FLOAT = "float";
	public static final String TYPE_DOUBLE = "double";
	public static final String TYPE_BOOLEAN = "boolean";
	public static final String TYPE_VOID = "void";

	public static final String TYPE_NUMBER = "number";
	public static final String TYPE_STRING = "String";
	public static final String TYPE_CLASS = "Class";
	public static final String TYPE_OBJECT = "Class";
	public static final String TYPE_SYSTEM = "Class";
	
	public static final String KEYWORD_NULL = "null";
	public static final String KEYWORD_NEW = "new";
	public static final String KEYWORD_THIS = "this";
	public static final String KEYWORD_SUPER = "super";
	public static final String KEYWORD_TRUE = "true";
	public static final String KEYWORD_FALSE = "false";
	
	public static final String NAME_QUALIFIER = ".";
	
	
	public static boolean isPrimitiveTypeName(String name) {
		if (name.equals(TYPE_BOOLEAN) || name.equals(TYPE_CHAR) || 
				name.equals(TYPE_BYTE) || name.equals(TYPE_DOUBLE) || 
				name.equals(TYPE_INT) || name.equals(TYPE_LONG) || 
				name.equals(TYPE_FLOAT) || name.equals(TYPE_SHORT) ||
				name.equals(TYPE_VOID)) return true;
		else return false;
		
	}

	public static boolean isAutoImportedTypeName(String name) {
		if (name.equals(TYPE_STRING) || name.equals(TYPE_CLASS) ||
				name.equals(TYPE_OBJECT) || name.equals(TYPE_SYSTEM)) return true;
		else return false;
		
	}
}
