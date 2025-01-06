package org.nanomodeller.Calculation.Tools;

public class complex {
    double re, im;

    public double getRe() {
        return re;
    }

    public void setRe(double re) {
        this.re = re;
    }

    public double getIm() {
        return im;
    }

    public void setIm(double im) {
        this.im = im;
    }

    public complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public complex(complex c) {
        this.re = c.re;
        this.im = c.im;
    }
    public complex(double re) {
        this.re = re;
        this.im = 0;
    }
    public complex() {
        this.re = 0;
        this.im = 0;
    }
    public void add(complex c){
        re += c.re;
        im += c.im;
    }
    public void times(double d) {
        re *= d;
        im *= d;
    }
    public void times(double d, boolean isReal) {
        if (isReal){
            times(d);
        }else{
            double tempRe = re;
            re = -im * d;
            im = tempRe * d;
        }
    }

    public void times(complex c) {
        double tempRe = re;
        re = c.re * re - c.im * im;
        im = c.re * im + c.im * tempRe;
    }


    public void divide(complex c) {
        double inverseDenominator = 1.0 / (c.re * c.re + c.im * c.im);
        double tempRe = re;
        re = (re * c.re + im * c.im) * inverseDenominator;
        im = (im * c.re - tempRe * c.im) * inverseDenominator;
    }

    public void divide(double d) {
        double nominator = 1.0 / d;
        re *= nominator;
        im *= nominator;
    }

    public void inverse() {
        double nominator = 1 / (re * re + im * im);
        re = re * nominator;
        im = -im * nominator;
    }

    public void minus(complex c) {
        re = re - c.re;
        im = im - c.im;
    }

    public void conjugate() {
        im = -im;
    }

    public double modulus() {
        return Math.sqrt(re * re + im * im);
    }
    public void add(double d){
        re += d;
    }

}
