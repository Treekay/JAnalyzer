package softwareStructure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import util.Debug;
import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016年4月8日
 * @version 1.0
 */
class SoftwareStructFileManager {
	protected DetailedTypeStructEntryManager typeStructManager = null;
	protected MethodStructEntryManager methodStructManager = null;
	protected NameTableManager tableManager = null;

	protected RandomAccessFile typeStructIndexFile = null;
	protected RandomAccessFile methodStructIndexFile = null;
	protected RandomAccessFile implementedMethodListFile = null;
	protected RandomAccessFile ancestorTypeListFile = null;
	protected RandomAccessFile parentTypeListFile = null;
	protected RandomAccessFile descendantTypeListFile = null;
	protected RandomAccessFile childrenTypeListFile = null;
	protected RandomAccessFile usedTypeSetFile = null;
	protected RandomAccessFile parameterTypeListFile = null;
	protected RandomAccessFile usedFieldSetFile = null;
	protected RandomAccessFile calledMethodMapFile = null;
	
	public SoftwareStructFileManager() {
	}
	
	public boolean read(SoftwareStructManager manager, String path) {
		if (!openStructFiles(path, "r")) return false;
		
		typeStructManager = manager.getDetailedTypeStructEntryManager();
		methodStructManager = manager.getMethodStructManager();
		tableManager = manager.getNameTableManager();
		
		// Get all detailed type definition of the system!
		List<DetailedTypeDefinition> allDetailedTypeList = manager.getAllDetailedTypeDefinition();
		int total = allDetailedTypeList.size();
		int counter = 0;
		int totalMethodNumber = 0;
		
		// Initialize the structure map in two structure manager
		for (DetailedTypeDefinition type : allDetailedTypeList) {
			DetailedTypeStructEntry entry = new DetailedTypeStructEntry(type);
			entry.initializeStructureInformation();
			typeStructManager.put(type, entry);
//			Debug.time("Put type: " + type.getUniqueId());
			
			List<MethodDefinition> methodList = type.getMethodList();
			if (methodList != null) {
				for (MethodDefinition method : methodList) {
					MethodStructEntry methodEntry = new MethodStructEntry(method);
					methodEntry.initializeStructureInformation();
					methodStructManager.put(method, methodEntry);
//					Debug.time("\tPut method: " + method.getUniqueId());
					totalMethodNumber++;
				}
			}
		}
		
		while (counter < total) {
			try {
				String detailedTypeId = typeStructIndexFile.readUTF();
				DetailedTypeStructEntry entry = typeStructManager.get(detailedTypeId);
				
				Debug.time("Total type " + total + ", reading " + counter + " " + detailedTypeId + "....!");

				int depthOfInheritance = typeStructIndexFile.readInt();
				double averageInheritDepth = typeStructIndexFile.readDouble();
				int classToLeafDepth = typeStructIndexFile.readInt();
				entry.setDepthOfInheritance(depthOfInheritance);
				entry.setAverageInheritanceDepth(averageInheritDepth);
				entry.setClassToLeafDepth(classToLeafDepth);
				
				long position = typeStructIndexFile.readLong();
				int size = typeStructIndexFile.readInt();
				readImplementedMethodList(entry, position, size);

				position = typeStructIndexFile.readLong();
				size = typeStructIndexFile.readInt();
				readAncestorTypeList(entry, position, size);

				position = typeStructIndexFile.readLong();
				size = typeStructIndexFile.readInt();
				readParentTypeList(entry, position, size);

				position = typeStructIndexFile.readLong();
				size = typeStructIndexFile.readInt();
				readDescendantTypeList(entry, position, size);

				position = typeStructIndexFile.readLong();
				size = typeStructIndexFile.readInt();
				readChildrenTypeList(entry, position, size);

				position = typeStructIndexFile.readLong();
				size = typeStructIndexFile.readInt();
				readUsedTypeSet(entry, position, size);
			} catch (IOException e) {
				e.printStackTrace();
				clear();
				return false;
			}
			
			counter++;
		}
		
		counter = 0;
		while (counter < totalMethodNumber) {
			try {
				String methodId = methodStructIndexFile.readUTF();
				MethodStructEntry entry = methodStructManager.get(methodId);

				Debug.time("Total method " + totalMethodNumber + ", reading " + counter + " " + methodId + " ....!");
				
				long position = methodStructIndexFile.readLong();
				int size = methodStructIndexFile.readInt();
				readParameterTypeList(entry, position, size);
				
				position = methodStructIndexFile.readLong();
				size = methodStructIndexFile.readInt();
				readUsedFieldSet(entry, position, size);
				
				position = methodStructIndexFile.readLong();
				size = methodStructIndexFile.readInt();
				readCalledMethodMap(entry, position, size);
			} catch (IOException e) {
				e.printStackTrace();
				clear();
				return false;
			}
			
			counter++;
		}
		
		clear();
		return true;
	}

	public boolean write(SoftwareStructManager manager, String path) {
		if (!openStructFiles(path, "rw")) return false;
		
		typeStructManager = manager.getDetailedTypeStructEntryManager();
		methodStructManager = manager.getMethodStructManager();

		long position = 0;
		int size = 0;
		Set<String> typeIdSet = typeStructManager.getDetailedTypeDefinitionIdSet();
		int total = typeIdSet.size();
		int counter = 0;
		
		for (String typeId : typeIdSet) {
			Debug.time("Total type " + total + ", writing " + counter + " " + typeId + " ....!");
			try {
				typeStructIndexFile.writeUTF(typeId);
				DetailedTypeStructEntry entry = typeStructManager.get(typeId);
				typeStructIndexFile.writeInt(entry.getDepthOfInheritance());
				typeStructIndexFile.writeDouble(entry.getAverageInheritanceDepth());
				typeStructIndexFile.writeInt(entry.getClassToLeafDepth());

				
				size = entry.implementedMethodList.size();
				position = writeImplementedMethodList(entry.implementedMethodList);
				typeStructIndexFile.writeLong(position);
				typeStructIndexFile.writeInt(size);
				
				size = entry.ancestorTypeList.size();
				position = writeAncestorTypeList(entry.ancestorTypeList);
				typeStructIndexFile.writeLong(position);
				typeStructIndexFile.writeInt(size);

				size = entry.parentTypeList.size();
				position = writeParentTypeList(entry.parentTypeList);
				typeStructIndexFile.writeLong(position);
				typeStructIndexFile.writeInt(size);

				size = entry.descendantTypeList.size();
				position = writeDescendantTypeList(entry.descendantTypeList);
				typeStructIndexFile.writeLong(position);
				typeStructIndexFile.writeInt(size);

				size = entry.childrenTypeList.size();
				position = writeChildrenTypeList(entry.childrenTypeList);
				typeStructIndexFile.writeLong(position);
				typeStructIndexFile.writeInt(size);

				size = entry.usedTypeSet.size();
				position = writeUsedTypeSet(entry.usedTypeSet);
				typeStructIndexFile.writeLong(position);
				typeStructIndexFile.writeInt(size);
			} catch (IOException e) {
				e.printStackTrace();
				closeStructFiles();
				return false;
			}
			counter++;
		}
		
		Set<String> methodIdSet = methodStructManager.getMethodIdSet();
		total = methodIdSet.size();
		counter = 0;
		for (String methodId : methodIdSet) {
			Debug.time("Total method " + total + ", writing " + counter + " " + methodId + " ....!");
			
			try {
				methodStructIndexFile.writeUTF(methodId);
				MethodStructEntry entry = methodStructManager.get(methodId);
				
				size = entry.parameterTypeList.size();
				position = writeParameterTypeList(entry.parameterTypeList);
				methodStructIndexFile.writeLong(position);
				methodStructIndexFile.writeInt(size);
				
				size = entry.usedFieldSet.size();
				position = writeUsedFieldSet(entry.usedFieldSet);
				methodStructIndexFile.writeLong(position);
				methodStructIndexFile.writeInt(size);

				size = entry.calledMethodMap.size();
				position = writeCalledMethodMap(entry.calledMethodMap);
				methodStructIndexFile.writeLong(position);
				methodStructIndexFile.writeInt(size);
			} catch (IOException e) {
				e.printStackTrace();
				closeStructFiles();
				return false;
			}
			counter++;
		}
		
		closeStructFiles();
		return true;
	}
	
	protected void clear() {
		closeStructFiles();
//		typeStructManager.clear();
//		methodStructManager.clear();
//		typeStructManager = null;
//		methodStructManager = null;
	}

	protected boolean openStructFiles(String path, String mode) {
		try {
			typeStructIndexFile = new RandomAccessFile(path + "__typestruct.index", mode);
			methodStructIndexFile = new RandomAccessFile(path + "__methodstruct.index", mode);
			implementedMethodListFile = new RandomAccessFile(path + "__implementedmethod.list", mode);
			ancestorTypeListFile = new RandomAccessFile(path + "__ancestortype.list", mode);
			parentTypeListFile = new RandomAccessFile(path + "__parenttype.list", mode);
			descendantTypeListFile = new RandomAccessFile(path + "__descendanttype.list", mode);
			childrenTypeListFile = new RandomAccessFile(path + "__childrentype.list", mode);
			usedTypeSetFile = new RandomAccessFile(path + "__usedtype.set", mode);
			parameterTypeListFile = new RandomAccessFile(path + "__parametertype.list", mode);
			usedFieldSetFile = new RandomAccessFile(path + "__usedfield.set", mode);
			calledMethodMapFile = new RandomAccessFile(path + "__calledmethod.map", mode);
		} catch (FileNotFoundException e) {
//			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	protected void closeStructFiles() {
		try {
			typeStructIndexFile.close();
			methodStructIndexFile.close();
			implementedMethodListFile.close();
			ancestorTypeListFile.close();
			parentTypeListFile.close();
			descendantTypeListFile.close();
			childrenTypeListFile.close();
			usedTypeSetFile.close();
			parameterTypeListFile.close();
			usedFieldSetFile.close();
			calledMethodMapFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readCalledMethodMap(MethodStructEntry entry, long position, int size) throws IOException {
		if (size <= 0) return;
		TreeMap<MethodDefinition, MethodCallInformation> calledMethodMap = new TreeMap<MethodDefinition, MethodCallInformation>();
		calledMethodMapFile.seek(position);
		for (int i = 0; i < size; i++) {
			String methodId = calledMethodMapFile.readUTF();
			int staticCallNumber = calledMethodMapFile.readInt();
			int polymorphicCallNumber = calledMethodMapFile.readInt();
			
			MethodStructEntry calleeEntry = methodStructManager.get(methodId);
			MethodDefinition callee = calleeEntry.getMethod();
			MethodCallInformation callInformation = new MethodCallInformation(staticCallNumber, polymorphicCallNumber);
			calledMethodMap.put(callee, callInformation);
		}
		entry.setCalledMethodMap(calledMethodMap);
	}
	
	private long writeCalledMethodMap(TreeMap<MethodDefinition, MethodCallInformation> calledMethodMap) throws IOException {
		long startPosition = calledMethodMapFile.getFilePointer(); 
		Set<MethodDefinition> calledMethodSet = calledMethodMap.keySet();
		for (MethodDefinition callee : calledMethodSet) {
			MethodCallInformation callInformation = calledMethodMap.get(callee);
			calledMethodMapFile.writeUTF(callee.getUniqueId());
			calledMethodMapFile.writeInt(callInformation.getStaticCallNumber());
			calledMethodMapFile.writeInt(callInformation.getPolymorphicCallNumber());
		}
		return startPosition; 
	}

	private void readUsedFieldSet(MethodStructEntry entry, long position, int size) throws IOException {
		usedFieldSetFile.seek(position);
		for (int i = 0; i < size; i++) {
			String fieldId = usedFieldSetFile.readUTF();
			NameDefinition definition = tableManager.findDefinitionById(fieldId);
			if (definition == null) {
				System.out.println("Can not find field for " + fieldId);
			}
			FieldDefinition field = (FieldDefinition)definition;
			entry.addFieldUsing(field);
		}
	}
	
	private long writeUsedFieldSet(Set<FieldDefinition> usedFieldSet) throws IOException {
		long startPosition = usedFieldSetFile.getFilePointer();
		for (FieldDefinition field : usedFieldSet) {
			String fieldId = field.getUniqueId();
			usedFieldSetFile.writeUTF(fieldId);;
		}
		return startPosition;
	}

	private void readParameterTypeList(MethodStructEntry entry, long position, int size) throws IOException {
		parameterTypeListFile.seek(position);
		for (int i = 0; i < size; i++) {
			String typeId = parameterTypeListFile.readUTF();
			if (NameDefinition.getDefinitionLocationStringFromId(typeId) != null) {
				// This type must be a detailed type or an enumeration type
				DetailedTypeStructEntry typeEntry = typeStructManager.get(typeId);
				if (typeEntry != null) {
//					System.out.println("Can not find type " + typeId + " from map!");
					TypeDefinition type = typeEntry.getType();
					entry.addParameterType(type);
				} else {
					// This type must be an enumeration type
					TypeDefinition type = (TypeDefinition)tableManager.findDefinitionById(typeId);
					entry.addParameterType(type);
				}
			} else {
				// This type must be a simple type, it should be found in the root scope (system scope) of the name table;
				TypeDefinition type = (TypeDefinition)tableManager.findDefinitionById(typeId);
				entry.addParameterType(type);
			}
		}
	}
	
	private long writeParameterTypeList(List<TypeDefinition> parameterTypeList) throws IOException {
		long startPosition = parameterTypeListFile.getFilePointer();
		for (TypeDefinition type : parameterTypeList) {
			String typeId = type.getUniqueId();
			parameterTypeListFile.writeUTF(typeId);;
		}
		return startPosition;
	}

	private void readImplementedMethodList(DetailedTypeStructEntry entry, long position, int size) throws IOException {
		implementedMethodListFile.seek(position);
		for (int i = 0; i < size; i++) {
			String methodId = implementedMethodListFile.readUTF();
			MethodStructEntry methodEntry = methodStructManager.get(methodId);
			if (methodEntry == null) {
				System.out.println("Can not get method entry for " + methodId);
			}
			MethodDefinition method = methodEntry.getMethod();
			entry.addImplementedMethod(method);
		}
	}

	private long writeImplementedMethodList(List<MethodDefinition> implementedMethodList) throws IOException {
		long startPosition = implementedMethodListFile.getFilePointer();
		for (MethodDefinition method : implementedMethodList) {
			String methodId = method.getUniqueId();
			implementedMethodListFile.writeUTF(methodId);;
		}
		return startPosition;
	}
	
	private void readAncestorTypeList(DetailedTypeStructEntry entry, long position, int size) throws IOException {
		ancestorTypeListFile.seek(position);
		for (int i = 0; i < size; i++) {
			String typeId = ancestorTypeListFile.readUTF();
			DetailedTypeDefinition type = typeStructManager.get(typeId).getType();
			entry.addAncestorType(type);
		}
	}
	
	private long writeAncestorTypeList(List<DetailedTypeDefinition> ancestorTypeList) throws IOException {
		long startPosition = ancestorTypeListFile.getFilePointer();
		for (DetailedTypeDefinition type : ancestorTypeList) {
			String typeId = type.getUniqueId();
			ancestorTypeListFile.writeUTF(typeId);;
		}
		return startPosition;
	}

	private void readParentTypeList(DetailedTypeStructEntry entry, long position, int size) throws IOException {
		parentTypeListFile.seek(position);
		for (int i = 0; i < size; i++) {
			String typeId = parentTypeListFile.readUTF();
			DetailedTypeDefinition type = typeStructManager.get(typeId).getType();
			entry.addParentType(type);
		}
	}

	private long writeParentTypeList(List<DetailedTypeDefinition> parentTypeList) throws IOException {
		long startPosition = parentTypeListFile.getFilePointer();
		for (DetailedTypeDefinition type : parentTypeList) {
			String typeId = type.getUniqueId();
			parentTypeListFile.writeUTF(typeId);;
		}
		return startPosition;
	}

	private void readDescendantTypeList(DetailedTypeStructEntry entry, long position, int size) throws IOException {
		descendantTypeListFile.seek(position);
		List<DetailedTypeDefinition> descendantTypeList = new ArrayList<DetailedTypeDefinition>();
		for (int i = 0; i < size; i++) {
			String typeId = descendantTypeListFile.readUTF();
			DetailedTypeDefinition type = typeStructManager.get(typeId).getType();
			descendantTypeList.add(type);
		}
		entry.setDescendantTypeList(descendantTypeList);
	}
	
	private long writeDescendantTypeList(List<DetailedTypeDefinition> descendantTypeList) throws IOException {
		long startPosition = descendantTypeListFile.getFilePointer();
		for (DetailedTypeDefinition type : descendantTypeList) {
			String typeId = type.getUniqueId();
			descendantTypeListFile.writeUTF(typeId);;
		}
		return startPosition;
	}

	private void readChildrenTypeList(DetailedTypeStructEntry entry, long position, int size) throws IOException {
		childrenTypeListFile.seek(position);
		List<DetailedTypeDefinition> childrenTypeList = new ArrayList<DetailedTypeDefinition>();
		for (int i = 0; i < size; i++) {
			String typeId = childrenTypeListFile.readUTF();
			DetailedTypeDefinition type = typeStructManager.get(typeId).getType();
			childrenTypeList.add(type);
		}
		entry.setChildrenTypeList(childrenTypeList);
	}

	private long writeChildrenTypeList(List<DetailedTypeDefinition> childrenTypeList) throws IOException {
		long startPosition = childrenTypeListFile.getFilePointer();
		for (DetailedTypeDefinition type : childrenTypeList) {
			String typeId = type.getUniqueId();
			childrenTypeListFile.writeUTF(typeId);;
		}
		return startPosition;
	}

	private void readUsedTypeSet(DetailedTypeStructEntry entry, long position, int size) throws IOException {
		usedTypeSetFile.seek(position);
		for (int i = 0; i < size; i++) {
			String typeId = usedTypeSetFile.readUTF();
			DetailedTypeDefinition type = typeStructManager.get(typeId).getType();
			entry.addUsedType(type);
		}
	}
	
	private long writeUsedTypeSet(Set<DetailedTypeDefinition> usedTypeSet) throws IOException {
		long startPosition = usedTypeSetFile.getFilePointer();
		for (DetailedTypeDefinition type : usedTypeSet) {
			String typeId = type.getUniqueId();
			usedTypeSetFile.writeUTF(typeId);;
		}
		return startPosition;
	}

}
