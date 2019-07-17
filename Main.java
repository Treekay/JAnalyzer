import java.awt.*;
import javax.swing.*;

import gui.astViewer.SimpleASTViewer;
import gui.toolkit.*;
import model.Current;
import view.*;

public class Main {
	public static void main(String args[]) {
		// 设置页面
		MainFrame.init("JAnalyzer", MainFrame.screenWidth - 15, MainFrame.screenHeight - 100, 0, 0);

		// 菜单栏
		MenuField menuBar = new MenuField();
		MainFrame.getMainFrame().setJMenuBar(menuBar.getMenuBar());

		// 创建组件
		NavigatorField navigator = new NavigatorField();
		CodeField codeField = new CodeField();
		GraphField graphField = new GraphField();
		NameTableField tableField = new NameTableField();

		// 组件布局
		JSplitPane navPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		JSplitPane displayPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		JSplitPane generatePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		MainFrame.getContentPane().add(navPane);

		navPane.setDividerLocation(MainFrame.screenWidth / 5);
		navPane.setLeftComponent(navigator.getContentPane());
		navPane.setRightComponent(displayPane);

		displayPane.setDividerLocation(MainFrame.screenWidth * 2 / 5);
		displayPane.setLeftComponent(codeField.contentPane);
		displayPane.setRightComponent(generatePane);
		
		generatePane.setDividerLocation(MainFrame.screenHeight / 2);
		generatePane.setTopComponent(graphField.contentPane);
		generatePane.setBottomComponent(tableField.contentPane);
		
		MainFrame.start();
		
		Current.SelectAndLoadFile();
	}
}