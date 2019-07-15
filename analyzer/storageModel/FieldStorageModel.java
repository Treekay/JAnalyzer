package analyzer.storageModel;

import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.NameReference;
import sourceCodeAST.SourceCodeLocation;

/**
 * @author Zhou Xiaocong
 * @since 2018年4月18日
 * @version 1.0
 *
 */
public class FieldStorageModel implements IAbstractStorageModel {
	// The object which the field belongs to. If it is null, then it is "super", i.e. it is for super field access expression
	private NameReference object = null;		 
	private FieldDefinition field = null;		
	
	/**
	 * The parameter object is a name reference, which should be resolved before creating an instance of FieldStorageModel
	 */
	public FieldStorageModel(NameReference object, FieldDefinition field) {
//		System.out.println("Create field storage mode for " + object.toSimpleString() + "." + field.getSimpleName());
		this.object = object;
		this.field = field;
	}

	@Override
	public boolean referToSameStorage(IAbstractStorageModel another) {
		if (another instanceof FieldStorageModel) {
			FieldStorageModel anotherModel = (FieldStorageModel)another;
			NameReference anotherObject = anotherModel.object;
			FieldDefinition anotherField = anotherModel.field;
			if (field != anotherField) return false;
			if (object == null && anotherObject == null) return true;
			if (object != null && anotherObject != null) {
				if (object.getDefinition() != anotherObject.getDefinition()) return false;
				else return true;
			} else return false;
		}
		return false;
	}

	@Override
	public boolean accessible(SourceCodeLocation location) {
		return true;
	}
	
	public NameReference getObjectExpression() {
		return object;
	}
	
	public FieldDefinition getFieldDefinition() {
		return field;
	}
	
	@Override
	public String getExpression() {
		if (object != null) return object.toSimpleString() + "." + field.getSimpleName();
		else return "super." + field.getSimpleName();
	}
	
	@Override
	public StorageModelKind getKind() {
		return StorageModelKind.SMK_FIELD;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public NameDefinition getCoreDefinition() {
		return field;
	}
}
