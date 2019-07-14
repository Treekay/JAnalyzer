package nameTable.nameDefinition;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.visitor.NameTableVisitor;
import sourceCodeAST.SourceCodeLocation;

/**
 * The class represents a enumeration type definition
 * @author Zhou Xiaocong
 * @since 2013-2-27
 * @version 1.0
 * 
 * @update 2015/11/5
 * 		Refactor the class according to the design document
 */
public class EnumTypeDefinition extends TypeDefinition implements NameScope {
	private List<EnumConstantDefinition> constantList = null;
	private List<TypeReference> superList = null;
	private SourceCodeLocation endLocation = null;

	private List<NameReference> referenceList = null;
	private int modifier = 0; 									// The modifier flag of the enum type
	
	public EnumTypeDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, 
			NameScope scope, SourceCodeLocation endLocation) {
		super(simpleName, fullQualifiedName, location, scope);
		this.endLocation = endLocation;
	}

	@Override
	public boolean containsLocation(SourceCodeLocation location) {
		return location.isBetween(this.location, endLocation);
	}

	/* (non-Javadoc)
	 * @see nameTable.TypeDefinition#isDetailedType()
	 */
	@Override
	public boolean isDetailedType() {
		return false;
	}

	@Override
	public boolean isEnumType() {
		return true;
	}
	
	public boolean isInterface() {
		return false;
	}

	/**
	 * Set the modifier flag 
	 */
	public void setModifierFlag(int flag) {
		this.modifier = flag;
	}
	
	/**
	 * Return the enumeration constants defined in the enumeration type
	 */
	public List<EnumConstantDefinition> getConstantList() {
		return constantList;
	}
	
	/**
	 * Add an enumeration constant for the enumeratino type
	 */
	public void addConstant(EnumConstantDefinition constant) {
		if (constantList == null) constantList = new ArrayList<EnumConstantDefinition>();
		constantList.add(constant);
	}

	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		if (nameDef.getDefinitionKind() != NameDefinitionKind.NDK_ENUM_CONSTANT) 
			throw new IllegalNameDefinition("Only enum constant can be defined in a enum definition!");
		if (constantList == null) constantList = new ArrayList<EnumConstantDefinition>();
		constantList.add((EnumConstantDefinition) nameDef);
	}

	/**
	 * Return the package definition object which this detailed type belongs to 
	 */
	public PackageDefinition getEnclosingPackage() {
		NameScope currentScope = scope;
		while (currentScope.getScopeKind() != NameScopeKind.NSK_PACKAGE) currentScope = currentScope.getEnclosingScope();
		return (PackageDefinition)currentScope;
	}

	/**
	 * Test if the class is public according to the modifier flag
	 */
	public boolean isPublic() {
		return Modifier.isPublic(modifier);
	}
	
	@Override
	public NameScope getEnclosingScope() {
		return scope;
	}

	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_ENUM_TYPE;
	}

	@Override
	public String getScopeName() {
		return simpleName;
	}

	@Override
	public List<NameScope> getSubScopeList() {
		return null;
	}

	@Override
	public boolean resolve(NameReference reference) {
		if (reference.getReferenceKind() == NameReferenceKind.NRK_TYPE){
			if (this.match(reference)) return true;
		}
		if (constantList != null) {
			for (EnumConstantDefinition constant : constantList) {
				if (constant.match(reference)) return true;
			}
		}

		// If we can not match the name in the fields, methods and types of the type, we resolve the name 
		// reference in the super class of the type
		TypeDefinition superTypeDef = getSuperClassDefinition();
		if (superTypeDef != null) {
			// Resolve the reference in the super class of the type 
			if (superTypeDef.resolve(reference)) return true;
		}
		
		// If we can resolve the name reference in the super class, we try to resolve it in the parent scope
		return getEnclosingScope().resolve(reference);
	}

	/**
	 * Return the super types of the enumeration type
	 */
	public List<TypeReference> getSuperList() {
		return superList;
	}

	/**
	 * Add a super type for the enumeration type
	 */
	public void addSuperType(TypeReference superType) {
		if (superList == null) superList = new ArrayList<TypeReference>();
		superList.add(superType);
	}

	@Override
	public TypeDefinition getSuperClassDefinition() {
		if (superList == null) return null;
		if (superList.size() < 1) return null;
		TypeReference superClassRef = superList.get(0);
		if (superClassRef.resolveBinding()) {
			TypeDefinition superTypeDefinition = (TypeDefinition)superClassRef.getDefinition();
			if (superTypeDefinition.isInterface()) return null;
			else return superTypeDefinition;
		}
		
		return null;
	}

	@Override
	public void addReference(NameReference reference) {
		if (reference == null) return;
		if (referenceList == null) referenceList = new ArrayList<NameReference>();
		referenceList.add(reference);
		
	}

	@Override
	public boolean isEnclosedInScope(NameScope ancestorScope) {
		NameScope parent = getEnclosingScope();
		while (parent != null) {
			if (parent == ancestorScope) return true;
			parent = parent.getEnclosingScope();
		}
		return false;
	}
	
	@Override
	/**
	 * Accept a visitor to visit the current scope
	 */
	public void accept(NameTableVisitor visitor) {
		visitor.preVisit(this);
		
		visitor.visit(this);
		
		visitor.endVisit(this);
		visitor.postVisit(this);
	}
	
	@Override
	public SourceCodeLocation getScopeStart() {
		return getLocation();
	}

	@Override
	public SourceCodeLocation getScopeEnd() {
		// TODO Auto-generated method stub
		return endLocation;
	}
	
	@Override
	public List<NameReference> getReferenceList() {
		return referenceList;
	}
	
}
