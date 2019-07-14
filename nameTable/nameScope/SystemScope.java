package nameTable.nameScope;

import java.util.ArrayList;
import java.util.List;

import sourceCodeAST.SourceCodeLocation;
import nameTable.filter.DetailedTypeDefinitionFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.IllegalNameDefinition;
import nameTable.nameDefinition.ImportedStaticMemberDefinition;
import nameTable.nameDefinition.ImportedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.visitor.NameDefinitionVisitor;
import nameTable.visitor.NameTableVisitor;

/**
 * The class represents the system scope, which can be regarded as the entry to the name table. 
 * 
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 */
public class SystemScope implements NameScope {
	public static final String SYSTEM_PACKAGE_NAME = "java.lang";
	public static final String ROOT_OBJECT_NAME = "Object";
	
	private static final String SYSTEM_SCOPE_NAME = "<System>";	// The default name of the system scope
	private List<PackageDefinition> packageList = null;			// The packages of the system
	
	private List<ImportedTypeDefinition> importedTypeList = null;
	private List<ImportedStaticMemberDefinition> importedStaticMemberList = null;
	
	private List<NameReference> referenceList = null;				// The references occurs in the system scope. Generally, it will be null!
	
	private List<DetailedTypeDefinition> allDetailedTypeList = null;	// A buffer to store a list of all detailed type definition.
	private ImportedTypeDefinition rootObject = null;
	
	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		if (nameDef.getDefinitionKind() == NameDefinitionKind.NDK_PACKAGE) {
			if (packageList == null) packageList = new ArrayList<PackageDefinition>();
			packageList.add((PackageDefinition) nameDef);
		} else if (nameDef.getDefinitionKind() == NameDefinitionKind.NDK_TYPE) {
			if (importedTypeList == null) importedTypeList = new ArrayList<ImportedTypeDefinition>();
			importedTypeList.add((ImportedTypeDefinition)nameDef);
			if (nameDef.getSimpleName().equals(ROOT_OBJECT_NAME) && nameDef.getFullQualifiedName().equals(SYSTEM_PACKAGE_NAME + "." + ROOT_OBJECT_NAME)) 
				rootObject = (ImportedTypeDefinition)nameDef;
		} else if (nameDef.getDefinitionKind() == NameDefinitionKind.NDK_STATIC_MEMBER) {
			if (importedStaticMemberList == null) importedStaticMemberList = new ArrayList<ImportedStaticMemberDefinition>();
			importedStaticMemberList.add((ImportedStaticMemberDefinition)nameDef);
		}
	}

	public ImportedTypeDefinition getRootObjectDefinition() {
		return rootObject;
	}
	
	@Override
	public NameScope getEnclosingScope() {
		return null;
	}

	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_SYSTEM;
	}

	@Override
	public String getScopeName() {
		return SYSTEM_SCOPE_NAME;
	}

	@Override
	public List<NameScope> getSubScopeList() {
		if (packageList == null) return null;
		List<NameScope> result = new ArrayList<NameScope>();
		for (PackageDefinition pkgDef : packageList) result.add(pkgDef);
		return result;
	}

	@Override
	/**
	 * For system scope, resolving a name reference is to match the reference in the name list 
	 * defined in the system scope!
	 */
	public boolean resolve(NameReference reference) {
		PackageDefinition systemPackage = null;

		if (packageList != null) {
			for (PackageDefinition packageDef : packageList) {
				// Test if there is the system package!
				if (packageDef.getFullQualifiedName().equals(SystemScope.SYSTEM_PACKAGE_NAME)) systemPackage = packageDef;
				if (packageDef.match(reference)) return true;
			}
		}
		
		if (importedTypeList != null){
			for (ImportedTypeDefinition importedType : importedTypeList) {
				if (importedType.match(reference)) return true;
			}
		}
		
		if (importedStaticMemberList != null){
			for (ImportedStaticMemberDefinition importedStaticMember : importedStaticMemberList) {
				if (importedStaticMember.match(reference)) return true;
			}
		}
		
		if (systemPackage != null) {
			if (reference.getReferenceKind() == NameReferenceKind.NRK_TYPE || reference.getReferenceKind() == NameReferenceKind.NRK_LITERAL) {
				// Finally, try to match the reference (a type, or a literal) in the system package!
				return systemPackage.matchTypeWithReference(reference);
			} else return false;
		} else return false;
	}

	/**
	 * @return the packages
	 */
	public List<PackageDefinition> getPackageList() {
		return packageList;
	}

	/**
	 * Find the package definition by name
	 */
	public PackageDefinition findPackageByName(String packageName) {
		if (packageList == null) return null;
		for (PackageDefinition packageDef : packageList) {
			if (packageDef.getFullQualifiedName().equals(packageName)) return packageDef;
		}
		return null;
	}
	
	public PackageDefinition getUnnamedPackageDefinition() {
		if (packageList == null) return null;
		for (PackageDefinition packageDef : packageList) {
			if (packageDef.isUnnamedPackage()) return packageDef;
		}
		return null;
	}
	
	/**
	 * @return the imported type list
	 */
	public List<ImportedTypeDefinition> getImportedTypeList() {
		return importedTypeList;
	}

	/**
	 * @return the imported static member list
	 */
	public List<ImportedStaticMemberDefinition> getImportedStaticMemberList() {
		return importedStaticMemberList;
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
		return true;
	}

	
	public List<DetailedTypeDefinition> getAllDetailedTypeDefinitions() {
		// If this method has been called, then we return the result directly
		if (allDetailedTypeList != null) return allDetailedTypeList;	
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new DetailedTypeDefinitionFilter());
		
		this.accept(visitor);
		allDetailedTypeList = new ArrayList<DetailedTypeDefinition>();
		List<NameDefinition> resultList = visitor.getResult();
		for (NameDefinition definition : resultList) {
			allDetailedTypeList.add((DetailedTypeDefinition)definition);
		}
		
		return allDetailedTypeList;
	}

	
	/**
	 * Return all methods defined in the sub-type of the baseType (and not equal to baseType), and redefine (i.e. override) the given method!
	 */
	public List<MethodDefinition> getAllOverrideMethods(DetailedTypeDefinition baseType, MethodDefinition method) {
		List<MethodDefinition> result = new ArrayList<MethodDefinition>();
		
		List<DetailedTypeDefinition> allDetailedTypeList = getAllDetailedTypeDefinitions();
		for (DetailedTypeDefinition type : allDetailedTypeList) {
			
			if (type != baseType && type.isSubtypeOf(baseType)) {
				List<MethodDefinition> methodList = type.getMethodList();
				if (methodList != null) {
					for (MethodDefinition methodInSubType : methodList) {
						if (methodInSubType.isOverrideMethod(method)) {
							result.add(methodInSubType);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Return all methods defined in the sub-type of the baseType (and not equal to baseType), and redefine (i.e. override) the given method!
	 */
	public List<MethodDefinition> getAllOverrideMethods(ImportedTypeDefinition baseType, MethodDefinition method) {
		List<MethodDefinition> result = new ArrayList<MethodDefinition>();
		
		for (ImportedTypeDefinition type : importedTypeList) {
			if (type != baseType && type.isSubtypeOf(baseType)) {
				List<MethodDefinition> methodList = type.getMethodList();
				if (methodList != null) {
					for (MethodDefinition methodInSubType : methodList) {
						if (methodInSubType.isOverrideMethod(method)) {
							result.add(methodInSubType);
						}
					}
				}
			}
		}
		return result;
	}
	
	public static SystemScope getRootScope(NameScope startScope) {
		NameScope currentScope = startScope;
		while (currentScope != null) {
			NameScope parent = currentScope.getEnclosingScope();
			if (parent == null) break;
			currentScope = parent;
		}
		if (currentScope == null || currentScope.getScopeKind() != NameScopeKind.NSK_SYSTEM) 
			throw new AssertionError("The root scope of " + startScope.getScopeName() + " is not a system scope!");
		return (SystemScope)currentScope;
	}

	@Override
	/**
	 * Accept a visitor to visit the current scope
	 */
	public void accept(NameTableVisitor visitor) {
		visitor.preVisit(this);
		
		boolean visitSubscope = visitor.visit(this);
		if (visitSubscope == true && packageList != null) {
			for (PackageDefinition pkgDef : packageList) pkgDef.accept(visitor);
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
