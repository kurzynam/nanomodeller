package org.nanomodeller.Tools;

import org.jscience.mathematics.number.Complex;
import org.jscience.mathematics.vector.ComplexMatrix;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MathOperation {

//
//    public static double countTransmision(double left,double right,Complex M){
//        return left*right*Math.pow(M.magnitude(),2);
//    }
//
//    public static long integerPow(long base, int exponent){
//        long result = base;
//        for (int i = 0; i < exponent -1 ; i++){
//            result *=base;
//        }
//        return result;
//    }
//    public static Complex[][] increaseDiagonalReal(ComplexMatrix M,int dim, double increment){
//        Complex[][] c=new Complex[dim][dim];
//        for(int i=0;i<dim;i++){
//            for(int j=0;j<dim;j++){
//                if(i!=j){
//                    c[i][j]=Complex.valueOf(M.get(i, j).getReal(),M.get(i, j).getImaginary());
//                }else{
//                    c[i][j]=Complex.valueOf(M.get(i, j).getReal()+increment,M.get(i, j).getImaginary());
//                }
//            }
//        }
//        return c;
//    }
//    public static Complex[][] decreaseDimension(ComplexMatrix M){
//        int d=M.getNumberOfColumns();
//        Complex[][] c=new Complex[d-1][d-1];
//        for(int i=0;i<d-1;i++){
//            for(int j=0;j<d-1;j++){
//                c[i][j]=M.get(i, j);
//            }
//        }
//        return c;
//    }
//
//    public static Complex[][] cofactor(ComplexMatrix M,int row,int col){
//        int d=M.getNumberOfColumns()-1;
//        Complex[][] c=new Complex[d][d];
//        int incrX=0,incrY=0;
//        PrintWriter pw = null;
//        try {
//            pw = new PrintWriter(new FileWriter("M.txt"));
//
//            for(int i=0;i<d;i++){
//                if(i>=row){
//                    incrX=1;
//                }
//                for(int j=0;j<d;j++){
//                    if(j>=col){
//                        incrY=1;
//                    }
//                    c[i][j]=M.get(i+incrX, j+incrY);
//                    pw.printf("%16s",c[i][j].getReal()+"+i"+c[i][j].getImaginary());
//                }
//                pw.println();
//                pw.println();
//                incrY=0;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return c;
//    }
//    public static Complex[][] set(ComplexMatrix M,int row,int col,Complex change){
//        Complex[][] c=new Complex[M.getNumberOfRows()][M.getNumberOfColumns()];
//        for(int i=0;i<M.getNumberOfRows();i++){
//            for(int j=0;j<M.getNumberOfColumns();j++){
//                c[i][j]=M.get(i, j);
//            }
//        }
//        c[row][col]=change;
//        return c;
//    }
}

