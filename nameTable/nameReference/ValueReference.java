package nameTable.nameReference;

import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A reference to a value, that is, it is a field or a variable. For a value reference, we can test if it is 
 * a left value reference or a right value reference. 
 * 
 * @Important_Note(Zhou Xiaocong 2014-1-3): However, when we can not resolve a value reference as field or a variable, we may try to resolve it 
 * 		as a type reference, since in a qualified name such as System.out, the qualified may be a class name (i.e. a type name). And then, its kind
 * 		set to be NRK_TYPE, but it can not be cast to a TypeReference object!
 * @author Zhou Xiaocong
 * @since 2013-3-28
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class ValueReference extends NameReference {
	private boolean isLeftValue = false;

	public ValueReference(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
		kind = NameReferenceKind.NRK_VARIABLE;
	}

	public ValueReference(String name, SourceCodeLocation location, NameScope scope, NameReferenceKind kind) {
		super(name, location, scope, kind);
	}

	/**
	 * Test whether the reference is represent a left value reference
	 */
	public boolean isLeftValue() {
		return isLeftValue;
	}
	
	/**
	 * Test whether the reference is represent a value reference
	 */
	public boolean isValueReference() {
		return true;
	}

	/**
	 * Set the value reference is a left value reference
	 */
	@Override
	public void setLeftValueReference() {
		isLeftValue = true;
	}
	
	/**
	 * Return a better string of the reference for debugging
	 */
	public String toMultilineString(int indent, boolean includeLiteral) {
	
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		StringBuffer buffer = new StringBuffer(indentString);
		if (isLeftValue) buffer.append(kind.id + " Reference [Left Value Name = " + name);
		else buffer.append(kind.id + " Reference [Right Value Name = " + name);
		if (indent > 0) buffer.append(" @" + location.toString() + "]\n");
		else buffer.append(" @" + location.getUniqueId() + "]\n");
			
		return buffer.toString();
	}
	
	/**
	 * The core reference of a value reference is itself
	 */
	public NameReference getCoreReference() {
		return this;
	}
	
}
