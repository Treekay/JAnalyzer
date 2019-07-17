package view;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import gui.astViewer.SimpleASTViewer;
import gui.toolkit.*;

import model.*;

public class NavigatorField {
    public static ClosableTabbedPane contentPane;
    static JTree tree;
    static DefaultTreeModel newModel;
    static DefaultMutableTreeNode Node;
    static DefaultMutableTreeNode temp;

    public NavigatorField() {
        Current.fileList = new ArrayList<File>();
        contentPane = new ClosableTabbedPane();
        
        Border titleBorder=BorderFactory.createTitledBorder("Navigator");            
        contentPane.setBorder(titleBorder); 
    }

    public static void addTreeTab(String path) {
        Node = traverseFolder(path);
        newModel = new DefaultTreeModel(Node);
        tree = new JTree(newModel);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 如果在这棵树上点击了2次,即双击
                if (e.getSource() == tree && e.getClickCount() == 1) {
                    // 按照鼠标点击的坐标点获取路径
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    if (selPath != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        OpenInCodeField(node.toString());
                    }
                }
            }
        });
        contentPane.addTab(new File(path).getName(), tree);
    }

    public static void OpenInCodeField(String fileName) {
    	if (Current.selectCurrentFile(fileName)) {
    		FileChooserAndOpener.loadFile();
            CodeField.addCodeTab(FileChooserAndOpener.getFileName(),
                    FileChooserAndOpener.getFileContentsWithLineNumber());
            Current.GenerateAST();
    	}
    }

    public static DefaultMutableTreeNode traverseFolder(String path) {
        DefaultMutableTreeNode parent = new DefaultMutableTreeNode(new File(path).getName());
        File file = new File(path);
        if (file.isFile()) {
        	Current.fileList.add(file);
            return parent;
        }
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                if (file.isDirectory()) {// 如果是空文件夹
                    DefaultMutableTreeNode dn = new DefaultMutableTreeNode(file.getName(), false);
                    return dn;
                }
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        // 是目录的话，生成节点，并添加里面的节点
                        parent.add(traverseFolder(file2.getAbsolutePath()));
                    } else {
                        // 是文件的话直接生成节点，并把该节点加到对应父节点上
                    	int pos = file2.getName().lastIndexOf(".");
                    	if (file2.getName().substring(pos + 1).equals("java")) {
                    		temp = new DefaultMutableTreeNode(file2.getName());
                    		parent.add(temp);
                    		Current.fileList.add(file2);                    		
                    	}
                    }
                }
            }
        } else {// 文件不存在
            return null;
        }
        return parent;
    }

    public ClosableTabbedPane getContentPane() {
        return contentPane;
    }
}