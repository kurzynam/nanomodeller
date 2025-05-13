package org.nanomodeller.Calculation;

import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.CommonProperties;
import org.nanomodeller.XMLMappingFiles.Matrix;
import org.nanomodeller.XMLMappingFiles.Parameters;


import java.io.*;

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
        try {
            calculate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCharge() {
        return charge;
    }

    public String getLdos() {
        return ldos;
    }

    private void calculate() throws IOException {
        StringWriter ldos = new StringWriter();
        StringWriter charge = null;//new StringWriter();
        StringWriter avgCharge = new StringWriter();
        Matrix matrix = new Matrix(par);

        int numOfAtoms = par.getAtoms().size();
        double invNumOfAtoms = 1.0 / numOfAtoms;
        float[] charges = new float[numOfAtoms];
        boolean shouldComputeN = cp.shouldCompute("n");
        boolean shouldComputeM = cp.shouldCompute("m");
        float eWidth = cp.getWidth("E");
        float mWidth = cp.getWidth("m");
        float nWidth = cp.getWidth("n");
        float maxM = cp.getMax("m");
        float incM = cp.getInc("m");
        float incN = cp.getInc("n");
        float incE = cp.getInc("E");
        float nMin = cp.getMin("n");
        float eMin = cp.getMin("E");
        float minM = cp.getMin("m");
        float m = minM;

        do {
            if (isFirstThread) {
                updateProgressBar(m - minM, "m", mWidth, NanoModeler.getInstance().getMenu().getThirdPB());
            }
            float n = nMin;
            float maxN = cp.getMax("n");
            do {
                for (int i = 0; i < numOfAtoms; i++) {
                    charges[i] = 0;
                }
                if (isFirstThread) {
                    updateProgressBar(n - nMin, "n", nWidth, NanoModeler.getInstance().getMenu().getSecondPB());
                }

                float tempE = eMin;
                do {
                    if (isFirstThread) {
                        updateProgressBar(tempE - eMin, "E", eWidth, NanoModeler.getInstance().getMenu().getFirstPB());
                    }
                    ZMatrixRMaj M = matrix.convertToComplexMatrix(m, n, tempE);
                    ZMatrixRMaj TempMatrix = new ZMatrixRMaj(M.numRows, M.numCols);
                    CommonOps_ZDRM.invert(M, TempMatrix);
                    int i = 0;

                    if (shouldComputeN)
                        append(ldos,n + "\t\t\t");
                    append(ldos,tempE + "");
                    for (Atom atom : par.getAtoms()) {
                        double imag = TempMatrix.getImag(atom.getID(), atom.getID());
                        double result = countLocalDensity(imag);
                        if (tempE <= 0)
                            charges[i++] += result * incE;
                        append(ldos,"\t\t\t" + result);
                    }
                    append(ldos,"\n");
                    tempE += incE;

                } while (tempE <= cp.getMax("E"));
                float avg = 0;
                int num = 0;
                for (float v : charges) {
                    if (shouldComputeN)
                        append(charge,n +"\t\t\t");
                    append(charge, num++ + "\t\t\t" + v + "\n");
                    avg += v;
                }
                append(charge,"\n");
                append(ldos, "\n");
                avg *= invNumOfAtoms;
                if (shouldComputeM)
                    append(avgCharge, m + "\t\t\t");
                if (shouldComputeN)
                    append(avgCharge,n +"\t\t\t");
                append(avgCharge, avg + "\n");

                n += incN;
            } while (n <= maxN);
            append(charge,"\n");
            append(ldos,"\n");
            append(avgCharge,"\n");
            m += incM;
        } while (m <= maxM);
//        this.charge = charge.toString();
        this.avgCharge = avgCharge.toString();
        this.ldos = ldos.toString();
    }
    public void append(StringWriter writer, String toWrite){
        if(writer != null)
            writer.append(toWrite);
    }
}
