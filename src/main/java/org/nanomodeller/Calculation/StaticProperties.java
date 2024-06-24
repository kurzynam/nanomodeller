package org.nanomodeller.Calculation;


import static org.nanomodeller.CommonPhysics.DensType.*;
import static org.nanomodeller.CommonPhysics.density;
import static org.nanomodeller.CommonPhysics.sigma;

import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Globals;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.XMLMappingFiles.*;
import org.jscience.mathematics.number.Complex;
import org.jscience.mathematics.vector.ComplexMatrix;

import javax.swing.*;

public class StaticProperties {


    public static void countStaticProperties(){
        long startTime = System.currentTimeMillis();
        CommonProperties cp = CommonProperties.getInstance();
        Parameters par = Parameters.getInstance();
        MyFileWriter ldosWriter = new MyFileWriter(par.getPath() + "/" + Globals.STATIC_LDOS_FILE_NAME_PATTERN);
        MyFileWriter chargeWriter = new MyFileWriter(par.getPath() + "/" +Globals.STATIC_NORMALISATION_FILE_NAME_PATTERN);
        Matrix matrix = Matrix.readMatrixFromDataFile(par);
        NanoModeler nm = NanoModeler.getInstance();
        Complex[] sigma1 = null;
        Complex[] sigma2 = null;
        Complex[] sigma3 = null;
        double[] charges = new double[par.getAtoms().size()];
        if (matrix.contains("O")){
            sigma1 = new Complex[cp.getStepsNum("E")];
            Complex[] dens = density(cp, vanHoveOne);
            int i = 0;
            for (Double e : cp.getVar("E")){
                sigma1[i++] = sigma(e, dens);
            }
        }
        if (matrix.contains("T")){
            sigma2 = new Complex[cp.getStepsNum("E")];
            Complex[] dens = density(cp, vanHoveTwo);
            int i = 0;
            for (Double e : cp.getVar("E")){
                sigma2[i++] = sigma(e, dens);
            }
        }
        if (matrix.contains("F")){
            sigma3 = new Complex[cp.getStepsNum("E")];
            Complex[] dens = density(cp, flat);
            int i = 0;
            for (Double e : cp.getVar("E")){
                sigma3[i++] = sigma(e, dens);
            }
        }
        StringBuilder header = new StringBuilder();
        for (Atom a : par.getAtoms()){
            if(a.getBool("Save"))
                header.append(", ").append(a.getTag());
        }
        ldosWriter.println("n, E" + header);
        chargeWriter.println("n, i, q");
        for (String property : CommonProperties.getInstance().getProperties().keySet()){
            matrix.getParser().addVariable(property, CommonProperties.getInstance().getDouble(property));
        }
        for (Double n : cp.getVar("n")) {
            for (int i = 0; i < par.getAtoms().size(); i++){
                charges[i] = 0;
            }
            matrix.getParser().addVariable("n", n);
            updateProgressBar(n, "n", cp, nm.getMenu().getSecondPB());
            for (double tempE : cp.getVar("E")) {
                updateProgressBar(tempE, "E", cp, nm.getMenu().getFirstPB());
                String results = "";
                matrix.getParser().addVariable("E", tempE);
//                if (sigma1 != null) {
//                    Complex comp = sigma1[toEnergyStep(tempE, dE, gp)];
//                    matrix.getParser().addComplexVariable("O", comp.getReal(), comp.getImaginary());
//                }
//                if (sigma2 != null) {
//                    Complex comp = sigma2[toEnergyStep(tempE, dE, gp)];
//                    matrix.getParser().addComplexVariable("T", comp.getReal(), comp.getImaginary());
//                }
//                if (sigma3 != null) {
//                    Complex comp = sigma3[toEnergyStep(tempE, dE, gp)];
//                    matrix.getParser().addComplexVariable("F", comp.getReal(), comp.getImaginary());
//                }
                ComplexMatrix M = matrix.convertToComplexMatrix();
                ComplexMatrix TempMatrix = M.inverse();
                for (Atom atom : par.getAtoms() ){
                    double result = countLocalDensity(TempMatrix.get(atom.getID(), atom.getID()));
                    if(atom.getBool("Save"))
                        results += result + ",";
                    if (tempE <= 0)
                        charges[atom.getID()] += result * cp.getInc("E");
                }
                ldosWriter.println(n +"," + tempE + "," + results);
            }
            for (Atom atom : par.getAtoms()) {
                chargeWriter.println(n + "," + atom.getID() + "," + charges[atom.getID()]);
            }
            chargeWriter.println();
            ldosWriter.println();
        }
        nm.getMenu().clearBars();
        ldosWriter.println();

        ldosWriter.close();
        chargeWriter.close();
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
    }

    private static void updateProgressBar(Double n, String name, CommonProperties cp, JProgressBar bar) {
        int percentageSec = (int) (100 * (n - cp.getMin(name))/ cp.getWidth(name));
        if (percentageSec % 5 == 0){
            bar.setValue(percentageSec);
            bar.setString(name + ": " + percentageSec + "%");
        }
    }

    public static double countLocalDensity(Complex M){
        return (-1/Math.PI)*M.getImaginary();
    }
}
