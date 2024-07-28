package org.nanomodeller.Calculation;

import org.jscience.mathematics.vector.ComplexMatrix;
import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.CommonProperties;
import org.nanomodeller.XMLMappingFiles.Matrix;
import org.nanomodeller.XMLMappingFiles.Parameters;

import java.io.StringWriter;

import static org.nanomodeller.Calculation.Tools.ProgressBarState.updateProgressBar;
import static org.nanomodeller.Calculation.StaticCalculations.countLocalDensity;


public class StaticCalculationsRunnable implements Runnable {
    private volatile String charge;
    private volatile String ldos;
    public boolean isFirst;

    private double start;

    private double end;

    private CommonProperties cp;

    private Parameters par;

    public StaticCalculationsRunnable(double start, double end, CommonProperties cp, Parameters par) {
        this.start = start;
        this.end = end;
        this.cp = cp;
        this.par = par;
    }

    @Override
    public void run() {
        calculate();
    }

    public String getCharge() {
        return charge;
    }

    public String getLdos() {
        return ldos;
    }

    private void calculate() {
        StringWriter ldos = new StringWriter();
        StringWriter charge = new StringWriter();
        Matrix matrix = Matrix.readMatrixFromDataFile(par, cp);
        double[] charges = new double[par.getAtoms().size()];
        StringBuilder header = new StringBuilder();
        boolean saveAnyLDOS = false;
        for (Atom a : par.getAtoms()){
            if(a.getBool("Save")) {
                header.append(", ").append(a.getID());
                saveAnyLDOS = true;
            }
        }
        if (saveAnyLDOS)
            ldos.append("\n");
        for (double n = start; n < end; n += cp.getInc("n")) {
            for (int i = 0; i < par.getAtoms().size(); i++){
                charges[i] = 0;
            }
            matrix.getParser().addVariable("n", n);
            if (isFirst)
                updateProgressBar(n - start, "n", end - start, NanoModeler.getInstance().getMenu().getSecondPB());
            for (double tempE : cp.getVar("E")) {
                if (isFirst)
                    updateProgressBar(tempE - cp.getMin("E"), "E", cp.getWidth("E"), NanoModeler.getInstance().getMenu().getFirstPB());
                String results = "";
                matrix.getParser().addVariable("E", tempE);
                ComplexMatrix M = matrix.convertToComplexMatrix();
                ComplexMatrix TempMatrix = M.inverse();
                for (Atom atom : par.getAtoms() ){
                    double result = countLocalDensity(TempMatrix.get(atom.getID(), atom.getID()));
                    if(atom.getBool("Save"))
                        results +=   "," + result;
                    if (tempE <= 0)
                        charges[atom.getID()] += result * cp.getInc("E");
                }
                if (saveAnyLDOS){
                    ldos.append(n +"," + tempE + results + "\n");
                }
            }
            for (Atom atom : par.getAtoms()) {
                charge.append(n + "," + atom.getID() + "," + charges[atom.getID()] + "\n");
            }
            charge.append("\n");
            if (saveAnyLDOS)
                ldos.append("\n");
        }
        double avg = 0;
        for( Double ch : charges){
           avg+= ch;
        }
        System.out.println(avg/20);
        this.charge =  charge.toString();
        this.ldos = ldos.toString();

    }


}
