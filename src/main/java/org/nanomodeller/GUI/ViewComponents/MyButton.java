package org.nanomodeller.GUI.ViewComponents;

import javax.swing.*;
import java.awt.*;

public class MyButton extends JButton{
    public MyButton(String text){
        super(text);
        initializeButton(text);
    }
    public MyButton(String text, ImageIcon image){
        super(text);
        setImageIcon(image);
        initializeButton(text);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(Color.GRAY);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(Color.DARK_GRAY);
            }
        });
    }

    public void setImageIcon(ImageIcon image) {
        if (image != null) {
            Image img = image.getImage();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Image newimg = img.getScaledInstance((int)(screenSize.getHeight()/44), (int)(screenSize.getHeight()/44), Image.SCALE_SMOOTH);
            ImageIcon imIc = new ImageIcon(newimg);
            setIcon(imIc);
        }
    }

    private void initializeButton(String text) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setHorizontalAlignment(SwingConstants.LEFT);
        setBackground(Color.DARK_GRAY);
        setForeground(Color.WHITE);
        setFont(new Font("Consolas", Font.PLAIN, 16));
        setPreferredSize(new Dimension((int)(screenSize.getWidth()/12),(int)(screenSize.getHeight()/40)));
        setMargin(new Insets(0, 0, 0, 0));
    }
}
