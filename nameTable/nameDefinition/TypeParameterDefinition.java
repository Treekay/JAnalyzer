package nameTable.nameDefinition;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A class represents type parameters for a generic class or a generic methods. However, so far, we do not match type parameters
 * when we resolve name references. 
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ5ÈÕ
 * @version 1.0
 *
 */
public class TypeParameterDefinition extends TypeDefinition {
	private List<TypeReference> boundList = null;
	// 2017/08/17 The value of the current type parameter, which is a temporary value for resolving
	// a reference
	private TypeReference value = null;
	
	public TypeParameterDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		super(simpleName, fullQualifiedName, location, scope);
	}

	/* (non-Javadoc)
	 * @see nameTable.NameDefinition#getNameDefinitionKind()
	 */
	@Override
	public NameDefinitionKind getDefinitionKind() {
		return NameDefinitionKind.NDK_TYPE_PARAMETER;
	}
	
	/**
	 * Test if the type is an interface. 
	 */
	@Override
	public boolean isInterface() {
		return false;
	}
	
	/**
	 * Test if the type is defined in package, i.e. test if the type is not a member type
	 */
	@Override
	public boolean isPackageMember() {
		return false;
	}
	
	/**
	 * Get current value store in this type parameter definition
	 */
	public TypeReference getCurrentValue() {
		return value;
	}

	/**
	 * Set current value, which is a type reference for instantiate the parameter for the current resolving reference
	 */
	public void setCurrentValue(TypeReference type) {
		this.value = type;
	}

	public List<TypeReference> getBoundList() {
		return boundList;
	}
	
	public boolean addBoundType(TypeReference boundType) {
		if (boundList == null) boundList = new ArrayList<TypeReference>();
		return boundList.add(boundType);
	}
	
	@Override
	public List<TypeReference> getSuperList() {
		return boundList;
	}
	
	/**
	 * Match a type reference with this parameter. If matched, bind this type reference to 
	 * the value of this type parameter.
	 * <p>Note: the value of this type parameter should have been resolved at least once!
	 */
	public boolean matchTypeReference(TypeReference type) {
		if (!match(type)) return false;
		
		// Note that if matched, then the type reference is bind to this parameter definition yet
		// in the method NameDefinition.match()
		if (value != null) {
			if (value.isResolved()) type.bindTo(value.getDefinition());
		}
		return true;
	}

}
