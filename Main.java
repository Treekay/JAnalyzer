import java.awt.*;
import javax.swing.*;
import gui.toolkit.*;
import view.*;

public class Main {
	public static void main(String args[]) {
		// 文件选择
		MainFrame.init("JAnalyzer");

		// 设置页面
		MainFrame.init("JAnalyzer", MainFrame.screenWidth - 15, MainFrame.screenHeight - 100, 0, 0);

		// 菜单栏
		MenuField menuBar = new MenuField();
		MainFrame.getMainFrame().setJMenuBar(menuBar.getMenuBar());

		// 创建组件
		NavigatorField navigator = new NavigatorField();
		CodeField codeField = new CodeField();
		GraphField graphField = new GraphField();

		// 组件布局
		JSplitPane navPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		JSplitPane displayPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		MainFrame.getContentPane().add(navPane);

		navPane.setDividerLocation(MainFrame.screenWidth / 5);
		navPane.setLeftComponent(navigator.getContentPane());
		navPane.setRightComponent(displayPane);

		displayPane.setDividerLocation(MainFrame.screenWidth * 2 / 5);
		displayPane.setLeftComponent(codeField.getContentPane());
		displayPane.setRightComponent(graphField.getContentPane());

		MainFrame.start();
	}
}