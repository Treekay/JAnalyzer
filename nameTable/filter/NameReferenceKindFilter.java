package nameTable.filter;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;

/**
 * The filter accepts the name reference with the given kind.
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ13ÈÕ
 * @version 1.0
 *
 */
public class NameReferenceKindFilter extends NameTableFilter {

	private NameTableFilter wrappedFilter = null;
	private NameReferenceKind acceptableKind = null;
	
	public NameReferenceKindFilter (NameTableFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameReferenceKindFilter (NameReferenceKind acceptableKind) {
		this.acceptableKind = acceptableKind;
	}

	public NameReferenceKindFilter (NameTableFilter wrappedFilter, NameReferenceKind acceptableKind) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableKind = acceptableKind;
	}

	public void setAccpetableKind(NameReferenceKind kind) {
		this.acceptableKind = kind;
	}
	
	@Override
	public boolean accept(NameReference reference) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(reference);
		
		if (acceptable && acceptableKind != null) {
			acceptable = (acceptableKind == reference.getReferenceKind());
		}
		return acceptable;
	}
}
