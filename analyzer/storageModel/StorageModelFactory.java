package analyzer.storageModel;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameReference.referenceGroup.NameReferenceGroupKind;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ24ÈÕ
 * @version 1.0
 *
 */
public class StorageModelFactory {

	public static IAbstractStorageModel extractLeftStorageModelInReference(NameReference reference) {
	//		SourceCodeLocation location = reference.getLocation();
	//		String file = location.getFileUnitName();
	//		int lineNumber = location.getLineNumber();
	//		int column = location.getColumn();
	//		String expression = reference.toSimpleString();
			
			if (!reference.isGroupReference()) {
	//			Debug.println(file + "\t(" + lineNumber + ":" + column + ")\tAssignment\t" + reference.getReferenceKind() + "\t" + expression + "\t" + reference.getDefinition().getUniqueId() + "\t" + reference.getDefinition().getDefinitionKind());
				return new SimpleStorageModel(reference.getDefinition());
			}
			NameReferenceGroup group = (NameReferenceGroup)reference;
			NameReferenceGroupKind groupKind = group.getGroupKind();
			List<NameReference> sublist = group.getSubReferenceList();
	
			IAbstractStorageModel result = null;
			if (groupKind == NameReferenceGroupKind.NRGK_ARRAY_ACCESS) {
				List<NameReference> indexList = new ArrayList<NameReference>();
				// Find the array name in this reference!
				NameReference firstSubReference = sublist.get(0);
				indexList.add(sublist.get(1));		// sublist.get(1) is the script expression for the last dimension of the array
				while (firstSubReference.isGroupReference()) {
					NameReferenceGroup firstGroup = (NameReferenceGroup)firstSubReference;
					if (firstGroup.getGroupKind() == NameReferenceGroupKind.NRGK_ARRAY_ACCESS) {
						// The original expression is a multiple dimension array access expression
						sublist = firstSubReference.getSubReferenceList();
						firstSubReference = sublist.get(0);
						indexList.add(sublist.get(1));
					} else break;
				}
				result = new ArrayStorageModel(firstSubReference.getDefinition(), indexList);
			} else if (groupKind == NameReferenceGroupKind.NRGK_FIELD_ACCESS) {
				result =  new FieldStorageModel(sublist.get(0), (FieldDefinition)sublist.get(1).getDefinition());
			} else if (groupKind == NameReferenceGroupKind.NRGK_METHOD_INVOCATION ||
					groupKind == NameReferenceGroupKind.NRGK_SUPER_METHOD_INVOCATION) {
				for (NameReference subreference : sublist) {
					if (subreference.getReferenceKind() == NameReferenceKind.NRK_METHOD) {
						result = new SimpleStorageModel(subreference.getDefinition());
					}
				}
			} else if (groupKind == NameReferenceGroupKind.NRGK_SUPER_FIELD_ACCESS) {
				NameReference firstReference = sublist.get(0);
				if (firstReference.getReferenceKind() == NameReferenceKind.NRK_TYPE) {
					result = new FieldStorageModel(firstReference, (FieldDefinition)sublist.get(1).getDefinition());
				} else result = new FieldStorageModel(null, (FieldDefinition)firstReference.getDefinition());
			} else if (groupKind == NameReferenceGroupKind.NRGK_THIS_EXPRESSION) {
				result = new SimpleStorageModel(sublist.get(0).getDefinition());
			} else if (groupKind == NameReferenceGroupKind.NRGK_QUALIFIED_NAME) {
				NameDefinition definition = sublist.get(1).getDefinition();
				if (definition == null) return null;
				if (definition.isFieldDefinition()) {
					result = new FieldStorageModel(sublist.get(0), (FieldDefinition)definition);
				} else result = new SimpleStorageModel(definition);
			} else if (groupKind == NameReferenceGroupKind.NRGK_CAST) {
				result = extractLeftStorageModelInReference(sublist.get(1));
			}
			
	//		Debug.println(file + "\t(" + lineNumber + ":" + column + ")\tAssignment\t" + groupKind + "\t" + expression + "\t" + result.getUniqueId() + "\t" + result.getDefinitionKind());
	//		System.out.println("Extract storage model for reference = [" + reference.toSimpleString() + "], group kind = " + groupKind);
			return result;
		}

}
