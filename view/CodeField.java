package view;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;

import gui.toolkit.*;
import model.Current;

public class CodeField {
    public static ClosableTabbedPane contentPane;

    public CodeField() {
        contentPane = new ClosableTabbedPane();
        Border titleBorder=BorderFactory.createTitledBorder("SourceCode");            
        contentPane.setBorder(titleBorder);           
    }

    public static void addCodeTab(String tabName, String fileText) {
        contentPane.addTab(tabName, updateCode(tabName, fileText));
    }

    public static JScrollPane updateCode(String fileName, String fileText) {
        JTextArea sourceText = new JTextArea();
        sourceText.setEditable(false);
        sourceText.setText(fileText);
        return new JScrollPane(sourceText);
    }

    public ClosableTabbedPane getContentPane() {
        return contentPane;
    }
}