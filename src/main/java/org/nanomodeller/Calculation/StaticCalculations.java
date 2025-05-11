package org.nanomodeller.Calculation;

import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.*;

import static org.nanomodeller.Constants.MRPI;

public class StaticCalculations {

    public static void countStaticProperties() {
        int cores = 1;
        Thread[] threads = new Thread[cores];
        StaticCalculationsRunnable[] spr = new StaticCalculationsRunnable[cores];
        CommonProperties cp = CommonProperties.getInstance();
        NanoModeler.getInstance().getMenu().clearBars();
        long startTime = System.currentTimeMillis();
        MyFileWriter ldosWriter = new MyFileWriter(Parameters.getInstance().getPath() + "/" + cp.getString("staticLDOSFileName"));
        MyFileWriter chargeWriter = new MyFileWriter(Parameters.getInstance().getPath() + "/" + cp.getString("staticChargeFileName"));
        MyFileWriter avgChargeWriter = new MyFileWriter(Parameters.getInstance().getPath() + "/avg" + cp.getString("staticChargeFileName"));

        for (int i = 0; i < threads.length; i++) {
            double width = cp.getWidth("n") / threads.length;
            double start = cp.getMin("n") + i * width;
            double end = start + width;
            spr[i] = new StaticCalculationsRunnable(CommonProperties.getInstance().clone(), Parameters.getInstance().clone());
            if (i == 0)
                spr[i].isFirstThread = true;
            threads[i] = new Thread(spr[i]);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        StringBuilder header = new StringBuilder();
        boolean saveAnyLDOS = false;

        for (Atom a : Parameters.getInstance().getAtoms()) {
            if (a.getBool("Save")) {
                header.append("\t\t\t").append(a.getID());
                saveAnyLDOS = true;
            }
        }
        if (saveAnyLDOS) {
            ldosWriter.print("n\t\t\tE" + header + "\n");
        }
        chargeWriter.print("n\t\t\ti\t\t\tq\n");
        avgChargeWriter.print("m\t\t\tn\t\t\tq\n");
        for (StaticCalculationsRunnable s : spr) {
            if (StringUtils.isNotEmpty(s.getCharge()))
                chargeWriter.print(s.getCharge());
            if (StringUtils.isNotEmpty(s.getAvgCharge()))
                avgChargeWriter.print(s.getAvgCharge());
            if (StringUtils.isNotEmpty(s.getLdos()))
                ldosWriter.print(s.getLdos());
        }
        ldosWriter.close();
        chargeWriter.close();
        avgChargeWriter.close();
        NanoModeler.getInstance().getMenu().clearBars();
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
    }

    public static double countLocalDensity(double im) {
        return im * MRPI;
    }
}
