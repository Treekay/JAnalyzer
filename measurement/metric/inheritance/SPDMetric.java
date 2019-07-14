package measurement.metric.inheritance;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import softwareStructure.SoftwareStructManager;

/**
 * A class to calculate the metric SPD metric, which is the number of class method which has the methods with the same 
 * name but different signature  (i.e. a method overload another method) in the descendants of the class. The method which 
 * has the methods with the same name but different signature in the ancestors or descendants of the class is called 
 * static polymorphism. 
 *   
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ14ÈÕ
 * @version 1.0
 */
public class SPDMetric extends SoftwareInheritanceMetric {

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;
		
		double value = getStaticPolymorphismInDescendants(structManager, type);
		measure.setValue(value);
		return true;
	}

	/**
	 * Calculate the number of method with static polymorphism.
	 * <p> This methods is public and static because it also be used to calculate the metric SP
	 */
	public static int getStaticPolymorphismInDescendants(SoftwareStructManager structManager, DetailedTypeDefinition type) {
		if (type.isInterface()) return 0;

		// Get all methods declared in the type
		List<MethodDefinition> methodList = type.getMethodList();
		if (methodList == null) return 0;
		
		// Note that all inherited methods give the all methods declared in the ancestors of the type. 
		// Note that inherited methods do not include those private methods and constructor in the ancestors 
		List<MethodDefinition> ancestorMethodList = structManager.getAllMethodsInDescendants(type);
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
