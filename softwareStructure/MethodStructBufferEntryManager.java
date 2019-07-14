package softwareStructure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Set;
import java.util.TreeMap;

import nameTable.nameDefinition.MethodDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ7ÈÕ
 * @version 1.0
 */
class MethodStructBufferEntryManager extends MethodStructEntryManager {
	protected RandomAccessFile buffer = null;
	protected long currentPosition = 0;
	protected MethodStructEntry[] entryBuffer = null;
	protected int currentBufferIndex = 0;
	protected int currentBufferLength = 0;
	
	public MethodStructBufferEntryManager() {
	}

	public void initialize(String systemPath) {
		structMap = new TreeMap<String, MethodStructEntry>();
		this.systemPath = systemPath;
		try {
			buffer = new RandomAccessFile(systemPath + ".methodcall", "rw");
		} catch (FileNotFoundException e) {
			throw new AssertionError("Can not open method calling external buffer!");
		}
		entryBuffer = new MethodStructEntry[32];
	}
	
	public void clear() {
		structMap = null;
		try {
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void put(MethodDefinition method, MethodStructEntry entry) {
		if (currentBufferLength < entryBuffer.length) {
			entryBuffer[currentBufferLength] = new MethodStructEntry(entry);
			currentBufferLength++;
		}
		
		MethodStructBufferEntry bufferEntry = new MethodStructBufferEntry(entry);
		writeCalledMethodMapToBufferFile(bufferEntry);
		structMap.put(method.getUniqueId(), bufferEntry);
	}
	
	@Override
	public MethodStructEntry get(MethodDefinition method) {
		for (int i = 0; i < currentBufferLength; i++) {
			if (entryBuffer[i].getMethod() == method) return entryBuffer[i];
		}
		MethodStructBufferEntry entryInMap = (MethodStructBufferEntry)structMap.get(method.getUniqueId());
		MethodStructBufferEntry entry = new MethodStructBufferEntry(entryInMap);
		TreeMap<MethodDefinition, MethodCallInformation> calledMethodMap = readCalledMethodMapFromBufferFile(entry);
		entry.setCalledMethodMap(calledMethodMap);
		
		entryBuffer[currentBufferIndex] = entry;
		if (++currentBufferIndex >= currentBufferLength) currentBufferIndex = 0;
		
//		int bufferIndex = 0;
//		for (int i = 0; i < currentBufferLength; i++) {
//			if (entryBuffer[i].getCalledMethodMapSize() < entryBuffer[bufferIndex].getCalledMethodMapSize()) bufferIndex = i; 
//		}
//		entryBuffer[bufferIndex] = entry;
		return entry;
	}
	
	public MethodStructEntry getWithoutCallInformation(MethodDefinition method) {
		MethodStructEntry entry = structMap.get(method.getUniqueId());
		return entry;
	}
	
	private void writeCalledMethodMapToBufferFile(MethodStructBufferEntry entry) {
		TreeMap<MethodDefinition, MethodCallInformation> calledMethodMap = entry.getCalledMethodMap();
		int size = 0;
		Set<MethodDefinition> methodSet = null;
		if (calledMethodMap != null) {
			methodSet = calledMethodMap.keySet();
			size = methodSet.size();
		}
		entry.setStartPosition(currentPosition);
		entry.setCalledMethodMap(null);
		entry.setCalledMethodMapSize(size);
		
		if (size > 0) {
			try {
				buffer.seek(currentPosition);
				for (MethodDefinition callee : methodSet) {
					MethodCallInformation callInformation = calledMethodMap.get(callee);
					buffer.writeUTF(callee.getUniqueId());
					buffer.writeInt(callInformation.getStaticCallNumber());
					buffer.writeInt(callInformation.getPolymorphicCallNumber());
				}
				currentPosition = buffer.getFilePointer();
			} catch (IOException e) {
				throw new AssertionError("Can not write method calling external buffer!");
			}
		}
	}

	private TreeMap<MethodDefinition, MethodCallInformation> readCalledMethodMapFromBufferFile(MethodStructBufferEntry entry) {
		TreeMap<MethodDefinition, MethodCallInformation> calledMethodMap = new TreeMap<MethodDefinition, MethodCallInformation>();
		
		long startPosition = entry.getStartPosition();
		int size = entry.getCalledMethodMapSize();
		
		if (size > 0) {
			try {
				buffer.seek(startPosition);
				for (int i = 0; i < size; i++) {
					String methodId = buffer.readUTF();
					int staticCallNumber = buffer.readInt();
					int polymorphicCallNumber = buffer.readInt();
					
					MethodStructEntry calleeEntry = structMap.get(methodId);
					MethodDefinition callee = calleeEntry.getMethod();
					MethodCallInformation callInformation = new MethodCallInformation(staticCallNumber, polymorphicCallNumber);
					calledMethodMap.put(callee, callInformation);
				}
			} catch (IOException e) {
				throw new AssertionError("Can not Read method calling external buffer!");
			}
		}
		return calledMethodMap;
	}
	
}
