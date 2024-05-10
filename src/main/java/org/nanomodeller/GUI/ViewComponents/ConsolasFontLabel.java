package org.nanomodeller.GUI.ViewComponents;

import javax.swing.*;
import java.awt.*;

public class ConsolasFontLabel extends JLabel {
    public ConsolasFontLabel(Color color, String text, int textSize){
        super(text);
        setForeground(color);
        setFont(new Font("Consolas", Font.PLAIN, textSize));
    }
}
