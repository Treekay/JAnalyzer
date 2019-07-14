package analyzer.dataTable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;

import util.Debug;

/**
 * A class to provide some static methods to utilize data table to generate some useful data or table
 * 
 * @author Zhou Xiaocong
 * @since 2015年10月19日
 * @version 1.0
 */
public class DataTableUtil {

	/**
	 * Count the number of different value in the column of dataTable. 
	 * The result data table has two column: Value, Number and Frequency
	 */
	public static DataTableManager countValueNumberDistribution(String[] valueArray) {
		if (valueArray == null || valueArray.length <= 0) return null;
		TreeMap<String, Integer> countMap = new TreeMap<String, Integer>();
		for (int i = 0; i < valueArray.length; i++) {
			String value = valueArray[i];
			Integer number = countMap.get(value);
			if (number == null) {
				countMap.put(value, new Integer(1));
			} else {
				countMap.put(value, new Integer(number.intValue()+1));
			}
		}
		
		DataTableManager result = new DataTableManager("ValueNumber");
		String[] columnNames = {"Value", "Number", "Frequency"};
		result.setColumnNames(columnNames);
		Set<String> valueSet = countMap.keySet();
		for (String value : valueSet) {
			Integer number = countMap.get(value);
			String numberString = countMap.get(value).toString();
			String[] lineArray = new String[3];
			lineArray[0] = value;
			lineArray[1] = numberString;
			double freq = (double)number.intValue() / valueArray.length;
			lineArray[2] =  Double.toString(freq);
			result.appendLine(lineArray);
		}
		return result;
	}
	
	/**
	 * Return the five numbers a double array. We will remove the NaN value in the column. 
	 * But if the column is not in the table or all values of the column are NaN, it will return NaN.
	 */
	public static double[] fiveNumberOfColumn(DataTableManager dataTable, String column) {
		double[] valueArray = dataTable.getColumnAsDoubleArray(column);
		return fiveNumberOfDoubleArray(valueArray);
	}

	/**
	 * Return the five numbers a double array. We will remove the NaN value in the column. 
	 * But if the column is not in the table or all values of the column are NaN, it will return NaN.
	 */
	public static double[] fiveNumberOfDoubleArray(double[] valueArray) {
		if (valueArray == null) return null;
		double[] result = new double[5];
		result[0] = StatUtils.min(valueArray);
		result[1] = StatUtils.percentile(valueArray, 25);
		result[2] = StatUtils.percentile(valueArray, 50);
		result[3] = StatUtils.percentile(valueArray, 75);
		result[4] = StatUtils.max(valueArray);
		return result;
	}

	/**
	 * Return the mean value of a double array. We will remove the NaN value in the column. 
	 * But if the column is not in the table or all values of the column are NaN, it will return NaN.
	 */
	public static double meanOfDoubleArray(double[] valueArray) {
		if (valueArray == null) return Double.NaN;
		double sum = 0.0;
		int counter = 0;
		for (int i = 0; i < valueArray.length; i++) {
			if (!Double.isInfinite(valueArray[i]) && !Double.isNaN(valueArray[i])) {
				sum += valueArray[i];
				counter += 1;
			}
		}
		if (counter > 0) return sum/counter;
		else return Double.NaN;
	}

	/**
	 * Return the mean value of a column in the given data table. We will remove the NaN value in the column. 
	 * But if the column is not in the table or all values of the column are NaN, it will return NaN.
	 */
	public static double meanOfColumn(DataTableManager dataTable, String column) {
		double[] valueArray = dataTable.getColumnAsDoubleArray(column);
		if (valueArray == null) return Double.NaN;
		double sum = 0.0;
		int counter = 0;
		for (int i = 0; i < valueArray.length; i++) {
			if (!Double.isInfinite(valueArray[i]) && !Double.isNaN(valueArray[i])) {
				sum += valueArray[i];
				counter += 1;
			}
		}
		if (counter > 0) return sum/counter;
		else return Double.NaN;
	}
	
	/**
	 * Return an array of index of the values in valueArray. By the returned index array, we can easily scan the 
	 * values in the valueArray according to decreasing order or increasing order of the values.   
	 * That is, if decreasingOrder == true, then the returned array [0] is the index of the maximal value in the array 
	 * and the returned array [length-1] is the index of the minimal value in the array with the minimal value. 
	 * Similarly, if decreasingOrder == false, then the returned array [0] is the index of the minimal value in the array
	 * while the returned array [length-1] is the index of the maximal value in the array.      
	 */
	public static int[] getSortedIndexOfDoubleArray(double[] valueArray, boolean decreasingOrder) {
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
	 * Return an array of index of the values in valueArray. By the returned index array, we can easily scan the 
	 * values in the valueArray according to decreasing order or increasing order of the values.   
	 * That is, if decreasingOrder == true, then the returned array [0] is the index of the maximal value in the array 
	 * and the returned array [length-1] is the index of the minimal value in the array with the minimal value. 
	 * Similarly, if decreasingOrder == false, then the returned array [0] is the index of the minimal value in the array
	 * while the returned array [length-1] is the index of the maximal value in the array.      
	 */
	public static int[] getSortedIndexOfStringArray(String[] valueArray, boolean decreasingOrder) {
		StringIndexedPair[] indexedValueArray = new StringIndexedPair[valueArray.length];
		for (int index = 0; index < valueArray.length; index++) {
			indexedValueArray[index] = new StringIndexedPair(valueArray[index], index); 
		}
		Arrays.sort(indexedValueArray);
		int[] sortedIndexArray = new int[valueArray.length];
		for (int index = 0; index < valueArray.length; index++) {
			if (decreasingOrder == false) sortedIndexArray[index] = indexedValueArray[index].index;
			else sortedIndexArray[index] = indexedValueArray[(valueArray.length-1)-index].index; 
		}
		return sortedIndexArray;
	}

	/**
	 *	给定一个百分比数组  percentileArray, 生成 double 数组　valueArray　中值的如下分布情况：
	 * Item    percentileArray[0]  percentileArray[1]  ...  percentileArray[n-1], percentileArray[i] 给出一个0到100之间的数
	 * Rank    给出valueArray中按照降序排列的排前百分之percentileArray[i]的数据的排名，如果percentileArray[i] = 0，则对应最大值，从而 Rank = 1
	 * Value   给出valueArray中排名在对应 Rank 的数据值
	 * Sum	       给出valueArray中从最大值到排名在Rank的数据之间的值的总和
	 * Ratio   给出valueArray中从最大值到排名在Rank的数据之间的值的总和占valueArray中所有值的总和的比
	 * 
	 * 注意，valueArray 中非double值（也即是 Double.NaN的数据）将作为最小值处理。	            
	 */
	public static DataTableManager generatePercentileDistribution(double[] valueArray, double[] percentileArray) {
		if (valueArray == null || percentileArray == null) return null;
		if (valueArray.length <= 0 || percentileArray.length <= 0) return null;
		
		DataTableManager tableManager = new DataTableManager("Percentile distribution");
		
		int[] sortedIndexArray = getSortedIndexOfDoubleArray(valueArray, true);
		// Calculate the sum of the values in valueArray
		double totalSum = 0;
		for (int index = 0; index < valueArray.length; index++) {
			if (!Double.isInfinite(valueArray[index])) totalSum += valueArray[index];
		}

		String[] columnNameArray = new String[percentileArray.length + 1];
		columnNameArray[0] = "Item";
		for (int index = 0; index < percentileArray.length; index++) {
			if (percentileArray[index] == 0) columnNameArray[index+1] = "Max";
			else columnNameArray[index+1] = Double.toString(percentileArray[index]);
		}
		tableManager.setColumnNames(columnNameArray);
		tableManager.setKeyColumnIndex(0);
		String[] itemColumn = {"Rank", "Value", "Sum", "Ratio"};
		tableManager.setColumnValue(columnNameArray[0], itemColumn);
		
		double currentSum = 0;
		int lastRank = 0;
		for (int index = 0; index < percentileArray.length; index++) {
			int rank = 1;
			if (percentileArray[index] == 0) {
				// We should get the maximal value in the valueArray, that is its rank should be 1
				rank = 1; 
			} else {
				// We compute the rank of the value that is ranked at the front of percentileArray[index]% in valueArray
				// For example, if we have 200 values in valueArray (i.e. valueArray.length = 200), and 
				// percentileArray[index] = 5, we should get the value at ranked 10
				rank = (int)((percentileArray[index] / 100) * valueArray.length);
				if (rank > valueArray.length) rank = valueArray.length;
			}
			// Note that the range of rank is from 1 to value.length, but the range of array index is from 0 to value.length-1
			// So we have to minus 1 from rank to get the index of the value.
			double value = valueArray[sortedIndexArray[rank-1]];
			// Compute the sum of the value which at ranked from 1 to rank-1. Since the currentSum has stored the sum
			// of the value which at ranked from 1 to lastRank, so here we only add the value which at ranked from
			// lastRank+1 to rank.
			for (int sumRank = lastRank+1; sumRank <= rank; sumRank++) {
				currentSum += valueArray[sortedIndexArray[sumRank-1]];
			}
			double ratio = 0;
			if (totalSum != 0) ratio = currentSum / totalSum;
			
			tableManager.setCellValue("Rank", columnNameArray[index+1], rank);
			tableManager.setCellValue("Value", columnNameArray[index+1], value);
			tableManager.setCellValue("Sum", columnNameArray[index+1], currentSum);
			tableManager.setCellValue("Ratio", columnNameArray[index+1], ratio);
			
			lastRank = rank;		// Remember the rank for computing the currentSum 
		}
		return tableManager;
	}

	public static DataTableManager findOutlierLines(DataTableManager dataManager, DataTableManager outlierIndicator) {
		return findOutlierLines(dataManager, outlierIndicator, false);
	}

	public static DataTableManager findOutlierLinesWithReason(DataTableManager dataManager, DataTableManager outlierIndicator) {
		return findOutlierLines(dataManager, outlierIndicator, true);
	}

	/**
	 * 在数据表 dataManager 中找异常值，异常值标准由数据表 outlierIndicator 提供。outlierIndicator 的数据列 Fields 的值对应 dataManager 中的列名，outlierIndicator
	 * 的数据列 Sd2 值对应异常值标准，例如outlierIndicator的某一行内容为："FLD 20.1"，表示dataManager中的列名为 FLD 的列的值大于 20.1 的数据行就是异常值。outlierIndicator
	 * 可能有多个异常值标准，这些标准之间的逻辑关系是“或”，即只要满足其中一条标准就是异常值。找到的异常值行将放在返回的数据表中，返回数据表列结构在参数 needReason 为false 时与
	 * dataManager的列结构完全一致，当参数 needReason 为true时则增加一列"Reason"来记录被选中为异常值的数据行的原因，即它的哪些列满足了异常值标准。   
	 */
	public static DataTableManager findOutlierLines(DataTableManager dataManager, DataTableManager outlierIndicator, boolean needReason) {
		DataTableManager resultManager = new DataTableManager("Outlier");
		String[] columnNameArray = dataManager.getColumnNameArray();
		String[] resultColumnNameArray = null;
		if (needReason == true) {
			resultColumnNameArray = new String[columnNameArray.length+2];
			resultColumnNameArray[resultColumnNameArray.length-2] = "Reason";
			resultColumnNameArray[resultColumnNameArray.length-1] = "Upper";
		} else {
			resultColumnNameArray = new String[columnNameArray.length+1];
			resultColumnNameArray[resultColumnNameArray.length-1] = "Upper";
		}
		for (int i = 0; i < columnNameArray.length; i++) resultColumnNameArray[i] = columnNameArray[i];
		resultManager.setColumnNames(resultColumnNameArray);

		int keyColumnIndex = dataManager.getKeyColumnIndex();
		resultManager.setKeyColumnIndex(keyColumnIndex);

		DataLineDefaultFilter filter = new DataLineDefaultFilter();
		for (int lineIndex = 0; lineIndex < outlierIndicator.getLineNumber(); lineIndex++) {
			String columnName = outlierIndicator.getCellValueAsString(lineIndex, "Fields");
			String outlierValue = outlierIndicator.getCellValueAsString(lineIndex, "Sd2");
			if (outlierValue == null) outlierValue = outlierIndicator.getCellValueAsString(lineIndex, "OutUp");
			if (outlierValue != null) {
				DataLineFilterValueCondition condition = new DataLineFilterValueCondition(columnName, outlierValue);
				condition.setLogic(ConditionLogicKind.CLK_OR);
				condition.setType(ConditionDataTypeKind.CDTK_NUMERIC);
				condition.setRelation(ConditionRelationKind.CRK_GREATER_EQUAL);
				filter.addCondition(condition);
			}
		}
		if (needReason == true) filter.setNeedReasonOn();
		
		for (int lineIndex = 0; lineIndex < dataManager.getLineNumber(); lineIndex++) {
			if (filter.accept(dataManager, lineIndex)) {
				Debug.println("Accept line " + lineIndex);
				
				String[] lineStringArray = dataManager.getLineAsStringArray(lineIndex);
				String[] resultLineStringArray = null;
				if (needReason == true) {
					resultLineStringArray = new String[lineStringArray.length+2];
					for (int i = 0; i < lineStringArray.length; i++) resultLineStringArray[i] = lineStringArray[i];
					resultLineStringArray[resultLineStringArray.length-2] = filter.getReason();
				} else resultLineStringArray = new String[lineStringArray.length+1];
				resultLineStringArray[resultLineStringArray.length-1] = "TRUE";
				resultManager.appendLine(resultLineStringArray);
			}
		}

		filter = new DataLineDefaultFilter();
		for (int lineIndex = 0; lineIndex < outlierIndicator.getLineNumber(); lineIndex++) {
			String columnName = outlierIndicator.getCellValueAsString(lineIndex, "Fields");
			String outlierValue = outlierIndicator.getCellValueAsString(lineIndex, "SdN2");
			if (outlierValue == null) outlierValue = outlierIndicator.getCellValueAsString(lineIndex, "OutLow");
			if (outlierValue != null) {
				DataLineFilterIntervalCondition condition = new DataLineFilterIntervalCondition(columnName, "0.0", outlierValue);
				condition.setLogic(ConditionLogicKind.CLK_OR);
				condition.setType(ConditionDataTypeKind.CDTK_NUMERIC);
				condition.setRelation(ConditionRelationKind.CRK_IN_INTERVAL);
				condition.setInterval(ConditionIntervalKind.CIK_OPEN_CLOSE);
				filter.addCondition(condition);
			}
		}
		if (needReason == true) filter.setNeedReasonOn();
		
		for (int lineIndex = 0; lineIndex < dataManager.getLineNumber(); lineIndex++) {
			if (filter.accept(dataManager, lineIndex)) {
				Debug.println("Accept line " + lineIndex);
				
				String[] lineStringArray = dataManager.getLineAsStringArray(lineIndex);
				String[] resultLineStringArray = null;
				if (needReason == true) {
					resultLineStringArray = new String[lineStringArray.length+2];
					for (int i = 0; i < lineStringArray.length; i++) resultLineStringArray[i] = lineStringArray[i];
					resultLineStringArray[resultLineStringArray.length-2] = filter.getReason();
				} else resultLineStringArray = new String[lineStringArray.length+1];
				resultLineStringArray[resultLineStringArray.length-1] = "FALSE";
				resultManager.appendLine(resultLineStringArray);
			}
		}
		
		return resultManager;
	}
	
	/**
	 * 在数据表 dataManager 查找列 columnName 取最大值和最小值的数据行。参数groupBy 也是 dataManager 中的列，表明在查找 dataManager 中列columnName 取
	 * 最大值最小值时需要根据 groupBy 这一列的值进行分组，这时参数 groups 提供了列 groupBy 这一列中用于分组的不同的数据值，即 dataManager 中的列 groupBy 都等于 groups[i] 的
	 * 行为一组数据，查找这一组数据中列 columnName 取最大值最小值的数据行放到返回的数据表中。参数 maxMin = 0 表示最大值和最小值都要，= 1 表示只要最大值，= -1 表示只要最小值。
	 * 参数 findLineNumber 表示要查找多少个最值，例如等于1表示只要最大值和/或最小值，等于2则表示除了最大值和/或最小值外，还要次大值和/或次小值等等。返回的数据表列结构与 dataManager 
	 * 完全相同。 
	 * <p>Note that the type of the column columnName must be double or int, and the type of the column groupBy must be String
	 * 
	 */
	public static DataTableManager findMaxMinLines(DataTableManager dataManager, String columnName, String groupBy, String[] groups, int findLineNumber, int maxMin) {
		// resultLineIndex will be the index of the line finally selected
		int[] resultLineIndexArray = null;
		if (maxMin == 0) resultLineIndexArray = new int[groups.length * findLineNumber * 2];
		else resultLineIndexArray = new int[groups.length * findLineNumber];
		for (int resultIndex = 0; resultIndex < resultLineIndexArray.length; resultIndex++) resultLineIndexArray[resultIndex] = -1;
		
		int resultIndex = 0;
		for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
			// The data line filter is used to select the line whose value of the column groupBy equals groups[groupIndex] 
			DataLineFilterValueCondition condition = new DataLineFilterValueCondition(groupBy, groups[groupIndex]);
			condition.setRelation(ConditionRelationKind.CRK_EQUAL);
			condition.setType(ConditionDataTypeKind.CDTK_STRING);
			DataLineDefaultFilter filter = new DataLineDefaultFilter();
			filter.addCondition(condition);
			
			List<ValueIndexPair> pairList = new ArrayList<ValueIndexPair>();
			for (int dataIndex = 0; dataIndex < dataManager.getLineNumber(); dataIndex++) {
				if (filter.accept(dataManager, dataIndex)) {
					// Add the value of the column columnName and its line index to pariList
					double value = dataManager.getCellValueAsDouble(dataIndex, columnName);
					pairList.add(new ValueIndexPair(value, dataIndex));
				}
			}
			
			if (pairList.size() <= 0) {
				// There is no line whose value of the column groupBy equals groups[groupIndex]
				continue;
			}
			
			double[] lineValueArray = new double[pairList.size()];
			int[] lineIndexArray = new int[pairList.size()];
			for (int i = 0; i < pairList.size(); i++) {
				lineValueArray[i] = pairList.get(i).value;
				lineIndexArray[i] = pairList.get(i).index;
			}
			// Find the maximal and minimal value in the lineValueArray by generating its sorted index 
			int[] sortedIndex = getSortedIndexOfDoubleArray(lineValueArray, true);
			for (int i = 0; i < findLineNumber; i++) {
				int maxIndex = i;
				if (maxMin == 0 || maxMin == 1) {
					if (maxIndex >= 0 && maxIndex < lineValueArray.length) {
						// Correspondingly, the content of lineIndexArray[maxIndex] will be the index of the line in the dataManager
						// which the value of the column columnName is the i th maximal value in this group
						resultLineIndexArray[resultIndex] = lineIndexArray[sortedIndex[maxIndex]];
						resultIndex = resultIndex + 1;
					}
				}
			}
			for (int i = 0; i < findLineNumber; i++) {
				int minIndex = sortedIndex.length-i-1;
				if (maxMin == 0 || maxMin == -1) {
					if (minIndex >= 0 && minIndex < lineValueArray.length) {
						// Correspondingly, the content of lineIndexArray[minIndex] will be the index of the line in the dataManager
						// which the value of the column columnName is the i th minimal value in this group
						resultLineIndexArray[resultIndex] = lineIndexArray[sortedIndex[minIndex]];
						resultIndex = resultIndex + 1;
					}
				}
			}
		}

		DataTableManager resultManager = DataTableManager.createEmptyDataTableWithSameStructure(dataManager, "MaxMin");
		// So far, the index of the line should be selected are stored in the resultLineIndexArray
		for (int i = 0; i < resultLineIndexArray.length; i++) {
			int index = resultLineIndexArray[i];
			if (index >= 0 && index < dataManager.getLineNumber()) {
				String[] lineStringArray = dataManager.getLineAsStringArray(index);
				resultManager.appendLine(lineStringArray);
			}
		}
		return resultManager;
	}
	

	/**
	 * 在数据表 dataManager 查找列 columnName 取最大值和最小值的数据行。参数 maxMin = 0 表示最大值和最小值都要，= 1 表示只要最大值，= -1 表示只要最小值。
	 * 参数 findLineNumber 表示要查找多少个最值，例如等于1表示只要最大值和/或最小值，等于2则表示除了最大值和/或最小值外，还要次大值和/或次小值等等。返回的数据表列结构与 dataManager 
	 * 完全相同。 
	 * <p>Note that the type of the column columnName must be double or int
	 * 
	 */
	public static DataTableManager findMaxMinLines(DataTableManager dataManager, String columnName, int findLineNumber, int maxMin) {
		DataTableManager resultManager = new DataTableManager("MaxMin");

		String[] columnNameArray = dataManager.getColumnNameArray();
		resultManager.setColumnNames(columnNameArray);

		int keyColumnIndex = dataManager.getKeyColumnIndex();
		resultManager.setKeyColumnIndex(keyColumnIndex);

		double[] lineValueArray = dataManager.getColumnAsDoubleArray(columnName);
		int[] sortedIndex = getSortedIndexOfDoubleArray(lineValueArray, true);
		
		for (int i = 0; i < findLineNumber; i++) {
			int maxIndex = i;
			if (maxMin == 0 || maxMin == 1) {
				if (maxIndex >= 0 && maxIndex < lineValueArray.length) {
					// Correspondingly, sortedIndex[maxIndex] is the index of the line in the dataManager
					// which the value of the column columnName is the i th maximal value in this group
					String[] lineStringArray = dataManager.getLineAsStringArray(sortedIndex[maxIndex]);
					resultManager.appendLine(lineStringArray);
				}
			}
		}
		for (int i = 0; i < findLineNumber; i++) {
			int minIndex = sortedIndex.length-i-1;
			if (maxMin == 0 || maxMin == -1) {
				if (minIndex >= 0 && minIndex < lineValueArray.length) {
					// Correspondingly, the content of lineIndexArray[minIndex] will be the index of the line in the dataManager
					// which the value of the column columnName is the i th minimal value in this group
					String[] lineStringArray = dataManager.getLineAsStringArray(sortedIndex[minIndex]);
					resultManager.appendLine(lineStringArray);
				}
			}
		}
		return resultManager;
	}
	
	/**
	 * 在数据表 dataManager 查找列 columnName 取最大值和最小值的数据行。参数groupBy 也是 dataManager 中的列，表明在查找 dataManager 中列columnName 取
	 * 最大值最小值时需要根据 groupBy 这一列的所有不同的值进行分组。参数 maxMin = 0 表示最大值和最小值都要，= 1 表示只要最大值，= -1 表示只要最小值。
	 * 参数 findLineNumber 表示要查找多少个最值，例如等于1表示只要最大值和/或最小值，等于2则表示除了最大值和/或最小值外，还要次大值和/或次小值等等。返回的数据表列结构与 dataManager 
	 * 完全相同。 
	 * <p>Note that the type of the column columnName must be double or int, and the type of the column groupBy must be String
	 * 
	 */
	public static DataTableManager findMaxMinLines(DataTableManager dataManager, String columnName, String groupBy, int findLineNumber, int maxMin) {
		if (groupBy == null) return findMaxMinLines(dataManager, columnName, findLineNumber, maxMin);
		
		String[] groupColumnStringArray = dataManager.getColumnAsStringArray(groupBy);
		Set<String> groupValueSet = new TreeSet<String>();
		// Find the different values in the column groupBy
		for (int i = 0; i < groupColumnStringArray.length; i++) {
			String groupValue = groupColumnStringArray[i];
			if (!groupValueSet.contains(groupValue)) groupValueSet.add(groupValue);
		}
		// Transform the groupValueSet to an array
		String[] groupValueArray = new String[groupValueSet.size()];
		int index = 0;
		for (String groupValue : groupValueSet) {
			groupValueArray[index] = groupValue;
			index = index + 1;
		}
		return findMaxMinLines(dataManager, columnName, groupBy, groupValueArray, findLineNumber, maxMin);
	}
	
	/**
	 * 从数据表 dataManager 中选择一些在某个范围之外的行。范围由数据表 rangeIndicator 指定，数据表 rangeIndicator 的至少含有 "Fields", "Min", "Max" 这几列，
	 * 其中列 "Fields"的值对应数据表 dataManager 的列名，"Min"和"Max"的值是 double 类型。<b>数据表 rangeIndicator 的每一行数据用来选择 dataManager 中的对应列（由
	 * rangeIndicator 的 "Fields"列指定）小于等于最小值（由rangeIndicator的 "Min"列指定）或大于等于最大值（由rangeIndicator的"Max"列指定）的数据行</b>。数据表 
	 * rangeIndicator 可针对数据表 dataManager 的多个列指定范围，它的每一行对应数据表 dataManager 的一列（应在的范围），但 rangeIndicator 的多个行所指定的范围 
	 * 条件之间是<b>与关系</b>，即数据表 dataManager 中的某一行的值在 rangeIndicator 所有行指定的范围外，则该行才会被选中。选中的行放到返回的结果数
	 * 据表。返回的结果数据表与 dataManager 有完全相同的结构（只是行是选中的行而已）。 
	 */
	public static DataTableManager selectDataLinesOutOfRange(DataTableManager dataManager, DataTableManager rangeIndicator) {
		DataLineDefaultFilter filter = new DataLineDefaultFilter();
		for (int lineIndex = 0; lineIndex < rangeIndicator.getLineNumber(); lineIndex++) {
			String columnName = rangeIndicator.getCellValueAsString(lineIndex, "Fields");
			String minValue =  rangeIndicator.getCellValueAsString(lineIndex, "Min");
			String maxValue =  rangeIndicator.getCellValueAsString(lineIndex, "Max");
			DataLineFilterIntervalCondition condition = new DataLineFilterIntervalCondition(columnName, minValue, maxValue);
			condition.setLogic(ConditionLogicKind.CLK_AND);
			condition.setType(ConditionDataTypeKind.CDTK_NUMERIC);
			condition.setRelation(ConditionRelationKind.CRK_OUT_INTERVAL);
			condition.setInterval(ConditionIntervalKind.CIK_OPEN_OPEN);
			filter.addCondition(condition);
		}
		
		DataTableManager resultManager = DataTableManager.createEmptyDataTableWithSameStructure(dataManager, "OutOfRange");
		for (int lineIndex = 0; lineIndex < dataManager.getLineNumber(); lineIndex++) {
			if (filter.accept(dataManager, lineIndex)) {
				String[] lineStringArray = dataManager.getLineAsStringArray(lineIndex);
				resultManager.appendLine(lineStringArray);
			}
		}
		return resultManager;
	}
	
	/**
	 * 从数据表 dataManager 中选择一些在某个范围之外的行。范围由数据表 rangeIndicator 指定，数据表 rangeIndicator 的至少含有 "Fields", "Min", "Max" 这几列，
	 * 其中列 "Fields"的值对应数据表 dataManager 的列名，"Min"和"Max"的值是 double 类型。<b>数据表 rangeIndicator 的每一行数据用来选择 dataManager 中的对应列（由
	 * rangeIndicator 的 "Fields"列指定）小于等于最小值（由rangeIndicator的 "Min"列指定）或大于等于最大值（由rangeIndicator的"Max"列指定）的数据行</b>。数据表 
	 * rangeIndicator 可针对数据表 dataManager 的多个列指定范围，它的每一行对应数据表 dataManager 的一列（应在的范围），但 rangeIndicator 的多个行所指定的范围 
	 * 条件之间是<b>与关系</b>，即数据表 dataManager 中的某一行的值在 rangeIndicator 所有行指定的范围外，则该行才会被选中。
	 * <p>这个方法返回选中的行的下标所构成的数组 
	 */
	public static int[] indexOfSelectedDataLinesOutOfRange(DataTableManager dataManager, DataTableManager rangeIndicator) {
		DataLineDefaultFilter filter = new DataLineDefaultFilter();
		for (int lineIndex = 0; lineIndex < rangeIndicator.getLineNumber(); lineIndex++) {
			String columnName = rangeIndicator.getCellValueAsString(lineIndex, "Fields");
			String minValue =  rangeIndicator.getCellValueAsString(lineIndex, "Min");
			String maxValue =  rangeIndicator.getCellValueAsString(lineIndex, "Max");
			DataLineFilterIntervalCondition condition = new DataLineFilterIntervalCondition(columnName, minValue, maxValue);
			condition.setLogic(ConditionLogicKind.CLK_AND);
			condition.setType(ConditionDataTypeKind.CDTK_NUMERIC);
			condition.setRelation(ConditionRelationKind.CRK_OUT_INTERVAL);
			condition.setInterval(ConditionIntervalKind.CIK_OPEN_OPEN);
			filter.addCondition(condition);
		}

		boolean[] lineSelected = new boolean[dataManager.getLineNumber()];
		int selectedCounter = 0;
		for (int lineIndex = 0; lineIndex < dataManager.getLineNumber(); lineIndex++) {
			if (filter.accept(dataManager, lineIndex)) {
				lineSelected[lineIndex] = true;
				selectedCounter++;
			}
			else lineSelected[lineIndex] = false;
		}
		if (selectedCounter > 0) {
			int[] resultIndexArray = new int[selectedCounter];
			int index = 0;
			for (int lineIndex = 0; lineIndex < dataManager.getLineNumber(); lineIndex++) {
				if (lineSelected[lineIndex] == true) {
					resultIndexArray[index] = lineIndex;
					index = index + 1;
				}
			}
			return resultIndexArray;
		} else return null;
	}

	/**
	 * 从数据表 dataManager 中选择一些在某个范围之内的行。范围由数据表 rangeIndicator 指定，数据表 rangeIndicator 的至少含有 "Fields", "Min", "Max" 这几列，
	 * 其中列 "Fields"的值对应数据表 dataManager 的列名，"Min"和"Max"的值是 double 类型。<b>数据表 rangeIndicator 的每一行数据用来选择 dataManager 中的对应列（由
	 * rangeIndicator 的 "Fields"列指定）大于等于最小值（由rangeIndicator的 "Min"列指定）且小于等于最大值（由rangeIndicator的"Max"列指定）的数据行</b>。数据表 
	 * rangeIndicator 可针对数据表 dataManager 的多个列指定范围，它的每一行对应数据表 dataManager 的一列（应在的范围），但 rangeIndicator 的多个行所指定的范围 
	 * 条件之间是<b>与关系</b>，而非或关系，即数据表 dataManager 中某一行相应的列都满足 rangeIndicator 指定的范围内，该行才会被选中。选中的行放到返回的结果数
	 * 据表。返回的结果数据表与 dataManager 有完全相同的结构（只是行是选中的行而已）。 
	 */
	public static DataTableManager selectDataLinesInRange(DataTableManager dataManager, DataTableManager rangeIndicator) {
		DataLineDefaultFilter filter = new DataLineDefaultFilter();
		for (int lineIndex = 0; lineIndex < rangeIndicator.getLineNumber(); lineIndex++) {
			String columnName = rangeIndicator.getCellValueAsString(lineIndex, "Fields");
			String minValue =  rangeIndicator.getCellValueAsString(lineIndex, "Min");
			String maxValue =  rangeIndicator.getCellValueAsString(lineIndex, "Max");
			DataLineFilterIntervalCondition condition = new DataLineFilterIntervalCondition(columnName, minValue, maxValue);
			condition.setLogic(ConditionLogicKind.CLK_AND);
			condition.setType(ConditionDataTypeKind.CDTK_NUMERIC);
			condition.setRelation(ConditionRelationKind.CRK_IN_INTERVAL);
			condition.setInterval(ConditionIntervalKind.CIK_CLOSE_CLOSE);
			filter.addCondition(condition);
		}
		
		DataTableManager resultManager = DataTableManager.createEmptyDataTableWithSameStructure(dataManager, "InRange");
		for (int lineIndex = 0; lineIndex < dataManager.getLineNumber(); lineIndex++) {
			if (filter.accept(dataManager, lineIndex)) {
				String[] lineStringArray = dataManager.getLineAsStringArray(lineIndex);
				resultManager.appendLine(lineStringArray);
			}
		}
		return resultManager;
	}
	
	/**
	 * 从数据表 dataManager 中选择一些在某个范围之内的行。范围由数据表 rangeIndicator 指定，数据表 rangeIndicator 的至少含有 "Fields", "Min", "Max" 这几列，
	 * 其中列 "Fields"的值对应数据表 dataManager 的列名，"Min"和"Max"的值是 double 类型。<b>数据表 rangeIndicator 的每一行数据用来选择 dataManager 中的对应列（由
	 * rangeIndicator 的 "Fields"列指定）大于等于最小值（由rangeIndicator的 "Min"列指定）且小于等于最大值（由rangeIndicator的"Max"列指定）的数据行</b>。数据表 
	 * rangeIndicator 可针对数据表 dataManager 的多个列指定范围，它的每一行对应数据表 dataManager 的一列（应在的范围），但 rangeIndicator 的多个行所指定的范围 
	 * 条件之间是<b>与关系</b>，而非或关系，即数据表 dataManager 中某一行相应的列都满足 rangeIndicator 指定的范围内，该行才会被选中。选中的行放到返回的结果数
	 * 据表。返回的结果数据表与 dataManager 有完全相同的结构（只是行是选中的行而已）。 
	 * <p>这个方法返回选中的行的下标所构成的数组 
	 */
	public static int[] indexOfSelectedDataLinesInRange(DataTableManager dataManager, DataTableManager rangeIndicator) {
		DataLineDefaultFilter filter = new DataLineDefaultFilter();
		for (int lineIndex = 0; lineIndex < rangeIndicator.getLineNumber(); lineIndex++) {
			String columnName = rangeIndicator.getCellValueAsString(lineIndex, "Fields");
			String minValue =  rangeIndicator.getCellValueAsString(lineIndex, "Min");
			String maxValue =  rangeIndicator.getCellValueAsString(lineIndex, "Max");
			DataLineFilterIntervalCondition condition = new DataLineFilterIntervalCondition(columnName, minValue, maxValue);
			condition.setLogic(ConditionLogicKind.CLK_AND);
			condition.setType(ConditionDataTypeKind.CDTK_NUMERIC);
			condition.setRelation(ConditionRelationKind.CRK_IN_INTERVAL);
			condition.setInterval(ConditionIntervalKind.CIK_CLOSE_CLOSE);
			filter.addCondition(condition);
		}

		boolean[] lineSelected = new boolean[dataManager.getLineNumber()];
		int selectedCounter = 0;
		for (int lineIndex = 0; lineIndex < dataManager.getLineNumber(); lineIndex++) {
			if (filter.accept(dataManager, lineIndex)) {
				lineSelected[lineIndex] = true;
				selectedCounter++;
			}
			else lineSelected[lineIndex] = false;
		}
		if (selectedCounter > 0) {
			int[] resultIndexArray = new int[selectedCounter];
			int index = 0;
			for (int lineIndex = 0; lineIndex < dataManager.getLineNumber(); lineIndex++) {
				if (lineSelected[lineIndex] == true) {
					resultIndexArray[index] = lineIndex;
					index = index + 1;
				}
			}
			return resultIndexArray;
		} else return null;
	}

	/**
	 * 将数据表的行列转换，原来的列名作为第一行，而原来的第一行作为列名 
	 */
	public static DataTableManager exchangeRowAndColumn(DataTableManager manager) {
		DataTableManager result = new DataTableManager("result");
		
		String[] columnNames = manager.getColumnNameArray();
		String[] columnValueArray = manager.getColumnAsStringArray(columnNames[0]);
		String[] resultColumnNames = new String[columnValueArray.length+1];
		resultColumnNames[0] = columnNames[0];
		for (int i = 1; i < resultColumnNames.length; i++) resultColumnNames[i] = columnValueArray[i-1];
		result.setColumnNames(resultColumnNames);
		
		for (int i = 1; i < columnNames.length; i++) {
			columnValueArray = manager.getColumnAsStringArray(columnNames[i]);
			String[] lineArray = new String[columnValueArray.length+1];
			lineArray[0] = columnNames[i];
			for (int j = 1; j < lineArray.length; j++) lineArray[j] = columnValueArray[j-1];
			result.appendLine(lineArray);
		}
		
		return result;
	}
	
	/**
	 * 将数据表 table 的内容写成可以在 LaTex 作为表格显示的样式，其中 columnNames 用来选择 table 中的列。mergeLine 表示 table 中的几行数据合并为 LaTex 中表格的一行进行打印，当
	 * 选择的 table 的列比较少的时候可以几行数据打印在 LaTex表格中的一行。 
	 */
	public static void writeDataLinesAsLatexTableLines(PrintWriter writer, DataTableManager table, String[] columnNames, int mergeLine) throws IOException {
		if (columnNames == null) columnNames = table.getColumnNameArray();
		if (mergeLine <= 0) mergeLine = 1;
		
		writer.print("\\begin{tabular}{");
		for (int i = 0; i < mergeLine; i++) {
			for (int colIndex = 0; colIndex < columnNames.length; colIndex++) {
				if (colIndex == 0 && i == 0) writer.print("c");
				else writer.print("|c");
			}
		}
		writer.println("}");
		
		writer.print("\\hline");
		for (int i = 0; i < mergeLine; i++) {
			for (int colIndex = 0; colIndex < columnNames.length; colIndex++) {
				if (colIndex == 0 && i == 0) writer.print("{\\heiti " + columnNames[colIndex] + "} ");
				else writer.print(" & " + "{\\heiti " + columnNames[colIndex] + "} ");
			}
		}
		writer.println(" \\\\");
		
		int index = 0;
		while (index < table.getLineNumber()) {
			writer.print("\\hline ");
			for (int i = 0; i < mergeLine; i++) {
				String[] lineStringArray = null;
				if (index < table.getLineNumber()) {
					lineStringArray = table.getLineAsStringArray(index, columnNames);
				}
				for (int colIndex = 0; colIndex < columnNames.length; colIndex++) {
					String columnString = " ";
					if (lineStringArray != null) columnString = lineStringArray[colIndex];
					
/*					if (colIndex == 0) {
						if (i == 0) writer.print("\\pname{" + columnString + "} ");
						else writer.print(" & \\pname{" + columnString + "} ");
					} else writer.print(" & " + columnString);*/
					if (colIndex == 0) {
					if (i == 0) writer.print(columnString);
					else writer.print(" & " + columnString);
				} else writer.print(" & " + columnString);
				}
				index = index + 1;
			}
			writer.println(" \\\\");
		}
		
		writer.println("\\hline");
		writer.println("\\end{tabular}");
	}
	
	public static void writeDataLinesAsLatexTableLines(PrintWriter writer, DataTableManager table, String[] columnNames) throws IOException {
		writeDataLinesAsLatexTableLines(writer, table, columnNames, 1);
	}


}

class ValueIndexPair {
	double value;
	int index;
	
	public ValueIndexPair(double value, int index) {
		this.value = value;
		this.index = index;
	}
}

class StringIndexedPair implements Comparable<StringIndexedPair> {
	String value;
	int index;
	
	public StringIndexedPair(String value, int index) {
		this.value = value;
		this.index = index;
	}

	@Override
	public int compareTo(StringIndexedPair other) {
		return value.compareTo(other.value);
	}
}

class StringIndexedArray implements Comparable<StringIndexedArray> {
	String[] array;
	int index;
	boolean[] order;
	
	public StringIndexedArray(int arrayLength, int index, boolean[] order) {
		this.array = new String[arrayLength];
		this.index = index;
		this.order = order;
	}

	@Override
	public int compareTo(StringIndexedArray other) {
		for (int i = 0; i < array.length; i++) {
			int result = array[i].compareTo(other.array[i]);
			if (result != 0) {
				if (order[i] == true) return -result;  // decreasing order
				else return result;  // increasing order
			}
		}
		return 0;
	}
}
