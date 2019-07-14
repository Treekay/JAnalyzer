package nameTable.visitor;

import java.io.PrintWriter;
import java.util.List;

import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.ImportedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.SystemScope;

/**
 * A visitor to print all reference accepted by the filter.
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ14ÈÕ
 * @version 1.0
 *
 */
public class NameReferencePrinter extends NameTableVisitor {

	private PrintWriter writer = null;
	private int indent = 0;
	private boolean printBindedDefinition = false;
	
	// A filter to print appropriate name reference
	private NameTableFilter filter = null;
	
	public NameReferencePrinter() {
		writer = new PrintWriter(System.out);
	}

	public NameReferencePrinter(PrintWriter writer) {
		this.writer = writer;
	}

	public NameReferencePrinter(PrintWriter writer, NameTableFilter filter) {
		this.writer = writer;
		this.filter = filter;
	}
	
	public void setFilter(NameTableFilter filter) {
		this.filter = filter;
	}
	
	public void setPrintBindedDefinition() {
		printBindedDefinition = true;
	}
	
	/**
	 * Print references in system scope. In general there is no reference in system scope directly!
	 */
	public boolean visit(SystemScope scope) {
		List<NameReference> referenceList = scope.getReferenceList(); 
		if (referenceList != null) {
			for (NameReference reference : referenceList) {
				if (printBindedDefinition) reference.resolveBinding();
				
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				for (NameReference leafReference : leafReferenceList) {
					if (!accept(leafReference)) continue;
					
					writer.print(getIndentString(indent) + leafReference.getLocation() + " " + leafReference.getName() + " : " + leafReference.getReferenceKind());
					if (printBindedDefinition) {
						if (leafReference.isResolved()) {
							writer.println("[" + leafReference.getDefinition().getUniqueId() + "]");
						} else writer.println("[Not resolved!]");
					} else writer.println();
				}
			}
		}

		List<PackageDefinition> packageList = scope.getPackageList();
		if (packageList != null) {
			for (PackageDefinition name : packageList) {
				if (accept(name)) name.accept(this);
			}
		}
		return false;
	}
	
	/**
	 * Print references in package definition. In general there is no reference in package directly!
	 */
	public boolean visit(PackageDefinition scope) {
		List<NameReference> referenceList = scope.getReferenceList(); 
		if (referenceList != null) {
			for (NameReference reference : referenceList) {
				if (printBindedDefinition) reference.resolveBinding();
				
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				for (NameReference leafReference : leafReferenceList) {
					if (!accept(leafReference)) continue;
					
					writer.print(getIndentString(indent) + leafReference.getLocation() + " " + leafReference.getName() + " : " + leafReference.getReferenceKind());
					if (printBindedDefinition) {
						if (leafReference.isResolved()) {
							writer.println("[" + leafReference.getDefinition().getUniqueId() + "]");
						} else writer.println("[Not resolved!]");
					} else writer.println();
				}
			}
		}
		return true;
	}
	
	
	/**
	 * Print reference in the compilation unit scope
	 */
	public boolean visit(CompilationUnitScope scope) {
		PackageDefinition packageDefinition = scope.getEnclosingPackage();
		writer.println("Package: [" + packageDefinition.getFullQualifiedName() + "], Compilation unit : [" + scope.getUnitName() + "]");

		List<NameReference> referenceList = scope.getReferenceList(); 
		if (referenceList != null) {
			for (NameReference reference : referenceList) {
				if (printBindedDefinition) reference.resolveBinding();
				
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				for (NameReference leafReference : leafReferenceList) {
					if (!accept(leafReference)) continue;
					
					writer.print(getIndentString(indent+1) + leafReference.getLocation() + " " + leafReference.getName() + " : " + leafReference.getReferenceKind());
					if (printBindedDefinition) {
						if (leafReference.isResolved()) {
							writer.println("[" + leafReference.getDefinition().getUniqueId() + "]");
						} else writer.println("[Not resolved!]");
					} else writer.println();
				}
			}
		}
	
		indent = 1;
		List<TypeDefinition> types = scope.getTypeList();
		if (types != null) {
			for (TypeDefinition type : types) {
				if (!accept(type)) continue;
				
				if (type.isEnumType()) {
					EnumTypeDefinition enumType = (EnumTypeDefinition)type;
					enumType.accept(this);
				} else if (type.isDetailedType()) {
					DetailedTypeDefinition detailedType = (DetailedTypeDefinition)type;
					detailedType.accept(this);
				}
			}
		}
		indent = 0;
		return false;
	}
	
	/**
	 * Print references in detailed type definition.
	 */
	public boolean visit(DetailedTypeDefinition scope) {
		if (scope.isInterface()) writer.print(getIndentString(indent) + "Interface: ");
		else writer.print(getIndentString(indent) + "Class: ");
		writer.println(scope.getFullQualifiedName() + "[id: " + scope.getUniqueId() + "]");
		
		List<NameReference> referenceList = scope.getReferenceList(); 
		if (referenceList != null) {
			for (NameReference reference : referenceList) {
				if (printBindedDefinition) reference.resolveBinding();
				
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				for (NameReference leafReference : leafReferenceList) {
					if (!accept(leafReference)) continue;
					
					writer.print(getIndentString(indent+1) + leafReference.getLocation() + " " + leafReference.getName() + " : " + leafReference.getReferenceKind());
					if (printBindedDefinition) {
						if (leafReference.isResolved()) {
							writer.println("[" + leafReference.getDefinition().getUniqueId() + "]");
						} else writer.println("[Not resolved!]");
					} else writer.println();
				}
			}
		}
		
		List<MethodDefinition> methodList = scope.getMethodList();
		if (methodList != null) {
			writer.println(getIndentString(indent+1) + "Method: ");
			for (MethodDefinition method : methodList) {
				if (!accept(method)) continue;
				
				indent = indent + 2;
				// Use this printer to print method recursively!
				method.accept(this);
				indent = indent - 2;
			}
		}
		
		List<DetailedTypeDefinition> internalTypeList = scope.getTypeList();
		if (internalTypeList != null) {
			writer.println(getIndentString(indent+1) + "Internal type: ");
			for (DetailedTypeDefinition internalType : internalTypeList) {
				if (!accept(internalType)) continue;
				
				indent = indent + 2;
				// Use this printer to print internal type recursively!
				internalType.accept(this);
				indent = indent - 2;
			}
		}
		return false;
	}
	
	/**
	 * There is not any reference in imported type definition
	 */
	public boolean visit(ImportedTypeDefinition scope) {
		return false;
	}
	
	/**
	 * Print reference in enum type definition
	 */
	public boolean visit(EnumTypeDefinition scope) {
		writer.println("Enum: " + scope.getFullQualifiedName() + "[id: " + scope.getUniqueId() + "]");
		
		List<NameReference> referenceList = scope.getReferenceList(); 
		if (referenceList != null) {
			for (NameReference reference : referenceList) {
				if (printBindedDefinition) reference.resolveBinding();
				
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				for (NameReference leafReference : leafReferenceList) {
					if (!accept(leafReference)) continue;
					
					writer.print(getIndentString(indent+1) + leafReference.getLocation() + " " + leafReference.getName() + " : " + leafReference.getReferenceKind());
					if (printBindedDefinition) {
						if (leafReference.isResolved()) {
							writer.println("[" + leafReference.getDefinition().getUniqueId() + "]");
						} else writer.println("[Not resolved!]");
					} else writer.println();
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Print the method definition
	 */
	public boolean visit(MethodDefinition scope) {
		writer.print(getIndentString(indent) + scope.getSimpleName() + "(");
		List<VariableDefinition> vars = scope.getParameterList();
		if (vars != null) {
			boolean isFirst = true;
			for (VariableDefinition var : vars) {
				if (isFirst) {
					writer.print(var.getType().getName());
					isFirst = false;
				} else writer.print(", " + var.getType().getName());
			}
		}
		TypeReference returnType = scope.getReturnType();
		if (returnType != null) {
			writer.println(") : " + scope.getReturnType().getName());
		} else {
			if (scope.isConstructor()) {
				writer.println(") : Constructor");
			} else {
				writer.println(")");
			}
		}

		List<NameReference> referenceList = scope.getReferenceList(); 
		if (referenceList != null) {
			for (NameReference reference : referenceList) {
				if (printBindedDefinition) reference.resolveBinding();
				
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				for (NameReference leafReference : leafReferenceList) {
					if (!accept(leafReference)) continue;
					
					writer.print(getIndentString(indent+1) + leafReference.getLocation() + " " + leafReference.getName() + " : " + leafReference.getReferenceKind());
					if (printBindedDefinition) {
						if (leafReference.isResolved()) {
							writer.println("[" + leafReference.getDefinition().getUniqueId() + "]");
						} else writer.println("[Not resolved!]");
					} else writer.println();
				}
			}
		}
		
		LocalScope bodyScope = scope.getBodyScope();
		if (bodyScope != null) {
			indent = indent + 1;
			bodyScope.accept(this);
			indent = indent - 1;
		}
		return false;
	}

	/**
	 * Get local variables and types defined in a local types
	 */
	public boolean visit(LocalScope scope) {
		writer.println(getIndentString(indent) + "Local scope: [" + scope.getScopeStart().toString() + ", " + scope.getScopeEnd() + "]");

		List<NameReference> referenceList = scope.getReferenceList(); 
		if (referenceList != null) {
			for (NameReference reference : referenceList) {
				if (printBindedDefinition) reference.resolveBinding();
				
				List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
				for (NameReference leafReference : leafReferenceList) {
					if (!accept(leafReference)) continue;
					
					writer.print(getIndentString(indent+1) + leafReference.getLocation() + " " + leafReference.getName() + " : " + leafReference.getReferenceKind());
					if (printBindedDefinition) {
						if (leafReference.isResolved()) {
							writer.println("[" + leafReference.getDefinition().getUniqueId() + "]");
						} else writer.println("[Not resolved!]");
					} else writer.println();
				}
			}
		}
		
		List<DetailedTypeDefinition> types = scope.getLocalTypeList();
		if (types != null) {
			for (DetailedTypeDefinition type : types) {
				if (accept(type)) {
					indent = indent + 1;
					type.accept(this);
					indent = indent - 1;
				}
			}
		}
		
		List<LocalScope> subscopeList = scope.getSubLocalScope();
		if (subscopeList != null) {
			for (LocalScope subscope : subscopeList) {
				indent = indent + 1;
				subscope.accept(this);
				indent = indent - 1;
			}
		}
		return false;
	}
	
	private String getIndentString(int indent) {
		int indentLength = 4;
		char[] indentArray = new char[indent * indentLength];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = ' ';
		
		return new String(indentArray);
	}
	
	private boolean accept(NameDefinition definition) {
		if (filter == null) return true;
		else return filter.accept(definition);
	}
	
	private boolean accept(NameReference reference) {
		if (filter == null) return true;
		else return filter.accept(reference);
	}
	
}
