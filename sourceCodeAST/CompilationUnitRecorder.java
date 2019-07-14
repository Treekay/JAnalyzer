package sourceCodeAST;

import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * A data class to store unit file name, its AST root, and error message of the unit together.
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ10ÈÕ
 * @version 1.0
 *
 */
public class CompilationUnitRecorder {
	public final String unitName;
	public final CompilationUnit root;
	private String errorMessage = null;
	
	public CompilationUnitRecorder(String unitName, CompilationUnit root) {
		this.unitName = unitName;
		this.root = root;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
