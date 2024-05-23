package org.nanomodeller.GUI.Dialogs;

import com.bric.colorpicker.ColorPicker;
import org.nanomodeller.GUI.ViewComponents.MyButton;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.Bond;
import org.nanomodeller.XMLMappingFiles.Element;

import javax.swing.*;
import java.awt.*;

public class ColorDialog extends JDialog {
    private String selectedColor;

    private ElementPropertiesDialog parent;

    Color originalColor;

    Element element;
    JPanel contentPane = new JPanel();
    JPanel colorPane = new ColorPicker();

    JPanel butonPanel = new JPanel();

    JButton restoreButton = new MyButton("Restore");

    JButton okButton = new MyButton("OK");
    JButton cancelButton = new MyButton("Cancel");

    public ColorDialog (Element element, ElementPropertiesDialog parent, String rgb){
        this.parent = parent;
        this.element = element;
        initialize(rgb);
    }
    private void initialize(String rgb) {
        if (StringUtils.isEmpty(rgb)){
            originalColor = Color.WHITE;
        }
        setTitle("Color chooser");
        setContentPane(contentPane);
        contentPane.add(colorPane);
        butonPanel.add(okButton);
        butonPanel.add(restoreButton);
        butonPanel.add(cancelButton);
        butonPanel.setLayout(new FlowLayout());
        contentPane.add(butonPanel);
        setMinimumSize(new Dimension(colorPane.getPreferredSize().width,colorPane.getPreferredSize().height + 3* okButton.getPreferredSize().height));
        originalColor = convertStringToColor(rgb);
        ((ColorPicker) colorPane).setColor(originalColor);

        okButton.addActionListener(
                e -> {
                    Color color = ((ColorPicker) colorPane).getColor();
                    selectedColor = convertColorToString(color);
                    String text = parent.getEditorPane().getText();
                    if (StringUtils.isNotEmpty(text) && text.contains("<color>")) {
                        String repl = text.replaceAll("<color>.*?</color>", String.format("<color>%s</color>", selectedColor));
                        parent.getEditorPane().setText(repl);
                    }else{
                        String toReplace;
                        if (element instanceof Atom){
                            toReplace = "<Atom>";
                        }else if (element instanceof Bond){
                            toReplace = "<Bond>";
                        }
                        else {
                            toReplace = "<Electrode>";
                        }
                        String repl = text.replace(toReplace, String.format("%s\n" +
                                "    <color>%s</color>", toReplace, selectedColor));
                        parent.getEditorPane().setText(repl);
                    }

                    dispose();
                    setVisible(false);
                    parent.setVisible(true);

                }
        );
        cancelButton.addActionListener(e -> {
            parent.setVisible(true);
            setVisible(false);
            dispose();
        });
        restoreButton.addActionListener(e -> ((ColorPicker) colorPane).setColor(originalColor));
    }

    private static String convertColorToString(Color color) {
        return String.format("%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color convertStringToColor(String rgbString) {
        String[] rgbValues = rgbString.replaceAll("\\s", "").split(",");
        int red = Integer.parseInt(rgbValues[0].trim());
        int green = Integer.parseInt(rgbValues[1].trim());
        int blue = Integer.parseInt(rgbValues[2].trim());
        return new Color(red, green, blue);
    }

    public void showDialog() {
        setVisible(true);
    }

    public String getSelectedColor() {
        return selectedColor;
    }
}
