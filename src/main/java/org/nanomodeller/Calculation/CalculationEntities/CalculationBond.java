package org.nanomodeller.Calculation.CalculationEntities;


import org.ejml.data.Complex_F64;

import static org.nanomodeller.Globals.*;

public class CalculationBond extends CalculationItem{
    int first, second;
    private Double correlationCoupling;
    private Complex_F64 complexCoupling;

    public CalculationBond(int first, int second){
        this.first = second;
        this.second = first;
        this.complexCoupling = new Complex_F64(0,0);
    }

    public int getFirst() {
        return first;
    }
    public int getSecond(int i) {
        if(i == second)
            return first;
        return second;
    }

    public Complex_F64 getComplexCoupling() {
        return complexCoupling;
    }

    public void setImCoupling(double imCoupling) {
        this.complexCoupling.imaginary = 2 * imCoupling;
    }

    public void setCoupling(double coupling) {
        this.complexCoupling.real = 2 * coupling;
    }

    public Double getCorrelationCoupling() {
        return correlationCoupling;
    }

    public void setCorrelationCoupling(Double correlationCoupling) {
        this.correlationCoupling = 2 * correlationCoupling;
    }

    public int getOtherAtomID(int i) {
        if (i == first){
            return second;
        }
        return first;
    }

    @Override
    void fillItemFields() {
        this.setCoupling(this.properties.getOrDefault(COUPLING, 0.0));
        this.setImCoupling(this.properties.getOrDefault(IM_COUPLING, 0.0));
        this.setCorrelationCoupling(this.properties.getOrDefault(CORRELATION_COUPLING, 0.0));
    }
}
