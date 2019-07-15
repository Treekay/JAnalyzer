package measurement.metric.inheritance;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import softwareStructure.SoftwareStructManager;

/**
 * A class to calculate the metric SPA, which is the number of class method which has the methods with the same 
 * name but different signature (i.e. a method overload another method) in the ancestors of the class. The method 
 * which has the methods with the same name  but different signature in the ancestors or descendants of the class 
 * is called static polymorphism. 
 * <p>Note that we do not consider the metric SPA for interface, which will be always 0
 *   
 * @author Zhou Xiaocong
 * @since 2015年10月14日
 * @version 1.0
 */
public class SPAMetric extends SoftwareInheritanceMetric {

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;
		
		double value = getStaticPolymorphismInAncestors(structManager, type);
		measure.setValue(value);
		return true;
	}

	/**
	 * Calculate the number of method with static polymorphism.
	 * <p> This methods is public and static because it also be used to calculate the metric SP
	 */
	public static int getStaticPolymorphismInAncestors(SoftwareStructManager structManager, DetailedTypeDefinition type) {
		if (type.isInterface()) return 0;
		
		// Get all methods declared in the type
		List<MethodDefinition> methodList = type.getMethodList();
		if (methodList == null) return 0;
		
		// Note that all inherited methods give the all methods declared in the ancestors of the type. 
		// Note that inherited methods do not include those private methods and constructor in the ancestors 
		List<MethodDefinition> ancestorMethodList = structManager.getAllInheritedMethodList(type);
		if (ancestorMethodList == null) return 0;
		int result = 0;
		for (MethodDefinition method : methodList) {
			for (MethodDefinition ancestorMethod : ancestorMethodList) {
				// This method has static polymorphism since is an overloaded method for a method in an ancestor type.
				if (method.isOverloadMethod(ancestorMethod)) {
					result = result + 1;
					// Do not consider other ancestor methods, since we only compute the number of method rather than the number of overloaded
					break;
				}
			}
		}
		
		return result;
	}
}
