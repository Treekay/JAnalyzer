package gui.components;

import java.awt.*;
import javax.swing.*;
import gui.toolkit.*;

public class GraphField {
    private JSplitPane contentPane;
    private JScrollPane astPane;
    private JScrollPane cfgPane;
    public static JTextArea astText; // 用于放置抽象语法树
    public static JTextArea cfgText; // 用于放置程序控制流图

    public GraphField() {
        astText = new JTextArea();
        astText.setEditable(false);
        astPane = new JScrollPane(astText);

        cfgText = new JTextArea();
        cfgText.setEditable(false);
        cfgPane = new JScrollPane(cfgText);

        contentPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        contentPane.setDividerLocation(MainFrame.screenHeight / 2);
        contentPane.setTopComponent(astPane);
        contentPane.setBottomComponent(cfgPane);
    }

    public JSplitPane getContentPane() {
        return this.contentPane;
    }
}