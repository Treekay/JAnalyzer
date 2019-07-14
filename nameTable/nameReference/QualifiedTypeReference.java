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
 * A qualified type reference is a type reference with a qualifier, which can also be a qualified type reference,
 * and then it can be used to represent a type expression. A qualified type consists two parts: a qualifier and a
 * simple type (reference).
 * 
 * @author Zhou Xiaocong
 * @since 2013-3-27
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 *      Important notes: The last type reference is a simple name, and then its name is stored in the inherited 
 *      	attribute name. 
 */
public class QualifiedTypeReference extends TypeReference {
	private String fullQualifiedName = null;
	private TypeReference qualifier = null;

	public QualifiedTypeReference(String name, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
		this.fullQualifiedName = fullQualifiedName;
		typeKind = TypeReferenceKind.TRK_QUALIFIED;
	}

	public QualifiedTypeReference(QualifiedTypeReference other) {
		super(other);
		fullQualifiedName = other.fullQualifiedName;
		qualifier = other.qualifier;
		typeKind = other.typeKind;
	}

	/**
	 * @return the qualifier
	 */
	public TypeReference getQualifier() {
		return qualifier;
	}

	public String getFullQualifiedName() {
		return fullQualifiedName;
	}
	
	/**
	 * @param qualifier the qualifier to set
	 */
	public void setQualifier(TypeReference qualifier) {
		this.qualifier = qualifier;
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

	/**
	 * The core reference of qualified type reference is itself
	 */
	public NameReference getCoreReference() {
		return this;
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
