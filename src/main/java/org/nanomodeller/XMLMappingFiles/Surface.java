package org.nanomodeller.XMLMappingFiles;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.nanomodeller.Tools.StringUtils;

import java.awt.*;

import static org.nanomodeller.GUI.Dialogs.ColorDialog.convertColorToString;

@XmlRootElement(name="Surface")
public class Surface extends XMLTemplate {

    public Surface() {
        color = convertColorToString(Color.BLACK);
    }

    public float getFloat(String key) {
        String textVal = getString(key);
        return StringUtils.isEmpty(textVal) ? 0 : Float.parseFloat(textVal);
    }
}
