package org.nanomodeller.XMLMappingFiles;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="PlotOptions")
public class PlotOptions extends XMLTemplate{
    public static PlotOptions getInstance(){
        return GlobalProperties.getInstance().getPlotOptions();
    }
    private String customCommand;
    private boolean is3D;
    private String xaxis = "", yaxis = "", xlabel = "", ylabel = "",
            xrange = "", yrange = "", title = "", style = "";
    @XmlElement(name="is3D")
    public boolean isIs3D() {
        return is3D;
    }
    public void setIs3D(boolean is3D) {
        this.is3D = is3D;
    }
    @XmlElement(name="customCommand")
    public String getCustomCommand() {
        return customCommand;
    }
    public void setCustomCommand(String customCommand) {
        this.customCommand = customCommand;
    }
    @XmlElement(name="xaxis")
    public String getXaxis() {
        return xaxis;
    }
    public void setXaxis(String xaxis) {
        this.xaxis = xaxis;
    }
    @XmlElement(name="yaxis")
    public String getYaxis() {
        return yaxis;
    }
    public void setYaxis(String yaxis) {
        this.yaxis = yaxis;
    }
    @XmlElement(name="xlabel")
    public String getXlabel() {
        return xlabel;
    }
    public void setXlabel(String xlabel) {
        this.xlabel = xlabel;
    }
    @XmlElement(name="ylabel")
    public String getYlabel() {
        return ylabel;
    }
    public void setYlabel(String ylabel) {
        this.ylabel = ylabel;
    }
    @XmlElement(name="xrange")
    public String getXrange() {
        return xrange;
    }
    public void setXrange(String xrange) {
        this.xrange = xrange;
    }
    @XmlElement(name="yrange")
    public String getYrange() {
        return yrange;
    }
    public void setYrange(String yrange) {
        this.yrange = yrange;
    }
    @XmlElement(name="title")
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    @XmlElement(name="style")
    public String getStyle() {
        return style;
    }
    public void setStyle(String style) {
        this.style = style;
    }

    public double getDouble(String key) {
        return Double.parseDouble(getString(key));
    }
}
