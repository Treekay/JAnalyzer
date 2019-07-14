package nameTable.filter;

import nameTable.nameReference.NameReference;
import nameTable.nameScope.NameScope;

/**
 * The filter accepts name references in the given scope
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ13ÈÕ
 * @version 1.0
 *
 */
public class NameReferenceScopeFilter extends NameTableFilter {
	private NameTableFilter wrappedFilter = null;
	private NameScope acceptableScope = null;
	
	public NameReferenceScopeFilter(NameTableFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameReferenceScopeFilter(NameScope acceptableScope) {
		this.acceptableScope = acceptableScope;
	}

	public NameReferenceScopeFilter(NameTableFilter wrappedFilter, NameScope acceptableScope) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableScope = acceptableScope;
	}

	public void setAccpetableScope(NameScope scope) {
		this.acceptableScope = scope;
	}
	
	@Override
	public boolean accept(NameReference reference) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(reference);
		
		if (acceptable && acceptableScope != null) {
			acceptable = (acceptableScope == reference.getScope());
		}
		return acceptable;
	}
}
