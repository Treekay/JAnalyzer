package nameTable.visitor;

import java.util.ArrayList;
import java.util.List;

import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.ImportedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.SystemScope;

/**
 * A visitor to visit all references stored in scopes.
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ14ÈÕ
 * @version 1.0
 *
 */
public class NameReferenceVisitor extends NameTableVisitor {

	// The result list of name reference after the visiting
	private List<NameReference> result = new ArrayList<NameReference>();
	// A filter to accept appropriate name definition
	private NameTableFilter filter = null;
	
	public NameReferenceVisitor() {
	}

	public NameReferenceVisitor(NameTableFilter filter) {
		this.filter = filter;
	}
	
	public void reset() {
		result = new ArrayList<NameReference>();
	}
	
	public List<NameReference> getResult() {
		return result;
	}
	
	public void reset(NameTableFilter filter) {
		result = new ArrayList<NameReference>();
		this.filter = filter;
	}

	public void setFilter(NameTableFilter filter) {
		this.filter = filter;
	}

	/**
	 * Get all references in the scope!
	 */
	public boolean visit(SystemScope scope) {
		List<NameReference> referenceList = scope.getReferenceList();
		if (referenceList != null) {
			if (filter == null) result.addAll(scope.getReferenceList());
			else {
				for (NameReference reference : referenceList) {
					if (filter.accept(reference)) result.add(reference);
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Get all references in the scope!
	 */
	public boolean visit(PackageDefinition scope) {
		List<NameReference> referenceList = scope.getReferenceList();
		if (referenceList != null) {
			if (filter == null) result.addAll(scope.getReferenceList());
			else {
				for (NameReference reference : referenceList) {
					if (filter.accept(reference)) result.add(reference);
				}
			}
		}
		return true;
	}
	
	
	/**
	 * Get all references in the scope!
	 */
	public boolean visit(CompilationUnitScope scope) {
		List<NameReference> referenceList = scope.getReferenceList();
		if (referenceList != null) {
			if (filter == null) result.addAll(scope.getReferenceList());
			else {
				for (NameReference reference : referenceList) {
					if (filter.accept(reference)) result.add(reference);
				}
			}
		}
		return true;
	}
	
	/**
	 * Get all references in the scope!
	 */
	public boolean visit(DetailedTypeDefinition scope) {
		List<NameReference> referenceList = scope.getReferenceList();
		if (referenceList != null) {
			if (filter == null) result.addAll(scope.getReferenceList());
			else {
				for (NameReference reference : referenceList) {
					if (filter.accept(reference)) result.add(reference);
				}
			}
		}
		return true;
	}
	
	/**
	 * There is not any reference in imported type definition
	 */
	public boolean visit(ImportedTypeDefinition scope) {
		return false;
	}

	/**
	 * Get all references in the scope!
	 */
	public boolean visit(EnumTypeDefinition scope) {
		List<NameReference> referenceList = scope.getReferenceList();
		if (referenceList != null) {
			if (filter == null) result.addAll(scope.getReferenceList());
			else {
				for (NameReference reference : referenceList) {
					if (filter.accept(reference)) result.add(reference);
				}
			}
		}
		return true;
	}
	
	/**
	 * Get all references in the scope!
	 */
	public boolean visit(MethodDefinition scope) {
		List<NameReference> referenceList = scope.getReferenceList();
		if (referenceList != null) {
			if (filter == null) result.addAll(scope.getReferenceList());
			else {
				for (NameReference reference : referenceList) {
					if (filter.accept(reference)) result.add(reference);
				}
			}
		}
		return true;
	}

	/**
	 * Get all references in the scope!
	 */
	public boolean visit(LocalScope scope) {
		List<NameReference> referenceList = scope.getReferenceList();
		if (referenceList != null) {
			if (filter == null) result.addAll(scope.getReferenceList());
			else {
				for (NameReference reference : referenceList) {
					if (filter.accept(reference)) result.add(reference);
				}
			}
		}
		return true;
	}
	
}
