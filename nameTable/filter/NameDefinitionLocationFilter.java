package nameTable.filter;

import nameTable.nameDefinition.NameDefinition;
import sourceCodeAST.SourceCodeLocation;

/**
 * A filter accepted the definition whose location is greater and equal than start location and less than end location
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ9ÈÕ
 * @version 1.0
 *
 */
public class NameDefinitionLocationFilter extends NameTableFilter {

	private NameTableFilter wrappedFilter = null;
	private SourceCodeLocation start = null;
	private SourceCodeLocation end = null;
	
	public NameDefinitionLocationFilter (NameTableFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameDefinitionLocationFilter (SourceCodeLocation start, SourceCodeLocation end) {
		this.start = start;
		this.end = end;
	}

	public NameDefinitionLocationFilter (SourceCodeLocation start) {
		this.start = start;
	}

	public NameDefinitionLocationFilter (NameTableFilter wrappedFilter, SourceCodeLocation start, SourceCodeLocation end) {
		this.wrappedFilter = wrappedFilter;
		this.start = start;
		this.end = end;
	}

	public NameDefinitionLocationFilter (NameTableFilter wrappedFilter, SourceCodeLocation start) {
		this.wrappedFilter = wrappedFilter;
		this.start = start;
	}

	public void setAcceptableLocation(SourceCodeLocation start, SourceCodeLocation end) {
		this.start = start;
		this.end = end;
	}
	
	public void setAcceptableStartLocation(SourceCodeLocation start) {
		this.start = start;
	}

	public void setAcceptableEndLocation(SourceCodeLocation end) {
		this.end = end;
	}

	@Override
	public boolean accept(NameDefinition definition) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(definition);
		
		if (acceptable && start != null) {
			acceptable = (start.compareTo(definition.getLocation()) <= 0);
		}

		if (acceptable && end != null) {
			acceptable = (end.compareTo(definition.getLocation()) > 0);
		}
		return acceptable;
	}
}
