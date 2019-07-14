package softwareStructure;

import nameTable.nameDefinition.MethodDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ4ÈÕ
 * @version 1.0
 */
public class MethodWithCallInformation {

	private MethodDefinition method = null;
	private int callNumber = 0;
	
	public MethodWithCallInformation(MethodDefinition method, int callNumber) {
		this.method = method;
		this.callNumber = callNumber;
	}

	public MethodDefinition getMethod() {
		return method;
	}
	
	public int getCallNumber() {
		return callNumber;
	}
}
