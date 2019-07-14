package nameTable.filter;

import nameTable.nameDefinition.NameDefinition;

/**
 * A filter accepted the definition with the given unique id
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ9ÈÕ
 * @version 1.0
 *
 */
public class NameDefinitionUniqueIdFilter extends NameTableFilter {

	private NameTableFilter wrappedFilter = null;
	private String acceptableUniqueId = null;
	
	public NameDefinitionUniqueIdFilter(NameTableFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameDefinitionUniqueIdFilter(String acceptableId) {
		this.acceptableUniqueId = acceptableId;
	}

	public NameDefinitionUniqueIdFilter(NameTableFilter wrappedFilter, String acceptableId) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableUniqueId = acceptableId;
	}

	public void setAccpetableId(String id) {
		this.acceptableUniqueId = id;
	}
	
	@Override
	public boolean accept(NameDefinition definition) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(definition);
		
		if (acceptable && acceptableUniqueId != null) {
			acceptable = acceptableUniqueId.equals(definition.getUniqueId());
		}
		return acceptable;
	}
}
