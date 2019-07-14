package nameTable.nameScope;

import java.util.List;

import sourceCodeAST.SourceCodeLocation;
import nameTable.nameDefinition.IllegalNameDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.NameReference;
import nameTable.visitor.NameTableVisitor;

/**
 * The interface for name scopes
 * 
 * @author Zhou Xiaocong
 * @since 2013/2/21
 * @version 1.0
 * 
 * @update 2016/11/5
 * 		Refactor the class according to the design document
 */
public interface NameScope {
	
	/**
	 * @return The name of the scope
	 */
	public String getScopeName();
	
	/**
	 * @return The start location of the scope
	 */
	public SourceCodeLocation getScopeStart();
	
	/**
	 * @return The end location of the scope
	 */
	public SourceCodeLocation getScopeEnd();

	/**
	 * @return The enclosing scope (i.e. the parent) of the scope
	 */
	public NameScope getEnclosingScope();
	
	/**
	 * @return The list of scopes contained in the scope, i.e. the children of the scope
	 */
	public List<NameScope> getSubScopeList();
	
	/**
	 * Test if the current scope contains the given location  
	 */
	public boolean containsLocation(SourceCodeLocation location);

	/**
	 * Test if the current scope is enclosed in the given scope  
	 */
	public boolean isEnclosedInScope(NameScope scope);

	/**
	 * Define a name in the scope
	 */
	public void define(NameDefinition nameDef) throws IllegalNameDefinition;
	
	/**
	 * Resolve a name reference in the scope. If it is successful, call reference.bindTo() to bind the reference 
	 *   to its definition.
	 * @return If it is successful, return true, otherwise return false
	 */
	public boolean resolve(NameReference reference);
	
	/**
	 * return the kind of the name scope 
	 */
	public NameScopeKind getScopeKind();
	
	/**
	 * Add a reference to the current scope
	 */
	public void addReference(NameReference reference);

	/**
	 * Get reference list of the scope
	 */
	public List<NameReference> getReferenceList();
	
	/**
	 * For implement the visitor patter for visit the name table
	 */
	public void accept(NameTableVisitor visitor);
}
