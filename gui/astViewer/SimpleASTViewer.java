package gui.astViewer;

import java.util.Map;

import javax.swing.JFrame;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
//import org.eclipse.swt.custom.StyleRange;

public class SimpleASTViewer {
	
	protected String astViewerText = null;
	protected String sourceCode = null;
	protected CompilationUnit rootNode = null;
	
	private JFrame parent = null;
	
	private boolean hasParserError = false;
	
	public SimpleASTViewer(JFrame parent, String sourceCode) {
		this.parent = parent;
		this.sourceCode = sourceCode;
	}
	
	@SuppressWarnings("unchecked")
	public void parseSourceCode() {
		if (sourceCode == null) return;
		
		SimpleProgressMonitor pm = new SimpleProgressMonitor(parent);
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
		parser.setCompilerOptions(options);

		parser.setSource(sourceCode.toCharArray());
		rootNode = (CompilationUnit) parser.createAST(pm);
		visitASTTree();
	}
	 
	public String getParseErrorMessage() {
		StringBuilder msg = null;
		if (rootNode == null) return null;
		
		IProblem[] errors = rootNode.getProblems();
		if (errors != null && errors.length > 0) {
			msg = new StringBuilder(); 
			for (int i=0; i < errors.length; ++i) {
				IProblem problem = errors[i];
				if (problem.isError()) hasParserError = true;
				String message = problem.getMessage() + " line: " + problem.getSourceLineNumber();	
				msg.append(message);
				msg.append("\n");
			}
		}
		if (msg != null) return msg.toString();
		else return null;
	}
	
	public boolean hasParserError() {
		return hasParserError;
	}
	
	public void visitASTTree() {
		StringBuffer buffer = new StringBuffer();
		SimpleASTVisitor astVistor = new SimpleASTVisitor(buffer, rootNode);
		rootNode.accept(astVistor);
		
		astViewerText = buffer.toString();
	}
	
	public String getASTViewerText() {
		return astViewerText;
	}
	
	public CompilationUnit getASTRoot() {
		return rootNode;
	}
}
