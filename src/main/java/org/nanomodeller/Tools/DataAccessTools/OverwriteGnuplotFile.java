package org.nanomodeller.Tools.DataAccessTools;

import org.nanomodeller.Globals;
import org.nanomodeller.XMLMappingFiles.CommonProperties;

import static org.nanomodeller.Globals.*;
import static org.nanomodeller.Tools.StringUtils.nvl;


public class OverwriteGnuplotFile {

//    public static void overwriteGnuplotFile(int i, String gnuplotFilePath, String dataFilePath){
//        MyFileWriter mw = new MyFileWriter(gnuplotFilePath);
//        CommonProperties gp = CommonProperties.getInstance();
//        mw.println("path = '" + dataFilePath  + i + TXT + "'");
//        fillStaticGnuplotProperties(mw, gp);
//    }
//
//    public static void overwriteDynamicChargeFile(int i, String stepName) {
//        overwriteGnuplotFile(i, DYNAMIC_CHARGE_GNUPLOT_FILE_PATH, CommonProperties.getInstance().getDynamicPATH() + "/" +Globals.CHARGE_FILE_NAME_PATTERN);
//    }
//    public static void overwriteDynamicFermiLDOSFile(int i, String stepName) {
//        overwriteGnuplotFile(i, DYNAMIC_FERMI_LDOS_GNUPLOT_FILE_PATH, CommonProperties.getInstance().getDynamicPATH() + "/" +Globals.FERMI_LDOS_FILE_NAME_PATTERN);
//    }
//
//    public static void overwriteLastTFile(String filePattern, int i){
//        String lastTpattern = "";
//        if (filePattern.contains(Globals.LDOS_FILE_NAME_PATTERN)){
//            lastTpattern = Globals.LAST_T_LDOS_PATTERN;
//        }else if (filePattern.equals(Globals.NORMALISATION_FILE_NAME_PATTERN)){
//            lastTpattern = Globals.LAST_T_NORMALISATION_PATTERN;
//        }
//        MyFileWriter mw = new MyFileWriter(Globals.LAST_T_GNUPLOT_FILE_PATH);
//        CommonProperties gp = CommonProperties.getInstance();
//        String path = gp.getDynamicPATH() + lastTpattern + i + TXT;
//        mw.println("path = '" + path + "'");
//        mw.println("set key off");
//        mw.println("set samples 10000");
//        mw.printf("plot path u 2:3 with lines  lw 3 smooth csplines \n");
//        mw.close();
//    }
    public static void overwriteDynamicAVGDOSGnuplotFile(String path) {
        MyFileWriter mw = new MyFileWriter(DYNAMIC_AVGDOS_GNUPLOT_FILE_PATH);
        CommonProperties gp = CommonProperties.getInstance();
        mw.println("path = '" + path + "/AVGDOS"  + TXT + "'");
        fillDynamicGnuplotProperties(mw, gp, "AVGDOS(E,t)" );
        mw.close();
    }

    public static void overwriteDynamicTDOSGnuplotFile(String path) {
        MyFileWriter mw = new MyFileWriter(DYNAMIC_TDOS_GNUPLOT_FILE_PATH);
        CommonProperties gp = CommonProperties.getInstance();
        mw.println("path = '" + path + "/TDOS"  + TXT + "'");
        fillDynamicGnuplotProperties(mw, gp, "TDOS(E,t)" );
        mw.close();
    }
    public static void fillStaticGnuplotProperties(MyFileWriter mw, CommonProperties gp){

        mw.println("set key off");
        mw.printf("set xrange [%s]\n", gp.getString("xrange"));
        mw.printf("set yrange [%s]\n", gp.getString("yrange"));
        mw.printf("plot path u 1:2 every %s with lines  lw 3 smooth csplines\n", gp.getString("everyE"));
        mw.println("pause 1000");
        mw.close();
    }

    public static void fillDynamicGnuplotProperties(MyFileWriter mw, CommonProperties gp, String type){
        mw.println("set key off");
        mw.println("set grid");
        mw.println("set pm3d implicit at s");
        mw.println("set xtics font \"Verdana,7\"");
        mw.println("set ytics font \"Verdana,7\"");
        mw.println("set ztics font \"Verdana,7\"");
        mw.println("unset ztics");
        mw.println("set tics font \"Helvetica,7\"");
        mw.println("set xlabel 't' offset -9");
        mw.println("set ytics offset 0, -7");
        mw.println("set ylabel 'E' offset 0, -10");
        mw.printf("set title '%s' offset 0,-2\n", type);
        mw.printf("set view %s,%s,1,1\n", gp.getString("verticalView"),gp.getString("horizontalView"));
        mw.printf("set xrange [%s]\n", gp.getString("xrange"));
        mw.printf("set yrange [%s]\n", gp.getString("yrange"));
        mw.printf("set zrange [%s]\n", gp.getString("Zrange"));
        mw.printf("splot path u 1:2:3 every %s:%s with pm3d title '{/*1.5 %s}' \n", gp.getString("everyE"), gp.getString("everyT"),type);
        mw.println("pause 1000");
        mw.close();
    }
}
