package org.nanomodeller.XMLMappingFiles;

import org.nanomodeller.Calculation.CalculationEntities.CalculationAtom;
import org.nanomodeller.Calculation.CalculationEntities.CalculationElectrode;
import org.nanomodeller.GUI.NanoModeler;
import org.nfunk.jep.JEP;

import javax.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;

@XmlRootElement(name="Electrode")
public class Electrode extends StructureElement {
    private int atomIndex;
    private int ID;
    private Integer X;
    private Integer Y;

    public Electrode(int atomIndex, int ID, Integer x, Integer y) {
        this.atomIndex = atomIndex;
        this.ID = ID;
        tag = ID + "";
        setX(x);
        setY(y);
        ArrayList<StructureElement> electrodes = GlobalProperties.getInstance().getDefaultElements().getElectrodes();
        if (electrodes != null
                && !electrodes.isEmpty()){
            this.properties = electrodes.get(0).getProperties();
            this.color = electrodes.get(0).color;
        }
    }

    public Electrode(int atomIndex, int ID, int x, int y, Electrode electrode) {
        this.atomIndex = atomIndex;
        this.ID = ID;
        tag = ID + "";
        setX(x);
        setY(y);
        this.properties = electrode.properties;
        this.color = electrode.color;
    }

    public Boolean contains(int x, int y){
        int distance = NanoModeler.getInstance().getGridSize() * 2;
        return  (getX() - distance < x && x < getX() + distance) && (getY() - distance < y && y < getY() + distance);
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

    @XmlAttribute(name="ID")
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
    public static void initializeCalculationElectrodes(JEP parser, ArrayList<Electrode> electrodes,
                                                       Hashtable<Integer, CalculationElectrode> cElectrodes,
                                                       Hashtable<Integer, CalculationAtom> cAtoms) {
        electrodes.stream().forEach(electrode ->{
            CalculationElectrode el = new CalculationElectrode(electrode.getID());
            el.setAtomID(electrode.getAtomIndex());
            cAtoms.get(electrode.getAtomIndex()).setElID(el.getID());
            fillProperties(parser, electrode, el);
            cElectrodes.put(el.getID(), el);
            el.setElement(electrode);
        });
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
}
