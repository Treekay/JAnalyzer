package gui.toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.Component;
import javax.swing.Icon;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import model.*;

/**
 * 自定义带关闭按钮的TabbedPane
 */
public class ClosableTabbedPane extends JTabbedPane implements MouseListener {
    public ClosableTabbedPane() {
        super();
        addMouseListener(this);
    }

    @Override
    public void addTab(String title, Component component) {
        super.addTab(title, new CloseTabIcon(), component);
    }
    
	@Override
    public void mouseClicked(MouseEvent e) {
        int tabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());
        if (tabNumber < 0) {
            return;
        }
        if (((CloseTabIcon) getIconAt(tabNumber)) != null) {
            Rectangle rect = ((CloseTabIcon) getIconAt(tabNumber)).getBounds();
            boolean isFile = Current.selectCurrentFile(this.getSelectedComponent().getName());
            if (isFile) {
            	FileChooserAndOpener.loadFile();
            	Current.GenerateAST();
            	Current.GenerateNameTable();
            }
            if (rect.contains(e.getX(), e.getY())) {
                this.removeTabAt(tabNumber);
                if (this.getTabCount() != 0 && isFile) {
                	Current.selectCurrentFile(this.getComponentAt(this.getTabCount()-1).getName());
                }
            }
        }
    }
	 
    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
    
	class CloseTabIcon implements Icon {
	
	    private int x_pos;
	    private int y_pos;
	    private int width;
	    private int height;
	
	    public CloseTabIcon() {
	        width = 16;
	        height = 16;
	    }
	
	    @Override
	    public void paintIcon(Component c, Graphics g, int x, int y) {
	        this.x_pos = x;
	        this.y_pos = y;
	        Color col = g.getColor();
	        g.setColor(Color.black);
	        int y_p = y + 2;
	        g.drawLine(x + 3, y_p + 3, x + 10, y_p + 10);
	        g.drawLine(x + 3, y_p + 4, x + 9, y_p + 10);
	        g.drawLine(x + 4, y_p + 3, x + 10, y_p + 9);
	        g.drawLine(x + 10, y_p + 3, x + 3, y_p + 10);
	        g.drawLine(x + 10, y_p + 4, x + 4, y_p + 10);
	        g.drawLine(x + 9, y_p + 3, x + 3, y_p + 9);
	        g.setColor(col);
	    }
	
	    @Override
	    public int getIconWidth() {
	        return width;
	    }
	
	    @Override
	    public int getIconHeight() {
	        return height;
	    }
	
	    public Rectangle getBounds() {
	        return new Rectangle(x_pos, y_pos, width, height);
	    }
	}
}