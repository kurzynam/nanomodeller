package org.nanomodeller.XMLMappingFiles;

import org.apache.commons.math3.util.Pair;
import org.nanomodeller.Calculation.CalculationBond;
import org.nanomodeller.Globals;
import org.nanomodeller.Tools.PropertiesMap;
import org.nanomodeller.UnorderedPair;
import org.nfunk.jep.JEP;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Hashtable;

@XmlRootElement(name="Bond")
public class Bond extends Element{
    private int first;
    private int second;

    private String groupID;

    public Bond(int first, int second, Bond bond){
        if (first > second){
            this.second = first;
            this.first = second;
        }else{
            this.first = first;
            this.second = second;
        }
        this.properties = bond.properties;
        this.color = bond.getColor();
    }

    public Bond(int first, int second){
        if (first > second){
            this.second = first;
            this.first = second;
        }else{
            this.first = first;
            this.second = second;
        }
        this.properties = new PropertiesMap();
        setColor(Globals.BLACK);
    }

    public String getGroupID() {
        return groupID;
    }
    @XmlAttribute(name="GID")
    public void setGroupID(String groupID) {
        this.groupID = groupID;
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
        bonds.stream().forEach(bond ->{
            CalculationBond cb = new CalculationBond(bond.first, bond.second);
            fillProperties(parser, bond, cb);
            Hashtable<Integer, CalculationBond> bondsOfFirstAtom = cBonds.get(bond.first);
            if (bondsOfFirstAtom == null){
                bondsOfFirstAtom = new Hashtable<>();
                cBonds.put(bond.first, bondsOfFirstAtom);
            }
            bondsOfFirstAtom.put(bond.second, cb);
        });
    }

}
