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

    public static void applyTimeForItemsCalculation(JEP parser, Hashtable items) {
        applyTimeForItemsCalculation(parser, items, false);
    }
    public static void applyTimeForItemsCalculation(JEP parser, Hashtable items, boolean bonds) {
        for (Object it : items.values()){
            if(bonds){
                Hashtable table = (Hashtable) it;
                for (Object bond : table.values()){
                    CalculationItem item = (CalculationItem)bond;
                    fillProperties(parser, item);
                }
            }else {
                CalculationItem item = (CalculationItem)it;
                fillProperties(parser, item);
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
