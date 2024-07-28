package org.nanomodeller.XMLMappingFiles;

import org.nanomodeller.Calculation.CalculationEntities.CalculationBond;
import org.nfunk.jep.JEP;

import javax.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Hashtable;

@XmlRootElement(name="Bond")
public class Bond extends StructureElement {
    private int first;
    private int second;

    public Bond(int first, int second,/* int centerX, int centerY,*/ Bond bond){
        if (first > second){
            this.second = first;
            this.first = second;
        }else{
            this.first = first;
            this.second = second;
        }
//        this.setX(centerX);
//        this.setY(centerY);
        this.properties = bond.properties;
        this.color = bond.getColor();
        this.groupID = bond.groupID;
    }


    public Bond(int first, int second/*, int centerX, int centerY*/){
        if (first > second){
            this.second = first;
            this.first = second;
        }else{
            this.first = first;
            this.second = second;
        }
//        setY(centerY);
//        setX(centerX);
    }
    @XmlAttribute(name="first")
    public int getFirst(){
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }
    @XmlAttribute(name="second")
    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }
    public Bond(){

    }

    public static void initializeCalculationBonds(JEP parser, ArrayList<Bond> bonds, Hashtable<Integer, Hashtable<Integer,CalculationBond>> cBonds) {
        for (Bond bond : bonds) {
            insertBond(parser, cBonds, bond, bond.getFirst(), bond.getSecond());
            insertBond(parser, cBonds, bond, bond.getSecond(), bond.getFirst());
        }
    }

    private static void insertBond(JEP parser, Hashtable<Integer, Hashtable<Integer, CalculationBond>> cBonds, Bond bond, int first, int second) {
        CalculationBond cb = new CalculationBond(first, second);
        fillProperties(parser, bond, cb);
        Hashtable<Integer, CalculationBond> bondsOfFirstAtom = cBonds.computeIfAbsent(first, k -> new Hashtable<>());
        bondsOfFirstAtom.put(second, cb);
    }


}
