package gui.astViewer;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Deque;

import org.eclipse.jdt.core.dom.*;

public class ConciseASTVisitor extends SimpleASTVisitor {

	protected boolean isInMethodReturnType = false;			// 记录是否正在访问某个方法声明的返回类型
	// 记录当前正在访问的方法声明所声明的方法名字，目前只用一个字符串记录，存在的风险是，可能方法声明中又有方法声明（例如内部类、匿名类中的方法）时
	// 用一个字符串记录的方法声明可能是错误的！目前暂时忽略这种情况！
	protected String currentDeclareMethodName = null;		
	protected boolean isInMethodDeclaration = false;
	protected boolean isInMethodParameter = false;
	
	protected boolean isInMethodCallObject = false;
	protected boolean isInMethodCallParameter = false;
	protected Deque<String> methodCallStack = new ArrayDeque<String>();
	
	protected boolean isInElseBranch = false;
	protected boolean isInConditionalExpression = false;
	protected boolean isInThenBranch = false;
	
	public ConciseASTVisitor(StringBuffer buffer, CompilationUnit root) {
		super(buffer, root);
	}

	public String stateDependentMessage(ASTNode node) {
		String message = "";
		if (isInMethodReturnType && (node instanceof Type) && currentDeclareMethodName != null) {
			message = "[-> Return type of method {" + currentDeclareMethodName + "}]";
			return message;
		}
		if (isInMethodDeclaration && (node instanceof Block) && currentDeclareMethodName != null) {
			message = "[-> Body of method {" + currentDeclareMethodName + "}]";
			isInMethodDeclaration = false;      // 一旦访问过该方法体的语句块，就不再提供这种信息，因为方法体本身可能有很多块语句！
			return message;
		}
		if (isInMethodParameter && (node instanceof SingleVariableDeclaration) && currentDeclareMethodName != null) {
			message = "[-> Parameter of method {" + currentDeclareMethodName + "}]";
			return message;
		}
		if (isInMethodCallObject && (node instanceof Expression) && !methodCallStack.isEmpty()) {
			message = "[-> Object Expression for calling {" + methodCallStack.peekFirst() + "}]";
			return message;
		}
		if (isInMethodCallParameter && (node instanceof Expression) && !methodCallStack.isEmpty()) {
			message = "[-> Parameter Expression for calling {" + methodCallStack.peekFirst() + "}]";
			return message;
		}
		if (isInElseBranch && (node instanceof Statement)) {
			message = "[-> Else branch of the IfStatement]";
			return message;
		}
		if (isInThenBranch && (node instanceof Statement)) {
			message = "[-> Then branch of the IfStatement]";
			return message;
		}
		if (isInConditionalExpression && (node instanceof Expression)) {
			message = "[-> Conditional Expression of the If/Loop Statement]";
			isInConditionalExpression = false;
			return message;
		} 
		return message;
	}
	
	public boolean visit(MethodInvocation node) {
		String methodName = node.getName().getIdentifier();
		buffer.append("{" + methodName + "}\n");
		
		methodCallStack.addFirst(methodName);
		
		if (node.getExpression() != null) {
			isInMethodCallObject = true;
			node.getExpression().accept(this);
			isInMethodCallObject = false;
		}
		
		if (!node.typeArguments().isEmpty()) {
			for (Iterator<?> it = node.typeArguments().iterator(); it.hasNext(); ) {
				Type t = (Type) it.next();
				if (t != null) t.accept(this);
			}
		}
		
		for (Iterator<?> it = node.arguments().iterator(); it.hasNext(); ) {
			// 因为实际参数种可能还有方法调用及实参传递，所以这里要每次都设置 isInMethodCallParameter 这个标志！
			isInMethodCallParameter = true;
			Expression e = (Expression) it.next();
			if (e != null) e.accept(this);
			isInMethodCallParameter = false;
		}
		
		methodCallStack.removeFirst();
		return false;
	}
	
	public boolean visit(TypeDeclaration node) {
		buffer.append(node.isInterface() ? "{interface " : "{class " + node.getName().getFullyQualifiedName() + "}\n");

		if (!node.typeParameters().isEmpty()) {
			for (Iterator<?> it = node.typeParameters().iterator(); it.hasNext(); ) {
				TypeParameter t = (TypeParameter) it.next();
				if (t != null) t.accept(this);
			}
		}

		if (node.getSuperclassType() != null) node.getSuperclassType().accept(this);

		if (!node.superInterfaceTypes().isEmpty()) {
			for (Iterator<?> it = node.superInterfaceTypes().iterator(); it.hasNext(); ) {
				Type t = (Type) it.next();
				if (t != null) t.accept(this);
			}
		}

		for (Iterator<?> it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			if (d != null) d.accept(this);
		}

		return false;
	}

	public boolean visit(IfStatement node) {
		buffer.append("\n");
		isInConditionalExpression = true;
		node.getExpression().accept(this);
		isInConditionalExpression = false;

		isInThenBranch = true;
		node.getThenStatement().accept(this);
		isInThenBranch = false;
		
		if (node.getElseStatement() != null) {
			isInElseBranch = true;
			node.getElseStatement().accept(this);
			isInElseBranch = false;
		}
		return false;
	}

	/**
	 * 访问带前缀 (.) 的名字，我们希望得到它的全名，然后不再访问其子节点
	 */
	public boolean visit(QualifiedName node) {
		buffer.append("{" + getFullName(node) + "}\n");
		return false;
	}

	public boolean visit(QualifiedType node) {
		buffer.append("{" + getFullTypeName(node) + "}\n");
		return false;
	}
	
	public boolean visit(SimpleType node) {
		buffer.append("{" + getFullName(node.getName()) + "}\n");
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean visit(MethodDeclaration node) {
		currentDeclareMethodName = node.getName().getIdentifier();
		
		String message = node.getName().getIdentifier();
		if (!node.thrownExceptions().isEmpty()) {
			message = message + " throws ";
			for (Iterator<?> it = node.thrownExceptions().iterator(); it.hasNext(); ) {
				Name n = (Name) it.next();
				message = message + getFullName(n) + " ";
			}
			message = message.trim();
		}
		buffer.append("{" + message + "}\n");
		
		if (!node.isConstructor()) {
			if (node.getReturnType2() != null) {
				isInMethodReturnType = true;
				node.getReturnType2().accept(this);
				isInMethodReturnType = false;
			}
		}

		if (!node.typeParameters().isEmpty()) {
			for (Iterator<?> it = node.typeParameters().iterator(); it.hasNext(); ) {
				TypeParameter t = (TypeParameter) it.next();
				if (t != null) t.accept(this);
			}
		}

		isInMethodParameter = true;
		for (Iterator<?> it = node.parameters().iterator(); it.hasNext(); ) {
			SingleVariableDeclaration v = (SingleVariableDeclaration) it.next();
			if (v != null) v.accept(this);
		}
		isInMethodParameter = false;
		
		if (node.getBody() != null) {
			isInMethodDeclaration = true;
			node.getBody().accept(this);
			isInMethodDeclaration = false;
		}
		currentDeclareMethodName = null;

		return false;
	}
	
	
	/**
	 * 递归地获得一个名字节点所表示的全名
	 */
	private String getFullName(Name node) {
		if (node.isSimpleName()) return ((SimpleName)node).getIdentifier();		// 简单名字节点直接返回它的标示符（即名字）
		else {
			Name prefixNode = ((QualifiedName)node).getQualifier();
			// 递归地获取该名字的前缀
			return getFullName(prefixNode) + "." + ((QualifiedName)node).getName().getIdentifier();
		}
	}
	
	/**
	 * 递归地获得一个类型节点所表示的类型名字
	 */
	private String getFullTypeName(Type node) {
		// 暂时吧处理除  QualifiedType 和 SimpleType 以外的情况
		if (!node.isQualifiedType() && !node.isSimpleType()) return "";
		if (node.isSimpleType()) {
			Name nameNode = ((SimpleType)node).getName();
			// 注意 SimpleType 中的名字仍有可能是 QualifiedName，所以使用 getFullName() 获得全名
			return getFullName(nameNode); 
		} else {
			QualifiedType tempNode = (QualifiedType)node;
			Type prefixNode = tempNode.getQualifier(); 
			return getFullTypeName(prefixNode) + "." + tempNode.getName().getIdentifier();
		}
	}

}
