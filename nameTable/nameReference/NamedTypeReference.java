package nameTable.nameReference;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A class represents a named type reference
 * 
 * @author Zhou Xiaocong
 * @since 2016年11月6日
 * @version 1.0
 *
 */
public class NamedTypeReference extends TypeReference {
	private String fullQualifiedName = null;
	private NameReference qualifier = null;

	public NamedTypeReference(String name, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
		this.fullQualifiedName = fullQualifiedName;
		typeKind = TypeReferenceKind.TRK_NAMED;
	}

	public NamedTypeReference(NamedTypeReference other) {
		super(other);
		fullQualifiedName = other.fullQualifiedName;
		typeKind = other.typeKind;
	}


	/**
	 * @return the qualifier
	 */
	public NameReference getQualifier() {
		return qualifier;
	}

	/**
	 * @param qualifier the qualifier to set
	 */
	public void setQualifier(NameReference qualifier) {
		this.qualifier = qualifier;
	}

	/**
	 * The core reference of method reference is itself
	 */
	public NameReference getCoreReference() {
		return this;
	}
	
	/**
	 * Return sub-reference in a name reference
	 */
	@Override
	public List<NameReference> getSubReferenceList() {
		List<NameReference> result = new ArrayList<NameReference>();
		result.add(qualifier);
		return result;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		// At first resolve the reference as an entire type name reference in the current scope
		TypeReference tempReference = new TypeReference(fullQualifiedName, location, scope);
		if (scope.resolve(tempReference)) {
			bindTo(tempReference.definition);
			return true;
		}
		
		// If we can not resolve the entire qualified type name, then we resolve the qualifier
		if (qualifier.resolveBinding()) {
			NameDefinition nameDef = qualifier.getDefinition();
			NameDefinitionKind nameDefKind = nameDef.getDefinitionKind();
			if (nameDefKind == NameDefinitionKind.NDK_PACKAGE) {
				// Resolve the simple type reference in the package, and bind the entire reference to
				// the definition object binded to the simple type reference
				PackageDefinition packageDef = (PackageDefinition)nameDef;
				packageDef.resolve(this);
			} else if (nameDefKind == NameDefinitionKind.NDK_TYPE) {
				// Resolve the simple type reference in the type, and bind the entire reference to
				// the definition object binded to the simple type reference
				TypeDefinition typeDef = (TypeDefinition)nameDef;
				typeDef.resolve(this);
			}
		} 
		return isResolved();
	}
}
