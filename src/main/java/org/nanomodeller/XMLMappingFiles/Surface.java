package org.nanomodeller.XMLMappingFiles;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.awt.*;

import static org.nanomodeller.GUI.Dialogs.ColorDialog.convertColorToString;

@XmlRootElement(name="Surface")
public class Surface extends XMLTemplate {

    public Surface() {
        color = convertColorToString(Color.BLACK);
    }
}
