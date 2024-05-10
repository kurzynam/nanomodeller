package org.nanomodeller.XMLMappingFiles;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;

@XmlRootElement(name="root")
public class GlobalChainProperties {

    private ArrayList<Parameters> parameters = new ArrayList<Parameters>();
    private HashMap<String, String> paletteColors = new HashMap<>();
    private HashMap<String, Double> userDefinedVariables = new HashMap<>();
    public void addParameters(Parameters p){
        parameters.add(p);
    }
    private int everyT;
    private int everyE;
    private int timeDependentWriteEveryE;
    private int timeDependentWriteEveryT;
    private int horizontalView;
    private int verticalView;
    private int multiplotRows;
    private int multiplotCols;
    private double offsetStep;
    private double margin;
    private double textSize;
    private double xTicsOffset;
    private double yTicsOffset;
    private double dE;
    private double dt;
    private String saveTimeFrom;
    private String xticsStep;
    private String yticsStep;
    private String zticsStep;
    private String xRange;
    private String yRange;
    private String zRange;
    private String Emin;
    private String Emax;
    private String energyRange;
    private String staticPATH;
    private String dynamicPATH;
    private String color;
    private String graphLabelColor;
    private String font;
    private String multiplotStyle;
    private String customGnuplotCommands;
    private String crossSectionEnergy;
    private boolean showGrid;


    public String getSaveTimeFrom() {
        return saveTimeFrom;
    }

    public void setSaveTimeFrom(String saveTimeFrom) {
        this.saveTimeFrom = saveTimeFrom;
    }

    public String getXticsStep() {
        return xticsStep;
    }

    public void setXticsStep(String xticsStep) {
        this.xticsStep = xticsStep;
    }

    public String getYticsStep() {
        return yticsStep;
    }

    public void setYticsStep(String yticsStep) {
        this.yticsStep = yticsStep;
    }

    public String getZticsStep() {
        return zticsStep;
    }

    public void setZticsStep(String zticsStep) {
        this.zticsStep = zticsStep;
    }

    public String getCrossSectionEnergy() {
        return crossSectionEnergy;
    }

    public void setCrossSectionEnergy(String crossSectionEnergy) {
        this.crossSectionEnergy = crossSectionEnergy;
    }

    public String getCustomGnuplotCommands() {
        return customGnuplotCommands;
    }

    public void setCustomGnuplotCommands(String customGnuplotCommands) {
        this.customGnuplotCommands = customGnuplotCommands;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getMultiplotStyle() {
        return multiplotStyle;
    }

    public void setMultiplotStyle(String multiplotStyle) {
        this.multiplotStyle = multiplotStyle;
    }

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    public double getTextSize() {
        return textSize;
    }

    public void setTextSize(double textSize) {
        this.textSize = textSize;
    }

    public double getxTicsOffset() {
        return xTicsOffset;
    }

    public void setxTicsOffset(double xTicsOffset) {
        this.xTicsOffset = xTicsOffset;
    }

    public double getyTicsOffset() {
        return yTicsOffset;
    }

    public void setyTicsOffset(double yTicsOffset) {
        this.yTicsOffset = yTicsOffset;
    }

    public String getGraphLabelColor() {
        return graphLabelColor;
    }

    public void setGraphLabelColor(String graphLabelColor) {
        this.graphLabelColor = graphLabelColor;
    }

    public String getRowsFirst() {
        return rowsFirst;
    }

    public void setRowsFirst(String rowsFirst) {
        this.rowsFirst = rowsFirst;
    }

    private String rowsFirst;

    public double getOffsetStep() {
        return offsetStep;
    }

    public void setOffsetStep(double offset) {
        this.offsetStep = offset;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStaticPATH() {
        return staticPATH;
    }

    public void setStaticPATH(String staticPATH) {
        this.staticPATH = staticPATH;
    }

    public String getDynamicPATH() {
        return dynamicPATH;
    }

    public void setDynamicPATH(String dynamicPATH) {
        this.dynamicPATH = dynamicPATH;
    }

    public String getEmax() {
        return Emax;
    }

    public void setEmax(String emax) {
        Emax = emax;
    }

    public String getEmin() {
        return Emin;
    }

    public void setEmin(String emin) {
        Emin = emin;
    }

    public String getEnergyRange() {
        return energyRange;
    }
    public void setEnergyRange(String energyRange) {
        this.energyRange = energyRange;
        Emin = this.energyRange.trim().split(":")[0];
        Emax = this.energyRange.trim().split(":")[1];
    }

    @XmlElements(@XmlElement(name="Parameters"))
    public ArrayList<Parameters> getParameters() {
        return parameters;
    }
    public void setParameters(ArrayList<Parameters> p){
        parameters = p;
    }
    public void deleteParameter ( Parameters p){
        if (parameters.contains(p)){
            parameters.remove(p);
        }
    }
    public Parameters getParamByName(String name){
        for (Parameters p : parameters){
            if (name.equals(p.getName())){
                return p;
            }
        }
        return null;
    }
    public Parameters getParamByID(String id){
        for (Parameters p : parameters){
            if (id.equals(p.getId())){
                return p;
            }
        }
        return null;
    }

    public String getxRange() {
        return xRange;
    }

    public void setxRange(String xRange) {
        this.xRange = xRange;
    }

    public String getyRange() {
        return yRange;
    }

    public void setyRange(String yRange) {
        this.yRange = yRange;
    }

    public String getzRange() {
        return zRange;
    }

    public void setzRange(String zRange) {
        this.zRange = zRange;
    }

    public int getNumberOfEnergySteps(){
        if(dE == 0){
            return (int)(getElectrodesWidth()/0.01);
        }
        return (int)(getElectrodesWidth()/getdE());
    }

    public double getDoubleEmax(){
        return Double.parseDouble(getEmax());
    }

    public double getDoubleEmin(){
        return Double.parseDouble(getEmin());
    }

    public double getElectrodesWidth() {
        return Double.parseDouble(Emax) - Double.parseDouble(Emin);
    }

    public int getEveryT() {
        return everyT;
    }

    public void setEveryT(int everyT) {
        this.everyT = everyT;
    }

    public int getEveryE() {
        return everyE;
    }

    public void setEveryE(int everyE) {
        this.everyE = everyE;
    }

    public int getTimeDependentWriteEveryE() {
        return timeDependentWriteEveryE;
    }

    public void setTimeDependentWriteEveryE(int timeDependentWriteEveryE) {
        this.timeDependentWriteEveryE = timeDependentWriteEveryE;
    }

    public int getTimeDependentWriteEveryT() {
        return timeDependentWriteEveryT;
    }

    public void setTimeDependentWriteEveryT(int timeDependentWriteEveryT) {
        this.timeDependentWriteEveryT = timeDependentWriteEveryT;
    }

    public int getHorizontalView() {
        return horizontalView;
    }

    public void setHorizontalView(int horizontalView) {
        this.horizontalView = horizontalView;
    }

    public int getVerticalView() {
        return verticalView;
    }

    public void setVerticalView(int verticalView) {
        this.verticalView = verticalView;
    }

    public int getMultiplotRows() {
        return multiplotRows;
    }

    public void setMultiplotRows(int multiplotRows) {
        this.multiplotRows = multiplotRows;
    }

    public int getMultiplotCols() {
        return multiplotCols;
    }

    public void setMultiplotCols(int multiplotCols) {
        this.multiplotCols = multiplotCols;
    }

    public double getdE() {
        return dE == 0 ? 0.01 :dE;
    }

    public void setdE(double dE) {
        this.dE = dE;
    }

    public double getDt() {
        return dt;
    }

    public void setDt(double dt) {
        this.dt = dt;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public HashMap<String, String> getPaletteColors() {
        return paletteColors;
    }

    public void setPaletteColors(HashMap<String, String> paletteColors) {
        this.paletteColors = paletteColors;
    }

    public HashMap<String, Double> getUserDefinedVariables() {
        return userDefinedVariables;
    }

    public void setUserDefinedVariables(HashMap<String, Double> userDefinedVariables) {
        this.userDefinedVariables = userDefinedVariables;
    }
}
