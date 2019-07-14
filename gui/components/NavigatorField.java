package gui.components;

import java.awt.*;
import javax.swing.*;
import gui.toolkit.*;

public class NavigatorField {
    private JPanel contentPane;

    public NavigatorField() {
        contentPane = new JPanel();
        contentPane.setBackground(Color.darkGray);
    }

    public JPanel getContentPane() {
        return this.contentPane;
    }
}