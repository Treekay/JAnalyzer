package nameTable;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import sourceCodeAST.SourceCodeFile;
import sourceCodeAST.SourceCodeFileSet;
import sourceCodeAST.SourceCodeLocation;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.filter.NameDefinitionLocationFilter;
import nameTable.filter.NameDefinitionNameFilter;
import nameTable.filter.NameDefinitionUniqueIdFilter;
import nameTable.filter.NameScopeFilter;
import nameTable.filter.NameScopeLocationFilter;
import nameTable.filter.NameScopeNameFilter;
import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameDefinitionFinder;
import nameTable.visitor.NameDefinitionNumberVisitor;
import nameTable.visitor.NameDefinitionVisitor;
import nameTable.visitor.NameScopeFinder;
import nameTable.visitor.NameScopeVisitor;
import nameTable.visitor.NameTableVisitor;

/**
 * <p>The class to manage the name table. This class provides methods to access name definitions and name references.
 * We may extend the class to provide more methods to access name definitions and references quickly.
 * <p>Note that we distribute name definitions and name references to scopes, which may slow the access of name definitions
 * and references.
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2013-12-30 Zhou Xiaocong
 * 		Add methods for getting definitions, references or creating references for an AST node! 
 * @update 2014-1-1 Zhou Xiaocong
 * 		Add method int getTotalNumaberOfDefinitions(NameDefinitionKind kind) to do some statistics for definition
 * 		Add method int getTotalNumberOfCompilationUnits()
 * @update 2015-6-24 Zhou Xiaocong
 * 		Add some methods, refer to the notes for detailed information.
 * @update 2016/11/10
 * 		Refactor the class according to the design document
 */
public class NameTableManager {
	private SourceCodeFileSet codeFileSet = null;
	private SystemScope systemScope = null;

	public static NameTableManager createNameTableManager(String projectRootPath) {
		SourceCodeFileSet parser = new SourceCodeFileSet(projectRootPath);
		NameTableCreator creator = new NameDefinitionCreator(parser);
		String root = "C:\\";
		String[] fileNameArray = {root+"ZxcWork\\ToolKit\\data\\javalang.txt", root+"ZxcWork\\ToolKit\\data\\javautil.txt", root+"ZxcWork\\ToolKit\\data\\javaio.txt", }; 

		NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out), fileNameArray);
		if (creator.hasError()) {
			System.out.println("There are " + creator.getErrorUnitNumber() + " error unit files:");
			creator.printErrorUnitList(new PrintWriter(System.out));
			System.out.println();
		}
		return manager;
	}
	
	public static NameTableManager createNameTableManager(String projectRootPath, PrintWriter errorReportWriter) {
		SourceCodeFileSet parser = new SourceCodeFileSet(projectRootPath);
		NameTableCreator creator = new NameDefinitionCreator(parser);
		String root = "C:\\";
		String[] fileNameArray = {root+"ZxcWork\\ToolKit\\data\\javalang.txt", root+"ZxcWork\\ToolKit\\data\\javautil.txt", root+"ZxcWork\\ToolKit\\data\\javaio.txt", }; 

		NameTableManager manager = creator.createNameTableManager(errorReportWriter, fileNameArray);
		if (creator.hasError()) {
			errorReportWriter.println("There are " + creator.getErrorUnitNumber() + " error unit files:");
			creator.printErrorUnitList(errorReportWriter);
			errorReportWriter.println();
		}
		return manager;
	}

	public static NameTableManager createNameTableManager(String projectRootPath, PrintWriter errorReportWriter, String[] externalLibraryHeadFileArray) {
		SourceCodeFileSet parser = new SourceCodeFileSet(projectRootPath);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		NameTableManager manager = creator.createNameTableManager(errorReportWriter, externalLibraryHeadFileArray);
		if (creator.hasError()) {
			errorReportWriter.println("There are " + creator.getErrorUnitNumber() + " error unit files:");
			creator.printErrorUnitList(errorReportWriter);
			errorReportWriter.println();
		}
		return manager;
	}
	
	public static NameTableManager createNameTableManager(String projectRootPath, PrintWriter errorReportWriter, String[] externalLibraryHeadFileArray, boolean includeReferences) {
		SourceCodeFileSet parser = new SourceCodeFileSet(projectRootPath);
		NameTableCreator creator = new NameDefinitionCreator(parser);
		if (includeReferences) creator = new NameTableCreator(parser);

		NameTableManager manager = creator.createNameTableManager(errorReportWriter, externalLibraryHeadFileArray);
		if (creator.hasError()) {
			errorReportWriter.println("There are " + creator.getErrorUnitNumber() + " error unit files:");
			creator.printErrorUnitList(errorReportWriter);
			errorReportWriter.println();
		}
		return manager;
	}


	/**
	 * The component client should not use this constructor to create an instance of NameTableManager.
	 * He should use NameTableCreator.createNameTableManager to get such an instance.  
	 */
	public NameTableManager(SourceCodeFileSet codeFileSet, SystemScope systemScope) {
		this.codeFileSet = codeFileSet;
		this.systemScope = systemScope;
	}
	
	/**
	 * @return the rootScope
	 */
	public SystemScope getSystemScope() {
		return systemScope;
	}

	/**
	 * @return the source code file set
	 */
	public SourceCodeFileSet getSouceCodeFileSet() {
		return codeFileSet;
	}
	
	/**
	 * @return the start path of the code file set
	 */
	public String getSystemPath() {
		return codeFileSet.getStartPath();
	}
	
	
	/**
	 * Get all package definitions in the system
	 */
	public List<PackageDefinition> getAllPackageDefinitions() {
		return systemScope.getPackageList();
	}
	
	/**
	 * Find the package by a package name
	 */
	public PackageDefinition findPackageByName(String packageName) {
		return systemScope.findPackageByName(packageName);
	}

	/**
	 * Get all compilation unit scopes in the system
	 */
	public List<CompilationUnitScope> getAllCompilationUnitScopes() {
		List<PackageDefinition> packageList = systemScope.getPackageList();
		if (packageList == null) return null;
		
		List<CompilationUnitScope> result = new ArrayList<CompilationUnitScope>();
		for (PackageDefinition packageDef : packageList) {
			List<CompilationUnitScope> units = packageDef.getCompilationUnitScopeList();
			if (units == null) continue;
			for (CompilationUnitScope unit: units) result.add(unit);
		}
		return result;
	}
	
	/**
	 * Find the first compilation unit scope in the system by a unit name (= parser.getCurrentUnitFullName()) 
	 */
	public CompilationUnitScope findCompilationUnitScopeByUnitName(String unitName) {
		List<PackageDefinition> packageList = systemScope.getPackageList();
		if (packageList == null) return null;
		
		for (PackageDefinition packageDef : packageList) {
			List<CompilationUnitScope> units = packageDef.getCompilationUnitScopeList();
			if (units == null) continue;
			
			for (CompilationUnitScope unit: units) 
				if (unit.getUnitName().equals(unitName)) return unit;
		}
		return null;
	}
	
	/**
	 * Return the list of all detailed type definition (including internal types, local types and anonymous types)
	 */
	public List<DetailedTypeDefinition> getAllDetailedTypeDefinitions() {
		return systemScope.getAllDetailedTypeDefinitions();
	}
	
	/**
	 * Return the list of all detailed type definition (including internal types, local types and anonymous types) in the given name scope
	 */
	public List<DetailedTypeDefinition> getAllDetailedTypeDefinitions(NameScope scope) {
		List<DetailedTypeDefinition> typeList = new ArrayList<DetailedTypeDefinition>();
		List<DetailedTypeDefinition> allDetailedTypeList = systemScope.getAllDetailedTypeDefinitions();
		for (DetailedTypeDefinition type : allDetailedTypeList) {
			NameScope typeScope = type.getScope();
			if (typeScope == scope) typeList.add(type);
			else if (type.getScope().isEnclosedInScope(scope)) typeList.add(type);
		}
		return typeList;
	}

	/**
	 * Find the first name definition with the given simple name
	 */
	public NameDefinition findDefinitionOfSimpleName(String simpleName) {
		NameDefinitionFinder finder = new NameDefinitionFinder(new NameDefinitionNameFilter(simpleName));
		systemScope.accept(finder);
		return finder.getResult();
	}
	
	/**
	 * Return all definitions with the given simple name
	 */
	public List<NameDefinition> getAllDefinitionsOfSimpleName(String simpleName) {
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionNameFilter(simpleName));
		systemScope.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * Find the first name definition with the given simple name
	 */
	public NameDefinition findDefinitionOfFullQualifiedName(String fullQualifiedName) {
		NameDefinitionFinder finder = new NameDefinitionFinder(new NameDefinitionNameFilter(fullQualifiedName, true));
		systemScope.accept(finder);
		return finder.getResult();
	}

	/**
	 * Return all definitions between the given two source code locations
	 * (greater than or equal to start location and less than end location) 
	 */
	public List<NameDefinition> getAllDefinitionsBetweenLocations(SourceCodeLocation start, SourceCodeLocation end) {
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionLocationFilter(start, end));
		systemScope.accept(visitor);
		return visitor.getResult();
	}
	
	/**
	 * Return all definitions in the given scope (not include its subscope)
	 */
	public List<NameDefinition> getAllDefinitionsOfScope(NameScope scope) {
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		scope.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * Find the first name definition with the given simple name
	 */
	public NameDefinition findDefinitionByFilter(NameTableFilter filter) {
		NameDefinitionFinder finder = new NameDefinitionFinder(filter);
		systemScope.accept(finder);
		return finder.getResult();
	}
	
	/**
	 * Return all definitions accepted by the given filter
	 */
	public List<NameDefinition> getAllDefinitionsByFilter(NameTableFilter filter) {
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(filter);
		systemScope.accept(visitor);
		return visitor.getResult();
	}


	/**
	 * Find a name definition by its unique Id
	 */
	public NameDefinition findDefinitionById(String nameDefinitionId) {
		NameDefinitionFinder finder = new NameDefinitionFinder(new NameDefinitionUniqueIdFilter(nameDefinitionId));

		String locationString = NameDefinition.getDefinitionLocationStringFromId(nameDefinitionId);
		if (locationString != null) {
			String fullUnitName = SourceCodeLocation.getFileUnitNameFromId(locationString);
			if (fullUnitName != null) {
				List<PackageDefinition> packageList = systemScope.getPackageList();
				if (packageList == null) return null;
	
				for (PackageDefinition packageDef : packageList) {
					List<CompilationUnitScope> units = packageDef.getCompilationUnitScopeList();
					if (units == null) continue;
					for (CompilationUnitScope unit: units) {
						if (unit.getUnitName().equals(fullUnitName)) unit.accept(finder);
					}
				}
			} else return null;
		} else systemScope.accept(finder);
		return finder.getResult();
	}
	
	/**
	 * Get a name scope enclosing a source code location exactly, i.e. the location is in the scope, and 
	 * there is no other scope enclosing the location and is enclosed in the returned scope!
	 */
	public NameScope getScopeOfLocation(SourceCodeLocation location) {
		NameScope result = systemScope;
		List<NameScope> subscopes = result.getSubScopeList();
		while (subscopes != null) {
			boolean findScope = false;
			// Test if there is a sub-scope enclosing the given location
			for (NameScope scope : subscopes) {
				if (scope.containsLocation(location)) {
					findScope = true; 
					result = scope;
					
					subscopes = result.getSubScopeList();
					
					break;
				} else {
					// Check the next scope!
				}
			}
			if (!findScope) {
				// There is no sub-scope enclosing the given location. If the result is the system scope, we 
				// return null, because a real location for name reference or name definition can not be in 
				// the system scope!
				if (result == systemScope) return null;  
				else return result;
			} // else we continue to test if there is a sub-scope enclosing the given location
		}
		
		// The result scope enclosing the given location and it has not sub-scopes, so return it!
		return result;
	}

	/**
	 * Get a name scope enclosing a source code location exactly, i.e. the location is in the scope, and 
	 * there is no other scope enclosing the location and is enclosed in the returned scope!
	 */
	public NameScope getScopeOfStartAndEndLocation(SourceCodeLocation startLocation, SourceCodeLocation endLocation, NameScope startScope) {
		NameScopeFinder finder = new NameScopeFinder(new NameScopeLocationFilter(startLocation, endLocation));
		if (startScope != null) startScope.accept(finder); 
		else systemScope.accept(finder);
		return finder.getResult();
	}

	/**
	 * Get a name scope enclosing a source code location exactly, i.e. the location is in the scope, and 
	 * there is no other scope enclosing the location and is enclosed in the returned scope!
	 */
	public NameScope getScopeOfStartAndEndLocation(SourceCodeLocation startLocation, SourceCodeLocation endLocation) {
		NameScopeFinder finder = new NameScopeFinder(new NameScopeLocationFilter(startLocation, endLocation));
		systemScope.accept(finder);
		return finder.getResult();
	}

	/**
	 * Find the first name definition with the given simple name
	 */
	public List<NameScope> getAllScopesOfName(String name) {
		NameScopeVisitor finder = new NameScopeVisitor(new NameScopeNameFilter(name));
		systemScope.accept(finder);
		return finder.getResult();
	}
	
	/**
	 * Given a name definition, return the compilation unit scope enclosing the name. 
	 */
	public CompilationUnitScope getEnclosingCompilationUnitScope(NameDefinition name) {
		NameScope currentScope = name.getScope();
		
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_COMPILATION_UNIT) return (CompilationUnitScope)currentScope;
			currentScope = currentScope.getEnclosingScope();
		}
		
		return null;
	}

	/**
	 * Given a name reference, return the compilation unit scope enclosing the name. 
	 */
	public CompilationUnitScope getEnclosingCompilationUnitScope(NameReference name) {
		NameScope currentScope = name.getScope();
		
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_COMPILATION_UNIT) return (CompilationUnitScope)currentScope;
			currentScope = currentScope.getEnclosingScope();
		}
		
		return null;
	}

	/**
	 * Given a name definition, return the detailed type enclosing the name. 
	 */
	public DetailedTypeDefinition getEnclosingDetailedTypeDefinition(NameDefinition name) {
		NameScope currentScope = name.getScope();
		
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_DETAILED_TYPE) return (DetailedTypeDefinition)currentScope;
			currentScope = currentScope.getEnclosingScope();
		}
		
		return null;
	}

	/**
	 * Given a name reference, return the detailed type enclosing the name. 
	 */
	public DetailedTypeDefinition getEnclosingDetailedTypeDefinition(NameReference name) {
		NameScope currentScope = name.getScope();
		
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_DETAILED_TYPE) return (DetailedTypeDefinition)currentScope;
			currentScope = currentScope.getEnclosingScope();
		}
		
		return null;
	}
	
	/**
	 * Given a name definition, return the detailed type enclosing the name. 
	 */
	public MethodDefinition getEnclosingMethodDefinition(NameDefinition name) {
		NameScope currentScope = name.getScope();
		
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_METHOD) return (MethodDefinition)currentScope;
			currentScope = currentScope.getEnclosingScope();
		}
		
		return null;
	}

	/**
	 * Given a name reference, return the detailed type enclosing the name. 
	 */
	public MethodDefinition getEnclosingMethodDefinition(NameReference name) {
		NameScope currentScope = name.getScope();
		
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_METHOD) return (MethodDefinition)currentScope;
			currentScope = currentScope.getEnclosingScope();
		}
		
		return null;
	}
	
	/**
	 * Return all scopes accepted by the give filter
	 */
	public List<NameScope> getAllScopesByFilter(NameScopeFilter filter) {
		NameScopeVisitor visitor = new NameScopeVisitor(filter);
		systemScope.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * Return the number of all definition with the given kind
	 */
	public int getTotalNumberOfDefinitions(NameDefinitionKind kind) {
		NameDefinitionNumberVisitor visitor = new NameDefinitionNumberVisitor(new NameDefinitionKindFilter(kind));
		systemScope.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * Return the number of all definition accepted by the give filter
	 */
	public int getTotalNumberOfDefinitions(NameTableFilter filter) {
		NameDefinitionNumberVisitor visitor = new NameDefinitionNumberVisitor(filter);
		systemScope.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * Return the full path of the file contains the given name definition
	 */
	public String getCorrespondingFilePath(NameDefinition definition) {
		CompilationUnitScope unitScope = getEnclosingCompilationUnitScope(definition);
		if (unitScope == null) return null;
		SourceCodeFile codeFile = codeFileSet.findSourceCodeFileByFileUnitName(unitScope.getUnitName());
		if (codeFile == null) return null;
		return codeFile.getFileHandle().getAbsolutePath();
	}
	
	
	/**
	 * Accept a name table visitor to visit name definitions and references
	 */
	public void accept(NameTableVisitor visitor) {
		systemScope.accept(visitor);
	}
	
}

