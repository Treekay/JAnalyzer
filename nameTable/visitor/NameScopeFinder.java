package nameTable.visitor;

import nameTable.filter.NameScopeFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.ImportedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.SystemScope;

/**
 * A visitor to find a name scope (rather than all name scopes) accepted by the given filter.
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ9ÈÕ
 * @version 1.0
 *
 */
public class NameScopeFinder extends NameTableVisitor {
	// The result list of name scope after the visiting
	private NameScope result = null;
	// A filter to accept appropriate name definition
	private NameScopeFilter filter = null;
	
	public NameScopeFinder() {
	}
	
	public NameScopeFinder(NameScopeFilter filter) {
		this.filter = filter;
	}
	
	public void reset() {
		result = null;
	}
	
	public NameScope getResult() {
		return result;
	}
	
	public void setFilter(NameScopeFilter filter) {
		this.filter = filter;
	}

	public boolean visit(SystemScope scope) {
		if (accept(scope)) return false;
		return true;
	}
	
	public boolean visit(PackageDefinition scope) {
		if (accept(scope)) return false;
		return true;
	}
	
	public boolean visit(CompilationUnitScope scope) {
		if (accept(scope)) return false;
		return true;
	}
	
	public boolean visit(DetailedTypeDefinition scope) {
		if (accept(scope)) return false;
		return true;
	}
	
	/**
	 * We do not regard imported type definition as a valid scope
	 */
	public boolean visit(ImportedTypeDefinition scope) {
		return false;
	}

	public boolean visit(EnumTypeDefinition scope) {
		if (accept(scope)) return false;
		return true;
	}
	
	public boolean visit(MethodDefinition scope) {
		if (accept(scope)) return false;
		return true;
	}

	public boolean visit(LocalScope scope) {
		if (accept(scope)) return false;
		return true;
	}
	
	private boolean accept(NameScope scope) {
		if (filter == null) {
			result = scope;
			return true;
		} else if (filter.accept(scope)) {
			result = scope;
			return true;
		}
		return false;
	}
}
