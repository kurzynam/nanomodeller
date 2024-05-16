package org.nanomodeller.XMLMappingFiles;

import org.nanomodeller.Calculation.CalculationElectrode;
import org.nanomodeller.Tools.StringUtils;
import org.nfunk.jep.JEP;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Hashtable;

@XmlRootElement(name="Electrode")
public class Electrode extends Element {
    private int atomIndex;


    private int ID;
    private String X;
    private String Y;

    public Electrode(int atomIndex, int ID, String x, String y) {
        this.atomIndex = atomIndex;
        this.ID = ID;
        setX(x);
        setY(y);
    }

    public Electrode() {}


    @XmlAttribute(name="AtomID")
    public int getAtomIndex() {
        return atomIndex;
    }
    public void setAtomIndex(int atomIndex) {
        this.atomIndex = atomIndex;
    }

    @XmlAttribute(name="X")
    public String getX() {
        return X;
    }
    public void setX(String x) {
        X = x;
    }
    @XmlAttribute(name="Y")
    public String getY() {
        return Y;
    }
    public void setY(String y) {
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
