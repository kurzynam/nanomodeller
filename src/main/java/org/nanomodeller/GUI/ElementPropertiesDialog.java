package org.nanomodeller.GUI;

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

    public ElementPropertiesDialog(Element element,String title, String textAreaContent) {

        this.element = element;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(cancelButton);
        editorPane.setText(textAreaContent);
        setTitle(title);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension((int)screenSize.getWidth()/2, (int)(screenSize.getHeight()/1.3)));
        cancelButton.addActionListener(e -> dispose());
        applyToGroupButton.addActionListener(e -> {
            Element convertedElement = XMLHelper.convertXMLStringToAtom(editorPane.getText());
            String GID = convertedElement.getGroupID();
            element.setGroupID(GID);
            element.setProperties(convertedElement.getProperties());
            element.setColor(convertedElement.getColor());
            Predicate<Element> filter = el -> StringUtils.equals(el.getGroupID(), GID);
            Consumer<Element> action = el -> {
                el.setProperties(element.getProperties());
                el.setColor(element.getColor());};
            if (convertedElement instanceof Atom){
                NanoModeler.getInstance().getShapes().stream().map(shape -> shape.getAtom())
                        .filter(filter)
                        .forEach(action);
            }else if(convertedElement instanceof Bond){
                NanoModeler.getInstance().getAtomBonds().stream().map(shape -> shape.getBond())
                        .filter(filter)
                        .forEach(action);
            }else if(convertedElement instanceof Electrode){
                NanoModeler.getInstance().getElectrodes().stream().map(shape -> shape.getElectrode())
                        .filter(filter)
                        .forEach(action);
            }
            dispose();
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // call onCancel() on ESCAPE
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
            Element convertedElement = XMLHelper.convertXMLStringToAtom(editorPane.getText());
            element.setProperties(convertedElement.getProperties());
            element.setColor(convertedElement.getColor());
            element.setGroupID(convertedElement.getGroupID());
            dispose();
        });
        applyToAllButton.addActionListener(e -> {
            Element convertedElement = XMLHelper.convertXMLStringToAtom(editorPane.getText());
            Consumer<Element> action = el -> {
                el.setProperties(element.getProperties());
                el.setColor(element.getColor());};
            if (convertedElement instanceof Atom){
                NanoModeler.getInstance().getShapes().stream().map(shape -> shape.getAtom())
                        .forEach(action);
            }else if(convertedElement instanceof Bond){
                NanoModeler.getInstance().getAtomBonds().stream().map(shape -> shape.getBond())
                        .forEach(action);
            }else if(convertedElement instanceof Electrode){
                NanoModeler.getInstance().getElectrodes().stream().map(shape -> shape.getElectrode())
                        .forEach(action);
            }
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
    }
}
