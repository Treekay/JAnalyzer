package softwareStructure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.TreeMap;
import java.util.TreeSet;
import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ7ÈÕ
 * @version 1.0
 */
class DetailedTypeStructBufferEntryManager extends DetailedTypeStructEntryManager {
	protected RandomAccessFile buffer = null;
	protected long currentPosition = 0;

	public DetailedTypeStructBufferEntryManager() {
	}
	
	public void initialize(String systemPath) {
		structMap = new TreeMap<String, DetailedTypeStructEntry>();
		try {
			buffer = new RandomAccessFile(systemPath + ".typeuse", "rw");
		} catch (FileNotFoundException e) {
			throw new AssertionError("Can not open type using external buffer!");
		}
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
	public void put(DetailedTypeDefinition type, DetailedTypeStructEntry entry) {
		DetailedTypeStructBufferEntry bufferEntry = new DetailedTypeStructBufferEntry(entry);
		writeUsedTypeSetToBufferFile(bufferEntry);
		structMap.put(type.getUniqueId(), bufferEntry);
	}
	
	@Override
	public DetailedTypeStructEntry get(DetailedTypeDefinition type) {
		DetailedTypeStructBufferEntry entryInMap = (DetailedTypeStructBufferEntry)structMap.get(type.getUniqueId());
		DetailedTypeStructBufferEntry entry = new DetailedTypeStructBufferEntry(entryInMap);
		TreeSet<DetailedTypeDefinition> usedTypeSet = readUsedTypeSetFromBufferFile(entry); 
		entry.setUsedTypeSet(usedTypeSet);
		return entry;
	}
	
	@Override
	public DetailedTypeStructEntry getWithoutUsedTypeSet(DetailedTypeDefinition type) {
		return structMap.get(type.getUniqueId());
	}
	
	protected void writeUsedTypeSetToBufferFile(DetailedTypeStructBufferEntry entry) {
		TreeSet<DetailedTypeDefinition> usedTypeSet = entry.getUsedTypeSet();
		int size = 0;
		if (usedTypeSet != null) size = usedTypeSet.size();
		entry.setStartPosition(currentPosition);
		entry.setUsedTypeSet(null);
		entry.setUsedTypeSetSize(size);
		
		if (size > 0) {
			try {
				buffer.seek(currentPosition);
				for (DetailedTypeDefinition usedType : usedTypeSet) {
					buffer.writeUTF(usedType.getUniqueId());
				}
				currentPosition = buffer.getFilePointer();
			} catch (IOException e) {
				throw new AssertionError("Can not write type using external buffer!");
			}
		}
	}
	
	protected TreeSet<DetailedTypeDefinition> readUsedTypeSetFromBufferFile(DetailedTypeStructBufferEntry entry) {
		TreeSet<DetailedTypeDefinition> usedTypeSet = new TreeSet<DetailedTypeDefinition>();
		long startPosition = entry.getStartPosition();
		int size = entry.getUsedTypeSetSize();
		
		if (size > 0) {
			try {
				buffer.seek(startPosition);
				for (int i = 0; i < size; i++) {
					String usedTypeId = buffer.readUTF();
					
					DetailedTypeStructEntry usedTypeStructEntry = structMap.get(usedTypeId);
					DetailedTypeDefinition usedType = usedTypeStructEntry.getType();
					usedTypeSet.add(usedType);
				}
			} catch (IOException e) {
				throw new AssertionError("Can not Read type using external buffer!");
			}
		}
		return usedTypeSet;
	}
}
