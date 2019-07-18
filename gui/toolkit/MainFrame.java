package gui.toolkit;

import model.Config;
import model.Current;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;

public class MainFrame {
    // 获取显示器的宽度和高度，并置为公有属性，使用者可据此计算画框的位置
    public static final int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    public static final int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    // 设置主画框的缺省宽度和缺省位置
    private static int width = screenWidth / 3;
    private static int height = screenHeight / 4;
    private static int startX = screenWidth / 3;
    private static int startY = screenHeight / 3;
    private static JFrame frame;
    private static JPanel contentPane;

    // 使用私有的构造方法可防止使用者创建MainFrame对象，这是工具类的常见做法
    private MainFrame() {
    }

    // 不使用构造方法，而使用init()方法初始化，任何使用类MainFrame的程序必须先调用init()方法
    public static void init(String title) {
        frame = new JFrame(title);
        frame.setLocation(new Point(startX, startY));
        contentPane = (JPanel) frame.getContentPane();
        contentPane.setPreferredSize(new Dimension(width, height));
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {

//                if (JOptionPane.showConfirmDialog(frame,
//                        "是否退出?", "确认",
//                        JOptionPane.YES_NO_OPTION,
//                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                File tmpDir = new File(Config.TEMP_FILE_LOCATION);
                ArrayList<File> fileToDel = new ArrayList<>();
                if (null != tmpDir && tmpDir.isDirectory()) {
                    for (File f : tmpDir.listFiles()) {
                        if (f.isDirectory()) {
                            continue;
                        } else {
                            if (f.getName().startsWith("tempDotFile")
                                    || f.getName().startsWith("tempGraph")) {
                                fileToDel.add(f);
                            }
                        }
                    }
                }
                for (File f : fileToDel) {
                    f.delete();
                }
//                    frame.dispose();
//                    System.exit(0);
//                } else {
//                    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//                }

            }
        });
    }

    public static void init(String title, int w, int h, int x, int y) {
        width = w;
        height = h;
        startX = x;
        startY = y;
        init(title);
    }

    // 初始化画框并设置画框的观感
    public static void init(String title, int w, int h, int x, int y, String lookAndFeel) {
        try {
            if (lookAndFeel.equalsIgnoreCase("windows"))
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            else if (lookAndFeel.equalsIgnoreCase("system"))
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            else if (lookAndFeel.equalsIgnoreCase("motif"))
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            else
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }
        width = w;
        height = h;
        startX = x;
        startY = y;
        init(title);
    }

    // 使画框可见，从而启动整个GUI
    public static void start() {
        frame.pack();
        frame.setVisible(true);
    }

    // 获取画框的内容窗格，使用者可往此窗格添加所创建的GUI组件
    public static JPanel getContentPane() {
        return contentPane;
    }

    // 获取画框，使用对话框和菜单的程序要直接基于画框本身
    public static JFrame getMainFrame() {
        return frame;
    }

    public static void setCurrentTitle(String title) {
        MainFrame.getMainFrame().setTitle("JAnalyzer - " + Current.file.getName());
    }
}
