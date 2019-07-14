package nameTable.nameDefinition;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.NameReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A class represents an enumeration constant definition
 * @author Zhou Xiaocong
 * @since 2013-2-27
 * @version 1.0
 * 
 * @update 2015/11/5
 * 		Refactor the class according to the design document
 */
public class EnumConstantDefinition extends NameDefinition {
	private List<NameReference> argumentList = null;
	
	public EnumConstantDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		super(simpleName, fullQualifiedName, location, scope);
	}

	/* (non-Javadoc)
	 * @see nameTable.NameDefinition#getDefinitionKind()
	 */
	@Override
	public NameDefinitionKind getDefinitionKind() {
		return NameDefinitionKind.NDK_ENUM_CONSTANT;
	}

	/**
	 * Return the argument list defined in the enumeration constant
	 */
	public List<NameReference> getArgumentList() {
		return argumentList;
	}
	
	/**
	 * Add an argument for the enumeration constant
	 */
	public void addArgument(NameReference argument) {
		if (argumentList == null) argumentList = new ArrayList<NameReference>();
		argumentList.add(argument);
	}
}
