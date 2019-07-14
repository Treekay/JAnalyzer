package analyzer.objectExpression;

import nameTable.nameDefinition.NameDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê6ÔÂ15ÈÕ
 * @version 1.0
 *
 */
public class ObjectDefinitionRecorder extends ObjectExpressionRecorder {
	protected NameDefinition definition = null;

	public boolean isObjectDefinition() {
		return true;
	}
}
