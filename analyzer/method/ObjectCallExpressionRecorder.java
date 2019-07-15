package analyzer.method;

import nameTable.nameReference.NameReference;

/**
 * @author Zhou Xiaocong
 * @since 2018年7月26日
 * @version 1.0
 *
 */
public class ObjectCallExpressionRecorder extends CallExpressionRecorder {
	protected NameReference checkReference = null;		// may be null, if the return value is not checked
	protected boolean exactlyCheck = false;
	protected NameReference useReference = null;		// may be null, if the return value is not used
	protected boolean leftValueUse = false;
}
