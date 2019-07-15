package measurement.metric.size;

import java.util.List;

import nameTable.NameTableASTBridge;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import softwareStructure.SoftwareStructManager;

/**
 * @author Zhou Xiaocong
 * @since 2015年7月8日
 * @version 1.0
 * 
 * @update 2016/11/14
 * 		Use NameTableASTBridge to find AST node for compilation unit scope, detailed type definition and method definition.
 */
public class StatementCounterMetric extends SoftwareSizeMetric {
	private StatementCounter counter = new StatementCounter();
	
	@Override
	public void setSoftwareStructManager(SoftwareStructManager structManager) {
		if (structManager != this.structManager) {
			this.structManager = structManager;
			tableManager = structManager.getNameTableManager();
			parser = tableManager.getSouceCodeFileSet();
			
			counter.reset();
		}
	}

	@Override
	public void setMeasuringObject(NameScope objectScope) {
		if (this.objectScope != objectScope) {
			this.objectScope = objectScope;
			
			counter.reset();
		}
		if (objectScope.getScopeKind() == NameScopeKind.NSK_SYSTEM || objectScope.getScopeKind() == NameScopeKind.NSK_PACKAGE) counter.reset();
	}
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (tableManager == null || objectScope == null) return false;
		
		if (measure.match(SoftwareMeasureIdentifier.STMN) || measure.match(SoftwareMeasureIdentifier.LOOPSTMN) || 
				measure.match(SoftwareMeasureIdentifier.BRANCHSTMN)) {
			int totalStatementNumber = 0;
			if (measure.match(SoftwareMeasureIdentifier.STMN)) totalStatementNumber = counter.getTotalNumber();
			else if (measure.match(SoftwareMeasureIdentifier.LOOPSTMN)) totalStatementNumber = counter.getLoopNumber();
			else if (measure.match(SoftwareMeasureIdentifier.BRANCHSTMN)) totalStatementNumber = counter.getBranchNumber();
			if (totalStatementNumber == 0) {
				NameScopeKind kind = objectScope.getScopeKind();
				if (kind == NameScopeKind.NSK_SYSTEM) {
					totalStatementNumber = counterSystemStatement(measure);
				} else if (kind == NameScopeKind.NSK_COMPILATION_UNIT) {
					CompilationUnitScope unitScope = (CompilationUnitScope)objectScope;
					totalStatementNumber = counterCompilationUnitStatement(unitScope, measure);
				} else if (kind == NameScopeKind.NSK_PACKAGE) {
					PackageDefinition packageDefinition = (PackageDefinition)objectScope;
					totalStatementNumber = counterPackageStatement(packageDefinition, measure);
				} else if (kind == NameScopeKind.NSK_DETAILED_TYPE) {
					DetailedTypeDefinition typeDefinition = (DetailedTypeDefinition)objectScope;
					totalStatementNumber = counterClassStatement(typeDefinition, measure);
				} else if (kind == NameScopeKind.NSK_METHOD) {
					MethodDefinition methodDefinition = (MethodDefinition)objectScope;
					totalStatementNumber = counterMethodStatement(methodDefinition, measure);
				}
			}
			measure.setValue(totalStatementNumber);
			return true;
		}
		return false;
	}
	
	protected int counterSystemStatement(SoftwareMeasure measure) {
		List<CompilationUnitScope> unitList = tableManager.getAllCompilationUnitScopes();
		int totalNumber = 0;
		if (unitList == null) return totalNumber;
		
		for (CompilationUnitScope unit : unitList) {
			totalNumber += counterCompilationUnitStatement(unit, measure);
		}
		return totalNumber;
	}
	
	protected int counterCompilationUnitStatement(CompilationUnitScope unit, SoftwareMeasure measure) {
		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		CompilationUnit root = bridge.findASTNodeForCompilationUnitScope(unit);
		if (root == null) return 0;

		counter.reset();
		root.accept(counter);
		
		// Release the memory in the parser. When can findDeclarationForCompilationUnitScope, it will load 
		// file contents and AST to the memory.
		parser.releaseAST(unit.getUnitName());
		parser.releaseFileContent(unit.getUnitName());
		
		return counter.getStatementNumber(measure);
	}

	protected int counterPackageStatement(PackageDefinition packageDefinition, SoftwareMeasure measure) {
		List<CompilationUnitScope> unitList = packageDefinition.getCompilationUnitScopeList();
		int totalNumber = 0;
		if (unitList == null) return totalNumber;

		for (CompilationUnitScope unit : unitList) {
			totalNumber += counterCompilationUnitStatement(unit, measure);
		}
		return totalNumber;
	}

	protected int counterClassStatement(DetailedTypeDefinition type, SoftwareMeasure measure) {
		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		counter.reset();
		if (type.isAnonymous()) {
			AnonymousClassDeclaration typeDeclaration = bridge.findASTNodeForAnonymousClassDefinition(type);
			if (typeDeclaration == null) return 0;
			typeDeclaration.accept(counter);
		} else {
			TypeDeclaration typeDeclaration = bridge.findASTNodeForDetailedTypeDefinition(type);
			if (typeDeclaration == null) return 0;
			typeDeclaration.accept(counter);
		}
		return counter.getStatementNumber(measure);
	}
	
	protected int counterMethodStatement(MethodDefinition method, SoftwareMeasure measure) {
		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		MethodDeclaration methodDeclaration = bridge.findASTNodeForMethodDefinition(method);
		if (methodDeclaration == null) return 0;

		counter.reset();
		methodDeclaration.accept(counter);
		return counter.getStatementNumber(measure);
	}
}

class StatementCounter extends ASTVisitor {
	private int simpleStatementNumber = 0;
	private int ifPredicateNumber = 0;
	private int switchPredicateNumber = 0;
	private int switchCaseNumber = 0;
	private int doPredicateNumber = 0;
	private int whilePredicateNumber = 0;
	private int forPredicateNumber = 0;
	private int enhancedForNumber = 0;
	private int throwStatementNumber = 0;
	private int tryStatementNumber = 0;
	private int catchNumber = 0;
	private int synchronizeNumber = 0;
	
	public void reset() {
		simpleStatementNumber = 0;
		ifPredicateNumber = 0;
		switchPredicateNumber = 0;
		switchCaseNumber = 0;
		doPredicateNumber = 0;
		whilePredicateNumber = 0;
		forPredicateNumber = 0;
		enhancedForNumber = 0;		
		throwStatementNumber = 0;
		tryStatementNumber = 0;
		catchNumber = 0;
		synchronizeNumber = 0;
	}
	
	public int getStatementNumber(SoftwareMeasure measure) {
		int result = 0;
	
		if (measure.match(SoftwareMeasureIdentifier.STMN)) {
			return getTotalNumber();
		} else if (measure.match(SoftwareMeasureIdentifier.LOOPSTMN)) {
			return getLoopNumber();
		} else if (measure.match(SoftwareMeasureIdentifier.BRANCHSTMN)) {
			return getBranchNumber();
		}
		return result;
	}
	
	public int getTotalNumber() {
		return simpleStatementNumber+ifPredicateNumber+switchPredicateNumber+switchPredicateNumber+switchCaseNumber+
				doPredicateNumber+whilePredicateNumber+forPredicateNumber+enhancedForNumber+throwStatementNumber+
				tryStatementNumber+catchNumber+synchronizeNumber;
	}

	public int getSimpleStatementNumber() {
		return simpleStatementNumber;
	}

	public int getIfPredicateNumber() {
		return ifPredicateNumber;
	}

	public int getSwitchPredicateNumber() {
		return switchPredicateNumber;
	}

	public int getSwitchCaseNumber() {
		return switchCaseNumber;
	}

	public int getBranchNumber() {
		return ifPredicateNumber + switchPredicateNumber;
	}
	
	public int getDoPredicateNumber() {
		return doPredicateNumber;
	}

	public int getWhilePredicateNumber() {
		return whilePredicateNumber;
	}

	public int getForPredicateNumber() {
		return forPredicateNumber;
	}

	public int getEnhancedForNumber() {
		return enhancedForNumber;
	}
	
	public int getLoopNumber() {
		return doPredicateNumber + whilePredicateNumber + forPredicateNumber + enhancedForNumber;
	}
	
	public int getThrowStatementNumber() {
		return throwStatementNumber;
	}

	public int getTryStatementNumber() {
		return tryStatementNumber;
	}

	public int getCatchNumber() {
		return catchNumber;
	}

	public int getSynchronizeNumber() {
		return synchronizeNumber;
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
	 * Calculate the statements in anonymous class
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		return true;
	}


	/**
	 * Simple statement
	 */
	public boolean visit(AssertStatement node) {
		simpleStatementNumber++;
		return false;
	}

	/**
	 * Counter the statements in the block
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
	 * Simple statement
	 */
	public boolean visit(BreakStatement node) {
		simpleStatementNumber++;
		return false;
	}


	/**
	 * Counter the catchNumber, and counter the statements in its block 
	 */
	public boolean visit(CatchClause node) {
		catchNumber++;
		return true;
	}

	/**
	 * Counter the statements in its body 
	 */
	public boolean visit(CompilationUnit node) {
		return true;
	}

	/**
	 * Simple statement
	 */
	public boolean visit(ConstructorInvocation node) {
		simpleStatementNumber++;
		return true;
	}
	
	/**
	 * Simple statement
	 */
	public boolean visit(ContinueStatement node) {
		simpleStatementNumber++;
		return false;
	}

	/**
	 * Counter the do predicate, and then counter the statements in its block
	 */
	public boolean visit(DoStatement node) {
		doPredicateNumber++;
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(EmptyStatement node) {
		return false;
	}

	/**
	 * Counter enhanced for number, and then counter the statements in its block
	 */
	public boolean visit(EnhancedForStatement node) {
		enhancedForNumber++;
		return true;
	}

	/**
	 * We ignore statements in enum type (2018/06/15)!
	 * Simple statement
	 */
	public boolean visit(EnumConstantDeclaration node) {
//		simpleStatementNumber++;
		return false;
	}

	/**
	 * We ignore statements in enum type (2018/06/15)!
	 */
	public boolean visit(EnumDeclaration node) {
//		simpleStatementNumber++;
//		return true;
		return false;
	}

	/**
	 * Simple statement
	 */
	public boolean visit(ExpressionStatement node) {
		simpleStatementNumber++;
		return true;
	}

	/**
	 * Simple statement
	 */
	public boolean visit(FieldDeclaration node) {
		simpleStatementNumber++;
		return true;
	}

	/**
	 * Counter for predicate number and then counter statements in its block 
	 */
	public boolean visit(ForStatement node) {
		forPredicateNumber++;
		return true;
	}

	/**
	 * Counter if predicate number and then counter statements in its block 
	 */
	public boolean visit(IfStatement node) {
		ifPredicateNumber++;
		return true;
	}

	/**
	 * Simple statement
	 */
	public boolean visit(ImportDeclaration node) {
		simpleStatementNumber++;
		return false;
	}

	/**
	 * Counter statements in its block
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
	 * Counter statements in its body
	 */
	public boolean visit(MethodDeclaration node) {
		simpleStatementNumber++;
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(NormalAnnotation node) {
		return false;
	}

	/**
	 * Simple statements
	 */
	public boolean visit(PackageDeclaration node) {
		simpleStatementNumber++;
		return false;
	}
	
	/**
	 * Simple statement
	 */
	public boolean visit(ReturnStatement node) {
		simpleStatementNumber++;
		return true;
	}

	/**
	 * Ignore this kind of AST node so far
	 */
	public boolean visit(SingleMemberAnnotation node) {
		return false;
	}

	/**
	 * Simple statement
	 */
	public boolean visit(SuperConstructorInvocation node) {
		simpleStatementNumber++;
		return true;
	}
	
	/**
	 * Counter switch case number
	 */
	public boolean visit(SwitchCase node) {
		switchCaseNumber++;
		return false;
	}

	/**
	 * Counter switch number and then counter statements in its block 
	 */
	public boolean visit(SwitchStatement node) {
		switchPredicateNumber++;
		return true;
	}

	/**
	 * Counter synchronized number, and then counter statements in its block
	 */
	public boolean visit(SynchronizedStatement node) {
		synchronizeNumber++;
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
	 * Counter throws number
	 */
	public boolean visit(ThrowStatement node) {
		throwStatementNumber++;
		return true;
	}

	/**
	 * COunter try statement number, and then counter statements in its block
	 */
	public boolean visit(TryStatement node) {
		tryStatementNumber++;
		return true;
	}

	/**
	 * Counter its declaration as a simple statement, and then counter statements in its body
	 */
	public boolean visit(TypeDeclaration node) {
		simpleStatementNumber++;
		return true;
	}

	/**
	 * Counter its declaration as a simple statement, and then counter statements in its body
	 */
	public boolean visit(TypeDeclarationStatement node) {
		simpleStatementNumber++;
		return true;
	}

	/**
	 * Simple statement
	 */
	public boolean visit(VariableDeclarationStatement node) {
		simpleStatementNumber++;
		return true;
	}

	/**
	 * Counter while predicate, and then counter statements in its body
	 */
	public boolean visit(WhileStatement node) {
		whilePredicateNumber++;
		return true;
	}
}
