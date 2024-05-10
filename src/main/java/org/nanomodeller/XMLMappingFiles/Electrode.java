package org.nanomodeller.XMLMappingFiles;

import org.nanomodeller.Tools.StringUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Electrode")
public class Electrode {
    private int atomIndex;
    private String X;
    private String Y;
    private String coupling;
    private double parsedCoupling;
    private int id;
    private String dE;
    private String type;
    private String color;
    private String perturbation = "1";
    private double parsedPerturbation;

    public Electrode(int atomIndex, String x, String y, String coupling, double dE, int id, String type) {
        this.atomIndex = atomIndex;
        X = x;
        Y = y;
        this.coupling = coupling;
        this.id = id;
        this.dE = dE+"";
        this.type = type;
    }
    public Electrode(int atomIndex, int id, Electrode electrode) {
        this.atomIndex = atomIndex;
        X = electrode.X;
        Y = electrode.Y;
        this.coupling = electrode.coupling;
        this.dE = electrode.dE;
        this.type = electrode.type;
        this.color = electrode.color;
        this.id = id;
    }
    public Electrode() {}

    public double getParsedCoupling() {
        return parsedCoupling;
    }

    public void setParsedCoupling(double parsedCoupling) {
        this.parsedCoupling = parsedCoupling;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @XmlAttribute(name="dE")
    public String getdE() {return dE;}
    public void setdE(String dE) {this.dE = dE;}

    @XmlAttribute(name="electrode_id")
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    @XmlAttribute(name="atom_id")
    public int getAtomIndex() {
        return atomIndex;
    }
    public void setAtomIndex(int atomIndex) {
        this.atomIndex = atomIndex;
    }
    public int getIntAtomIndex() {
        return atomIndex;
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

    @XmlAttribute(name="Γ")
    public String getCoupling() {
        return coupling;
    }
    public void setCoupling(String coupling) {
        this.coupling = coupling;
    }

    public double getDoubleDE(double whatIfNull){
        if (StringUtils.isEmpty(dE)){
            return whatIfNull;
        }
        return Double.parseDouble(dE);
    }

    public double getParsedPerturbation() {
        return parsedPerturbation;
    }

    public void setParsedPerturbation(double parsedPerturbation) {
        this.parsedPerturbation = parsedPerturbation;
    }

    public String getPerturbation() {
        return perturbation;
    }

    public void setPerturbation(String perturbation) {
        this.perturbation = perturbation;
    }
}
