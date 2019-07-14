package view;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import gui.toolkit.*;

public class NavigatorField {
    public static ClosableTabbedPane contentPane;
    static JTree tree;
    static DefaultTreeModel newModel;
    static DefaultMutableTreeNode Node;
    static DefaultMutableTreeNode temp;

    public NavigatorField() {
        contentPane = new ClosableTabbedPane();
        contentPane.setName("资源管理器");
    }

    public static void updateTree(String path) {
        Node = traverseFolder(path);
        newModel = new DefaultTreeModel(Node);
        tree = new JTree(newModel);
        contentPane.removeAll();
        contentPane.addTab(new File(path).getName(), tree);
    }

    public static DefaultMutableTreeNode traverseFolder(String path) {
        DefaultMutableTreeNode parent = new DefaultMutableTreeNode(new File(path).getName());
        File file = new File(path);

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