package org.nanomodeller.GUI.Dialogs;

import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
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
    private JButton variablesButton;
    private JPanel colorPanel;
    private JScrollPane scrollPane;

    public ElementPropertiesDialog(XMLTemplate temp, String textAreaContent) {

        this.template = temp;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(cancelButton);
        editorPane.setText(textAreaContent);
        editorPane.setAutoscrolls(true);
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
                template.setVariables(convertedStructureElement.getVariables());
                template.setColor(convertedStructureElement.getColor());
                Predicate<StructureElement> filter = el -> StringUtils.equals(el.getGroupID(), GID);
                Consumer<StructureElement> action = el -> {
                    el.setProperties(template.getProperties());
                    el.setVariables(template.getVariables());
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
                    el.setVariables(convertedStructureElement.getVariables());
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
            XMLTemplate convertedElement = XMLHelper.convertXMLStringToElement(editorPane.getText(), template.getClass());
            template.setProperties(convertedElement.getProperties());
            template.setVariables(convertedElement.getVariables());
            if (template instanceof StructureElement){
                ((StructureElement)template).setGroupID(((StructureElement)convertedElement).getGroupID());
                ((StructureElement)template).setTag(((StructureElement)convertedElement).getTag());
            } else if (template instanceof PlotOptions) {
                PlotOptions optConv = ((PlotOptions)convertedElement);
                GlobalProperties.getInstance().setPlotOptions(optConv);
            } else if (template instanceof CommonProperties) {
                CommonProperties propConv = ((CommonProperties)convertedElement);
            }
            template.setColor(convertedElement.getColor());

            ((Component)NanoModeler.getInstance().getPaintSurface()).repaint();
            dispose();
        });


        variablesButton.addActionListener( e -> {
            String text = editorPane.getText();
            if (text.contains("<variables>")){
                String newVariable ="<variables>\n" +
                        "        <entry>\n" +
                        "            <key></key>\n" +
                        "            <value>\n" +
                        "                <incr></incr>\n" +
                        "                <max></max>\n" +
                        "                <min></min>\n" +
                        "            </value>\n" +
                        "        </entry>";
                editorPane.setText(text.replace("<variables>", newVariable));
            }else {
                String toReplace = getNameOfElementToReplace();
                String variables = toReplace + "\n" +
                        "    <variables>\n" +
                        "        <entry>\n" +
                        "            <key></key>\n" +
                        "            <value>\n" +
                        "                <incr></incr>\n" +
                        "                <max></max>\n" +
                        "                <min></min>\n" +
                        "            </value>\n" +
                        "        </entry>\n" +
                        "    </variables>";
                editorPane.setText(text.replace(toReplace, variables));
            }
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
                toReplace = getNameOfElementToReplace();
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

    private String getNameOfElementToReplace() {
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
        else if (template instanceof PlotOptions){
            toReplace = "<PlotOptions>";
        }
        else if (template instanceof CommonProperties){
            toReplace = "<CommonProperties>";
        }
        return toReplace;
    }

    public JEditorPane getEditorPane() {
        return editorPane;
    }
}
