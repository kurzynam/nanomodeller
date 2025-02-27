package org.nanomodeller.Calculation.CalculationEntities;

import java.util.ArrayList;

public class CalculationElectrode extends CalculationItem{
    private int ID;
    private int atomID;

    public int getAtomID() {
        return atomID;
    }

    public void setAtomID(int atomID) {
        this.atomID = atomID;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public static CalculationElectrode getElectrodeByID(int id, ArrayList<CalculationElectrode> calculationElectrodes) {
        return calculationElectrodes.stream().filter(electrode -> electrode.getID() == id).findFirst().get();
    }

    public CalculationElectrode(int ID) {
        this.ID = ID;
    }
}
