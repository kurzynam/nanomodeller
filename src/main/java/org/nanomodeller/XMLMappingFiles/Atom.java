package org.nanomodeller.XMLMappingFiles;


import javax.xml.bind.annotation.XmlAttribute;
import java.util.Comparator;

//@XmlRootElement(name="Atom")
public class Atom implements Comparable<Atom>{

    private int id;
    private String X;
    private String Y;
    private String E;
    private String n0;
    private String spinFlip;
    private String type;
    private String color;
    private String correlation;
    private double parsedCorrelation;
    private boolean saveLDOS = false;
    private boolean saveNormalisation = false;
    private double parsedPerturbation = 1;
    private String perturbation = "1";


    public String getPerturbation() {
        return perturbation;
    }

    public void setPerturbation(String perturbation) {
        this.perturbation = perturbation;
    }

    public double getParsedPerturbation() {
        return parsedPerturbation;
    }

    public void setParsedPerturbation(double parsedPerturbation) {
        this.parsedPerturbation = parsedPerturbation;
    }

    public Atom(Atom atom, int newID){
        id = newID;
        X = atom.X;
        Y = atom.Y;
        E = atom.E;
        n0 = atom.n0;
        spinFlip = atom.spinFlip;
        type = atom.type;
        color = atom.color;
        correlation = atom.correlation;
    }
    @Override
    public int compareTo(Atom o) {
        return Atom.Comparators.ID.compare(this, o);
    }

    public static class Comparators {

        public static Comparator<Atom> ID = Comparator.comparingInt(o -> o.getID());
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    @XmlAttribute(name="ID")
    public int getID() { return id; }
    public void setID(int id) { this.id = id; }

    public String getN0() { return n0; }
    public void setN0(String n0) { this.n0 = n0; }

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

    public String getE() {
        return E;
    }
    public void setE(String e) {
        E = e;
    }

    public String getSpinFlip() {return spinFlip;}
    public void setSpinFlip(String spinFlip) {this.spinFlip = spinFlip;}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCorrelation() {
        return correlation;
    }

    public void setCorrelation(String correlation) {
        this.correlation = correlation;
    }

    public Atom(){

    }

    public boolean isSaveLDOS() {
        return saveLDOS;
    }

    public void setSaveLDOS(boolean saveLDOS) {
        this.saveLDOS = saveLDOS;
    }

    public boolean isSaveNormalisation() {
        return saveNormalisation;
    }

    public void setSaveNormalisation(boolean saveNormalisation) {
        this.saveNormalisation = saveNormalisation;
    }

    public double getParsedCorrelation() {
        return parsedCorrelation;
    }

    public void setParsedCorrelation(double parsedCorrelation) {
        this.parsedCorrelation = parsedCorrelation;
    }

    public double getDoubleE() {return Double.parseDouble(E);}
    public double getDoubleN0() {
        return Double.parseDouble(n0);
    }
}
