package org.nanomodeller.GUI.ViewComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MyTextField extends JTextField implements MouseListener {
    public MyTextField(){
        intiializeComponent();
    }
    public MyTextField(String text){
        super(text);
        intiializeComponent();
    }

    private void intiializeComponent() {
        addMouseListener(this);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)screenSize.getWidth();
        int height = (int)screenSize.getHeight();
        setFont(new Font("Consolas", Font.PLAIN, height/60));
        setPreferredSize(new Dimension(width/30,height/50));
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        boolean isRightMouseButton = SwingUtilities.isRightMouseButton(e);
        if (isRightMouseButton){
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int)screenSize.getWidth();
            int height = (int)screenSize.getHeight();
            JTextArea tf = new JTextArea(this.getText());
            tf.setFont(new Font("Consolas", Font.PLAIN, 20));
            tf.setPreferredSize(new Dimension(width/3,height/14));
            tf.setLineWrap(true);
            int result = JOptionPane.showConfirmDialog(
                    null, tf, "Input",
                    JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                this.setText(tf.getText());
            }

        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }


    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
