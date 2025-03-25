package org.nanomodeller.Calculation;

import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Tools.DataAccessTools.FileOperationHelper;
import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.CommonProperties;
import org.nanomodeller.XMLMappingFiles.Matrix;
import org.nanomodeller.XMLMappingFiles.Parameters;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

        float[] charges = new float[par.getAtoms().size()];
        float m = cp.getMin("m");

        do {
            if (isFirstThread)
                updateProgressBar(m - cp.getMin("m"), "m", cp.getWidth("m"), NanoModeler.getInstance().getMenu().getThirdPB());
            float n = cp.getMin("n");
            do {
                for (int i = 0; i < par.getAtoms().size(); i++) {
                    charges[i] = 0;
                }
                if (isFirstThread)
                    updateProgressBar(n - cp.getMin("n"), "n", cp.getWidth("n"), NanoModeler.getInstance().getMenu().getSecondPB());

                float tempE = cp.getMin("E");
                do {
                    if (isFirstThread)
                        updateProgressBar(tempE - cp.getMin("E"), "E", cp.getWidth("E"), NanoModeler.getInstance().getMenu().getFirstPB());
                    ZMatrixRMaj M = matrix.convertToComplexMatrix(m, n, tempE);
                    ZMatrixRMaj TempMatrix = new ZMatrixRMaj(M.numRows, M.numCols);
                    CommonOps_ZDRM.invert(M, TempMatrix);
                    int i = 0;

                    if (cp.shouldCompute("n"))
                        ldos.append(n +",");
                    ldos.append(BigDecimal.valueOf(tempE).setScale(3, RoundingMode.HALF_UP) + "");
                    for (Atom atom : par.getAtoms()) {
                        double imag = TempMatrix.getImag(atom.getID(), atom.getID());
                        float result = countLocalDensity(imag);
                        if (tempE <= 0)
                            charges[i++] += result * cp.getInc("E");
                        ldos.append("\t\t\t" + BigDecimal.valueOf(result).setScale(4, RoundingMode.HALF_UP));
                    }
                    ldos.append("\n");
                    tempE += cp.getInc("E");

                } while (tempE <= cp.getMax("E"));
                float avg = 0;
                int num = 0;
                for (float v : charges) {
                    if (cp.shouldCompute("n"))
                        charge.append(n +"\t\t\t");
                    charge.append(num++ + "\t\t\t" + BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP) + "\n");
                    avg += v;
                }
                charge.append("\n");
                avg /= charges.length;
                if (cp.shouldCompute("m"))
                    avgCharge.append(m + "\t\t\t");
                if (cp.shouldCompute("n"))
                    avgCharge.append(n +"\t\t\t");
                avgCharge.append(BigDecimal.valueOf(avg).setScale(4, RoundingMode.HALF_UP) + "\n");
                n += cp.getInc("n");
            } while (n <= cp.getMax("n"));
            charge.append("\n");
            ldos.append("\n");
            avgCharge.append("\n");
            m += cp.getInc("m");
        } while (m <= cp.getMax("m"));
        this.charge = charge.toString();
        this.avgCharge = avgCharge.toString();
        this.ldos = ldos.toString();
    }
}
