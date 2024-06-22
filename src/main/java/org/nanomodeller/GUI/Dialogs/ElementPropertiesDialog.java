package org.nanomodeller.GUI.Dialogs;

import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ElementPropertiesDialog extends JDialog {
    private JPanel contentPane;

    private XMLTemplate template;
    private JButton cancelButton;
    private JButton applyToGroupButton;
    private JButton addGroupIDButton;
    private JButton addPropertyButton;
    private JButton changeColorButton;
    private JButton validateButton;
    private JEditorPane editorPane;
    private JButton applyToAllButton;
    private JButton applyButton;
    private JPanel editPanel;
    private JPanel colorPanel;
    private JScrollPane scrollPane;

    public ElementPropertiesDialog(XMLTemplate temp, String textAreaContent) {

        this.template = temp;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(cancelButton);
        editorPane.setText(textAreaContent);
        setTitle("Properties");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension((int)screenSize.getWidth()/2, (int)(screenSize.getHeight()/1.3)));
        boolean isStructureElement = template instanceof StructureElement;
        cancelButton.addActionListener(e -> dispose());
        if (isStructureElement){
            applyToGroupButton.addActionListener(e -> {
                XMLTemplate convertedStructureElement = XMLHelper.convertXMLStringToElement(editorPane.getText(), template.getClass());
                String GID = ((StructureElement)convertedStructureElement).getGroupID();
                ((StructureElement)template).setGroupID(GID);
                template.setProperties(convertedStructureElement.getProperties());
                template.setColor(convertedStructureElement.getColor());
                Predicate<StructureElement> filter = el -> StringUtils.equals(el.getGroupID(), GID);
                Consumer<StructureElement> action = el -> {
                    el.setProperties(template.getProperties());
                    el.setColor(template.getColor());};
                if (convertedStructureElement instanceof Atom){
                    NanoModeler.getInstance().getAtoms().values().stream()
                            .filter(filter)
                            .forEach(action);
                }else if(convertedStructureElement instanceof Bond){
                    NanoModeler.getInstance().getBonds().stream().filter(filter).forEach(action);
                }else if(convertedStructureElement instanceof Electrode){
                    NanoModeler.getInstance().getElectrodes().values().stream()
                            .filter(filter)
                            .forEach(action);
                }
                ((Component)NanoModeler.getInstance().getPaintSurface()).repaint();
                dispose();
            });
            addGroupIDButton.addActionListener(e -> {
                String text = editorPane.getText();
                if (!text.contains("<GID>")){
                    String toReplace;
                    if (template instanceof Atom){
                        toReplace = "<Atom>";
                    }else if (template instanceof Bond){
                        toReplace = "<Bond>";
                    }
                    else {
                        toReplace = "<Electrode>";
                    }
                    editorPane.setText(text.replace(toReplace, toReplace  + "\n    <GID></GID>"));
                }
            });
            applyToAllButton.addActionListener(e -> {
                XMLTemplate convertedStructureElement = XMLHelper.convertXMLStringToElement(editorPane.getText(), template.getClass());
                Consumer<StructureElement> action = el -> {
                    el.setProperties(convertedStructureElement.getProperties());
                    el.setColor(convertedStructureElement.getColor());};
                if (convertedStructureElement instanceof Atom){
                    NanoModeler.getInstance().getAtoms().values().stream()
                            .forEach(action);
                }else if(convertedStructureElement instanceof Bond) {
                    NanoModeler.getInstance().getBonds().stream().forEach(action);
                }else if(convertedStructureElement instanceof Electrode){
                    NanoModeler.getInstance().getElectrodes().values().stream()
                            .forEach(action);
                }
                ((Component)NanoModeler.getInstance().getPaintSurface()).repaint();
                dispose();
            });
        }

        applyToGroupButton.setVisible(isStructureElement);
        addGroupIDButton.setVisible(isStructureElement);
        applyToAllButton.setVisible(isStructureElement);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        applyButton.addActionListener(e -> {
            XMLTemplate convertedStructureElement = XMLHelper.convertXMLStringToElement(editorPane.getText(), template.getClass());
            template.setProperties(convertedStructureElement.getProperties());
            if (template instanceof StructureElement){
                ((StructureElement)template).setGroupID(((StructureElement)convertedStructureElement).getGroupID());
                ((StructureElement)template).setTag(((StructureElement)convertedStructureElement).getTag());
            }
            template.setColor(convertedStructureElement.getColor());

            ((Component)NanoModeler.getInstance().getPaintSurface()).repaint();
            dispose();
        });
        addPropertyButton.addActionListener(e -> {
            String text = editorPane.getText();
            if (text.contains("<properties>")){
                String newProperty ="<properties>\n" +
                        "        <entry>\n" +
                        "            <key></key>\n" +
                        "            <value></value>\n" +
                        "        </entry>";
                editorPane.setText(text.replace("<properties>", newProperty));
            }else {
                String toReplace = "";
                if (template instanceof Atom){
                    toReplace = "<Atom>";
                }else if (template instanceof Bond){
                    toReplace = "<Bond>";
                }
                else if (template instanceof Electrode){
                    toReplace = "<Electrode>";
                }
                else if (template instanceof Surface){
                    toReplace = "<Surface>";
                }
                String properties = toReplace + "\n" +
                        "    <properties>\n" +
                        "        <entry>\n" +
                        "            <key></key>\n" +
                        "            <value></value>\n" +
                        "        </entry>\n" +
                        "    </properties>";
                editorPane.setText(text.replace(toReplace, properties));
            }
        });
        changeColorButton.addActionListener(e -> {
            String text = editorPane.getText();
            String color = "255,255,255";
            if (text.contains("<color>") && text.contains("</color>")){
                int begin = text.indexOf("<color>");
                int end = text.indexOf("</color>");
                String value = text.substring(begin+7, end);
                if (StringUtils.isNotEmpty(value)){
                    color = value;
                }
            }
            ColorDialog colorDialog = new ColorDialog(template, this, color);
            this.setVisible(false);
            colorDialog.showDialog();
        });
    }

    public JEditorPane getEditorPane() {
        return editorPane;
    }
}
