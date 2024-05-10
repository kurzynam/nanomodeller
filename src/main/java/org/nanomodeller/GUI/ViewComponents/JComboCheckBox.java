package org.nanomodeller.GUI.ViewComponents;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class JComboCheckBox extends JComboBox
{

    public JComboCheckBox() {
        init();
    }

    @Override
    public void setPopupVisible(boolean v) {
    }
    @Override
    public void setSelectedItem(Object o) {
        if (o instanceof JCheckBox) {
            JCheckBox checkBox = ((JCheckBox) o);
            boolean value = checkBox.isSelected();
            checkBox.setSelected(!value);
            checkBox.updateUI();
        }

    }

    public JComboCheckBox(JCheckBox[] items) {
        super(items);
        init();
    }

    public JComboCheckBox(Vector items) {
        super(items);
        init();
    }

    public JComboCheckBox(ComboBoxModel aModel) {
        super(aModel);
        init();
    }

    private void init() {
        setRenderer(new ComboBoxRenderer());
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                itemSelected();
            }
        });
    }

    public void setLDOS(boolean value){
        ((JCheckBox)(this.getItemAt(1))).setSelected(value);
    }
    public void setNormalisation(boolean value){
        ((JCheckBox)(this.getItemAt(2))).setSelected(value);
    }
    public boolean getLDOS(){
        return ((JCheckBox)(this.getItemAt(1))).isSelected();
    }
    public boolean getNormalisation(){
        return ((JCheckBox)(this.getItemAt(2))).isSelected();
    }
    private void itemSelected() {
        if (getSelectedItem() instanceof JCheckBox) {
            JCheckBox jcb = (JCheckBox)getSelectedItem();
            jcb.setSelected(!jcb.isSelected());
        }
    }

    class ComboBoxRenderer implements ListCellRenderer {
        private JLabel label;

        public ComboBoxRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Component) {
                Component c = (Component)value;
                if (isSelected) {
                    c.setBackground(list.getSelectionBackground());
                    c.setForeground(list.getSelectionForeground());
                } else {
                    c.setBackground(list.getBackground());
                    c.setForeground(list.getForeground());
                }

                return c;
            } else {
                if (label == null) {
                    label = new JLabel(value.toString());
                }
                else {
                    label.setText(value.toString());
                }

                return label;
            }
        }
    }
}