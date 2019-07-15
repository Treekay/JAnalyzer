package sourceCodeAST;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Zhou Xiaocong
 * @since 2017年1月1日
 * @version 1.0
 *
 */
public class SourceCodeLocationASTVisitor extends ASTVisitor {
	private int nodePosition = 0;
	private int nodeLength = 0;
	
	private ASTNode matchedNode = null;
	
	public SourceCodeLocationASTVisitor(CompilationUnit root, SourceCodeLocation start, SourceCodeLocation end) {
		nodePosition = root.getPosition(start.getLineNumber(), start.getColumn());
		nodeLength = root.getPosition(end.getLineNumber(), end.getColumn()) - nodePosition;
	}
	
	public ASTNode getMatchedNode() {
		return matchedNode;
	}
	
	public boolean preVisit2(ASTNode node) {
		if (matchedNode != null) return false;
		
		int position = node.getStartPosition();
		int length = node.getLength();
		
		// So far, we match the AST node according to its position and its length!!!
		if (position == nodePosition && length == nodeLength) matchedNode = node;
		
		if (matchedNode == null) return true;		// Do not match the AST node, we need to visit its children.
		else return false;							// We have matched the AST node, we DO NOT need to visit its children.
	}
}
