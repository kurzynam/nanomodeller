package org.nanomodeller.Calculation.CalculationEntities;

import java.util.ArrayList;
import java.util.Hashtable;

import org.nanomodeller.XMLMappingFiles.Bond;
import org.nanomodeller.XMLMappingFiles.Electrode;
import org.nanomodeller.XMLMappingFiles.StructureElement;
import org.nfunk.jep.JEP;

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
        return !this.dontSkip.get(key);
    }

    public static void applyTimeForItemsCalculation(JEP parser, ArrayList<?> elements, Hashtable items) {
        for (int i = 0; i < elements.size(); i++){
            StructureElement structureElement = (StructureElement)elements.get(i);
            for (Object key : structureElement.getProperties().keySet()){

                CalculationItem calculationItem;
                if (structureElement instanceof Bond){
                   // for (CalculationBond b : ((Hashtable <Integer, Hashtable<Integer, CalculationBond>> items).keySet()).values())
                }
                else{
                    if (structureElement instanceof Electrode)
                        calculationItem = (CalculationItem) items.get(((Electrode) structureElement).getID());
                    else
                        calculationItem = (CalculationItem) items.get(i);
                    if (calculationItem.isSkip(key.toString())){
                        continue;
                    }
                    parser.parseExpression(structureElement.getString(key.toString()));
                    double val = parser.getValue();
                    calculationItem.setProperty(key.toString(), val);
                }
            }
        }
    }
}
