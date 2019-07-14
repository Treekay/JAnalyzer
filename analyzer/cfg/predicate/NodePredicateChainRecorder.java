package analyzer.cfg.predicate;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ10ÈÕ
 * @version 1.0
 *
 */
public class NodePredicateChainRecorder implements INodePredicateChainRecorder {
	protected NodePredicateRecorder truePredicate = null;
	protected NodePredicateRecorder falsePredicate = null;
	protected NodePredicateListChain chain = null;
	
	public NodePredicateRecorder getTruePredicate() {
		return truePredicate;
	}
	public void setTruePredicate(NodePredicateRecorder truePredicate) {
		this.truePredicate = truePredicate;
	}
	public NodePredicateRecorder getFalsePredicate() {
		return falsePredicate;
	}
	public void setFalsePredicate(NodePredicateRecorder falsePredicate) {
		this.falsePredicate = falsePredicate;
	}
	public NodePredicateListChain getPredicateChain() {
		return chain;
	}
	public void setPredicateChain(NodePredicateListChain chain) {
		this.chain = chain;
	}
}
