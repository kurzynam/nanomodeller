package org.nanomodeller.Calculation.CalculationEntities;

import java.util.ArrayList;

public class CalculationAtom extends CalculationItem{
    private int ID;
    private Integer elID;

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
    public void setID(int ID){
        this.ID = ID;
    }

    public static CalculationAtom getAtomByID(int id, ArrayList<CalculationAtom> calculationAtoms) {
        return calculationAtoms.stream().filter(atom -> atom.getID() == id).findFirst().get();
    }
}
