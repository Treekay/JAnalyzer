package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

import gui.toolkit.*;

public class TableField {
	public static JScrollPane contentPane;

    public TableField() {
    	contentPane = new JScrollPane();
    	Border titleBorder=BorderFactory.createTitledBorder("NameTable");            
        contentPane.setBorder(titleBorder); 
    }

    public JScrollPane getContentPane() {
        return this.contentPane;
    }
}