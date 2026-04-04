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

    public double getCoupling() {
        return complexCoupling.real;
    }

    public Complex_F64 getComplexCoupling() {
        return complexCoupling;
    }

    public void setComplexCoupling(Complex_F64 complexCoupling) {
        this.complexCoupling = complexCoupling;
    }

    public double getImCoupling() {
        return complexCoupling.imaginary;
    }

    public void setImCoupling(double imCoupling) {
        this.complexCoupling.imaginary = imCoupling;
    }

    public void setCoupling(double coupling) {
        this.complexCoupling.real = coupling;
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
        this.setCoupling(this.properties.getOrDefault(COUPLING, 0.0));
        this.setImCoupling(this.properties.getOrDefault(IM_COUPLING, 0.0));
        this.setCorrelationCoupling(this.properties.getOrDefault(CORRELATION_COUPLING, 0.0));
    }
}
