package softwareStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.TypeDefinition;

/**
 * A class store the generated structure information about a method
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ1ÈÕ
 * @version 1.0
 */
class MethodStructEntry {
	protected MethodDefinition method = null;
	protected List<TypeDefinition> parameterTypeList = null;
	protected TreeSet<FieldDefinition> usedFieldSet = null;
	protected TreeMap<MethodDefinition, MethodCallInformation> calledMethodMap = null;
	
	public MethodStructEntry(MethodDefinition method) {
		this.method = method;
	}
	
	public MethodStructEntry(MethodStructEntry other) {
		method = other.method;
		parameterTypeList = other.parameterTypeList;
		usedFieldSet = other.usedFieldSet;
		calledMethodMap = other.calledMethodMap;
	}
	
	public void initializeStructureInformation() {
		parameterTypeList = new ArrayList<TypeDefinition>();
		usedFieldSet = new TreeSet<FieldDefinition>();
		calledMethodMap = new TreeMap<MethodDefinition, MethodCallInformation>();
	}

	public boolean hasInitializedStructureInformation() {
		if (parameterTypeList != null) return true;
		else return false;
	}
	
	public MethodDefinition getMethod() {
		return method;
	}
	
	
	public TreeMap<MethodDefinition, MethodCallInformation> getCalledMethodMap() {
		return calledMethodMap;
	}

	public void setCalledMethodMap(TreeMap<MethodDefinition, MethodCallInformation> calledMethodMap) {
		this.calledMethodMap = calledMethodMap;
	}

	public void addMethodCallInformation(MethodDefinition callee, int staticCallNumber, int polymorphicCallNumber) {
		MethodCallInformation callInformation = calledMethodMap.get(callee);
		if (callInformation == null) {
			calledMethodMap.put(callee, new MethodCallInformation(staticCallNumber, polymorphicCallNumber));
		} else {
			int oldCallNumber = callInformation.getStaticCallNumber();
			callInformation.setStaticCallNumber(oldCallNumber + staticCallNumber);
			oldCallNumber = callInformation.getPolymorphicCallNumber();
			callInformation.setPolymorphicCallNumber(oldCallNumber + polymorphicCallNumber);
		}
	}
	
	
	public List<MethodWithCallInformation> getStaticInvocationMethodWithCallInformationList() {
		List<MethodWithCallInformation> result = new ArrayList<MethodWithCallInformation>();
		Set<MethodDefinition> calledMethodSet = calledMethodMap.keySet();
		
		for (MethodDefinition method : calledMethodSet) {
			MethodCallInformation callInfo = calledMethodMap.get(method);
			int staticCallNumber = callInfo.getStaticCallNumber();
			if (staticCallNumber > 0) result.add(new MethodWithCallInformation(method, staticCallNumber));
		}
		return result;
	}
	
	public List<MethodDefinition> getStaticInvocationMethodList() {
		List<MethodDefinition> result = new ArrayList<MethodDefinition>();
		Set<MethodDefinition> calledMethodSet = calledMethodMap.keySet();
		
		for (MethodDefinition method : calledMethodSet) {
			MethodCallInformation callInfo = calledMethodMap.get(method);
			if (callInfo.getStaticCallNumber() > 0) result.add(method);
		}
		return result;
	}

	public List<MethodDefinition> getStaticInvocationMethodListInRange(List<MethodDefinition> rangeList) {
		List<MethodDefinition> result = new ArrayList<MethodDefinition>();
		Set<MethodDefinition> calledMethodSet = calledMethodMap.keySet();
		
		for (MethodDefinition method : calledMethodSet) {
			if (!rangeList.contains(method)) continue; 
			MethodCallInformation callInfo = calledMethodMap.get(method);
			if (callInfo.getStaticCallNumber() > 0) result.add(method);
		}
		return result;
	}

	public List<MethodWithCallInformation> getPolymorphicInvocationMethodWithCallInformationList() {
		List<MethodWithCallInformation> result = new ArrayList<MethodWithCallInformation>();
		Set<MethodDefinition> calledMethodSet = calledMethodMap.keySet();
		
		for (MethodDefinition method : calledMethodSet) {
			MethodCallInformation callInfo = calledMethodMap.get(method);
			int PolymorphicCallNumber = callInfo.getPolymorphicCallNumber();
			result.add(new MethodWithCallInformation(method, PolymorphicCallNumber));
		}
		return result;
	}

	public List<MethodDefinition> getPolymorphicInvocationMethodList() {
		List<MethodDefinition> result = new ArrayList<MethodDefinition>();
		Set<MethodDefinition> calledMethodSet = calledMethodMap.keySet();
		
		for (MethodDefinition method : calledMethodSet) {
			result.add(method);
		}
		return result;
	}
	
	public List<MethodDefinition> getPolymorphicInvocationMethodListInRange(List<MethodDefinition> rangeList) {
		List<MethodDefinition> result = new ArrayList<MethodDefinition>();
		Set<MethodDefinition> calledMethodSet = calledMethodMap.keySet();
		
		for (MethodDefinition method : calledMethodSet) {
			if (rangeList.contains(method)) result.add(method);
		}
		return result;
	}

	public int getDirectStaticInvocationNumber(MethodDefinition callee) {
		MethodCallInformation callInfo = calledMethodMap.get(callee);
		if (callInfo != null) return callInfo.getStaticCallNumber();
		return 0;
	}

	public int getDirectPolymorphicInvocationNumber(MethodDefinition callee) {
		MethodCallInformation callInfo = calledMethodMap.get(callee);
		if (callInfo != null) return callInfo.getPolymorphicCallNumber();
		return 0;
	}
	
	public boolean addParameterType(TypeDefinition type) {
		if (!parameterTypeList.contains(type)) return parameterTypeList.add(type);
		return false;
	}
	
	public List<TypeDefinition> getParameterTypeList() {
		return parameterTypeList;
	}
	
	public boolean addFieldUsing(FieldDefinition field) {
		return usedFieldSet.add(field);
	}
	
	public List<FieldDefinition> getFieldUsingList() {
		List<FieldDefinition> result = new ArrayList<FieldDefinition>();
		for (FieldDefinition field : usedFieldSet) result.add(field);
		return result;
	}

	public List<FieldDefinition> getFieldUsingListInRange(List<FieldDefinition> rangeList) {
		List<FieldDefinition> result = new ArrayList<FieldDefinition>();
		for (FieldDefinition field : usedFieldSet) {
			if (rangeList.contains(field)) result.add(field);
		}
		return result;
	}

	public boolean hasIndirectPolyCallInformation() {
		// TODO Auto-generated method stub
		return false;
	}
}


class MethodCallInformation {
	private int staticCallNumber = 0;
	private int polymorphicCallNumber = 0;
	
	public MethodCallInformation() {
	}

	public MethodCallInformation(int staticCallNumber, int polymorphicCallNumber) {
		this.staticCallNumber = staticCallNumber;
		this.polymorphicCallNumber = polymorphicCallNumber;
	}
	
	public int getStaticCallNumber() {
		return staticCallNumber;
	}
	
	public int getPolymorphicCallNumber() {
		return polymorphicCallNumber;
	}
	
	public void setOneMoreStaticCallNumber() {
		staticCallNumber++;
	}

	public void setOneMorePolymorphicCallNumber() {
		polymorphicCallNumber++;
	}

	public void setStaticCallNumber(int number) {
		staticCallNumber = number;
	}

	public void setPolymorphicCallNumber(int number) {
		polymorphicCallNumber = number;
	}

	public boolean hasIndirectPolyCallInformation() {
		return false;
	}
}


