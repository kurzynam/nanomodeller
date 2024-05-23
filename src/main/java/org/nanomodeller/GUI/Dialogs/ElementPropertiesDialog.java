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

    private Element element;
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

    public ElementPropertiesDialog(Element element, String textAreaContent) {

        this.element = element;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(cancelButton);
        editorPane.setText(textAreaContent);
        setTitle("Properties");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension((int)screenSize.getWidth()/2, (int)(screenSize.getHeight()/1.3)));
        cancelButton.addActionListener(e -> dispose());
        applyToGroupButton.addActionListener(e -> {
            Element convertedElement = XMLHelper.convertXMLStringToElement(editorPane.getText(), element.getClass());
            String GID = convertedElement.getGroupID();
            element.setGroupID(GID);
            element.setProperties(convertedElement.getProperties());
            element.setColor(convertedElement.getColor());
            Predicate<Element> filter = el -> StringUtils.equals(el.getGroupID(), GID);
            Consumer<Element> action = el -> {
                el.setProperties(element.getProperties());
                el.setColor(element.getColor());};
            if (convertedElement instanceof Atom){
                NanoModeler.getInstance().getAtoms().values().stream()
                        .filter(filter)
                        .forEach(action);
            }else if(convertedElement instanceof Bond){
                NanoModeler.getInstance().getBonds().stream().filter(filter).forEach(action);
            }else if(convertedElement instanceof Electrode){
                NanoModeler.getInstance().getElectrodes().values().stream()
                        .filter(filter)
                        .forEach(action);
            }
            ((Component)NanoModeler.getInstance().getPaintSurface()).repaint();
            dispose();
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        addGroupIDButton.addActionListener(e -> {
            String text = editorPane.getText();
            if (!text.contains("<GID>")){
                String toReplace;
                if (element instanceof Atom){
                    toReplace = "<Atom>";
                }else if (element instanceof Bond){
                    toReplace = "<Bond>";
                }
                else {
                    toReplace = "<Electrode>";
                }
                editorPane.setText(text.replace(toReplace, toReplace  + "\n    <GID></GID>"));
            }
        });
        applyButton.addActionListener(e -> {
            Element convertedElement = XMLHelper.convertXMLStringToElement(editorPane.getText(), element.getClass());
            element.setProperties(convertedElement.getProperties());
            element.setColor(convertedElement.getColor());
            element.setGroupID(convertedElement.getGroupID());
            element.setTag(convertedElement.getTag());
            ((Component)NanoModeler.getInstance().getPaintSurface()).repaint();
            dispose();
        });
        applyToAllButton.addActionListener(e -> {
            Element convertedElement = XMLHelper.convertXMLStringToElement(editorPane.getText(), element.getClass());
            Consumer<Element> action = el -> {
                el.setProperties(convertedElement.getProperties());
                el.setColor(convertedElement.getColor());};
            if (convertedElement instanceof Atom){
                NanoModeler.getInstance().getAtoms().values().stream()
                        .forEach(action);
            }else if(convertedElement instanceof Bond) {
                NanoModeler.getInstance().getBonds().stream().forEach(action);
            }else if(convertedElement instanceof Electrode){
                NanoModeler.getInstance().getElectrodes().values().stream()
                        .forEach(action);
            }
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
                String toReplace;
                if (element instanceof Atom){
                    toReplace = "<Atom>";
                }else if (element instanceof Bond){
                    toReplace = "<Bond>";
                }
                else {
                    toReplace = "<Electrode>";
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
            String color = "0,0,0";
            if (text.contains("<color>") && text.contains("</color>")){
                int begin = text.indexOf("<color>");
                int end = text.indexOf("</color>");
                String value = text.substring(begin+7, end);
                if (StringUtils.isNotEmpty(value)){
                    color = value;
                }
            }
            ColorDialog colorDialog = new ColorDialog(element, this, color);
            this.setVisible(false);
            colorDialog.showDialog();
        });
    }

    public JEditorPane getEditorPane() {
        return editorPane;
    }
}
