package org.nanomodeller.XMLMappingFiles;


import jakarta.xml.bind.annotation.XmlRootElement;
import org.nanomodeller.Calculation.CalculationAtom;
import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Tools.PropertiesMap;
import org.nfunk.jep.JEP;

import javax.xml.bind.annotation.XmlAttribute;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;

@XmlRootElement(name="Atom")
public class Atom extends Element implements Comparable<Atom>{

    private int ID;

    private Hashtable<String, String> properties;
    private Integer X;
    private Integer Y;

    public Atom(Atom atom, int newID){
        ID = newID;
        color = atom.color;
        tag = newID + "";
        setGroupID(atom.groupID);
        properties = atom.properties;
    }

    public Atom(Atom atom, Integer x, Integer y, int newID){
        ID = newID;
        setX(x);
        setY(y);
        tag = newID + "";
        color = atom.color;
        setGroupID(atom.groupID);
        properties = atom.properties;
    }
    public void move(int dx, int dy){
        setX(this.getX() + dx);
        setY(this.getY() + dy);
    }
    public Boolean contains(int x, int y){
        int distance = NanoModeler.getInstance().getGridSize() * 2;
        return  (getX() - distance < x && x < getX() + distance) && (getY() - distance < y && y < getY() + distance);
    }

    public Atom(Integer x, Integer y, int newID){
        ID = newID;
        tag = newID + "";
        this.setX(x);
        this.setY(y);
    }

    public static Point middle(Atom first, Atom second){
        return new Point((first.getX() + second.getX())/2, (first.getY() + second.getY())/2);
    }
    @Override
    public int compareTo(Atom o) {
        return Atom.Comparators.ID.compare(this, o);
    }

    @XmlAttribute(name="X")
    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    @XmlAttribute(name="Y")
    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }

    public static class Comparators {
        public static Comparator<Atom> ID = Comparator.comparingInt(o -> o.getID());
    }
    @XmlAttribute(name="ID")
    public int getID() { return ID; }
    public void setID(int id) { this.ID = id; }

    public void setCoordinates(int x, int y){
        this.setX(x);
        this.setY(y);
    }

    public void setCoordinates(double x, double y){
        this.setX((int) x);
        this.setY((int) y);
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
