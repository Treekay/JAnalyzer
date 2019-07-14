package nameTable.filter;

import nameTable.nameDefinition.NameDefinition;

/**
 * A filter accepted the definition with the given simple name or the given full qualified name
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ9ÈÕ
 * @version 1.0
 *
 */
public class NameDefinitionNameFilter extends NameTableFilter {
	private NameTableFilter wrappedFilter = null;
	private String acceptableName = null;
	private boolean useFullQualifiedName = false;
	
	public NameDefinitionNameFilter(NameTableFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameDefinitionNameFilter(NameTableFilter wrappedFilter, String acceptableName) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableName = acceptableName;
	}

	public NameDefinitionNameFilter(NameTableFilter wrappedFilter, String acceptableName, boolean useFullQualifiedName) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableName = acceptableName;
		this.useFullQualifiedName = useFullQualifiedName;
	}

	public NameDefinitionNameFilter(String acceptableName, boolean useFullQualifiedName) {
		this.acceptableName = acceptableName;
		this.useFullQualifiedName = useFullQualifiedName;
	}

	public NameDefinitionNameFilter(String acceptableName) {
		this.acceptableName = acceptableName;
	}

	public void setAccpetableName(String name) {
		this.acceptableName = name;
	}
	
	public void setUseFullQualifiedName() {
		useFullQualifiedName = true;
	}
	
	public void setUseFullQualifiedName(boolean flag) {
		useFullQualifiedName = flag;
	}
	
	@Override
	public boolean accept(NameDefinition definition) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(definition);
		
		if (acceptable && acceptableName != null) {
			if (useFullQualifiedName == true) {
				acceptable = definition.getFullQualifiedName().endsWith(acceptableName);
			} else {
				acceptable = acceptableName.equals(definition.getSimpleName());
			}
		}
		return acceptable;
	}
}
