package org.nanomodeller.XMLMappingFiles;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Hashtable;

public class XMLTemplate {
    protected Hashtable<String, String> properties;
    protected String color;
    protected Hashtable<String, Range> variables;
    @XmlAttribute(name="color")
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }

    public Double getMin(String varName){
        return variables.get(varName).getMin();
    }
    public Double getMax(String varName){
        return variables.get(varName).getMax();
    }
    public boolean shouldCompute(String varName){
        return variables.get(varName).getMax() >= variables.get(varName).getMin();
    }

    public Double getInc(String varName){
        return variables.get(varName).getIncrement();
    }
    public Double getWidth(String varName){
        return getMax(varName) - getMin(varName);
    }
    public Integer getStepsNum(String varName){
        return (int)(getWidth(varName)/getInc(varName));
    }

    public String getString(String key){
        return properties.get(key);
    }
    public Double getDouble(String key){
        try {
            return Double.parseDouble(getString(key));
        }catch (Exception e){
            return (double) 0;
        }

    }

    public Range getVar(String varName){
        return variables.get(varName);
    }
    public Integer getInt(String key){
        try {
            return Integer.parseInt(getString(key));
        }catch (Exception e){
            return 0;
        }
    }

    public Boolean getBool(String key){
        try {
            return Boolean.parseBoolean(getString(key));
        }catch (Exception e){
            return false;
        }
    }


    @XmlElements(@XmlElement(name="Property"))
    public Hashtable<String, String> getProperties() {
        return properties;
    }
    public void setProperties(Hashtable<String, String> properties) {
        this.properties = properties;
    }

    @XmlElements(@XmlElement(name="Variable"))
    public Hashtable<String, Range> getVariables() {
        return variables;
    }
    public void setVariables(Hashtable<String, Range> v) {
        this.variables = v;
    }


}
