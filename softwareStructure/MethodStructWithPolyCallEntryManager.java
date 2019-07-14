package softwareStructure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import util.Debug;
import nameTable.nameDefinition.MethodDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ11ÈÕ
 * @version 1.0
 */
public class MethodStructWithPolyCallEntryManager extends MethodStructEntryManager {
	protected RandomAccessFile buffer = null;
	protected long currentPosition = 0;
	protected MethodStructWithPolyCallEntry[] entryBuffer = null;
	protected int currentBufferIndex = 0;
	protected int currentBufferLength = 0;
	
	public MethodStructWithPolyCallEntryManager() {
	}

	public void initialize(String systemPath) {
		structMap = new TreeMap<String, MethodStructEntry>();
		this.systemPath = systemPath;
		try {
			buffer = new RandomAccessFile(systemPath + ".polycall", "rw");
		} catch (FileNotFoundException e) {
			throw new AssertionError("Can not open method indirect polymorphic calling external buffer!");
		}
		entryBuffer = new MethodStructWithPolyCallEntry[10];
	}
	
	public void clear() {
		structMap = null;
		entryBuffer = null;
		try {
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void put(MethodDefinition method, MethodStructEntry entry) {
		MethodStructWithPolyCallEntry bufferEntry = new MethodStructWithPolyCallEntry(entry);
		structMap.put(method.getUniqueId(), bufferEntry);
	}

	@Override
	public MethodStructWithPolyCallEntry get(MethodDefinition method) {
		MethodStructWithPolyCallEntry entry = (MethodStructWithPolyCallEntry)structMap.get(method.getUniqueId());
		return entry;
	}
	
	public void createPolyCallSetForAllMethod() {
		Set<String> methodIdSet = structMap.keySet();
		
		Debug.time("Begin generate polymorphic calling information....");
		for (String methodId : methodIdSet) {
			Debug.println("\tMethod " + methodId);
			MethodStructWithPolyCallEntry entry = (MethodStructWithPolyCallEntry)structMap.get(methodId);
			if (!entry.hasIndirectPolyCallInformation()) {
				TreeSet<MethodDefinition> polyCalledMethodSet = createPolyCallSet(entry);
				entry.setPolyCalledMethodSet(polyCalledMethodSet);
				putWithPolyCallInformatoin(methodId, entry);
			}
		}
		Debug.time("End generate polymorphic calling information....");
	}
	
	public boolean hasIndirectPolyCallInformation() {
		return true;
	}
	
	public TreeSet<MethodDefinition> getPolymorphicInvocationMethodSet(MethodDefinition method) {
		MethodStructWithPolyCallEntry entry = getWithPolyCallInformation(method);
		return entry.getPolyCalledMethodSet();
	}

	private TreeSet<MethodDefinition> createPolyCallSet(MethodStructWithPolyCallEntry entry) {
		TreeSet<MethodDefinition> resultSet = new TreeSet<MethodDefinition>();
		TreeMap<MethodDefinition, MethodCallInformation> calledMapInEntry = entry.getCalledMethodMap();

		// Use a queue to get all methods which are directly or indirectly called by the given method
		// Note that we can NOT recursively get all methods which are directly or indirectly called by 
		// the given method, because this may result a forever loop when the method direct or indirect 
		// called the method itself (i.e. there are direct or indirect recursive calls in this method)
		// In fact, here we use a queue to search the indirect polymorphic call relations in a width-first
		// way, which should not get into a forever loop. But if we search such relations in a depth-first
		// way, we may get into a forever loop trap!
		LinkedList<MethodDefinition> calleeQueue = new LinkedList<MethodDefinition>();
		
		Set<MethodDefinition> directCalleeSet = calledMapInEntry.keySet();
		for (MethodDefinition method : directCalleeSet) {
			resultSet.add(method);
			calleeQueue.add(method);
		}
		
		while (!calleeQueue.isEmpty()) {
			MethodDefinition calleeMethod = calleeQueue.removeFirst();
			MethodStructWithPolyCallEntry calleeEntry = (MethodStructWithPolyCallEntry)structMap.get(calleeMethod.getUniqueId());
			if (calleeEntry.hasIndirectPolyCallInformation()) {
				// This callee has generated indirect polymorphic called method set, add all methods in this 
				// set to result set. Note that those methods need not to add to calleeQueue since this set 
				// include all direct or indirect methods called polymorphically by the callee.
				calleeEntry = getWithPolyCallInformation(calleeMethod);
				directCalleeSet = calleeEntry.getPolyCalledMethodSet();
				for (MethodDefinition callee : directCalleeSet) {
					resultSet.add(callee);
				}
			} else {
				directCalleeSet = calleeEntry.getCalledMethodMap().keySet();
				for (MethodDefinition callee : directCalleeSet) {
					if (!resultSet.contains(callee)) {
						resultSet.add(callee);
						calleeQueue.addLast(callee);
					}
				}
			}
		}
		
		return resultSet;
	}
	
	
	private void putWithPolyCallInformatoin(String methodId, MethodStructWithPolyCallEntry entry) {
		if (currentBufferLength < entryBuffer.length) {
			entryBuffer[currentBufferLength] = new MethodStructWithPolyCallEntry(entry);
			currentBufferLength++;
		}
		MethodStructWithPolyCallEntry bufferEntry = new MethodStructWithPolyCallEntry(entry);
		writePolyCallSetToBufferFile(bufferEntry);
		structMap.put(methodId, bufferEntry);
	}

	private MethodStructWithPolyCallEntry getWithPolyCallInformation(MethodDefinition method) {
		for (int i = 0; i < currentBufferLength; i++) {
			if (entryBuffer[i].getMethod() == method) return entryBuffer[i];
		}
		MethodStructWithPolyCallEntry entryInMap = (MethodStructWithPolyCallEntry)structMap.get(method.getUniqueId());
		MethodStructWithPolyCallEntry entry = new MethodStructWithPolyCallEntry(entryInMap);
		TreeSet<MethodDefinition> polyCallSet = readPolyCallSetFromBufferFile(entry);
		entry.setPolyCalledMethodSet(polyCallSet);
		
		entryBuffer[currentBufferIndex] = entry;
		if (++currentBufferIndex >= currentBufferLength) currentBufferIndex = 0;
		
//		int bufferIndex = 0;
//		for (int i = 0; i < currentBufferLength; i++) {
//			if (entryBuffer[i].getCalledMethodMapSize() < entryBuffer[bufferIndex].getCalledMethodMapSize()) bufferIndex = i; 
//		}
//		entryBuffer[bufferIndex] = entry;
		return entry;
	}
	
	private void writePolyCallSetToBufferFile(MethodStructWithPolyCallEntry entry) {
		TreeSet<MethodDefinition> polyCallSet = entry.getPolyCalledMethodSet();
		int size = 0;
		if (polyCallSet != null) size = polyCallSet.size();
		entry.setStartPosition(currentPosition);
		entry.setPolyCalledMethodSet(null);
		entry.setPolyCallSetSize(size);
		
		if (size > 0) {
			try {
				buffer.seek(currentPosition);
				for (MethodDefinition callee : polyCallSet) {
					buffer.writeUTF(callee.getUniqueId());
				}
				currentPosition = buffer.getFilePointer();
			} catch (IOException e) {
				throw new AssertionError("Can not write method indirect polymrophic calling external buffer!");
			}
		}
	}

	private TreeSet<MethodDefinition> readPolyCallSetFromBufferFile(MethodStructWithPolyCallEntry entry) {
		TreeSet<MethodDefinition> polyCallSet = new TreeSet<MethodDefinition>();
		
		long startPosition = entry.getStartPosition();
		int size = entry.getPolyCallSetSize();
		
		if (size > 0) {
			try {
				buffer.seek(startPosition);
				for (int i = 0; i < size; i++) {
					String methodId = buffer.readUTF();

					MethodStructEntry calleeEntry = structMap.get(methodId);
					MethodDefinition callee = calleeEntry.getMethod();
					polyCallSet.add(callee);
				}
			} catch (IOException e) {
				throw new AssertionError("Can not Read method indirect polymorhic calling external buffer!");
			}
		}
		return polyCallSet;
	}
}
