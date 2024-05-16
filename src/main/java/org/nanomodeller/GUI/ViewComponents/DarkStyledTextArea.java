package org.nanomodeller.GUI.ViewComponents;

import javax.swing.*;
import java.awt.*;

public class DarkStyledTextArea extends JTextArea {
    public DarkStyledTextArea(String text) {
        super(text);
        setFont(new Font("Monospaced", Font.PLAIN, 14));
        setForeground(Color.WHITE);
        setBackground(Color.BLACK);
        setCaretColor(Color.WHITE);
        setSelectionColor(Color.DARK_GRAY);
        setSelectedTextColor(Color.WHITE);
        SwingUtilities.updateComponentTreeUI(this);
    }
}
