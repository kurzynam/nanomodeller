package org.nanomodeller.GUI.Shapes;


import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.Bound;

import java.awt.geom.Line2D;

public class AtomBound {


    private Line2D line;
    private Bound bound;
    private Atom firstAtom;
    private Atom secondAtom;

    public AtomBound(Atom first, Atom second, Line2D line, Bound bound){
        this.bound = bound;
        this.line = line;
        this.firstAtom = first;
        this.secondAtom = second;
        this.bound.setFirst(first.getID());
        this.bound.setSecond(second.getID());
    }
    public AtomBound(Line2D line, Bound bound){
        this.bound = bound;
        this.line = line;
    }
    public AtomBound(int first, int second, Line2D line){
        this.bound = new Bound(first, second);
        this.line = line;
    }
    public void updateLine(AtomShape first, AtomShape second) {
        if (getLine() == null){
            setLine(new Line2D.Float((float) first.getShape().getX() + (float) first.getShape().getWidth() / 2,
                        (float) first.getShape().getY()  + (float) first.getShape().getHeight() / 2,
                        (float) second.getShape().getX() + (float) second.getShape().getWidth() / 2,
                        (float) second.getShape().getY() + (float) second.getShape().getHeight() / 2));

        }
        else {
            if (first != null && second != null)
            getLine().setLine(first.getShape().getCenterX(), first.getShape().getCenterY(), second.getShape().getCenterX(), second.getShape().getCenterY());
        }
    }

    public Bound getBound() {
        return bound;
    }

    public void setBound(Bound bound) {
        this.bound = bound;
    }

    public int getFirst() {
        return bound.getFirst();
    }

    public void setFirst(int first) {
        this.bound.setFirst(first);
    }

    public int getSecond() {
        return bound.getSecond();
    }

    public void setSecond(int second) {
        this.bound.setSecond(second);
    }

    public Line2D getLine() {
        return line;
    }

    public void setLine(Line2D line) {
        this.line = line;
    }

    public String getValue() {
        return bound.getCoupling();
    }

    public void setValue(String value) {
        this.bound.setCoupling(value);
    }

    public String getSpinOrbit() {
        return bound.getSpinOrbit();
    }

    public void setSpinOrbit(String spinOrbit) {
        this.bound.setSpinOrbit(spinOrbit);
    }

    public String getType() {
        return bound.getType();
    }

    public void setType(String type) {
        this.bound.setType(type);
    }

    public String getColor() {
        return bound.getColor();
    }

    public void setColor(String color) {
        this.bound.setColor(color);
    }

    public String getCorrelationCoupling() {
        return bound.getCorrelationCoupling();
    }

    public void setCorrelationCoupling(String correlationCoupling) {
        this.bound.setCorrelationCoupling(correlationCoupling);
    }
    public String getPerturbation(){
        return bound.getPerturbation();
    }
    public void setPerturbation(String per){
        this.bound.setPerturbation(per);
    }
    public double getParsedPerturbation(){
        return this.bound.getParsedPerturbation();
    }
    public void updateAtoms() {
        if (firstAtom != null && secondAtom != null) {
            setFirst(firstAtom.getID());
            setSecond(secondAtom.getID());
        }
    }
}
