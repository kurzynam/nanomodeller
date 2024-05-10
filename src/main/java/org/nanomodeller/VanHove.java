package org.nanomodeller;

import org.jscience.mathematics.number.Complex;

import static org.nanomodeller.CommonMath.comp;
import static org.nanomodeller.CommonMath.elypticFunction;
import static org.nanomodeller.CommonMath.phi;
import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static org.jscience.mathematics.number.Complex.ZERO;

public class VanHove {

    public static Complex onePeak(double E, double d){
        double absE = Math.abs(E);
        Complex arg = comp(d).divide(comp(absE,0.0000000001));
        arg = (Complex.ONE.minus(arg.pow(2)));
        arg = arg.sqrt();
        arg = elypticFunction(arg, false);
        arg = arg.plus(comp(PI/2));
        arg = arg.divide(Math.E*Math.E*PI*0.8);
        return arg;
    }

    public static Complex twoPeaks(double E, double t){
        double absE = Math.abs(E);
        Complex cE = comp(absE, 0.0000001);
        double result = 2/pow((PI*t), 2);
        Complex res = cE.times(result);
        Complex fi = phi(cE.divide(t));
        Complex arg = cE.times(4/t);
        if (absE < t){
            res = res.divide(fi.sqrt());
            res = res.times(elypticFunction(arg.divide(fi), true));
        }
        else if (absE < 3*t){
            Complex sqrt = arg.sqrt();
            res = res.divide(sqrt);
            res = res.times(elypticFunction(fi.divide(arg), true));
        }
        else{
            res = ZERO;
        }
        return res.times(0.5);
    }

}
