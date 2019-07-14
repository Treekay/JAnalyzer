package gui.toolkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
	 * ��ʾ�ļ�ѡ��Ի��򣬲����û�Ҫ�򿪵��ļ���ȱʡ����´� Java Դ�����ļ�
	 * 
	 * @return ���ѡ��ɹ����� true �����򷵻� false
	 */
	public boolean chooseFileName() {
		JFileChooser chooser = new JFileChooser();

		// ֻ�� .java �ļ�
		chooser.setFileFilter(new FileNameExtensionFilter("java", "java"));
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// ����һ�δ򿪵��ļ���ʼѡ������ϴ�û��ѡ����ʱfile == null�����ȱʡĿ¼��ʼѡ��
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
			return true;
		} else
			return false;
	}

	/**
	 * ѡ���ļ��ɹ�֮��װ���ļ����ݡ�
	 * 
	 * @return ���û��ѡ���ļ���������װ������з��� I/O ���󷵻� false ��װ��ɹ����� true
	 */
	public boolean loadFile() {
		if (Current.file == null)
			return false;

		try {
			FileInputStream fileIn = new FileInputStream(Current.file);
			ProgressMonitorInputStream progressIn = new ProgressMonitorInputStream(parent,
					"���ڶ�ȡ�ļ� [" + Current.file.getName() + "]", fileIn);

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

	/**
	 * �����Ѿ�ѡ����ļ���
	 * 
	 * @return ���û��ѡ���ļ��򷵻� null
	 */
	public String getFileName() {
		if (Current.file == null)
			return null;
		else
			return Current.file.getName();
	}

	/**
	 * �����Ѿ�ѡ����ļ���
	 * 
	 * @return ���û��ѡ���ļ��򷵻� null
	 */
	public String getFileFullName() {
		if (Current.file == null)
			return null;
		else
			return Current.file.getPath();
	}

	/**
	 * ���ذ����ļ�ȫ�����ݵ��ַ���
	 * 
	 * @return ����ļ�û��װ�سɹ��򷵻� null
	 */
	public String getFileContents() {
		return fileContents;
	}

	/**
	 * ���غ����кŵ��ļ�����
	 */

	public String getFileContentsWithLineNumber() {
		return fileContentsWithLineNumber;
	}
}
