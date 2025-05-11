package org.nanomodeller.Calculation.CalculationEntities;


import static org.nanomodeller.Globals.CORRELATION_COUPLING;
import static org.nanomodeller.Globals.COUPLING;

public class CalculationBond extends CalculationItem{
    int first, second;
    private double coupling;
    private Double correlationCoupling;

    public CalculationBond(int first, int second){
        this.first = first;
        this.second = second;
    }


    public int getFirst() {
        return first;
    }
    public int getSecond() {
        return second;
    }

    public double getCoupling() {
        return coupling;
    }

    public void setCoupling(double coupling) {
        this.coupling = coupling;
    }

    public Double getCorrelationCoupling() {
        return correlationCoupling;
    }

    public void setCorrelationCoupling(Double correlationCoupling) {
        this.correlationCoupling = correlationCoupling;
    }

    public int getOtherAtomID(int i) {
        if (i == first){
            return second;
        }
        return first;
    }

    @Override
    void fillItemFields() {
        this.setCoupling(this.properties.get(COUPLING));
        this.setCorrelationCoupling(this.properties.get(CORRELATION_COUPLING));
    }
}
