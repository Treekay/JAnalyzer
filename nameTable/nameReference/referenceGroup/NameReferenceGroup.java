package nameTable.nameReference.referenceGroup;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A name reference group is used to group all name references for an expression, in order to support to infer the 
 * static type of the expression
 * @author Zhou Xiaocong
 * @since 2013-2-23
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public abstract class NameReferenceGroup extends NameReference {
	public static final String OPERATOR_TIMES = "*";
	public static final String OPERATOR_DIVIDE = "/";
	public static final String OPERATOR_REMAINDER = "%";
	public static final String OPERATOR_PLUS = "+";
	public static final String OPERATOR_MINUS = "-";
	public static final String OPERATOR_LEFT_SHIFT = "<<";
	public static final String OPERATOR_RIGHT_SHIFT_SIGNED = ">>";
	public static final String OPERATOR_RIGHT_SHIFT_UNSIGNED = ">>>";
	public static final String OPERATOR_LESS = "<";
	public static final String OPERATOR_GREATER = ">";
	public static final String OPERATOR_LESS_EQUALS = "<=";
	public static final String OPERATOR_GREATER_EQUALS = ">=";
	public static final String OPERATOR_EQUALS = "==";
	public static final String OPERATOR_NOT_EQUALS = "!=";
	public static final String OPERATOR_XOR = "^";
	public static final String OPERATOR_AND = "&";
	public static final String OPERATOR_OR = "|";
	public static final String OPERATOR_CONDITIONAL_AND = "&&";
	public static final String OPERATOR_CONDITIONAL_OR = "||";
	public static final String OPERATOR_INCREMENT = "++";
	public static final String OPERATOR_DECREMENT = "--";
	public static final String OPERATOR_COMPLEMENT = "~";
	public static final String OPERATOR_NOT = "!";
	public static final String OPERATOR_ASSIGN = "=";
	public static final String OPERATOR_PLUS_ASSIGN = "+=";
	public static final String OPERATOR_MINUS_ASSIGN = "-=";
	public static final String OPERATOR_TIMES_ASSIGN = "*=";
	public static final String OPERATOR_DIVIDE_ASSIGN = "/=";
	public static final String OPERATOR_BIT_AND_ASSIGN = "&=";
	public static final String OPERATOR_BIT_OR_ASSIGN = "|=";
	public static final String OPERATOR_BIT_XOR_ASSIGN = "^=";
	public static final String OPERATOR_REMAINDER_ASSIGN = "%=";
	public static final String OPERATOR_LEFT_SHIFT_ASSIGN = "<<=";
	public static final String OPERATOR_RIGHT_SHIFT_SIGNED_ASSIGN = ">>=";
	public static final String OPERATOR_RIGHT_SHIFT_UNSIGNED_ASSIGN = ">>>=";

	protected String operator = null;			// The string of the operator, its value is included in the above final strings
	protected List<NameReference> subreferences = null;		// The references in the group. When all sub references are literal, it may be null.
	
	public NameReferenceGroup(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
		kind = NameReferenceKind.NRK_GROUP;
	}

	@Override
	public NameReferenceKind getReferenceKind() {
		return kind;
	}

	/**
	 * Get the list of all references in the group
	 */
	@Override
	public List<NameReference> getSubReferenceList() {
		return subreferences;
	}

	/**
	 * Add a reference to the group
	 */
	public void addSubReference(NameReference reference) {
		if (reference == null) return;
		if (this.subreferences == null) this.subreferences = new ArrayList<NameReference>();
		this.subreferences.add(reference);
	}

	/**
	 * Return the string representing the operator in the expression corresponding to the group
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * Set the string representing the operator in the expression corresponding to the group
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	/**
	 * Return the kind of the group. The sub-class itself determines its kind.
	 */
	public abstract NameReferenceGroupKind getGroupKind();
	
	/**
	 * Test whether the reference is a name group reference
	 */
	@Override
	public boolean isGroupReference() {
		return true;
	}
	
	/**
	 * Set all reference in the group to be left value reference. Actually, only those value references
	 * are set to be left value reference indeed
	 */
	@Override
	public void setLeftValueReference() {
		if (subreferences == null) return;
		for (NameReference reference : subreferences) reference.setLeftValueReference();
	}
	
	/**
	 * Resolve all references in the group. The sub-class determines how to resolve the references in
	 * the group according to the syntax of its corresponding expression.
	 */
	public abstract boolean resolveBinding();
	
	
	/**
	 * Return all reference at the leaf in the group, i.e. return all non-group-reference in 
	 * this reference group
	 */
	public List<NameReference> getReferencesAtLeaf() {
		List<NameReference> result = new ArrayList<NameReference>();
		if (subreferences == null) return result;
		
		for (NameReference reference : subreferences) {
			List<NameReference> referenceList = reference.getReferencesAtLeaf();
			result.addAll(referenceList);
		}
		return result;
	}
	
	@Override
	public String toFullString() {
		final int MAX_LENGTH = 20;
		String nameString = name;

		if (nameString.length() > MAX_LENGTH) nameString = nameString.substring(0, MAX_LENGTH) + "...";
		StringBuffer buffer = new StringBuffer(kind.id + " Reference [Name = " + nameString + ", location = " + 
								location.getUniqueId() + ", scope = " + scope.getScopeName() + "]");
		if (subreferences != null) {
			buffer.append("\n");
			for (NameReference reference : subreferences) {
				buffer.append("\t" + reference.toFullString() + "\n");
			}
		}
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int MAX_LENGTH = 20;
		String nameString = name;

		if (nameString.length() > MAX_LENGTH) nameString = nameString.substring(0, MAX_LENGTH) + "...";
		StringBuffer buffer = new StringBuffer(kind.id + " Reference [Name = " + nameString + ", location = " + 
				location.toString() + ", scope = " + scope.getScopeName() + "]");
		if (subreferences != null) {
			buffer.append("\n");
			for (NameReference reference : subreferences) {
				buffer.append("\t" + reference.toString() + "\n");
			}
		}
		return buffer.toString();
	}

	/**
	 * Return a better string of the reference for debugging
	 */
	public String toMultilineString(int indent, boolean includeLiteral) {
		final int MAX_LENGTH = 20;
		
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		StringBuffer buffer = new StringBuffer(indentString + kind.id + " Reference  ");
		String nameString = name;
		if (nameString.length() > MAX_LENGTH) nameString = nameString.substring(0, MAX_LENGTH) + "...";
		buffer.append("[Name = " + nameString);

		if (indent > 0) buffer.append(" @" + location.toString() + "]\n");
		else buffer.append(" @" + location.getUniqueId() + "]\n");

		if (subreferences != null) {
			for (NameReference reference : subreferences) {
				buffer.append(reference.toMultilineString(indent+1, includeLiteral));
			}
		}
		return buffer.toString();
	}

	/**
	 * Display the definition binded to the reference
	 */
	public String bindedDefinitionToString() {
		final int MAX_LENGTH = 20;
		StringBuffer buffer = new StringBuffer();
		String nameString = name;

		if (nameString.length() > MAX_LENGTH) nameString = nameString.substring(0, MAX_LENGTH) + "...";
		if (definition != null) buffer.append("The group [" + nameString + "] is binded to: " + definition.toFullString());
		else buffer.append("The group [" + nameString + "] has not been resolved!");
		if (subreferences != null) {
			buffer.append("\n");
			for (NameReference reference : subreferences) {
				buffer.append(reference.bindedDefinitionToString() + "\n");
			}
		}
		return buffer.toString();
	}
	
	public boolean isArithematicOperator() {
		if (operator == null) return false;
		if (operator.equals(OPERATOR_DECREMENT) || operator.equals(OPERATOR_INCREMENT) || operator.equals(OPERATOR_PLUS) || operator.equals(OPERATOR_MINUS) ||
				operator.equals(OPERATOR_DIVIDE) || operator.equals(OPERATOR_TIMES) || operator.equals(OPERATOR_REMAINDER) || operator.equals(OPERATOR_AND) ||
				operator.equals(OPERATOR_COMPLEMENT) || operator.equals(OPERATOR_OR) || operator.equals(OPERATOR_XOR)) 
			return true;
		else return false;
	}
	
	public boolean isShiftOperator() {
		if (operator == null) return false;
		if (operator.equals(OPERATOR_LEFT_SHIFT) || operator.equals(OPERATOR_RIGHT_SHIFT_SIGNED) || 
				operator.equals(OPERATOR_RIGHT_SHIFT_UNSIGNED)) return true;
		else return false;
	}
	
	public boolean isLogicOperator() {
		if (operator == null) return false;
		if (operator.equals(OPERATOR_NOT) || operator.equals(OPERATOR_CONDITIONAL_AND) || 
				operator.equals(OPERATOR_CONDITIONAL_OR)) return true;
		else return false;
	}

	public boolean isRelationalOperator() {
		if (operator == null) return false;
		if (operator.equals(OPERATOR_LESS) || operator.equals(OPERATOR_GREATER) || operator.equals(OPERATOR_LESS_EQUALS) ||
				operator.equals(OPERATOR_NOT_EQUALS) || operator.equals(OPERATOR_GREATER_EQUALS) || operator.equals(OPERATOR_EQUALS) ) return true;
		else return false;
	}
}
