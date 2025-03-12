package org.nanomodeller;

import org.jscience.mathematics.number.Complex;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;

import java.io.BufferedReader;

import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static org.jscience.mathematics.number.Complex.ONE;
import static org.jscience.mathematics.number.Complex.ZERO;

public class CommonMath {
    public static Complex comp(double re, double im){
        return Complex.valueOf(re, im);
    }
    public static Complex comp(double re){
        return Complex.valueOf(re, 0);
    }

    public static Complex elypticFunction(Complex m, boolean twoPeaks){
        Complex result = ZERO;
        Complex temp;
        Double dphi = 0.01;
        for (double phi = 0; phi <= PI/2; phi += dphi){
            temp = (comp(dphi,0).divide(
                    (ONE.minus(m.times(pow(Math.sin(phi), 2)))).sqrt())
            );
            if (twoPeaks && temp.getReal() < 0){
                temp = temp.times(-1);
            }
            result = result.plus(temp);
        }
        return result;
    }
    public static Complex phi(Complex x){
        return (x.plus(ONE)).pow(2).minus((x.pow(2).minus(ONE)).pow(2).times(0.25));
    }
}
