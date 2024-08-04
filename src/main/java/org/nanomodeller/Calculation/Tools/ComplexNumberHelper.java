package org.nanomodeller.Calculation.Tools;


import org.jscience.mathematics.number.Complex;

public final class ComplexNumberHelper{

    public static Complex add(double... c){
        double re = 0;
        double im = 0;
        int len = c.length / 2;
        for(int i = 0; i < len; i++){
            re += c[i];
            im += c[i + len];
        }
        return Complex.valueOf(re, im);
    }

}
