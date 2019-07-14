package sourceCodeAST;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An iterator for traversing all source code files in a SourceCodeFileSet object. 
 * @author Zhou Xiaocong
 * @since 2016Äê9ÔÂ28ÈÕ
 * @version 1.0
 *
 */
public class SourceCodeFileSetIterator implements Iterator<SourceCodeFile> {

	private Map<String, SourceCodeFile> fileMap = null;
	private Set<String> fileUnitNameSet = null;
	private String currentFileUnitName = null;
	private Iterator<String> setIterator = null;

	SourceCodeFileSetIterator(SourceCodeFileSet fileSet) {
		fileMap = fileSet.getSourceCodeFileMap();
		this.fileUnitNameSet = fileMap.keySet();
		setIterator = fileUnitNameSet.iterator();
	}

	public boolean hasNext() {
		return setIterator.hasNext();
	}

	public SourceCodeFile next() {
		currentFileUnitName = setIterator.next();
		return fileMap.get(currentFileUnitName);
	}

	public String getCurrentFileUnitName() {
		return currentFileUnitName;
	}
}
