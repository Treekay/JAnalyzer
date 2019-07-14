package nameTable.filter;

import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;

/**
 * A name definition filter accepted the definition with the given name definition kind!
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ9ÈÕ
 * @version 1.0
 *
 */
public class NameDefinitionKindFilter extends NameTableFilter {

	private NameTableFilter wrappedFilter = null;
	private NameDefinitionKind acceptableKind = null;
	
	public NameDefinitionKindFilter (NameTableFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameDefinitionKindFilter (NameDefinitionKind acceptableKind) {
		this.acceptableKind = acceptableKind;
	}

	public NameDefinitionKindFilter (NameTableFilter wrappedFilter, NameDefinitionKind acceptableKind) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableKind = acceptableKind;
	}

	public void setAccpetableKind(NameDefinitionKind kind) {
		this.acceptableKind = kind;
	}
	
	@Override
	public boolean accept(NameDefinition definition) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(definition);
		
		if (acceptable && acceptableKind != null) {
			acceptable = (acceptableKind == definition.getDefinitionKind());
		}
		return acceptable;
	}
}
