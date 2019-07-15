package softwareStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.TypeDefinition;

/**
 * A class to store the generated structure of a detailed type definition.
 *  
 * @author Zhou Xiaocong
 * @since 2016年4月1日
 * @version 1.0
 */
class DetailedTypeStructEntry {
	protected DetailedTypeDefinition detailedType = null;
	
	// Buffered the list of methods and fields of the detailed type definition
	protected List<MethodDefinition> implementedMethodList = null;
	// Note that the implemented field can be easily got by the list in the detailed type definition

	// ancestorTypeList includes all ancestor types (class and interface) except parent types of this detailed type
	protected List<DetailedTypeDefinition> ancestorTypeList = null;
	// parentTypeList includes all parent types (class and interface) of this detailed type
	protected List<DetailedTypeDefinition> parentTypeList = null;
	// For a detailed type (class or interface), the depth of inheritance is the maximal length from an ancestor type which parent 
	// is simple type to this type. Note such ancestor type may be not unique!
	protected int depthOfInheritance = 0;
	// For a detailed type (class or interface), the average inherit depth is the average of the average inherit depth of its 
	// detailed type parents, increased by one. If it has no detailed type parents, its average inherit depth is zero! 
	protected double averageInheritDepth = 0;
	
	// descendantTypeList includes all descendant types (class and interface) except children types of this detailed type
	protected List<DetailedTypeDefinition> descendantTypeList = null;
	protected List<DetailedTypeDefinition> childrenTypeList = null;
	protected int classToLeafDepth = 0;
	
	// usedTypeSet includes all detailed types which are used in this detailed type. A detailed type B is used by this detailed 
	// type A, if (1) Given m is a method of A, (i) the return type of m is B, (ii) B is the type of a parameter, a throw exception 
	// or a local variable of m, (iii) m statically and directly calls a method m', and m' is declared in B (even if it is not 
	// implemented by B); (2) Given d is a field of A, (i) the type of d is B, (ii) B is used in the initial expression of d.
	// Note that the usedTypeSet does not include its ancestor types.
	protected TreeSet<DetailedTypeDefinition> usedTypeSet = null;
	
	
	public DetailedTypeStructEntry(DetailedTypeDefinition type) {
		detailedType = type;
	}
	
	public DetailedTypeStructEntry(DetailedTypeStructEntry other) {
		detailedType = other.detailedType;
		implementedMethodList = other.implementedMethodList;
		ancestorTypeList = other.ancestorTypeList;
		parentTypeList = other.parentTypeList;
		depthOfInheritance = other.depthOfInheritance;
		averageInheritDepth = other.averageInheritDepth;
		descendantTypeList = other.descendantTypeList;
		childrenTypeList = other.childrenTypeList;
		classToLeafDepth = other.classToLeafDepth;
		usedTypeSet = other.usedTypeSet;
	}

	public void initializeStructureInformation() {
		implementedMethodList = new ArrayList<MethodDefinition>();
		ancestorTypeList = new ArrayList<DetailedTypeDefinition>();
		parentTypeList = new ArrayList<DetailedTypeDefinition>();
		usedTypeSet  = new TreeSet<DetailedTypeDefinition>();
	}
	
	public boolean hasInitializedStructureInformation() {
		if (implementedMethodList != null) return true;
		else return false;
	}
	
	public DetailedTypeDefinition getType() {
		return detailedType;
	}
	
	
	public boolean addImplementedMethod(MethodDefinition method) {
		return implementedMethodList.add(method);
	}

	public List<MethodDefinition> getImplementedMethodList() {
		return implementedMethodList;
	}
	
	public List<DetailedTypeDefinition> getAncestorTypeList() {
		return ancestorTypeList;
	}
	
	public List<DetailedTypeDefinition> getAllAncestorTypeList() {
		List<DetailedTypeDefinition> result = new ArrayList<DetailedTypeDefinition>();
		result.addAll(parentTypeList);
		result.addAll(ancestorTypeList);
		return result;
	}
	
	public List<DetailedTypeDefinition> getAllDetailedAncestorTypeList() {
		List<DetailedTypeDefinition> result = new ArrayList<DetailedTypeDefinition>();
		result.addAll(parentTypeList);
		result.addAll(ancestorTypeList);
		return result;
	}
	
	public List<DetailedTypeDefinition> getAncestorClassList() {
		List<DetailedTypeDefinition> result = new ArrayList<DetailedTypeDefinition>();
		for (TypeDefinition type : parentTypeList) {
			if (!type.isInterface()) result.add((DetailedTypeDefinition)type);
		}
		for (TypeDefinition type : ancestorTypeList) {
			if (!type.isInterface()) result.add((DetailedTypeDefinition)type);
		}
		return result;
	}
	
	public List<DetailedTypeDefinition> getDetailedParentTypeList() {
		return parentTypeList;
	}
	
	public List<DetailedTypeDefinition> getAllParentTypeList() {
		return parentTypeList;
	}
	
	public DetailedTypeDefinition getParentClass() {
		for (TypeDefinition type : parentTypeList) {
			if (type.isDetailedType() && !type.isInterface()) return (DetailedTypeDefinition)type;
		}
		return null;
	}

	public boolean addAncestorType(DetailedTypeDefinition superType) {
		return ancestorTypeList.add(superType);
	}
	
	public boolean addParentType(DetailedTypeDefinition parentType) {
		return parentTypeList.add(parentType);
	}

	public int getDepthOfInheritance() {
		return depthOfInheritance;
	}
	
	public void setDepthOfInheritance(int level) {
		depthOfInheritance = level;
	}

	public double getAverageInheritanceDepth() {
		return averageInheritDepth;
	}
	
	public void setAverageInheritanceDepth(double depth) {
		averageInheritDepth = depth;
	}

	public void setChildrenTypeList(List<DetailedTypeDefinition> childrenTypeList) {
		this.childrenTypeList = childrenTypeList;
	}
	
	public void setDescendantTypeList(List<DetailedTypeDefinition> descendantTypeList) {
		this.descendantTypeList = descendantTypeList;
	}

	public List<DetailedTypeDefinition> getDescendantTypeList() {
		return descendantTypeList;
	}

	public List<DetailedTypeDefinition> getAllDescendantTypeList() {
		List<DetailedTypeDefinition> result = new ArrayList<DetailedTypeDefinition>();
		result.addAll(childrenTypeList);
		result.addAll(descendantTypeList);
		return result;
	}

	public List<DetailedTypeDefinition> getChildrenClassList() {
		List<DetailedTypeDefinition> result = new ArrayList<DetailedTypeDefinition>();
		for (DetailedTypeDefinition type : childrenTypeList) {
			if (!type.isInterface()) result.add(type);
		}
		return result;
	}
	
	public List<DetailedTypeDefinition> getAllChildrenTypeList() {
		return childrenTypeList;
	}
	
	public List<DetailedTypeDefinition> getDescendantClassList() {
		List<DetailedTypeDefinition> result = new ArrayList<DetailedTypeDefinition>();
		for (DetailedTypeDefinition type : childrenTypeList) {
			if (!type.isInterface()) result.add(type);
		}
		for (DetailedTypeDefinition type : descendantTypeList) {
			if (!type.isInterface()) result.add(type);
		}
		return result;
	}
	
	public int getClassToLeafDepth() {
		return classToLeafDepth;
	}
	
	public void setClassToLeafDepth(int level) {
		classToLeafDepth = level;
	}
	
	public boolean addUsedType(DetailedTypeDefinition type) {
		return usedTypeSet.add(type);
	}
	
	public List<MethodDefinition> getAllInheritedMethodList() {
		List<MethodDefinition> resultList = new ArrayList<MethodDefinition>();
		
		// Add the methods declared in the ancestors of the given type to the result method list
		for (TypeDefinition ancestor : parentTypeList) {
			if (!ancestor.isDetailedType()) continue;
			List<MethodDefinition> ancestorMethods = ((DetailedTypeDefinition)ancestor).getMethodList();
			if (ancestorMethods == null) continue;
			
			for (MethodDefinition method : ancestorMethods) {
				// Add the non-private methods declared in the ancestor type to the result list
				if (!method.isPrivate() && !method.isConstructor() && !method.isStatic()) resultList.add(method); 
			}
		}
		for (TypeDefinition ancestor : ancestorTypeList) {
			if (!ancestor.isDetailedType()) continue;
			List<MethodDefinition> ancestorMethods = ((DetailedTypeDefinition)ancestor).getMethodList();
			if (ancestorMethods == null) continue;
			
			for (MethodDefinition method : ancestorMethods) {
				// Add the non-private methods declared in the ancestor type to the result list
				if (!method.isPrivate() && !method.isConstructor() && !method.isStatic()) resultList.add(method); 
			}
		}
		return resultList;
	}

	public List<MethodDefinition> getAllMethodsInDescendants() {
		List<MethodDefinition> resultList = new ArrayList<MethodDefinition>();
		
		// Add the methods declared in the ancestors of the given type to the result method list
		for (DetailedTypeDefinition descendant: childrenTypeList) {
			if (!descendant.isDetailedType()) continue;
			List<MethodDefinition> descendantMethods = ((DetailedTypeDefinition)descendant).getMethodList();
			if (descendantMethods == null) continue;
			
			for (MethodDefinition method : descendantMethods) {
				// Add the non-private methods declared in the ancestor type to the result list
				if (!method.isPrivate() && !method.isConstructor() && !method.isStatic()) resultList.add(method); 
			}
		}
		for (DetailedTypeDefinition descendant: descendantTypeList) {
			if (!descendant.isDetailedType()) continue;
			List<MethodDefinition> descendantMethods = ((DetailedTypeDefinition)descendant).getMethodList();
			if (descendantMethods == null) continue;
			
			for (MethodDefinition method : descendantMethods) {
				// Add the non-private methods declared in the ancestor type to the result list
				if (!method.isPrivate() && !method.isConstructor() && !method.isStatic()) resultList.add(method); 
			}
		}
		return resultList;
	}
	
	public List<FieldDefinition> getAllInheritedFieldList() {
		List<FieldDefinition> resultList = new ArrayList<FieldDefinition>();
		if (ancestorTypeList == null) return resultList;
		
		// Add the methods declared in the ancestors of the given type to the result method list
		for (TypeDefinition ancestor : parentTypeList) {
			if (!ancestor.isDetailedType() || ancestor.isInterface()) continue;
			List<FieldDefinition> ancestorFields = ((DetailedTypeDefinition)ancestor).getFieldList();
			if (ancestorFields == null) continue;
			
			for (FieldDefinition field : ancestorFields) {
				// Add the non-private methods declared in the ancestor type to the result list
				if (!field.isPrivate() && !field.isStatic()) resultList.add(field); 
			}
		}
		for (TypeDefinition ancestor : ancestorTypeList) {
			if (!ancestor.isDetailedType() || ancestor.isInterface()) continue;
			List<FieldDefinition> ancestorFields = ((DetailedTypeDefinition)ancestor).getFieldList();
			if (ancestorFields == null) continue;
			
			for (FieldDefinition field : ancestorFields) {
				// Add the non-private methods declared in the ancestor type to the result list
				if (!field.isPrivate() && !field.isStatic()) resultList.add(field); 
			}
		}
		return resultList;
	}
	
	public Set<DetailedTypeDefinition> getUsedTypeSet() {
		return usedTypeSet;
	}
	
	public boolean hasUsedType(DetailedTypeDefinition type) {
		return usedTypeSet.contains(type);
	}
	
	public int getUsedTypeNumber() {
		return usedTypeSet.size();
	}
}

