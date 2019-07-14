package analyzer.objectExpression;

import nameTable.nameDefinition.NameDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2018��6��15��
 * @version 1.0
 *
 */
public class ObjectDefinitionRecorder extends ObjectExpressionRecorder {
	protected NameDefinition definition = null;

	public boolean isObjectDefinition() {
		return true;
	}
}
