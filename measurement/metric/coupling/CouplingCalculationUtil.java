package measurement.metric.coupling;

import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import softwareStructure.SoftwareStructManager;

/**
 * This class help other class to calculate coupling metric
 * 
 * @author Li Jingsheng
 * @update 2015/09/28
 * @update 2015/10/12, Zhou Xiaocong
 *
 */
public class CouplingCalculationUtil {

	/**
	 * Get the number of field in client class whose type is  the service class. We consider all fields (i.e. include inherited 
	 * fields) of the client class.
	 */
	public static int getNumberOfCA(SoftwareStructManager structManager, DetailedTypeDefinition client, DetailedTypeDefinition service) {
		List<FieldDefinition> fieldList = structManager.getAllFieldList(client);
		if (fieldList == null) return 0;
		
		int sum = 0;
		for (FieldDefinition field : fieldList) {
			// We consider the parameterized type in the declaration of the field
			List<TypeDefinition> fieldTypeList = field.getTypeDefinition(true);
			if (fieldTypeList != null) {
				for (TypeDefinition fieldType : fieldTypeList) {
					if (fieldType == service) {
						sum = sum + 1;
						
//						Debug.println("\tCA: field " + field.getLabel() + " type is " + fieldType.getLabel() + ", sum = " + sum);
					}
				}
			}
		}
		return sum;
	}
	
	/**
	 * Get the number of parameter in methods of client class whose type is the service class. We consider all new methods
	 * of the client class.
	 */
	public static int getNumberOfCM(SoftwareStructManager structManager, DetailedTypeDefinition client, DetailedTypeDefinition service) {
		List<MethodDefinition> newMethodList = structManager.getNewMethods(client);
		if (newMethodList == null) return 0;

		int sum = 0;
		for (MethodDefinition method : newMethodList) {
			List<VariableDefinition> parameterList = method.getParameterList();
			if (parameterList != null) {
				for (VariableDefinition parameter : parameterList) {
					// We consider the parameterized type in the declaration of the parameter
					List<TypeDefinition> parameterTypeList = parameter.getTypeDefinition(true);
					if (parameterTypeList != null) {
						for (TypeDefinition parameterType : parameterTypeList) {
							if (parameterType == service) {
								sum = sum + 1;

//								Debug.println("\tCM: method " + method.getLabel() + " parameter " + parameter.getLabel() + " type is " + parameterType.getLabel() + ", sum = " + sum);
							}
						}
					}
				}
			}
		}
		return sum;
	}
	
	/**
	 * Get the number of invocation that the implemented methods in client class call the new or override methods in service class
	 */
	public static int getNumberOfMM(SoftwareStructManager structManager, DetailedTypeDefinition client, DetailedTypeDefinition service) {
		List<MethodDefinition> clientImplementedMethodList = structManager.getImplementedMethodList(client);
		List<MethodDefinition> serviceNewMethodList = structManager.getNewMethods(service);
		List<MethodDefinition> serviceOverridedMethodList = structManager.getOverriddenMethods(service);
		
		if (clientImplementedMethodList == null) return 0;
		int sum = 0;
		for (MethodDefinition clientMethod : clientImplementedMethodList) {
			if (serviceNewMethodList != null) {
				for (MethodDefinition serviceNewMethod : serviceNewMethodList) {
					sum = sum + structManager.getDirectPolymorphicInvocationNumber(clientMethod, serviceNewMethod); 
				}
			}
			if (serviceOverridedMethodList != null) {
				for (MethodDefinition serviceOverridedMethod : serviceOverridedMethodList) {
					sum = sum + structManager.getDirectPolymorphicInvocationNumber(clientMethod, serviceOverridedMethod);
				}
			}
		}
		return sum;
	}

	/**
	 * Test if the detailed type definition other is indeed other detailed type definition for the given type, i.e. 
	 * return true when (other != type) and (other not in ancestorTypeList) and (other not in descendantTypeList), otherwise
	 * return false.
	 * <p>Here, we assume that ancestorTypeList is the list of ancestor types of the given type, and descendantTypeList is 
	 * the list of descendant types of the given type. 
	 * <p>We require that the caller passes ancestor type list and descendant type list rather than getting them from the 
	 * given type by using a SoftwareStructManager object, because generally we may call this method many many times to test 
	 * if another type is indeed the other type for a given type, and then it is not effective to getting these two lists 
	 * for each calling.     
	 */
	public static boolean isOtherDetailedTypeDefinition(DetailedTypeDefinition other, DetailedTypeDefinition type, 
			List<DetailedTypeDefinition> ancestorTypeList, List<DetailedTypeDefinition> descendantTypeList) {
		if (other == type) return false;
		if (ancestorTypeList != null) {
			for (TypeDefinition ancestorType : ancestorTypeList) {
				if (other == ancestorType) return false; 
			}
		}
		if (descendantTypeList != null) {
			for (DetailedTypeDefinition descendantType : descendantTypeList) {
				if (other == descendantType) return false; 
			}
		}
		return true;
	}
	
	
}
