package org.nanomodeller.Calculation;


import static org.nanomodeller.CommonPhysics.DensType.*;
import static org.nanomodeller.CommonPhysics.density;
import static org.nanomodeller.CommonPhysics.sigma;
import static org.nanomodeller.CommonPhysics.toEnergyStep;

import org.nanomodeller.Globals;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.Matrix;
import org.nanomodeller.XMLMappingFiles.Parameters;
import org.nanomodeller.XMLMappingFiles.GlobalProperties;
import org.jscience.mathematics.number.Complex;
import org.jscience.mathematics.vector.ComplexMatrix;

public class StaticProperties {


    public static void countStaticProperties(String stepName){

        GlobalProperties gp = GlobalProperties.getInstance();
        Parameters par = Parameters.getInstance();
        MyFileWriter ldosWriter = new MyFileWriter(par.getPath() + "/" + Globals.STATIC_LDOS_FILE_NAME_PATTERN);
        MyFileWriter normalisationWriter = new MyFileWriter(par.getPath() + "/" +Globals.STATIC_NORMALISATION_FILE_NAME_PATTERN);
        Matrix matrix = Matrix.readMatrixFromDataFile(par);
        Complex[] sigma1 = null;
        Complex[] sigma2 = null;
        Complex[] sigma3 = null;
        double[] normalisations = new double[par.getAtoms().size()];
        double dE = gp.getdE();
        double Emin = gp.getDoubleEmin();
        double Emax = gp.getDoubleEmax();

        if (matrix.contains("O")){
            sigma1 = new Complex[gp.getNumberOfEnergySteps()];
            Complex[] dens = density(gp, vanHoveOne);
            for (double e = Emin; e <= Emax; e+= dE){
                sigma1[toEnergyStep(e, dE, gp)] = sigma(e, dens, Emin, Emax);
            }
        }
        if (matrix.contains("T")){
            sigma2 = new Complex[gp.getNumberOfEnergySteps()];
            Complex[] dens = density(gp, vanHoveTwo);
            for (double e = Emin; e <= Emax; e+= dE){
                sigma2[toEnergyStep(e, dE, gp)] = sigma(e, dens, Emin, Emax);
            }
        }
        if (matrix.contains("F")){
            sigma3 = new Complex[gp.getNumberOfEnergySteps()];
            Complex[] dens = density(gp, flat);
            for (double e = Emin; e <= Emax; e+= dE){
                sigma3[toEnergyStep(e, dE, gp)] = sigma(e, dens, Emin, Emax);
            }
        }
        for (int i = 0; i < par.getAtoms().size(); i++){
            normalisations[i] = 0;
        }
        String header = "n, E";
        for (Atom a : par.getAtoms()){
            header += ", " + a.getTag();
        }
        ldosWriter.println(header);
        for (int n = 0; n < 1; n++) {
            matrix.getParser().addVariable("n", n);
            for (double tempE = Emin; tempE < Emax; tempE += dE) {
                String results = "";
                String normalisation = "";
                matrix.getParser().addVariable("E", tempE);
                if (sigma1 != null) {
                    Complex comp = sigma1[toEnergyStep(tempE, dE, gp)];
                    matrix.getParser().addComplexVariable("O", comp.getReal(), comp.getImaginary());
                }
                if (sigma2 != null) {
                    Complex comp = sigma2[toEnergyStep(tempE, dE, gp)];
                    matrix.getParser().addComplexVariable("T", comp.getReal(), comp.getImaginary());
                }
                if (sigma3 != null) {
                    Complex comp = sigma3[toEnergyStep(tempE, dE, gp)];
                    matrix.getParser().addComplexVariable("F", comp.getReal(), comp.getImaginary());
                }
                ComplexMatrix M = matrix.convertToComplexMatrix();
                ComplexMatrix TempMatrix = M.inverse();
                for (int i = 0; i < par.getAtoms().size(); i++) {
                    double result = countLocalDensity(TempMatrix.get(i, i));
                    normalisations[i] += result * dE;
                    results += result +",";
                    normalisation += normalisations[i] + ",";
                }
                ldosWriter.println(n +"," + tempE + "," + results);
                normalisationWriter.println(n +"," + tempE + "," + normalisation);
            }
            ldosWriter.println();
            normalisationWriter.println();
        }
        ldosWriter.close();
        normalisationWriter.close();
    }
    public static double countLocalDensity(Complex M){
        return (-1/Math.PI)*M.getImaginary();
    }
}
