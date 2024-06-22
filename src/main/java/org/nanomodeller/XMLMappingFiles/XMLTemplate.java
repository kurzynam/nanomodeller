package org.nanomodeller.XMLMappingFiles;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Hashtable;

public class XMLTemplate {
    protected Hashtable<String, String> properties;
    protected String color;
    @XmlAttribute(name="color")
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }

    public String getString(String key){
        return properties.get(key);
    }
    public Double getDouble(String key){
        return Double.parseDouble(getString(key));
    }
    public Integer getInt(String key){
        return Integer.parseInt(getString(key));
    }


    @XmlElements(@XmlElement(name="Property"))
    public Hashtable<String, String> getProperties() {
        return properties;
    }
    public void setProperties(Hashtable<String, String> properties) {
        this.properties = properties;
    }
}
