package view;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import gui.toolkit.*;
import model.Current;
import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.visitor.NameDefinitionPrinter;
import sourceCodeAST.*;
import util.Debug;

public class TableField {
    public static JScrollPane contentPane;
    public static JTextArea nameTableText; // 用于放置程序控制流图


    public TableField() {
        nameTableText = new JTextArea();
        nameTableText.setEditable(false);

        contentPane = new JScrollPane(nameTableText);
        Border titleBorder = BorderFactory.createTitledBorder("NameTable");
        contentPane.setBorder(titleBorder);
    }
    
    public static void getNameTable() {
//    	SourceCodeFileSet parser = new SourceCodeFileSet(Current.file.getPath());
//		NameTableCreator creator = new NameDefinitionCreator(parser);
//		String[] fileNameArray = {"E:\\ZxcWork\\ToolKit\\data\\javalang.txt", "E:\\ZxcWork\\ToolKit\\data\\javautil.txt", "E:\\ZxcWork\\ToolKit\\data\\javaio.txt", }; 
//
//		NameTableManager manager = creator.createNameTableManager(new PrintWriter(System.out), fileNameArray);
//		
//		PrintWriter writer = new PrintWriter(new File(resultFile));
//		NameDefinitionPrinter definitionPrinter = new NameDefinitionPrinter(writer);
//		definitionPrinter.setPrintVariable(true);
//		manager.accept(definitionPrinter);
//		
//		writer.close();
    }
}