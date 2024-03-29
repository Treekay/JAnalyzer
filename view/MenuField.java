package view;

import java.awt.event.*;
import javax.swing.*;
import gui.toolkit.*;
import model.Current;

public class MenuField {
    private JMenuBar menuBar;

    private final String OPEN_COMMAND = "open";
    private final String CREATE_AST_COMMAND = "createAST";
    private final String CREATE_CFG_COMMAND = "createCFG";
    private final String CREATE_NAME_TABLE_COMMAND = "CreateNameTable";
    private final String ABOUT_COMMAND = "about";
    private final String EXIT_COMMAND = "exit";

    public MenuField() {
        menuBar = new JMenuBar();

        MainFrame.getMainFrame().addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        initMenuBar();
    }

    public JMenuBar getMenuBar() {
        return this.menuBar;
    }

    private void initMenuBar() {
        MenuItemListener menuItemListener = new MenuItemListener();
        // 创建第一个主菜单项
        JMenu menu = new JMenu("文件(F)");
        menu.setMnemonic(KeyEvent.VK_F); // 设置字符键F为快捷键
        menuBar.add(menu); // 加入到菜单条

        // 设置子菜单项
        JMenuItem menuItem = new JMenuItem("打开(O)");
        // 设置字符键 O 为快捷键
        menuItem.setMnemonic(KeyEvent.VK_O);
        // 设置此菜单项的加速键为 Ctrl+O
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(menuItemListener);
        menuItem.setActionCommand(OPEN_COMMAND);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("语法树(A)");
        // 设置字符键 A 为快捷键
        menuItem.setMnemonic(KeyEvent.VK_A);
        // 设置此菜单项的加速键为 Ctrl+A
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(menuItemListener);
        menuItem.setActionCommand(CREATE_AST_COMMAND);
        menu.add(menuItem);

        menuItem = new JMenuItem("控制流图(G)");
        // 设置字符键 G 为快捷键
        menuItem.setMnemonic(KeyEvent.VK_G);
        // 设置此菜单项的加速键为 Ctrl+G
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(menuItemListener);
        menuItem.setActionCommand(CREATE_CFG_COMMAND);
        menu.add(menuItem);

        menuItem = new JMenuItem("名字表(N)");
        // 设置字符键 N 为快捷键
        menuItem.setMnemonic(KeyEvent.VK_N);
        // 设置此菜单项的加速键为 Ctrl+N
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(menuItemListener);
        menuItem.setActionCommand(CREATE_NAME_TABLE_COMMAND);
        menu.add(menuItem);

        menu.addSeparator();

        // 为第一个主菜单添加最后一个菜单项
        menuItem = new JMenuItem("退出");
        menuItem.addActionListener(menuItemListener);
        menuItem.setActionCommand(EXIT_COMMAND); // 设置命令为退出程序
        menu.add(menuItem);

        // 创建第二个主菜单项
        menu = new JMenu("帮助(H)");
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);
        menuItem = new JMenuItem("关于...");
        menuItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
        menuItem.addActionListener(menuItemListener);
        menuItem.setActionCommand(ABOUT_COMMAND);
        menu.add(menuItem);
    }

    class MenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JMenuItem source = (JMenuItem) (e.getSource());
            String command = source.getActionCommand();
            switch (command) {
            case OPEN_COMMAND:
                Current.SelectAndLoadFile();
                break;
            case CREATE_AST_COMMAND:
                Current.GenerateAST();
                break;
            case CREATE_CFG_COMMAND:
                Current.GenerateCFG();
                break;
            case CREATE_NAME_TABLE_COMMAND:
            	Current.GenerateNameTable();
                break;
            case ABOUT_COMMAND:
                JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "Java程序抽象语法树展示", "关于",
                        JOptionPane.WARNING_MESSAGE);
                break;
            case EXIT_COMMAND:
                System.exit(0);
                break;
            default:
                break;
            }
        }
    }

}