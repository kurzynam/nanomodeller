package org.nanomodeller.GUI.ViewComponents;

import org.nanomodeller.GUI.Menus.Menu;

import java.awt.Color;
import javax.swing.JFrame;


public class MainFrame extends JFrame {

    private static MainFrame instance=null;

    public static MainFrame getInstance() {
        if(instance == null) {
            instance = new MainFrame();
        }
        return instance;
    }
    public MainFrame(){
        setBackground(Color.black);
        this.getContentPane().setBackground(Color.black);
        setSize(500,560);
        setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        repaint();
        show();
        setJMenuBar(new Menu(null));
    }


}

