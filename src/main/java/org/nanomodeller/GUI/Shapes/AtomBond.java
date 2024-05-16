package org.nanomodeller.GUI.Shapes;


import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.Bond;

import java.awt.*;
import java.awt.geom.Line2D;

import static org.nanomodeller.Globals.BLACK;

public class AtomBond {


    private Line2D line;
    private Bond bond;
    private AtomShape firstAtom;
    private AtomShape secondAtom;

    public AtomBond(AtomShape first, AtomShape second, Bond bond){
        this.bond = bond;
        if (first.getID() < second.getID()){
            this.firstAtom = first;
            this.secondAtom = second;
        }else{
            this.firstAtom = second;
            this.secondAtom = first;
        }
        setColor(BLACK);
        this.bond.setFirst(firstAtom.getID());
        this.bond.setSecond(secondAtom.getID());
        updateLine();
    }
    public void updateLine() {
        if (getLine() == null){
            setLine(new Line2D.Double(firstAtom.getShape().getX() +  firstAtom.getShape().getWidth() / 2,
                        firstAtom.getShape().getY()  + firstAtom.getShape().getHeight() / 2,
                        secondAtom.getShape().getX() + secondAtom.getShape().getWidth() / 2,
                        secondAtom.getShape().getY() + secondAtom.getShape().getHeight() / 2));

        }
        else {
            if (firstAtom != null && secondAtom != null)
            getLine().setLine(firstAtom.getShape().getCenterX(), firstAtom.getShape().getCenterY(), secondAtom.getShape().getCenterX(), secondAtom.getShape().getCenterY());
        }
    }
    public void setBond(Bond bond) {
        this.bond = bond;
    }

    public int getFirst() {
        return bond.getFirst();
    }

    public void setFirst(int first) {
        this.bond.setFirst(first);
    }

    public int getSecond() {
        return bond.getSecond();
    }

    public Bond getBond() {
        return bond;
    }

    public AtomShape getFirstAtom() {
        return firstAtom;
    }


    public AtomShape getSecondAtom() {
        return secondAtom;
    }

    public void setSecond(int second) {
        this.bond.setSecond(second);
    }

    public Line2D getLine() {
        return line;
    }

    private void setLine(Line2D line) {
        this.line = line;
    }

    public String getColor() {
        return bond.getColor();
    }

    public void setColor(String color) {
        this.bond.setColor(color);
    }

}
