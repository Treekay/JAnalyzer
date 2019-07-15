package gui.astViewer;

import org.eclipse.jdt.core.dom.*;

public class SimpleASTVisitor extends ASTVisitor {
	protected StringBuffer buffer = null;
	protected int indent = 0;
	protected CompilationUnit root = null;
	
	public SimpleASTVisitor(StringBuffer buffer, CompilationUnit root) {
		this.buffer = buffer;
		this.root = root;
	}
	
	public void preVisit(ASTNode node) {
		if (isIgnoreNode(node)) return;
		buffer.append(generatePrefixString(node));
		buffer.append(stateDependentMessage(node));
		indent = indent + 1;
	}
	
	public void postVisit(ASTNode node) {
		if (isIgnoreNode(node)) return;
		indent = indent - 1;
	}
	
	public String stateDependentMessage(ASTNode node) {
		return "";
	}

	private boolean isIgnoreNode(ASTNode node) {
		if ((node instanceof EmptyStatement) || (node instanceof Modifier) || 
				(node instanceof Javadoc) || (node instanceof LineComment)) return true;
		return false;
	}
		
	/**
	 * 简单访问 Annotation及其子节点
	 */
	public boolean visit(AnnotationTypeDeclaration node) {
		buffer.append("\n");
		return true;
	}
	
	/**
	 * 简单访问  Annotation及其子节点
	 */
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		buffer.append("\n");
		return true;
	}
	
	/**
	 * 暂时不访问匿名类及其子节点
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		buffer.append("\n");
		return true;
	}
	
	/**
	 * 访问数组访问节点
	 */
	public boolean visit(ArrayAccess node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问数组创建节点
	 */
	public boolean visit(ArrayCreation node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问数组初始化节点
	 */
	public boolean visit(ArrayInitializer node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问数组类型节点
	 */
	public boolean visit(ArrayType node) {
		buffer.append("\n");
		return true;
	}

	/** 
	 * 访问断言语句节点
	 */
	public boolean visit(AssertStatement node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问赋值语句节点
	 */
	public boolean visit(Assignment node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问块语句节点
	 */ 
	public boolean visit(Block node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问注释块节点
	 */
	public boolean visit(BlockComment node) {
		this.buffer.append("/* Comments */\n");
		return false;
	}

	/**
	 * 访问布尔常量节点
	 */
	public boolean visit(BooleanLiteral node) {
		if (node.booleanValue() == true) this.buffer.append("{true}\n");
		else this.buffer.append("{false}\n");

		return true;
	}

	/**
	 * 访问 break 语句节点
	 */
	public boolean visit(BreakStatement node) {
		buffer.append("{break}\n");
		return true;
	}

	/** 
	 * 访问类型转换表达式节点
	 */
	public boolean visit(CastExpression node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问 catch 语句块
	 */
	public boolean visit(CatchClause node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问字符常量节点
	 */
	public boolean visit(CharacterLiteral node) {
		this.buffer.append("{" + node.getEscapedValue()+"}\n");
		return false;
	}

	public boolean visit(ClassInstanceCreation node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问根节点
	 */
	public boolean visit(CompilationUnit node) {
		buffer.append("\n");
		return true;
	}
	
	/**
	 * 访问条件表达式节点
	 */
	public boolean visit(ConditionalExpression node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问构造方法调用节点
	 */
	public boolean visit(ConstructorInvocation node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问 continue 语句节点
	 */
	public boolean visit(ContinueStatement node) {
		buffer.append("{continue}\n");
		return true;
	}

	/**
	 * 访问 do 语句节点
	 */
	public boolean visit(DoStatement node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问空语句节点
	 */
	public boolean visit(EmptyStatement node) {
		return false;
	}

	/**
	 * 访问增强的 for 循环语句节点
	 */
	public boolean visit(EnhancedForStatement node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问常量枚举声明语句节点
	 */
	public boolean visit(EnumConstantDeclaration node) {
		buffer.append("\n");
		return true;
	}

	/** 
	 * 访问枚举声明语句节点
	 */
	public boolean visit(EnumDeclaration node) {
		buffer.append("\n");
		System.out.println("This is enum node");
		return true;
	}

	/**
	 * 访问表达式语句节点
	 */
	public boolean visit(ExpressionStatement node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问字段访问语句节点
	 */
	public boolean visit(FieldAccess node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问字段声明语句节点 
	 */
	public boolean visit(FieldDeclaration node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问 for 循环语句节点
	 */
	public boolean visit(ForStatement node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问 if 语句节点
	 */
	public boolean visit(IfStatement node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问 import 语句节点
	 */
	@SuppressWarnings("deprecation")
	public boolean visit(ImportDeclaration node) {
		buffer.append("{import ");
		if (node.getAST().apiLevel() >= AST.JLS2) {
			if (node.isStatic()) this.buffer.append("static ");
		}
		buffer.append(node.getName().getFullyQualifiedName());
		if (node.isOnDemand()) this.buffer.append(".*");
		buffer.append("}\n");
		return false;
	}

	/**
	 * 访问中缀表达式节点
	 */
	public boolean visit(InfixExpression node) {
		buffer.append("{" + node.getOperator().toString() + "}\n");
		return true;
	}

	/**
	 * 访问 instanceof 表达式语句节点
	 */
	public boolean visit(InstanceofExpression node) {
		buffer.append("{ instanceof }\n");
		return true;
	}

	/**
	 * 访问初始化语句节点
	 */
	public boolean visit(Initializer node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(Javadoc node) {
		return false;
	}

	/**
	 * 访问带标签语句节点
	 */
	public boolean visit(LabeledStatement node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问行注释节点
	 */
	public boolean visit(LineComment node) {
		return false;
	}


	/**
	 * 访问标记节点
	 */
	public boolean visit(MarkerAnnotation node) {
		buffer.append("\n");
		return true;
	}


	public boolean visit(MemberRef node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(MemberValuePair node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(MethodRef node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(MethodRefParameter node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问访问声明节点
	 */
	public boolean visit(MethodDeclaration node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 访问方法调用节点
	 */
	public boolean visit(MethodInvocation node) {
		buffer.append("\n");
		return true;
	}

	/**
	 * 前面已经处理像 public, static 这样的修饰词节点，这里不再访问！
	 */
	public boolean visit(Modifier node) {
		return false;
	}

	public boolean visit(NormalAnnotation node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(NullLiteral node) {
		buffer.append("{null}\n");
		return false;
	}

	public boolean visit(NumberLiteral node) {
		buffer.append("{" + node.getToken() + "}\n");
		return false;
	}

	public boolean visit(PackageDeclaration node) {
		buffer.append("{package ");
		buffer.append(node.getName().getFullyQualifiedName());
		buffer.append("}\n");
		return false;
	}


	public boolean visit(ParameterizedType node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(ParenthesizedExpression node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(PostfixExpression node) {
		buffer.append("{" + node.getOperator().toString() + "}\n");
		return true;
	}

	public boolean visit(PrefixExpression node) {
		buffer.append("{" + node.getOperator().toString() + "}\n");
		return true;
	}

	public boolean visit(PrimitiveType node) {
		buffer.append("{" + node.getPrimitiveTypeCode().toString() + "}\n");
		return false;
	}

	public boolean visit(QualifiedName node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(QualifiedType node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(ReturnStatement node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(SimpleName node) {
		buffer.append("{" + node.getIdentifier() + "}\n"); 
		return false;
	}

	public boolean visit(SimpleType node) {
		buffer.append("\n");
		return true;
	}


	public boolean visit(SingleMemberAnnotation node) {
		buffer.append("\n");
		return true;
	}


	public boolean visit(SingleVariableDeclaration node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(StringLiteral node) {
		buffer.append("{" + node.getEscapedValue() + "}\n");
		return false;
	}

	public boolean visit(SuperConstructorInvocation node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(SuperFieldAccess node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(SuperMethodInvocation node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(SwitchCase node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(SwitchStatement node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(SynchronizedStatement node) {
		buffer.append("\n");
		return true;
	}


	public boolean visit(TagElement node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(TextElement node) {
		buffer.append("{" + node.getText() + "}\n");
		return false;
	}

	public boolean visit(ThisExpression node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(ThrowStatement node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(TryStatement node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(TypeDeclaration node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(TypeDeclarationStatement node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(TypeLiteral node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(TypeParameter node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(VariableDeclarationExpression node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(VariableDeclarationStatement node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(VariableDeclarationFragment node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(WhileStatement node) {
		buffer.append("\n");
		return true;
	}

	public boolean visit(WildcardType node) {
		buffer.append("\n");
		return true;
	}
	

	/**
	 * 生成访问 AST 节点时，在访问节点的子节点之前应该生成的字符串
	 */
	private String generatePrefixString(ASTNode node) {
		return getIndentString() + getLineString(node) + getSimpleClassName(node) + getModifierString(node);
	}
	
	/**
	 * 计算该节点应该缩进多少！
	 */
	private String getIndentString() {
		final String indentSpace = "    ";
		String result = "";
		for (int i = 0; i < indent; i++) result = result + indentSpace;
		
		return result;
	}

	/**
	 * 返回该节点所在编译单元（Java源文件）所在的行号！
	 */
	private String getLineString(ASTNode node) {
		return root.getLineNumber(node.getStartPosition()) + " ";
	}
	
	/**
	 * 返回该节点的一些修饰符，如public, static, final 等等
	 */
	private String getModifierString(ASTNode node) {
		String modifiers = "";
		if (node instanceof BodyDeclaration) {
			int mod = ((BodyDeclaration)node).getModifiers();
			if (Modifier.isAbstract(mod)) modifiers += Modifier.ModifierKeyword.ABSTRACT_KEYWORD.toString() + " ";
			if (Modifier.isFinal(mod)) modifiers += Modifier.ModifierKeyword.FINAL_KEYWORD.toString() + " ";
			if (Modifier.isNative(mod)) modifiers += Modifier.ModifierKeyword.NATIVE_KEYWORD.toString() + " ";
			if (Modifier.isPrivate(mod)) modifiers += Modifier.ModifierKeyword.PRIVATE_KEYWORD.toString() + " ";
			if (Modifier.isProtected(mod)) modifiers += Modifier.ModifierKeyword.PROTECTED_KEYWORD.toString() + " ";
			if (Modifier.isPublic(mod)) modifiers += Modifier.ModifierKeyword.PUBLIC_KEYWORD.toString() + " ";
			if (Modifier.isStatic(mod)) modifiers += Modifier.ModifierKeyword.STATIC_KEYWORD.toString() + " ";
			if (Modifier.isStrictfp(mod)) modifiers += Modifier.ModifierKeyword.STRICTFP_KEYWORD.toString() + " ";
			if (Modifier.isSynchronized(mod)) modifiers += Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD.toString() + " ";
			if (Modifier.isTransient(mod)) modifiers += Modifier.ModifierKeyword.TRANSIENT_KEYWORD.toString() + " ";
			if (Modifier.isVolatile(mod)) modifiers += Modifier.ModifierKeyword.VOLATILE_KEYWORD.toString() + " ";	
			modifiers = "[" + modifiers.trim() + "]";
		}
		return modifiers;
	}
	
	/** 
	 * 返回节点所属的类名
	 */
	private String getSimpleClassName(ASTNode node) {
		String className = node.getClass().getName();
		int index = className.lastIndexOf(".");
		if (index > 0) className = className.substring(index+1);
		return className;
	}
	
}
