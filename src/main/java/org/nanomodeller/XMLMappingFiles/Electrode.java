package org.nanomodeller.XMLMappingFiles;

import org.nanomodeller.Calculation.CalculationElectrode;
import org.nfunk.jep.JEP;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;

@XmlRootElement(name="Electrode")
public class Electrode extends Element {
    private int atomIndex;


    private int ID;
    private Integer X;
    private Integer Y;

    public Electrode(int atomIndex, int ID, Integer x, Integer y) {
        this.atomIndex = atomIndex;
        this.ID = ID;
        setX(x);
        setY(y);
    }
    public Boolean contains(Point point, int distance) {
        return contains(point.x, point.y, distance);
    }
    public Electrode() {}

    public void setCoordinates(Integer x, Integer y){
        setX(x);
        setY(y);
    }

    public void move(int dx, int dy){
        setX(this.getX() + dx);
        setY(this.getY() + dy);
    }
    public Boolean contains(int x, int y, int distance){
        return  (getX() - distance < x && x < getX() + distance) && (getY() - distance < y && y < getY() + distance);
    }

    public void setCoordinates(double x, double y){
        setX((int) x);
        setY((int) y);
    }


    @XmlAttribute(name="AtomID")
    public int getAtomIndex() {
        return atomIndex;
    }
    public void setAtomIndex(int atomIndex) {
        this.atomIndex = atomIndex;
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
    @XmlAttribute(name="ID")
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
    public static void initializeCalculationElectrodes(JEP parser, ArrayList<Electrode> electrodes, Hashtable<Integer, CalculationElectrode> cElectrodes) {
        electrodes.stream().forEach(electrode ->{
            CalculationElectrode el = new CalculationElectrode(electrode.getAtomIndex());
            fillProperties(parser, electrode, el);
            cElectrodes.put(el.getID(), el);
        });
    }
}
