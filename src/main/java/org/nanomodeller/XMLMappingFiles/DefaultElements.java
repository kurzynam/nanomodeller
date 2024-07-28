package org.nanomodeller.XMLMappingFiles;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;


@XmlRootElement(name="DefaultElements")
public class DefaultElements  extends XMLTemplate implements Cloneable{
    private ArrayList<StructureElement> bonds = new ArrayList<>();
    private ArrayList<StructureElement> atoms = new ArrayList<>();
    private ArrayList<StructureElement> electrodes = new ArrayList<>();



    private static DefaultElements instance;

    @XmlElements(@XmlElement(name="Electrode"))
    public ArrayList<StructureElement> getElectrodes() {
        return electrodes;
    }

    @XmlElements(@XmlElement(name="Bond"))
    public ArrayList<StructureElement> getBonds() {
        return bonds;
    }

    @XmlElements(@XmlElement(name="Atom"))
    public ArrayList<StructureElement> getAtoms() {
        return atoms;
    }

    @Override
    public DefaultElements clone() {
        try {
            DefaultElements clone = (DefaultElements) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public static DefaultElements getInstance(){
        return GlobalProperties.getInstance().getDefaultElements();
    }
}
