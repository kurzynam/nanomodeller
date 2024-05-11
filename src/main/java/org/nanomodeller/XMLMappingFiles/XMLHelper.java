package org.nanomodeller.XMLMappingFiles;

import org.nanomodeller.Globals;

import java.io.File;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;


public class XMLHelper {


    public static GlobalProperties readGlobalPropertiesFromXMLFile(String path){
        File file = new File(path);
        GlobalProperties gp = null;// GlobalProperties.getInstance();
        try {
            JAXBContext jaxbContext =

                    JAXBContext.newInstance(GlobalProperties.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            gp = (GlobalProperties) jaxbUnmarshaller.unmarshal(file);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return gp;
    }

    public static void readPropertiesFromXMLFile(String path){
        File file = new File(path);
        Parameters properties = null;
        if (file.exists()) {
            try {
                JAXBContext jaxbContext =

                        JAXBContext.newInstance(Parameters.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                properties = (Parameters) jaxbUnmarshaller.unmarshal(file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Parameters.reloadInstance(properties);
        } else {
            Parameters.reloadInstance(null);
        }
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

    public static void convertObjectToXML(Object o, String path) {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(o.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(o, new File(path));
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public static void convertObjectToXML(Object o){
        convertObjectToXML(o, Globals.XML_FILE_PATH);
    }

}