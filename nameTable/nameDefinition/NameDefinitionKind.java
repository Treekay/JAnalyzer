package nameTable.nameDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public enum NameDefinitionKind {
	NDK_PACKAGE("Package"),
	NDK_TYPE("Type"),
	NDK_FIELD("Field"),
	NDK_METHOD("Method"),
	NDK_PARAMETER("Parameter"),
	NDK_VARIABLE("Variable"),
	NDK_TYPE_PARAMETER("Type Parameter"),
	NDK_ENUM_CONSTANT("Enum Constant"),
	NDK_STATIC_MEMBER("Static Member"),
	NDK_UNKNOWN("Unknown");

	public final String id;
	
	private NameDefinitionKind(String id) {
		this.id = id;
	}
}
