package nameTable.nameDefinition;

import java.util.ArrayList;
import java.util.List;

import sourceCodeAST.SourceCodeLocation;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.visitor.NameTableVisitor;

/**
 * The class represent a package definition
 * @author Zhou Xiaocong
 * @since 2013-2-1
 * @version 1.0
 * 
 * @update 2015/11/5
 * 		Refactor the class according to the design document
 */
public class PackageDefinition extends NameDefinition implements NameScope {
	private static final String UNNAMED_PACKAGE_NAME = "<UnnamedPckage>";	// The name of the unnamed package
	private List<CompilationUnitScope> unitList = null; 		// The compilation units in the package
	
	private List<NameReference> referenceList = null;			// The references defined in the package directly
															// Generally, it should be null!
	/**
	 * Constructor for unnamed package
	 */
	public PackageDefinition(NameScope scope) {
		super(UNNAMED_PACKAGE_NAME, UNNAMED_PACKAGE_NAME, null, scope);
	}
	
	public PackageDefinition(String packageName, NameScope scope) {
		super(packageName, packageName, null, scope);
	}

	@Override
	public NameDefinitionKind getDefinitionKind() {
		return NameDefinitionKind.NDK_PACKAGE;
	}

	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		throw new IllegalNameDefinition("Can not define names in a package directly!");
	}

	@Override
	public NameScope getEnclosingScope() {
		return scope;
	}

	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_PACKAGE;
	}

	@Override
	public String getScopeName() {
		return simpleName;
	}

	@Override
	public List<NameScope> getSubScopeList() {
		if (unitList == null) return null;
		List<NameScope> result = new ArrayList<NameScope>(unitList.size());
		for (CompilationUnitScope scope : unitList) result.add(scope);
		return result;
	}

	@Override
	public boolean resolve(NameReference reference) {
		// In package definition, we match the name reference to the type defined in the package, i.e. 
		// the package member type definition. 
		if (unitList != null) {
			for (CompilationUnitScope unit : unitList) {
				List<TypeDefinition> types = unit.getTypeList();
				
				if (types != null) {
					for (TypeDefinition type : types) {
						if (type.match(reference)) return true;
					}
				}
			}
		}
		return getEnclosingScope().resolve(reference);
	}

	/**
	 * Match the reference in the type list of the compilation unit
	 */
	public boolean matchTypeWithReference(NameReference reference) {
		if (unitList == null) return false;
		for (CompilationUnitScope unit : unitList) {
			if (unit.match(reference)) return true;
		}
		return false;
	}
	
	/**
	 * Return the compilation unit scope defined in the package
	 */
	public List<CompilationUnitScope> getCompilationUnitScopeList() {
		return unitList;
	}
	
	/**
	 * Return all package member detailed type definition 
	 */
	public List<DetailedTypeDefinition> getAllDetailedTypeDefinitions() {
		List<DetailedTypeDefinition> resultList = new ArrayList<DetailedTypeDefinition>();
		if (unitList == null) return resultList;
		for (CompilationUnitScope unitScope : unitList) {
			List<TypeDefinition> typeList = unitScope.getTypeList();
			if (typeList != null) {
				for (TypeDefinition type : typeList) {
					if (type.isDetailedType()) resultList.add((DetailedTypeDefinition)type);
				}
			}
		}
		return resultList;
	}
	

	/**
	 * Add a compilation unit scope for the package
	 */
	public void addCompilationUnitScope(CompilationUnitScope compUnit) {
		if (unitList == null) unitList = new ArrayList<CompilationUnitScope>();
		unitList.add(compUnit);
	}
	
	/**
	 * Test if the package represent the unnamed package
	 */
	public boolean isUnnamedPackage() {
		if (simpleName.equals(UNNAMED_PACKAGE_NAME)) return true;
		else return false;
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
	public boolean containsLocation(SourceCodeLocation location) {
		if (unitList == null) return false;
		for (CompilationUnitScope unit : unitList) {
			if (unit.getUnitName().equals(location.getFileUnitName())) return true;
		}
		return false;
	}
	
	@Override
	/**
	 * Accept a visitor to visit the current scope
	 */
	public void accept(NameTableVisitor visitor) {
		visitor.preVisit(this);
		
		boolean visitSubscope = visitor.visit(this);
		
		if (visitSubscope == true && unitList != null) {
			for (CompilationUnitScope scope : unitList) scope.accept(visitor);
		}
		
		visitor.endVisit(this);
		visitor.postVisit(this);
	}

	@Override
	public SourceCodeLocation getScopeStart() {
		return null;
	}

	@Override
	public SourceCodeLocation getScopeEnd() {
		return null;
	}
	
	@Override
	public List<NameReference> getReferenceList() {
		return referenceList;
	}
}


