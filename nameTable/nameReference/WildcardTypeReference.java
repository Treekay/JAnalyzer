package nameTable.nameReference;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A class represents a wildcard type reference.
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ7ÈÕ
 * @version 1.0
 *
 */
public class WildcardTypeReference extends TypeReference {
	protected TypeReference bound = null;
	protected boolean isUpperBound = true;

	public WildcardTypeReference(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
		typeKind = TypeReferenceKind.TRK_WILDCARD;
	}

	public WildcardTypeReference(WildcardTypeReference other) {
		super(other);
		bound = other.bound;
		isUpperBound = other.isUpperBound;
		typeKind = other.typeKind;
	}

	public void setBound(TypeReference bound) {
		this.bound = bound;
	}
	
	public TypeReference getBound() {
		return bound;
	}
	
	public void setUpperBound(boolean isUpperBound) {
		this.isUpperBound = isUpperBound;
	}
	
	public void setUpperBound() {
		this.isUpperBound = true;
	}
	
	public boolean isUpperBound() {
		return isUpperBound;
	}

	/**
	 * Return sub-reference in a name reference
	 */
	@Override
	public List<NameReference> getSubReferenceList() {
		List<NameReference> result = new ArrayList<NameReference>();
		if (bound != null) result.add(bound);
		return result;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		if (bound != null) {
			if (bound.resolveBinding()) {
				bindTo(bound.definition);
				return true;
			} 
		}
		return false;
	}
	
	/**
	 * The core reference of a wildcard type reference is the core reference of its bound
	 */
	public NameReference getCoreReference() {
		if (bound == null) return this;
		else return bound.getCoreReference();
	}
	
}
