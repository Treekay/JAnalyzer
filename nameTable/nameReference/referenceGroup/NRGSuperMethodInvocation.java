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
 * The name reference group corresponds to super method invocation expression. 
 * <p>		SuperMethodInvocation: [ ClassName . ] super .  [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 * 		Important note: The method arguments and type arguments is setted when we create this group.
 * 			As to so far, we do not consider the type arguments in the reference.
 */
public class NRGSuperMethodInvocation extends NameReferenceGroup {

	public NRGSuperMethodInvocation(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_SUPER_METHOD_INVOCATION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The super method invocation reference group " + this.toFullString() + " has not sub-references!");
		
		// For super method invocation, if the first reference is a type reference, then resolve the reference, 
		// bind it to a type definition, and get the super type of this type definition, and resolve the 
		// second reference in the super type.
		// If the first reference is not a type reference, then find the first type definition enclosing 
		// the reference, and resolve the reference in the super type of the type definition
		NameReference firstRef = subreferences.get(0);
		MethodReference methodRef = null;
		TypeDefinition superTypeDef = null;
		TypeDefinition typeDef = null;
		if (firstRef.getReferenceKind() == NameReferenceKind.NRK_TYPE){
			TypeReference typeRef = (TypeReference)firstRef;
			typeRef.resolveBinding();
			typeDef = (TypeDefinition)typeRef.getDefinition();
			methodRef = (MethodReference)subreferences.get(1);
		} else {
			typeDef = getEnclosingTypeDefinition();
			methodRef = (MethodReference)firstRef;
		}
		
		// Resolve type arguments and arguments of the method reference!
		List<TypeReference> typeArgumentList = methodRef.getTypeArgumentList();
		if (typeArgumentList != null) {
			for (TypeReference typeArgument : typeArgumentList) typeArgument.resolveBinding();
		}
		List<NameReference> argumentList = methodRef.getArgumentList();
		if (argumentList != null) {
			for (NameReference argument : argumentList) argument.resolveBinding();
		}
		
		if (typeDef != null) superTypeDef = typeDef.getSuperClassDefinition();
		if (superTypeDef != null) superTypeDef.resolve(methodRef);
		
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
	 * The core reference of a super method invocation expression is the method reference in the expression
	 */
	public NameReference getCoreReference() {
		NameReference firstRef = subreferences.get(0);
		MethodReference methodRef = null;
		if (firstRef.getReferenceKind() == NameReferenceKind.NRK_TYPE){
			methodRef = (MethodReference)subreferences.get(1);
		} else {
			methodRef = (MethodReference)firstRef;
		}
		return methodRef.getCoreReference();
	}
}
