package org.nanomodeller.Calculation.CalculationEntities;

public class CalculationBond extends CalculationItem{
    int first, second;
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

    public int getOtherAtomID(int i) {
        if (i == first){
            return second;
        }
        return first;
    }
}
