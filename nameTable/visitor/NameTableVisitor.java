package nameTable.visitor;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.ImportedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.SystemScope;

/**
 * The base class for visiting the name table.
 * 
 * @author Zhou Xiaocong
 * @since 2015Äê6ÔÂ24ÈÕ
 * @version 1.0
 */
public class NameTableVisitor {

	public void preVisit(NameScope scope) {
		return;
	}

	public void postVisit(NameScope scope) {
		return;
	}
	
	public boolean visit(SystemScope scope) {
		return true;
	}
	
	public boolean visit(PackageDefinition scope) {
		return true;
	}
	
	public boolean visit(CompilationUnitScope scope) {
		return true;
	}
	
	public boolean visit(DetailedTypeDefinition scope) {
		return true;
	}
	
	public boolean visit(ImportedTypeDefinition scope) {
		return true;
	}
	
	public boolean visit(EnumTypeDefinition scope) {
		return true;
	}
	
	public boolean visit(MethodDefinition scope) {
		return true;
	}

	public boolean visit(LocalScope scope) {
		return true;
	}

	public void endVisit(SystemScope scope) {
	}
	
	public void endVisit(PackageDefinition scope) {
	}
	
	public void endVisit(CompilationUnitScope scope) {
	}
	
	public void endVisit(DetailedTypeDefinition scope) {
	}
	
	public void endVisit(ImportedTypeDefinition scope) {
	}

	public void endVisit(EnumTypeDefinition scope) {
	}
	
	public void endVisit(MethodDefinition scope) {
	}

	public void endVisit(LocalScope scope) {
	}

}
