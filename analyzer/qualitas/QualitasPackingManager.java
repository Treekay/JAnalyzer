package analyzer.qualitas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeSet;

import analyzer.dataTable.DataTableManager;
import util.Debug;
import util.SourceFilePackingManager;
import util.SystemVersionComparator;

/**
 * @author Zhou Xiaocong
 * @since 2015年9月17日
 * @version 1.0
 */
public class QualitasPackingManager {

	public static void main(String[] args) {
//		compareFileList("netbeans", "netbeans-6.9.1");

//		testCopySingleSystem();
		
		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
//			if (!systemName.equals("ant")) continue;
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			for (int j = 0; j < versions.length; j++) {
//				renameJavaFilesNotInFileList(systemName, versions[j]);
				System.out.println("Packing system " + versions[j]);
				deleteDirectoryWithoutJavaFiles(systemName, versions[j]);
			}
		}
		
	}
	
	public static void compareFileList(String systemName, String version) {
		String systemPath = QualitasPathsManager.getSystemPath(systemName, version);
		String packingFileListName = systemPath + "filelist.txt"; 
		String errorFileListName = systemPath + "error.txt"; 
		String originalFileListName = QualitasPathsManager.getSystemPath(systemName, version, QualitasPathsManager.defaultOriginalPath) + "filelist.txt";
		String reportFile = systemPath + "compare-filelist.txt";
		
		DataTableManager packingTable = new DataTableManager("packing");
		DataTableManager errorTable = new DataTableManager("error");
		DataTableManager originalTable = new DataTableManager("oringinal");
		DataTableManager reportTable = new DataTableManager("report");
		String[] columnNames = {"File", "Note", "Match"};
		String[] tableColumn = {"File"};
		
		packingTable.setColumnNames(tableColumn);
		originalTable.setColumnNames(tableColumn);
		reportTable.setColumnNames(columnNames);
		
		try {
			packingTable.read(packingFileListName, false);
			originalTable.read(originalFileListName, false);
			errorTable.read(errorFileListName, true);
		} catch (Exception exc) {
			exc.printStackTrace();
			return;
		}
		
		int packingFileNumber = packingTable.getLineNumber();
		for (int i = 0; i < packingFileNumber; i++) {
			String[] lineArray = new String[3];
			lineArray[0] = packingTable.getCellValueAsString(i, 0);
			lineArray[1] = "packing";
			lineArray[2] = "0";
			reportTable.appendLine(lineArray);
		}
		int errorFileNumber = errorTable.getLineNumber();
		for (int i = 0; i < errorFileNumber; i++) {
			String[] lineArray = new String[3];
			lineArray[0] = errorTable.getCellValueAsString(i, 0);
			lineArray[1] = "error";
			lineArray[2] = "0";
			reportTable.appendLine(lineArray);
		}
		int originalFileNumber = originalTable.getLineNumber();
		for (int i = 0; i < originalFileNumber; i++) {
			String originalFileName = originalTable.getCellValueAsString(i, 0);
			boolean matched = false;
			for (int j = 0; j < packingFileNumber+errorFileNumber; j++) {
				String packingFileName = reportTable.getCellValueAsString(j, 0);
				String noteInReport = reportTable.getCellValueAsString(j,  2); 
				packingFileName = packingFileName.replace('\\',  '/');
				int oldMatch = reportTable.getCellValueAsInt(j, 2);
				int match = matchFileName(originalFileName, packingFileName);

				if (match > 0 && oldMatch > 0) System.out.println("Find two match, new " + match + ", old " + oldMatch + ", for packing file " + packingFileName + ", and original file " + originalFileName);
				else if (match > 1 && match > oldMatch) {
					if (!noteInReport.equals("error")) reportTable.setCellValue(j, "Note", "both");
					reportTable.setCellValue(j, "Match", match);
					matched = true;
					break;
				}
			}
			if (!matched) {
				String[] lineArray = new String[3];
				lineArray[0] = originalFileName;
				lineArray[1] = "original";
				lineArray[2] = "0";
				reportTable.appendLine(lineArray);
				System.out.println("\tCan not match " + originalFileName + " to any files!");
			}
		}
		try {
			reportTable.write(reportFile);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	public static int matchFileName(String one, String two) {
		String[] oneArray = one.split("/");
		String[] twoArray = two.split("/");

		int oneIndex = oneArray.length-1;
		int twoIndex = twoArray.length-1;
		int count = 0;
		while (oneIndex >= 0 && twoIndex >= 0) {
			if (!oneArray[oneIndex].equals(twoArray[twoIndex])) break;
			count++;
			oneIndex--;
			twoIndex--;
		}
		if (count == oneArray.length || count == twoArray.length) return 100;
		return count;
	}

	public static void deleteDirectoryWithoutJavaFiles(String systemName, String version) {
		String systemPath = QualitasPathsManager.getSystemPath(systemName, version);
		File startPath = new File(systemPath);
		deleteDirectoryWithoutJavaFiles(startPath);
	}
	
	public static void deleteDirectoryWithoutJavaFiles(File startPath) {
		if (!startPath.exists()) return;
		if (startPath.isFile()) {
			if (startPath.getName().endsWith(".javb")) {
				System.out.println("Delete file: " + startPath.getPath());
				startPath.delete();
			}
			return;
		}
		
		File[] contents = startPath.listFiles(); 
		if (contents != null) {
			for (int i = 0; i < contents.length; i++) {
				if (contents[i].isFile()) {
					if (contents[i].getName().endsWith(".javb")) {
						System.out.println("Delete file: " + contents[i].getPath());
						contents[i].delete();
					}
				} else if (contents[i].isDirectory()) deleteDirectoryWithoutJavaFiles(contents[i]);
			}
			contents = startPath.listFiles();
		}
		if (contents == null) {
			startPath.delete();
		} else if (contents.length <= 0) {
			startPath.delete();
		}
	}
	
	/**
	 * Use the filelist.txt generated by QualitasMetadataManager.compareMetadataAndNameTable to rename the file which
	 * not in the filelist.txt. We regard these files as not the source code files of the system. They may be example, 
	 * test case, or demo of the system.
	 */
	public static void renameJavaFilesNotInFileList(String systemName, String version) {
		String systemPath = QualitasPathsManager.getSystemPath(systemName, version);
		String fileListName = systemPath + "filelist.txt"; 
		DataTableManager listTable = new DataTableManager("list");
		String[] columnNames = {"FileName"};
		listTable.setColumnNames(columnNames);
		try {
			listTable.read(fileListName, false);
		} catch (Exception exc) {
			exc.printStackTrace();
			return;
		}
		// Load the file list to a tree set so that we can find the file quickly!
		TreeSet<String> fileNameSet = new TreeSet<String>();
		int fileNumber = listTable.getLineNumber();
		for (int i = 0; i < fileNumber; i++) {
			String fileName = listTable.getCellValueAsString(i, "FileName");
			fileNameSet.add(fileName);
		}
		File startPath = new File(systemPath);
		renameJavaFilesNotInFileList(systemPath, startPath, fileNameSet);
	}
	
	/**
	 * Recursively scan all files in the path startPath and its sub-path
	 */
	public static void renameJavaFilesNotInFileList(String systemPath, File startPath, TreeSet<String> fileNameSet) {
		if (!startPath.exists()) return;
		if (startPath.isFile()) {
			renameAJavaFileNotInFileList(systemPath, startPath, fileNameSet);
			return;
		}
		
		File[] contents = startPath.listFiles(); 
		if (contents == null) return;
		if (contents.length <= 0) return;
		for (int i = 0; i < contents.length; i++) {
			if (contents[i].isFile()) renameAJavaFileNotInFileList(systemPath, contents[i], fileNameSet);
			else if (contents[i].isDirectory()) renameJavaFilesNotInFileList(systemPath, contents[i], fileNameSet);
		}
	}
	
	/**
	 * Rename the file given by the parameter, if it is not in the fileNameSet. Note that the file name in fileNameSet
	 * is a unit file name which is used in source code location. We must transfer the path in file to unit file name as 
	 * in the class SourceCodeFileSet when we search the file in fileNameSet!
	 * 
	 * @see SourceCodeFileSet 
	 */
	public static void renameAJavaFileNotInFileList(String systemPath, File file, TreeSet<String> fileNameSet) {
		// Treat the path in file to unit file name (which is used in source code location) as in the class SourceCodeParser
		if (!file.getName().endsWith(".java")) return;
		String fullFileName = file.getPath();
		String unitFileName = fullFileName.replace(systemPath, "");
		if (!fileNameSet.contains(unitFileName)) {
			// The file given by the parameter is not in fileNameSet
			String newFileName = fullFileName.replace(".java", ".javb");
			File newFile = new File(newFileName);
			boolean renameDone = false;  
			try {
				renameDone = file.renameTo(newFile);
				System.out.println("Rename file " + fullFileName + " to " + newFileName);
			} catch (Exception exc) {
				exc.printStackTrace();
				renameDone = false;
			}
			if (!renameDone) {
				System.out.println("\tCan not rename file [" + fullFileName + "] to [" + newFileName + "]!");
				boolean deleteDone = file.delete();
				if (!deleteDone) System.out.println("\t And can not delete file [" + fullFileName + "]!");
				else System.out.println("\t File [" + fullFileName + "] has been deleted!");
				
//				throw new AssertionError("Can not rename file [" + fullFileName + "] to [" + newFileName + "]!");
			}
		} //else System.out.println("File " + fullFileName + " is in the file list!");
	}
	
	public static void findSpecialPaths(String systemName) {
		String systemPath = QualitasPathsManager.getSystemStartPath(systemName);
		
		String result = systemPath + "pathReport.txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = null;
		
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		try {
			String info = QualitasPathsManager.getDebugFile();
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		String[] versions = QualitasPathsManager.getSystemVersions(systemName);
		
		for (int index = 0; index < versions.length; index++) {
			String path = QualitasPathsManager.getSystemPath(systemName, versions[index]);
			System.out.println("Check path " + path);
			File startPath = new File(path);
			try {
				findSpecialPaths(startPath, writer);
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}

		writer.close();
	}
	
	public static void findSpecialPaths(File startPath, PrintWriter writer) throws IOException {
		if (!startPath.exists()) return;
		if (startPath.isFile()) return;

		if (isSpecialPath(startPath.getName())) {
			writer.println(startPath.getAbsolutePath());
		}
		
		File[] contents = startPath.listFiles(); 
		if (contents == null) return;
		if (contents.length <= 0) return;
		for (int i = 0; i < contents.length; i++) findSpecialPaths(contents[i], writer);
	}
	
	public static boolean isSpecialPath(String name) {
		if (name.contains("testcase") || name.contains("example") || name.contains("document")) return true;
		if (name.contains("Testcase") || name.contains("Example") || name.contains("Document")) return true;
		return false;
	}
	
	
	public static void testCopySingleSystem() {
		String systemPath = "C:\\Qualitas\\QualitasCorpus-20130901r\\Systems\\jre\\jre-1.6.0\\src\\";
		String destPath = "C:\\QualitasPacking\\recent\\jre\\jre-1.6.0\\";
		String betterPath = systemPath;
		
		try {
			betterPath = SourceFilePackingManager.betterCopySystemSourceFiles(systemPath, destPath);

			if (betterPath != null) {
				System.out.println("Find better path " + betterPath);
				// 拷贝之后我们进行比较，将比较的结果写在文件中！
				String result = "C:\\ZxcTemp\\result.txt";									// The generated report file
				PrintWriter out = new PrintWriter(new FileOutputStream(new File(result)));
				SystemVersionComparator.compareDirectories(betterPath, destPath, out);
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void testCopyMultipleSystem() {
		String systemName = "azureus";
		String sourceRootPath = "C:\\Qualitas\\QualitasCorpus-20130901e\\Systems\\";
		String[] versions = QualitasPathsManager.getSystemVersions(systemName, sourceRootPath);
		
		String systemPath = sourceRootPath + systemName + QualitasPathsManager.pathSeparator;
		String destPath = QualitasPathsManager.getSystemStartPath(systemName);
		
		String result = QualitasPathsManager.getTestingResultFile();
		PrintWriter writer = new PrintWriter(System.out);
		try {
			writer = new PrintWriter(new FileOutputStream(result));
		} catch (FileNotFoundException exce) {
			exce.printStackTrace();
		}

		for (int i = 0; i < versions.length; i++) {
			String sourceSystem = systemPath + versions[i] + QualitasPathsManager.pathSeparator;
			String destSystem = destPath + versions[i] + QualitasPathsManager.pathSeparator;
			if (!hasJavaSourceCodeFile(destSystem)) {
				try {
					SourceFilePackingManager.betterCopySystemSourceFiles(sourceSystem, destSystem);
					if (!hasJavaSourceCodeFile(destSystem)) {
						// Copy is failed?
						writer.println("Can not copy any Java files from [" + sourceSystem + "] to [" + destSystem + "]!");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		writer.close();
	}
	
	/**
	 * Check if there are some files or sub-directory in a path
	 */
	public static boolean hasContents(String path) {
		File file = new File(path);
		if (!file.exists()) return false;
		if (file.isFile()) return true;
		File[] contents = file.listFiles();
		if (contents == null) return false;
		if (contents.length <= 0) return false;
		return true;
	}
	
	/**
	 * Check if there are some Java files in a path or its sub-path
	 */
	public static boolean hasJavaSourceCodeFile(File path) {
		if (!path.exists()) return false;
		if (path.isFile() && path.getName().endsWith(".java")) return true;
		File[] contents = path.listFiles();
		if (contents == null) return false;
		if (contents.length <= 0) return false;
		for (int i = 0; i < contents.length; i++) {
			if (hasJavaSourceCodeFile(contents[i])) return true;
		}
		return false;
	}

	/**
	 * Check if there are some Java files in a path or its sub-path
	 */
	public static boolean hasJavaSourceCodeFile(String path) {
		return hasJavaSourceCodeFile(new File(path));
	}
}
