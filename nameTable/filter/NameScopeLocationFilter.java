package nameTable.filter;

import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A filter accepted the scope contained the given location, or equal to / greater than start and less than end (if provided 
 * two locations)!
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ9ÈÕ
 * @version 1.0
 *
 */
public class NameScopeLocationFilter extends NameScopeFilter {
	private NameScopeFilter wrappedFilter = null;
	private SourceCodeLocation acceptableLocation = null;
	private SourceCodeLocation acceptableEndLocation = null;
	
	public NameScopeLocationFilter(NameScopeFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameScopeLocationFilter(SourceCodeLocation acceptableLocation) {
		this.acceptableLocation = acceptableLocation;
	}

	public NameScopeLocationFilter(SourceCodeLocation acceptableLocation, SourceCodeLocation acceptableEndLocation) {
		this.acceptableLocation = acceptableLocation;
		this.acceptableEndLocation = acceptableEndLocation;
	}

	public NameScopeLocationFilter(NameScopeFilter wrappedFilter, SourceCodeLocation acceptableLocation) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableLocation = acceptableLocation;
	}

	public NameScopeLocationFilter(NameScopeFilter wrappedFilter, SourceCodeLocation acceptableLocation, SourceCodeLocation acceptableEndLocation) {
		this.wrappedFilter = wrappedFilter;
		this.acceptableLocation = acceptableLocation;
		this.acceptableEndLocation = acceptableEndLocation;
	}

	public void setAccpetableLocation(SourceCodeLocation location) {
		this.acceptableLocation = location;
	}
	
	public void setAccpetableEndLocation(SourceCodeLocation location) {
		this.acceptableEndLocation = location;
	}

	@Override
	public boolean accept(NameScope scope) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(scope);
		
		if (acceptable && acceptableLocation != null) {
			if (acceptableEndLocation != null) {
				SourceCodeLocation start = scope.getScopeStart();
				SourceCodeLocation end = scope.getScopeEnd();
				if (start != null) acceptable = acceptable && start.equals(acceptableLocation);
				if (acceptable && end != null) acceptable = end.equals(acceptableEndLocation);
			} else acceptable = scope.containsLocation(acceptableLocation);
		}
		return acceptable;
	}
}
