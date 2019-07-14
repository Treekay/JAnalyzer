package gui.toolkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Current;

public class FileChooserAndOpener {
	String fileContents = null;
	String fileContentsWithLineNumber = null;

	JFrame parent = null;

	public FileChooserAndOpener(JFrame parent) {
		this.parent = parent;
	}

	/**
	 * 显示文件选择对话框，并让用户要打开的文件。缺省情况下打开 Java 源程序文件
	 * 
	 * @return 如果选择成功返回 true ，否则返回 false
	 */
	public boolean chooseFileName() {
		JFileChooser chooser = new JFileChooser();

		// 只打开 .java 文件
		chooser.setFileFilter(new FileNameExtensionFilter("java", "java"));
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// 从上一次打开的文件开始选择，如果上次没有选择，这时file == null，则从缺省目录开始选择
		if (Current.file != null)
			chooser.setCurrentDirectory(Current.file);
		else
			chooser.setCurrentDirectory(new File("."));

		int result = chooser.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			Current.file = chooser.getSelectedFile();
			if (Current.file.isDirectory()) {
				System.out.println("Directory:" + Current.file.getAbsolutePath());
			} else if (Current.file.isFile()) {
				System.out.println("File:" + Current.file.getAbsolutePath());
			}
			System.out.println(chooser.getSelectedFile().getName());
			return true;
		} else
			return false;
	}

	/**
	 * 选择文件成功之后装入文件内容。
	 * 
	 * @return 如何没有选择文件，或者在装入过程中发生 I/O 错误返回 false ，装入成功返回 true
	 */
	public boolean loadFile() {
		if (Current.file == null)
			return false;

		try {
			FileInputStream fileIn = new FileInputStream(Current.file);
			ProgressMonitorInputStream progressIn = new ProgressMonitorInputStream(parent,
					"正在读取文件 [" + Current.file.getName() + "]", fileIn);

			final Scanner in = new Scanner(progressIn);
			StringBuffer buffer = new StringBuffer();
			StringBuffer bufferWithLine = new StringBuffer();
			int lineCounter = 0;
			while (in.hasNextLine()) {
				lineCounter++;
				String line = in.nextLine();
				buffer.append(line + "\n");
				bufferWithLine.append(lineCounter + " " + line + "\n");
			}
			fileContents = buffer.toString();
			fileContentsWithLineNumber = bufferWithLine.toString();
			in.close();
			return true;
		} catch (IOException exc) {
			return false;
		}
	}

	public static void getFiles(File file) {
		if (file != null) {
			File[] f = file.listFiles();
			if (f != null) {
				for (int i = 0; i < f.length; i++) {
					getFiles(f[i]);
				}
			} else {
				System.out.println(file);
			}
		}
	}

	/**
	 * 返回已经选择的文件名
	 * 
	 * @return 如果没有选择文件则返回 null
	 */
	public String getFileName() {
		if (Current.file == null)
			return null;
		else
			return Current.file.getName();
	}

	/**
	 * 返回已经选择的文件名
	 * 
	 * @return 如果没有选择文件则返回 null
	 */
	public String getFileFullName() {
		if (Current.file == null)
			return null;
		else
			return Current.file.getPath();
	}

	/**
	 * 返回包含文件全部内容的字符串
	 * 
	 * @return 如果文件没有装载成功则返回 null
	 */
	public String getFileContents() {
		return fileContents;
	}

	/**
	 * 返回含有行号的文件内容
	 */

	public String getFileContentsWithLineNumber() {
		return fileContentsWithLineNumber;
	}
}
