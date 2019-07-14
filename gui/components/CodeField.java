package gui.components;

import java.awt.*;
import javax.swing.*;
import gui.toolkit.*;
import model.Current;

public class CodeField {
    public static ClosableTabbedPane contentPane;

    public CodeField() {
        contentPane = new ClosableTabbedPane();
    }

    public ClosableTabbedPane getContentPane() {
        return this.contentPane;
    }
}