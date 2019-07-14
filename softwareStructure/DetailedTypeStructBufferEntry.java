package softwareStructure;

import java.util.TreeSet;

import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ8ÈÕ
 * @version 1.0
 */
class DetailedTypeStructBufferEntry extends DetailedTypeStructEntry{
	protected int usedTypeSetSize = 0;
	protected long startPosition = 0;
	
	public DetailedTypeStructBufferEntry(DetailedTypeDefinition type) {
		super(type);
	}
	
	public DetailedTypeStructBufferEntry(DetailedTypeStructEntry other) {
		super(other);
	}

	public DetailedTypeStructBufferEntry(DetailedTypeStructBufferEntry other) {
		super(other);
		usedTypeSetSize = other.usedTypeSetSize;
		startPosition = other.startPosition;
	}

	public int getUsedTypeSetSize() {
		return usedTypeSetSize;
	}

	public void setUsedTypeSetSize(int usedTypeSetSize) {
		this.usedTypeSetSize = usedTypeSetSize;
	}

	public long getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(long startPosition) {
		this.startPosition = startPosition;
	}

	public void setUsedTypeSet(TreeSet<DetailedTypeDefinition> usedTypeSet) {
		this.usedTypeSet = usedTypeSet;
	}

	public TreeSet<DetailedTypeDefinition> getUsedTypeSet() {
		return usedTypeSet;
	}
	
}
