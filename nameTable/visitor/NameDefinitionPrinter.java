package nameTable.visitor;

import java.io.PrintWriter;
import java.util.List;

import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumConstantDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.ImportedStaticMemberDefinition;
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
import sourceCodeAST.SourceCodeLocation;

/**
 * A visitor to print all name definitions accepted by the filters. The name definitions will be printed to a text file according 
 * to pre-defined formation.
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ14ÈÕ
 * @version 1.0
 *
 */
public class NameDefinitionPrinter extends NameTableVisitor {
	private PrintWriter writer = null;
	private int indent = 0;
	private boolean printVariable = false;
	
	// A filter to print appropriate name definition
	private NameTableFilter filter = null;
	
	public NameDefinitionPrinter() {
		writer = new PrintWriter(System.out);
	}

	public NameDefinitionPrinter(PrintWriter writer) {
		this.writer = writer;
	}

	public NameDefinitionPrinter(PrintWriter writer, NameTableFilter filter) {
		this.writer = writer;
		this.filter = filter;
	}
	
	public void setFilter(NameTableFilter filter) {
		this.filter = filter;
	}
	
	public void setPrintVariable(boolean flag) {
		this.printVariable = flag;
	}
	
	public void close() {
		this.writer.close();
	}

	/**
	 * Print imported type and static member in the system scope, and then print the package definition by this printer recursively!
	 */
	public boolean visit(SystemScope scope) {
		List<ImportedTypeDefinition> importedTypeList = scope.getImportedTypeList();
		if (importedTypeList != null) {
			writer.println("Imported Type:");
			for (ImportedTypeDefinition type : importedTypeList) {
				if (accept(type)) {
					indent = indent + 1;
					type.accept(this);
					indent = indent - 1;
				}
			}
		}
		
		List<ImportedStaticMemberDefinition> importedStaticMemberList = scope.getImportedStaticMemberList();
		if (importedStaticMemberList != null) {
			for (ImportedStaticMemberDefinition member : importedStaticMemberList) {
				writer.println("Imported Static Member:");
				if (accept(member)) {
					writer.println(getIndentString(1) + member.getFullQualifiedName());
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
	 * Print the definition in the package, we use this printer to print visit its compilation unit scope recursively!
	 */
	public boolean visit(PackageDefinition scope) {
		List<CompilationUnitScope> unitScopeList = scope.getCompilationUnitScopeList();
		if (unitScopeList != null) {
			for (CompilationUnitScope unitScope : unitScopeList) unitScope.accept(this);
		}
		return false;
	}
	
	
	/**
	 * Get the type definitions in a compilation unit scope
	 */
	public boolean visit(CompilationUnitScope scope) {
		PackageDefinition packageDefinition = scope.getEnclosingPackage();
		writer.println("Package: [" + packageDefinition.getFullQualifiedName() + "], Compilation unit : [" + scope.getUnitName() + "]");

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
	 * Get the definitions defined in a detailed type, including its fields, methods and types.
	 */
	public boolean visit(DetailedTypeDefinition scope) {
		String locationString = "-1:0";
		SourceCodeLocation location = scope.getLocation();
		if (location != null) locationString = location.toString();
		if (scope.isInterface()) writer.print(getIndentString(indent) + locationString + " Interface: ");
		else writer.print(getIndentString(indent) + locationString + " Class: ");
		writer.println(scope.getFullQualifiedName());
		
		List<TypeReference> superTypeList = scope.getSuperList();
		if (superTypeList != null) {
			writer.println(getIndentString(indent+1) + "Super type: ");
			for (TypeReference superTypeReference : superTypeList) {
				if (superTypeReference.resolveBinding()) {
					writer.println(getIndentString(indent+2) + superTypeReference.getName() + 
							"[" + superTypeReference.getDefinition().getUniqueId() + "]");
				} else {
					writer.println(getIndentString(indent+2) + superTypeReference.getName() + 
							"[Not resolved!]");
				}
				
			}
		}
		
		List<FieldDefinition> fieldList = scope.getFieldList();
		if (fieldList != null) {
			writer.println(getIndentString(indent+1) + "Field: ");
			for (FieldDefinition field : fieldList) {
				if (!accept(field)) continue;
				location = field.getLocation();
				if (location != null) locationString = location.toString();
				else locationString = "-1:0";
				if (scope.isInterface()) writer.print(getIndentString(indent) + locationString + " Interface: ");
				TypeReference fieldType = field.getType();
				writer.println(getIndentString(indent+2) + locationString + " " + field.getSimpleName() + " : " + 
						fieldType.getName());
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
	 * Get the definitions defined in a detailed type, including its fields, methods and types.
	 */
	public boolean visit(ImportedTypeDefinition scope) {
		String locationString = "-1:0";
		SourceCodeLocation location = scope.getLocation();
		if (location != null) locationString = location.toString();
		if (scope.isInterface()) writer.print(getIndentString(indent) + locationString + " Interface: ");
		else writer.print(getIndentString(indent) + locationString + " Class: ");
		writer.println(scope.getFullQualifiedName());
		
		List<TypeReference> superTypeList = scope.getSuperList();
		if (superTypeList != null) {
			writer.println(getIndentString(indent+1) + "Super type: ");
			for (TypeReference superTypeReference : superTypeList) {
				if (superTypeReference.resolveBinding()) {
					writer.println(getIndentString(indent+2) + superTypeReference.getName() + 
							"[" + superTypeReference.getDefinition().getUniqueId() + "]");
				} else {
					writer.println(getIndentString(indent+2) + superTypeReference.getName() + 
							"[Not resolved!]");
				}
				
			}
		}
		
		List<FieldDefinition> fieldList = scope.getFieldList();
		if (fieldList != null) {
			writer.println(getIndentString(indent+1) + "Field: ");
			for (FieldDefinition field : fieldList) {
				if (!accept(field)) continue;
				location = field.getLocation();
				if (location != null) locationString = location.toString();
				else locationString = "-1:0";
				TypeReference fieldType = field.getType();
				writer.println(getIndentString(indent+2) + locationString + " " + field.getSimpleName() + " : " + 
						fieldType.getName());
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
		
		List<ImportedTypeDefinition> internalTypeList = scope.getTypeList();
		if (internalTypeList != null) {
			writer.println(getIndentString(indent+1) + "Internal type: ");
			for (ImportedTypeDefinition internalType : internalTypeList) {
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
	 * Print the enum type definition
	 */
	public boolean visit(EnumTypeDefinition scope) {
		String locationString = "-1:0";
		SourceCodeLocation location = scope.getLocation();
		if (location != null) locationString = location.toString();
		writer.println(locationString + " Enum: " + scope.getFullQualifiedName());
		List<TypeReference> superTypeList = scope.getSuperList();
		
		if (superTypeList != null) {
			writer.println(getIndentString(indent+1) + "Super type: ");
			for (TypeReference superTypeReference : superTypeList) {
				if (superTypeReference.resolveBinding()) {
					writer.println(getIndentString(indent+2) + superTypeReference.getName() + 
							"[" + superTypeReference.getDefinition().getUniqueId() + "]");
				} else {
					writer.println(getIndentString(indent+2) + superTypeReference.getName() + 
							"[Not resolved!]");
				}
				
			}
		}
		
		List<EnumConstantDefinition> constantList = scope.getConstantList();
		if (constantList != null) {
			writer.println(getIndentString(indent+1) + "Constant: ");
			for (EnumConstantDefinition constant : constantList) {
				if (!accept(constant)) continue;
				writer.print(getIndentString(indent+2) + constant.getSimpleName());
				List<NameReference> argumentList = constant.getArgumentList();
				if (argumentList != null) {
					writer.print("(");
					boolean isFirst = true;
					for (NameReference argument : argumentList) {
						if (isFirst) {
							writer.print(argument.getName());
							isFirst = false;
						}
						else writer.print(", " + argument.getName());
					}
					writer.print(")");
				}
				writer.println();
			}
		}
		return false;
	}
	
	/**
	 * Print the method definition
	 */
	public boolean visit(MethodDefinition scope) {
		String locationString = "-1:0";
		SourceCodeLocation location = scope.getLocation();
		if (location != null) locationString = location.toString();
		writer.print(getIndentString(indent) + locationString + " " + scope.getSimpleName() + "(");
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
		if (!printVariable) {
			if (!hasLocalType(scope)) return false;
		}
		
		writer.println(getIndentString(indent) + "Local scope: [" + scope.getScopeStart().toString() + ", " + scope.getScopeEnd() + "]");
		List<VariableDefinition> vars = scope.getVariableList();
		if (vars != null && printVariable) {
			writer.println(getIndentString(indent+1) + "Local variable: ");
			for (VariableDefinition var : vars) {
				if (accept(var)) {
					String locationString = "-1:0";
					SourceCodeLocation location = var.getLocation();
					if (location != null) locationString = location.toString();
					TypeReference varType = var.getType();
					writer.println(getIndentString(indent+2) + locationString + " " + var.getSimpleName() + " : " + 
							varType.getName());
					
				}
			}
		}
		List<DetailedTypeDefinition> types = scope.getLocalTypeList();
		if (types != null) {
			writer.println(getIndentString(indent+1) + "Local Type: ");
			for (DetailedTypeDefinition type : types) {
				if (accept(type)) {
					indent = indent + 2;
					type.accept(this);
					indent = indent - 2;
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
	
	private boolean hasLocalType(LocalScope scope) {
		List<LocalScope> subscopeList = scope.getSubLocalScope();
		if (subscopeList != null) {
			for (LocalScope subscope : subscopeList) {
				if (hasLocalType(subscope)) return true;
			}
		}
		List<DetailedTypeDefinition> localTypeList = scope.getLocalTypeList();
		if (localTypeList != null) {
			if (localTypeList.size() > 0) return true;
		}
		return false;
	}
}
