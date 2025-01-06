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

    public static double[] times(double re1, double im1, double re2, double im2) {
        double realPart = re1 * re2 - im1 * im2;
        double imaginaryPart = re1 * im2 + im1 * re2;
        return new double[]{realPart, imaginaryPart};
    }


    public static double[] divide(double re1, double im1, double re2, double im2) {
        double denominator = re2 * re2 + im2 * im2;
        double realPart = (re1 * re2 + im1 * im2) / denominator;
        double imaginaryPart = (im1 * re2 - re1 * im2) / denominator;
        return new double[]{realPart, imaginaryPart};
    }
    public static double[] subtract(double re1, double im1, double re2, double im2) {
        double realPart = re1 - re2;
        double imaginaryPart = im1 - im2;
        return new double[]{realPart, imaginaryPart};
    }
    public static void add(double[] result, double re1, double im1, double re2, double im2) {
        double realPart = re1 + re2;
        double imaginaryPart = im1 + im2;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }

    public static void times(double[] result, double re1, double im1, double re2, double im2) {
        double realPart = re1 * re2 - im1* im2;
        double imaginaryPart = re1 * im2 + im1 * re2;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }
    public static void times(double[] result, double[] complex1, double[] complex2) {
        double re1 = complex1[0];
        double im1 = complex1[1];
        double re2 = complex2[0];
        double im2 = complex2[1];

        double realPart = re1 * re2 - im1 * im2;
        double imaginaryPart = re1 * im2 + im1 * re2;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }

    public static void times(double[] result, double[] complex1) {
        double re1 = complex1[0];
        double im1 = complex1[1];
        double re2 = result[0];
        double im2 = result[1];

        double realPart = re1 * re2 - im1 * im2;
        double imaginaryPart = re1 * im2 + im1 * re2;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }

    public static void times(double[] result, double re, double im) {
        double re2 = result[0];
        double im2 = result[1];

        double realPart = re * re2 - im * im2;
        double imaginaryPart = re * im2 + im * re2;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }


    public static void times(double[] result, Complex c) {
        double re2 = result[0];
        double im2 = result[1];

        double realPart = c.getReal() * re2 - c.getImaginary() * im2;
        double imaginaryPart = c.getReal() * im2 + c.getImaginary() * re2;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }

    public static void times(double[] result, double factor) {
        result[0] *= factor;
        result[1] *= factor;
    }
    public static void divide(double[] result, double[] complex1, double[] complex2) {
        double re1 = complex1[0];
        double im1 = complex1[1];
        double re2 = complex2[0];
        double im2 = complex2[1];

        double denominator = re2 * re2 + im2 * im2;
        double realPart = (re1 * re2 + im1 * im2) / denominator;
        double imaginaryPart = (im1 * re2 - re1 * im2) / denominator;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }
    public static void add(double[] result, double[] complex1, double[] complex2) {
        double re1 = complex1[0];
        double im1 = complex1[1];
        double re2 = complex2[0];
        double im2 = complex2[1];

        double realPart = re1 + re2;
        double imaginaryPart = im1 + im2;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }

    public static void add(double[] result, double[] complex1) {
        double re1 = complex1[0];
        double im1 = complex1[1];
        double re2 = result[0];
        double im2 = result[1];

        double realPart = re1 + re2;
        double imaginaryPart = im1 + im2;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }
    public static void add(double[] result, Complex c) {
        double re1 = c.getReal();
        double im1 = c.getImaginary();
        double re2 = result[0];
        double im2 = result[1];

        double realPart = re1 + re2;
        double imaginaryPart = im1 + im2;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }
    public static void add(double[] result, double re, double im) {
        double re2 = result[0];
        double im2 = result[1];

        double realPart = re + re2;
        double imaginaryPart = im + im2;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }

    public static void subtract(double[] result, double[] complex1, double[] complex2) {
        double re1 = complex1[0];
        double im1 = complex1[1];
        double re2 = complex2[0];
        double im2 = complex2[1];

        double realPart = re1 - re2;
        double imaginaryPart = im1 - im2;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }

    public static void subtract(double[] result, double[] complex1) {
        double re1 = complex1[0];
        double im1 = complex1[1];
        double re2 = result[0];
        double im2 = result[1];

        double realPart = re2 - re1;
        double imaginaryPart = im2 - im1;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }
    public static void subtract(double[] result, Complex c) {
        double re1 = c.getReal();
        double im1 = c.getImaginary();
        double re2 = result[0];
        double im2 = result[1];

        double realPart = re2 - re1;
        double imaginaryPart = im2 - im1;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }
    public static void subtract(double[] result, double re, double im) {
        double re2 = result[0];
        double im2 = result[1];

        double realPart = re2 - re;
        double imaginaryPart = im2 - im;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }

    public static void divide(double[] result, double re, double im) {
        double re2 = result[0];
        double im2 = result[1];

        double denominator = re * re + im * im;
        double realPart = (re2 * re + im2 * im) / denominator;
        double imaginaryPart = (im2 * re - re2 * im) / denominator;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }
    public static void divide(double[] result, Complex c) {
        double re1 = c.getReal();
        double im1 = c.getImaginary();
        double re2 = result[0];
        double im2 = result[1];

        double denominator = re1 * re1 + im1 * im1;
        double realPart = (re2 * re1 + im2 * im1) / denominator;
        double imaginaryPart = (im2 * re1 - re2 * im1) / denominator;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }
    public static void divide(double[] result, double[] complex1) {
        double re1 = complex1[0];
        double im1 = complex1[1];
        double re2 = result[0];
        double im2 = result[1];

        double denominator = re1 * re1 + im1 * im1;
        double realPart = (re2 * re1 + im2 * im1) / denominator;
        double imaginaryPart = (im2 * re1 - re2 * im1) / denominator;
        result[0] = realPart;
        result[1] = imaginaryPart;
    }





}
