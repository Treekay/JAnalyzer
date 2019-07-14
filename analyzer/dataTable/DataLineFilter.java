package analyzer.dataTable;

/**
 * A interface to filter lines in data table
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ24ÈÕ
 * @version 1.0
 */
public interface DataLineFilter {
	
	/**
	 * Test if accept a line (given by the index) in a data table manager. 
	 */
	public boolean accept(DataTableManager manager, int lineIndex);

	/**
	 * Test if accept a line (given by the key value of the line) in a data table manager. 
	 */
	public boolean accept(DataTableManager manager, String lineKeyValue);

}
