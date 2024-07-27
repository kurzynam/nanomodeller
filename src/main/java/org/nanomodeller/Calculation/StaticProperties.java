package org.nanomodeller.Calculation;


import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Globals;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.*;
import org.jscience.mathematics.number.Complex;
import org.jscience.mathematics.vector.ComplexMatrix;

import javax.swing.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;

public class StaticProperties {


    public static void countStaticProperties(){

        int cores = 3;//Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[cores];
        StaticPropertiesRunnable[] spr = new StaticPropertiesRunnable[cores];
        CommonProperties cp = CommonProperties.getInstance();
        NanoModeler.getInstance().getMenu().clearBars();
        long startTime = System.currentTimeMillis();
        MyFileWriter ldosWriter = new MyFileWriter(Parameters.getInstance().getPath() + "/" + cp.getString("staticLDOSFileName"));
        MyFileWriter chargeWriter = new MyFileWriter(Parameters.getInstance().getPath() + "/" +cp.getString("staticChargeFileName"));
        for (int i = 0; i < threads.length; i++){
            double width = cp.getWidth("n")/threads.length;
            double start = cp.getMin("n") + i * width;
            double end = start +  width;
            spr[i] = new StaticPropertiesRunnable(start, end, CommonProperties.getInstance().clone(),Parameters.getInstance().clone());
            if (i == 0)
                spr[i].isFirst = true;
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
        for (Atom a : Parameters.getInstance().getAtoms()){
            if(a.getBool("Save")) {
                header.append(",").append(a.getID());
                saveAnyLDOS = true;
            }
        }
        if (saveAnyLDOS) {
            ldosWriter.print("n,E" + header + "\n");
        }
        chargeWriter.print("n,i,q\n");

//        for (StaticPropertiesRunnable s : spr){
//            if (StringUtils.isNotEmpty(s.getLdos()))
//                ldosWriter.print(s.getLdos());
//        }

        for (StaticPropertiesRunnable s : spr){
            if (StringUtils.isNotEmpty(s.getCharge()))
                chargeWriter.print(s.getCharge());
            if (StringUtils.isNotEmpty(s.getLdos()))
                ldosWriter.print(s.getLdos());
        }

        ldosWriter.close();
        chargeWriter.close();
        NanoModeler.getInstance().getMenu().clearBars();
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);

    }


    public static double countLocalDensity(Complex M){
        return (-1/Math.PI)*M.getImaginary();
    }













}
