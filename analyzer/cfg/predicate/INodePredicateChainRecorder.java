package analyzer.cfg.predicate;

import graph.cfg.IFlowInfoRecorder;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ10ÈÕ
 * @version 1.0
 *
 */
public interface INodePredicateChainRecorder extends IFlowInfoRecorder {
	
	public NodePredicateRecorder getTruePredicate();
	
	public void setTruePredicate(NodePredicateRecorder truePredicate);
	
	public NodePredicateRecorder getFalsePredicate();
	
	public void setFalsePredicate(NodePredicateRecorder falsePredicate);
	
	public NodePredicateListChain getPredicateChain();
	
	public void setPredicateChain(NodePredicateListChain chain);
}
