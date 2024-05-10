package org.nanomodeller.GUI.ViewComponents;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MyPanel extends JPanel {
    public JLabel parentLabel;
    public MyPanel(String backgroundImagePath){
        super.setBackground(Color.BLACK);
        try {
            parentLabel=new JLabel(new ImageIcon(ImageIO.read(new File(backgroundImagePath))));
        } catch (IOException e) {
            parentLabel=new JLabel();
        }
        parentLabel.setLayout(new FlowLayout());
        add(parentLabel);
    }
}
