package nameTable.creator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;


/**
 * A visitor class to find a type declaration for a type definition. 
 * 
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ1ÈÕ
 * @version 1.0
 * 
 * @update 2018/06/05
 * 		To support finding AnonymousClassDeclaration
 */
public class TypeDeclarationASTVisitor extends ASTVisitor {
	private List<TypeDeclaration> resultList = null;
	private List<AnonymousClassDeclaration> anonyList = null;
	
	public List<TypeDeclaration> getResultList() {
		return resultList;
	}
	
	public List<AnonymousClassDeclaration> getAnonyClassList() {
		return anonyList;
	}
	
	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(AnnotationTypeDeclaration node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		if (anonyList == null) anonyList = new ArrayList<AnonymousClassDeclaration>();
		anonyList.add(node);
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ArrayAccess node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ArrayCreation node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ArrayInitializer node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ArrayType node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(AssertStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(Assignment node) {
		return true;
	}

	/**
	 * Goto its children to find local types
	 */
	public boolean visit(Block node) {
		return true;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(BlockComment node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(BooleanLiteral node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(BreakStatement node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(CastExpression node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(CatchClause node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(CharacterLiteral node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ClassInstanceCreation node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not occur in a block node
	 */
	public boolean visit(CompilationUnit node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ConditionalExpression node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ConstructorInvocation node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ContinueStatement node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(DoStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(EmptyStatement node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(EnhancedForStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(EnumConstantDeclaration node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(EnumDeclaration node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ExpressionStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(FieldAccess node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(FieldDeclaration node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ForStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(IfStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ImportDeclaration node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(InfixExpression node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(InstanceofExpression node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(Initializer node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(Javadoc node) {
		// visit tag elements inside doc comments only if requested
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(LabeledStatement node) {
		return true;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(LineComment node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(MarkerAnnotation node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(MemberRef node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(MemberValuePair node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(MethodRef node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(MethodRefParameter node) {
		return false;
	}


	/**
	 * Goto its children to find local types
	 */
	public boolean visit(MethodDeclaration node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(MethodInvocation node) {
		return true;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(Modifier node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(NormalAnnotation node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(NullLiteral node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(NumberLiteral node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far. It can not occur in a block
	 */
	public boolean visit(PackageDeclaration node) {
		return false;
	}
	
	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ParameterizedType node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ParenthesizedExpression node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(PostfixExpression node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(PrefixExpression node) {
		return true;
	}

	/**
	 * Use the type visitor to visit the node
	 */
	public boolean visit(PrimitiveType node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(QualifiedName node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(QualifiedType node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ReturnStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SimpleName node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SimpleType node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SingleMemberAnnotation node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SingleVariableDeclaration node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(StringLiteral node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SuperConstructorInvocation node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SuperFieldAccess node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SuperMethodInvocation node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SwitchCase node) {
		return true;
	}

	/**
	 * Only need to visit its children
	 */
	public boolean visit(SwitchStatement node) {
		return true;
	}

	/**
	 * Only need to visit its children
	 */
	public boolean visit(SynchronizedStatement node) {
		return true;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(TagElement node) {
		return false;
	}


	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(TextElement node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ThisExpression node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(ThrowStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(TryStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not occur in a block directly
	 */
	public boolean visit(TypeDeclaration node) {
		if (resultList == null) resultList = new ArrayList<TypeDeclaration>();
		resultList.add(node);
		return true;
	}

	/**
	 * Goto its children to find local type.
	 */
	public boolean visit(TypeDeclarationStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(TypeLiteral node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(TypeParameter node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(VariableDeclarationExpression node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(VariableDeclarationStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(VariableDeclarationFragment node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(WhileStatement node) {
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(WildcardType node) {
		return false;
	}

}
