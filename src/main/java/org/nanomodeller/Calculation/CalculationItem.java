package org.nanomodeller.Calculation;

import java.util.Hashtable;
import org.nanomodeller.XMLMappingFiles.Element;
import org.nfunk.jep.JEP;

import java.util.ArrayList;
import java.util.HashMap;

public class CalculationItem {
    protected HashMap<String, Double> properties = new HashMap<>();
    protected HashMap<String, Boolean> dontSkip = new HashMap<>();
    public HashMap<String, Double> getProperties() {
        return properties;
    }
    public void setProperty(String key, Double value) {
        if(properties.containsKey(key)) {
            properties.replace(key, value);
        }
        else {
            properties.put(key, value);
            dontSkip.put(key, true);
        }
    }
    public Double get(String key) {
        return properties.get(key);
    }
    public void skip(String key){
        this.dontSkip.replace(key, false);
    }
    public boolean isSkip(String key){
        return this.dontSkip.get(key);
    }

    public static void applyTimeForItemsCalculation(JEP parser, ArrayList<?> elements, Hashtable items) {
        for (int i = 0; i < elements.size(); i++){
            Element element = (Element)elements.get(i);
            for (Object key : element.getProperties().keySet()){
                if (((CalculationItem)items.get(i)).isSkip(key.toString())){
                    continue;
                }
                parser.parseExpression(element.getString(key.toString()));
                double val = parser.getValue();
                ((CalculationItem)items.get(i)).setProperty(key.toString(), val);
            }
        }
    }
}
