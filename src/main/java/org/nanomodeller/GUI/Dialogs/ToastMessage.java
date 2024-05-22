package org.nanomodeller.GUI.Dialogs;

import org.nanomodeller.GUI.NanoModeler;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ToastMessage extends JDialog {
    int miliseconds;

    public ToastMessage(String toastString, int time, NanoModeler sm) {
        this.miliseconds = time;
        setUndecorated(true);
        getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        panel.setBackground(Color.GRAY);
        panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
        getContentPane().add(panel, BorderLayout.CENTER);

        JLabel toastLabel = new JLabel("");
        toastLabel.setText(toastString);
        toastLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        toastLabel.setForeground(Color.WHITE);

        setBounds(100, 100, toastLabel.getPreferredSize().width + 20, 31);


        setAlwaysOnTop(true);
        int x = sm.getLocation().x + 5 * sm.getWidth() / 11;
        int y = sm.getLocation().y + 8 * sm.getHeight() / 10;
        setLocation(x,y);
        panel.add(toastLabel);
        setVisible(false);

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(miliseconds);
                    dispose();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}