package softwareStructure;

import java.util.Set;
import java.util.TreeMap;

import nameTable.nameDefinition.MethodDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016年4月6日
 * @version 1.0
 */
public class MethodStructEntryManager {
	protected TreeMap<String, MethodStructEntry> structMap = null;
	protected String systemPath = null;
	
	public MethodStructEntryManager() {
	}
	
	public void initialize(String systemPath) {
		structMap = new TreeMap<String, MethodStructEntry>();
		this.systemPath = systemPath;
	}
	
	public void clear() {
		structMap = null;
	}
	
	public void put(MethodDefinition method, MethodStructEntry entry) {
		structMap.put(method.getUniqueId(), entry);
	}
	
	public MethodStructEntry get(MethodDefinition method) {
		MethodStructEntry entry = structMap.get(method.getUniqueId());
		return entry;
	}
	
	public MethodStructEntry get(String methodId) {
		MethodStructEntry entry = structMap.get(methodId);
		return entry;
	}

	public MethodStructEntry getWithoutCallInformation(MethodDefinition method) {
		MethodStructEntry entry = structMap.get(method.getUniqueId());
		return entry;
	}
	
	public Set<String> getMethodIdSet() {
		return structMap.keySet();
	}
	
	public String[] getSortedMethodIdArray() {
		Set<String> methodIdSet = structMap.keySet();
		String[] methodIdArray = new String[methodIdSet.size()];
		int index = 0;
		for (String methodId : methodIdSet) {
			methodIdArray[index] = methodId;
			index++;
		}
		return methodIdArray;
	}
	
	public MethodDefinition[] getSortedMethodArray() {
		Set<String> methodIdSet = structMap.keySet();
		MethodDefinition[] methodArray = new MethodDefinition[methodIdSet.size()];
		int index = 0;
		for (String methodId : methodIdSet) {
			MethodStructEntry entry = structMap.get(methodId);
			methodArray[index] = entry.getMethod();
			index++;
		}
		return methodArray;
	}
}
