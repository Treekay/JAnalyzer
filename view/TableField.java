package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

import gui.toolkit.*;

public class TableField {
    public static JScrollPane contentPane;
    public static JTextArea nameTableText; // 用于放置程序控制流图


    public TableField() {
//        contentPane = new JScrollPane();
        nameTableText = new JTextArea();
        nameTableText.setEditable(false);

//        Border titleBorder = BorderFactory.createTitledBorder("NameTable");
//        contentPane.setBorder(titleBorder);
        contentPane = new JScrollPane(nameTableText);
    }

    public JScrollPane getContentPane() {
        return this.contentPane;
    }
}