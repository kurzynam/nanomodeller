package org.nanomodeller.Calculation;

import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Globals;
import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.CommonProperties;
import org.nanomodeller.XMLMappingFiles.Matrix;
import org.nanomodeller.XMLMappingFiles.Parameters;


import java.io.*;
import java.util.Arrays;

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
        StringWriter charge = new StringWriter();
        StringWriter avgCharge = new StringWriter();
        Matrix matrix = new Matrix(par);
        boolean isSymmetric = matrix.isSymmetric();
        int numOfAtoms = par.getAtoms().size();
        double invNumOfAtoms = 1.0 / numOfAtoms;
        float[] charges = new float[numOfAtoms];
        boolean shouldComputeT = cp.shouldCompute(Globals.time);
        boolean shouldComputeN = cp.shouldCompute(Globals.n);
        float eWidth = cp.getWidth(Globals.energy);
        float mWidth = cp.getWidth(Globals.n);
        float tWidth = cp.getWidth(Globals.time);
        float maxN = cp.getMax(Globals.n);
        float incN = cp.getInc(Globals.n);
        float incT = cp.getInc(Globals.time);
        float incE = cp.getInc(Globals.energy);
        float tMin = cp.getMin(Globals.time);
        float eMin = cp.getMin(Globals.energy);
        float minN = cp.getMin(Globals.n);
        float n = minN;

        do {
            if (isFirstThread) {
                updateProgressBar(n - minN, Globals.n, mWidth, NanoModeler.getInstance().getMenu().getThirdPB());
            }
            float t = tMin;
            float maxT = cp.getMax(Globals.time);
            do {
                Arrays.fill(charges, 0);
                if (isFirstThread) {
                    updateProgressBar(t - tMin, Globals.time, tWidth, NanoModeler.getInstance().getMenu().getSecondPB());
                }

                float tempE = eMin;
                do {
                    if (isFirstThread) {
                        updateProgressBar(tempE - eMin, Globals.energy, eWidth, NanoModeler.getInstance().getMenu().getFirstPB());
                    }
                    ZMatrixRMaj M = matrix.convertToComplexMatrix(n, t, tempE);
                    ZMatrixRMaj TempMatrix = new ZMatrixRMaj(M.numRows, M.numCols);
                    CommonOps_ZDRM.invert(M, TempMatrix);
                    int i = 0;

                    if (shouldComputeT)
                        append(ldos,t + "\t\t\t");
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

                } while (tempE <= cp.getMax(Globals.energy));
                    float avg = 0;
                    int num = 0;
                    for (float v : charges) {
//                    if (shouldComputeN)
//                        append(charge,n +"\t\t\t");
//                    append(charge, num++ + "\t\t\t" + v + "\n");
                    avg += v;
                }
                append(charge,"\n");
                append(ldos, "\n");
//                avg = charges[4];
                avg *= invNumOfAtoms;
                if (shouldComputeN){
                    append(avgCharge, n + "\t\t\t");

                }

                if (shouldComputeT)
                    append(avgCharge,t + "\t\t\t");
                append(avgCharge, avg + "\n");

                t += incT;
            } while (t <= maxT);
            append(charge,"\n");
            append(ldos,"\n");
            append(avgCharge,"\n");
            n += incN;
        } while (n <= maxN);
        this.charge = charge.toString();
        this.avgCharge = avgCharge.toString();
        this.ldos = ldos.toString();
    }
    public void append(StringWriter writer, String toWrite){
        if(writer != null)
            writer.append(toWrite);
    }
}
