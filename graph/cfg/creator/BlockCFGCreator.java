package graph.cfg.creator;

import graph.cfg.ControlFlowGraph;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Create CFG for Block statement
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 *
 */
public class BlockCFGCreator extends StatementCFGCreator {

	@SuppressWarnings("unchecked")
	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodeLabel) {
		
		Block block = (Block)astNode;
		// Get the statements in the block. The element type of the list returned by Block.statements() should be Statement
		List<Statement> statements = block.statements();

		if (statements == null) return precedeNodeList;
		
		// Create CFG for the statements in the block
		for (Statement statement : statements) {
			StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(statement);
			precedeNodeList = creator.create(currentCFG, statement, precedeNodeList, null);
		}
		
		return precedeNodeList;
	}

}
