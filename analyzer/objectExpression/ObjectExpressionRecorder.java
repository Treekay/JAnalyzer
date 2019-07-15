package analyzer.objectExpression;

/**
 * @author Zhou Xiaocong
 * @since 2018年6月15日
 * @version 1.0
 *
 */
public class ObjectExpressionRecorder {
	protected ObjectExpressionKind kind = ObjectExpressionKind.OEK_UNKNOWN;
	protected ObjectExpressionUsageKind usage = ObjectExpressionUsageKind.OEUK_UNKNOWN;
	
	protected String className = null;
	protected String methodName = null;
	protected boolean isDereference = false;
	protected boolean isPolynomial = false;

	public boolean isObjectDefinition() {
		return false;
	}
}
