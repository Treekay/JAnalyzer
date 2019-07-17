package model;

import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import gui.astViewer.SimpleASTViewer;
import gui.toolkit.FileChooserAndOpener;
import gui.toolkit.MainFrame;

import org.eclipse.jdt.core.dom.CompilationUnit;
import nameTable.NameTableManager;
import view.*;

public class Current {
    public static File file;
    public static ArrayList<File> fileList;
    public static String cfgImagePath = null;
    public static CompilationUnit astRoot = null;
    public static NameTableManager tableManager = null;

    public static boolean selectCurrentFile(String fileName) {
        for (File file : Current.fileList) {
            if (file.getName().equals(fileName)) {
                Current.file = file;
                MainFrame.setCurrentTitle("JAnalyzer - " + file.getName());
                return true;
            }
        }
        return false;
    }

    public static void SelectAndLoadFile() {
        if (FileChooserAndOpener.chooseFileName() == true) {
            if (Current.file.isFile()) {
                FileChooserAndOpener.loadFile();
                MainFrame.setCurrentTitle("JAnalyzer - " + Current.file.getName());
                CodeField.addCodeTab(FileChooserAndOpener.getFileName(),
                        FileChooserAndOpener.getFileContentsWithLineNumber());
                GenerateAST();
                GenerateNameTable();
            }
            NavigatorField.addTreeTab(Current.file.getAbsolutePath());
        }
    }

    public static void GenerateNameTable() {
    	if (FileChooserAndOpener.loadFile() == true) {
            String fileContents = FileChooserAndOpener.getFileContents();
            if (fileContents == null) {
                FileChooserAndOpener.chooseFileName();
                FileChooserAndOpener.loadFile();
                MainFrame.setCurrentTitle("JAnalyzer - " + Current.file.getName());
                fileContents = FileChooserAndOpener.getFileContents();
                
                astRoot = null; 		// For regenerate the ast for the new file!
                tableManager = null;
            }

            String fileName = FileChooserAndOpener.getFileFullName();
            tableManager = NameTableManager.createNameTableManager(fileName);
            
            NameTableField.updateNameTable(tableManager);
    	}
    }

    public static void GenerateAST() {
        if (FileChooserAndOpener.loadFile() == true) {
            String fileContents = FileChooserAndOpener.getFileContents();
            if (fileContents == null) {
                FileChooserAndOpener.chooseFileName();
                FileChooserAndOpener.loadFile();
                MainFrame.setCurrentTitle("JAnalyzer - " + Current.file.getName());
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
            GraphField.updateAST(viewer);
        }
    }

    public static void GenerateCFG() {
        if (FileChooserAndOpener.loadFile() == true) {
            String fileContents = FileChooserAndOpener.getFileContents();
            if (fileContents == null) {
                FileChooserAndOpener.chooseFileName();
                FileChooserAndOpener.loadFile();
                MainFrame.setCurrentTitle("JAnalyzer - " + Current.file.getName());
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
            }

            GraphField.updateCFG();
        }
    }
}