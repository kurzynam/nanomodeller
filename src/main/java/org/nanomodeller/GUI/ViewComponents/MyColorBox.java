package org.nanomodeller.GUI.ViewComponents;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.*;

public class MyColorBox extends JComboBox {

    public Hashtable<String, Color> colors;

    public MyColorBox() {
        super();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        Enumeration colorNames = addColors().keys();
        while (colorNames.hasMoreElements()) {
            String temp = colorNames.nextElement().toString();
            model.addElement(temp);
        }
        setModel(model);
        setRenderer(new ColorRenderer());
        this.setOpaque(true);
        this.setSelectedIndex(0);
    }

    @Override
    public void setSelectedItem(Object anObject) {
        super.setSelectedItem(anObject);

        setBackground((Color) colors.get(anObject));
        setFont(new Font("Consolas", Font.PLAIN, 20));
        if (isDarkColor(anObject.toString())) {
            setForeground(Color.white);
        }
        else{
            setForeground(Color.black);
        }
    }
    public boolean isDarkColor(String color) {
        return color.equals("BLACK") || color.equals("DARK GRAY")
                || color.equals("BROWN")
                || color.equals("BLUE")
                || color.equals("PURPLE")
                || color.equals("MAGENTA")
                || color.equals("DARK GREEN");
    }

    public Color getSelectedColor() {

        return this.getBackground();
    }

    private Hashtable addColors() {

        colors = new <String, Color>Hashtable();

        colors.put("WHITE", Color.WHITE);
        colors.put("BLUE", Color.BLUE);
        colors.put("GREEN", Color.GREEN);
        colors.put("YELLOW", Color.YELLOW);
        colors.put("ORANGE", Color.ORANGE);
        colors.put("CYAN", Color.CYAN);
        colors.put("DARK GRAY", Color.DARK_GRAY);
        colors.put("GRAY", Color.GRAY);
        colors.put("RED", Color.RED);
        colors.put("PINK", Color.PINK);
        colors.put("MAGENTA", Color.MAGENTA);
        colors.put("BLACK", Color.BLACK);
        colors.put("PURPLE", new Color(0xB406F0));
        colors.put("DARK GREEN", new Color(0x00B010));
        colors.put("DARK YELLOW", new Color(0xD8CF07));
        colors.put("BROWN", new Color(0x8F4106));
        return colors;
    }
    class ColorRenderer extends MyLabel implements javax.swing.ListCellRenderer {
        public ColorRenderer() {
            super("");
            this.setOpaque(true);
        }
        public Component getListCellRendererComponent(JList list, Object key, int index,
                                                      boolean isSelected, boolean cellHasFocus) {

            Color color = colors.get(key);;
            String name = key.toString();

            list.setSelectionBackground(null);
            list.setSelectionForeground(null);

            if(isSelected){
                setBorder(BorderFactory.createEtchedBorder());
            } else {
                setBorder(null);
            }
            setFont(new Font("Consolas", Font.PLAIN, 20));
            setBackground(color);
            setText(name);
            setForeground(Color.black);
            if(isDarkColor(name)){
                setForeground(Color.white);
            }
            else{
                setForeground(Color.black);
            }

            return this;
        }
    }
}
