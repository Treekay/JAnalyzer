package gui.astViewer;

import gui.toolkit.FileChooserAndOpener;
import gui.toolkit.MainFrame;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.*;

import org.eclipse.jdt.core.dom.CompilationUnit;
import nameTable.NameTableManager;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.TypeReference;
import nameTable.visitor.NameDefinitionPrinter;
import nameTable.visitor.NameDefinitionVisitor;

import java.util.List;

public class TestASTViewer {
	// public static void main(String[] args) {
	// // ��ʼ�������򣬵�����λ�úͿ��ȣ�ʹ����ʾ�����İ�ť��Ư��
	// int widthSpace = 15;
	// int heightSpace = 100;

	// MainFrame.init("Java���������ͼչʾ����", MainFrame.screenWidth - widthSpace,
	// MainFrame.screenHeight - heightSpace,
	// 0, 0, "system");

	// DemoMenuCreator demo = new DemoMenuCreator(MainFrame.getContentPane(),
	// MainFrame.getMainFrame());
	// // ������ʾ�õĲ˵�����������˵����������������������������������
	// demo.createMenu();
	// // ���������򣬲�������ʾ
	// MainFrame.start();
	// }
}

class DemoMenuCreator {
	private Container place; // ������ʾ���������
	private JFrame topLevelFrame; // ���ò˵��Ķ�������
	private JTabbedPane tabbedPane;
	private int astTabIndex;
	private JTextArea sourceText; // ���ڷ���Դ�����ļ�
	private JTextArea astText; // ���ڷ��ó����﷨��
	private JTextArea cfgText; // ���ڷ��ó��������ͼ
	private int cfgTabIndex;
	private JTextArea nameText; // For name table
	private int nameTabIndex;

	private final String OPEN_COMMAND = "open";
	private final String ASTPARSER_COMMAND = "astparser";
	private final String ABOUT_COMMAND = "about";
	private final String EXIT_COMMAND = "exit";
	private final String CONCISEAST_COMMAND = "consiceast";
	private final String CREATE_CFG_COMMAND = "createCFG";
	private final String CREATE_NAME_TABLE_COMMAND = "CreateNameTable";

	private FileChooserAndOpener fileOpener = null;
	private CompilationUnit astRoot = null;
	private NameTableManager tableManager = null;

	// public DemoMenuCreator(Container place, JFrame topLevelFrame) {
	// this.place = place;
	// this.topLevelFrame = topLevelFrame;
	// fileOpener = new FileChooserAndOpener(topLevelFrame);
	// }

	// ����������ʾ�����
	public void createMenu() {
		JSplitPane hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		hSplitPane.setDividerLocation(MainFrame.screenWidth / 2);
		place.add(hSplitPane);

		sourceText = new JTextArea();
		sourceText.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(sourceText);
		hSplitPane.setLeftComponent(scrollPane);

		tabbedPane = new JTabbedPane();
		hSplitPane.setRightComponent(tabbedPane);

		astText = new JTextArea();
		astText.setEditable(false);
		scrollPane = new JScrollPane(astText);
		tabbedPane.addTab("�����﷨��", scrollPane);
		astTabIndex = 0;

		cfgText = new JTextArea();
		cfgText.setEditable(false);
		scrollPane = new JScrollPane(cfgText);
		tabbedPane.addTab("������ͼ", scrollPane);
		cfgTabIndex = 1;

		nameText = new JTextArea();
		nameText.setEditable(false);
		scrollPane = new JScrollPane(nameText);
		tabbedPane.addTab("���ֱ�", scrollPane);
		nameTabIndex = 2;

		hSplitPane.resetToPreferredSizes();

		// �����˵��ļ�����
		MenuListener menuListener = new MenuListener();
		// �����˵���
		JMenuBar menuBar = new JMenuBar();
		topLevelFrame.setJMenuBar(menuBar); // �����ڶ�������

		// ������һ�����˵���
		JMenu menu = new JMenu("�ļ�(F)");
		menu.setMnemonic(KeyEvent.VK_F); // �����ַ���FΪ��ݼ�
		menuBar.add(menu); // ���뵽�˵���
		// ���õ�һ�����˵���ĵ�һ���Ӳ˵���
		JMenuItem menuItem = new JMenuItem("��(O)", null);
		menuItem.setMnemonic(KeyEvent.VK_O);
		// ���ô˲˵���ļ��ټ�ΪCtrl+O
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(OPEN_COMMAND); // ��������Ϊ�˳�����
		menu.add(menuItem); // ���뵽��һ�����˵���

		// ���õ�һ�����˵���ĵ�һ���Ӳ˵���
		menuItem = new JMenuItem("�﷨��(A)", null);
		menuItem.setMnemonic(KeyEvent.VK_A);
		// ���ô˲˵���ļ��ټ�ΪCtrl+A
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(ASTPARSER_COMMAND); // ��������
		menu.add(menuItem); // ���뵽��һ�����˵���

		// ���õ�һ�����˵���ĵڶ����Ӳ˵���
		menuItem = new JMenuItem("�����﷨��(C)", null);
		menuItem.setMnemonic(KeyEvent.VK_C);
		// ���ô˲˵���ļ��ټ�ΪCtrl+C
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(CONCISEAST_COMMAND); // ��������
		menu.add(menuItem); // ���뵽��һ�����˵���

		// ���õ�һ�����˵���ĵ������Ӳ˵���
		menuItem = new JMenuItem("������ͼ(G)", null);
		menuItem.setMnemonic(KeyEvent.VK_G);
		// ���ô˲˵���ļ��ټ�ΪCtrl+G
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(CREATE_CFG_COMMAND); // ��������Ϊ�˳�����
		menu.add(menuItem); // ���뵽�ڶ������˵���

		// ���õ�һ�����˵���ĵ������Ӳ˵���
		menuItem = new JMenuItem("���ֱ�(N)", null);
		menuItem.setMnemonic(KeyEvent.VK_N);
		// ���ô˲˵���ļ��ټ�ΪCtrl+N
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(CREATE_NAME_TABLE_COMMAND); // ��������
		menu.add(menuItem); // ���뵽��һ�����˵���

		menu.addSeparator();
		// Ϊ��һ�����˵��������һ���˵���
		menuItem = new JMenuItem("�˳�");
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(EXIT_COMMAND); // ��������Ϊ�˳�����
		menu.add(menuItem);
		// �ڶ������˵���.
		menu = new JMenu("����(H)");
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);
		menuItem = new JMenuItem("����...");
		menuItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(ABOUT_COMMAND);
		menu.add(menuItem);
	}

	// �����˵���İ��¶���
	private class MenuListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JMenuItem source = (JMenuItem) (e.getSource());
			String command = source.getActionCommand();
			if (command.equals(ABOUT_COMMAND)) {
				// ����һ������ʾһЩ��Ϣ
				JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "Java��������﷨��չʾ", "����",
						JOptionPane.WARNING_MESSAGE);
			} else if (command.equals(EXIT_COMMAND))
				System.exit(1); // �˳���������
			else if (command.equals(OPEN_COMMAND)) {
				if (fileOpener.chooseFileName() == true && fileOpener.loadFile() == true) {
					sourceText.setText(fileOpener.getFileContentsWithLineNumber());
					topLevelFrame.setTitle(fileOpener.getFileName());
					astText.setText("");
					cfgText.setText("");
					nameText.setText("");

					astRoot = null; // For regenerate the ast for the new file!
					tableManager = null;
				}
			} else if (command.equals(ASTPARSER_COMMAND)) {
				String fileContents = fileOpener.getFileContents();
				if (fileContents == null) {
					fileOpener.chooseFileName();
					fileOpener.loadFile();
					fileContents = fileOpener.getFileContents();
					topLevelFrame.setTitle(fileOpener.getFileName());
					cfgText.setText("");
					nameText.setText("");

					astRoot = null; // For regenerate the ast for the new file!
					tableManager = null;
				}
				sourceText.setText(fileOpener.getFileContentsWithLineNumber());

				SimpleASTViewer viewer = new SimpleASTViewer(topLevelFrame, fileContents);
				viewer.parseSourceCode();
				String errorMessage = viewer.getParseErrorMessage();
				if (errorMessage != null) {
					JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "������ִ���\n" + errorMessage, "��ʾ",
							JOptionPane.WARNING_MESSAGE);
				}
				if (viewer.hasParserError())
					astRoot = null;
				else
					astRoot = viewer.getASTRoot();
				astText.setText(viewer.getASTViewerText());
				tabbedPane.setSelectedIndex(astTabIndex);
			} else if (command.equals(CONCISEAST_COMMAND)) {
				String fileContents = fileOpener.getFileContents();
				if (fileContents == null) {
					fileOpener.chooseFileName();
					fileOpener.loadFile();
					fileContents = fileOpener.getFileContents();
					topLevelFrame.setTitle(fileOpener.getFileName());
					cfgText.setText("");
					nameText.setText("");

					astRoot = null; // For regenerate the ast for the new file!
					tableManager = null;
				}
				sourceText.setText(fileOpener.getFileContentsWithLineNumber());

				ConciseASTViewer viewer = new ConciseASTViewer(topLevelFrame, fileContents);
				viewer.parseSourceCode();
				String errorMessage = viewer.getParseErrorMessage();
				if (errorMessage != null) {
					JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "������ִ���\n" + errorMessage, "��ʾ",
							JOptionPane.WARNING_MESSAGE);
				}
				if (viewer.hasParserError())
					astRoot = null;
				else
					astRoot = viewer.getASTRoot();
				astText.setText(viewer.getASTViewerText());
				tabbedPane.setSelectedIndex(astTabIndex);
			} else if (command.equals(CREATE_CFG_COMMAND)) {
				String fileContents = fileOpener.getFileContents();
				if (fileContents == null) {
					fileOpener.chooseFileName();
					fileOpener.loadFile();
					fileContents = fileOpener.getFileContents();
					topLevelFrame.setTitle(fileOpener.getFileName());

					astRoot = null; // For regenerate the ast for the new file!
					tableManager = null;
					nameText.setText("");
				}
				sourceText.setText(fileOpener.getFileContentsWithLineNumber());

				if (astRoot == null) {
					SimpleASTViewer viewer = new SimpleASTViewer(topLevelFrame, fileContents);
					viewer.parseSourceCode();
					String errorMessage = viewer.getParseErrorMessage();
					if (errorMessage != null) {
						JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "������ִ���\n" + errorMessage, "��ʾ",
								JOptionPane.WARNING_MESSAGE);
					}
					astRoot = viewer.getASTRoot();
					astText.setText(viewer.getASTViewerText());
				}

				try {
					ControlFlowGraphViewer viewer = new ControlFlowGraphViewer(fileOpener.getFileName(), astRoot);
					cfgText.setText(viewer.createCFGToText());
					tabbedPane.setSelectedIndex(cfgTabIndex);
				} catch (Exception exp) {
					exp.printStackTrace();
					cfgText.setText(exp.toString());
					JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "���ɿ�����ͼ��������", "��ʾ",
							JOptionPane.WARNING_MESSAGE);
				}
			} else if (command.equals(CREATE_NAME_TABLE_COMMAND)) {
				String fileContents = fileOpener.getFileContents();
				if (fileContents == null) {
					fileOpener.chooseFileName();
					fileOpener.loadFile();
					fileContents = fileOpener.getFileContents();
					topLevelFrame.setTitle(fileOpener.getFileName());

					astRoot = null; // For regenerate the ast for the new file!
					tableManager = null;
					cfgText.setText("");
				}
				sourceText.setText(fileOpener.getFileContentsWithLineNumber());

				if (tableManager == null) {
					String fileName = fileOpener.getFileFullName();
					tableManager = NameTableManager.createNameTableManager(fileName);
				}
				try {
					StringWriter nameTableString = new StringWriter();
					PrintWriter writer = new PrintWriter(nameTableString);
					NameDefinitionPrinter definitionPrinter = new NameDefinitionPrinter(writer);
					definitionPrinter.setPrintVariable(true);
					tableManager.accept(definitionPrinter);

					writer.println();
					writer.println("DetailedTypeDefinition List: ");
					List<DetailedTypeDefinition> typeList = tableManager.getAllDetailedTypeDefinitions();
					for (DetailedTypeDefinition type : typeList) {
						String typeInfo = type.getSimpleName();
						List<TypeReference> superList = type.getSuperList();
						if (superList != null) {
							typeInfo = typeInfo + ", super type: ";
							boolean firstType = true;
							for (TypeReference superType : superList) {
								if (firstType)
									typeInfo = typeInfo + " " + superType.toDeclarationString();
								else
									typeInfo = typeInfo + ", " + superType.toDeclarationString();
							}
						}
						writer.println("\t" + typeInfo);
					}
					writer.println("FieldDefinition List: ");
					NameDefinitionVisitor visitor = new NameDefinitionVisitor(
							new NameDefinitionKindFilter(NameDefinitionKind.NDK_FIELD));
					tableManager.accept(visitor);
					List<NameDefinition> definitionList = visitor.getResult();
					for (NameDefinition definition : definitionList) {
						FieldDefinition field = (FieldDefinition) definition;
						String fieldInfo = field.getSimpleName();
						TypeReference typeReference = field.getDeclareTypeReference();
						if (typeReference != null)
							fieldInfo = fieldInfo + " : " + typeReference.toDeclarationString();
						writer.println("\t" + fieldInfo);
					}
					writer.println("MethodDefinition List: ");
					visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
					tableManager.accept(visitor);
					definitionList = visitor.getResult();
					for (NameDefinition definition : definitionList) {
						MethodDefinition method = (MethodDefinition) definition;
						String methodInfo = method.getSimpleName();
						List<VariableDefinition> paraList = method.getParameterList();
						if (paraList != null) {
							methodInfo = methodInfo + "(";
							boolean firstPara = true;
							for (VariableDefinition parameter : paraList) {
								String paraInfo = parameter.toDeclarationString();
								if (firstPara) {
									methodInfo = methodInfo + paraInfo;
									firstPara = false;
								} else
									methodInfo = methodInfo + ", " + paraInfo;
							}
							methodInfo = methodInfo + ")";
						} else
							methodInfo = methodInfo + "()";
						TypeReference returnTypeReference = method.getDeclareTypeReference();
						if (returnTypeReference != null)
							methodInfo = methodInfo + " : " + returnTypeReference.toDeclarationString();
						writer.println("\t" + methodInfo);
					}
					nameText.setText(nameTableString.toString());
					writer.close();
					nameTableString.close();
					tabbedPane.setSelectedIndex(nameTabIndex);
				} catch (Exception exp) {
					exp.printStackTrace();
					cfgText.setText(exp.toString());
					JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "�������ֱ���������", "��ʾ",
							JOptionPane.WARNING_MESSAGE);
				}
			} else {
				// ����һ������ʾһЩ��Ϣ
				JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "�Բ�������˵����ܻ�û��ʵ�֣�", "��ʾ",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}
