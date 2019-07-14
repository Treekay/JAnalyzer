package nameTable.filter;

import nameTable.nameDefinition.NameDefinition;

/**
 * The filter accepts all detailed type definition!
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ1ÈÕ
 * @version 1.0
 */
public class DetailedTypeDefinitionFilter extends NameTableFilter {
	NameTableFilter wrappedFilter = null;

	public DetailedTypeDefinitionFilter() {
	}
	
	public DetailedTypeDefinitionFilter(NameTableFilter wrapped) {
		wrappedFilter = wrapped;
	}

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isDetailedType()) return false;
		if (wrappedFilter != null) return wrappedFilter.accept(definition);
		else return true;
	}
}
