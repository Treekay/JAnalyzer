package view;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import gui.toolkit.*;
import model.Current;
import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.TypeReference;
import nameTable.visitor.NameDefinitionPrinter;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.*;
import util.Debug;

public class NameTableField {
    public static JScrollPane contentPane;
//    public static JTextArea nameTableText;
    public static JTabbedPane tabbedPane;
    public static JTree nameTable;
    public static DefaultTreeModel treeModel;
    public static DefaultMutableTreeNode Variables;
    public static DefaultMutableTreeNode Methods;

    public NameTableField() {
//        nameTableText = new JTextArea();
//        nameTableText.setEditable(false);
    	tabbedPane = new JTabbedPane();
        contentPane = new JScrollPane(tabbedPane);
        
        Border titleBorder = BorderFactory.createTitledBorder("NameTable");
        contentPane.setBorder(titleBorder);
    }
    
    public static void updateNameTable(NameTableManager tableManager) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(Current.file.getName());
		Variables = new DefaultMutableTreeNode("Variables");
		Methods = new DefaultMutableTreeNode("Methods");
		treeModel = new DefaultTreeModel(root);
		treeModel.insertNodeInto(Variables, root, root.getChildCount());
        treeModel.insertNodeInto(Methods, root, root.getChildCount());
        
        NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_FIELD));
        tableManager.accept(visitor);
        List<NameDefinition> definitionList = visitor.getResult();
        for (NameDefinition definition : definitionList) {
            FieldDefinition field = (FieldDefinition)definition;
            String fieldInfo = field.getSimpleName();
            TypeReference typeReference = field.getDeclareTypeReference();
            if (typeReference != null) fieldInfo = fieldInfo + " : " + typeReference.toDeclarationString();
            
            DefaultMutableTreeNode leafnode1 = new DefaultMutableTreeNode(fieldInfo);
            treeModel.insertNodeInto(leafnode1, Variables, Variables.getChildCount());
        }
         
         visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
         tableManager.accept(visitor);
         definitionList = visitor.getResult();
         for (NameDefinition definition : definitionList) {
             MethodDefinition method = (MethodDefinition)definition;
             String methodInfo = method.getSimpleName();
             List<VariableDefinition> paraList = method.getParameterList();
             if (paraList != null) {
                 methodInfo = methodInfo + "(";
                 boolean firstPara = true;
                 for (VariableDefinition parameter : paraList) {
                     String paraInfo = parameter.toDeclarationString();
                     if (firstPara) {
                         methodInfo = methodInfo + paraInfo;
                         firstPara = false;
                     } else methodInfo = methodInfo + ", " + paraInfo;
                 }
                 methodInfo = methodInfo + ")";
             } else methodInfo = methodInfo + "()";
             TypeReference returnTypeReference = method.getDeclareTypeReference();
             if (returnTypeReference != null) methodInfo = methodInfo + " : " + returnTypeReference.toDeclarationString();
             
             DefaultMutableTreeNode leafnode2 = new DefaultMutableTreeNode(methodInfo);
             treeModel.insertNodeInto(leafnode2, Methods, Methods.getChildCount());
         }
         nameTable = new JTree(treeModel);
         tabbedPane.removeAll();
         tabbedPane.addTab(Current.file.getName(), nameTable);
    }
}