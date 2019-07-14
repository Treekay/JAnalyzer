package nameTable.filter;

import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class NameScopeKindFilter extends NameScopeFilter {

	private NameScopeFilter wrappedFilter = null;
	private NameScopeKind acceptableKind = null;
	
	public NameScopeKindFilter (NameScopeFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameScopeKindFilter (NameScopeKind acceptableKind) {
		this.acceptableKind = acceptableKind;
	}

	public NameScopeKindFilter (NameScopeFilter wrappedFilter, NameScopeKind acceptableKind) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableKind = acceptableKind;
	}

	public void setAccpetableKind(NameScopeKind kind) {
		this.acceptableKind = kind;
	}
	
	@Override
	public boolean accept(NameScope scope) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(scope);
		
		if (acceptable && acceptableKind != null) {
			acceptable = (acceptableKind == scope.getScopeKind());
		}
		return acceptable;
	}
}
