package org.nanomodeller.XMLMappingFiles;


import jakarta.xml.bind.annotation.XmlRootElement;
import org.nanomodeller.Calculation.CalculationAtom;
import org.nanomodeller.Tools.PropertiesMap;
import org.nfunk.jep.JEP;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;

@XmlRootElement(name="Atom")
public class Atom extends Element implements Comparable<Atom>{

    private int ID;
    private Double X;
    private Double Y;
    private PropertiesMap properties;
    public Atom(Atom atom, int newID){
        ID = newID;
        X = atom.X;
        Y = atom.Y;
        color = atom.color;
        setGroupID(atom.groupID);
        properties = atom.properties;
    }
    @Override
    public int compareTo(Atom o) {
        return Atom.Comparators.ID.compare(this, o);
    }

    public static class Comparators {
        public static Comparator<Atom> ID = Comparator.comparingInt(o -> o.getID());
    }
    @XmlAttribute(name="ID")
    public int getID() { return ID; }
    public void setID(int id) { this.ID = id; }


    @XmlAttribute(name="color")
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }

    @XmlAttribute(name="X")
    public Double getX() {
        return X;
    }
    public void setX(Double x) {
        X = x;
    }
    @XmlAttribute(name="Y")
    public Double getY() {
        return Y;
    }
    public void setY(Double y) {
        Y = y;
    }

    public static void initializeCalculationAtoms(JEP parser, ArrayList<Atom> atoms, Hashtable<Integer, CalculationAtom> cAtoms) {
        atoms.stream().forEach(atom ->{
            CalculationAtom ato = new CalculationAtom(atom.getID());
            fillProperties(parser, atom, ato);
            cAtoms.put(ato.getID(), ato);
        });
    }
    public Atom(){}
}
