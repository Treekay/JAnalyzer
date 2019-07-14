package nameTable.nameReference.referenceGroup;

import java.util.List;

import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * The name reference group corresponds to class instance creation expression. 
 * <p>		[ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 * 		Important note: The method arguments and type arguments is setted when we create this group.
 * 			As to so far, we do not consider the type arguments in the reference.
 */
public class NRGClassInstanceCreation extends NameReferenceGroup {

	public NRGClassInstanceCreation(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_CLASS_INSTANCE_CREATION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The class instance creation reference group " + this.toFullString() + " has not sub-references!");
		
		// Bind the group to the type definition of the first or second reference (which is a type reference) of 
		// the class instance expression
		NameReference firstRef = subreferences.get(0);
		firstRef.resolveBinding();
		NameReferenceKind firstRefKind = firstRef.getReferenceKind();
		if (firstRefKind == NameReferenceKind.NRK_TYPE){
			// The group is binded to the type definition of the first reference
			TypeReference typeRef = (TypeReference)firstRef;
			if (!typeRef.isResolved()) typeRef.resolveBinding();
			bindTo(typeRef.getDefinition());
			
			MethodReference methodRef = (MethodReference)subreferences.get(1);
			
			// Resolve its type arguments and arguments
			List<TypeReference> typeArgumentList = methodRef.getTypeArgumentList();
			if (typeArgumentList != null) {
				for (TypeReference typeArgument : typeArgumentList) typeArgument.resolveBinding();
			}
			List<NameReference> argumentList = methodRef.getArgumentList();
			if (argumentList != null) {
				for (NameReference argument : argumentList) argument.resolveBinding();
			}
			
			if (typeRef.isResolved()) {
				// Resolved the constructor invocation in the definition of typeRef
				TypeDefinition typeDef = (TypeDefinition)typeRef.getDefinition();
				typeDef.resolve(methodRef);
			}
			
			for (int index = 2; index < subreferences.size(); index++) {
				NameReference otherRef = subreferences.get(index);
				otherRef.resolveBinding();
			}
		} else {
			NameReference secondRef = subreferences.get(1);
			if (secondRef.getReferenceKind() == NameReferenceKind.NRK_TYPE) {
				TypeReference typeRef = (TypeReference)secondRef;
				if (!typeRef.isResolved()) {
					// We need resolve the type reference in the scope of the type of the first reference
					TypeDefinition varTypeDef = firstRef.getResultTypeDefinition();
					if (varTypeDef != null) varTypeDef.resolve(typeRef);
				}
				bindTo(typeRef.getDefinition());

				MethodReference methodRef = (MethodReference)subreferences.get(2);
				// Resolve its type arguments and arguments
				List<TypeReference> typeArgumentList = methodRef.getTypeArgumentList();
				if (typeArgumentList != null) {
					for (TypeReference typeArgument : typeArgumentList) typeArgument.resolveBinding();
				}
				List<NameReference> argumentList = methodRef.getArgumentList();
				if (argumentList != null) {
					for (NameReference argument : argumentList) argument.resolveBinding();
				}
				
				if (typeRef.isResolved()) {
					// Resolved the constructor invocation in the definition of typeRef
					TypeDefinition typeDef = (TypeDefinition)typeRef.getDefinition();
					typeDef.resolve(methodRef);
				}
				for (int index = 3; index < subreferences.size(); index++) {
					NameReference otherRef = subreferences.get(index);
					otherRef.resolveBinding();
				}
			} else {
				throw new AssertionError("The first and second operand in class instance createion expression " + this.toFullString() + " is not a type!");
			}
		}

		return isResolved();
	}
	
	/**
	 * The core reference of a class instance creation expression is the method (constructor) reference in the expression
	 */
	public NameReference getCoreReference() {
		NameReference firstRef = subreferences.get(0);
		NameReferenceKind firstRefKind = firstRef.getReferenceKind();
		MethodReference methodRef = null;		
		if (firstRefKind == NameReferenceKind.NRK_TYPE){
			methodRef = (MethodReference)subreferences.get(1);
		} else {
			NameReference secondRef = subreferences.get(1);
			if (secondRef.getReferenceKind() == NameReferenceKind.NRK_TYPE) {
				methodRef = (MethodReference)subreferences.get(2);
			} 
		}
		if (methodRef == null) return this;
		else return methodRef.getCoreReference();
	}
}
