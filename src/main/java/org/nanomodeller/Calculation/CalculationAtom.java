package org.nanomodeller.Calculation;

import java.util.ArrayList;

public class CalculationAtom extends CalculationItem{
    private int ID;

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
