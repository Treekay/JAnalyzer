package graph.cfg.creator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

/**
 * <p>The factory class for statement CFG creator. The object of this class can find suitable statement CFG creator for 
 * a statement AST node
 * <p>Important note: we will reuse the CFG creator for the same statements, so all CFG creator should have not internal
 * state, or the implement of its method create() should not depend no its internal state.
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 */
public class StatementCFGCreatorFactory {
	private static BlockCFGCreator blockCreator = new BlockCFGCreator();
	private static BreakCFGCreator breakCreator = new BreakCFGCreator();
	private static ContinueCFGCreator continueCreator = new ContinueCFGCreator();
	private static DoWhileCFGCreator doCreator = new DoWhileCFGCreator();
	private static EnhancedForCFGCreator enForCreator = new EnhancedForCFGCreator();
	private static ForCFGCreator forCreator = new ForCFGCreator();
	private static IfCFGCreator ifCreator = new IfCFGCreator();
	private static LabelCFGCreator labelCreator = new LabelCFGCreator();
	private static ReturnCFGCreator returnCreator = new ReturnCFGCreator();
	private static SimpleStatementCFGCreator simpleCreator = new SimpleStatementCFGCreator();
	private static SwitchCFGCreator switchCreator = new SwitchCFGCreator();
	private static SynchronizeCFGCreator synCreator = new SynchronizeCFGCreator();
	private static ThrowCFGCreator throwCreator = new ThrowCFGCreator();
	private static TryCFGCreator tryCreator = new TryCFGCreator();
	private static WhileCFGCreator whileCreator = new WhileCFGCreator();

	// The table to match StatementCFGCreator objects and statement types
	private static StatementCFGCreatorMatch[] table = {
			new StatementCFGCreatorMatch(blockCreator, ASTNode.BLOCK),
			new StatementCFGCreatorMatch(breakCreator, ASTNode.BREAK_STATEMENT),
			new StatementCFGCreatorMatch(continueCreator, ASTNode.CONTINUE_STATEMENT),
			new StatementCFGCreatorMatch(doCreator, ASTNode.DO_STATEMENT),
			new StatementCFGCreatorMatch(enForCreator, ASTNode.ENHANCED_FOR_STATEMENT),
			new StatementCFGCreatorMatch(forCreator, ASTNode.FOR_STATEMENT),
			new StatementCFGCreatorMatch(ifCreator, ASTNode.IF_STATEMENT),
			new StatementCFGCreatorMatch(labelCreator, ASTNode.LABELED_STATEMENT),
			new StatementCFGCreatorMatch(returnCreator, ASTNode.RETURN_STATEMENT),
			new StatementCFGCreatorMatch(simpleCreator, ASTNode.EMPTY_STATEMENT),
			new StatementCFGCreatorMatch(simpleCreator, ASTNode.ASSERT_STATEMENT),
			new StatementCFGCreatorMatch(simpleCreator, ASTNode.CONSTRUCTOR_INVOCATION),
			new StatementCFGCreatorMatch(simpleCreator, ASTNode.EXPRESSION_STATEMENT),
			new StatementCFGCreatorMatch(simpleCreator, ASTNode.SUPER_CONSTRUCTOR_INVOCATION),
			new StatementCFGCreatorMatch(simpleCreator, ASTNode.TYPE_DECLARATION_STATEMENT),
			new StatementCFGCreatorMatch(simpleCreator, ASTNode.VARIABLE_DECLARATION_STATEMENT),
			new StatementCFGCreatorMatch(switchCreator, ASTNode.SWITCH_STATEMENT),
			new StatementCFGCreatorMatch(synCreator, ASTNode.SYNCHRONIZED_STATEMENT),
			new StatementCFGCreatorMatch(throwCreator, ASTNode.THROW_STATEMENT),
			new StatementCFGCreatorMatch(tryCreator, ASTNode.TRY_STATEMENT),
			new StatementCFGCreatorMatch(whileCreator, ASTNode.WHILE_STATEMENT),
	};
	
	/**
	 * This method use a table to match StatementCFGCreator objects and statement types. By using this table, this method find the suitable 
	 * statement CFG creator for the statement astNode.
	 * @param astNode : The statement AST node for creating CFG
	 * @return The suitable StatementCFGCreator object for creating CFG for the statement.
	 */
	public static StatementCFGCreator getCreator(Statement astNode) {
		
		StatementCFGCreator creator = null;
		for (StatementCFGCreatorMatch match : table) {
			if (astNode.getNodeType() == match.getStatementType()) {
				creator = match.getCreator();
				break;
			}
		}
		if (creator == null) {
			throw new AssertionError("Meet unexpected statement type for finding StatementCFGCreator: " + astNode);
		}
		return creator;
	}
	
	public static BlockCFGCreator getCreator(Block astNode) {
		return blockCreator;
	}
}

/**
 * An internal class for matching creator and statement type.
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 *
 */
final class StatementCFGCreatorMatch {
	private StatementCFGCreator creator;
	private int statementType;				// Use the constants defined in ASTNode. ASTNode.getNodeType() can return the type of a AST node.

	public StatementCFGCreatorMatch(StatementCFGCreator creator, int statementType) {
		this.creator = creator;
		this.statementType = statementType;
	}

	public StatementCFGCreator getCreator() {
		return creator;
	}

	public int getStatementType() {
		return statementType;
	}
}



