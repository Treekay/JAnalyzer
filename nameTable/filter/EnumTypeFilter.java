package nameTable.filter;

import nameTable.nameDefinition.NameDefinition;

/**
 * The filter accepts all enum type
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ14ÈÕ
 * @version 1.0
 *
 */
public class EnumTypeFilter extends NameTableFilter {
	NameTableFilter wrappedFilter = null;

	public EnumTypeFilter() {
	}
	
	public EnumTypeFilter(NameTableFilter wrapped) {
		wrappedFilter = wrapped;
	}

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isEnumType()) return false;
		if (wrappedFilter != null) return wrappedFilter.accept(definition);
		else return true;
	}
}
