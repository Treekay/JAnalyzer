package nameTable.nameScope;

import java.util.ArrayList;
import java.util.List;

import sourceCodeAST.SourceCodeLocation;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.IllegalNameDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.ImportedTypeDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.visitor.NameTableVisitor;

/**
 * The class represents a compilation unit scope
 * 
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2014-1-1 Zhou Xiaocong
 * 		Modify the method resolve() and bindImportDeclaration() to support on-demand import declaration!
 * 		Modify the field 'types' to List<DetailedTypeDefinition>
 * 
 * @update 2016/11/6
 * 		Refactor the class according to the design document
 */
public class CompilationUnitScope implements NameScope, Comparable<CompilationUnitScope> {
	// File name of the compilation unit. It is used to generation the source code location of the name definitions and name references
	private String unitName = null;				
	private PackageDefinition enclosingPackage = null;				// The package of the compilation unit
	private List<TypeDefinition> typeList = null;					// The type definitions defined in the compilation unit
	private List<NameReference> importedTypeList = null;			// The type reference or package reference imported by the compilation unit
	private List<NameReference> importedStaticMemberList = null;	// The type reference or package reference imported by the compilation unit
	
	private SourceCodeLocation startLocation = null;
	private SourceCodeLocation endLocation = null;
	
	private List<NameReference> referenceList = null;	// The references occurs in the compilation unit directly. Generally, it will be null!

	public CompilationUnitScope(String unitName, PackageDefinition enclosingPackage, SourceCodeLocation start, SourceCodeLocation end) {
		this.unitName = unitName;
		this.enclosingPackage = enclosingPackage;
		this.startLocation = start;
		this.endLocation = end; 
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#define(nameTable.NameDefinition)
	 */
	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		if (nameDef.getDefinitionKind() == NameDefinitionKind.NDK_TYPE) {
			TypeDefinition typeDef = (TypeDefinition)nameDef;
			if (!typeDef.isDetailedType() && !typeDef.isEnumType()) throw new IllegalNameDefinition("The name defined in a compilation unit must be a detailed type or enumeration!");
			if (!typeDef.isPackageMember()) throw new IllegalNameDefinition("The name defined in a compilation unit must be a top level type!");
			if (typeList == null) typeList = new ArrayList<TypeDefinition>();
			typeList.add(typeDef);
		} else throw new IllegalNameDefinition("The name defined in a compilation unit must be a type!");
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getEnclosingScope()
	 */
	@Override
	public NameScope getEnclosingScope() {
		return enclosingPackage;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getNameScopeKind()
	 */
	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_COMPILATION_UNIT;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getScopeName()
	 */
	@Override
	public String getScopeName() {
		return unitName;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getSubScopeList()
	 */
	@Override
	public List<NameScope> getSubScopeList() {
		if (typeList == null) return null;
		List<NameScope> result = new ArrayList<NameScope>();
		for (TypeDefinition type : typeList) {
			if (type.isEnumType()) result.add((EnumTypeDefinition)type);
			else if (type.isDetailedType()) result.add((DetailedTypeDefinition)type);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#resolve(nameTable.NameReference)
	 */
	@Override
	public boolean resolve(NameReference reference) {
		NameReferenceKind refKind = reference.getReferenceKind();
		if (refKind != NameReferenceKind.NRK_TYPE && refKind != NameReferenceKind.NRK_PACKAGE) {
			// In a compilation unit, and its enclosing scope (i.e. a package, or the system scope), we can only resolve 
			// a package reference or a type reference 
			return false;
		}
		
		// Match the reference in the type list
		if (refKind == NameReferenceKind.NRK_TYPE && typeList != null) {
			for (TypeDefinition type : typeList) {
				if (type.match(reference)) return true;
			}
		}
		
		// Match the reference in the imported type list
		if (importedTypeList != null) {
			for (NameReference importedType : importedTypeList) {
				NameDefinition nameDef = importedType.getDefinition();
				
				if (nameDef != null) {
					NameDefinitionKind defKind = nameDef.getDefinitionKind();
					if (defKind == NameDefinitionKind.NDK_PACKAGE && refKind == NameReferenceKind.NRK_TYPE) {
						// The import declaration refer to a package (i.e. an on-demand declaration), and the reference is a type reference, 
						// we try to match the reference with the detailed type defined in the package!
						PackageDefinition packageDef = (PackageDefinition)nameDef;
						if (packageDef.resolve(reference)) return true;
					} else {
						// The import declaration refer to a package and the reference is a package reference, or the import declaration refer to
						// a type and the reference is also a type reference, we try to match their names
						if (nameDef.match(reference)) return true; 
					}
				}
			}
		}
		
		// Resolve the reference in the enclosing scope
		return getEnclosingScope().resolve(reference);
	}

	/**
	 * @return the fullFileName
	 */
	public String getUnitName() {
		return unitName;
	}

	/**
	 * @return the packageScope
	 */
	public PackageDefinition getEnclosingPackage() {
		return enclosingPackage;
	}
	
	/**
	 * @return the types
	 */
	public List<TypeDefinition> getTypeList() {
		return typeList;
	}

	/**
	 * @return the imported type list
	 */
	public List<NameReference> getImportedTypeList() {
		return importedTypeList;
	}

	/**
	 * Add imported type reference to the imported type list
	 */
	public void addImportedTypeReference(NameReference importedType) {
		if (importedTypeList == null) importedTypeList = new ArrayList<NameReference>();
		importedTypeList.add(importedType);
	}
	
	/**
	 * @return the imported static member list
	 */
	public List<NameReference> getImportedStaticMemberList() {
		return importedStaticMemberList;
	}

	/**
	 * Add imported static member reference to the imported static member list
	 */
	public void addImportedStaticMemberReference(NameReference importedStaticMember) {
		if (importedStaticMemberList == null) importedStaticMemberList = new ArrayList<NameReference>();
		importedStaticMemberList.add(importedStaticMember);
	}
	
	/**
	 * Match the reference in the type list of the compilation unit
	 */
	public boolean match(NameReference reference) {
		if (typeList == null) return false;
		for (TypeDefinition type : typeList) {
			if (type.match(reference)) return true;
		}
		return false;
	}
	
	/**
	 * Bind the type reference in the import list to the appropriate type definition
	 */
	public void bindImportDeclaration() {
		SystemScope systemScope = (SystemScope)getEnclosingScope().getEnclosingScope();
		if (systemScope == null) throw new AssertionError("The system scope is null in compilation unit: " + unitName + "!");
		
		if (importedTypeList == null) return;
		
		for (NameReference importDecl : importedTypeList) {
			if (importDecl.getReferenceKind() == NameReferenceKind.NRK_TYPE) {
				// The import declaration is a single-type-import declaration
				boolean success = false;
				String name = importDecl.getName(); 
				int dotIndex = name.lastIndexOf(NameReferenceLabel.NAME_QUALIFIER);
				if (dotIndex < 1) throw new AssertionError("The imported type [" + name + "] at " + importDecl.getLocation().getFileUnitName() + " have not package name!");
				
				String packageName = name.substring(0, dotIndex);
				String typeName = name.substring(dotIndex + 1);
				
				PackageDefinition packageDef = systemScope.findPackageByName(packageName);
				if (packageDef != null) {
					success =  packageDef.matchTypeWithReference(importDecl);
				} else {
					success = systemScope.resolve(importDecl);
				}

				if (!success) {
					ImportedTypeDefinition typeDef = new ImportedTypeDefinition(typeName, name, systemScope);
					systemScope.define(typeDef);
					importDecl.bindTo(typeDef);
				} else {
					if (importDecl.getDefinition() == null) throw new AssertionError("Internal error for bind imported type!");
				}
			} else {
				// The import declaration is an on-demand import, and it refer to a package reference
				String packageName = importDecl.getName();
				PackageDefinition packageDef = systemScope.findPackageByName(packageName);
				// If we find a package definition, we bind the reference to this package definition, otherwise we do nothing!
				if (packageDef != null) importDecl.bindTo(packageDef);
			}
		}
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
		return unitName.equals(location.getFileUnitName());
	}

	@Override
	/**
	 * Accept a visitor to visit the current scope
	 */
	public void accept(NameTableVisitor visitor) {
		visitor.preVisit(this);
		
		boolean visitSubscope = visitor.visit(this);
		
		if (visitSubscope == true && typeList != null) {
			for (TypeDefinition type : typeList) {
				if (type.isEnumType()) {
					EnumTypeDefinition enumType = (EnumTypeDefinition)type;
					enumType.accept(visitor);
				} else if (type.isDetailedType()) {
					DetailedTypeDefinition detailedType = (DetailedTypeDefinition)type;
					detailedType.accept(visitor);
				}
			}
		}
		
		visitor.endVisit(this);
		visitor.postVisit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unitName == null) ? 0 : unitName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;

		if (!(obj instanceof CompilationUnitScope)) return false;
		CompilationUnitScope other = (CompilationUnitScope)obj;
		return unitName.equals(other.unitName);
	}
	
	@Override
	public int compareTo(CompilationUnitScope other) {
		return unitName.compareTo(other.unitName);
	}

	@Override
	public SourceCodeLocation getScopeStart() {
		return startLocation;
	}

	@Override
	public SourceCodeLocation getScopeEnd() {
		return endLocation;
	}
	
	@Override
	public List<NameReference> getReferenceList() {
		return referenceList;
	}
}
