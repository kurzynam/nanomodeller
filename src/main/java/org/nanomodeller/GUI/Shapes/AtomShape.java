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


    public String getColor() {
        return atom.getColor();
    }

    public void setColor(String color) {
        this.atom.setColor(color);
    }
    public Atom getAtom() {
        return atom;
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

    public static class Comparators {
        public static Comparator<AtomShape> ID = Comparator.comparingInt(o -> o.getID());
    }

}
