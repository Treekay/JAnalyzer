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
	public static void main(String[] args) {
		// 初始化主画框，调整其位置和宽度，使得显示出来的按钮更漂亮
		int widthSpace = 15;
		int heightSpace = 100;

		MainFrame.init("Java程序控制流图展示工具", MainFrame.screenWidth - widthSpace, MainFrame.screenHeight - heightSpace, 0, 0,
				"system");

		DemoMenuCreator demo = new DemoMenuCreator(MainFrame.getContentPane(), MainFrame.getMainFrame());
		// 创建演示用的菜单和组件，并菜单放置在主画框，组件放置主画框的内容面板
		demo.createMenu();
		// 启动主画框，并进行演示
		MainFrame.start();
	}
}

class DemoMenuCreator {
	private Container place; // 放置演示组件的容器
	private JFrame topLevelFrame; // 放置菜单的顶层容器
	private JTabbedPane tabbedPane;
	private int astTabIndex;
	private JTextArea sourceText; // 用于放置源代码文件
	private JTextArea astText; // 用于放置抽象语法树
	private JTextArea cfgText; // 用于放置程序控制流图
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

	public DemoMenuCreator(Container place, JFrame topLevelFrame) {
		this.place = place;
		this.topLevelFrame = topLevelFrame;
		fileOpener = new FileChooserAndOpener(topLevelFrame);
	}

	// 创建用于演示的组件
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
		tabbedPane.addTab("抽象语法树", scrollPane);
		astTabIndex = 0;

		cfgText = new JTextArea();
		cfgText.setEditable(false);
		scrollPane = new JScrollPane(cfgText);
		tabbedPane.addTab("控制流图", scrollPane);
		cfgTabIndex = 1;

		nameText = new JTextArea();
		nameText.setEditable(false);
		scrollPane = new JScrollPane(nameText);
		tabbedPane.addTab("名字表", scrollPane);
		nameTabIndex = 2;

		hSplitPane.resetToPreferredSizes();

		// 创建菜单的监听器
		MenuListener menuListener = new MenuListener();
		// 创建菜单条
		JMenuBar menuBar = new JMenuBar();
		topLevelFrame.setJMenuBar(menuBar); // 放置在顶层容器

		// 创建第一个主菜单项
		JMenu menu = new JMenu("文件(F)");
		menu.setMnemonic(KeyEvent.VK_F); // 设置字符键F为快捷键
		menuBar.add(menu); // 加入到菜单条
		// 设置第一个主菜单项的第一个子菜单项
		JMenuItem menuItem = new JMenuItem("打开(O)", null);
		menuItem.setMnemonic(KeyEvent.VK_O);
		// 设置此菜单项的加速键为Ctrl+O
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(OPEN_COMMAND); // 设置命令为退出程序
		menu.add(menuItem); // 加入到第一个主菜单项

		// 设置第一个主菜单项的第一个子菜单项
		menuItem = new JMenuItem("语法树(A)", null);
		menuItem.setMnemonic(KeyEvent.VK_A);
		// 设置此菜单项的加速键为Ctrl+A
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(ASTPARSER_COMMAND); // 设置命令
		menu.add(menuItem); // 加入到第一个主菜单项

		// 设置第一个主菜单项的第二个子菜单项
		menuItem = new JMenuItem("紧凑语法树(C)", null);
		menuItem.setMnemonic(KeyEvent.VK_C);
		// 设置此菜单项的加速键为Ctrl+C
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(CONCISEAST_COMMAND); // 设置命令
		menu.add(menuItem); // 加入到第一个主菜单项

		// 设置第一个主菜单项的第三个子菜单项
		menuItem = new JMenuItem("控制流图(G)", null);
		menuItem.setMnemonic(KeyEvent.VK_G);
		// 设置此菜单项的加速键为Ctrl+G
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(CREATE_CFG_COMMAND); // 设置命令为退出程序
		menu.add(menuItem); // 加入到第二个主菜单项

		// 设置第一个主菜单项的第三个子菜单项
		menuItem = new JMenuItem("名字表(N)", null);
		menuItem.setMnemonic(KeyEvent.VK_N);
		// 设置此菜单项的加速键为Ctrl+N
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(CREATE_NAME_TABLE_COMMAND); // 设置命令
		menu.add(menuItem); // 加入到第一个主菜单项

		menu.addSeparator();
		// 为第一个主菜单添加最后一个菜单项
		menuItem = new JMenuItem("退出");
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(EXIT_COMMAND); // 设置命令为退出程序
		menu.add(menuItem);
		// 第二个主菜单项.
		menu = new JMenu("帮助(H)");
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);
		menuItem = new JMenuItem("关于...");
		menuItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
		menuItem.addActionListener(menuListener);
		menuItem.setActionCommand(ABOUT_COMMAND);
		menu.add(menuItem);
	}

	// 监听菜单项的按下动作
	private class MenuListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JMenuItem source = (JMenuItem) (e.getSource());
			String command = source.getActionCommand();
			if (command.equals(ABOUT_COMMAND)) {
				// 弹出一窗口显示一些信息
				JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "Java程序抽象语法树展示", "关于",
						JOptionPane.WARNING_MESSAGE);
			} else if (command.equals(EXIT_COMMAND))
				System.exit(1); // 退出整个程序
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
					JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "编译出现错误：\n" + errorMessage, "警示",
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
					JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "编译出现错误：\n" + errorMessage, "警示",
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
						JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "编译出现错误：\n" + errorMessage, "警示",
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
					JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "生成控制流图发生错误！", "警示",
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
					JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "生成名字表发生错误！", "警示",
							JOptionPane.WARNING_MESSAGE);
				}
			} else {
				// 弹出一窗口显示一些信息
				JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "对不起，这项菜单功能还没有实现！", "警示",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}
