package analyzer.dataTable;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ21ÈÕ
 * @version 1.0
 */
public class TestDataTable {

	public static void testPercentileDistribution() {
		String dataPath = "C:\\ZxcWork\\ProgramAnalysis\\data\\PAmetric\\";
		String dataFile = dataPath + "data.txt";
		String resultFile = dataPath + "result.txt";
		String columnName = "OMMEC";
		
		DataTableManager manager = new DataTableManager("Metric");
		try {
			manager.read(dataFile, true);
			Debug.println("line number = " + manager.getLineNumber() + ", column number = " + manager.getColumnNumber());

			double[] valueArray = manager.getColumnAsDoubleArray(columnName);
			double[] percentileArray = {0, 5, 10, 25, 50};
			DataTableManager distributManager = DataTableUtil.generatePercentileDistribution(valueArray, percentileArray);
			
			distributManager.write(resultFile);
//			PrintWriter writer = new PrintWriter(new FileOutputStream(resultFile));
//			writeDataLinesAsLatexTableLines(writer, distributManager, null);
//			writer.close();
		} catch (Exception exec) {
			exec.printStackTrace();
		}
	}
	
	public static void testFindOutlierLines() {
		String dataPath = "C:\\ZxcWork\\ProgramAnalysis\\data\\PAmetric\\";
		String dataFile = dataPath + "PAmetric(class).txt";
		String outlierFile = dataPath + "outlier.txt";
		String resultFile = dataPath + "PA-inherit-outlier-class.txt";
		
		DataTableManager dataManager = new DataTableManager("Metric");
		DataTableManager outlierIndicator = new DataTableManager("OutlierIndicator");
		try {
			dataManager.read(dataFile, true);
			Debug.println("Data: line number = " + dataManager.getLineNumber() + ", column number = " + dataManager.getColumnNumber());
			
			outlierIndicator.read(outlierFile, true);
			
			DataTableManager resultManager = DataTableUtil.findOutlierLinesWithReason(dataManager, outlierIndicator);
			Debug.println("Result: line number = " + resultManager.getLineNumber() + ", column number = " + resultManager.getColumnNumber());
			resultManager.write(resultFile);
			
//			PrintWriter writer = new PrintWriter(new FileOutputStream(resultFile));
//			DataTableUtil.writeDataLinesAsLatexTableLines(writer, manager, null);
//			writer.close();
		} catch (Exception exec) {
			exec.printStackTrace();
		}
	}

	public static void testFindMaxMinLines() {
		String dataPath = "C:\\ZxcWork\\ProgramAnalysis\\data\\PAmetric\\";
		String dataFile = dataPath + "PA-coupling-corvalue.txt";
		String resultFile = dataPath + "temp.txt";
		
		DataTableManager dataManager = new DataTableManager("Metric");
		try {
			dataManager.read(dataFile, true);
			Debug.println("Data: line number = " + dataManager.getLineNumber() + ", column number = " + dataManager.getColumnNumber());
			
			DataTableManager resultManager = DataTableUtil.findMaxMinLines(dataManager, "Cor", "MetricA", 2, 1);
			Debug.println("Result: line number = " + resultManager.getLineNumber() + ", column number = " + resultManager.getColumnNumber());
			resultManager.write(resultFile);
			
//			PrintWriter writer = new PrintWriter(new FileOutputStream(resultFile));
//			DataTableUtil.writeDataLinesAsLatexTableLines(writer, manager, null);
//			writer.close();
		} catch (Exception exec) {
			exec.printStackTrace();
		}
	}

	public static void testSelectDataLinesOutOfRange() {
		String dataPath = "C:\\ZxcWork\\ProgramAnalysis\\data\\PAmetric\\";
		String dataFile = dataPath + "dataTwo.txt";
		String rangeFile = dataPath + "temp.txt";
		String resultOneFile = dataPath + "dataTwoOne.txt";
		String resultTwoFile = dataPath + "dataTwoTwo.txt";
		
		DataTableManager dataManager = new DataTableManager("Metric");
		DataTableManager rangeManager = new DataTableManager("Range");
		try {
			dataManager.read(dataFile, true);
			rangeManager.read(rangeFile,  true);
			Debug.println("Data: line number = " + dataManager.getLineNumber() + ", column number = " + dataManager.getColumnNumber());
			
			int[] selectedIndex = DataTableUtil.indexOfSelectedDataLinesOutOfRange(dataManager, rangeManager);
			DataTableManager resultOneManager = DataTableManager.createEmptyDataTableWithSameStructure(dataManager, "OutOfRange");
			DataTableManager resultTwoManager = DataTableManager.createEmptyDataTableWithSameStructure(dataManager, "Remain");

			for (int lineIndex = 0; lineIndex < dataManager.getLineNumber(); lineIndex++) {
				boolean isSelected = false;
				if (selectedIndex != null) {
					for (int i = 0; i < selectedIndex.length; i++) {
						if (selectedIndex[i] == lineIndex) {
							isSelected = true;
							break;
						}
					}
				}
				String[] lineStringArray = dataManager.getLineAsStringArray(lineIndex);
				if (isSelected == true) resultOneManager.appendLine(lineStringArray);
				else resultTwoManager.appendLine(lineStringArray);
			}
			
			Debug.println("OutOfRange: line number = " + resultOneManager.getLineNumber() + ", column number = " + resultOneManager.getColumnNumber());
			Debug.println("Remain: line number = " + resultTwoManager.getLineNumber() + ", column number = " + resultTwoManager.getColumnNumber());
			resultOneManager.write(resultOneFile);
			resultTwoManager.write(resultTwoFile);
			
//			PrintWriter writer = new PrintWriter(new FileOutputStream(resultFile));
//			DataTableUtil.writeDataLinesAsLatexTableLines(writer, manager, null);
//			writer.close();
		} catch (Exception exec) {
			exec.printStackTrace();
		}
	}

	public static void testWriteDataLineToLatex() {
		String dataPath = "C:\\ZxcWork\\ProgramAnalysis\\data\\";
		String dataFile = dataPath + "result.txt";
		String resultFile = dataPath + "temp.txt";
//		String[] columnNames = {"Metrics", "Min", "Q1", "Median", "Q3", "Max", "Sum", "Mean", "Sd", "Skew", "Kurt", "CV", "Gini" };
//		String[] columnNames = {"Metrics", "Len", "Median", "Mean", "Sd", "Skew", "Kurt", "CV", "Gini" };
//		String[] columnNames = {"Class", "FLD", "MTHD", "ELOC", "ALLMTHD", "IMPMTHD", "ALLFLD", "IHFLD" };
//		String[] columnNames = {"Class", "Reason"};
//		String[] columnNames = {"MetricA", "MetricB", "Cor"};
//		String[] columnNames = {"Fields", "Log", "Num", "Mean", "Sd", "Sd2", "Num2", "Sd3", "Num3" };
//		String[] columnNames = {"Version", "Domain", "Notes"};
		
		DataTableManager manager = new DataTableManager("Metric");
		try {
			manager.read(dataFile, true);
			Debug.println("line number = " + manager.getLineNumber() + ", column number = " + manager.getColumnNumber());
			
			PrintWriter writer = new PrintWriter(new FileOutputStream(resultFile));
			DataTableUtil.writeDataLinesAsLatexTableLines(writer, manager, null, 2);
			writer.close();
		} catch (Exception exec) {
			exec.printStackTrace();
		}
	}
	
	public static void testReadDataTable() {
		String dataPath = "C:\\ZxcWork\\ProgramAnalysis\\data\\";
		String dataFile = dataPath + "temp.txt";
		String resultFile = dataPath + "result.txt";

		DataTableManager manager = new DataTableManager("Metric");
		try {
			manager.read(dataFile, true);
			Debug.println("line number = " + manager.getLineNumber() + ", column number = " + manager.getColumnNumber());
			
			PrintWriter writer = new PrintWriter(new FileOutputStream(resultFile));
			manager.write(resultFile);
			writer.close();
		} catch (Exception exec) {
			exec.printStackTrace();
		}
	}

	public static void testExchangeRowAndColumn() {
		String dataPath = "C:\\ZxcWork\\ProgramAnalysis\\data\\";
		String dataFile = dataPath + "temp.txt";
		String resultFile = dataPath + "result.txt";
		
		DataTableManager manager = new DataTableManager("temp");
		try {
			manager.read(dataFile, true);
			Debug.println("line number = " + manager.getLineNumber() + ", column number = " + manager.getColumnNumber());
			
			DataTableManager result = DataTableUtil.exchangeRowAndColumn(manager);
			result.write(resultFile);;
		} catch (Exception exec) {
			exec.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		testSelectDataLinesOutOfRange();
//		testPercentileDistribution();
//		testFindOutlierLines();		
//		testFindMaxMinLines();
		testWriteDataLineToLatex();
//		testExchangeRowAndColumn();
//		testReadDataTable();
	}
}
