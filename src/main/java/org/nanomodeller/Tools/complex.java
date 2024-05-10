package org.nanomodeller.Tools;

import org.jscience.mathematics.number.Complex;

public class complex{

    private Complex number;

    public complex (Complex c){
        number = c;
    }

    public void plus(double d){
        number = number.plus(Complex.valueOf(d,0));
    }
    public void minus(double d){
        number = number.minus(Complex.valueOf(d,0));
    }
}
