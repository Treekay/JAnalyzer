package nameTable.nameDefinition;

import nameTable.nameScope.NameScope;

/**
 * A class represents a imported static member
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ5ÈÕ
 * @version 1.0
 *
 */
public class ImportedStaticMemberDefinition extends NameDefinition {

	public ImportedStaticMemberDefinition(String simpleName, String fullQualifiedName, NameScope scope) {
		super(simpleName, fullQualifiedName, null, scope);
	}

	/* (non-Javadoc)
	 * @see nameTable.nameDefinition.NameDefinition#getDefinitionKind()
	 */
	@Override
	public NameDefinitionKind getDefinitionKind() {
		// TODO Auto-generated method stub
		return NameDefinitionKind.NDK_STATIC_MEMBER;
	}

	public boolean isImportedStaticMember() {
		return true;
	}
}
