package org.nanomodeller.XMLMappingFiles;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="CommonProperties")
public class CommonProperties extends XMLTemplate{
    public static CommonProperties getInstance(){
        return GlobalProperties.getInstance().getCommonProperties();
    }
}
