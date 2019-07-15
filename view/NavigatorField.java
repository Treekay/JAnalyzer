package view;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import gui.toolkit.*;

import model.*;

public class NavigatorField {
    public static ClosableTabbedPane contentPane;
    private static ArrayList<File> fileList;
    static JTree tree;
    static DefaultTreeModel newModel;
    static DefaultMutableTreeNode Node;
    static DefaultMutableTreeNode temp;

    public NavigatorField() {
        fileList = new ArrayList<File>();
        contentPane = new ClosableTabbedPane();
        contentPane.setName("资源管理器");
    }

    public static void addTreeTab(String path) {
        Node = traverseFolder(path);
        newModel = new DefaultTreeModel(Node);
        tree = new JTree(newModel);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 如果在这棵树上点击了2次,即双击
                if (e.getSource() == tree && e.getClickCount() == 2) {
                    // 按照鼠标点击的坐标点获取路径
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    if (selPath != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        selectCurrentFile(node.toString());
                    }
                }
            }
        });
        contentPane.addTab(new File(path).getName(), tree);
    }

    public static void selectCurrentFile(String fileName) {
        for (File file : fileList) {
            if (file.getName().equals(fileName)) {
                Current.file = file;
                FileChooserAndOpener.loadFile();
                CodeField.addCodeTab(FileChooserAndOpener.getFileName(),
                        FileChooserAndOpener.getFileContentsWithLineNumber());
                return;
            }
        }
    }

    public static DefaultMutableTreeNode traverseFolder(String path) {
        DefaultMutableTreeNode parent = new DefaultMutableTreeNode(new File(path).getName());
        File file = new File(path);
        if (file.isFile()) {
            fileList.add(file);
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
                        temp = new DefaultMutableTreeNode(file2.getName());
                        parent.add(temp);
                        fileList.add(file2);
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