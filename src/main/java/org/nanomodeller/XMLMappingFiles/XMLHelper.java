package org.nanomodeller.XMLMappingFiles;

import org.nanomodeller.Globals;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


public class XMLHelper {


    public static GlobalChainProperties readParametersFromXMLFile(String path){
        File file = new File(path);
        GlobalChainProperties gp = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(GlobalChainProperties.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            gp = (GlobalChainProperties) jaxbUnmarshaller.unmarshal(file);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return gp;
    }

    public static Matrix readMatrixFromXMLFile(String path){
        File file = new File(path);
        Matrix matrix = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Matrix.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            matrix = (Matrix) jaxbUnmarshaller.unmarshal(file);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return matrix;
    }

    public static void convertObjectToXML(Object o) {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(o.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(o, new File(Globals.XML_FILE_PATH));
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}