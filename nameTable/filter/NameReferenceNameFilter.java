package nameTable.filter;

import nameTable.nameReference.NameReference;

/**
 * The filter accepts name references with the given name
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ13ÈÕ
 * @version 1.0
 *
 */
public class NameReferenceNameFilter extends NameTableFilter {

	private NameTableFilter wrappedFilter = null;
	private String acceptableName = null;
	
	public NameReferenceNameFilter(NameTableFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameReferenceNameFilter(NameTableFilter wrappedFilter, String acceptableName) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableName = acceptableName;
	}

	public NameReferenceNameFilter(String acceptableName) {
		this.acceptableName = acceptableName;
	}

	public void setAccpetableName(String name) {
		this.acceptableName = name;
	}
	
	@Override
	public boolean accept(NameReference reference) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(reference);
		
		if (acceptable && acceptableName != null) {
			acceptable = acceptableName.equals(reference.getName());
		}
		return acceptable;
	}
}
