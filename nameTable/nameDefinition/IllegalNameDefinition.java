package nameTable.nameDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 */
public class IllegalNameDefinition extends AssertionError {
	private static final long serialVersionUID = 6646448588056915490L;

	public IllegalNameDefinition(String message) {
		super(message);
	}
	
}
