package org.nanomodeller.GUI.ViewComponents;

import javax.swing.*;
import java.awt.*;

public class MyMenuItem extends JMenuItem {

    public MyMenuItem(String text){
        super(text);
        setFont(new Font("Consolas", Font.PLAIN, 22));
        setMargin(new Insets(0, 0, 0, 0));
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
    }
    public MyMenuItem(String text, ImageIcon image){
        super(text);
        Image img = image.getImage() ;
        Image newimg = img.getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH );
        setFont(new Font("Consolas", Font.PLAIN, 22));
        setMargin(new Insets(0, 0, 0, 0));
        ImageIcon imIc = new ImageIcon(newimg);
        setIcon(imIc);
    }
    public MyMenuItem(String text, ImageIcon image, int width, int height){
        super(text);
        Image img = image.getImage() ;
        Image newimg = img.getScaledInstance( width, height,  java.awt.Image.SCALE_SMOOTH );
        setFont(new Font("Consolas", Font.PLAIN, 22));
        setMargin(new Insets(0, 0, 0, 0));
        ImageIcon imIc = new ImageIcon(newimg);
        setIcon(imIc);
    }

    public MyMenuItem(String text, String path, int width, int height){
        super(text);
        Image img = new ImageIcon(path).getImage();
        Image newimg = img.getScaledInstance( width, height,  java.awt.Image.SCALE_SMOOTH );
        setFont(new Font("Consolas", Font.PLAIN, 22));
        setMargin(new Insets(0, 0, 0, 0));
        ImageIcon imIc = new ImageIcon(newimg);
        setIcon(imIc);
    }


}
