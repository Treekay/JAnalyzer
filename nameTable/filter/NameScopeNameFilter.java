package nameTable.filter;

import nameTable.nameScope.NameScope;

/**
 * A filter accepted the scope with the given name
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ9ÈÕ
 * @version 1.0
 *
 */
public class NameScopeNameFilter extends NameScopeFilter {
	private NameScopeFilter wrappedFilter = null;
	private String acceptableName = null;
	
	public NameScopeNameFilter(NameScopeFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameScopeNameFilter(String acceptableName) {
		this.acceptableName = acceptableName;
	}

	public NameScopeNameFilter(NameScopeFilter wrappedFilter, String acceptableName) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableName = acceptableName;
	}

	public void setAccpetableName(String name) {
		this.acceptableName = name;
	}
	
	@Override
	public boolean accept(NameScope scope) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(scope);
		
		if (acceptable && acceptableName != null) {
			acceptable = acceptableName.equals(scope.getScopeName());
		}
		return acceptable;
	}
}
