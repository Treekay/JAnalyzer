package graph.cfg;

/**
 * This class collects all label strings for the execution point
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 *
 */
public class ExecutionPointLabel {
	public static final String ASSERTION = "Assertion";
	public static final String CONSTRUCTOR_INVOCATION = "ConstructorInvocation";
	public static final String SUPER_CONSTRUCTOR_INVOCATION = "SuperConstructorInvocation";
	public static final String TYPE_DECLARATION = "TypeDeclaration";
	public static final String VARIABLE_DECLARATION = "VariableDelcaration";
	public static final String EXPRESSION = "Expression";
	
	public static final String ENHANCED_FOR_PREDICATE = "EnhancedForPredicate";
	public static final String IF_PREDICATE = "IfPredicate";
	public static final String SWITCH_PREDICATE = "SwitchPredicate";
	public static final String DO_WHILE_PREDICATE = "DoWhilePredicate";
	public static final String WHILE_PREDICATE = "WhilePredicate";
	public static final String FOR_PREDICATE = "ForPredicate";

	public static final String DO_START = "DoStart";
	public static final String TRY_BLOCK_START = "TryBlockStart";
	public static final String SYNCHRONIZE_START = "Synchronize";
	public static final String CATCH_CLAUSE_START = "CatchClauseStart";

	public static final String CATCH_CLAUSE = "CatchClause";
	public static final String FINALLY_BLOCK = "FinallyBlock";
	
	/**
	 * The label of the start node of the labeled statement is "Label:" + the label in the statement
	 */
	public static final String LABEL_START = "Label:";
	
	public static final String FINALLY_START = "FinallyStart";
	
	public static final String IF_END = "IfEnd";
	public static final String SWITCH_END = "SwitchEnd";
	public static final String DO_END = "DoEnd";
	public static final String WHILE_END = "WhileEnd";
	public static final String FOR_END = "ForEnd";
	public static final String ENHANCED_FOR_END = "EnhancedForEnd";
	public static final String TRY_BLOCK_END = "TryBlockEnd";
	public static final String TRY_END = "TryEnd";
	public static final String SYNCHRONIZE_END = "SynchronizeEnd";
	public static final String LABEL_END = "LabelEnd";
	public static final String CATCH_CLAUSE_END = "CatchClauseEnd";
	
	/**
	 * The label of the break node is "break " + the label in the statement
	 */
	public static final String BREAK_LABEL = "break ";
	/**
	 * The label of the break node is "break " + the label in the statement
	 */
	public static final String CONTINUE_LABEL = "continue ";
	
	public static final String RETURN_LABEL = "return";
	public static final String THROW_LABEL = "throw";
	
	public static final String START = "Start";
	public static final String END = "End";
	public static final String ABNORMAL_END = "AbnormalEnd";
	
	public static final String ID_SEPERATOR = ":";
}
