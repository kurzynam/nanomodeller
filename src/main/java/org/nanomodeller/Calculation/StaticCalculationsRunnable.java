package org.nanomodeller.Calculation;

import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
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
    private volatile String avgCharge;
    public boolean isFirstThread;

    private CommonProperties cp;
    private Parameters par;

    public StaticCalculationsRunnable(CommonProperties cp, Parameters par) {
        this.cp = cp;
        this.par = par;
    }

    public String getAvgCharge() {
        return avgCharge;
    }

    public void setAvgCharge(String avgCharge) {
        this.avgCharge = avgCharge;
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
        StringWriter avgCharge = new StringWriter();
        Matrix matrix = new Matrix(par);
        double[] charges = new double[par.getAtoms().size()];
        double m = cp.getMin("m");

        do {
            if (isFirstThread)
                updateProgressBar(m - cp.getMin("m"), "m", cp.getWidth("m"), NanoModeler.getInstance().getMenu().getThirdPB());
            double n = cp.getMin("n");
            do {
                for (int i = 0; i < par.getAtoms().size(); i++) {
                    charges[i] = 0;
                }
                if (isFirstThread)
                    updateProgressBar(n - cp.getMin("n"), "n", cp.getWidth("n"), NanoModeler.getInstance().getMenu().getSecondPB());

                double tempE = cp.getMin("E");
                do {
                    if (isFirstThread)
                        updateProgressBar(tempE - cp.getMin("E"), "E", cp.getWidth("E"), NanoModeler.getInstance().getMenu().getFirstPB());
                    ZMatrixRMaj M = matrix.convertToComplexMatrix(m, n, tempE);
                    ZMatrixRMaj TempMatrix = new ZMatrixRMaj(M.numRows, M.numCols);
                    CommonOps_ZDRM.invert(M, TempMatrix);
                    int i = 0;
                    for (Atom atom : par.getAtoms()) {
                        double real = TempMatrix.getReal(atom.getID(), atom.getID());
                        double imag = TempMatrix.getImag(atom.getID(), atom.getID());
                        double result = countLocalDensity(imag);
                        if (tempE <= 0)
                            charges[i++] += result * cp.getInc("E");
                    }
                    tempE += cp.getInc("E");

                } while (tempE <= cp.getMax("E"));
                double avg = 0;
                for (double v : charges) {
                    avg += v;
                }
                avg /= charges.length;
                if (cp.shouldCompute("m"))
                    avgCharge.append(m + ",");
                if (cp.shouldCompute("n"))
                    avgCharge.append(n +",");
                avgCharge.append(avg + "\n");
                n += cp.getInc("n");
            } while (n <= cp.getMax("n"));
            avgCharge.append("\n");
            m += cp.getInc("m");
        } while (m <= cp.getMax("m"));
        this.charge = charge.toString();
        this.avgCharge = avgCharge.toString();
        this.ldos = ldos.toString();
    }
}
