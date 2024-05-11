package org.nanomodeller.GUI;


import org.nanomodeller.GUI.ViewComponents.ConsolasFontLabel;
import org.nanomodeller.GUI.ViewComponents.MyButton;
import org.nanomodeller.GUI.ViewComponents.MyColorBox;
import org.nanomodeller.GUI.ViewComponents.MyTextField;
import org.nanomodeller.Globals;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.GlobalProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;

import static org.nanomodeller.Globals.BACKGROUND_IMAGE_PATH;


public class PaletteChooserFrame extends JFrame {

    GlobalProperties gp;

    public PaletteChooserFrame(GlobalProperties gp){
        super("Options");
        this.gp = gp;
        setBackground(new Color(1));
        this.getContentPane().setBackground(Color.black);
        setSize(500,500);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setContentPane(new OptionsPanel());
        repaint();
        show();
    }

    private class OptionsPanel extends JPanel {


        ConsolasFontLabel colorLabel;
        ConsolasFontLabel valueLabel;

        MyColorBox[] colorBoxes;
        MyTextField[] valueTextFields;
        JButton okButton;
        JButton cancelButton;

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);
            ImageIcon im = new ImageIcon(BACKGROUND_IMAGE_PATH);
            g.drawImage(im.getImage(), 0, 0, null);
        }


        public OptionsPanel(){


            initializeComponents();
            GridBagLayout layout = new GridBagLayout();
            setLayout(layout);
            GridBagConstraints pointer = new GridBagConstraints();
            pointer.fill = GridBagConstraints.HORIZONTAL;
            pointer.weightx = 0.5;
            pointer.weighty = 0.5;
            pointer.gridx = 0;
            pointer.gridy = 0;

            add(valueLabel, pointer);
            for (MyTextField textField : valueTextFields) {
                pointer.gridy++;
                add(textField, pointer);
            }
            pointer.gridy++;
            add(okButton, pointer);

            pointer.gridx = 1;
            pointer.gridy = 0;

            add(colorLabel, pointer);
            for (MyColorBox colorBox : colorBoxes) {
                pointer.gridy++;
                add(colorBox, pointer);
            }
            pointer.gridy++;
            add(cancelButton, pointer);


        }
        private void initializeComponents(){

            ArrayList<String> sortedKeys = new ArrayList(gp.getPaletteColors().keySet());
            Collections.sort(sortedKeys);
            colorBoxes = new MyColorBox[15];
            for (int i =0; i < colorBoxes.length; i++) {
                colorBoxes[i] = new MyColorBox();
                if (i < sortedKeys.size()){
                    String key = sortedKeys.get(i);
                    colorBoxes[i].setSelectedItem(gp.getPaletteColors().get(key));
                }
                else{
                    colorBoxes[i].setSelectedItem(Globals.WHITE);
                }
            }
            valueTextFields = new MyTextField[15];
            for (int i =0; i < valueTextFields.length; i++) {
                valueTextFields[i] = new MyTextField();
                if (i < sortedKeys.size()){
                    String key = sortedKeys.get(i);
                    valueTextFields[i].setText(key);
                }
            }
            valueLabel = new ConsolasFontLabel(Color.BLACK,"Fraction of Zmax", 16);
            colorLabel = new ConsolasFontLabel(Color.BLACK,"Color", 16);

            okButton=new MyButton("Apply");
            cancelButton=new MyButton("Exit");

            okButton.addActionListener((ActionEvent event) -> {
                gp.getPaletteColors().clear();
                for (int i = 0; i < valueTextFields.length; i++){
                    String text = valueTextFields[i].getText();
                    if (StringUtils.isNotEmpty(text)){
                        String color = colorBoxes[i].getSelectedItem() + "";
                        gp.getPaletteColors().put(text, color);
                    }
                }
                //convertObjectToXML(gp);
                PaletteChooserFrame.this.dispose();
            });
            cancelButton.addActionListener((ActionEvent event) -> {
                PaletteChooserFrame.this.dispose();
            });
        }
    }


}
