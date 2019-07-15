package measurement.metric.size;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.creator.CFGCreator;
import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import nameTable.NameTableASTBridge;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import softwareStructure.SoftwareStructManager;
import sourceCodeAST.SourceCodeLocation;

/**
 * @author Zhou Xiaocong
 * @since 2018年6月10日
 * @version 1.0
 *
 */
public class CFGNodeCounterMetric extends SoftwareSizeMetric {
	int totalNodeCounter = 0;
	int predicateNodeCounter = 0;
	
	@Override
	public void setSoftwareStructManager(SoftwareStructManager structManager) {
		if (structManager != this.structManager) {
			this.structManager = structManager;
			tableManager = structManager.getNameTableManager();
			parser = tableManager.getSouceCodeFileSet();
			
			totalNodeCounter = 0;
			predicateNodeCounter = 0;
		}
	}

	@Override
	public void setMeasuringObject(NameScope objectScope) {
		if (this.objectScope != objectScope) {
			this.objectScope = objectScope;
			
			totalNodeCounter = 0;
			predicateNodeCounter = 0;
		}
	}
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (tableManager == null || objectScope == null) return false;
		
		if (measure.match(SoftwareMeasureIdentifier.CFGNODE) || measure.match(SoftwareMeasureIdentifier.CFGPREDICATE)) {
			if (totalNodeCounter == 0) {
				NameScopeKind kind = objectScope.getScopeKind();
				if (kind == NameScopeKind.NSK_SYSTEM) {
					counterSystemCFGNode();
				} else if (kind == NameScopeKind.NSK_COMPILATION_UNIT) {
					CompilationUnitScope unitScope = (CompilationUnitScope)objectScope;
					counterCompilationUnitCFGNode(unitScope);
				} else if (kind == NameScopeKind.NSK_PACKAGE) {
					PackageDefinition packageDefinition = (PackageDefinition)objectScope;
					counterPackageCFGNode(packageDefinition);
				} else if (kind == NameScopeKind.NSK_DETAILED_TYPE) {
					DetailedTypeDefinition typeDefinition = (DetailedTypeDefinition)objectScope;
					counterClassCFGNode(typeDefinition);
				} else if (kind == NameScopeKind.NSK_METHOD) {
					MethodDefinition methodDefinition = (MethodDefinition)objectScope;
					counterMethodCFGNode(methodDefinition);
				}
			}
			if (measure.match(SoftwareMeasureIdentifier.CFGNODE)) measure.setValue(totalNodeCounter);
			else measure.setValue(predicateNodeCounter);
			return true;
		}
		return false;
	}
	
	protected void counterSystemCFGNode() {
		List<CompilationUnitScope> unitList = tableManager.getAllCompilationUnitScopes();
		if (unitList == null) return;
		
		for (CompilationUnitScope unit : unitList) {
			counterCompilationUnitCFGNode(unit);
		}
	}
	
	protected void counterCompilationUnitCFGNode(CompilationUnitScope unit) {
		List<DetailedTypeDefinition> typeList = tableManager.getAllDetailedTypeDefinitions(unit);
		for (DetailedTypeDefinition type : typeList) {
			if (type.isInterface()) continue;
			
			counterClassCFGNode(unit, type);
		}
		
		// Release the memory in the parser. When can findDeclarationForCompilationUnitScope, it will load 
		// file contents and AST to the memory.
		parser.releaseAST(unit.getUnitName());
		parser.releaseFileContent(unit.getUnitName());
	}

	protected void counterPackageCFGNode(PackageDefinition packageDefinition) {
		List<CompilationUnitScope> unitList = packageDefinition.getCompilationUnitScopeList();
		if (unitList == null) return;

		for (CompilationUnitScope unit : unitList) {
			counterCompilationUnitCFGNode(unit);
		}
	}

	protected void counterClassCFGNode(DetailedTypeDefinition type) {
		CompilationUnitScope unit = tableManager.getEnclosingCompilationUnitScope(type);
		counterClassCFGNode(unit, type);
	}
	
	@SuppressWarnings({ "unchecked" })
	protected void counterClassCFGNode(CompilationUnitScope unit, DetailedTypeDefinition type) {
		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		String unitFileName = unit.getUnitName();
		CompilationUnit astRoot = tableManager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitFileName);
		
		List<BodyDeclaration> bodyList = null;
		
		if (type.isAnonymous()) {
			AnonymousClassDeclaration classDeclaration = bridge.findASTNodeForAnonymousClassDefinition(type);
			if (classDeclaration == null) {
				throw new AssertionError("Can not find declaration for anonymous class " + type.getUniqueId());
			}
			bodyList = classDeclaration.bodyDeclarations();
		} else {
			TypeDeclaration classDeclaration = bridge.findASTNodeForDetailedTypeDefinition(type);
			if (classDeclaration == null) {
				throw new AssertionError("Can not find declaration for class " + type.getUniqueId());
			}
			
			bodyList = classDeclaration.bodyDeclarations();
		}
		if (bodyList == null) return;
		
		for (BodyDeclaration bodyDecl : bodyList) {
			if (bodyDecl.getNodeType() == ASTNode.INITIALIZER) {
				Initializer initializer = (Initializer)bodyDecl;
				SourceCodeLocation location = SourceCodeLocation.getStartLocation(bodyDecl, astRoot, unitFileName);
				String id = "Initializer@" + location.getLineNumber() + ":" + location.getColumn();
				Block body = initializer.getBody();
				CFGCreator cfgCreator = new CFGCreator(unitFileName, astRoot);
				ControlFlowGraph cfg = cfgCreator.create(body, id, id, id);
				
				if (cfg != null) counterCFGNode(cfg);
			} else if (bodyDecl.getNodeType() == ASTNode.METHOD_DECLARATION) {
				MethodDeclaration methodDeclaration = (MethodDeclaration)bodyDecl;

				CFGCreator cfgCreator = new CFGCreator(unitFileName, astRoot);
				ControlFlowGraph cfg = cfgCreator.create(methodDeclaration, type.getSimpleName());
				if (cfg != null) counterCFGNode(cfg);
			}
		}
		// We do not process types in a type. All types should be processed in the compilation unit scope!
		
	}
	
	protected void counterMethodCFGNode(MethodDefinition method) {
		NameTableASTBridge bridge = new NameTableASTBridge(tableManager);
		MethodDeclaration methodDeclaration = bridge.findASTNodeForMethodDefinition(method);
		if (methodDeclaration == null) return;
		
		CompilationUnitScope unit = tableManager.getEnclosingCompilationUnitScope(method);
		CompilationUnit astRoot = bridge.findASTNodeForCompilationUnitScope(unit);
		if (astRoot == null) return;
		
		String unitFileName = unit.getUnitName();
		
		CFGCreator cfgCreator = new CFGCreator(unitFileName, astRoot);
		ControlFlowGraph cfg = cfgCreator.create(methodDeclaration, method.getSimpleName());
		if (cfg != null) counterCFGNode(cfg);
	}
	
	protected void counterCFGNode(ControlFlowGraph cfg) {
		List<GraphNode> nodeList = cfg.getAllNodes();
		if (nodeList == null) return;
		
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				if (!node.isVirtual()) {
					totalNodeCounter++;
					if (node.isPredicate()) predicateNodeCounter++;
				}
			}
		}
	}
}
