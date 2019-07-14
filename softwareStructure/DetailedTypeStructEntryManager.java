package softwareStructure;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ6ÈÕ
 * @version 1.0
 */
public class DetailedTypeStructEntryManager {
	protected TreeMap<String, DetailedTypeStructEntry> structMap = null;
	
	public DetailedTypeStructEntryManager() {
	}

	public void initialize(String systemPath) {
		structMap = new TreeMap<String, DetailedTypeStructEntry>();
	}
	
	public void clear() {
		structMap = null;
	}

	public void put(DetailedTypeDefinition type, DetailedTypeStructEntry entry) {
		structMap.put(type.getUniqueId(), entry);
	}
	
	public DetailedTypeStructEntry get(DetailedTypeDefinition type) {
		return structMap.get(type.getUniqueId());
	}

	public DetailedTypeStructEntry get(String typeId) {
		return structMap.get(typeId);
	}

	public DetailedTypeStructEntry getWithoutUsedTypeSet(DetailedTypeDefinition type) {
		return structMap.get(type.getUniqueId());
	}
	
	public Set<DetailedTypeDefinition> getDetailedTypeDefinitionSet() {
		TreeSet<DetailedTypeDefinition> resultSet = new TreeSet<DetailedTypeDefinition>();
		Set<String> keySet = structMap.keySet();
		for (String key : keySet) {
			DetailedTypeStructEntry entry = structMap.get(key);
			resultSet.add(entry.getType());
		}
		return resultSet;
	}
	
	public Set<String> getDetailedTypeDefinitionIdSet() {
		return structMap.keySet();
	}
}
