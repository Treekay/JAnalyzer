package nameTable.nameReference;

/**
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public enum NameReferenceKind {
	NRK_PACKAGE("Package"),
	NRK_TYPE("Type"),
	NRK_METHOD("Method"),
	NRK_FIELD("Field"),
	NRK_VARIABLE("Variable"),
	NRK_LITERAL("Literal"),
	NRK_GROUP("Group"),
	NRK_UNKNOWN("Unknown");
	
	public final String id;
	
	private NameReferenceKind(String id) {
		this.id = id;
	}
}
