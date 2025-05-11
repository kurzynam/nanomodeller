package org.nanomodeller.Calculation.CalculationEntities;

import java.util.ArrayList;

import static org.nanomodeller.Globals.COUPLING;
import static org.nanomodeller.Globals.PERTURBATION_COUPLING;


public class CalculationElectrode extends CalculationItem{
    private int ID;
    private int atomID;
    private double coupling;
    private Double perturbationCoupling;

    public double getCoupling() {
        return coupling;
    }

    public void setCoupling(double coupling) {
        this.coupling = coupling;
    }

    public Double getPerturbationCoupling() {
        return perturbationCoupling;
    }

    public void setPerturbationCoupling(Double perturbationCoupling) {
        this.perturbationCoupling = perturbationCoupling;
    }

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

    @Override
    void fillItemFields() {

        this.setCoupling(this.properties.get(COUPLING));
        this.setPerturbationCoupling(this.properties.get(PERTURBATION_COUPLING));
    }
}
