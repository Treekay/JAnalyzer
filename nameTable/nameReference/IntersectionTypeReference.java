package nameTable.nameReference;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A class represents a reference referred to intersection types.
 *  
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ7ÈÕ
 * @version 1.0
 *
 */
public class IntersectionTypeReference extends TypeReference {
	private List<TypeReference> typeList = null;
	
	public IntersectionTypeReference(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
		typeKind = TypeReferenceKind.TRK_INTERSECTION;
	}

	public IntersectionTypeReference(IntersectionTypeReference other) {
		super(other);
		typeList = new ArrayList<TypeReference>();
		typeList.addAll(other.typeList);
		typeKind = other.typeKind;
	}
	
	public void addType(TypeReference type) {
		if (typeList == null) typeList = new ArrayList<TypeReference>();
		typeList.add(type);
	}
	
	public List<TypeReference> getTypeList() {
		return typeList;
	}
	
	/**
	 * Return sub-reference in a name reference
	 */
	@Override
	public List<NameReference> getSubReferenceList() {
		List<NameReference> result = new ArrayList<NameReference>();
		if (typeList != null) {
			for (TypeReference type : typeList) result.add(type);
		}
		return result;
	}
	
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		// Resolve all types in typeList
		if (typeList != null && typeList.size() > 0) {
			TypeDefinition firstResolvedType = null;
			for (TypeReference type : typeList) {
				type.resolveBinding();
				if (type.isResolved() && firstResolvedType == null) {
					firstResolvedType = (TypeDefinition)type.getDefinition();
				}
			}
			bindTo(firstResolvedType);
		}
		return isResolved();
	}
	
	/**
	 * The core reference of an intersection type reference is the core reference of its first type reference
	 * in the list of type references
	 */
	public NameReference getCoreReference() {
		if (typeList == null) return this;
		else if (typeList.size() <= 0) return this;
		else {
			TypeReference firstType = typeList.get(0);
			return firstType.getCoreReference();
		}
	}
	
}
