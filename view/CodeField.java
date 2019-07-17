package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import gui.toolkit.*;

public class CodeField {
    public static ClosableTabbedPane contentPane;
    public static JTextPane sourceText;

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
    	sourceText = new JTextPane();
        sourceText.setEditable(false);
        sourceText.setText(fileText);
        sourceText.setMinimumSize(new Dimension(contentPane.getWidth(), contentPane.getHeight()));
        JPanel panel = new JPanel();
        panel.add(sourceText);
        JScrollPane sourcePane = new JScrollPane(panel);        
        sourcePane.setName(fileName);
        
        return sourcePane;
    }
    
    public static boolean findStringInFile(String toFindString) {
    	int pos = sourceText.getText().indexOf(toFindString);
    	if(pos == -1)
    		return false;
    	
    	//取消上一次匹配字符串的标记
    	StyledDocument preDocument = (StyledDocument) sourceText.getDocument();
    	SimpleAttributeSet preAtrributes = new SimpleAttributeSet();
    	StyleConstants.setForeground(preAtrributes, Color.black);
    	preDocument.setCharacterAttributes(0, sourceText.getText().length(), preAtrributes, false);
    	
    	//搜索匹配的字符串并进行标记
    	while(pos != -1) {
    		int tail = pos + toFindString.length();
    		sourceText.select(pos, tail);
    		StyledDocument document =(StyledDocument) sourceText.getDocument();
    		
    		SimpleAttributeSet attributes = new SimpleAttributeSet();

    	    StyleConstants.setForeground(attributes, Color.red);
    	    document.setCharacterAttributes(pos,toFindString.length(),attributes ,false);
    	    
    	    pos = sourceText.getText().indexOf(toFindString,tail);
    	}
    	return true;
    }
}