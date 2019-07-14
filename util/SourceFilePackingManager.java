package util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 管理一个系统下的所有源文件，目的是要整理一个开源项目下的所有可用于分析的文件（目前只是.java源文件），支持将这些文件单列出来（单独拷贝到另外的目录），
 * 以及寻找更好的系统根目录文件（由于Java源文件的程序包对应目录，有时很多开源项目下有很深的子目录结构，但是实际上这些目录只有一个子目录，没有其他目录，
 * 例如JEdit中org.gjt.sp.jedit程序包，实际JEdit的src目录下只有org有Java文件，而org目录下只有gjt这个子目录，而gjt下只有sp这个子目录，sp
 * 下只有jedit这个子目录，如果系统目录从src开始算起，那么分析时每个程序包都带有"org.gjt.sp.jedit"这个串，非常浪费存储空间，但如果从最后的 jedit 
 * 开始算起，则对于我们的分析没有什么影响，足够区分不同的程序包和源文件（编译单元），但可节省许多分析时的存储空间）
 * 
 * @author Zhou Xiaocong
 * @version 1.0
 * @since 2015/09/01
 *
 */
public class SourceFilePackingManager {
	public static final String pathSeparator = "\\";
	
	private String systemId = null;			// 每个开源项目应该有一个的系统名称
	private String versionId = null;		// 系统的版本号。对于每个开源系统，系统名称+版本号 应该唯一
	private String path = null;				// 系统根目录
	
	private String betterPath = null;		// 这个类可以探查到的可以更好作为系统根目录的目录名
	
	// 用来标记和管理目录的源文件结构类对象的根
	private SourceFilesStructure pathTreeRoot = null; 

	public SourceFilePackingManager(String systemId, String versionId, String root) {
		this.systemId = systemId;
		this.versionId = versionId;
		this.path = root;
	}

	public String getSystemId() {
		return systemId;
	}

	public String getVersionId() {
		return versionId;
	}

	public String getRootPath() {
		return path;
	}

	public String getBetterRootPath() {
		return betterPath;
	}
	
	/**
	 * 提供一个工具方法，直接根据某个系统目录path，探查其底下是否有更好的，可作为系统根目录的目录。返回 null 表示给定的目录下没有
	 * 任何需要的源文件；否则如果没有更好的目录，则会返回 path
	 */
	public static String findBetterSystemPath(String systemPath) {
		// 创建一个临时用来探查目录的管理器对象
		SourceFilePackingManager manager = new SourceFilePackingManager("", "", systemPath);
		manager.buildSourceFilesStructure();
		manager.findBetterRootPath();
		return manager.getBetterRootPath();
	}
	
	/**
	 * 在探查给定系统源目录 sourceSystemPath 下是否有更好的目录betterPath之后，再将betterPath底下的所有源文件拷贝到目标目录 destPath
	 * 该方法返回找到的可能更好的系统目录，以便调用清楚是从哪个源文件目录进行拷贝的！
	 */
	public static String betterCopySystemSourceFiles(String sourceSystemPath, String destPath) throws IOException {
		String betterPath = findBetterSystemPath(sourceSystemPath);
		if (betterPath == null) return null;
		
		File start = new File(betterPath);
		if (start.isFile()) {
			if (!isSourceFile(start)) return betterPath;	// 不是所需要的源文件就不拷贝了！
			
			File dest = new File(destPath);
			if (dest.isFile()) copyBinaryFile(start, dest);
			else {
				String destFileName = destPath + start.getName();
				dest = new File(destFileName);
				copyBinaryFile(start, dest);
			}
			return betterPath;
		}
		
		File dest = new File(destPath);
		if (dest.isFile()) {
			throw new IOException("The destination [" + destPath + "] is not a path!");
		}
		if (!dest.exists()) dest.mkdirs();		// 目标目录不存在的话，创建该目录（及其可能需要创建的父目录）
		copyAllSourceFiles(betterPath, destPath);	// 将目录 betterPath下的所有源文件拷贝到目录 destPath
		return betterPath;
	}
	
	/**
	 *	在创建表示源文件结构的根节点之后，就可以探查是否有更好的系统根目录 
	 */
	public void findBetterRootPath() {
		betterPath = path;	// 从原来的 path 开始
		SourceFilesStructure node = pathTreeRoot;
		while (node != null) {
			if (!node.hasChild()) {
				// 当前节点没有儿子，而且当前节点没有所需要的源文件，表明这个系统目录下根本没有任何需要的源文件
				if (!node.hasSourceFile()) betterPath = null;
				
				System.out.println("Return null in !node.hasChild()!");
				return;	
			}
			ArrayList<SourceFilesStructure> children = node.getChildren();
			SourceFilesStructure hasSourceFileChild = null;
			for (int i = 0; i < children.size(); i++) {
				SourceFilesStructure child = children.get(i);
				if (child.hasSourceFile()) {
					if (hasSourceFileChild == null) hasSourceFileChild = child;
					else {
						System.out.println("Return [" + betterPath + "] in hasSourceFileChild != null!");
						return; // 当前节点有两个子目录都有需要的源文件，探查结束，结果在 betterPath 中
					}
				}
			}
			if (hasSourceFileChild == null) {
				// 当前节点的所有儿子都没有所需要的源文件，这也表明整个系统目录下根本没有任何需要的源文件
				betterPath = null;

				System.out.println("Return null in hasSourceFileChild == null!");
				return;
			} else {
				// 当前节点有且仅有一个儿子有所需要的源文件，注意我们约定系统目录要以 pathSeparator 结束
				betterPath = betterPath + hasSourceFileChild.getPathName() + pathSeparator;
				node = hasSourceFileChild;		// 走向这个儿子做进一步的探查
			}
		}
	}
	
	/**
	 * 以设置的 path 开始，调用内部的 buildSourceFilesStructure(File) 方法构建表示文件结构的树，并将结果记录
	 * 在 pathTreeRoot。注意，如果 path 对应单个文件，而且不是需要分析的源文件，则 pathTreeRoot 是 null。
	 */
	public void buildSourceFilesStructure() {
		File start = new File(path);

		if (start.isFile()) {
			// 系统目录 path 不是一个目录，而是单个文件，这时只有该文件是需要的源文件时， pathTreeRoot 才不是 null
			if (isSourceFile(start)) {
				// 只有在该文件是需要的源文件时才创建节点，使用文件简单名创建
				pathTreeRoot = new SourceFilesStructure(start.getName());				
				pathTreeRoot.setFlag(true);
			}  else pathTreeRoot = null;
			return;
		}
		// 注意上面的判断，以及避免了 buildSourceFilesStructure(File)注释中所说的第一种情况
		pathTreeRoot = buildSourceFilesStructure(start);
	}
	
	/**
	 * 以start 为起点，创建表示文件结构的树，可能有三种情况：
	 * (1) 如果 start 对应一个文件而非目录，则当这个文件是需要的源文件时，返回对应该单个文件的节点（根据下面的策略，这种情况应该
	 * 		只有当给出的根目录就是单个文件时才会出现），否则返回 null；
	 * (2) 如果 start 是一个目录，但它不再含有子目录，这时如果该目录下至少有一个需要的源文件，则返回的一个表示有需要源文件 （flag域为 true），
	 * 		的节点；如果该目录下没有任何需要的源文件，则返回一个表示没有需要源文件（flag域为false）的节点。返回的节点总是没有儿子（children域为 null）
	 * (3) 如果 start 是一个目录，其中至少含有一个子目录，则除了根据该目录下的文件设置 flag 域之外，还要根据子目录中的情况设置 flag 域，只要其中
	 * 		一个有需要的源文件，则flag设置为 true 。这时返回的节点总是会进一步创建儿子节点。
	 * 
	 */
	private SourceFilesStructure buildSourceFilesStructure(File start) {
		SourceFilesStructure node = null;
		
		if (start.isFile()) {
			// start不是一个目录，而是一个文件
			if (isSourceFile(start)) {
				// 只有在该文件是需要的源文件时才创建对应的节点，使用文件简单名创建
				node = new SourceFilesStructure(start.getName());				
				node.setFlag(true);
			}  // 否则不创建
			return node;
		}
		
		node = new SourceFilesStructure(start.getName());	// 使用简单名创建节点
		File[] filesInPath = start.listFiles(new JavaSourceFileFilter());
		if (filesInPath == null) return node;		// 这个目录下没有任何需要的源文件，也没有子目录
		
		for (int index = 0; index < filesInPath.length; index++) {
			File current = filesInPath[index];
			if (current.isFile()) {
				// 对于单个文件我们不再递归调用本方法去构建进一步的源文件目录结构
				if (isSourceFile(current)) node.setFlag(true);
			} else {
				// 递归调用本方法创建表示子目录的源文件结构节点
				SourceFilesStructure child = buildSourceFilesStructure(current);
				if (child != null) {
					node.addChild(child);
					if (child.hasSourceFile()) node.setFlag(true);
				}
			}
		}
		
		return node;
	}
	
	static class JavaSourceFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) return true;
			if (isSourceFile(pathname)) return true;
			return false;
		}
	}
	
	private static boolean isSourceFile(File file) {
		if (file.isFile() && file.getName().endsWith(".java")) return true;
		return false;
	}
	
	/**
	 * 使用面向字节的文件流拷贝二进制文件
	 */
	public static void copyBinaryFile(File inFile, File outFile) throws IOException {
		System.out.println("Copy file [" + inFile.getAbsolutePath() + "] to [" + outFile.getAbsolutePath() + "] ....");
		
		FileInputStream in = new FileInputStream(inFile);
		FileOutputStream out = new FileOutputStream(outFile);
		
		final int bufferLength = 4096;
		byte[] buffer = new byte[bufferLength];
		while (true) {
			int readBytes = in.read(buffer);
			if (readBytes == -1) break;
			out.write(buffer, 0, readBytes);
		}
		in.close();
		out.close();

		System.out.println("Copy end ....");
	}

	/**
	 * 拷贝某个目录下的所有源文件到目标目录
	 */
	private static boolean copyAllSourceFiles(String sourcePath, String destPath) throws IOException {
		boolean indeedCopyFile = false;
		
		File source = new File(sourcePath);
		File[] sourceFiles = source.listFiles(new JavaSourceFileFilter());
		if (sourceFiles == null) return indeedCopyFile;
		
		for (int i = 0; i < sourceFiles.length; i++) {
			File sourceFile = sourceFiles[i];
			if (sourceFile.isFile()) {
				String sourceName = sourcePath + sourceFile.getName();
				String destName = destPath + sourceFile.getName();
				copyBinaryFile(new File(sourceName), new File(destName));
				indeedCopyFile = true;
			} else {
				String sourceChildPath = sourcePath + sourceFile.getName() + pathSeparator;
				String destChildPath = destPath +  sourceFile.getName() + pathSeparator;
				File dest = new File(destChildPath);
				if (!dest.exists()) dest.mkdir();
				
				boolean childCopyFile = copyAllSourceFiles(sourceChildPath, destChildPath);
				if (!childCopyFile) {
					// 该子目录实际上没有拷贝任何源文件，删除该子目录，避免留下太多空子目录！
					dest.delete();
				} else indeedCopyFile = true;
			}
		}
		return indeedCopyFile;
	}

	
}

/**
 * 用来标记源文件目录结构中是否含有需要的源文件
 * 
 * @author Zhou Xiaocong
 * @version 1.0
 * @since 2015/09/01
 *
 */
class SourceFilesStructure {
	String pathName = null;								// 对应的目录名（简单名）
	boolean flag = false;								// 标记对应的目录是否有需要的源文件
	ArrayList<SourceFilesStructure> children = null;	// 子目录的结构
	
	public SourceFilesStructure(String pathName) {
		this.pathName = pathName;
		children = new ArrayList<SourceFilesStructure>();
	}

	public String getPathName() {
		return pathName;
	}

	public boolean hasSourceFile() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	public ArrayList<SourceFilesStructure> getChildren() {
		return children;
	}
	
	public void addChild(SourceFilesStructure child) {
		children.add(child);
	}
	
	public boolean hasChild() {
		if (children == null) return false;
		if (children.size() <= 0) return false;
		return true;
	}
}
