package softwareStructure;

import nameTable.nameDefinition.MethodDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016年4月8日
 * @version 1.0
 */
class MethodStructBufferEntry extends MethodStructEntry {
	protected int calledMethodMapSize = 0;
	protected long startPosition = 0;

	public MethodStructBufferEntry(MethodDefinition method) {
		super(method);
	}

	public MethodStructBufferEntry(MethodStructEntry other) {
		super(other);
	}
	
	public MethodStructBufferEntry(MethodStructBufferEntry other) {
		super(other);
		calledMethodMapSize = other.calledMethodMapSize;
		startPosition = other.startPosition;
	}

	public int getCalledMethodMapSize() {
		return calledMethodMapSize;
	}

	public void setCalledMethodMapSize(int calledMethodMapSize) {
		this.calledMethodMapSize = calledMethodMapSize;
	}

	public long getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(long startPosition) {
		this.startPosition = startPosition;
	}

}
