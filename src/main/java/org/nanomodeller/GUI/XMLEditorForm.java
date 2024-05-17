package org.nanomodeller.GUI;

import org.nanomodeller.GUI.ViewComponents.FuturisticScrollBar;

import javax.swing.text.Document;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class XMLEditorForm extends JFrame{
    private JPanel mainPanel;
    private JButton addGIDbutton;
    private JButton button2;
    private JButton cancelButton;
    private JButton applybutton;
    private JButton applyToAllButton;
    private JButton applyToGroupButton;
    private JButton propertyButton;
    private JEditorPane editorPane;
    private JPanel rightPanel;
    private JPanel leftPanel;
    private JPanel downPanel;
    private JPanel upPanel;
    private JScrollPane scrollPane;




    public XMLEditorForm() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        initializeButtons();
        setContentPane(mainPanel);
        leftPanel.setBackground(Color.BLACK);
        rightPanel.setBackground(Color.BLACK);
        upPanel.setBackground(Color.BLACK);
        downPanel.setBackground(Color.BLACK);
        setMinimumSize(new Dimension(200,340));
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        applybutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        applyToGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        applyToAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        addGIDbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        propertyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        show();
    }


    private void initializeButtons() {
        JButton[] buttons = {propertyButton, addGIDbutton,
                applybutton, applyToAllButton,
                button2, cancelButton, applyToGroupButton};
        for (JButton button : buttons){
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setBackground(Color.DARK_GRAY);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Consolas", Font.BOLD, 16));
            button.setPreferredSize(new Dimension((int)(screenSize.getWidth()/12),(int)(screenSize.getHeight()/20)));
            button.setMargin(new Insets(0, 0, 0, 0));
        }
        scrollPane.getVerticalScrollBar().setUI(new FuturisticScrollBar().new FuturisticScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new FuturisticScrollBar().new FuturisticScrollBarUI());

    }

    private void initializeTextPane(){
        editorPane.setEditorKitForContentType("text/xml", new XmlEditorKit());
        editorPane.setContentType("text/xml");
    }


    static class XmlEditorKit extends HTMLEditorKit {
        @Override
        public Document createDefaultDocument() {
            StyleSheet styles = getStyleSheet();
            styles.addRule("xml { color: #888888; }"); // Styl dla elementów XML
            styles.addRule("tag { color: #0000FF; }"); // Styl dla tagów
            styles.addRule("attribute { color: #FF0000; }"); // Styl dla atrybutów
            styles.addRule("value { color: #008000; }"); // Styl dla wartości atrybutów

            return super.createDefaultDocument();
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JButton getAddGIDbutton() {
        return addGIDbutton;
    }

    public JButton getButton2() {
        return button2;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JButton getApplybutton() {
        return applybutton;
    }

    public JButton getApplyToAllButton() {
        return applyToAllButton;
    }

    public JButton getApplyToGroupButton() {
        return applyToGroupButton;
    }

    public JButton getPropertyButton() {
        return propertyButton;
    }

    public JEditorPane getEditorPane() {
        return editorPane;
    }

    public JPanel getRightPanel() {
        return rightPanel;
    }

    public JPanel getLeftPanel() {
        return leftPanel;
    }

    public JPanel getDownPanel() {
        return downPanel;
    }

    public JPanel getUpPanel() {
        return upPanel;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }
}
