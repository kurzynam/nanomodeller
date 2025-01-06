package org.nanomodeller.Calculation.Tools;

import org.jscience.mathematics.number.Complex;

public class cmplx {
    public double re;
    public double im;

    public cmplx(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public cmplx(Complex c){
        this.re = c.getReal();
        this.im = c.getImaginary();
    }

    public Complex toComplex(){
        return Complex.valueOf(re, im);
    }
    public void timesRe(double re, double im) {
        double tempRe = re;
        this.re = re * this.re - im * this.im;
        this.im = re * this.im + im * tempRe;
    }


    public void timesRe(Complex c) {
        double tempRe = re;
        re = c.getReal() * re - c.getImaginary() * im;
        im = c.getReal() * im + c.getImaginary() * tempRe;
    }

    public void plus(Complex c) {
        re += c.getReal();
        im += c.getImaginary();
    }

    public void subtractFrom(Complex c) {
        re = c.getReal() - re;
        im = c.getImaginary() - im;
    }

    public void subtract(cmplx c) {
        re -= c.re;
        im -= c.im;
    }

    public void plus(cmplx c) {
        re += c.re;
        im += c.im;
    }

    public void subtract(Complex c) {
        re -= c.getReal();
        im -= c.getImaginary();
    }
    public void timesRe(double d) {
        re *= d;
        im *= d;
    }
    public void timesIm(double d) {
        double tempRe = re;
        re = -im * d;
        im = d * tempRe;
    }
    public void divide(Complex c) {
        double inverseDenominator = 1.0 / (c.getReal() * c.getReal() + c.getImaginary() * c.getImaginary());
        double tempRe = re;
        re = (re * c.getReal() + im * c.getImaginary()) * inverseDenominator;
        im = (im * c.getReal() - tempRe * c.getImaginary()) * inverseDenominator;
    }


}
