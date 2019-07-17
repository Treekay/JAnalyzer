package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.border.Border;

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
import view.*;
import graph.cfg.creator.TestCFGCreator;

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
                GenerateNameTable();
            }
            NavigatorField.addTreeTab(Current.file.getAbsolutePath());
        }
    }

//    public static void GenerateNameTable() {
//    	if (FileChooserAndOpener.loadFile() == true) {
//            String fileContents = FileChooserAndOpener.getFileContents();
//            if (fileContents == null) {
//                FileChooserAndOpener.chooseFileName();
//                FileChooserAndOpener.loadFile();
//                MainFrame.getMainFrame().setTitle("JAnalyzer - " + Current.file.getName());
//                fileContents = FileChooserAndOpener.getFileContents();
//                
//                astRoot = null; 		// For regenerate the ast for the new file!
//                tableManager = null;
//            }
//
//            String fileName = FileChooserAndOpener.getFileFullName();
//            tableManager = NameTableManager.createNameTableManager(fileName);
//            
//	        try {
//	            StringWriter nameTableString = new StringWriter();
//	            PrintWriter writer = new PrintWriter(nameTableString);
//	            NameDefinitionPrinter definitionPrinter = new NameDefinitionPrinter(writer);
//	            definitionPrinter.setPrintVariable(true);
//	            tableManager.accept(definitionPrinter);
//	
//	            writer.println();
//	            writer.println("DetailedTypeDefinition List: ");
//	            List<DetailedTypeDefinition> typeList = tableManager.getAllDetailedTypeDefinitions();
//	            for (DetailedTypeDefinition type : typeList) {
//	                String typeInfo = type.getSimpleName();
//	                List<TypeReference> superList = type.getSuperList();
//	                if (superList != null) {
//	                    typeInfo = typeInfo + ", super type: ";
//	                    boolean firstType = true;
//	                    for (TypeReference superType : superList) {
//	                        if (firstType) typeInfo = typeInfo + " " + superType.toDeclarationString();
//	                        else typeInfo = typeInfo + ", " + superType.toDeclarationString();
//	                    }
//	                }
//	                writer.println("\t" + typeInfo);
//	            }
//	            writer.println("FieldDefinition List: ");
//	            NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_FIELD));
//	            tableManager.accept(visitor);
//	            List<NameDefinition> definitionList = visitor.getResult();
//	            for (NameDefinition definition : definitionList) {
//	                FieldDefinition field = (FieldDefinition)definition;
//	                String fieldInfo = field.getSimpleName();
//	                TypeReference typeReference = field.getDeclareTypeReference();
//	                if (typeReference != null) fieldInfo = fieldInfo + " : " + typeReference.toDeclarationString();
//	                writer.println("\t" + fieldInfo);
//	            }
//	            writer.println("MethodDefinition List: ");
//	            visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
//	            tableManager.accept(visitor);
//	            definitionList = visitor.getResult();
//	            for (NameDefinition definition : definitionList) {
//	                MethodDefinition method = (MethodDefinition)definition;
//	                String methodInfo = method.getSimpleName();
//	                List<VariableDefinition> paraList = method.getParameterList();
//	                if (paraList != null) {
//	                    methodInfo = methodInfo + "(";
//	                    boolean firstPara = true;
//	                    for (VariableDefinition parameter : paraList) {
//	                        String paraInfo = parameter.toDeclarationString();
//	                        if (firstPara) {
//	                            methodInfo = methodInfo + paraInfo;
//	                            firstPara = false;
//	                        } else methodInfo = methodInfo + ", " + paraInfo;
//	                    }
//	                    methodInfo = methodInfo + ")";
//	                } else methodInfo = methodInfo + "()";
//	                TypeReference returnTypeReference = method.getDeclareTypeReference();
//	                if (returnTypeReference != null) methodInfo = methodInfo + " : " + returnTypeReference.toDeclarationString();
//	                writer.println("\t" + methodInfo);
//	            }
//	            NameTableField.nameTableText.setText(nameTableString.toString());
//	            writer.close();
//	            nameTableString.close();
//	        } catch (Exception exp) {
//	            exp.printStackTrace();
//	            JOptionPane.showMessageDialog(MainFrame.getMainFrame(),
//	                    "生成名字表发生错误！", "警示", JOptionPane.WARNING_MESSAGE);
//	        }
//	        
//	        Border titleBorder=BorderFactory.createTitledBorder("NameTable - " + Current.file.getName());            
//	        NameTableField.contentPane.setBorder(titleBorder); 
//    	}
//    	
//    }
    
    public static void GenerateNameTable() {
    	if (FileChooserAndOpener.loadFile() == true) {
            String fileContents = FileChooserAndOpener.getFileContents();
            if (fileContents == null) {
                FileChooserAndOpener.chooseFileName();
                FileChooserAndOpener.loadFile();
                MainFrame.getMainFrame().setTitle("JAnalyzer - " + Current.file.getName());
                fileContents = FileChooserAndOpener.getFileContents();
                
                astRoot = null; 		// For regenerate the ast for the new file!
                tableManager = null;
            }

            String fileName = FileChooserAndOpener.getFileFullName();
            tableManager = NameTableManager.createNameTableManager(fileName);
            
	        try {
	            StringWriter nameTableString = new StringWriter();
	            PrintWriter writer = new PrintWriter(nameTableString);
	            NameDefinitionPrinter definitionPrinter = new NameDefinitionPrinter(writer);
	            definitionPrinter.setPrintVariable(true);
	            tableManager.accept(definitionPrinter);
	            NameTableField.updateNameTable(tableManager);
	        }  catch (Exception exp) {
	            exp.printStackTrace();
	            JOptionPane.showMessageDialog(MainFrame.getMainFrame(),
	                    "生成名字表发生错误！", "警示", JOptionPane.WARNING_MESSAGE);
	        }
	        
	        Border titleBorder=BorderFactory.createTitledBorder("NameTable - " + Current.file.getName());            
	        NameTableField.contentPane.setBorder(titleBorder); 
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
            GraphField.contentPane.setSelectedIndex(0);
            GraphField.contentPane.getComponentAt(0).setName(Current.file.getName());
            Border titleBorder=BorderFactory.createTitledBorder("GraphField - " + GraphField.contentPane.getSelectedComponent().getName());            
            GraphField.contentPane.setBorder(titleBorder); 
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
//                        .width(700)
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
}