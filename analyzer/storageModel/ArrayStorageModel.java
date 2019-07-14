package analyzer.storageModel;

import java.util.List;

import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.NameReference;
import sourceCodeAST.SourceCodeLocation;

public class ArrayStorageModel implements IAbstractStorageModel {
	private NameDefinition name;  					// The name of the array
	// The subscript index expressions of the array, which can be more than one expressions for multiple dimension array. 
	// NOTE: the first index expression is the subscript for the last dimension of the array. 
	private List<NameReference> indexList; 			   
	
	public ArrayStorageModel(NameDefinition name, List<NameReference> indexList) {
		this.name = name;
		this.indexList = indexList;
	}

	@Override
	public boolean referToSameStorage(IAbstractStorageModel another) {
		if (another instanceof ArrayStorageModel) {
			ArrayStorageModel anotherArray = (ArrayStorageModel)another;
			// Here we temporarily ignore the index expressions of the array. In other words, we consider 
			// an array access expression probably accesses any elements of the array, and then different index
			// expressions are regarded as to refer to the same storage
			if (name == anotherArray.name) return checkIndexList(anotherArray);
			else return false;
		}
		return false;
	}

	@Override
	public boolean accessible(SourceCodeLocation location) {
		return true;
	}

	public NameDefinition getName() {
		return name;
	}
	
	public List<NameReference> getIndexList() {
		return indexList;
	}
	
	public String getSubscriptExpression() {
		StringBuilder result = new StringBuilder();
		for (NameReference index : indexList) {
			result.append("[" + index.toSimpleString() + "]");
		}
		return result.toString();
	}
	
	@Override
	public String getExpression() {
		return name.getSimpleName() + getSubscriptExpression();
	}
	
	boolean checkIndexList(ArrayStorageModel another) {
		int size = indexList.size();
		if (size > another.indexList.size()) size = another.indexList.size();
		
		for (int i = 0; i < size; i++) {
			NameReference indexExpression = indexList.get(i);
			NameReference anotherExpression = another.indexList.get(i);
			if (indexExpression.isLiteralReference() && anotherExpression.isLiteralReference()) {
				if (!indexExpression.toSimpleString().equals(anotherExpression.toSimpleString())) return false; 
			}
		}
		return true;
	}

	@Override
	public StorageModelKind getKind() {
		return StorageModelKind.SMK_ARRAY;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public NameDefinition getCoreDefinition() {
		return name;
	}
			

}
