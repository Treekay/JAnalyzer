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
    	for (int i = 0; i < contentPane.getTabCount(); i++) {
    		if (contentPane.getComponentAt(i).getName().equals(tabName)) {
    			contentPane.setSelectedIndex(i);
    			return;
    		}
    	}
        contentPane.addTab(tabName, updateCode(tabName, fileText));
        contentPane.setSelectedIndex(contentPane.getTabCount()-1);
    }

    public static JScrollPane updateCode(String fileName, String fileText) {
        JTextArea sourceText = new JTextArea();
        sourceText.setEditable(false);
        sourceText.setText(fileText);
        JScrollPane sourcePane = new JScrollPane(sourceText);
        sourcePane.setName(fileName);
        return sourcePane;
    }

    public ClosableTabbedPane getContentPane() {
        return contentPane;
    }
}