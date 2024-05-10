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

    public ElectrodeShape(AtomShape atom, Rectangle2D rectangle, Line2D line, Electrode electrode){
        this.electrode = electrode;
        this.setAtom(atom);
        this.setRectangle(rectangle);
        this.setLine(line);
        this.electrode = electrode;

    }

    public ElectrodeShape(Rectangle2D rectangle, int id){
        this.electrode = new Electrode();
        this.setRectangle(rectangle);
        this.electrode.setCoupling("1");
        this.electrode.setdE("0.01");
        this.electrode.setColor(Globals.BLACK);
        this.electrode.setId(id);
        this.electrode.setAtomIndex(-1);
    }

    public void updateLine() {
        if (getLine() != null) {
            getLine().setLine(getAtom().getShape().getCenterX(), getAtom().getShape().getCenterY(), getRectangle().getCenterX(), getRectangle().getCenterY());
        }
    }

    public Electrode getElectrode() {
        return electrode;
    }

    public void setElectrode(Electrode electrode) {
        this.electrode = electrode;
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

    public String getCoupling() {
        return electrode.getCoupling();
    }

    public void setCoupling(String coupling) {
        this.electrode.setCoupling(coupling);
    }

    public String getType() {
        return electrode.getType();
    }

    public void setType(String type) {
        this.electrode.setType(type);
    }

    public String getdE() {
        return electrode.getdE();
    }

    public void setdE(String dE) {
        this.electrode.setdE(dE);
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
        electrode.setId(id);
    }

    public int getID(){
        return electrode.getId();
    }

    public String getPerturbation(){
        return electrode.getPerturbation();
    }
    public void setPerturbation(String per){
        this.electrode.setPerturbation(per);
    }
    public double getParsedPerturbation(){
        return this.electrode.getParsedPerturbation();
    }
}