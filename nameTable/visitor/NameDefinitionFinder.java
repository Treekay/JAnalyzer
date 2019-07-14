package nameTable.visitor;

import java.util.List;

import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumConstantDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.ImportedStaticMemberDefinition;
import nameTable.nameDefinition.ImportedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.SystemScope;

/**
 * To find a name definition rather than to get all definitions accepted by the given filter. 
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ9ÈÕ
 * @version 1.0
 *
 */
public class NameDefinitionFinder extends NameTableVisitor {
	private NameTableFilter filter = null;
	private NameDefinition result = null;
	
	public NameDefinitionFinder() {
	}
	
	public NameDefinitionFinder(NameTableFilter filter) {
		this.filter = filter;
	}

	public void reset() {
		result = null;
	}
	
	public void reset(NameTableFilter filter) {
		result = null;
		this.filter = filter;
	}

	public NameDefinition getResult() {
		return result;
	}
	
	public void setFilter(NameTableFilter filter) {
		this.filter = filter;
	}

	/**
	 * Get the definitions in the system scope including the global names defined in the system scope
	 * and the package definitions.
	 */
	public boolean visit(SystemScope scope) {
		List<ImportedTypeDefinition> importedTypeList = scope.getImportedTypeList();
		if (importedTypeList != null) {
			for (ImportedTypeDefinition type : importedTypeList) {
				if (accept(type)) return false;
			}
		}
		
		List<ImportedStaticMemberDefinition> importedStaticMemberList = scope.getImportedStaticMemberList();
		if (importedStaticMemberList != null) {
			for (ImportedStaticMemberDefinition member : importedStaticMemberList) {
				if (accept(member)) return false;
			}
		}

		List<PackageDefinition> packageList = scope.getPackageList();
		if (packageList != null) {
			for (PackageDefinition name : packageList) {
				if (accept(name)) return false;
			}
		}
		return true;
	}
	
	/**
	 * There is no definition defined in a package directly
	 */
	public boolean visit(PackageDefinition scope) {
		return true;
	}
	
	
	/**
	 * Get the type definitions in a compilation unit scope
	 */
	public boolean visit(CompilationUnitScope scope) {
		List<TypeDefinition> types = scope.getTypeList();
		if (types != null) {
			for (TypeDefinition type : types) {
				if (accept(type)) return false;
			}
		}
		return true;
	}
	
	/**
	 * Get the definitions defined in a detailed type, including its fields, methods and types.
	 */
	public boolean visit(DetailedTypeDefinition scope) {
		List<FieldDefinition> fields = scope.getFieldList();
		if (fields != null) {
			for (FieldDefinition field : fields) {
				if (accept(field)) return false;
			}
		}
		List<MethodDefinition> methods = scope.getMethodList();
		if (methods != null) {
			for (MethodDefinition method : methods) {
				if (accept(method)) return false;
			}
		}
		List<DetailedTypeDefinition> types = scope.getTypeList();
		if (types != null) {
			for (DetailedTypeDefinition type : types) {
				if (accept(type)) return false;
			}
		}
		return true;
	}
	
	/**
	 * Get the constants defined in a enum type
	 */
	public boolean visit(EnumTypeDefinition scope) {
		List<EnumConstantDefinition> names = scope.getConstantList();
		if (names != null) {
			for (EnumConstantDefinition name : names) {
				if (accept(name)) return false;
			}
		}
		return true;
	}
	
	/**
	 * Get the parameters defined in a method
	 */
	public boolean visit(MethodDefinition scope) {
		List<VariableDefinition> vars = scope.getParameterList();
		if (vars != null) {
			for (VariableDefinition var : vars) {
				if (accept(var)) return false;
			}
		}
		return true;
	}

	/**
	 * Get local variables and types defined in a local types
	 */
	public boolean visit(LocalScope scope) {
		List<VariableDefinition> vars = scope.getVariableList();
		if (vars != null) {
			for (VariableDefinition var : vars) {
				if (accept(var)) return false;
			}
		}
		List<DetailedTypeDefinition> types = scope.getLocalTypeList();
		if (types != null) {
			for (DetailedTypeDefinition type : types) {
				if (accept(type)) return false;
			}
		}
		return true;
	}
	

	/**
	 * Test if the given definition should be accepted by the filter (if it is not null) of this visitor.
	 */
	private boolean accept(NameDefinition definition) {
		if (filter == null) {
			result = definition;
			return true;
		} else if (filter.accept(definition)) {
			result = definition;
			return true;
		}
		return false;
	}
}
