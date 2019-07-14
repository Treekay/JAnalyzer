package nameTable.nameScope;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.IllegalNameDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.visitor.NameTableVisitor;
import sourceCodeAST.SourceCodeLocation;

/**
 * The class represents a local scope
 * 
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2015/11/5
 * 		Refactor the class according to the design document
 */
public class LocalScope implements NameScope {
	private SourceCodeLocation startLocation = null;				// The start position of the scope
	private SourceCodeLocation endLocation = null;					// The end position of the scope
	private List<VariableDefinition> variableList = null;		// The variables defined in the scope
	private List<DetailedTypeDefinition> localTypeList = null;	// The local types defined in the scope
	private List<LocalScope> subLocalscopeList = null;				// The sub-scopes enclosed in the scope
	private NameScope enclosingScope = null;				// The scope enclosed this scope

	private List<NameReference> referenceList = null;			// The references occur in the scope
	
	public LocalScope(NameScope parentScope, SourceCodeLocation start, SourceCodeLocation end) {
		this.startLocation = start;
		this.endLocation = end;
		this.enclosingScope = parentScope;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#define(nameTable.NameDefinition)
	 */
	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		NameDefinitionKind nameKind = nameDef.getDefinitionKind();
		if (nameKind == NameDefinitionKind.NDK_TYPE) {
			if (localTypeList == null) localTypeList = new ArrayList<DetailedTypeDefinition>();
			localTypeList.add((DetailedTypeDefinition) nameDef);
		} else if (nameKind == NameDefinitionKind.NDK_VARIABLE) {
			if (variableList == null) variableList = new ArrayList<VariableDefinition>();
			variableList.add((VariableDefinition) nameDef);
		} else throw new IllegalNameDefinition("The name defined in a local scope must be a local type or a variable!");
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getEnclosingScope()
	 */
	@Override
	public NameScope getEnclosingScope() {
		return enclosingScope;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getNameScopeKind()
	 */
	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_LOCAL;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getScopeName()
	 */
	@Override
	public String getScopeName() {
		return "<Block>@" + startLocation.getUniqueId();
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getSubScopeList()
	 */
	@Override
	public List<NameScope> getSubScopeList() {
		if (localTypeList == null && subLocalscopeList == null) return null;
		List<NameScope> result = new ArrayList<NameScope>();
		if (localTypeList != null) {
			for (DetailedTypeDefinition type : localTypeList) result.add(type);
		}
		if (subLocalscopeList != null) {
			for (LocalScope scope : subLocalscopeList) result.add(scope);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#resolve(nameTable.NameReference)
	 */
	@Override
	public boolean resolve(NameReference reference) {
		if (reference.getReferenceKind() == NameReferenceKind.NRK_VARIABLE) {
			if (variableList != null) {
				for (VariableDefinition var : variableList) {
					if (var.match(reference)) return true;
				}
			}
		} else if (reference.getReferenceKind() == NameReferenceKind.NRK_TYPE) {
			if (localTypeList != null) {
				for (TypeDefinition type : localTypeList) {
					if (type.match(reference)) return true;
				}
			}
		}
		return getEnclosingScope().resolve(reference);
	}

	/**
	 * @return the start
	 */
	public SourceCodeLocation getScopeStart() {
		return startLocation;
	}

	/**
	 * @return the end
	 */
	public SourceCodeLocation getScopeEnd() {
		return endLocation;
	}

	/**
	 * @return the variables
	 */
	public List<VariableDefinition> getVariableList() {
		return variableList;
	}

	/**
	 * @return the localTypes
	 */
	public List<DetailedTypeDefinition> getLocalTypeList() {
		return localTypeList;
	}

	/**
	 * @return the subscopes
	 */
	public List<LocalScope> getSubLocalScope() {
		return subLocalscopeList;
	}

	/**
	 * Add a sub-scope to the local scope
	 */
	public void addSubLocalScope(LocalScope scope) {
		if (subLocalscopeList == null) subLocalscopeList = new ArrayList<LocalScope>();
		subLocalscopeList.add(scope);
	}
	
	/**
	 * Get all local variable definition in this local scope and its sub scopes
	 */
	public List<VariableDefinition> getAllLocalVaraibleDefinitions() {
		List<VariableDefinition> result = new ArrayList<VariableDefinition>();
		if (variableList != null) result.addAll(variableList);
		if (subLocalscopeList != null) {
			for (LocalScope subscope : subLocalscopeList) {
				List<VariableDefinition> subResult = subscope.getAllLocalVaraibleDefinitions();
				result.addAll(subResult);
			}
		}
		return result;
	}

	@Override
	public void addReference(NameReference reference) {
		if (reference == null) return;
		if (referenceList == null) referenceList = new ArrayList<NameReference>();
		referenceList.add(reference);
		
	}

	@Override
	public boolean isEnclosedInScope(NameScope ancestorScope) {
		NameScope parent = getEnclosingScope();
		while (parent != null) {
			if (parent == ancestorScope) return true;
			parent = parent.getEnclosingScope();
		}
		return false;
	}

	@Override
	public boolean containsLocation(SourceCodeLocation location) {
		return location.isBetween(startLocation, endLocation);
	}

	@Override
	/**
	 * Accept a visitor to visit the current scope
	 */
	public void accept(NameTableVisitor visitor) {
		visitor.preVisit(this);
		
		boolean visitSubscope = visitor.visit(this);
		if (visitSubscope == true && localTypeList != null) {
			for (DetailedTypeDefinition type : localTypeList) type.accept(visitor);
		}
		if (visitSubscope == true && subLocalscopeList != null) {
			for (LocalScope scope : subLocalscopeList) scope.accept(visitor);
		}
		
		visitor.endVisit(this);
		visitor.postVisit(this);
	}
	
	@Override
	public List<NameReference> getReferenceList() {
		return referenceList;
	}
	
}
