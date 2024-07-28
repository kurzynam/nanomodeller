package org.nanomodeller.XMLMappingFiles;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import static org.nanomodeller.Globals.XML_FILE_PATH;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.readGlobalPropertiesFromXMLFile;


@XmlRootElement(name="GlobalProperties")
public class GlobalProperties{

    private static GlobalProperties instance;
    private CommonProperties commonProperties;

    public static GlobalProperties getInstance(){
        if (instance == null){
            instance = readGlobalPropertiesFromXMLFile(XML_FILE_PATH);
        }
        return instance;
    }
    private String dynamicPATH;

    private PlotOptions plotOptions;

    private DefaultElements defaultElements;

    public static void setInstance(GlobalProperties instance) {
        GlobalProperties.instance = instance;
    }

    public DefaultElements getDefaultElements() {
        return defaultElements;
    }

    public void setDefaultElements(DefaultElements defaultElements) {
        this.defaultElements = defaultElements;
    }

    public String getDynamicPATH() {
        return dynamicPATH;
    }

    public void setDynamicPATH(String dynamicPATH) {
        this.dynamicPATH = dynamicPATH;
    }


    @XmlElement(name="PlotOptions")
    public PlotOptions getPlotOptions() {
        return plotOptions;
    }
    @XmlElement(name="CommonProperties")
    public CommonProperties getCommonProperties() {
        return commonProperties;
    }

    public void setCommonProperties(CommonProperties commonProperties) {
        this.commonProperties = commonProperties;
    }

    public void setPlotOptions(PlotOptions plotOptions) {
        this.plotOptions = plotOptions;
    }

}
