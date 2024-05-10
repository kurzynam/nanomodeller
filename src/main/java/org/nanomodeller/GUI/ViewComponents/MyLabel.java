package org.nanomodeller.GUI.ViewComponents;

import javax.swing.*;
import java.awt.*;

public class MyLabel extends JLabel {
    public MyLabel(String text){
        super(text);
        setFont(new Font("Consolas", Font.PLAIN, 20));
    }
}
