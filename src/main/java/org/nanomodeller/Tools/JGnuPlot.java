package org.nanomodeller.Tools;

import org.nanomodeller.GUI.Shapes.AtomShape;
import org.nanomodeller.Globals;
import org.nanomodeller.XMLMappingFiles.GlobalProperties;
import com.panayotis.gnuplot.GNUPlotParameters;
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.FileDataSet;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.nanomodeller.Globals.GNUPLOT_PATH;


public class JGnuPlot extends JavaPlot {

    public GlobalProperties getProperties() {
        return properties;
    }

    GNUPlotParameters parameters;
    GlobalProperties properties;
    public JGnuPlot(boolean is3D){
        super(GNUPLOT_PATH, is3D);
        parameters = new GNUPlotParameters(is3D);
        setParameters(parameters);
        properties = GlobalProperties.getInstance();
    }
    public void setMultiplotStyle(){
        setMapView(true);
        setTopMargin(1 - getProperties().getMargin());
        setGap(getProperties().getMargin());
        setBottomMargin(getProperties().getMargin());
        setLeftMargin(getProperties().getMargin());
        setRightMargin(1 - getProperties().getMargin());
        setMultiplotLayout(properties.getMultiplotRows(),
                properties.getMultiplotCols(),"Colums first".equals(properties.getRowsFirst()));
        //unsetColorBox();
    }

    public JGnuPlot (){
        super(false);
        parameters = new GNUPlotParameters(false);
        setParameters(parameters);
        properties = GlobalProperties.getInstance();
    }


    public void appendCommand(String command){
        parameters.getPreInit().add(command + "\n");
    }

    public void readXMLGraphProperties(){
        setPalette();
        setAllticsFont();
        if (StringUtils.isNotEmpty(properties.getxRange())){
            setXrange(properties.getxRange());
        }
        if (StringUtils.isNotEmpty(properties.getyRange())){
            setYrange(properties.getyRange());
        }
        if (StringUtils.isNotEmpty(properties.getzRange())){
            setZrange(properties.getzRange());
        }
        setGrid(properties.isShowGrid());
        if (Globals.MULTIPLOT.equals(getProperties().getMultiplotStyle())){
            setMultiplotStyle();
        }
        appendCommand(properties.getCustomGnuplotCommands());
        appendCommand("set datafile separator \",\"");
    }

    private void setGrid(boolean showGrid) {
        if(showGrid){
            appendCommand("set grid");
        }
        else {
            appendCommand("unset grid");
        }
    }

    public void setYrange(String yrange){
        appendCommand("set yrange ["+yrange+"]");
    }

    public void setXrange(String xrange){
        appendCommand("set xrange ["+xrange+"]");
    }

    public void setZrange(String zrange){
        appendCommand("set zrange ["+zrange+"]");
    }
    public void addFilePath(String path) {
        try {
              this.addPlot(new FileDataSet(new File(path)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void setPlotStyle(int plotNumber){
        PlotStyle stl = ((AbstractPlot) getPlots().get(plotNumber)).getPlotStyle();
        stl.setStyle(Style.LINESPOINTS);
    }
    public void setSamples(int samples){
        appendCommand("set samples " + samples);
    }
    public void setXrange(double xmin, double xmax){
        getAxis("x").setBoundaries(xmin, xmax);
    }
    public void unsetXTics(){
        appendCommand("unset xtics");
    }
    public void unsetYTics(){
        appendCommand("unset ytics");
    }
    public void setXTics(){
        appendCommand("set xtics");
    }
    public void setYTics(){
        appendCommand("set ytics");
    }
    public void setRightMargin(double margin){
        appendCommand("if (!exists(\"MP_RIGHT\"))   MP_RIGHT = " + 0.82);
    }
    public void setBottomMargin(double margin){
        appendCommand("if (!exists(\"MP_BOTTOM\"))   MP_BOTTOM = " + 0.18);
    }
    public void setTopMargin(double margin){
        appendCommand("if (!exists(\"MP_TOP\"))   MP_TOP = " + 0.92);
    }
    public void setGap(double gap){
        appendCommand("if (!exists(\"MP_GAP\"))   MP_GAP = " + gap);
    }
    public void setLeftMargin(double margin){
        appendCommand("if (!exists(\"MP_LEFT\"))   MP_LEFT = " + 0.18);
    }
    public void setMapView(boolean isViewMap){
        if (isViewMap)
            appendCommand("set view map");
        else
            appendCommand("unset view map");
    }

    public void setYrange(double ymin, double ymax){
        getAxis("y").setBoundaries(ymin, ymax);
    }
    public void setZrange(double zmin, double zmax){
        getAxis("z").setBoundaries(zmin, zmax);
    }

    public void setMultiplotLayout(int rows, int cols, boolean colsfirst) {
        String order = colsfirst ? "columnsfirst" : "rowsfirst";
        appendCommand("set multiplot layout "+ rows + "," + cols + " " + order + " margins screen MP_LEFT, MP_RIGHT, MP_BOTTOM, MP_TOP spacing screen MP_GAP");
        appendCommand("set ytics offset " + properties.getyTicsOffset()+",0");
        appendCommand("set xtics offset 0," + properties.getxTicsOffset());
    }
    public void unsetColorBox(){
        appendCommand("unset colorbox");
    }
    public void unsetMultiplot(){
        appendCommand("unset multiplot");
    }
    public void setCbRange(double cbmin, double cbmax){
        appendCommand("set cbrange ["+cbmin+":"+cbmax+"]");
    }
    public void pause(int time){
        appendCommand("pause " + time);
    }

    public void setPalette(){
        ArrayList<String> sortedKeys = new ArrayList(properties.getPaletteColors().keySet());
        Collections.sort(sortedKeys);
        String result = "set palette defined (";

        for(Iterator<String> i = sortedKeys.iterator(); i.hasNext();) {
            String key = i.next();
            result += Double.parseDouble(key) + " " + StringUtils.toDoubleQuotes(properties.
                    getPaletteColors().get(key).toLowerCase().replace(" ", "-"));
            if(i.hasNext()) {
                result += ",";
            }
        }
        result += ")";
        appendCommand(result);
    }

    public void addPlotCommand(String path, ArrayList<AtomShape> shapes, ArrayList<String> selectedSteps){
        addPlotCommand(path, shapes, selectedSteps,"");
    }
    public void addSplotCommand(String path, ArrayList<AtomShape> shapes, ArrayList<String> selectedSteps){
        addSplotCommand(path, shapes,selectedSteps,"");
    }
    public void addSplotCommand(String path, ArrayList<AtomShape> shapes,
                                ArrayList<String> selectedSteps, String title){

        boolean colsFirst = "Colums first".equals(properties.getRowsFirst());
        boolean isMultiplot = true;
        setZticsFont();
        int cols = properties.getMultiplotCols();

        Collections.sort(selectedSteps);
        int stepsSize = selectedSteps.size();
        int shapesSize = shapes.size();
        int maxGraphInex = shapesSize*stepsSize -1;
        int rows = (int)Math.ceil((maxGraphInex+0.0)/cols);
        String style = getProperties().getFont();
        double size = getProperties().getTextSize();
        appendCommand(" set label 1 center at graph 1,1.1 font " + StringUtils.toDoubleQuotes(style +"," +size));
        for (int j = 0; j < stepsSize; j++){
            String step = selectedSteps.get(j);
            for (int i = 0; i < shapesSize; i++) {

                //appendCommand(String.format("set label 1 %s",StringUtils.toSingleQuotes((shapes.get(i).getID()+1) + "")));
                if(isMultiplot){
                    if(colsFirst){

                    }
                    else{
                        int graphNr = j*stepsSize + i;
                        if (graphNr % cols == 0){
                            setYticsFont();
                            appendCommand("set ylabel 'ε-ε_0' rotate by 0");
                        }
                        else {
                            unsetYTics();
                            appendCommand("unset ylabel");
                        }
                        if (graphNr == maxGraphInex){
                            setXticsFont();
                        }
                        else {
                            //appendCommand(String.format("set label 11 %s",StringUtils.toSingleQuotes((i+1) + "")));
                            if (graphNr > maxGraphInex - cols){
                                setXticsFont();;
                                appendCommand("set xlabel 't'");
                            }
                            else {
                                unsetXTics();
                                appendCommand("unset xlabel");
                            }
                        }
                    }
                }

                int id = shapes.get(i).getID() + 4;
                String e_zero = shapes.get(i).getAtom().getString("OnSiteEnergy").toString();
                String ids = "2:($3-3):" + id;
                String command = String.format("splot %s i %s u %s every  %s:%s title %s with pm3d",
                        StringUtils.toSingleQuotes(path), step, ids, properties.getEveryT(),
                        properties.getEveryE(), StringUtils.toSingleQuotes(title));
                appendCommand(command);
            }
        }
    }

    public void addPlotCommand(String path, ArrayList<AtomShape> shapes,ArrayList<String> selectedSteps, String title){

            String command = "plot ";
            int counter = 0;
            int size = selectedSteps.size();
            if (size > 0){
                for (int j = 0; j < size; j++){
                    String step = selectedSteps.get(j);
                    for (int i = 0; i < shapes.size(); i++) {
                        int id = shapes.get(i).getID() + 3;
                        double incr = counter++ * getProperties().getOffsetStep();
                        String ids = "2:" + String.format("($%d + %f)", id, incr);
                        command += String.format("%s u %s every :::%s::%s title %s with lines lw 2 smooth csplines",
                                StringUtils.toSingleQuotes(path), ids, step, step, StringUtils.toSingleQuotes(title));
                        if (i != shapes.size() - 1) {
                            command += ", ";
                        }
                    }
                    if (j != selectedSteps.size() - 1) {
                        command += ", ";
                    }
                }
            }else{
                for (int i = 0; i < shapes.size(); i++) {
                    int num = shapes.get(i).getID()  + 1;
                    title = num + "";
                    int id = shapes.get(i).getID() + 3;
                    double incr = counter++ * getProperties().getOffsetStep();
                    String ids = "2:" + String.format("($%d + %f)", id, incr);
                    command += String.format("%s u %s title %s with lines lw 2 smooth csplines",
                            StringUtils.toSingleQuotes(path), ids, StringUtils.toSingleQuotes(title));
                    if (i != shapes.size() - 1) {
                        command += ", ";
                    }
                }
            }
            //command += " with lines lw 2 smooth csplines";
            appendCommand(command);
    }

    public void add2DSplotCommand(String path, ArrayList<AtomShape> shapes, String title){

        String command = "splot ";
        for (int i = 0; i < shapes.size(); i++) {
            int id = shapes.get(i).getID() + 3;
            String ids = "1:2:" + id;
            command += StringUtils.toSingleQuotes(path) + " u " + ids  +" title " + StringUtils.toSingleQuotes(title);
            if (i != shapes.size() - 1) {
                command += ", ";
            }
        }
        command += " with pm3d";
        appendCommand(command);
    }
    public void add2DSplotCommandMultiplot(String path, ArrayList<AtomShape> shapes, String title){

        boolean colsFirst = "Colums first".equals(properties.getRowsFirst());
        int rows = properties.getMultiplotRows();
        int cols = properties.getMultiplotCols();

        for (int i = 0; i < shapes.size(); i++) {
            if (colsFirst){
                if (i % rows == 0){
                    setYTics();
                }
                else{
                    unsetXTics();
                }
            }
            String command = "splot ";
            int id = shapes.get(i).getID() + 3;
            String ids = "1:2:" + id;
            command += StringUtils.toSingleQuotes(path) + " u " + ids  +" title " + StringUtils.toSingleQuotes(title);
            command += " with pm3d";
            appendCommand(command);
        }

    }

    public void addPlotCommandMultiplot(String path, ArrayList<AtomShape> shapes,ArrayList<String> selectedSteps, String title){

        int counter = 0;
        for (int j = 0; j < selectedSteps.size(); j++){
            String step = selectedSteps.get(j);
            for (int i = 0; i < shapes.size(); i++) {
                String command = "plot ";
                int id = shapes.get(i).getID() + 3;
                double incr = counter++ * getProperties().getOffsetStep();
                String ids = "2:" + String.format("($%d + %f)", id, incr);
                command += StringUtils.toSingleQuotes(path) + " u " + ids +String.format(" every :::%s::%s ", step,step) +" title " + StringUtils.toSingleQuotes(title);
                command += " with lines lw 2";
                appendCommand(command);
            }
        }
    }

//    public void crossSection(String path, ArrayList<AtomShape> shapes,ArrayList<String> selectedSteps, String crossection, String title) {
//
//        String command = "plot ";
//        int counter = 0;
//        for (int j = 0; j < selectedSteps.size(); j++) {
//            String step = selectedSteps.get(j);
//            for (int i = 0; i < shapes.size(); i++) {
//                double incr = counter++ * getProperties().getOffsetStep();
//                int id = shapes.get(i).getID() + 4;
//                String ids = "2:" + String.format("($%d + %f)", id, incr);
//                command += String.format("%s i %s u %s every ::%s::%s with lines lw 2 title %s",
//                        StringUtils.toSingleQuotes(path), step, ids, crossection,
//                        crossection, StringUtils.toSingleQuotes(title));
//                if (i != shapes.size() - 1) {
//                    command += ", ";
//                }
//            }
//            if (j != selectedSteps.size() - 1) {
//                command += ", ";
//            }
//        }
//        appendCommand(command);
//    }
//
//    public void addSplotCommand(LinkedHashMap<String, String> plotsWithtitles){
//        String result = "splot ";
//        int counter = 0;
//        for(Iterator<String> i = plotsWithtitles.keySet().iterator(); i.hasNext();) {
//            String key = i.next();
//            double incr = counter * getProperties().getOffsetStep();
//            result += StringUtils.toSingleQuotes(plotsWithtitles.get(key)) +
//                " u 2:1:($3+"+incr+") title '' with pm3d";
//            if(i.hasNext()) {
//                result += ", ";
//            }
//            counter++;
//        }
//        appendCommand(result);
//    }
//
//    public void crossSectionMultiplot(String path, ArrayList<AtomShape> shapes,ArrayList<String> selectedSteps, String crossection, String title) {
//
//        String formattedPath = StringUtils.toSingleQuotes(path);
//        String formattedTitle = StringUtils.toSingleQuotes(title);
//        for (int j = 0; j < selectedSteps.size(); j++) {
//            String step = selectedSteps.get(j);
//            for (int i = 0; i < shapes.size(); i++) {
//                int id = shapes.get(i).getID() + 4;
//                String ids = "2:" + id;
//                String command = String.format("plot %s i %s u %s  every ::%s::%s title %s",
//                        formattedPath, step, ids, crossection, crossection, formattedTitle);
//                appendCommand(command);
//            }
//        }
//    }
//
//    public void crossSection3D(String path, ArrayList<AtomShape> shapes,ArrayList<String> selectedSteps, String crossection, String title) {
//
//        String command = "splot ";
//        String formattedPath = StringUtils.toSingleQuotes(path);
//        String formattedTitle = StringUtils.toSingleQuotes(title);
//        for (int i = 0; i < shapes.size(); i++) {
//            int id = shapes.get(i).getID() + 4;
//            String ids = "1:2:" + id;
//            command = String.format("%s u %s every ::%s::%s with lines lw 2 title %s",
//                    formattedPath, ids, crossection, crossection, formattedTitle);
//            if (i != shapes.size() - 1) {
//                command += ", ";
//            }
//        }
//        appendCommand(command);
//    }
//
//    public void crossSection3DMultiplot(String path, ArrayList<AtomShape> shapes,ArrayList<String> selectedSteps, String crossection, String title) {
//
//        int counter = 0;
//        for (int i = 0; i < shapes.size(); i++) {
//            double incr = counter++ * getProperties().getOffsetStep();
//            int id = shapes.get(i).getID() + 4;
//            String ids = "1:2:" + String.format("($%d + %f)", id, incr);
//            String command = String.format("splot %s u %s every ::%s::%s with lines lw 2 title %s",
//                    StringUtils.toSingleQuotes(path), ids,
//                    crossection, crossection, StringUtils.toSingleQuotes(title));
//            appendCommand(command);
//        }
//    }

    public void crossSection(String path, ArrayList<AtomShape> shapes,ArrayList<String> selectedSteps, String crossection, String title) {

        String command = "plot ";
        for (int j = 0; j < selectedSteps.size(); j++) {
            String step = selectedSteps.get(j);
            for (int i = 0; i < shapes.size(); i++) {
                title = (j+1) + "-" +(i+1);
                int id = shapes.get(i).getID() + 4;
                String ids = "2:" + id;
                command += StringUtils.toSingleQuotes(path) + String.format(" i %s ",step)+" u " + ids
                        + String.format(" every ::%s::%s with linespoints", crossection, crossection)
                        + " title " + StringUtils.toSingleQuotes(title);
                if (i != shapes.size() - 1) {
                    command += ", ";
                }
            }
            if (j != selectedSteps.size() - 1) {
                command += ", ";
            }
        }
        appendCommand(command);
    }

    public void crossSectionMultiplot(String path, ArrayList<AtomShape> shapes,ArrayList<String> selectedSteps, String crossection, String title) {

        for (int j = 0; j < selectedSteps.size(); j++) {
            String step = selectedSteps.get(j);
            for (int i = 0; i < shapes.size(); i++) {
                String command = "plot ";
                int id = shapes.get(i).getID() + 4;
                String ids = "2:" + id;
                command += StringUtils.toSingleQuotes(path) + String.format(" i %s ",step)+" u " + ids
                        + String.format(" every ::%s::%s ", crossection, crossection)
                        + " title " + StringUtils.toSingleQuotes(title);
                appendCommand(command);
            }
        }
    }

    public void crossSection3D(String path, ArrayList<AtomShape> shapes,ArrayList<String> selectedSteps, String crossection, String title) {

        String command = "splot ";
        for (int i = 0; i < shapes.size(); i++) {
            int id = shapes.get(i).getID() + 4;
            String ids = "1:2:" + id;
            command += StringUtils.toSingleQuotes(path) + " u " + ids
                    + String.format(" every ::%s::%s ", crossection, crossection)
                    + " title " + StringUtils.toSingleQuotes(title);
            if (i != shapes.size() - 1) {
                command += ", ";
            }
        }
        appendCommand(command);
    }

    public void crossSection3DMultiplot(String path, ArrayList<AtomShape> shapes,ArrayList<String> selectedSteps, String crossection, String title) {

        for (int i = 0; i < shapes.size(); i++) {
            String command = "splot ";
            int id = shapes.get(i).getID() + 4;
            String ids = "1:2:" + id;
            command += StringUtils.toSingleQuotes(path) + " u " + ids
                    + String.format(" every ::%s::%s ", crossection, crossection)
                    + " title " + StringUtils.toSingleQuotes(title);
            appendCommand(command);
        }
    }

    public void setAllticsFont(){
        String style = getProperties().getFont();
        double size =getProperties().getTextSize();
        setXticsFont(style,size);
        setYticsFont(style,size);
        setZticsFont(style,size);
    }
    public void setAllticsFontStyle(String style) {
        setXticsFontStyle(style);
        setYticsFontStyle(style);
        setZticsFontStyle(style);
    }
    public void setAllticsFontSize(double size) {
        setXticsFontSize(size);
        setYticsFontSize(size);
        setZticsFontSize(size);
    }
    public void setXticsFont(){
        String style = getProperties().getFont();
        double size = getProperties().getTextSize();
        setXticsFont(style,size);
        String xticsStep = getProperties().getXticsStep();
        if (StringUtils.isNotEmpty(xticsStep))
            appendCommand("set xtics " + xticsStep);

    }

    public void setYticsFont(){
        String style = getProperties().getFont();
        double size = getProperties().getTextSize();
        setYticsFont(style,size);
        String yticsStep = getProperties().getYticsStep();
        if (StringUtils.isNotEmpty(yticsStep))
            appendCommand("set ytics " + yticsStep);
    }

    public void setZticsFont(){
        String style = getProperties().getFont();
        double size = getProperties().getTextSize();
        setZticsFont(style,size);
        setCbticsFont(style,size);
        String zticsStep = getProperties().getZticsStep();
        if (StringUtils.isNotEmpty(zticsStep)) {
            appendCommand("set ztics " + zticsStep);
            appendCommand("set cbtics " + zticsStep);
        }
    }

    public void setXticsFont(String style, double size){
        appendCommand("set xtics font" + StringUtils.toDoubleQuotes(style +"," +size));
    }
    public void setXticsFontStyle(String style) {
        appendCommand("set xtics font" + StringUtils.toDoubleQuotes(style +","));
    }
    public void setXticsFontSize(double size) {
        appendCommand("set xtics font" + StringUtils.toDoubleQuotes("," +size));
    }
    public void setZticsFont(String style, double size){
        appendCommand("set ztics font" + StringUtils.toDoubleQuotes(style +"," +size));
    }
    public void setCbticsFont(String style, double size){
        appendCommand("set cbtics font" + StringUtils.toDoubleQuotes(style +"," +size));
    }
    public void setZticsFontStyle(String style) {
        appendCommand("set ztics font" + StringUtils.toDoubleQuotes(style +","));
    }
    public void setZticsFontSize(double size) {
        appendCommand("set ztics font" + StringUtils.toDoubleQuotes("," +size));
    }
    public void setYticsFont(String style, double size){
        appendCommand("set ytics font" + StringUtils.toDoubleQuotes(style +"," +size));
    }
    public void setYticsFontStyle(String style) {
        appendCommand("set ytics font" + StringUtils.toDoubleQuotes(style +","));
    }
    public void setYticsFontSize(double size) {
        appendCommand("set ytics font" + StringUtils.toDoubleQuotes("," +size));
    }
}
