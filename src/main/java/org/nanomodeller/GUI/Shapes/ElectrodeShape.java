package org.nanomodeller.GUI.Shapes;

import org.nanomodeller.Globals;
import org.nanomodeller.XMLMappingFiles.Electrode;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class ElectrodeShape {

    private AtomShape atom;
    private Rectangle2D rectangle;
    private Electrode electrode;
    private Line2D line;

    public ElectrodeShape(AtomShape atom, Rectangle2D rectangle, Electrode electrode){
        this.electrode = electrode;
        this.setAtom(atom);
        this.setRectangle(rectangle);
        this.electrode = electrode;
        updateLine();

    }

    public ElectrodeShape(Rectangle2D rectangle, int id){
        this.electrode = new Electrode();
        this.setRectangle(rectangle);
        this.electrode.setColor(Globals.BLACK);
        this.electrode.setAtomIndex(id);
        this.electrode.setAtomIndex(-1);
    }

    public void updateLine() {
        line = new Line2D.Double(getAtom().getShape().getCenterX(), getAtom().getShape().getCenterY(), getRectangle().getCenterX(), getRectangle().getCenterY());
    }

    public Electrode getElectrode() {
        return electrode;
    }
    public AtomShape getAtom() {
        return atom;
    }

    public void setAtom(AtomShape atom) {
        this.atom = atom;
        if (electrode != null && atom != null){
            this.electrode.setAtomIndex(atom.getID());
        }
        else if (atom == null){
            this.electrode.setAtomIndex(-1);
        }
    }

    public Rectangle2D getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle2D rectangle) {
        this.rectangle = rectangle;
    }


    public Line2D getLine() {
        return line;
    }

    public void setLine(Line2D line) {
        this.line = line;
    }

    public String getColor() {
        return electrode.getColor();
    }

    public void setColor(String color) {
        this.electrode.setColor(color);
    }

    public void setID(int id){
        electrode.setID(id);
    }

    public int getID(){
        return electrode.getID();
    }

}