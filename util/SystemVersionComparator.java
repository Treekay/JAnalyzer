package util;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.MyersDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;


/**
 * Compare the java files in two directories. The class compare the contents of two java files with the same 
 * file name in two different directories by the way of string simply matching, and report those java files in
 * a directory but not in the other directory.
 * 
 * @author Zhou Xiaocong
 * @since 2013/6/12
 * @version 1.0
 *
 */
public class SystemVersionComparator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dirOne = "C:\\ZxcWork\\ProgramAnalysis\\data\\result1.txt";			// One directory
		String dirTwo = "C:\\ZxcWork\\ProgramAnalysis\\data\\result.txt";	// Another directory
//		String dirOne = "C:\\ZxcWork\\ProgramAnalysis\\src\\";			// One directory
//		String dirTwo = "C:\\ZxcWork\\JAnalyzer\\src\\";	// Another directory
		String result = "C:\\ZxcTemp\\result.txt";									// The generated report file
		
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(new File(result)));
			File one = new File(dirOne);
			File two = new File(dirTwo);
			compareSourceFiles(one, two, out);
//			compareDirectories(dirOne, dirTwo, out);
			out.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public static void compareDirectories(String dirOne, String dirTwo, PrintWriter out) throws IOException {
		List<File> oneFiles = getAllJavaSourceFiles(dirOne);
		List<File> twoFiles = getAllJavaSourceFiles(dirTwo);
		
		List<FilePair> differentFiles = new ArrayList<FilePair>();
		List<File> filesOnlyInOne = new ArrayList<File>();
		List<File> filesOnlyInTwo = new ArrayList<File>();
		
		for (File oneFile : oneFiles) {
			String oneFileName = oneFile.getAbsolutePath().replace(dirOne, "");
			boolean findFile = false;
			for (File twoFile : twoFiles) {
				String twoFileName = twoFile.getAbsolutePath().replace(dirTwo, "");
				if (oneFileName.equals(twoFileName)) {
					findFile = true;
					out.println("Compare [" + oneFile.getAbsolutePath() + "] with [" + twoFile.getAbsolutePath() + "]:");
					if (compareSourceFiles(oneFile, twoFile, out)) {
						differentFiles.add(new FilePair(oneFile, twoFile));
					}
					break;
				}
			}
			
			if (!findFile) {
				out.println("Can not find file " + oneFile.getAbsolutePath() + " in directory " + dirTwo);
				out.println();

				filesOnlyInOne.add(oneFile);
			}
		}
		
		for (File twoFile : twoFiles) {
			String twoFileName = twoFile.getAbsolutePath().replace(dirTwo, "");
			boolean findFile = false;
			for (File oneFile : oneFiles) {
				String oneFileName = oneFile.getAbsolutePath().replace(dirOne, "");
				if (oneFileName.equals(twoFileName)) {
					findFile = true;
					break;
				}
			}
			
			if (!findFile) {
				out.println("Can not find file " + twoFile.getAbsolutePath() + " in directory " + dirOne);
				out.println();

				filesOnlyInTwo.add(twoFile);
			}
		}
		
		out.println();
		out.println("Summary: ");
		boolean hasDifferences = false;
		if (differentFiles.size() > 0) {
			hasDifferences = true;
			out.println("The following files are different in two directory: ");
			for (FilePair filePair : differentFiles) {
				out.println("\t" + filePair.one.getAbsolutePath());
				if (filePair.one.lastModified() > filePair.two.lastModified()) {
					out.println("\t\tFile in directory " + dirOne + " is newer!");
				} else if (filePair.one.lastModified() < filePair.two.lastModified()) {
					out.println("\t\tFile in directory " + dirTwo + " is newer!");
				}
				if (filePair.one.length() > filePair.two.length()) {
					out.println("\t\tFile in directory " + dirOne + " is larger!");
				} else if (filePair.one.length() < filePair.two.length()) {
					out.println("\t\tFile in directory " + dirTwo + " is larger!");
				}
			}
		}
		if ( filesOnlyInOne.size() > 0) {
			hasDifferences = true;
			out.println("The following files are only found directory " + dirOne + ": ");
			for (File file :  filesOnlyInOne) out.println("\t" + file.getAbsolutePath());
		}
		if ( filesOnlyInTwo.size() > 0) {
			hasDifferences = true;
			out.println("The following files are only found directory " + dirTwo + ": ");
			for (File file :  filesOnlyInTwo) out.println("\t" + file.getAbsolutePath());
		}
		if (!hasDifferences) out.println("\tThere is no difference between two directories!");
		out.flush();
	}

	public static boolean compareSourceFiles(File one, File two, PrintWriter out) throws IOException {
		RawText oneText = new RawText(one);
		RawText twoText = new RawText(two);
		EditList result = MyersDiff.INSTANCE.diff(RawTextComparator.DEFAULT, oneText, twoText);
		if (result.size() <= 0) return false; 
		else {
			// Print the first 5 edit scripts
			out.println("===========================================================================================================");
			for (int i = 0; i < result.size(); i++) {
				Edit edit = result.get(i);
				out.println("Difference #" + i + ": " + edit.toString());
				int oneBegin = edit.getBeginA();
				int oneEnd = edit.getEndA();
				String oneString = oneText.getString(oneBegin, oneEnd, true);
				int twoBegin = edit.getBeginB();
				int twoEnd = edit.getEndB();
				String twoString = twoText.getString(twoBegin, twoEnd, true);
				
				out.println(">>>>>>>>>>>>>>>> In file " + one.getAbsolutePath() + ":");
				out.println(oneString);
				out.println(">>>>>>>>>>>>>>>> In file " + two.getAbsolutePath() + ":");
				out.println(twoString);
				out.println("-------------------------------------------------------------------------------------------------------");
			}
//			if (result.size() > 5) {
//				out.print("More difference ");
//				for (int i = 5; i < result.size(); i++) {
//					Edit edit = result.get(i);
//					out.print(edit.toString() + " ");
//				}
//				out.println();
//			}
			out.println();
			return true;
		}
	}

		
/*	
	public static boolean compareSourceFiles(File one, File two, PrintWriter out) throws IOException {
		int lineCounter = 0;
		int unmatchCounter = 0;
		final int MAX_UNMATCH_NUMBER = 10;
		
		final Scanner inOne = new Scanner(one);
		final Scanner inTwo = new Scanner(two);
		while (inOne.hasNextLine() && inTwo.hasNextLine() && unmatchCounter <= MAX_UNMATCH_NUMBER) {
			lineCounter = lineCounter + 1;
			String lineOne = inOne.nextLine();
			String lineTwo = inTwo.nextLine();
			if (!lineOne.equals(lineTwo)) {
				unmatchCounter = unmatchCounter + 1;
				out.println("To be different at line " + lineCounter + ": ");
				out.println("    " + lineOne);
				out.println("    " + lineTwo);
			}
		}
		
		if (inOne.hasNextLine() && unmatchCounter <= MAX_UNMATCH_NUMBER) {
			lineCounter = lineCounter + 1;
			String lineOne = inOne.nextLine().trim();
			
			while (lineOne.equals("") && inOne.hasNextLine()) {
				lineCounter = lineCounter + 1;
				lineOne = inOne.nextLine().trim();
			}
			if (!lineOne.equals("")) {
				unmatchCounter = unmatchCounter + 1;
				out.println("To be different at line " + lineCounter + ": ");
				out.println("    " + lineOne);
				out.println("    " + "MEET THE END OF FILE!");
			}
		}
		
		if (inTwo.hasNextLine() && unmatchCounter <= MAX_UNMATCH_NUMBER) {
			lineCounter = lineCounter + 1;
			String lineTwo = inTwo.nextLine().trim();
			
			while (lineTwo.equals("") && inTwo.hasNextLine()) {
				lineCounter = lineCounter + 1;
				lineTwo = inTwo.nextLine().trim();
			}
			if (!lineTwo.equals("")) {
				unmatchCounter = unmatchCounter + 1;
				out.println("To be different at line " + lineCounter + ": ");
				out.println("    " + "MEET THE END OF FILE!");
				out.println("    " + lineTwo);
			}
		}
		
		boolean hasDifferences = true;
		if (unmatchCounter <= 0) {
			hasDifferences = false;
			out.println("The two files are the same!");
		} else if (unmatchCounter <= MAX_UNMATCH_NUMBER) {
			out.println("There are " + unmatchCounter + " difference(s)!");
		} else out.println("There are more than " + MAX_UNMATCH_NUMBER + " difference(s)");
		out.println();

		inOne.close();
		inTwo.close();
		out.flush();
		
		return hasDifferences;
	}
*/
	
	static class JavaSourceFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) return true;
			if (pathname.isFile() && pathname.getName().endsWith(".java")) return true;
			return false;
		}
	}

	/**
	 * Use JavaSourceFileFilter to get all java source file in the systemPath
	 */
	private static List<File> getAllJavaSourceFiles(String systemPath) {
		ArrayList<File> files = new ArrayList<File>();
		File dir = new File(systemPath);
		if (dir.isFile()) {
			if (dir.getName().endsWith(".java")) files.add(dir);
			return files;
		}
		
		FileFilter filter = new JavaSourceFileFilter();
		File[] temp = dir.listFiles(filter);
		if (temp != null) {
			for (int index = 0; index < temp.length; index++) {
				if (temp[index].isFile()) files.add(temp[index]);
				if (temp[index].isDirectory()) {
					List<File> tempResult = getAllJavaSourceFiles(temp[index].getAbsolutePath());
					for (File file : tempResult) files.add(file);
				}
			}
		}
		return files;
	}
}

class FilePair {
	File one;
	File two;
	
	public FilePair(File one, File two) {
		this.one = one;
		this.two = two;
	}
}

