package nameTable.creator;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.IntersectionTypeReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.NamedTypeReference;
import nameTable.nameReference.ParameterizedTypeReference;
import nameTable.nameReference.QualifiedTypeReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameReference.UnionTypeReference;
import nameTable.nameReference.WildcardTypeReference;
import nameTable.nameScope.NameScope;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.WildcardType;

import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeLocation;

/**
 * Visit a type node, create a type reference for the node. Note the type reference created by the class DO NOT
 * add to the creator
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2016/11/11
 * 		Refactor the class according to the design document
 */
public class TypeASTVisitor extends ASTVisitor {
	private String name = null;				// Name of the type reference
	private int dimension = 0;				// Possible dimension number of the type
	
	private NameScope currentScope = null;
	private CompilationUnitRecorder unitFile = null;
	
	private SourceCodeLocation resultLocation = null;
	private TypeReference result = null;
	
	public TypeASTVisitor(CompilationUnitRecorder unitFile, NameScope currentScope) {
		this.unitFile = unitFile;
		this.currentScope = currentScope;
	}
	
	public void reset(CompilationUnitRecorder unitFile, NameScope currentScope) {
		this.unitFile = unitFile;
		this.currentScope = currentScope;

		name = null;
		dimension = 0;
		result = null;
	}
	
	public void reset(NameScope currentScope) {
		this.currentScope = currentScope;

		name = null;
		dimension = 0;
		result = null;
	}
	
	public TypeReference getResult() {
		if (result != null) return result;
		
		result = new TypeReference(name, resultLocation, currentScope);
		result.setDimension(dimension);
		return result;
	}

	/**
	 * ArrayType Type [][]
	 * Use this visitor to find the name of the element type recursively!
	 */
	public boolean visit(ArrayType node) {
		dimension = node.getDimensions();
		node.getElementType().accept(this);
		return false;
	}
	
	/**
	 * Intersection Type: Type & Type { & Type }
	 */
	public boolean visit(IntersectionType node) {
		IntersectionTypeReference resultTypeRef = new IntersectionTypeReference(node.toString(), resultLocation, currentScope);
		
		@SuppressWarnings("unchecked")
		List<Type> typeList = node.types();
		for (Type type : typeList) {
			reset(currentScope);
			type.accept(this);
			TypeReference lastResult = getResult();
			resultTypeRef.addType(lastResult);
		}
		result = resultTypeRef;
		return false;
	}
	

	/**
	 * Visit the named qualified type as a named type! 
	 * Important Notes: So far there is no difference between NameQualifiedType and QualifiedType. We need further to investigate how
	 * 		to deal with the AST Node nameQualifiedType, QualifiedType, and QualifiedName as type reference! 
	 */
	public boolean visit(NameQualifiedType node) {
		node.getQualifier().accept(this);
		TypeReference lastResult = getResult();
		
		// Composite the name of the qualified type
		name = name + NameReferenceLabel.NAME_QUALIFIER + node.getName().getFullyQualifiedName();
		String simpleName = node.getName().getFullyQualifiedName();
		resultLocation = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		NamedTypeReference namedTypeRef = new NamedTypeReference(simpleName, name, resultLocation, currentScope);

		namedTypeRef.setQualifier(lastResult);
		
		// Set the result to the qualified type reference represent the entire node!
		result = namedTypeRef;
		return false;
	}
	
	/**
	 * ParameterizedType: Type < Type { , Type } >
	 * We only add the reference for the first type, the type arguments are ignored
	 * So we use this visitor to find the name of the first type recursively!
	 */
	public boolean visit(ParameterizedType node) {
		node.getType().accept(this);
		TypeReference primaryType = getResult();
		
		ParameterizedTypeReference resultTypeRef = new ParameterizedTypeReference(node.toString(), resultLocation, currentScope);
		resultTypeRef.setPrimaryType(primaryType);
		
		List<TypeReference> parameterList = new ArrayList<TypeReference>();
		@SuppressWarnings("unchecked")
		List<Type> typeList = node.typeArguments();
		for (Type type : typeList) {
			reset(currentScope);
			type.accept(this);
			TypeReference lastResult = getResult();
			parameterList.add(lastResult);
		}
		resultTypeRef.setArgumentList(parameterList);
		result = resultTypeRef;
		return false;
	}
	
	/**
	 * For primitive type, just set the name and location of the result type reference
	 */
	public boolean visit(PrimitiveType node) {
		name = node.getPrimitiveTypeCode().toString();
		resultLocation = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		return false;
	}

	/**
	 * QualifiedType: Type . SimpleName
	 * We use this visitor to composite the name of the type
	 */
	public boolean visit(QualifiedType node) {
		node.getQualifier().accept(this);
		TypeReference lastResult = getResult();
		
		// Composite the name of the qualified type
		name = name + NameReferenceLabel.NAME_QUALIFIER + node.getName().getFullyQualifiedName();
		String simpleName = node.getName().getFullyQualifiedName();
		resultLocation = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		QualifiedTypeReference qualifiedTypeRef = new QualifiedTypeReference(simpleName, name, resultLocation, currentScope);

		qualifiedTypeRef.setQualifier(lastResult);
		
		// Set the result to the qualified type reference represent the entire node!
		result = qualifiedTypeRef;
		return false;
	}
	
	/**
	 * Since a simple type can be a qualified name, so we should to visit its children
	 */
	public boolean visit(SimpleType node) {
		return true;
	}

	/**
	 * Get the name for create the result type reference
	 */
	public boolean visit(SimpleName node) {
		name = node.getFullyQualifiedName();
		resultLocation = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		return false;
	}

	/**
	 * Visit the qualified name as a qualified type!
	 */
	public boolean visit(QualifiedName node) {
		node.getQualifier().accept(this);
		TypeReference lastResult = getResult();
		
		// Composite the name of the qualified type
		name = name + NameReferenceLabel.NAME_QUALIFIER + node.getName().getFullyQualifiedName();
		String simpleName = node.getName().getFullyQualifiedName();
		resultLocation = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		QualifiedTypeReference qualifiedTypeRef = new QualifiedTypeReference(simpleName, name, resultLocation, currentScope);

		qualifiedTypeRef.setQualifier(lastResult);
		
		// Set the result to the qualified type reference represent the entire node!
		result = qualifiedTypeRef;
		
		return false;
	}

	/**
	 * Union Type: Type | Type { | Type }
	 */
	public boolean visit(UnionType node) {
		UnionTypeReference resultTypeRef = new UnionTypeReference(node.toString(), resultLocation, currentScope);
		
		@SuppressWarnings("unchecked")
		List<Type> typeList = node.types();
		for (Type type : typeList) {
			reset(currentScope);
			type.accept(this);
			TypeReference lastResult = getResult();
			resultTypeRef.addType(lastResult);
		}
		result = resultTypeRef;
		return false;
	}
	
	/**
	 * WildcardType: ? [ ( extends | super) Type ]
	 */
	public boolean visit(WildcardType node) {
		WildcardTypeReference resultTypeRef = new WildcardTypeReference(node.toString(), resultLocation, currentScope);
		
		Type boundType = node.getBound();
		if (boundType != null) {
			boundType.accept(this);
			TypeReference boundTypeRef = getResult();
			resultTypeRef.setBound(boundTypeRef);
		}
		result = resultTypeRef;
		return false;
	}
	
}
