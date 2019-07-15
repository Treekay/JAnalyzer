package softwareStructure;

import java.util.TreeSet;

import nameTable.nameDefinition.MethodDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016年4月11日
 * @version 1.0
 */
public class MethodStructWithPolyCallEntry extends MethodStructEntry {
	protected TreeSet<MethodDefinition> polyCalledMethodSet = null;
	protected int size = -1;		// -1 means this entry has not generated indirect polymorphic method calling set 
	protected long position = 0;
	
	public MethodStructWithPolyCallEntry(MethodDefinition method) {
		super(method);
	}

	public MethodStructWithPolyCallEntry(MethodStructEntry other) {
		super(other);
	}

	public MethodStructWithPolyCallEntry(MethodStructWithPolyCallEntry other) {
		super(other);
		polyCalledMethodSet = other.polyCalledMethodSet;
		size = other.size;
		position = other.position;
	}

	public TreeSet<MethodDefinition> getPolyCalledMethodSet() {
		return polyCalledMethodSet;
	}

	public void setPolyCalledMethodSet(TreeSet<MethodDefinition> indirectPolyCalledMethodSet) {
		this.polyCalledMethodSet = indirectPolyCalledMethodSet;
	}

	public int getPolyCallSetSize() {
		return size;
	}

	public void setPolyCallSetSize(int size) {
		this.size = size;
	}

	public long getStartPosition() {
		return position;
	}

	public void setStartPosition(long position) {
		this.position = position;
	}
	
	@Override
	public boolean hasIndirectPolyCallInformation() {
		return (size >= 0);
	}
}
