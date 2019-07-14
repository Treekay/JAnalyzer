package nameTable.creator;

import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * A data class to store unit file name, its AST root, and error message of the unit together.
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ10ÈÕ
 * @version 1.0
 *
 */
public class CompilationUnitFile {
	String unitName = null;
	CompilationUnit root = null;
	String errorMessage = null;
	
	CompilationUnitFile(String unitName, CompilationUnit root) {
		this.unitName = unitName;
		this.root = root;
	}
	
	void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getUnitName() {
		return unitName;
	}
	
	public CompilationUnit getASTRoot() {
		return root;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
