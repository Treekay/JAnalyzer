package gui.astViewer;

import graph.cfg.ControlFlowGraph;
import graph.cfg.creator.CFGCreator;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class ControlFlowGraphViewer {
	private CFGCreator creator = null;
	
	private List<ControlFlowGraph> cfgList = null;

	public ControlFlowGraphViewer(String sourceFileName, CompilationUnit root) {
		creator = new CFGCreator(sourceFileName, root);
	}
	
	public String createCFGToText() {
		if (cfgList == null) cfgList = creator.create();
		StringBuffer buffer = new StringBuffer();
		
		for (ControlFlowGraph cfg : cfgList) {
			buffer.append("\n" + cfg.getId() + "[" + cfg.getLabel() + "]\n");
			buffer.append(cfg.toFullString());
		}
		return buffer.toString();
	}
}
