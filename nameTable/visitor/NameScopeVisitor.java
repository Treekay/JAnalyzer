package nameTable.visitor;

import java.util.ArrayList;
import java.util.List;

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
 * A Visitor class for get all name scope in the name table
 * 
 * @author Zhou Xiaocong
 * @since 2015Äê6ÔÂ24ÈÕ
 * @version 1.0
 */
public class NameScopeVisitor extends NameTableVisitor {

	// The result list of name scope after the visiting
	private List<NameScope> result = new ArrayList<NameScope>();
	// A filter to accept appropriate name definition
	private NameScopeFilter filter = null;
	
	public NameScopeVisitor() {
	}
	
	public NameScopeVisitor(NameScopeFilter filter) {
		this.filter = filter;
	}
	
	public void reset() {
		result = new ArrayList<NameScope>();
	}

	public void reset(NameScopeFilter filter) {
		result = new ArrayList<NameScope>();
		this.filter = filter;
	}
	
	public List<NameScope> getResult() {
		return result;
	}
	
	public void setFilter(NameScopeFilter filter) {
		this.filter = filter;
	}

	public boolean visit(SystemScope scope) {
		if (filter == null) result.add(scope);
		else if (filter.accept(scope)) result.add(scope);
		
		return true;
	}
	
	public boolean visit(PackageDefinition scope) {
		if (filter == null) result.add(scope);
		else if (filter.accept(scope)) result.add(scope);
		
		return true;
	}
	
	
	public boolean visit(CompilationUnitScope scope) {
		if (filter == null) result.add(scope);
		else if (filter.accept(scope)) result.add(scope);
		
		return true;
	}
	
	public boolean visit(DetailedTypeDefinition scope) {
		if (filter == null) result.add(scope);
		else if (filter.accept(scope)) result.add(scope);
		
		return true;
	}
	
	/**
	 * We do not regard imported type definition as a valid scope
	 */
	public boolean visit(ImportedTypeDefinition scope) {
		return false;
	}

	public boolean visit(EnumTypeDefinition scope) {
		if (filter == null) result.add(scope);
		else if (filter.accept(scope)) result.add(scope);
		
		return true;
	}
	
	public boolean visit(MethodDefinition scope) {
		if (filter == null) result.add(scope);
		else if (filter.accept(scope)) result.add(scope);
		
		return true;
	}

	public boolean visit(LocalScope scope) {
		if (filter == null) result.add(scope);
		else if (filter.accept(scope)) result.add(scope);
		
		return true;
	}
}
