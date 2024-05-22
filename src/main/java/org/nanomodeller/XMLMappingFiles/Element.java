package org.nanomodeller.XMLMappingFiles;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import org.nanomodeller.Calculation.CalculationItem;
import org.nanomodeller.Tools.PropertiesMap;
import org.nfunk.jep.JEP;

import javax.xml.bind.annotation.XmlAttribute;

public class Element {

    protected PropertiesMap properties;
    protected String color;
    protected String groupID;

    protected String iconSVGcode;
    @XmlElements(@XmlElement(name="Property"))
    public PropertiesMap getProperties() {
        return properties;
    }
    public void setProperties(PropertiesMap properties) {
        this.properties = properties;
    }
    public Double getDouble(String key){
        return properties.getDouble(key);
    }
    public String getString(String key){
        return properties.getString(key);
    }
    public Integer getInt(String key){
        return properties.getInt(key);
    }
    public Boolean getBool(String key){
        return properties.getBool(key);
    }



    public void setProperty(String key, String value) {
        if (properties.containsKey(key))
            properties.replace(key, value);
        else
            properties.put(key, value);
    }
    @XmlElement(name="GID")
    public String getGroupID() {
        return groupID;
    }
    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }
    @XmlAttribute(name="color")
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public static void fillProperties(JEP parser, Element element, CalculationItem item) {
        for (Object key : element.getProperties().keySet()){
            String sValue = element.getString(key.toString());
            parser.parseExpression(sValue);
            double val = parser.getValue();
            item.setProperty(key.toString(), val);
            if (val > 0 || "0".equals(sValue) || "0.0".equals(sValue)) {
                item.skip(key.toString());
            }
        }
    }

    public String getIconSVGcode() {
        return iconSVGcode;
    }

    public void setIconSVGcode(String iconSVGcode) {
        this.iconSVGcode = iconSVGcode;
    }
}
