package sourceCodeAST;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * An object of SourceCodeFile hold the information about a Java source code file content and its AST root node. 
 * @author Zhou Xiaocong
 * @since 2016年9月28日
 * @version 1.0
 *
 */
public class SourceCodeFile {
	private File fileHandle = null;					// Handle of the source code file
	// The unit name of the source code file, which is related to the root path of the whole source code file set
	private String unitName = null;					
	
	private String fileContent = null;

	private CompilationUnit rootASTNode = null;
	private String parsingErrorMessage = null;
	private boolean hasParsingError = false;
	
	private int totalLines = 0; 		// The line number of the file;
	private long totalSpaces = 0;		// The total spaces of the file  

	public SourceCodeFile(File handle) {
		this.fileHandle = handle;
	}
	
	public File getFileHandle() {
		return fileHandle;
	}
	
	/**
	 * We use three names of a Java source code file, full name, unit name and simple name. The full name 
	 * of a file includes all paths, usually starting with a symbol of hard-disk (e.g. C:\). The unit name of a 
	 * file is related to the start path of the source code file set it belong to. The unit name + the start path
	 * equal to the full name of the file. The simple name of a file is the name which does not include any path.
	 * 
	 * <p>Every source code file can be distinguished by its unit name in a source code file set. Of course, they can 
	 * be distinguished by their full names too.   
	 */
	public String getFileFullName() {
		return fileHandle.getPath();
	}
	
	void setFileUnitName(String unitName) {
		this.unitName = unitName;
	}
	
	public String getFileUnitName() {
		return unitName;
	}
	
	public String getFileContent() {
		if (fileContent == null) loadContent();
		return fileContent;
	}
	
	/**
	 * Check if the AST of the source code file has been created successfully. If the current object does not 
	 * hold the AST root node (i.e. rootASTNode == null), it will try to create AST for this file automatically.
	 * <p>If created AST with no parse error, then return true, otherwise return false.  
	 */
	public boolean hasCreatedAST() {
		if (hasParsingError) return false;
		if (rootASTNode == null) createAST();
		if (rootASTNode == null) {
			hasParsingError = true;
			parsingErrorMessage = "UNKNOWN ERROR: Can not create AST for " + fileHandle.getPath() + "!";
			return false;
		}
		
		StringBuilder msg = null;
		IProblem[] errors = rootASTNode.getProblems();
		if (errors != null && errors.length > 0) {
			msg = new StringBuilder(); 
			for (int i=0; i < errors.length; ++i) {
				IProblem problem = errors[i];
				if (problem.isError()) {
					hasParsingError = true;
					String message = "Line " + problem.getSourceLineNumber() + ": " + problem.getMessage();	
					msg.append(message);
					msg.append("\r\n\t\t");
				} // Ignore all warnings!
			}
		}
		if (msg != null) parsingErrorMessage = msg.toString();
		else parsingErrorMessage = null;
		
		if (hasParsingError) return false;
		else return true;
	}

	/**
	 * @pre-condition The client must have called hasCreatedAST()!
	 */
	public CompilationUnit getASTRoot() {
		return rootASTNode;
	}

	/**
	 * pre-condition: The client must have called hasCreatedAST()!
	 */
	public String getParsingErrorMessage() {
		return parsingErrorMessage;
	}
	
	public int getTotalLines() {
		if (totalLines == 0) loadContent();
		return totalLines;
	}
	
	public long getTotalSpaces() {
		if (totalSpaces == 0) loadContent();
		return totalSpaces;
	}

	/**
	 * Set file content to null for release the memory occupied by it, since file content may use many memories. 
	 */
	public void releaseFileContent() {
		fileContent = null;
	}
	
	/**
	 * Set AST root to null for release the memory occupied by it, since AST root may use many memories. 
	 */
	public void releaseAST() {
		rootASTNode = null;
		parsingErrorMessage = null;
		hasParsingError = false;		
	}
	

	private void loadContent() {
		if (fileHandle == null) return;
		
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(fileHandle));
			String line = reader.readLine();
			StringBuffer buffer = new StringBuffer(); 
			while (line != null) {
				buffer.append(line + "\n");
				totalLines = totalLines + 1;
				line = reader.readLine();
			}
			reader.close(); 
			fileContent = buffer.toString();
			totalSpaces = fileHandle.getTotalSpace();
		} catch (IOException exc) {
			fileContent = null;
		}
	}
	
	@SuppressWarnings({"rawtypes", "unchecked" })
	private void createAST() {
		if (fileHandle == null) return;
		if (fileContent == null) loadContent();
		if (fileContent == null) return;
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		// For parsing the source code in Java 1.8, the compile options must be set!
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		parser.setCompilerOptions(options);

		parser.setSource(fileContent.toCharArray());
		parsingErrorMessage = null;
		hasParsingError = false;
		rootASTNode = (CompilationUnit) parser.createAST(null);
	}
}
