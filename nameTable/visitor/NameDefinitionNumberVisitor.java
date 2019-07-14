package nameTable.visitor;

import java.util.List;

import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumConstantDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.SystemScope;

/**
 * A visitor for counting the number of name definitions accepted by the filter.
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ14ÈÕ
 * @version 1.0
 *
 */
public class NameDefinitionNumberVisitor extends NameTableVisitor {


	// The result number of name definition after the visiting
	private int result = 0;
	// A filter to accept appropriate name definition
	private NameTableFilter filter = null;
	
	public NameDefinitionNumberVisitor() {
	}

	public NameDefinitionNumberVisitor(NameTableFilter filter) {
		this.filter = filter;
	}
	
	public void reset() {
		result = 0;
	}
	
	public int getResult() {
		return result;
	}
	
	public void reset(NameTableFilter filter) {
		result = 0;
		this.filter = filter;
	}

	public void setFilter(NameTableFilter filter) {
		this.filter = filter;
	}

	/**
	 * Just counter the number of package definitions in the system scope, not including imported type and static member.
	 */
	public boolean visit(SystemScope scope) {
		List<PackageDefinition> packageList = scope.getPackageList();
		if (packageList != null) {
			for (PackageDefinition name : packageList) {
				if (filter == null) result++;
				else if (filter.accept(name)) result++;
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
				if (filter == null) result++;
				else if (filter.accept(type)) result++;
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
				if (filter == null) result++;
				else if (filter.accept(field)) result++;
			}
		}
		List<MethodDefinition> methods = scope.getMethodList();
		if (methods != null) {
			for (MethodDefinition method : methods) {
				if (filter == null) result++;
				else if (filter.accept(method)) result++;
			}
		}
		List<DetailedTypeDefinition> types = scope.getTypeList();
		if (types != null) {
			for (DetailedTypeDefinition type : types) {
				if (filter == null) result++;
				else if (filter.accept(type)) result++;
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
				if (filter == null) result++;
				else if (filter.accept(name)) result++;
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
				if (filter == null) result++;
				else if (filter.accept(var)) result++;
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
				if (filter == null) result++;
				else if (filter.accept(var)) result++;
			}
		}
		List<DetailedTypeDefinition> types = scope.getLocalTypeList();
		if (types != null) {
			for (DetailedTypeDefinition type : types) {
				if (filter == null) result++;
				else if (filter.accept(type)) result++;
			}
		}
		return true;
	}
}
