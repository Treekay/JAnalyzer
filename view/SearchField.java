package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;

import org.omg.CORBA.PUBLIC_MEMBER;

import gui.toolkit.*;
import model.Current;

public class SearchField {
	public static JPanel contentPane;
	public static JTextField searchText;
	public static JButton findButton;
	public static JButton cancelButton;
	
	public SearchField() {
		SearchButtonListener searchButtonListener = new SearchButtonListener();
		CancelButtonListener cancelButtonListener = new CancelButtonListener();
		
		contentPane = new JPanel();
		Border titleBorder=BorderFactory.createTitledBorder("Find");            
        contentPane.setBorder(titleBorder); 
        
        searchText = new JTextField(20);
        searchText.setEditable(true);
        contentPane.add(searchText);
        
        findButton = new JButton("Find All");
        findButton.addActionListener(searchButtonListener);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(cancelButtonListener);
        contentPane.add(findButton);
        contentPane.add(cancelButton);
	}
	
	class SearchButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(searchText.getText().equals("")) {
				JOptionPane.showMessageDialog(MainFrame.getMainFrame(),
                        "请输入要进行查找的字符串", "警示", JOptionPane.WARNING_MESSAGE);
			}
			else if(CodeField.contentPane.getTabCount() == 0) {
				JOptionPane.showMessageDialog(MainFrame.getMainFrame(),
                        "请先打开要查找的源文件", "警示", JOptionPane.WARNING_MESSAGE);
			}
			else {
				String toFindString = searchText.getText();
				if(!CodeField.findStringInFile(toFindString)) {
					JOptionPane.showMessageDialog(MainFrame.getMainFrame(),
	                        "没有与之匹配的字符串", "警示", JOptionPane.WARNING_MESSAGE);
				}
			}
		}
	}
	
	class CancelButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			CodeField.cleanMatch();
			searchText.setText("");
		}
		
	}
}
