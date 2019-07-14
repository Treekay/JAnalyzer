package nameTable.nameDefinition;

import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A class represents an anonymous class definition
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ13ÈÕ
 * @version 1.0
 *
 */
public class AnonymousClassDefinition extends DetailedTypeDefinition {

	public AnonymousClassDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location,
			NameScope scope, SourceCodeLocation endLocation) {
		super(simpleName, fullQualifiedName, location, scope, endLocation);
	}

	@Override
	public boolean isAnonymous() {
		return true;
	}
	
	public static String getAnonymousClassSimpleName(SourceCodeLocation location) {
		return "AnonyClass@" + location.getLineNumber();
	}
}
