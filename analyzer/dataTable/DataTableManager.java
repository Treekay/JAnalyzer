package analyzer.dataTable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;

/**
 * The object of this class represent a data table. Each line of the data table has the format as "vlaue\tvalue\tvalue..."
 * The number of the columns (i.e. the values splitted by "\t") is predefined and can not be changed after 
 * creating the data table, but the line of the table can be changed after creating the data table.
 * 
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ7ÈÕ
 * @version 1.0
 */
public class DataTableManager {
	protected String dataTableId = null;
	protected List<String[]> table = null;
	protected String[] columnNameArray = null;
	protected int keyColumnIndex = 0;
	
	public DataTableManager(String id) {
		dataTableId = id;
	}
	
	public void clear() {
		columnNameArray = null;
		keyColumnIndex = 0;
		table = null;
	}
	
	public String[] getColumnNameArray() {
		return columnNameArray;
	}
	
	/**
	 * Set the identifiers of the column of the data table
	 */
	public void setColumnNames(String[] columnNames) {
		this.columnNameArray = columnNames;
	}
	
	/**
	 * Set the index of the key column, i.e. the content of the key column can be used to identify the line
	 * of the data table
	 */
	public boolean setKeyColumnIndex(int index) {
		if (columnNameArray == null) return false;
		if (index < 0 || index >= columnNameArray.length) return false;
		keyColumnIndex = index;
		return true;
	}

	
	public int getKeyColumnIndex() {
		return keyColumnIndex;
	}
	
	/**
	 * Set the index of the key column, i.e. the content of the key column can be used to identify the line
	 * of the data table
	 */
	public boolean setKeyColumnIndex(String name) {
		if (columnNameArray == null) return false;
		int index = getColumnIndex(name);
		if (index < 0) return false;
		keyColumnIndex = index;
		return true;
	}
	
	/**
	 * Append a line to the data table. For calling this method, the column id should be defined before
	 * The length of the line must be equal to the number of the columns
	 */
	public boolean appendLine(String[] line) {
		if (columnNameArray == null) return false;
		if (table == null) table = new ArrayList<String[]>();
		if (line.length != columnNameArray.length) return false;
		return table.add(line);
	}

	public int getColumnNumber() {
		if (columnNameArray == null) return 0;
		return columnNameArray.length;
	}
	
	public int getLineNumber() {
		if (table == null) return 0;
		return table.size();
	}
	
	public int getColumnIndex(String columnName) {
		if (columnNameArray == null) return -1;
		for (int i = 0; i < columnNameArray.length; i++) {
			if (columnName.equals(columnNameArray[i])) return i;
		}
		return -1;
	}
	
	public int getLineIndex(String lineKey) {
		if (table == null) return -1;
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			if (lineArray[keyColumnIndex].equals(lineKey)) return i; 
		}
		return -1;
	}
	
	public String[] getLineAsStringArray(String lineKey) {
		if (table == null) return null;
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			if (lineArray[keyColumnIndex].equals(lineKey)) return lineArray; 
		}
		return null;
	}

	public String[] getLineAsStringArray(int lineIndex) {
		if (table == null) return null;
		if (lineIndex < 0 || lineIndex >= table.size()) return null;
		return table.get(lineIndex);
	}

	public List<String> getLineAsStringList(String lineKey) {
		if (table == null) return null;
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			if (lineArray[keyColumnIndex].equals(lineKey)) {
				List<String> resultList = new ArrayList<String>();
				for (int j = 0; j < lineArray.length; j++) resultList.add(lineArray[j]);
				return resultList;
			}
		}
		return null;
	}
	
	public List<String> getLineAsStringList(int lineIndex) {
		if (table == null) return null;
		if (lineIndex < 0 || lineIndex >= table.size()) return null;
		String[] lineArray = table.get(lineIndex);
		List<String> resultList = new ArrayList<String>();
		for (int j = 0; j < lineArray.length; j++) resultList.add(lineArray[j]);
		return resultList;
	}
	
	public List<String> getLineAsStringList(String lineKey, String[] columnNames) {
		int lineIndex = getLineIndex(lineKey);
		if (lineIndex < 0) return null;
		List<String> resultList = new ArrayList<String>();
		String[] lineValue = table.get(lineIndex);
		for (int i = 0; i < columnNames.length; i++) {
			int index = getColumnIndex(columnNames[i]);
			resultList.add(lineValue[index]);
		}
		return resultList;
	}

	public List<String> getLineAsStringList(int lineIndex, String[] columnNames) {
		if (lineIndex < 0 && lineIndex >= table.size()) return null;
		List<String> resultList = new ArrayList<String>();
		String[] lineValue = table.get(lineIndex);
		for (int i = 0; i < columnNames.length; i++) {
			int index = getColumnIndex(columnNames[i]);
			resultList.add(lineValue[index]);
		}
		return resultList;
	}

	public String[] getLineAsStringArray(String lineKey, String[] columnNames) {
		int lineIndex = getLineIndex(lineKey);
		if (lineIndex < 0) return null;
		String[] resultArray = new String[columnNames.length];
		String[] lineValue = table.get(lineIndex);
		for (int i = 0; i < columnNames.length; i++) {
			int index = getColumnIndex(columnNames[i]);
			resultArray[i] = lineValue[index];
		}
		return resultArray;
	}

	public String[] getLineAsStringArray(int lineIndex, String[] columnNames) {
		if (lineIndex < 0 && lineIndex >= table.size()) return null;
		String[] resultArray = new String[columnNames.length];
		String[] lineValue = table.get(lineIndex);
		for (int i = 0; i < columnNames.length; i++) {
			int index = getColumnIndex(columnNames[i]);
			resultArray[i] = lineValue[index];
		}
		return resultArray;
	}

	public String[] getColumnAsStringArray(String columnName) {
		int index = getColumnIndex(columnName);
		if (index < 0) return null;
		if (table == null || table.size() <= 0) return null;
		String[] resultValues = new String[table.size()];
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			resultValues[i] = lineArray[index];
		}
		return resultValues;
	}
	
	public double[] getColumnAsDoubleArray(String columnName) {
		int index = getColumnIndex(columnName);
		if (index < 0) return null;
		if (table == null || table.size() <= 0) return null;
		double[] resultValues = new double[table.size()];
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			String valueString = lineArray[index];
			if (valueString.equals("NA") || valueString.equals("NaN")) resultValues[i] = Double.NaN;
			else {
				try {
					resultValues[i] = Double.parseDouble(lineArray[index]);
				} catch (Exception exc) {
					resultValues[i] = Double.NaN;
				}
			}
		}
		return resultValues;
	}

	public int[] getColumnAsIntArray(String columnName) {
		int index = getColumnIndex(columnName);
		if (index < 0) return null;
		if (table == null || table.size() <= 0) return null;
		int[] resultValues = new int[table.size()];
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			try {
				resultValues[i] = Integer.parseInt(lineArray[index]);
			} catch (Exception exc) {
				resultValues[i] = 0;
			}
		}
		return resultValues;
	}

	public boolean[] getColumnAsBooleanArray(String columnName) {
		int index = getColumnIndex(columnName);
		if (index < 0) return null;
		if (table == null || table.size() <= 0) return null;
		boolean[] resultValues = new boolean[table.size()];
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			if (lineArray[index].equals("T") || lineArray[index].equals("t")) {
				resultValues[i] = true;
			} else if (lineArray[index].equals("F") || lineArray[index].equals("f")) {
				resultValues[i] = false;
			} else {
				try {
					resultValues[i] = Boolean.parseBoolean(lineArray[index]);
				} catch (Exception exc) {
					resultValues[i] = false;
				}
			}
		}
		return resultValues;
	}
	
	public List<String> getColumnAsStringList(String columnName) {
		int index = getColumnIndex(columnName);
		if (index < 0) return null;
		if (table == null || table.size() <= 0) return null;
		List<String> resultList = new ArrayList<String>();
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			resultList.add(lineArray[index]);
		}
		return resultList;
	}
	
	public String getCellValueAsString(String lineKey, String columnName) {
		if (columnName == null || table == null) return null;
		int index = getColumnIndex(columnName);
		if (index < 0) return null;
		
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			if (lineArray[keyColumnIndex].equals(lineKey)) {
				return lineArray[index];
			}
		}
		return null;
	}
	
	public double getCellValueAsDouble(String lineKey, String columnName) {
		if (columnName == null || table == null) return Double.NaN;
		int index = getColumnIndex(columnName);
		if (index < 0) return Double.NaN;
		
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			if (lineArray[keyColumnIndex].equals(lineKey)) {
				if (lineArray[index].equals("NA") || lineArray[index].equals("NaN")) return Double.NaN;
				else {
					try {
						return Double.parseDouble(lineArray[index]);
					} catch (Exception exc) {
						return Double.NaN;
					}
				}
			}
		}
		return Double.NaN;
	}
	
	public int getCellValueAsInt(String lineKey, String columnName) {
		if (columnName == null || table == null) return 0;
		int index = getColumnIndex(columnName);
		if (index < 0) return 0;
		
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			if (lineArray[keyColumnIndex].equals(lineKey)) {
				try {
					return Integer.parseInt(lineArray[index]);
				} catch (Exception exc) {
					return 0;
				}
			}
		}
		return 0;
	}

	public String getCellValueAsString(int lineIndex, String columnName) {
		if (columnName == null || table == null) return null;
		if (lineIndex < 0 || lineIndex > table.size()) return null;
		
		int index = getColumnIndex(columnName);
		if (index < 0) return null;
		
		String[] lineArray = table.get(lineIndex);
		return lineArray[index];
	}
	
	public double getCellValueAsDouble(int lineIndex, String columnName) {
		if (columnName == null || table == null) return Double.NaN;
		if (lineIndex < 0 || lineIndex > table.size()) return Double.NaN;

		int index = getColumnIndex(columnName);
		if (index < 0) return Double.NaN;
		
		String[] lineArray = table.get(lineIndex);
		if (lineArray[index].equals("NA") || lineArray[index].equals("NaN")) return Double.NaN;
		else {
			try {
				return Double.parseDouble(lineArray[index]);
			} catch (Exception exc) {
				return Double.NaN;
			}
		}
	}
	
	public int getCellValueAsInt(int lineIndex, String columnName) {
		if (columnName == null || table == null) return 0;
		if (lineIndex < 0 || lineIndex > table.size()) return 0;

		int index = getColumnIndex(columnName);
		if (index < 0) return 0;
		
		String[] lineArray = table.get(lineIndex);
		try {
			return Integer.parseInt(lineArray[index]);
		} catch (Exception exc) {
			return 0;
		}
	}

	public String getCellValueAsString(int lineIndex, int columnIndex) {
		if (table == null) return null;
		if (lineIndex < 0 || lineIndex > table.size()) return null;
		
		if (columnIndex < 0 || columnIndex >= columnNameArray.length) return null;
		
		String[] lineArray = table.get(lineIndex);
		return lineArray[columnIndex];
	}
	
	public double getCellValueAsDouble(int lineIndex, int columnIndex) {
		if (table == null) return Double.NaN;
		if (lineIndex < 0 || lineIndex > table.size()) return Double.NaN;

		if (columnIndex < 0 || columnIndex >= columnNameArray.length) return Double.NaN;
		
		String[] lineArray = table.get(lineIndex);
		if (lineArray[columnIndex].equals("NA") || lineArray[columnIndex].equals("NaN")) return Double.NaN;
		else {
			try {
				return Double.parseDouble(lineArray[columnIndex]);
			} catch (Exception exc) {
				return Double.NaN;
			}
		}
	}
	
	public int getCellValueAsInt(int lineIndex, int columnIndex) {
		if (table == null) return 0;
		if (lineIndex < 0 || lineIndex > table.size()) return 0;

		if (columnIndex < 0 || columnIndex >= columnNameArray.length) return 0;
		
		String[] lineArray = table.get(lineIndex);
		try {
			return Integer.parseInt(lineArray[columnIndex]);
		} catch (Exception exc) {
			return 0;
		}
	}
	
	/**
	 * Set the value of a data table cell. The string value should not include space or "\t"!!
	 */
	public boolean setCellValue(String lineKey, String columnName, String value) {
		if (columnName == null || table == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			if (lineArray[keyColumnIndex].equals(lineKey)) {
				lineArray[index] = value;
				return true;
			}
		}
		return false;
	}
	
	public boolean setCellValue(String lineKey, String columnName, double value) {
		if (columnName == null || table == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			if (lineArray[keyColumnIndex].equals(lineKey)) {
				if (Double.isNaN(value)) lineArray[index] = "NA";
				else lineArray[index] = Double.toString(value);
				return true;
			}
		}
		return false;
	}

	public boolean setCellValue(String lineKey, String columnName, int value) {
		if (columnName == null || table == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			if (lineArray[keyColumnIndex].equals(lineKey)) {
				lineArray[index] = Integer.toString(value);
			}
		}
		return false;
	}

	public boolean setCellValue(String lineKey, String columnName, boolean value) {
		if (columnName == null || table == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			if (lineArray[keyColumnIndex].equals(lineKey)) {
				lineArray[index] = Boolean.toString(value);
			}
		}
		return false;
	}

	/**
	 * Set the value of a data table cell. The string value should not include space or "\t"!!
	 */
	public boolean setCellValue(int lineIndex, String columnName, String value) {
		if (columnName == null || table == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		if (lineIndex < 0 || lineIndex >= table.size()) return false;
		String[] lineArray = table.get(lineIndex);
		lineArray[index] = value;
		return true;
	}
	
	public boolean setCellValue(int lineIndex, String columnName, double value) {
		if (columnName == null || table == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		if (lineIndex < 0 || lineIndex >= table.size()) return false;
		String[] lineArray = table.get(lineIndex);
		if (Double.isNaN(value)) lineArray[index] = "NA";
		else lineArray[index] = Double.toString(value);
		return true;
	}

	public boolean setCellValue(int lineIndex, String columnName, int value) {
		if (columnName == null || table == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		if (lineIndex < 0 || lineIndex >= table.size()) return false;
		String[] lineArray = table.get(lineIndex);
		lineArray[index] = Integer.toString(value);
		return true;
	}

	public boolean setCellValue(int lineIndex, String columnName, boolean value) {
		if (columnName == null || table == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		if (lineIndex < 0 || lineIndex >= table.size()) return false;
		String[] lineArray = table.get(lineIndex);
		lineArray[index] = Boolean.toString(value);
		return true;
	}
	
	/**
	 * Set all values of a column given by columnName to the value in valueStringList
	 * @return If valueStringList.size() == table.size() (i.e. valueStringList.size() is equal to the line number of data table) 
	 * return true, else return false.
	 */
	public boolean setColumnValue(String columnName, List<String> valueStringList) {
		if (columnName == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		if (table == null) {
			table = new ArrayList<String[]>();
			for (int i = 0; i < valueStringList.size(); i++) {
				String[] tableValue = new String[columnNameArray.length];
				table.add(tableValue);
			}
		}
		for (int i = 0; i < table.size() && i < valueStringList.size(); i++) {
			String[] tableValue = table.get(i);
			tableValue[index] = valueStringList.get(i);
		}
		if (table.size() == valueStringList.size()) return true;
		else return false;
	}

	/**
	 * Set all values of a column given by columnName to the value in valueStringList
	 * @return If valueStringArray.length == table.size() (i.e. valueStringList.length is equal to the line number of data table) 
	 * return true, else return false.
	 */
	public boolean setColumnValue(String columnName, String[] valueStringArray) {
		if (columnName == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		if (table == null) {
			table = new ArrayList<String[]>();
			for (int i = 0; i < valueStringArray.length; i++) {
				String[] tableValue = new String[columnNameArray.length];
				table.add(tableValue);
			}
		}
		for (int i = 0; i < table.size() && i < valueStringArray.length; i++) {
			String[] tableValue = table.get(i);
			tableValue[index] = valueStringArray[i];
		}
		if (table.size() == valueStringArray.length) return true;
		else return false;
	}
	

	/**
	 * Set all values of a column given by columnName to the value in valueStringList
	 * @return If valueStringArray.length == table.size() (i.e. valueStringList.length is equal to the line number of data table) 
	 * return true, else return false.
	 */
	public boolean setColumnValue(String columnName, double[] valueArray) {
		if (columnName == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		if (table == null) {
			table = new ArrayList<String[]>();
			for (int i = 0; i < valueArray.length; i++) {
				String[] tableValue = new String[columnNameArray.length];
				table.add(tableValue);
			}
		}
		for (int i = 0; i < table.size() && i < valueArray.length; i++) {
			String[] tableValue = table.get(i);
			if (Double.isNaN(valueArray[i])) tableValue[index] = "NA";
			else tableValue[index] = Double.toString(valueArray[i]);
		}
		if (table.size() == valueArray.length) return true;
		else return false;
	}
	
	public boolean setLineValue(String lineKey, String[] columnNames, double[] valueArray) {
		if (columnNames.length != valueArray.length) return false;
		
		int lineIndex = getLineIndex(lineKey);
		if (lineIndex < 0) return false;
		for (int i = 0; i < columnNames.length; i++) {
			int index = getColumnIndex(columnNames[i]);
			String[] lineValue = table.get(lineIndex);
			if (Double.isNaN(valueArray[i])) lineValue[index] = "NA";
			else lineValue[index] = Double.toString(valueArray[i]);
		}
		return true;
	}

	public boolean setLineValue(String lineKey, String[] columnNames, int[] valueArray) {
		if (columnNames.length != valueArray.length) return false;
		
		int lineIndex = getLineIndex(lineKey);
		if (lineIndex < 0) return false;
		for (int i = 0; i < columnNames.length; i++) {
			int index = getColumnIndex(columnNames[i]);
			String[] lineValue = table.get(lineIndex);
			lineValue[index] = Integer.toString(valueArray[i]);
		}
		return true;
	}

	public boolean setLineValue(String lineKey, String[] columnNames, boolean[] valueArray) {
		if (columnNames.length != valueArray.length) return false;
		
		int lineIndex = getLineIndex(lineKey);
		if (lineIndex < 0) return false;
		for (int i = 0; i < columnNames.length; i++) {
			int index = getColumnIndex(columnNames[i]);
			String[] lineValue = table.get(lineIndex);
			lineValue[index] = Boolean.toString(valueArray[i]);
		}
		return true;
	}

	public boolean setLineValue(String lineKey, String[] columnNames, String[] valueArray) {
		if (columnNames.length != valueArray.length) return false;
		
		int lineIndex = getLineIndex(lineKey);
		if (lineIndex < 0) return false;
		for (int i = 0; i < columnNames.length; i++) {
			int index = getColumnIndex(columnNames[i]);
			String[] lineValue = table.get(lineIndex);
			lineValue[index] = valueArray[i];
		}
		return true;
	}
	
	
	/**
	 * Set all values of a column given by columnName to the value in valueStringList
	 * @return If valueStringArray.length == table.size() (i.e. valueStringList.length is equal to the line number of data table) 
	 * return true, else return false.
	 */
	public boolean setColumnValue(String columnName, int[] valueArray) {
		if (columnName == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		if (table == null) {
			table = new ArrayList<String[]>();
			for (int i = 0; i < valueArray.length; i++) {
				String[] tableValue = new String[columnNameArray.length];
				table.add(tableValue);
			}
		}
		for (int i = 0; i < table.size() && i < valueArray.length; i++) {
			String[] tableValue = table.get(i);
			tableValue[index] = Integer.toString(valueArray[i]);
		}
		if (table.size() == valueArray.length) return true;
		else return false;
	}

	/**
	 * Set all values of a column given by columnName to the value in valueStringList
	 * @return If valueStringArray.length == table.size() (i.e. valueStringList.length is equal to the line number of data table) 
	 * return true, else return false.
	 */
	public boolean setColumnValue(String columnName, boolean[] valueArray) {
		if (columnName == null) return false;
		int index = getColumnIndex(columnName);
		if (index < 0) return false;
		
		if (table == null) {
			table = new ArrayList<String[]>();
			for (int i = 0; i < valueArray.length; i++) {
				String[] tableValue = new String[columnNameArray.length];
				table.add(tableValue);
			}
		}
		for (int i = 0; i < table.size() && i < valueArray.length; i++) {
			String[] tableValue = table.get(i);
			tableValue[index] = Boolean.toString(valueArray[i]);
		}
		if (table.size() == valueArray.length) return true;
		else return false;
	}
	
	/**
	 * Read the data from a text file  
	 * @param file: Each line of the file gives a list of values as "value\tvalue\tvalue\tvalue...." 
	 * @param hasTitle: the first line is the title? 
	 */
	public boolean read(String file, int titleLine) throws IOException {
		return read(file, titleLine, 1, 0);
	}

	/**
	 * Read the data from a text file  
	 * @param file: Each line of the file gives a list of values as "value\tvalue\tvalue\tvalue...." 
	 * @param hasTitle: the first line is the title? 
	 */
	public boolean read(String file, boolean hasTitle) throws IOException {
		int titleLine = 0;
		if (hasTitle == false) titleLine = -1;
		return read(file, titleLine, 1, 0);
	}

	/**
	 * Read the data from startLine to the end of a text file.  
	 * @param file: Each line of the file gives a list of values as "value\tvalue\tvalue\tvalue...." 
	 * @param hasTitle: the first line is the title? 
	 */
	public boolean read(String file, boolean hasTitle, int startLine) throws IOException {
		int titleLine = 0;
		if (hasTitle == false) titleLine = -1;
		return read(file, titleLine, startLine, 0);
	}
	
	/**
	 * Read the data between startLine (include) and endLine(exclude) from a text file. If titleLine >= 0, we always 
	 * read the first line as title, and then read the data between startLine and endLine. The line number of data always  
	 * starts from 0(include title line), and if endLine == 0, then we read the data between startLine and the end of file. 
	 *    	
	 * @param file: Each line of the file gives a list of values as "value\tvalue\tvalue\tvalue...."
	 * @param hasTitle: the first line is the title? 
	 * @return When the file has more lines, return true, otherwise return false.   
	 */
	public boolean read(String file, int titleLine, int startLine, int endLine) throws IOException {
		final String splitter = "\t";		// the table char is used as splitter in the file
		int infoNumber = -1;
		
		table = new ArrayList<String[]>();
		Scanner scanner = new Scanner(new File(file));
		int lineCounter = 0;
		if (startLine < titleLine) startLine = titleLine;
		if (startLine < 0) startLine = 0;
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (titleLine >= 0 && lineCounter == titleLine) {
				// The first line is the title line of the file, set the column ids to as the title line
				columnNameArray = line.split(splitter);
				lineCounter = lineCounter + 1;
				infoNumber = columnNameArray.length;
				continue;
			} 
			if (lineCounter < startLine) {
				lineCounter = lineCounter + 1;
				continue;
			}
			if (endLine != 0) {
				if (lineCounter >= endLine) {
					scanner.close();
					return true;
				}
			}
			// This is line give a list of values 
			String[] infoStrings = line.split(splitter);
			if (infoNumber < 0) {
				// When we read the first line, we set the information number
				infoNumber = infoStrings.length;
			}
			if (infoStrings.length != infoNumber) {
				System.err.println("In file " + file + " has illegal line [" + line + "]!");
			} else table.add(infoStrings);
			lineCounter = lineCounter + 1;
		}
		scanner.close();
		return false;
	}
	
	/**
	 * Write the data table to the given text file.
	 * @param file: each line of the file gives a list of values as "value\tvalue\tvalue\tvalue...."
	 */
	public void write(String file) throws IOException {
		if (table == null) return;
		
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		if (columnNameArray != null && columnNameArray.length > 0) {
			writer.print(columnNameArray[0]);
			for (int i = 1; i < columnNameArray.length; i++) writer.print("\t" + columnNameArray[i]);
			writer.println();
		}
		
		for (String[] valueStrings : table) {
			writer.print(valueStrings[0]);
			for (int i = 1; i < valueStrings.length; i++) writer.print("\t" + valueStrings[i]);
			writer.println();
		}
		writer.close();
	}

	/**
	 * Write the data table to the given text file, and sorted by a string column in given order.
	 * @param file: each line of the file gives a list of values as "value\tvalue\tvalue\tvalue...."
	 */
	public void writeWithSortedStringColumn(String file, String columnName, boolean decreasingOrder) throws IOException {
		if (table == null) return;
		
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		if (columnNameArray != null && columnNameArray.length > 0) {
			writer.print(columnNameArray[0]);
			for (int i = 1; i < columnNameArray.length; i++) writer.print("\t" + columnNameArray[i]);
			writer.println();
		}
		
		int[] sortedIndex = this.getSortedIndexByStringColumn(columnName, decreasingOrder);
		if (sortedIndex == null) {
			writer.close();
			throw new IOException("Can not find column " + columnName + " in the data table!");
		}
		
		for (int index = 0; index < table.size(); index++) {
			String[] valueStrings = table.get(sortedIndex[index]);
			writer.print(valueStrings[0]);
			for (int i = 1; i < valueStrings.length; i++) writer.print("\t" + valueStrings[i]);
			writer.println();
		}
		writer.close();
	}
	
	/**
	 * Write the data table to the given text file, and sorted by a string column in given order.
	 * @param file: each line of the file gives a list of values as "value\tvalue\tvalue\tvalue...."
	 */
	public void writeWithSortedStringColumnArray(String file, String[] columnNameArray, boolean[] order) throws IOException {
		if (table == null) return;
		
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		if (columnNameArray != null && columnNameArray.length > 0) {
			writer.print(columnNameArray[0]);
			for (int i = 1; i < columnNameArray.length; i++) writer.print("\t" + columnNameArray[i]);
			writer.println();
		}
		
		int[] sortedIndex = this.getSortedIndexByStringColumnArray(columnNameArray, order);
		if (sortedIndex == null) {
			writer.close();
			throw new IOException("Can not sort the table for given column name array and order array!");
		}
		
		for (int index = 0; index < table.size(); index++) {
			String[] valueStrings = table.get(sortedIndex[index]);
			writer.print(valueStrings[0]);
			for (int i = 1; i < valueStrings.length; i++) writer.print("\t" + valueStrings[i]);
			writer.println();
		}
		writer.close();
	}

	/**
	 * Return an array of index of the lines in the table. By the returned index array, we can easily scan the 
	 * lines in the table according to decreasing order of the values in the given column.   
	 * That is, then the returned array [0] is the index of the line in the table 
	 * with the maximal value in the given column, and the returned array [length-1] is the index of the line in 
	 * the table with the minimal value in the given column.       
	 */
	public int[] getDecreasingSortedIndexByDoubleColumn(String columnName) {
		return getSortedIndexByDoubleColumn(columnName, true);
	}
	
	/**
	 * Return an array of index of the lines in the table. By the returned index array, we can easily scan the 
	 * lines in the table according to increasing order of the values in the given column.   
	 * That is, the returned array [0] is the index of the line in the table with the minimal value in the given 
	 * column, while the returned array [length-1] is the index of the line with the maximal value.      
	 */
	public int[] getIncreasingSortedIndexByDoubleColumn(String columnName) {
		return getSortedIndexByDoubleColumn(columnName, false);
	}
	
	/**
	 * Return an array of index of the lines in the table. By the returned index array, we can easily scan the 
	 * lines in the table according to decreasing order or increasing order of the values in the given column.   
	 * That is, if decreasingOrder == true, then the returned array [0] is the index of the line in the table 
	 * with the maximal value in the given column, and the returned array [length-1] is the index of the line in 
	 * the table with the minimal value in the given column. Similarly, if decreasingOrder == false, then the 
	 * returned array [0] is the index of the line in the table with the minimal value in the given column, while 
	 * the returned array [length-1] is the index of the line with the maximal value.      
	 */
	protected int[] getSortedIndexByDoubleColumn(String columnName, boolean decreasingOrder) {
		double[] valueArray = getColumnAsDoubleArray(columnName);
		if (valueArray == null || valueArray.length == 0) return null;

		// Use the class NaturalRanking in org.apache.commons.math3 to get the rank of the valueArray 
		NaturalRanking naturalRanking = new NaturalRanking(NaNStrategy.MINIMAL, TiesStrategy.SEQUENTIAL);
		double[] rankArray = naturalRanking.rank(valueArray); 
		int[] sortedIndexArray = new int[valueArray.length];
		for (int index = 0; index < valueArray.length; index++) {
			int sortedIndex = 0;
			if (decreasingOrder == true) {
				// Generate the index of the values in valueArray in decreasing order according to the rank array
				sortedIndex = valueArray.length - (int)rankArray[index];
				// Note that the value in valueArray at the index should be placed in sortedIndex when we 
				// sort the valueArray in decreasing order. For example, if the rankArray[index] = valueArray.length, 
				// that is, valueArray[index] is the maximal value in the array, then it will be placed in 0 (i.e. the first
				// element of the decreasing sorted array), which is equal to valueArray.length - rankArray[index]
			} else {
				// Generate the index of the values in valueArray in increasing order according to the rank array
				sortedIndex = (int)rankArray[index] - 1;
				// Note that the value in valueArray at the index should be placed in sortedIndex when we 
				// sort the valueArray in increasing order. For example, if the rankArray[index] = 1, 
				// that is, valueArray[index] is the minimal value in the array, then it will be placed in 0 (i.e. the first
				// element of the decreasing sorted array), which is equal to  rankArray[index] - 1
			}
			sortedIndexArray[sortedIndex] = index;
		}
		return sortedIndexArray;
	}

	/**
	 * Return an array of index of the lines in the table. By the returned index array, we can easily scan the 
	 * lines in the table according to decreasing order of the values in the given column.   
	 * That is, then the returned array [0] is the index of the line in the table 
	 * with the maximal value in the given column, and the returned array [length-1] is the index of the line in 
	 * the table with the minimal value in the given column.       
	 */
	public int[] getDecreasingSortedIndexByStringColumn(String columnName) {
		return getSortedIndexByStringColumn(columnName, true);
	}
	
	/**
	 * Return an array of index of the lines in the table. By the returned index array, we can easily scan the 
	 * lines in the table according to increasing order of the values in the given column.   
	 * That is, the returned array [0] is the index of the line in the table with the minimal value in the given 
	 * column, while the returned array [length-1] is the index of the line with the maximal value.      
	 */
	public int[] getIncreasingSortedIndexByStringColumn(String columnName) {
		return getSortedIndexByStringColumn(columnName, false);
	}

	/**
	 * Return an array of index of the lines in the table. By the returned index array, we can easily scan the 
	 * lines in the table according to decreasing order or increasing order of the values in the given column.   
	 * That is, if decreasingOrder == true, then the returned array [0] is the index of the line in the table 
	 * with the maximal value in the given column, and the returned array [length-1] is the index of the line in 
	 * the table with the minimal value in the given column. Similarly, if decreasingOrder == false, then the 
	 * returned array [0] is the index of the line in the table with the minimal value in the given column, while 
	 * the returned array [length-1] is the index of the line with the maximal value.      
	 */
	protected int[] getSortedIndexByStringColumn(String columnName, boolean decreasingOrder) {
		int columnIndex = getColumnIndex(columnName);
		if (columnIndex < 0) return null;
		if (table == null || table.size() <= 0) return null;
		
		StringIndexedPair[] indexedValueArray = new StringIndexedPair[table.size()];
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			indexedValueArray[i] = new StringIndexedPair(lineArray[columnIndex], i);
		}
		Arrays.sort(indexedValueArray);
		int[] sortedIndexArray = new int[indexedValueArray.length];
		for (int index = 0; index < indexedValueArray.length; index++) {
			if (decreasingOrder == false) sortedIndexArray[index] = indexedValueArray[index].index;
			else sortedIndexArray[index] = indexedValueArray[(indexedValueArray.length-1)-index].index; 
		}
		return sortedIndexArray;
	}
	

	/**
	 * Return an array of index of the lines in the table. By the returned index array, we can easily scan the 
	 * lines in the table according to decreasing order or increasing order of the values in the given columns.
	 * <p>Note: if order[i] == true, the lines is sorted by decreasing order for the column columnNames[i]!   
	 */
	protected int[] getSortedIndexByStringColumnArray(String[] columnNames, boolean[] order) {
		if (table == null || table.size() <= 0) return null;
		if (columnNames.length <= 0 || order.length <= 0 || columnNames.length != order.length) return null;
		int[] columnIndexes = new int[order.length];
		for (int i = 0; i < order.length; i++) columnIndexes[i] = getColumnIndex(columnNames[i]);
		
		StringIndexedArray[] indexedValueArray = new StringIndexedArray[table.size()];
		for (int i = 0; i < table.size(); i++) {
			String[] lineArray = table.get(i);
			indexedValueArray[i] = new StringIndexedArray(order.length, i, order);
			for (int j = 0; j < order.length; j++) indexedValueArray[i].array[j] = lineArray[columnIndexes[j]];
		}
		Arrays.sort(indexedValueArray);
		int[] sortedIndexArray = new int[indexedValueArray.length];
		for (int index = 0; index < indexedValueArray.length; index++) {
			sortedIndexArray[index] = indexedValueArray[index].index;
		}
		return sortedIndexArray;
	}

	public static DataTableManager createEmptyDataTableWithSameStructure(DataTableManager sourceManager, String newManagerIdentifier) {
		DataTableManager resultManager = new DataTableManager(newManagerIdentifier);

		String[] columnNameArray = sourceManager.getColumnNameArray();
		resultManager.setColumnNames(columnNameArray);

		int keyColumnIndex = sourceManager.getKeyColumnIndex();
		resultManager.setKeyColumnIndex(keyColumnIndex);
		
		return resultManager;
	}
	
	/**
	 * Because the columns in the data table is splitted by space (or table) characters, the data cells can not include
	 * space characters, and then we must replace space characters in a data cell to be semicolon(';') or LaTeX space("~") 
	 */
	public static String replaceSpaceWithSemicolon(String data) {
		return data.replace(' ', ';');
	}

	/**
	 * Because the columns in the data table is splitted by space (or table) characters, the data cells can not include
	 * space characters, and then we must replace space characters in a data cell to be semicolon(';') or LaTeX space("~") 
	 */
	public static String replaceSpaceWithLaTeXSpace(String data) {
		return data.replaceAll("\\h", "~");
	}

	/**
	 * Because the columns in the data table is splitted by space (or table) characters, the data cells can not include
	 * space characters, and then we must replace space characters in a data cell to be semicolon(';') or LaTeX space("~") 
	 */
	public static String replaceLaTeXSpaceWithSpace(String data) {
		return data.replace('~', ' ');
	}
}
