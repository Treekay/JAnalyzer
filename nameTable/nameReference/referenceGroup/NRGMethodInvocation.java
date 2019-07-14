package nameTable.nameReference.referenceGroup;

import java.util.List;

import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to method invocation expression. 
 * <p>		MethodInvocation: [ Expression . ] [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] ) 
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 * 		Important note: The method arguments and type arguments is setted when we create this group.
 * 			As to so far, we do not consider the type arguments in the reference.
 */
public class NRGMethodInvocation extends NameReferenceGroup {

	public NRGMethodInvocation(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_METHOD_INVOCATION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The method invocation reference group " + this.toFullString() + " has not sub-references!");
		
		// For method invocation, we should resolve reference before the method reference at first, 
		// it should be binded to a type definition, and then resolve the method reference in this 
		// definition with the remain references as the parameter!
		NameReference firstRef = subreferences.get(0);
		MethodReference methodRef = null;
		if (firstRef.getReferenceKind() != NameReferenceKind.NRK_METHOD) {
			methodRef = (MethodReference)subreferences.get(1);
			// Resolve its type arguments and arguments
			List<TypeReference> typeArgumentList = methodRef.getTypeArgumentList();
			if (typeArgumentList != null) {
				for (TypeReference typeArgument : typeArgumentList) typeArgument.resolveBinding();
			}
			List<NameReference> argumentList = methodRef.getArgumentList();
			if (argumentList != null) {
				for (NameReference argument : argumentList) argument.resolveBinding();
			}
			
			// The first expression gives the object to call the method, we should resolve the method reference
			// in the type definition binded to the first expression
			TypeDefinition typeDef = null;
			firstRef.resolveBinding();

			if (firstRef.isResolved()) typeDef = firstRef.getResultTypeDefinition();
			else {
				// The first expression may be a class name (i.e. use a class name to call its static method!
				if (firstRef.getReferenceKind() == NameReferenceKind.NRK_VARIABLE) {
					// This means the expression is a simple name, and when we create the reference, we set its kind to be NRK_VARIABLE
					// However, a simple name may be a class name! we set the kind to be NRK_TYPE, and try to resolve it!
					firstRef.setReferenceKind(NameReferenceKind.NRK_TYPE);
					firstRef.resolveBinding();
					if (firstRef.isResolved()) typeDef = firstRef.getResultTypeDefinition();
					else firstRef.setReferenceKind(NameReferenceKind.NRK_VARIABLE);   // We can not resolve it as a class name, restore its kind to NRK_VARIABLE! 
				}
			}
			
			if (typeDef != null) {
				typeDef.resolve(methodRef);
			}
		} else {
			// The first expression is the method reference, we resolve the method reference in the 
			// current scope
			methodRef = (MethodReference)firstRef;
			// Resolve its type arguments and arguments
			List<TypeReference> typeArgumentList = methodRef.getTypeArgumentList();
			if (typeArgumentList != null) {
				for (TypeReference typeArgument : typeArgumentList) typeArgument.resolveBinding();
			}
			List<NameReference> argumentList = methodRef.getArgumentList();
			if (argumentList != null) {
				for (NameReference argument : argumentList) argument.resolveBinding();
			}
			methodRef.resolveBinding();
		}
		// Bind the group reference to the return type of the method 
		if (methodRef.isResolved()) {
			MethodDefinition methodDef = (MethodDefinition)methodRef.getDefinition();
			TypeReference typeRef = methodDef.getReturnType();
			
			if (typeRef != null) {
				typeRef.resolveBinding();
				bindTo(typeRef.getDefinition());
			}
		}
		return isResolved();
	}
	
	/**
	 * The core reference of a method invocation expression is the method reference in the expression
	 */
	public NameReference getCoreReference() {
		NameReference firstRef = subreferences.get(0);
		MethodReference methodRef = null;
		if (firstRef.getReferenceKind() != NameReferenceKind.NRK_METHOD) {
			methodRef = (MethodReference)subreferences.get(1);
		} else {
			// The first expression is the method reference, we resolve the method reference in the 
			// current scope
			methodRef = (MethodReference)firstRef;
		}
		return methodRef.getCoreReference();
	}
}
