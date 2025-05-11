package org.nanomodeller.Calculation.Tools;

import org.ejml.data.Complex_F64;

public class ComplexOperations {
    public static void timesR(Complex_F64 c, double re){
        c.imaginary *= re;
        c.real *= re;
    }
    public static void timesI(Complex_F64 c, double im) {
        double real = -c.imaginary * im;
        double imaginary = c.real * im;
        c.real = real;
        c.imaginary = imaginary;
    }
    public static void timesC(Complex_F64 c, double re, double im) {
        double a = c.real;
        double b = c.imaginary;

        double real = a * re - b * im;
        double imaginary = a * im + b * re;

        c.real = real;
        c.imaginary = imaginary;
    }
    public static void timesC(Complex_F64 c1, Complex_F64 c2) {
        double a = c1.real;
        double b = c1.imaginary;
        double re = c2.real;
        double im = c2.imaginary;

        double real = a * re - b * im;
        double imaginary = a * im + b * re;

        c1.real = real;
        c1.imaginary = imaginary;
    }
    public static void invC(Complex_F64 c) {
        double invDenominator = 1.0 / (c.real * c.real + c.imaginary * c.imaginary);
        c.real *= invDenominator;
        c.imaginary *= -invDenominator;
    }

    public static void plusR(Complex_F64 c, double re) {
        c.real += re;
    }

    public static void plusI(Complex_F64 c, double im) {
        c.imaginary += im;
    }

    public static void plusC(Complex_F64 c, double re, double im) {
        c.real += re;
        c.imaginary += im;
    }

    public static void plusC(Complex_F64 c1, Complex_F64 c2) {
        c1.real += c2.real;
        c1.imaginary += c2.imaginary;
    }

    public static void minusR(Complex_F64 c, double re) {
        c.real -= re;
    }

    public static void minusI(Complex_F64 c, double im) {
        c.imaginary -= im;
    }

    public static void minusC(Complex_F64 c, double re, double im) {
        c.real -= re;
        c.imaginary -= im;
    }

    public static void minusC(Complex_F64 c1, Complex_F64 c2) {
        c1.real -= c2.real;
        c1.imaginary -= c2.imaginary;
    }
    public static void toZero(Complex_F64 c1) {
        c1.real = 0;
        c1.imaginary = 0;
    }

    public static void toOne(Complex_F64 c1) {
        c1.real = 1;
        c1.imaginary = 0;
    }
    public static void set(Complex_F64 c1, Complex_F64 c2) {
        c1.real = c2.real;
        c1.imaginary = c2.imaginary;
    }
    public static void toI(Complex_F64 c1) {
        c1.real = 0;
        c1.imaginary = 1;
    }
    public static void set(Complex_F64 c, double re, double im) {
        c.real = re;
        c.imaginary = im;
    }
    public static void setExp(Complex_F64 c, double phase){
        c.real = Math.cos(phase);
        c.imaginary = Math.sin(phase);
    }
}
