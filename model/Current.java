package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import gui.astViewer.ControlFlowGraphViewer;
import gui.astViewer.SimpleASTViewer;
import gui.toolkit.FileChooserAndOpener;
import gui.toolkit.MainFrame;

import org.eclipse.jdt.core.dom.CompilationUnit;
import nameTable.NameTableManager;
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
import view.CodeField;
import view.GraphField;
import view.NavigatorField;

public class Current {
    public static File file;
    public static CompilationUnit astRoot = null;
    public static NameTableManager tableManager = null;
    
    public static void SelectAndLoadFile() {
    	if (FileChooserAndOpener.chooseFileName() == true) {
            if (Current.file.isFile()) {
                FileChooserAndOpener.loadFile();
                MainFrame.getMainFrame().setTitle("JAnalyzer - " + Current.file.getName());
                CodeField.addCodeTab(FileChooserAndOpener.getFileName(),
                        FileChooserAndOpener.getFileContentsWithLineNumber());
                GenerateAST();
            }
            NavigatorField.addTreeTab(Current.file.getAbsolutePath());
        }
    }
    
    public static void GenerateAST() {
    	if (FileChooserAndOpener.loadFile() == true) {
            String fileContents = FileChooserAndOpener.getFileContents();
            if (fileContents == null) {
                FileChooserAndOpener.chooseFileName();
                FileChooserAndOpener.loadFile();
                MainFrame.getMainFrame().setTitle("JAnalyzer - " + Current.file.getName());
                fileContents = FileChooserAndOpener.getFileContents();
            }
            SimpleASTViewer viewer = new SimpleASTViewer(MainFrame.getMainFrame(), fileContents);
            viewer.parseSourceCode();
            String errorMessage = viewer.getParseErrorMessage();
            if (errorMessage != null) {
                JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "编译出现错误：\n" + errorMessage, "警示",
                        JOptionPane.WARNING_MESSAGE);
            }
            if (viewer.hasParserError())
                Current.astRoot = null;
            else
                Current.astRoot = viewer.getASTRoot();
            GraphField.astText.setText(viewer.getASTViewerText());
        }
    }
    
    public static void GenerateCFG() {
    	if (FileChooserAndOpener.loadFile() == true) {
            String fileContents = FileChooserAndOpener.getFileContents();
            if (fileContents == null) {
                FileChooserAndOpener.chooseFileName();
                FileChooserAndOpener.loadFile();
                MainFrame.getMainFrame().setTitle("JAnalyzer - " + Current.file.getName());
                fileContents = FileChooserAndOpener.getFileContents();
            }

            if (Current.astRoot == null) {
                SimpleASTViewer viewer = new SimpleASTViewer(MainFrame.getMainFrame(), fileContents);
                viewer.parseSourceCode();
                String errorMessage = viewer.getParseErrorMessage();
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "编译出现错误：\n" + errorMessage, "警示",
                            JOptionPane.WARNING_MESSAGE);
                }
                Current.astRoot = viewer.getASTRoot();
                GraphField.astText.setText(viewer.getASTViewerText());
            }

            try {
                ControlFlowGraphViewer viewer = new ControlFlowGraphViewer(FileChooserAndOpener.getFileName(),
                        Current.astRoot);
                GraphField.cfgText.setText(viewer.createCFGToText());
            } catch (Exception exp) {
                exp.printStackTrace();
                GraphField.cfgText.setText(exp.toString());
                JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "生成控制流图发生错误！", "警示",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}