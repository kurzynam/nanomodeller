package org.nanomodeller.XMLMappingFiles;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="CommonProperties")
public class CommonProperties extends XMLTemplate implements Cloneable{
    public static CommonProperties getInstance(){
        return GlobalProperties.getInstance().getCommonProperties();
    }

    @Override
    public CommonProperties clone(){
        try {
            return (CommonProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
