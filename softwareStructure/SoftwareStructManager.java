package softwareStructure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import nameTable.NameTableManager;
import nameTable.filter.DetailedTypeDefinitionFilter;
import nameTable.filter.EnumTypeFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameDefinitionVisitor;

/**
 * Manage the structure of a system by given the name table of the software system
 * @author Zhou Xiaocong
 * @since 2015Äê6ÔÂ30ÈÕ
 * @version 1.0
 */
public class SoftwareStructManager {
	protected NameTableManager tableManager = null;
	
	protected DetailedTypeStructEntryManager typeStructManager = null;
	protected MethodStructEntryManager methodStructManager = null;
	
	protected List<DetailedTypeDefinition> classList = null;
	// Note that all enumeration type is not detailed type definition in our name table!!
	protected List<EnumTypeDefinition> enumList = null;

	public SoftwareStructManager(NameTableManager manager) {
		this.tableManager = manager;
	}
	
	public boolean readSoftwareStructure() {
		SoftwareStructFileManager manager = new SoftwareStructFileManager();
		return manager.read(this, tableManager.getSystemPath());
	}

	public boolean writeSoftwareStructure() {
		SoftwareStructFileManager manager = new SoftwareStructFileManager();
		boolean success = manager.write(this, tableManager.getSystemPath());
		return success;
	}

	public void createSoftwareStructure() {
		SoftwareStructCreator creator = new SoftwareStructCreator();
		creator.create(this);
	}
	
	public void readOrCreateSoftwareStructure() {
		SoftwareStructFileManager manager = new SoftwareStructFileManager();
		boolean success = manager.read(this, tableManager.getSystemPath()); 
		if (!success) {
			SoftwareStructCreator creator = new SoftwareStructCreator();
			creator.create(this);
			manager.write(this, tableManager.getSystemPath());
		}
	}
	
	public void clearSoftwareStructure() {
		methodStructManager.clear();
		typeStructManager.clear();
	}
	
	public DetailedTypeStructEntryManager getDetailedTypeStructEntryManager() {
		if (typeStructManager == null) {
			typeStructManager = new DetailedTypeStructEntryManager();
			typeStructManager.initialize(tableManager.getSystemPath());
		}
		return typeStructManager;
	}
	
	public MethodStructEntryManager getMethodStructManager() {
		if (methodStructManager == null) {
			methodStructManager = new MethodStructEntryManager();
			methodStructManager.initialize(tableManager.getSystemPath());
		}
		return methodStructManager;
	}

	public NameTableManager getNameTableManager() {
		return tableManager;
	}

	public List<DetailedTypeDefinition> getAllDetailedTypeDefinition() {
		if (classList == null) {
			SystemScope systemScope = tableManager.getSystemScope();
			NameDefinitionVisitor visitor = new NameDefinitionVisitor();
			visitor.setFilter(new DetailedTypeDefinitionFilter());
			systemScope.accept(visitor);
			List<NameDefinition> allDetailedTypeList = visitor.getResult();
			classList = new ArrayList<DetailedTypeDefinition>();
			for (NameDefinition type : allDetailedTypeList) classList.add((DetailedTypeDefinition)type);
		}
		return classList;
	}
	
	public List<EnumTypeDefinition> getAllEnumTypeDefinition() {
		if (enumList == null) {
			NameDefinitionVisitor visitor = new NameDefinitionVisitor(new EnumTypeFilter());
			SystemScope rootScope = tableManager.getSystemScope();
			rootScope.accept(visitor);
			List<NameDefinition> allEnumTypes = visitor.getResult();
			enumList = new ArrayList<EnumTypeDefinition>();
			for (NameDefinition type : allEnumTypes) enumList.add((EnumTypeDefinition)type);
		}
		return enumList;
	}
	
	/**
	 * If the given class extends a detailed type, then return this detailed type, otherwise return null
	 */
	public DetailedTypeDefinition getParentClass(DetailedTypeDefinition aClass) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aClass);
		if (typeStructEntry != null) return typeStructEntry.getParentClass();
		return null;
	}
	
	/**
	 * Return the super class and interface of a given type. The returned type must be detailed type definition 
	 */
	public List<DetailedTypeDefinition> getDetailedParentTypeList(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getDetailedParentTypeList();
		return null;
	}
	

	/**
	 * Return the super class and interface of a given type. The returned type include simple type definitions 
	 */
	public List<DetailedTypeDefinition> getAllParentTypeList(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getAllParentTypeList();
		return null;
	}
	
	/**
	 * Return all ancestor classes (do not include interface and simple type) of a given class 
	 */
	public List<DetailedTypeDefinition> getAncestorClassList(DetailedTypeDefinition aClass) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aClass);
		if (typeStructEntry != null) return typeStructEntry.getAncestorClassList();
		return null;
	}
	
	/**
	 * Return all ancestors (include classes and interface, and also include simple type) of a given class
	 */
	public List<DetailedTypeDefinition> getAllAncestorTypeList(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getAllAncestorTypeList();
		return null;
	}
	
	/**
	 * Get the average inheritance depth of a type (class or interface). If the type does not have extended any class 
	 * and does not have implemented any interface, then its averageDepthOfInheritance = 1 (since every class or interface 
	 * implicitly is a child of the class Object), otherwise, its averageDepthOfInheritance will be the average of 
	 * averageDepthOfInheritance of its parent (directed extended class and directed implemented interfaces) plus 1. 
	 * Here plus 1 because every class or interface implicitly is a child of the class Object, and plus 1 will ensure 
	 * that the averageDepthOfInheritance of a child class (or interface) is always greater than its parent
	 */
	public double getAverageInheritanceDepth(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getAverageInheritanceDepth();
		return 0;
	}

	/**
	 * Get the depth of the type (class or interface) in inheritance tree. If the class does not have extended any class
	 * and dose not have implemented any interface, then its depth = 1 (since every class or interface implicitly is a 
	 * child of the class Object), otherwise, its depth will be the maximum of depth of its parent (directed extended class
	 * and directed implemented interfaces) plus 1. 
	 */
	public int getDepthOfInheritance(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getDepthOfInheritance();
		return 0;
	}
	
	/**
	 * Return all detailed type ancestors (include classes and interface, but not include simple type) of a given class
	 */
	public List<DetailedTypeDefinition> getAllDetailedAncestorTypeList(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getAllDetailedAncestorTypeList();
		return null;
	}
	
	/**
	 * Get the children class (not interface) of a given class (not a interface) 
	 */
	public List<DetailedTypeDefinition> getChildrenClassList(DetailedTypeDefinition aClass) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aClass);
		if (typeStructEntry != null) return typeStructEntry.getChildrenClassList();
		return null;
	}
	
	/**
	 * Get all children (include sub-classes and sub-interfaces) of a given type (maybe a interface)
	 */
	public List<DetailedTypeDefinition> getAllChildrenTypeList(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getAllChildrenTypeList();
		return null;
	}
	
	/**
	 * Get all descendant class (not include sub-interfaces) of a given class. Maybe we can use DetailedTypeDefinition.isSubtypeOf() to 
	 * simplify the following implementation. However, the following implementation may be more effective. 
	 */
	public List<DetailedTypeDefinition> getDescendantClassList(DetailedTypeDefinition aClass) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aClass);
		if (typeStructEntry != null) return typeStructEntry.getDescendantClassList();
		return null;
	}

	/**
	 * Get all descendant class or interface of a given type. Maybe we can use DetailedTypeDefinition.isSubtypeOf() to 
	 * simplify the following implementation. However, the following implementation may be more effective. 
	 */
	public List<DetailedTypeDefinition> getAllDescendantTypeList(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getAllDescendantTypeList();
		return null;
	}
	
	public Set<DetailedTypeDefinition> getUsedOtherDetailedTypeDefinitionSet(DetailedTypeDefinition type) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.get(type);
		if (typeStructEntry == null) return null;

		Set<DetailedTypeDefinition> otherTypeSet = new TreeSet<DetailedTypeDefinition>();
		
		List<DetailedTypeDefinition> ancestorTypeList = typeStructEntry.getAncestorTypeList();
		List<DetailedTypeDefinition> parentTypeList = typeStructEntry.getAllParentTypeList();
		List<DetailedTypeDefinition> descendantTypeList = typeStructEntry.getDescendantTypeList();
		List<DetailedTypeDefinition> childrenTypeList = typeStructEntry.getAllChildrenTypeList();
		
		// Note that a used other type set is a used class or its descendant class, which is not ancestor or descendant class of 
		// the given type.
		Set<DetailedTypeDefinition> usedTypeSet = typeStructEntry.getUsedTypeSet();
		for (DetailedTypeDefinition usedType : usedTypeSet) {
			// We only consider other class!
			if (usedType.isInterface()) continue;
			// If the used type is descendant type of the given type, then it and its descendant can not be other used type!
			if (descendantTypeList.contains(usedType) || childrenTypeList.contains(usedType)) continue;
			// If the used type is parent type of the given type, then it and its descendant can not be other used type!
			if (parentTypeList.contains(usedType)) continue;
			
			List<DetailedTypeDefinition> descendantOfUsedTypeList = getAllDescendantTypeList(usedType);
			if (!ancestorTypeList.contains(usedType)) otherTypeSet.add(usedType);
			
			for (DetailedTypeDefinition descendantOfUsedType : descendantOfUsedTypeList) {
				if (!descendantTypeList.contains(descendantOfUsedType) && !childrenTypeList.contains(descendantOfUsedType) &&
						!ancestorTypeList.contains(descendantOfUsedType) && parentTypeList.contains(descendantOfUsedType)) 
					otherTypeSet.add(usedType);
			}
		}
		return otherTypeSet;
	}
	
	public int getTypeToLeafDepth(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getClassToLeafDepth();
		return 0;
	}

	/**
	 * Test if the parent is indeed the parent (super class) of the child
	 */
	public boolean hasDirectChildClassRelation(DetailedTypeDefinition parent, DetailedTypeDefinition child) {
		return (getParentClass(child) == parent);
	}

	/**
	 * Test if the ancestor is indeed an ancestor class of the child
	 */
	public boolean hasInheritanceClassRelation(DetailedTypeDefinition ancestor, DetailedTypeDefinition descendant) {
		List<DetailedTypeDefinition> ancestorList = getAncestorClassList(descendant);
		if (ancestorList == null) return false;
		return ancestorList.contains(ancestor);
	}
	
	/**
	 * Test if the parent is indeed the parent (super class or super interface) of the child
	 */
	public boolean hasDirectChildTypeRelation(DetailedTypeDefinition parent, DetailedTypeDefinition child) {
		List<DetailedTypeDefinition> parentList = getAllParentTypeList(child);
		if (parentList == null) return false;
		return parentList.contains(parent);
	}

	/**
	 * Test if the ancestor is indeed an ancestor type (include ancestor interface) of the child
	 */
	public boolean hasInheritanceTypeRelation(DetailedTypeDefinition ancestor, DetailedTypeDefinition descendant) {
		List<DetailedTypeDefinition> ancestorList = getAllAncestorTypeList(descendant);
		if (ancestorList == null) return false;
		return ancestorList.contains(ancestor);
	}
	
	/**
	 * Get those methods which is implemented in the given type, i.e. declaration in the given type and with the method body
	 */
	public List<MethodDefinition> getImplementedMethodList(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getImplementedMethodList();
		return null;
	}

	/**
	 * Get those methods which is usable for the given type, including the following methods:
	 * (1) The abstract method declared in the given type;
	 * (2) Declared in the ancestor types, and is not private method, and is not re-defined (i.e. override) 
	 *     by the method declared in the given type.    
	 */
	public List<MethodDefinition> getDeclaredMethodList(DetailedTypeDefinition aType) {
		List<MethodDefinition> resultList = new ArrayList<MethodDefinition>();
		List<MethodDefinition> typeMethods = aType.getMethodList();
			
		if (typeMethods != null) {
			// Add the abstract method in the given types to the result method list
			for (MethodDefinition method : typeMethods) {
				if (method.isAbstract()) resultList.add(method);
			}
		}
		
		// Add the (non-private and non-overridden) methods declared in the ancestors of the given type to the 
		// result method list
		List<MethodDefinition> ancestorMethodList = getAllInheritedMethodList(aType);
		if (ancestorMethodList == null) return resultList;
		
		for (MethodDefinition ancestorMethod : ancestorMethodList) {
			boolean hasBeenOverridden = false;
			if (typeMethods != null) {
				for (MethodDefinition thisTypeMethod : typeMethods) {
					if (thisTypeMethod.isOverrideMethod(ancestorMethod)) {
						hasBeenOverridden = true;
						break;
					}
				}
			}
			// Add the non-overridden method inherited from the ancestor type to the result list
			if (!hasBeenOverridden) resultList.add(ancestorMethod);
		}
		return resultList;
	}
	
	public List<MethodDefinition> getAllMethodList(DetailedTypeDefinition aType) {
		List<MethodDefinition> resultList = new ArrayList<MethodDefinition>();
		resultList.addAll(getImplementedMethodList(aType));
		resultList.addAll(getDeclaredMethodList(aType));
		return resultList;
	}

	/**
	 * Get all methods which are inherited from the ancestors of a given type, i.e. the non-private methods declared in an ancestor 
 	 * of the given type
	 */
	public List<MethodDefinition> getAllInheritedMethodList(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getAllInheritedMethodList();
		return null;
	}
	
	/**
	 * Get all methods which are inherited from the ancestors of a given type, and is not re-defined (overridden) by
	 * a method declared in the given type 
	 */
	public List<MethodDefinition> getInheritedMethods(DetailedTypeDefinition aType) {
		List<MethodDefinition> resultList = new ArrayList<MethodDefinition>();
		List<MethodDefinition> ancestorMethodList = getAllInheritedMethodList(aType);
		if (ancestorMethodList == null) return resultList;

		List<MethodDefinition> typeMethods = aType.getMethodList();
		
		for (MethodDefinition ancestorMethod : ancestorMethodList) {
			boolean hasBeenOverridden = false;
			if (typeMethods != null) {
				for (MethodDefinition thisTypeMethod : typeMethods) {
					if (thisTypeMethod.isOverrideMethod(ancestorMethod)) {
						hasBeenOverridden = true;
						break;
					}
				}
			}
			// Add the non-overridden method inherited from the ancestor type to the result list
			if (!hasBeenOverridden) resultList.add(ancestorMethod);
		}
	
		return resultList;
	}

	/**
	 * Get all methods which are declared in the given type and override a method inherited from the ancestors of a given type
	 */
	public List<MethodDefinition> getOverriddenMethods(DetailedTypeDefinition aType) {
		List<MethodDefinition> resultList = new ArrayList<MethodDefinition>();
		List<MethodDefinition> typeMethods = aType.getMethodList();
		if (typeMethods == null) return resultList;
		
		List<MethodDefinition> ancestorMethodList = getAllInheritedMethodList(aType);
		if (ancestorMethodList == null) return resultList;

		for (MethodDefinition thisTypeMethod : typeMethods) {
			boolean hasBeenOverridden = false;
			for (MethodDefinition ancestorMethod : ancestorMethodList) {
				if (thisTypeMethod.isOverrideMethod(ancestorMethod)) {
					hasBeenOverridden = true;
					break;
				}
			}
			// Add the method declared in the given type and overridden a method inherited from the ancestor type to the result list
			if (hasBeenOverridden) resultList.add(thisTypeMethod);
		}
	
		return resultList;
	}

	/**
	 * Get all methods which are declared in the given type and non-override a method inherited from the ancestors of a given type
	 */
	public List<MethodDefinition> getNewMethods(DetailedTypeDefinition aType) {
		List<MethodDefinition> resultList = new ArrayList<MethodDefinition>();
		List<MethodDefinition> typeMethods = aType.getMethodList();
		if (typeMethods == null) return resultList;
		
		List<MethodDefinition> ancestorMethodList = getAllInheritedMethodList(aType);
		if (ancestorMethodList == null) return resultList;

		for (MethodDefinition thisTypeMethod : typeMethods) {
			boolean hasBeenOverridden = false;
			for (MethodDefinition ancestorMethod : ancestorMethodList) {
				if (thisTypeMethod.isOverrideMethod(ancestorMethod)) {
					hasBeenOverridden = true;
					break;
				}
			}
			// Add the method declared in the given type and non-overridden a method inherited from the ancestor type to the result list
			if (!hasBeenOverridden) resultList.add(thisTypeMethod);
		}
		return resultList;
	}
	
	/**
	 * Get those methods which are implemented and public in the given type
	 */
	public List<MethodDefinition> getImplementedPublicMethodList(DetailedTypeDefinition aType) {
		List<MethodDefinition> resultList = new ArrayList<MethodDefinition>();
		List<MethodDefinition> implementedMethodList = getImplementedMethodList(aType);;
		if (implementedMethodList == null) return resultList;
		
		// Add the implemented and public method in the given type to the result method list
		for (MethodDefinition method : implementedMethodList) {
			if (method.isPublic()) resultList.add(method);
		}
		return resultList;
	}

	/**
	 * Get those methods which are declared and public of the given type
	 */
	public List<MethodDefinition> getDeclaredPublicMethodList(DetailedTypeDefinition aType) {
		List<MethodDefinition> resultList = new ArrayList<MethodDefinition>();
		List<MethodDefinition> declaredMethodList = getDeclaredMethodList(aType);
		if (declaredMethodList == null) return resultList;
		
		// Add the declared and public method in the given type to the result method list
		for (MethodDefinition method : declaredMethodList) {
			if (method.isPublic()) resultList.add(method);
		}
		return resultList;
	}
	
	/**
	 * Get those methods which are public of the given type, included inherited methods.
	 */
	public List<MethodDefinition> getPublicMethodList(DetailedTypeDefinition aType) {
		List<MethodDefinition> resultList = new ArrayList<MethodDefinition>();

		List<MethodDefinition> implementedMethodList = getImplementedMethodList(aType);;
		if (implementedMethodList != null) {
			// Add the implemented and public method in the given type to the result method list
			for (MethodDefinition method : implementedMethodList) {
				if (method.isPublic()) resultList.add(method);
			}
		}
		
		List<MethodDefinition> declaredMethodList = getDeclaredMethodList(aType);;
		if (declaredMethodList != null) {
			// Add the declared and public method in the given type to the result method list
			for (MethodDefinition method : declaredMethodList) {
				if (method.isPublic()) resultList.add(method);
			}
		}
		
		return resultList;
	}

	/**
	 * Get those methods which are non-public of the given type, included inherited methods.
	 */
	public List<MethodDefinition> getNonPublicMethodList(DetailedTypeDefinition aType) {
		List<MethodDefinition> resultList = new ArrayList<MethodDefinition>();

		List<MethodDefinition> implementedMethodList = getImplementedMethodList(aType);;
		if (implementedMethodList != null) {
			// Add the implemented and non-public method in the given type to the result method list
			for (MethodDefinition method : implementedMethodList) {
				if (!method.isPublic()) resultList.add(method);
			}
		}
		
		List<MethodDefinition> declaredMethodList = getDeclaredMethodList(aType);;
		if (declaredMethodList != null) {
			// Add the declared and non-public method in the given type to the result method list
			for (MethodDefinition method : declaredMethodList) {
				if (!method.isPublic()) resultList.add(method);
			}
		}
		return resultList;
	}
	
	public List<MethodDefinition> getAllMethodsInDescendants(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getAllMethodsInDescendants();
		return null;
	}
	
	/**
	 * Get the fields declared in the given type
	 */
	public List<FieldDefinition> getImplementedFieldList(DetailedTypeDefinition aType) {
		return aType.getFieldList();
	}

	/**
	 * Get the fields inherited from an ancestor of  the given type, those fields is non-static and non-private in the ancestor
	 * Note that the static and private fields can not be inherited.
	 */
	public List<FieldDefinition> getDeclaredFieldList(DetailedTypeDefinition aType) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.getWithoutUsedTypeSet(aType);
		if (typeStructEntry != null) return typeStructEntry.getAllInheritedFieldList();
		return null;
	}
	
	/**
	 * Get all fields of the given type, include the fields declared in the given type, and those fields inherited from ancestors
	 * Note that the static and private fields can not be inherited.
	 */
	public List<FieldDefinition> getAllFieldList(DetailedTypeDefinition aType) {
		List<FieldDefinition> resultList = new ArrayList<FieldDefinition>();
		List<FieldDefinition> implementedFields = getImplementedFieldList(aType);
		if (implementedFields != null) resultList.addAll(implementedFields);
		
		List<FieldDefinition> declaredFields = getDeclaredFieldList(aType);
		if (declaredFields != null) resultList.addAll(declaredFields);
		return resultList;
	}
	
	/**
	 * Get all methods which are directly static called by the given method
	 */
	public List<MethodDefinition> getDirectStaticInvocationMethodList(MethodDefinition method) {
		if (method.isAbstract()) return null;

		MethodStructEntry methodStructEntry = methodStructManager.get(method);
		if (methodStructEntry != null) return methodStructEntry.getStaticInvocationMethodList();
		return null;
	}
	
	/**
	 * Get all methods with information on call number which are directly static called by the given method
	 */
	public List<MethodWithCallInformation> getDirectStaticInvocationMethodWithCallInformationList(MethodDefinition method) {
		if (method.isAbstract()) return null;

		MethodStructEntry methodStructEntry = methodStructManager.get(method);
		if (methodStructEntry != null) return methodStructEntry.getStaticInvocationMethodWithCallInformationList();
		return null;
	}
	
	/**
	 * Get all methods which are directly polymorphically called by the given method
	 */
	public List<MethodDefinition> getDirectPolymorphicInvocationMethodList(MethodDefinition method) {
		if (method.isAbstract()) return null;

		MethodStructEntry methodStructEntry = methodStructManager.get(method);
		if (methodStructEntry != null) return methodStructEntry.getPolymorphicInvocationMethodList();
		return null;
	}
	
	/**
	 * Get all methods with information on call number which are directly static called by the given method
	 */
	public List<MethodWithCallInformation> getDirectPolymorphicInvocationMethodWithCallInformationList(MethodDefinition method) {
		if (method.isAbstract()) return null;

		MethodStructEntry methodStructEntry = methodStructManager.get(method);
		if (methodStructEntry != null) return methodStructEntry.getPolymorphicInvocationMethodWithCallInformationList();
		return null;
	}
	
	/**
	 * Get all methods which are directly or indirectly static called by the given method
	 * Since we always need to remove replicative methods in the return set, so we use a set rather than a list
	 * for the return value. This method use TreeSet to store the return value, which speed up the search of the
	 * replicative methods.
	 */
	public Set<MethodDefinition> getStaticInvocationMethodSet(MethodDefinition method) {
		Set<MethodDefinition> resultSet = new TreeSet<MethodDefinition>();
		List<MethodDefinition> directCalleeList = getDirectStaticInvocationMethodList(method);

		// Use a queue to get all methods which are directly or indirectly called by the given method
		LinkedList<MethodDefinition> calleeQueue = new LinkedList<MethodDefinition>();
		resultSet.addAll(directCalleeList);
		calleeQueue.addAll(directCalleeList);
		
		while (!calleeQueue.isEmpty()) {
			MethodDefinition calleeMethod = calleeQueue.removeFirst();
			directCalleeList = getDirectStaticInvocationMethodList(calleeMethod);
			if (directCalleeList != null) {
				for (MethodDefinition callee : directCalleeList) {
					if (!resultSet.contains(callee)) {
						resultSet.add(callee);
						calleeQueue.addLast(callee);
					}
				}
			}
		}
		return resultSet;
	}

	/**
	 * Get all methods which are directly or indirectly static called by the given method
	 * Since we always need to remove replicative methods in the return set, so we use a set rather than a list
	 * for the return value. This method use TreeSet to store the return value, which speed up the search of the
	 * replicative methods.
	 */
	public Set<MethodDefinition> getStaticInvocationMethodSet(MethodDefinition method, int maxCallDepth) {
		if (maxCallDepth <= 0) return getStaticInvocationMethodSet(method);
		
		Set<MethodDefinition> resultSet = new TreeSet<MethodDefinition>();
		List<MethodDefinition> directCalleeList = getDirectStaticInvocationMethodList(method);

		// Use a queue to get all methods which are directly or indirectly called by the given method
		LinkedList<MethodWithCallInformation> calleeQueue = new LinkedList<MethodWithCallInformation>();
		resultSet.addAll(directCalleeList);
		for (MethodDefinition callee : directCalleeList) {
			calleeQueue.add(new MethodWithCallInformation(callee, 1));
		}
		
		while (!calleeQueue.isEmpty()) {
			MethodWithCallInformation methodWithCallInformation = calleeQueue.removeFirst();
			// We use call number in MethodWithCallInformation to record the call depth
			int callDepth = methodWithCallInformation.getCallNumber(); 
			if (callDepth >= maxCallDepth) continue;
			MethodDefinition calleeMethod = methodWithCallInformation.getMethod();
			
			directCalleeList = getDirectStaticInvocationMethodList(calleeMethod);
			if (directCalleeList != null) {
				for (MethodDefinition callee : directCalleeList) {
					if (!resultSet.contains(callee)) {
						resultSet.add(callee);
						calleeQueue.addLast(new MethodWithCallInformation(callee, callDepth+1));
					}
				}
			}
		}
		return resultSet;
	}
	
	/**
	 * Get the set of all methods which are directly or indirectly polymorphically called by the given method.
	 * Since we always need to remove replicative methods in the return set, so we use a set rather than a list
	 * for the return value. This method use TreeSet to store the return value, which speed up the search of the
	 * replicative methods.
	 */
	public Set<MethodDefinition> getPolymorphicInvocationMethodSet(MethodDefinition method) {
		Set<MethodDefinition> resultSet = new TreeSet<MethodDefinition>();

		// Use a queue to get all methods which are directly or indirectly called by the given method
		LinkedList<MethodDefinition> calleeQueue = new LinkedList<MethodDefinition>();
		
		List<MethodDefinition> directCalleeList = getDirectPolymorphicInvocationMethodList(method);
		if (directCalleeList == null) return resultSet;
		
		resultSet.addAll(directCalleeList);
		calleeQueue.addAll(directCalleeList);
		
		while (!calleeQueue.isEmpty()) {
			MethodDefinition calleeMethod = calleeQueue.removeFirst();
			directCalleeList = getDirectPolymorphicInvocationMethodList(calleeMethod);
			if (directCalleeList != null) {
				for (MethodDefinition callee : directCalleeList) {
					if (!resultSet.contains(callee)) {
						resultSet.add(callee);
						if (!callee.isAbstract()) calleeQueue.addLast(callee);
					}
				}
			}
		}
		return resultSet;
	}
	
	/**
	 * Get all methods which are directly or indirectly static called by the given method
	 * Since we always need to remove replicative methods in the return set, so we use a set rather than a list
	 * for the return value. This method use TreeSet to store the return value, which speed up the search of the
	 * replicative methods.
	 */
	public Set<MethodDefinition> getPolymorphicInvocationMethodSet(MethodDefinition method, int maxCallDepth) {
		if (maxCallDepth <= 0) return getPolymorphicInvocationMethodSet(method);
		
		Set<MethodDefinition> resultSet = new TreeSet<MethodDefinition>();
		List<MethodDefinition> directCalleeList = getDirectPolymorphicInvocationMethodList(method);

		// Use a queue to get all methods which are directly or indirectly called by the given method
		LinkedList<MethodWithCallInformation> calleeQueue = new LinkedList<MethodWithCallInformation>();
		resultSet.addAll(directCalleeList);
		for (MethodDefinition callee : directCalleeList) {
			calleeQueue.add(new MethodWithCallInformation(callee, 1));
		}
		
		while (!calleeQueue.isEmpty()) {
			MethodWithCallInformation methodWithCallInformation = calleeQueue.removeFirst();
			// We use call number in MethodWithCallInformation to record the call depth
			int callDepth = methodWithCallInformation.getCallNumber(); 
			if (callDepth >= maxCallDepth) continue;
			MethodDefinition calleeMethod = methodWithCallInformation.getMethod();
			
			directCalleeList = getDirectPolymorphicInvocationMethodList(calleeMethod);
			if (directCalleeList != null) {
				for (MethodDefinition callee : directCalleeList) {
					if (!resultSet.contains(callee)) {
						resultSet.add(callee);
						calleeQueue.addLast(new MethodWithCallInformation(callee, callDepth+1));
					}
				}
			}
		}
		return resultSet;
	}

	/**
	 *	Return the response set of the class, i.e. the set of all methods of the class (including inherited methods), 
	 *	and the methods directly OR INDIRECTLY and polymorphically called by the methods of the class.
	 */
	public Set<MethodDefinition> getResponseSet(DetailedTypeDefinition type) {
		Set<MethodDefinition> resultSet = new TreeSet<MethodDefinition>();
		if (type.isInterface()) return resultSet;

		List<MethodDefinition> methodList = getAllMethodList(type);
		if (methodList == null) return resultSet;
		
		// Use a queue to get all methods which are directly or indirectly called by the given method
		LinkedList<MethodDefinition> calleeQueue = new LinkedList<MethodDefinition>();
		for (MethodDefinition method : methodList) {
			if (!resultSet.contains(method)) {
				resultSet.add(method);
				if (!method.isAbstract()) calleeQueue.add(method);
			}
		}
		
		while (!calleeQueue.isEmpty()) {
			MethodDefinition calleeMethod = calleeQueue.removeFirst();
			methodList = getDirectPolymorphicInvocationMethodList(calleeMethod);
			if (methodList != null) {
				for (MethodDefinition callee : methodList) {
					if (!resultSet.contains(callee)) {
						resultSet.add(callee);
						if (!callee.isAbstract()) calleeQueue.addLast(callee);
					}
				}
			}
		}
		return resultSet;
	}

	/**
	 * Given the number of calls which the callee is directly called by the caller. 
	 */
	public int getDirectStaticInvocationNumber(MethodDefinition caller, MethodDefinition callee) {
		if (caller.isAbstract()) return 0;
		
		MethodStructEntry methodStructEntry = methodStructManager.get(caller);
		if (methodStructEntry != null) return methodStructEntry.getDirectStaticInvocationNumber(callee);
		return 0;
	}
	
	/**
	 * Given the number of calls which the callee is directly polymorphically called by the caller. 
	 */
	public int getDirectPolymorphicInvocationNumber(MethodDefinition caller, MethodDefinition callee) {
		if (caller.isAbstract()) return 0;

		MethodStructEntry methodStructEntry = methodStructManager.get(caller);
		if (methodStructEntry != null) return methodStructEntry.getDirectPolymorphicInvocationNumber(callee);
		return 0;
	}
	
	/**
	 * Get all parameters of the given method 
	 */
	public List<VariableDefinition> getParameterList(MethodDefinition method) {
		List<VariableDefinition> resultList = new ArrayList<VariableDefinition>();
		List<VariableDefinition> parameters = method.getParameterList();
		if (parameters != null) {
			if (parameters.size() > 0) resultList.addAll(parameters);
		}
		return resultList;
	}
	
	/**
	 * Get all types occurring in the parameters of the given method
	 */
	public List<TypeDefinition> getParameterTypeList(MethodDefinition method) {
		MethodStructEntry methodStructEntry = methodStructManager.get(method);
		if (methodStructEntry != null) return methodStructEntry.getParameterTypeList();
		return null;
	}
	
	/**
	 * Get all types occurring in the parameters of implemented methods in given type
	 */
	public List<TypeDefinition> getParameterTypeList(DetailedTypeDefinition type) {
		List<TypeDefinition> resultList = new ArrayList<TypeDefinition>();
		List<MethodDefinition> methodList = getImplementedMethodList(type);
		if (methodList == null) return resultList;
		for (MethodDefinition method : methodList) {
			List<TypeDefinition> parameterTypeList = getParameterTypeList(method);
			if (parameterTypeList != null) {
				for (TypeDefinition paraType : parameterTypeList) {
					if (!resultList.contains(paraType)) resultList.add(paraType);
				}
			}
		}
		return resultList; 
	}
	
	/**
	 * Get all types occurring in fields of the given type
	 */
	public List<TypeDefinition> getFieldTypeList(DetailedTypeDefinition type) {
		List<TypeDefinition> resultList = new ArrayList<TypeDefinition>();
		
		List<FieldDefinition> fieldList = getAllFieldList(type);;
		for (FieldDefinition field : fieldList) {
			List<TypeDefinition> fieldTypeList = field.getTypeDefinition(true);
			if (fieldTypeList != null) {
				for (TypeDefinition fieldType : fieldTypeList) {
					if(!resultList.contains(fieldType)) resultList.add(fieldType);
				}
			}
		}
		
		return resultList;
	}

	/**
	 * Get all types occurring in implemented fields (i.e. non-inherited field) of the given type
	 */
	public List<TypeDefinition> getImplementedFieldTypeList(DetailedTypeDefinition type) {
		List<TypeDefinition> resultList = new ArrayList<TypeDefinition>();
		
		List<FieldDefinition> fieldList = getImplementedFieldList(type);
		if (fieldList == null) return resultList;
		for (FieldDefinition field : fieldList) {
			List<TypeDefinition> fieldTypeList = field.getTypeDefinition(true);
			if (fieldTypeList != null) {
				for (TypeDefinition fieldType : fieldTypeList) {
					if(!resultList.contains(fieldType)) resultList.add(fieldType);
				}
			}
		}
		return resultList;
	}
	
	/**
	 * Get directly field (all fields, i.e. including inherited fields, of the class of the given method) references 
	 * of the given method. 
	 */
	public List<FieldDefinition> getDirectFieldUsingList(MethodDefinition method) {
		if (method.isAbstract()) return null;
		
		MethodStructEntry methodStructEntry = methodStructManager.get(method);
		if (methodStructEntry != null) return methodStructEntry.getFieldUsingList();
		return null;
	}

	/**
	 * Get directly field (implemented fields, i.e. non-inherited fields, of the class of the given method) references 
	 * of the given method. 
	 */
	public List<FieldDefinition> getDirectImplementedFieldReferencesInMethod(MethodDefinition method) {
		if (method.isAbstract()) return new ArrayList<FieldDefinition>();

		DetailedTypeDefinition type = tableManager.getEnclosingDetailedTypeDefinition(method);
		List<FieldDefinition> fieldUsingList = getDirectFieldUsingList(method);
		List<FieldDefinition> implementedFieldList = getImplementedFieldList(type);
		
		if (fieldUsingList == null || implementedFieldList == null) return null;
		List<FieldDefinition> resultList = new ArrayList<FieldDefinition>();
		for (FieldDefinition fieldInUsingList : fieldUsingList) {
			if (implementedFieldList.contains(fieldInUsingList)) resultList.add(fieldInUsingList);
		}
		return resultList;
	}
	
	/**
	 * Create and return the given kind of (implemented) method invocation matrix for a class
	 */
	protected MethodInvocationMatrix createMethodInvocationMatrix(DetailedTypeDefinition aClass, boolean forDirect, boolean forStatic) {
		if (forStatic == true) {
			if (forDirect == true) return createDirectStaticMethodInvocationMatrix(aClass);
			else return createIndirectStaticMethodInvocationMatrix(aClass);
		} else {
			if (forDirect == true) return createDirectPolymorphicMethodInvocationMatrix(aClass);
			else return createIndirectPolymorphicMethodInvocationMatrix(aClass);
		}
	}

	/**
	 * Create a matrix to represent the direct and static invocation relations between implemented methods in the given class
	 */
	public MethodInvocationMatrix createDirectStaticMethodInvocationMatrix(DetailedTypeDefinition aClass) {
		List<MethodDefinition> methodList = getImplementedMethodList(aClass);
		if (methodList == null) return null;
		
		MethodInvocationMatrix resultMatrix = new MethodInvocationMatrix(aClass, true, true);
		resultMatrix.setMethodList(methodList);
		
		for (MethodDefinition caller : methodList) {
			MethodStructEntry methodStructEntry = methodStructManager.get(caller);
			List<MethodDefinition> calleeList = methodStructEntry.getStaticInvocationMethodListInRange(methodList);
			if (calleeList != null) {
				for (MethodDefinition callee : calleeList) {
					resultMatrix.setInvocationRelation(caller, callee);
				}
			}
		}
		return resultMatrix;
	}

	/**
	 * Create a matrix to represent the direct and polymorphic invocation relations between implemented methods in the given class
	 */
	public MethodInvocationMatrix createDirectPolymorphicMethodInvocationMatrix(DetailedTypeDefinition aClass) {
		List<MethodDefinition> methodList = getImplementedMethodList(aClass);
		if (methodList == null) return null;
		
		MethodInvocationMatrix resultMatrix = new MethodInvocationMatrix(aClass, true, false);
		resultMatrix.setMethodList(methodList);
		
		for (MethodDefinition caller : methodList) {
			MethodStructEntry methodStructEntry = methodStructManager.get(caller);
			List<MethodDefinition> calleeList = methodStructEntry.getPolymorphicInvocationMethodListInRange(methodList);
			if (calleeList != null) {
				for (MethodDefinition callee : calleeList) {
					resultMatrix.setInvocationRelation(caller, callee);
				}
			}
		}
		return resultMatrix;
	}
	
	/**
	 * Create a matrix to represent the indirect and static invocation relations between implemented methods in the given class. 
	 */
	public MethodInvocationMatrix createIndirectStaticMethodInvocationMatrix(DetailedTypeDefinition aClass) {
		List<MethodDefinition> methodList = getImplementedMethodList(aClass);
		if (methodList == null) return null;
		
		MethodInvocationMatrix resultMatrix = new MethodInvocationMatrix(aClass, false, true);
		resultMatrix.setMethodList(methodList);
		
		for (MethodDefinition method : methodList) {
			Set<MethodDefinition> calledMethodSet = getStaticInvocationMethodSet(method);
			for (MethodDefinition calledMethod : methodList) {
				if (calledMethodSet.contains(calledMethod)) resultMatrix.setInvocationRelation(method, calledMethod);
			}
		}
		return resultMatrix;
	}

	/**
	 * Create a matrix to represent the indirect and polymorphic invocation relations between implemented methods in the given class. 
	 */
	public MethodInvocationMatrix createIndirectPolymorphicMethodInvocationMatrix(DetailedTypeDefinition aClass) {
		List<MethodDefinition> methodList = getImplementedMethodList(aClass);
		if (methodList == null) return null;
		
		MethodInvocationMatrix resultMatrix = new MethodInvocationMatrix(aClass, false, false);
		resultMatrix.setMethodList(methodList);
		
		for (MethodDefinition method : methodList) {
			Set<MethodDefinition> calledMethodSet = getPolymorphicInvocationMethodSet(method);
			for (MethodDefinition calledMethod : methodList) {
				if (calledMethodSet.contains(calledMethod)) resultMatrix.setInvocationRelation(method, calledMethod);
			}
		}
		return resultMatrix;
	}
	
	
	/**
	 * Create and return the given kind of (implemented) method (implemented) field reference matrix for a class
	 * Note that we only use static (i.e. non-polymorphic) method invocation to calculate indirect field reference.
	 */
	protected FieldReferenceMatrix createFieldReferenceMatrix(DetailedTypeDefinition aClass, boolean forDirect) {
		FieldReferenceMatrix resultMatrix = createDirectFieldReferenceMatrix(aClass);
		
		if (forDirect == true) return resultMatrix;
		if (resultMatrix == null) return null;

		MethodInvocationMatrix invocationMatrix = createIndirectStaticMethodInvocationMatrix(aClass);
		int invocationMatrixLength = invocationMatrix.getMatrixLength();
		int referenceMatrixRowLength = resultMatrix.getRowLength();
		int referenceMatrixColLength = resultMatrix.getColLength();
		int[][] indirectReferenceMatrix = getReferenceTransitiveClosure(resultMatrix.getMatrix(), referenceMatrixRowLength, referenceMatrixColLength, invocationMatrix.getMatrix(), invocationMatrixLength);
		resultMatrix.setMatrix(indirectReferenceMatrix);
		return resultMatrix;
	}
	
	/**
	 * Create and return the direct (implemented) method (implemented) field reference matrix for a class
	 * Note that we only use static (i.e. non-polymorphic) method invocation to calculate indirect field reference.
	 */
	public FieldReferenceMatrix createDirectFieldReferenceMatrix(DetailedTypeDefinition aClass) {
 		List<MethodDefinition> methodList = getImplementedMethodList(aClass);
		List<FieldDefinition> fieldList = getAllFieldList(aClass);
		if (methodList == null || fieldList == null) return null;
		
		FieldReferenceMatrix resultMatrix = new FieldReferenceMatrix(aClass, true);
		resultMatrix.setMethodList(methodList);
		resultMatrix.setFieldList(fieldList);
		
		for (MethodDefinition method : methodList) {
			MethodStructEntry methodStructEntry = methodStructManager.get(method);
			List<FieldDefinition> referenceList = methodStructEntry.getFieldUsingListInRange(fieldList);
			if (referenceList != null) {
				for (FieldDefinition referredField : referenceList) {
					resultMatrix.setReferenceRelation(method, referredField);
				}
			}
		}
		return resultMatrix;
	}
	
	/**
	 * Create and return the indirect (implemented) method (implemented) field reference matrix for a class
	 * Note that we only use static (i.e. non-polymorphic) method invocation to calculate indirect field reference.
	 */
	public FieldReferenceMatrix createIndirectFieldReferenceMatrix(DetailedTypeDefinition aClass) {
		return createFieldReferenceMatrix(aClass, false);
	}

	/**
	 * Create and return the given kind of (implemented) method (implemented) field type reference matrix for a class
	 * Say a method and a field has type reference relation, if the type of field is the type of a parameter of the method or 
	 * is the return type of the method.
	 * Note that we only use static (i.e. non-polymorphic) method invocation to calculate indirect field reference.
	 */
	protected FieldReferenceMatrix createFieldTypeReferenceMatrix(DetailedTypeDefinition aClass, boolean forDirect) {
		FieldReferenceMatrix resultMatrix = createDirectFieldTypeReferenceMatrix(aClass);
		
		if (forDirect == true) return resultMatrix;
		if (resultMatrix == null) return null;

		MethodInvocationMatrix invocationMatrix = createIndirectStaticMethodInvocationMatrix(aClass);
		int invocationMatrixLength = invocationMatrix.getMatrixLength();
		int referenceMatrixRowLength = resultMatrix.getRowLength();
		int referenceMatrixColLength = resultMatrix.getColLength();
		int[][] indirectReferenceMatrix = getReferenceTransitiveClosure(resultMatrix.getMatrix(), referenceMatrixRowLength, referenceMatrixColLength, invocationMatrix.getMatrix(), invocationMatrixLength);
		resultMatrix.setMatrix(indirectReferenceMatrix);
		return resultMatrix;
	}
	
	/**
	 * Create and return direct (implemented) method (implemented) field type reference matrix for a class
	 * Say a method and a field has type reference relation, if the type of field is the type of a parameter of the method or 
	 * is the return type of the method.
	 * Note that we only use static (i.e. non-polymorphic) method invocation to calculate indirect field reference.
	 */
	public FieldReferenceMatrix createDirectFieldTypeReferenceMatrix(DetailedTypeDefinition aClass) {
 		List<MethodDefinition> methodList = getImplementedMethodList(aClass);
		List<FieldDefinition> fieldList = getAllFieldList(aClass);
		if (methodList == null || fieldList == null) return null;

		FieldReferenceMatrix resultMatrix = new FieldReferenceMatrix(aClass, true);
		resultMatrix.setMethodList(methodList);
		resultMatrix.setFieldList(fieldList);
		for (MethodDefinition method : methodList) {
			List<TypeDefinition> methodTypeList = new ArrayList<TypeDefinition>();
			TypeDefinition returnType = method.getReturnTypeDefinition();
			if (returnType != null) methodTypeList.add(returnType);
			List<TypeDefinition> paraTypeList = getParameterTypeList(method);
			if (paraTypeList != null) methodTypeList.addAll(paraTypeList);
			
			for (FieldDefinition field : fieldList) {
				List<TypeDefinition> fieldTypeList = field.getTypeDefinition(true);
				
				// Is there any type in methodTypeList equal to type in fieldTypeList?
				// If yes, the method and the field have type reference relation
				boolean hasRelation = false;   
				for (TypeDefinition methodType : methodTypeList) {
					for (TypeDefinition fieldType : fieldTypeList) {
						if (fieldType == methodType) {
							hasRelation = true;
							break;
						}
					}
					if (hasRelation) break;
				}
				if (hasRelation) {
					resultMatrix.setReferenceRelation(method, field);
				}
			}
		}
		
		return resultMatrix;
	}
	
	/**
	 * Create and return indirect (implemented) method (implemented) field type reference matrix for a class
	 * Say a method and a field has type reference relation, if the type of field is the type of a parameter of the method or 
	 * is the return type of the method.
	 * Note that we only use static (i.e. non-polymorphic) method invocation to calculate indirect field reference.
	 */
	public FieldReferenceMatrix createIndirectFieldTypeReferenceMatrix(DetailedTypeDefinition aClass) {
		return createFieldTypeReferenceMatrix(aClass, false);
	}
	
	/**
	 * Create and return the given kind of (implemented) method (implemented) method-method reference matrix for a class. 
	 * A method-method reference matrix record whether two method (directly or indirectly) refer to a common field.
	 * Note that we only use static (i.e. non-polymorphic) method invocation to calculate indirect field reference.
	 */
	protected MethodReferenceMatrix createMethodReferenceMatrix(DetailedTypeDefinition aClass, boolean forDirect) {
		FieldReferenceMatrix fieldRefMatrix = createFieldReferenceMatrix(aClass, forDirect);
		if (fieldRefMatrix == null) return null;
		
		MethodReferenceMatrix resultMatrix = new MethodReferenceMatrix(aClass, forDirect);
		List<MethodDefinition> methodList = fieldRefMatrix.getMethodList();
		resultMatrix.setMethodList(methodList);
		int methodLength = methodList.size();
		int[][] matrix = new int[methodLength][methodLength];
		
		List<FieldDefinition> fieldList = fieldRefMatrix.getFieldList();
		for (int row = 0; row < methodLength; row++) {
			for (int col = 0; col < methodLength; col++) {
				// We do not set the diagonal elements, i.e. we assume that it is not meaningful for 
				// the method and itself refer to a common field (this is always be true for any method)
				if (row == col) continue;		  
				MethodDefinition rowMethod = methodList.get(row);
				MethodDefinition colMethod = methodList.get(col);
				for (FieldDefinition field : fieldList) {
					if (fieldRefMatrix.hasReferenceRelation(rowMethod, field) && fieldRefMatrix.hasReferenceRelation(colMethod, field)) {
						// Both the rowMethod and the colMethod refer to the field 
						matrix[row][col] = 1;
						break;
					}
				}
			}
		}
		resultMatrix.setMatrix(matrix);
		return resultMatrix;
	}

	public MethodReferenceMatrix createDirectMethodReferenceMatrix(DetailedTypeDefinition aClass) {
		return createMethodReferenceMatrix(aClass, true);
	}

	public MethodReferenceMatrix createIndirectMethodReferenceMatrix(DetailedTypeDefinition aClass) {
		return createMethodReferenceMatrix(aClass, false);
	}
	
	/**
	 * Use Warshall algorithm to calculate the transitive closure of a length x length matrix. 
	 */
	public int[][] getTransitiveClosure(int[][] matrix, int length) {
		int[][] result = new int[length][length];

		// Initialize the result matrix to the input matrix
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) result[i][j] = matrix[i][j];
		}
		
		// calculate the transitive closure, please refer to Warshall algorithm.
		for (int k = 0; k < length; k++) {
			for (int i = 0; i < length; i++) {
				for (int j = 0; j < length; j++) {
					if (result[i][k] > 0 && result[k][j] > 0) result[i][j] = 1;  
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Use the invocation relationship between methods (stored in the matrix : invocationMatrix) to calculate indirect method-field 
	 * reference relation. Say the $i$th method and the $j$th field has indirect field reference, if referenceMatrix[i][j] == 1, or
	 * exists k, invocationMatrix[i][k] == 1 and referenceMatrix[k][j] == 1.  
	 * The caller of the method must confirm that the parameters about the lengths of the matrix passed into the method are correctly! 
	 */
	private int[][] getReferenceTransitiveClosure(int[][] referenceMatrix, int referenceMatrixRowLength, int referenceMatrixColLength, int[][] invocationMatrix, int invocationMatrixLength) {
		int[][] result = new int[referenceMatrixRowLength][referenceMatrixColLength];
		
		for (int i = 0; i < referenceMatrixRowLength; i++) {
			for (int j = 0; j < referenceMatrixColLength; j++) {
				if (referenceMatrix[i][j] == 1) result[i][j] = 1;
				else {
					for (int k = 0; k < invocationMatrixLength; k++) {
						if (invocationMatrix[i][k] == 1 && referenceMatrix[k][j] == 1) result[i][j] = 1;
						else result[i][j] = 0;
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Test whether a class (detailed type) one uses a class two, where using relation defined as in metric CBO: 
	 * 		Say class one use class two, if there exists a method m implemented in class one, and m directly call a method
	 * 		in class two, or reference a field of class two.  
	 */
	public boolean hasUsingRelation(DetailedTypeDefinition one, DetailedTypeDefinition two) {
		if (one == two) return false;
		
		DetailedTypeStructEntry typeStructEntry = typeStructManager.get(one);
		return typeStructEntry.hasUsedType(two);
	}
	
	/**
	 * Return the total number of detailed type definitions using the given type or used by the given type
	 */
	public int getUsingDetailedTypeNumber(DetailedTypeDefinition currentType) {
		int result = 0;
		Set<DetailedTypeDefinition> typeSet = typeStructManager.getDetailedTypeDefinitionSet();
		for (DetailedTypeDefinition type : typeSet) {
			DetailedTypeStructEntry typeStructEntry = typeStructManager.get(type);
			if (type == currentType) result = result + typeStructEntry.getUsedTypeNumber();
			else if (typeStructEntry.hasUsedType(currentType)) result = result + 1;
		}
		return result;
	}

	/**
	 * Return the total number of other detailed type definitions (i.e. not the ancestor or descendant of the given type) 
	 * using the given type or used by the given type
	 */
	public int getUsingOtherDetailedTypeNumber(DetailedTypeDefinition currentType) {
		int result = 0;
		DetailedTypeStructEntry typeStructEntry = typeStructManager.get(currentType);
		Set<DetailedTypeDefinition> typeSet = typeStructManager.getDetailedTypeDefinitionSet();
		for (DetailedTypeDefinition type : typeSet) {
			if (type != currentType) {
				if (typeStructEntry.hasUsedType(type) && 
						!hasInheritanceTypeRelation(type, currentType) && 
						!hasInheritanceTypeRelation(currentType, type)) result = result + 1;
			}
		}
		
		for (DetailedTypeDefinition type : typeSet) {
			if (type != currentType) {
				typeStructEntry = typeStructManager.get(type);
				if (typeStructEntry.hasUsedType(currentType) && 
						!hasInheritanceTypeRelation(type, currentType) && 
						!hasInheritanceTypeRelation(currentType, type)) result = result + 1;
			}
		}
		return result;
	}
	
	/**
	 * Return the total number of detailed type definitions used by the give type
	 */
	public int getImportUsingDetailedTypeNumber(DetailedTypeDefinition type) {
		DetailedTypeStructEntry typeStructEntry = typeStructManager.get(type);
		return typeStructEntry.getUsedTypeNumber();
	}

	/**
	 * Return the total number of detailed type definitions using the give type
	 */
	public int getExportUsingDetailedTypeNumber(DetailedTypeDefinition currentType) {
		int result = 0;
		Set<DetailedTypeDefinition> typeSet = typeStructManager.getDetailedTypeDefinitionSet();
		for (DetailedTypeDefinition type : typeSet) {
			if (type != currentType) {
				DetailedTypeStructEntry typeStructEntry = typeStructManager.get(type);
				if (typeStructEntry.hasUsedType(currentType)) result = result + 1;
			}
		}
		return result;
	}
	
	/**
	 * Return a matrix to indicate the using relations between detailed types (i.e. the detailed type 
	 * corresponds to the row use the detailed type corresponds to the column). 
	 * <p> Note that the matrix is possibly not a symmetric matrix.  
	 */
	public int[][] getDetailedTypeUsingMatrix() {
		Set<DetailedTypeDefinition> typeSet = typeStructManager.getDetailedTypeDefinitionSet();
		DetailedTypeDefinition[] typeArray = new DetailedTypeDefinition[typeSet.size()];
		int index = 0;
		for (DetailedTypeDefinition type : typeSet) {
			typeArray[index] = type;
			index++;
		}
		int classNumber = typeArray.length;
		int[][] detailedTypeUsingMatrix = new int[classNumber][classNumber];
		for (int i = 0; i < classNumber; i++) {
			DetailedTypeStructEntry typeStructEntry = typeStructManager.get(typeArray[i]);
			for (int j = 0; j < classNumber; j++) {
				if (i != j) {
					if (typeStructEntry.hasUsedType(typeArray[j])) detailedTypeUsingMatrix[i][j] = 1;
					else detailedTypeUsingMatrix[i][j] = 0;
				} else detailedTypeUsingMatrix[i][j] = 0;
			}
		}
		return detailedTypeUsingMatrix;
	}
}

