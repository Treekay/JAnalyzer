package analyzer.storageModel;

import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;
import sourceCodeAST.SourceCodeLocation;

/**
 * @author Zhou Xiaocong
 * @since 2018年4月10日
 * @version 1.0
 *
 */
public class SimpleStorageModel implements IAbstractStorageModel {
	// A name definition refer to a storage, and then it can be regarded as a simple storage model
	NameDefinition definition = null;
	
	public SimpleStorageModel(NameDefinition definition) {
		this.definition = definition;
	}

	public NameDefinition getDefinition() {
		return definition;
	}
	
	@Override
	public boolean referToSameStorage(IAbstractStorageModel another) {
		if (another instanceof SimpleStorageModel) {
			SimpleStorageModel storage = (SimpleStorageModel)another;
			return (definition == storage.definition);
		}
		return false;
	}

	@Override
	public boolean accessible(SourceCodeLocation location) {
		return definition.getScope().containsLocation(location);
	}

	@Override
	public String getExpression() {
		return definition.getSimpleName();
	}
	
	@Override
	public StorageModelKind getKind() {
		return StorageModelKind.SMK_SIMPLE;
	}

	@Override
	public boolean isPrimitive() {
		TypeDefinition type = definition.getDeclareTypeDefinition();
		if (type == null) return true;
		if (type.isPrimitive()) return true;
		else return false;
	}

	@Override
	public NameDefinition getCoreDefinition() {
		return definition;
	}
}
