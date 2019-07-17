package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

import gui.toolkit.*;
import model.Config;
import model.Current;

public class GraphField {
	public static JTabbedPane contentPane;
    public static JScrollPane astPane;
    public static JScrollPane cfgPane;
    public static JTextArea astText; // 用于放置抽象语法树
    public static JLabel cfgGraph; // 用于放置程序控制流图

    public GraphField() {
    	contentPane = new JTabbedPane();
    	Border titleBorder=BorderFactory.createTitledBorder("GraphField");            
        contentPane.setBorder(titleBorder); 
    	
        astText = new JTextArea();
        astText.setEditable(false);
        astPane = new JScrollPane(astText);

        cfgGraph = new JLabel();
        cfgGraph.setIcon(new ImageIcon("src/view/Pigeons.jpg"));
        cfgGraph.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		try {
        			if (Current.cfgImagePath != null) {
        				Desktop.getDesktop().open(new File(Current.cfgImagePath));        			        				
        			}
        		} catch (Exception exp) {
                    exp.printStackTrace();
                  JOptionPane.showMessageDialog(MainFrame.getMainFrame(),
                          "打开CFG图片时发生错误", "警示", JOptionPane.WARNING_MESSAGE);
              }
        	}
        });

        cfgPane = new JScrollPane(cfgGraph);

        contentPane.addTab("抽象语法树", astPane);
        contentPane.addTab("控制流图", cfgPane);
    }
}