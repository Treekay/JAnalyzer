package nameTable.filter;

import nameTable.nameDefinition.NameDefinition;
import nameTable.nameScope.NameScope;

/**
 * A filter accepted the definition in the given scope
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ9ÈÕ
 * @version 1.0
 *
 */
public class NameDefinitionScopeFilter extends NameTableFilter {
	private NameTableFilter wrappedFilter = null;
	private NameScope acceptableScope = null;
	
	public NameDefinitionScopeFilter(NameTableFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameDefinitionScopeFilter(NameScope acceptableScope) {
		this.acceptableScope = acceptableScope;
	}

	public NameDefinitionScopeFilter(NameTableFilter wrappedFilter, NameScope acceptableScope) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableScope = acceptableScope;
	}

	public void setAccpetableScope(NameScope scope) {
		this.acceptableScope = scope;
	}
	
	@Override
	public boolean accept(NameDefinition definition) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(definition);
		
		if (acceptable && acceptableScope != null) {
			acceptable = (acceptableScope == definition.getScope());
		}
		return acceptable;
	}
}
