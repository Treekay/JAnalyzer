package measurement.measure;

/**
 * @author Zhou Xiaocong
 * @since 2015年9月19日
 * @version 1.0
 */
public enum MeasureObjectKind {
	MOK_SYSTEM, 	// Corresponding to name scope SystemScope
	MOK_PACKAGE,	// Corresponding to name scope PackageDefinition
	MOK_UNIT,		// Corresponding to name scope CompilationUnitScope
	MOK_CLASS,		// Corresponding to name scope DetailedTypeDefinition
	MOK_METHOD,		// Corresponding to name scope MethodDefinition
}
