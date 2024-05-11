package org.nanomodeller.GUI.Shapes;

import org.nanomodeller.Globals;
import org.nanomodeller.XMLMappingFiles.Atom;

import java.awt.geom.Ellipse2D;
import java.util.Comparator;

public class AtomShape implements Comparable<AtomShape>{

    private Ellipse2D shape;
    private Atom atom;

    public AtomShape(Ellipse2D shape, int ID){
        this.setShape(shape);
        atom = new Atom();
        this.atom.setE("0");
        this.atom.setN0("0");
        this.atom.setSpinFlip("0");
        this.atom.setID(ID);
        this.atom.setColor(Globals.BLACK);
    }
    public AtomShape(Ellipse2D shape, Atom atom){
        this.setShape(shape);
        this.atom = atom;
    }
    @Override
    public int compareTo(AtomShape o) {
        return Comparators.ID.compare(this, o);
    }

    public Ellipse2D getShape() {
        return shape;
    }

    public void setShape(Ellipse2D shape) {
        this.shape = shape;
    }

    public String getEnergy() {
        return atom.getE();
    }

    public void setEnergy(String energy) {
        this.atom.setE(energy);
    }

    public String getnZero() {
        return atom.getN0();
    }

    public void setnZero(String nZero) {
        this.atom.setN0(nZero);
    }

    public String getSpinFlip() {
        return atom.getSpinFlip();
    }

    public void setSpinFlip(String spinFlip) {
        this.atom.setSpinFlip(spinFlip);
    }

    public String getType() {
        return atom.getType();
    }

    public void setType(String type) {
        this.atom.setType(type);
    }

    public String getColor() {
        return atom.getColor();
    }

    public void setColor(String color) {
        this.atom.setColor(color);
    }

    public void setCorrelation(String val){
        atom.setCorrelation(val);
    }
    public String getCorrelation(){
        return atom.getCorrelation();
    }
    public Atom getAtom() {
        return atom;
    }

    public boolean isSaveLDOS() {
        return atom.isSaveLDOS();
    }

    public void setSaveLDOS(boolean saveLDOS) {
        this.atom.setSaveLDOS(saveLDOS);
    }

    public boolean isSaveNormalisation() {
        return atom.isSaveNormalisation();
    }

    public void setSaveNormalisation(boolean saveNormalisation) {
        this.atom.setSaveNormalisation(saveNormalisation);
    }

    public void setAtom(Atom atom) {
        this.atom = atom;
    }

    public int getID() {
        return atom.getID();
    }

    public void setID(int ID) {
        this.atom.setID(ID);
    }

    public String getPerturbation() {
        return this.atom.getPerturbation();
    }
    public void setPerturbation(String perturbation){
        this.atom.setPerturbation(perturbation);
    }

    public static class Comparators {
        public static Comparator<AtomShape> ID = Comparator.comparingInt(o -> o.getID());
    }

}
