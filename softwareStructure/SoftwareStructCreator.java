package softwareStructure;

import java.util.ArrayList;
import java.util.List;

import util.Debug;
import nameTable.NameTableManager;
import nameTable.creator.NameReferenceCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;

/**
 * @author Zhou Xiaocong
 * @since 2016年4月1日
 * @version 1.0
 */
class SoftwareStructCreator {
	protected NameTableManager tableManager = null;
	protected NameReferenceCreator referenceCreator = null;
	protected DetailedTypeStructEntryManager typeStructManager = null;
	protected MethodStructEntryManager methodStructManager = null;
	List<DetailedTypeDefinition> allDetailedTypeList = null;
	
	public SoftwareStructCreator() {
	}

	public SoftwareStructManager create(SoftwareStructManager resultManager) {
		tableManager = resultManager.getNameTableManager();
		referenceCreator = new NameReferenceCreator(tableManager);
		
		typeStructManager = resultManager.getDetailedTypeStructEntryManager();
		methodStructManager = resultManager.getMethodStructManager();
		
		// Get all detailed type definition of the system!
		allDetailedTypeList = resultManager.getAllDetailedTypeDefinition();
		int total = allDetailedTypeList.size();
		int counter = 0;
		
		// Initialized the structure map in typeStructManager, so that we can get a object of DetailedTypeDefinition from this 
		// map even when we have not create structure information for this detail type. We need to get such object for 
		// DetailedTypeStructEntryManagerWithBuffer, because when we read the usedTypeSet from the external buffer, we have 
		// to get such object of the used type by its unique ID. Note that a detailed type maybe uses another detailed type
		// for which we has not created structure information.
		for (DetailedTypeDefinition currentType : allDetailedTypeList) {
			DetailedTypeStructEntry entry = new DetailedTypeStructEntry(currentType);
			typeStructManager.put(currentType, entry);
		}
		
		// Scan all detailed types, and generate structure information about the detailed types and method definitions
		// But this step does not create descendant type information of detailed types
		for (DetailedTypeDefinition currentType : allDetailedTypeList) {
			// The structure information about the current type maybe have generated, since we generated the structure information of 
			// all ancestors of a type first. 
			Debug.println("Total " + total + ", Create structure for " + counter + " " + currentType.getFullQualifiedName());
			createStructureInformationForType(currentType);
			counter++;
		}
		
		// Create descendant type information for all types
		counter = 0;
		for (DetailedTypeDefinition currentType : allDetailedTypeList) {
			Debug.println("Total " + total + ", Create descendant type for " + counter + " " + currentType.getFullQualifiedName());
			createDescendantTypeInformationForType(currentType);
			counter++;
		}
		return resultManager;
	}
	
	protected DetailedTypeStructEntry createDescendantTypeInformationForType(DetailedTypeDefinition type) {
		List<DetailedTypeDefinition> descendantTypeList = null;
		List<DetailedTypeDefinition> childrenTypeList = null;
		
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(type);
		childrenTypeList = typeStructEntry.getAllChildrenTypeList();
		if (childrenTypeList != null) {
			// Here we assume the descendant types list (include all children types) are created and buffered in the map!
			return typeStructEntry;
		}
		
		childrenTypeList = new ArrayList<DetailedTypeDefinition>();
		for (DetailedTypeDefinition possibleChildType : allDetailedTypeList) {
			DetailedTypeStructEntry childTypeStructEntry = typeStructManager.getWithoutUsedTypeSet(possibleChildType);
			if (childTypeStructEntry != null) {
				List<DetailedTypeDefinition> parentOfChildTypeList = childTypeStructEntry.getAllParentTypeList();
				if (parentOfChildTypeList.contains(type)) {
					childrenTypeList.add(possibleChildType); 
				}
			} else {
				List<TypeReference> superTypeList = possibleChildType.getSuperList();
				if (superTypeList == null) continue;
				
				for (TypeReference superTypeReference : superTypeList) {
					superTypeReference.resolveBinding();
					if (superTypeReference.isResolved()) continue;
					TypeDefinition superTypeDefinition = (TypeDefinition)superTypeReference.getDefinition();
					if (!superTypeDefinition.isDetailedType()) continue;
					DetailedTypeDefinition superDetailedType = (DetailedTypeDefinition)superTypeDefinition;
					if (superDetailedType == type) {
						childrenTypeList.add(possibleChildType);
						break;
					}
				}
			}
		}
		
		descendantTypeList = new ArrayList<DetailedTypeDefinition>();
		int classToLeafDepth = 0;
		for (DetailedTypeDefinition childType : childrenTypeList) {
			DetailedTypeStructEntry childTypeStructEntry = createDescendantTypeInformationForType(childType);
			List<DetailedTypeDefinition> descendantOfChildTypeList = childTypeStructEntry.getAllDescendantTypeList();
			for (DetailedTypeDefinition descendantOfChildType : descendantOfChildTypeList) {
				if (childrenTypeList.contains(descendantOfChildType)) continue;
				if (descendantTypeList.contains(descendantOfChildType)) continue;
				descendantTypeList.add(descendantOfChildType);
			}
			
			int currentLeafDepth = childTypeStructEntry.getClassToLeafDepth() + 1;
			if (classToLeafDepth < currentLeafDepth) classToLeafDepth = currentLeafDepth;
		}
		typeStructEntry.setChildrenTypeList(childrenTypeList);
		typeStructEntry.setDescendantTypeList(descendantTypeList);
		typeStructEntry.setClassToLeafDepth(classToLeafDepth);
		return typeStructEntry;
	}
	
	protected DetailedTypeStructEntry createStructureInformationForType(DetailedTypeDefinition type) {
		DetailedTypeStructEntry currentEntry = typeStructManager.getWithoutUsedTypeSet(type);
		if (currentEntry.hasInitializedStructureInformation()) return currentEntry;
		currentEntry.initializeStructureInformation();
		
		// First, we scan its super type list
		List<TypeReference> superTypeList = type.getSuperList();
		int depthOfInheritance = 0;
		double averageOfInheritanceDepth = 0;
		int detailedSuperTypeNumber = 0;
		if (superTypeList != null) {
			for (TypeReference superTypeReference : superTypeList) {
				superTypeReference.resolveBinding();
				TypeDefinition superTypeDefinition = (TypeDefinition)superTypeReference.getDefinition();
				if (superTypeDefinition != null) {
					if (superTypeDefinition.isDetailedType()) {
						DetailedTypeDefinition superDetailedType = (DetailedTypeDefinition)superTypeDefinition;
						DetailedTypeStructEntry superTypeStructEntry = typeStructManager.getWithoutUsedTypeSet(superDetailedType);
						if (!superTypeStructEntry.hasInitializedStructureInformation()) {
							// Super type has not been scanned, so we scan the super type first
							superTypeStructEntry = createStructureInformationForType(superDetailedType);
						} 
						
						// We inherit some structure information from the super type for this type
						List<DetailedTypeDefinition> ancestorOfSuperTypeList = superTypeStructEntry.getAllAncestorTypeList();
						for (DetailedTypeDefinition ancestor : ancestorOfSuperTypeList) {
							currentEntry.addAncestorType(ancestor);
						}
						currentEntry.addParentType(superDetailedType);
						detailedSuperTypeNumber++;
						
						averageOfInheritanceDepth += superTypeStructEntry.getAverageInheritanceDepth();
						int currentDepth = superTypeStructEntry.getDepthOfInheritance() + 1;
						if (depthOfInheritance < currentDepth) depthOfInheritance = currentDepth;
					}
				}
			}
		}
		if (detailedSuperTypeNumber > 0) averageOfInheritanceDepth = averageOfInheritanceDepth/ detailedSuperTypeNumber + 1;
		currentEntry.setAverageInheritanceDepth(averageOfInheritanceDepth);
		currentEntry.setDepthOfInheritance(depthOfInheritance);
		
		// So far, we ignore the inner type declared in a detailed type. This does not mean we do not scan the inner type, we may 
		// scan it in the loop given in the above method create(). But this means we do not regard the fields, the methods and used
		// types in the inner type as the fields, methods and used types of the outer type.
		
		// Scan fields of the current type
		List<FieldDefinition> fieldList = type.getFieldList();
		if (fieldList != null) {
			for (FieldDefinition field : fieldList) {
				// Get the type definitions used to declare the type of the field
				List<TypeDefinition> fieldTypeList = field.getTypeDefinition(true);
				// So far we do not buffer the type definitions used to declare the type, we only add it to the used type set of 
				// this type.
				for (TypeDefinition fieldType : fieldTypeList) {
					if (fieldType.isDetailedType()) currentEntry.addUsedType((DetailedTypeDefinition)fieldType);
				}
				// Scan references in field initialize expression for get structure information of this type
				List<NameReference> referenceList = referenceCreator.createReferences(field);
				scanReferenceInFieldInitializeExpression(currentEntry, referenceList);
			}
		}
		
		
		// Scan methods of the current type
		List<MethodDefinition> methodList = type.getMethodList();
		if (methodList != null) {
			for (MethodDefinition method : methodList) {
				if (!method.isAbstract()) currentEntry.addImplementedMethod(method);
				MethodStructEntry methodStructEntry = createMethodStructureInformation(currentEntry, method);
				methodStructManager.put(method, methodStructEntry);
			}
		}
		typeStructManager.put(type, currentEntry);
		return currentEntry;
	}
	
	protected void scanReferenceInFieldInitializeExpression(DetailedTypeStructEntry currentEntry, List<NameReference> referenceList) {
		if (referenceList == null) return;
		for (NameReference reference : referenceList) {
			if (hasTypeReference(reference)) {
				reference.resolveBinding();
				NameReferenceKind kind = reference.getReferenceKind();
				if (kind == NameReferenceKind.NRK_GROUP) {
					NameReferenceGroup group = (NameReferenceGroup)reference;
					List<NameReference> leafreferenceList = group.getReferencesAtLeaf();
					for (NameReference leafreference : leafreferenceList) {
						scanSingleReferenceInFieldInitializeExpression(currentEntry, leafreference);
					}
				} else scanSingleReferenceInFieldInitializeExpression(currentEntry, reference);
			}
		}
	}
	
	protected void scanSingleReferenceInFieldInitializeExpression(DetailedTypeStructEntry typeStructEntry, NameReference reference) {
		if (!reference.isResolved()) return;
		if (reference.getReferenceKind() != NameReferenceKind.NRK_TYPE) return;
		TypeDefinition type = (TypeDefinition)reference.getDefinition();
		if (!type.isDetailedType()) return;
		DetailedTypeDefinition detailedType = (DetailedTypeDefinition)type;
		typeStructEntry.addUsedType(detailedType);
	}
	
	/**
	 * Test whether a reference is a type reference or a reference group includes type reference
	 */
	protected boolean hasTypeReference(NameReference reference) {
		NameReferenceKind kind = reference.getReferenceKind();
		if (kind == NameReferenceKind.NRK_GROUP) {
			NameReferenceGroup group = (NameReferenceGroup)reference;
			List<NameReference> subreferenceList = group.getSubReferenceList();
			if (subreferenceList == null) return false;
			for (NameReference subreference : subreferenceList) {
				if (hasTypeReference(subreference)) return true;
			}
			return false;
		} else if (kind == NameReferenceKind.NRK_TYPE) return true;
		else return false;
	}

	/**
	 * Test whether a reference is a type, field or method reference,  or a reference group includes type, field or method reference
	 */
	protected boolean hasTypeFieldOrMethodReference(NameReference reference) {
		NameReferenceKind kind = reference.getReferenceKind();
		if (kind == NameReferenceKind.NRK_GROUP) {
			NameReferenceGroup group = (NameReferenceGroup)reference;
			List<NameReference> subreferenceList = group.getSubReferenceList();
			if (subreferenceList == null) return false;
			for (NameReference subreference : subreferenceList) {
				if (hasTypeFieldOrMethodReference(subreference)) return true;
			}
			return false;
		} else if (kind == NameReferenceKind.NRK_TYPE) return true;
		else if (kind == NameReferenceKind.NRK_FIELD) return true;
		else if (kind == NameReferenceKind.NRK_METHOD) return true;
		else if (kind == NameReferenceKind.NRK_VARIABLE) return true;
		else return false;
	}

	protected void scanMethodStructureInformation(DetailedTypeStructEntry typeStructEntry, MethodDefinition method) {
		// Scan the return type of the method
		List<TypeDefinition> returnTypeList = method.getReturnTypeDefinition(true);
		if (returnTypeList != null) {
			for (TypeDefinition returnType : returnTypeList) {
				if (returnType == null) continue;
				if (returnType.isDetailedType()) typeStructEntry.addUsedType((DetailedTypeDefinition)returnType);
			}
		}

		// Scan the throw types of the method
		List<TypeReference> throwTypeReferenceList = method.getThrowTypeList();
		if (throwTypeReferenceList != null) {
			for (TypeReference throwTypeReference : throwTypeReferenceList) {
				throwTypeReference.resolveBinding();
				if (throwTypeReference.isResolved()) {
					TypeDefinition throwType = (TypeDefinition)throwTypeReference.getDefinition(); 
					if (throwType.isDetailedType()) typeStructEntry.addUsedType((DetailedTypeDefinition)throwType);
				}
			}
		}

		// Scan the parameters of the method
		List<VariableDefinition> parameterList = method.getParameterList();
		if (parameterList != null) {
			for (VariableDefinition parameter : parameterList) {
				List<TypeDefinition> oneParameterTypes = parameter.getTypeDefinition(true);
				if (oneParameterTypes != null) {
					for (TypeDefinition parameterType : oneParameterTypes) {
						if (parameterType == null) continue;
						if (parameterType.isDetailedType()) typeStructEntry.addUsedType((DetailedTypeDefinition)parameterType);
					}
					
				}
			}
		}
		
		// Scan the body of the method
		List<NameReference> referenceList = referenceCreator.createReferences(method);
		scanReferenceInMethodBody(typeStructEntry, null, referenceList);
	}

	protected MethodStructEntry createMethodStructureInformation(DetailedTypeStructEntry typeStructEntry, MethodDefinition method) {
		// Scan the return type of the method
		List<TypeDefinition> returnTypeList = method.getReturnTypeDefinition(true);
		if (returnTypeList != null) {
			for (TypeDefinition returnType : returnTypeList) {
				if (returnType == null) continue;
				if (returnType.isDetailedType()) typeStructEntry.addUsedType((DetailedTypeDefinition)returnType);
			}
		}

		// Scan the throw types of the method
		List<TypeReference> throwTypeReferenceList = method.getThrowTypeList();
		if (throwTypeReferenceList != null) {
			for (TypeReference throwTypeReference : throwTypeReferenceList) {
				throwTypeReference.resolveBinding();
				if (throwTypeReference.isResolved()) {
					TypeDefinition throwType = (TypeDefinition)throwTypeReference.getDefinition(); 
					if (throwType.isDetailedType()) typeStructEntry.addUsedType((DetailedTypeDefinition)throwType);
				}
			}
		}

		MethodStructEntry currentEntry = new MethodStructEntry(method);
		currentEntry.initializeStructureInformation();
		// Scan the parameters of the method
		List<VariableDefinition> parameterList = method.getParameterList();
		if (parameterList != null) {
			for (VariableDefinition parameter : parameterList) {
				List<TypeDefinition> oneParameterTypes = parameter.getTypeDefinition(true);
				if (oneParameterTypes != null) {
					for (TypeDefinition parameterType : oneParameterTypes) {
						if (parameterType == null) continue;
						if (parameterType.isDetailedType()) {
							typeStructEntry.addUsedType((DetailedTypeDefinition)parameterType);
						}
						currentEntry.addParameterType(parameterType);
					}
					
				}
			}
		}
		
		// Scan the body of the method
		List<NameReference> referenceList = referenceCreator.createReferences(method);
		scanReferenceInMethodBody(typeStructEntry, currentEntry, referenceList);
		return currentEntry;
	}
	
	protected void scanReferenceInMethodBody(DetailedTypeStructEntry typeStructEntry, MethodStructEntry methodStructEntry, List<NameReference> referenceList) {
		if (referenceList == null) return;
		for (NameReference reference : referenceList) {
			if (hasTypeFieldOrMethodReference(reference)) {
				reference.resolveBinding();
				NameReferenceKind kind = reference.getReferenceKind();
				if (kind == NameReferenceKind.NRK_GROUP) {
					NameReferenceGroup group = (NameReferenceGroup)reference;
					List<NameReference> leafreferenceList = group.getReferencesAtLeaf();
					for (NameReference leafreference : leafreferenceList) {
//						if (leafreference.getName().contains("mb")) {
//							System.out.println("\tScan leaf reference " + leafreference.getName() + " of group " + reference.getName());
//							System.out.println("\t\tLeaf reference bind to " + leafreference.getDefinition().getFullQualifiedName() + ", group bind to " + reference.getDefinition().getFullQualifiedName());
//						}
						scanSingleReferenceInMethodBody(typeStructEntry, methodStructEntry, leafreference);
					}
				} else {
//					if (reference.getName().contains("mb")) {
//						System.out.println("\tScan reference " + reference.toFullString());
//					}
					scanSingleReferenceInMethodBody(typeStructEntry, methodStructEntry, reference);
				}
			}
		}
	}
	
	protected void scanSingleReferenceInMethodBody(DetailedTypeStructEntry typeStructEntry, MethodStructEntry methodStructEntry, NameReference reference) {
		if (!reference.isResolved()) return;
		NameReferenceKind kind = reference.getReferenceKind();
		if (kind == NameReferenceKind.NRK_TYPE && typeStructEntry != null) {
			TypeDefinition type = (TypeDefinition)reference.getDefinition();
			if (!type.isDetailedType()) return;
			DetailedTypeDefinition detailedType = (DetailedTypeDefinition)type;
			typeStructEntry.addUsedType(detailedType);
		} else if (kind == NameReferenceKind.NRK_FIELD || kind == NameReferenceKind.NRK_VARIABLE) {
			NameDefinition nameDefinition = reference.getDefinition();
			if (!nameDefinition.isFieldDefinition()) return;
			FieldDefinition field = (FieldDefinition)nameDefinition;
			if (methodStructEntry != null) methodStructEntry.addFieldUsing(field);
			if (typeStructEntry != null) {
				TypeDefinition enclosingType = field.getEnclosingType();
				if (enclosingType.isDetailedType()) typeStructEntry.addUsedType((DetailedTypeDefinition)enclosingType);
			}
		} else if (kind == NameReferenceKind.NRK_METHOD) {
			MethodReference methodReference = (MethodReference)reference;
			MethodDefinition staticCalledMethod = (MethodDefinition)methodReference.getDefinition();
			if (methodStructEntry != null) {
				List<MethodDefinition> polymorphicCalledMethodList = methodReference.getAlternativeList();
				if (polymorphicCalledMethodList != null) {
					for (MethodDefinition polymorphicCalledMethod : polymorphicCalledMethodList) {
						if (polymorphicCalledMethod == staticCalledMethod) methodStructEntry.addMethodCallInformation(polymorphicCalledMethod, 1, 1);
						else methodStructEntry.addMethodCallInformation(polymorphicCalledMethod, 0, 1);
					}
				} else methodStructEntry.addMethodCallInformation(staticCalledMethod, 1, 0);
			}
			if (typeStructEntry != null) {
				TypeDefinition enclosingType = staticCalledMethod.getEnclosingType();
				if (enclosingType.isDetailedType()) typeStructEntry.addUsedType((DetailedTypeDefinition)enclosingType);
			}
		}
	}
}
