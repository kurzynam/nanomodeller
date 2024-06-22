package org.nanomodeller.XMLMappingFiles;

import org.nanomodeller.Globals;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

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
                JAXBContext jaxbContext = JAXBContext.newInstance(Parameters.class);
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

    public static void convertObjectToXMLFile(Object o, String path) {

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

    public static String convertObjectToXMLString(Object o) {

        try {
            StringWriter writer = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(o.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(o, writer);
            return writer.toString();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static XMLTemplate convertXMLStringToElement(String xml, Class elementType){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(elementType);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xml);
            if (elementType.equals(Atom.class)) {
                return (Atom) unmarshaller.unmarshal(reader);
            } else if (elementType.equals(Bond.class)) {
                return (Bond) unmarshaller.unmarshal(reader);
            } else if (elementType.equals(Electrode.class)) {
                return (Electrode) unmarshaller.unmarshal(reader);
            } else if (elementType.equals(Surface.class)) {
                return (Surface) unmarshaller.unmarshal(reader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void convertObjectToXMLFile(Object o){
        convertObjectToXMLFile(o, Globals.XML_FILE_PATH);
    }

}