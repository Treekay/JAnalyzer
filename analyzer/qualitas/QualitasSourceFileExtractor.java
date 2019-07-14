package analyzer.qualitas;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Extract source files in Qualitas, since the source files are compressed in the original Qualitas data set
 * @author xiaocong
 * @since 2015/09/17
 *
 */
public class QualitasSourceFileExtractor {

	public static void main(String[] args) {
		extractQualitasSystem();
	}
	
	public static void extractQualitasSystem() {
		String rootPath = "C:\\Qualitas\\QualitasCorpus-20130901e\\Systems\\";
		String systemName = "azureus";
		String result = QualitasPathsManager.getTestingResultFile();			// The generated report file
		
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(new File(result)));
			extractArchiveFilesInCompressedDirectory(rootPath, systemName, out);
			extractArchiveFilesInSrcDirectory(rootPath, systemName, out);
			out.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	public static void extractArchiveFilesInCompressedDirectory(String rootPath, String systemName, PrintWriter writer) {
		String archiveDir = "compressed";
		String srcDir = "src";
		
		String[] versions = QualitasPathsManager.getSystemVersions(systemName, rootPath);
		for (int i = 0; i < versions.length; i++) {
			String destPath = rootPath + systemName + "\\" + versions[i] + "\\" + srcDir;
			if (QualitasPackingManager.hasContents(destPath)) {
				System.out.println("The path [" + destPath + "] has contents yet!");
				writer.println("The path [" + destPath + "] has contents yet!");
				continue;
			}
			
			String archivePath = rootPath + systemName + "\\" + versions[i] + "\\" + archiveDir;
			System.out.println("Extract path: " + archivePath);
			File path = new File(archivePath);
			if (!path.exists()) {
				writer.println("The path [" + archivePath + "] does not exist!");
				continue;
			}
			File[] archives = path.listFiles(new SourceArchiveFileFilter());
			String archiveFile = null;
			if (archives.length == 1) {
				archiveFile = archivePath + "\\" + archives[0].getName();
				writer.println("Find the only archive file: " + archiveFile);
			}
			else {
				for (int j = 0; j < archives.length; j++) {
					String name = archives[j].getName();
					if (name.contains("src") || name.contains("source")) {
						archiveFile = archivePath + "\\" + archives[j].getName();
						writer.println("Find the src or source archive file: " + archiveFile);
						break;
					}
				}
			}
			if (archiveFile == null) {
				writer.println("Can not determine which archive file is source code archive in path [" + archivePath + "!]");
				continue;
			}
			
			System.out.println("Extract file [" + archiveFile + "] to path [" + destPath + "]...");
			extract(archiveFile, destPath);
			System.out.println("Extract end...!");
		}
	}

	public static void extractArchiveFilesInSrcDirectory(String rootPath, String systemName, PrintWriter writer) {
		String archiveDir = "src";
		
		String[] versions = QualitasPathsManager.getSystemVersions(systemName, rootPath);
		for (int i = 0; i < versions.length; i++) {
			String destPath = rootPath + systemName + "\\" + versions[i] + "\\" + archiveDir;
			if (!QualitasPackingManager.hasContents(destPath)) {
				System.out.println("The path [" + destPath + "] has not contents yet!");
				writer.println("The path [" + destPath + "] has not contents yet!");
				continue;
			}

			if (QualitasPackingManager.hasJavaSourceCodeFile(destPath)) {
				System.out.println("The path [" + destPath + "] has Java source code files yet!");
				writer.println("The path [" + destPath + "] has Java source code files yet!");
				continue;
			}
			
			String archivePath = rootPath + systemName + "\\" + versions[i] + "\\" + archiveDir;
			System.out.println("Extract path: " + archivePath);
			String archiveFile = findSourceArchiveFileInSrcDirectory(archivePath, false, writer);
			if (archiveFile == null) {
				writer.println("Can not determine which archive file is source code archive in path [" + archivePath + "!]");
				continue;
			}
			
			System.out.println("Extract file [" + archiveFile + "] to path [" + destPath + "]...");
			extract(archiveFile, destPath);
			System.out.println("Extract end...!");
		}
	}
	
	private static String findSourceArchiveFileInSrcDirectory(String startPath, boolean hasFoundArchiveFile, PrintWriter writer) {
		File path = new File(startPath);
		String archiveFile = null;
		File[] archives = path.listFiles(new SourceArchiveFileFilter());
		if (archives != null) {
			if (archives.length == 1 && hasFoundArchiveFile == false) {
				archiveFile = startPath + "\\" + archives[0].getName();
				writer.println("Find the only archive file: " + archiveFile);
			} else if (archives.length > 0) {
				hasFoundArchiveFile = true;
				for (int j = 0; j < archives.length; j++) {
					String name = archives[j].getName();
					if (name.contains("src") || name.contains("source")) {
						archiveFile = startPath + "\\" + archives[j].getName();
						writer.println("Find the src or source archive file: " + archiveFile);
						break;
					}
				}
			}
		}
		
		if (archiveFile != null) return archiveFile;
		File[] dirs = path.listFiles(new DirectoryFilter());
		if (dirs == null) return null;
		for (int i = 0; i < dirs.length; i++) {
			archiveFile = findSourceArchiveFileInSrcDirectory(dirs[i].getAbsolutePath(), hasFoundArchiveFile, writer);
			if (archiveFile != null) return archiveFile;
		}
		
		return archiveFile;
	}
	
	public static void testExtract() {
		String archiveFileOne = "C:\\ZxcWork\\ToolKit\\data\\ZxcTemp.rar";
		String destPathOne = "C:\\ZxcWork\\ToolKit\\data\\";
		String archiveFileTwo = "C:\\ZxcWork\\ToolKit\\data\\src.zip";
		String destPathTwo = "C:\\ZxcWork\\ToolKit\\data\\src\\";
		
		System.out.println("Extract two....");
		extract(archiveFileTwo, destPathTwo);
		System.out.println("Extract one....");
		extract(archiveFileOne, destPathOne);
		System.out.println("End...!");
	}
	
	public static void extract(String archiveFile, String destPath) {
		final String extractProgram = "C:\\software\\7zip\\7z.exe";
		Runtime rt = Runtime.getRuntime();

		String[] args = { extractProgram, "x", archiveFile, "-o"+destPath, "-y" };

		try {
			rt.exec(args);
//			Process p = rt.exec(args); 
//			System.out.println("Exit value: " + p.exitValue());
//			p.waitFor();
//		} catch (InterruptedException exc) {
//			System.err.println("Error: The execution of the external program 7z.exe was interrupted  for arguments " + args.toString() + " !");
//			exc.printStackTrace();
		} catch (IOException exc) {
			System.err.println("Error: Throws IOException during the execution of 7z.exe for arguments " + args.toString() + " !");
			exc.printStackTrace();
		}
	}

	static class SourceArchiveFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) return false;
			if (pathname.isFile()) {
				String name = pathname.getName();
				if (name.endsWith(".zip")) return true;
				if (name.endsWith(".ZIP")) return true;
				if (name.endsWith(".gz")) return true;
				if (name.endsWith(".tar")) return true;
				if (name.endsWith(".tgz")) return true;
				if (name.endsWith(".jar")) return true;
			}
			return false;
		}
	}
	
	
	static class DirectoryFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) return true;
			return false;
		}
	}
}
