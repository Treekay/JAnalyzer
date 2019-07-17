package view;

import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

import gui.toolkit.*;

public class GraphField {
	public static JTabbedPane contentPane;
    public static JScrollPane astPane;
    public static JScrollPane cfgPane;
    public static JTextArea astText; // 用于放置抽象语法树
    public static JTextArea cfgText; // 用于放置程序控制流图
    public static JLabel cfgGraph;

    public GraphField() {
    	contentPane = new JTabbedPane();
    	Border titleBorder=BorderFactory.createTitledBorder("GraphField");            
        contentPane.setBorder(titleBorder); 
    	
        astText = new JTextArea();
        astText.setEditable(false);
        astPane = new JScrollPane(astText);

        cfgGraph = new JLabel();
        cfgGraph.setIcon(new ImageIcon("src/view/Pigeons.jpg"));

        cfgText = new JTextArea();
        cfgText.setEditable(false);

        cfgPane = new JScrollPane(cfgText);

        contentPane.addTab("抽象语法树", astPane);
        contentPane.addTab("控制流图", cfgPane);
        contentPane.addTab("cfgGraph", cfgGraph);
    }
}