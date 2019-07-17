package model;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.*;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import gui.astViewer.ControlFlowGraphViewer;
import gui.astViewer.SimpleASTViewer;
import gui.toolkit.FileChooserAndOpener;
import gui.toolkit.MainFrame;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
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
import sourceCodeAST.SourceCodeFile;
import sourceCodeAST.SourceCodeFileSet;
import view.CodeField;
import view.GraphField;
import view.NavigatorField;
import graph.cfg.creator.TestCFGCreator;

public class Current {
    public static File file;
    public static ArrayList<File> fileList;
    public static CompilationUnit astRoot = null;
    public static NameTableManager tableManager = null;

    public static boolean selectCurrentFile(String fileName) {
        for (File file : Current.fileList) {
            if (file.getName().equals(fileName)) {
                Current.file = file;
                MainFrame.getMainFrame().setTitle("JAnalyzer - " + file.getName());
                return true;
            }
        }
        return false;
    }

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

                // Add generate Image from dot file logic

                String sourceCodePath = Current.file.getPath();
                String dotFileResult = Config.TEMP_FILE_LOCATION + "tempDotFile.dot";
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
                MutableGraph g = Parser.read(new FileInputStream(new File("C:\\Users\\Steve\\AppData\\Local\\Temp\\tempDotFile.dot")));
                Graphviz.fromGraph(g)
                        .width(700)
                        .render(Format.PNG)
                        .toFile(new File(Config.TEMP_FILE_LOCATION + "tempGraph.png"));

                GraphField.cfgGraph.setIcon(new ImageIcon(Config.TEMP_FILE_LOCATION + "tempGraph.png"));


                // End of Image logic

            } catch (Exception exp) {
                exp.printStackTrace();
                GraphField.cfgText.setText(exp.toString());
                JOptionPane.showMessageDialog(MainFrame.getMainFrame(), "生成控制流图发生错误！", "警示",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}