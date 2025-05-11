package org.nanomodeller.Calculation.CalculationEntities;

import java.util.Hashtable;

import org.nanomodeller.XMLMappingFiles.StructureElement;
import org.nfunk.jep.JEP;

import java.util.HashMap;

public abstract class CalculationItem {
    protected HashMap<String, Double> properties = new HashMap<>();
    protected HashMap<String, Boolean> dontSkip = new HashMap<>();

    protected StructureElement element;

    public StructureElement getElement() {
        return element;
    }

    public void setElement(StructureElement element) {
        this.element = element;
    }

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
        return !this.dontSkip.get(key);
    }


    public static void applyTimeForItemsCalculation(JEP parser, CalculationItem[] items) {
        for (Object it : items){
            CalculationItem item = (CalculationItem)it;
            fillProperties(parser, item);

        }
    }

    public static void applyTimeForItemsCalculation(JEP parser, CalculationItem[][] items) {
        for (Object it : items){
            CalculationItem[] table = (CalculationItem[]) it;
            for (Object bond : table){
                CalculationItem item = (CalculationItem)bond;
                if (bond != null) {
                    fillProperties(parser, item);
                }
            }
        }
    }

    private static void fillProperties(JEP parser, CalculationItem item) {
        StructureElement structureElement = item.element;
        for (Object key : structureElement.getProperties().keySet()) {
            if (item.isSkip(key.toString())) {
                continue;
            }
            parser.parseExpression(structureElement.getString(key.toString()));
            double val = parser.getValue();
            item.setProperty(key.toString(), val);

        }
        item.fillItemFields();
    }

    abstract void fillItemFields() ;
}
