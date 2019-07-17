package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Random;

import javax.swing.*;
import javax.swing.border.Border;
import graph.cfg.creator.TestCFGCreator;
import gui.astViewer.SimpleASTViewer;
import gui.toolkit.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
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
        contentPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	Border titleBorder=BorderFactory.createTitledBorder("GraphField - " + GraphField.contentPane.getSelectedComponent().getName());            
                GraphField.contentPane.setBorder(titleBorder); 
            }
        });
    	
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
    
    public static void updateAST(SimpleASTViewer viewer ) {
    	GraphField.astText.setText(viewer.getASTViewerText());
        GraphField.contentPane.setSelectedIndex(0);
        GraphField.contentPane.getComponentAt(0).setName(Current.file.getName());
        Border titleBorder=BorderFactory.createTitledBorder("GraphField - " + GraphField.contentPane.getSelectedComponent().getName());            
        GraphField.contentPane.setBorder(titleBorder); 
    }
    
    public static void updateCFG() {
    	try {
        	if (Current.astRoot == null) {
        		System.out.print("astRoot null");
        		return;
        	}
            // Add generate Image from dot file logic
            String sourceCodePath = Current.file.getPath();
            String dotFileResult = Config.TEMP_FILE_LOCATION + "tempDotFile.dot";
            String pngFileResult = Config.TEMP_FILE_LOCATION + "tempGraph" + (new Random()).nextInt() + ".png";
            Current.cfgImagePath = pngFileResult;
            System.out.println(dotFileResult);
            PrintWriter output = null;
            try {
                output = new PrintWriter(new FileOutputStream(dotFileResult));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }

            TestCFGCreator.testMatchASTNode(sourceCodePath, output);
            if (output != null) output.close();
            MutableGraph g = Parser.read(new FileInputStream(new File(dotFileResult)));
            Graphviz.fromGraph(g)
                    .yInvert(true)
                    .render(Format.PNG)
                    .toFile(new File(pngFileResult));

            GraphField.cfgGraph.setIcon(new ImageIcon(pngFileResult));
            GraphField.contentPane.setSelectedIndex(1);
            GraphField.contentPane.getComponentAt(1).setName(Current.file.getName());
            Border titleBorder=BorderFactory.createTitledBorder("GraphField - " + GraphField.contentPane.getSelectedComponent().getName());            
            GraphField.contentPane.setBorder(titleBorder); 
            // End of Image logic

        } catch (Exception exp) {
            exp.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "生成控制流图发生错误！", "警示",
                    JOptionPane.WARNING_MESSAGE);
        }
    	
    }
}