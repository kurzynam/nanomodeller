package org.nanomodeller.Calculation.CalculationEntities;

import java.util.ArrayList;

import static org.nanomodeller.Globals.*;
import static org.nanomodeller.Globals.SPIN_FLIP;

public class CalculationAtom extends CalculationItem{
    private int ID;
    private Integer elID;
    private double spinFlip;
    private double initialOccupation;
    private double onSiteEnergy;

    public Integer getElID() {
        return elID;
    }

    public void setElID(Integer elID) {
        this.elID = elID;
    }

    public CalculationAtom(int ID){
        this.ID = ID;
    }
    public int getID(){
        return ID;
    }

    public double getSpinFlip() {
        return spinFlip;
    }

    public void setSpinFlip(double spinFlip) {
        this.spinFlip = spinFlip;
    }

    public double getInitialOccupation() {
        return initialOccupation;
    }

    public void setInitialOccupation(double initialOccupation) {
        this.initialOccupation = initialOccupation;
    }

    public double getOnSiteEnergy() {
        return onSiteEnergy;
    }

    public void setOnSiteEnergy(double onSiteEnergy) {
        this.onSiteEnergy = onSiteEnergy;
    }

    public static CalculationAtom getAtomByID(int id, ArrayList<CalculationAtom> calculationAtoms) {
        return calculationAtoms.stream().filter(atom -> atom.getID() == id).findFirst().get();
    }

    @Override
    void fillItemFields() {
        this.setInitialOccupation(this.properties.get(INITIAL_OCCUPATION));
        this.setSpinFlip(this.properties.get(SPIN_FLIP));
        this.setOnSiteEnergy(this.properties.get(ON_SITE_ENERGY));
    }
}
