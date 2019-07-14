package sourceCodeAST;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * An object of SourceCodeFileSet hold the map from a set of unit names to their source code files. All source code files is under
 * the start path of the set.  
 * @author Zhou Xiaocong
 * @since 2016Äê9ÔÂ28ÈÕ
 * @version 1.0
 *
 */
public class SourceCodeFileSet implements Iterable<SourceCodeFile> {
	public final String pathSeparator = "\\";

	private String startPath = null;
	private String rootFile = null;
	private TreeMap<String, SourceCodeFile> fileMap = null;

	/**
	 * @param rootFile: it should be either the full name of a Java source code file or a start path of a source code file set. 
	 * If you give the full name of a Java source code file, it means there only one file in the source code file set, and its 
	 * start path is its full name minus its simple name. If you give the start path of the source code file set, you should end 
	 * with the path separator (.e. "\\").  
	 */
	public SourceCodeFileSet(String rootFile) {
		this.rootFile = rootFile;
		File dir = new File(rootFile);
		if (dir.isFile()) {
			if (dir.getParent() == null)
				this.startPath = "";
			else
				this.startPath = dir.getParent() + pathSeparator;
		} else
			this.startPath = rootFile;

		// Load all java files to the array allInfo.
		createFileMap();
	}

	/**
	 * Return the iterator of this source code file set. Since this class implements the interface Iterable<SourceCodeFile>, you
	 * can use for-each loop to traverse all souce code file in the set, for example
	 * <p>
	 * for (SourceCodeFile codeFile : codeFileSet) {
	 * 		...
	 * }</p> 
	 */
	public SourceCodeFileSetIterator iterator() {
		SourceCodeFileSetIterator iterator = new SourceCodeFileSetIterator(this);
		return iterator;
	}

	public long getTotalLineNumbersOfAllFiles() {
		long result = 0;

		Collection<SourceCodeFile> fileSet = fileMap.values();
		for (SourceCodeFile sourceCodeFile : fileSet)
			result += sourceCodeFile.getTotalLines();

		return result;
	}

	public long getTotalSpacesOfAllFiles() {
		long result = 0;
		Collection<SourceCodeFile> fileSet = fileMap.values();
		for (SourceCodeFile sourceCodeFile : fileSet)
			result += sourceCodeFile.getTotalSpaces();

		return result;
	}

	public int getFileNumber() {
		return fileMap.size();
	}

	/**
	 * If we have a source code file object, we can use this method to get its unit name in this source code file set.
	 * We can not get unit name from the source code file object directly, since it do not have the information about the 
	 * start path of the source file code set!!
	 */
	public String getFileUnitName(SourceCodeFile sourceCodeFile) {
		return getFileUnitName(sourceCodeFile.getFileFullName());
	}

	/**
	 * Get the source code file of the file given by the file unit name
	 */
	public SourceCodeFile findSourceCodeFileByFileUnitName(String fileUnitName) {
		return fileMap.get(fileUnitName);
	}

	/**
	 * Get the content of the source code file given by the file unit name
	 */
	public String findSourceCodeFileContentByFileUnitName(String fileUnitName) {
		SourceCodeFile sourceCodeFile = fileMap.get(fileUnitName);
		return sourceCodeFile.getFileContent();
	}

	/**
	 * Get AST root node of the source code file given by the file unit name
	 */
	public CompilationUnit findSourceCodeFileASTRootByFileUnitName(String fileUnitName) {
		SourceCodeFile sourceCodeFile = fileMap.get(fileUnitName);
		if (sourceCodeFile.hasCreatedAST())
			return sourceCodeFile.getASTRoot();
		else
			return null;
	}

	/**
	 * Release memory of all file contents
	 */
	public void releaseAllFileContents() {
		Collection<SourceCodeFile> fileSet = fileMap.values();
		for (SourceCodeFile sourceCodeFile : fileSet)
			sourceCodeFile.releaseFileContent();
	}

	/**
	 * Release memory of all AST
	 */
	public void releaseAllASTs() {
		Collection<SourceCodeFile> fileSet = fileMap.values();
		for (SourceCodeFile sourceCodeFile : fileSet)
			sourceCodeFile.releaseAST();
	}

	/**
	 * Release memory of the file content of a source code file given by its unit name.
	 */
	public void releaseFileContent(String fileUnitName) {
		SourceCodeFile codeFile = fileMap.get(fileUnitName);
		if (codeFile != null)
			codeFile.releaseFileContent();
	}

	/**
	 * Release memory of the AST of a source code file given by its unit name!
	 */
	public void releaseAST(String fileUnitName) {
		SourceCodeFile codeFile = fileMap.get(fileUnitName);
		if (codeFile != null)
			codeFile.releaseFileContent();
	}

	public String getStartPath() {
		return startPath;
	}

	class JavaSourceFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory())
				return true;
			if (pathname.isFile() && pathname.getName().endsWith(".java"))
				return true;
			return false;
		}
	}

	/**
	 * Load all java files in the path given in the constructor method
	 */
	private void createFileMap() {
		ArrayList<File> files = getAllJavaSourceFiles(rootFile);
		fileMap = new TreeMap<String, SourceCodeFile>();

		for (File file : files) {
			SourceCodeFile sourceCodeFile = new SourceCodeFile(file);
			String fileUnitName = getFileUnitName(sourceCodeFile.getFileFullName());
			sourceCodeFile.setFileUnitName(fileUnitName);
			fileMap.put(fileUnitName, sourceCodeFile);
		}
	}

	/**
	 * Use JavaSourceFileFilter to get all java source file under the startPath
	 */
	private ArrayList<File> getAllJavaSourceFiles(String rootPath) {
		ArrayList<File> files = new ArrayList<File>();
		File dir = new File(rootPath);
		if (dir.isFile()) {
			if (dir.getName().endsWith(".java"))
				files.add(dir);
			return files;
		}

		FileFilter filter = new JavaSourceFileFilter();
		File[] temp = dir.listFiles(filter);
		if (temp != null) {
			for (int index = 0; index < temp.length; index++) {
				if (temp[index].isFile())
					files.add(temp[index]);
				if (temp[index].isDirectory()) {
					List<File> tempResult = getAllJavaSourceFiles(temp[index]
							.getAbsolutePath());
					for (File file : tempResult)
						files.add(file);
				}
			}
		}
		return files;
	}

	/**
	 * Transform the full name of a source code file to its unit name, i.e. replace the startPath in its full name to
	 * empty string. Note that we only need unit name to distinguish each file in the source code file set!
	 */
	private String getFileUnitName(String fileFullName) {
		return fileFullName.replace(startPath, "");
	}

	Map<String, SourceCodeFile> getSourceCodeFileMap() {
		return fileMap;
	}
}
