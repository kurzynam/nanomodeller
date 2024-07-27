package org.nanomodeller.XMLMappingFiles;

import jakarta.xml.bind.annotation.XmlAttribute;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Optional;

@XmlRootElement(name="Parameters")
public class Parameters implements Cloneable{

    private static Parameters instance;

    public static Parameters getInstance(){
        if (instance == null){
            instance = new Parameters();
        }
        return instance;
    }

    public static void reloadInstance(Parameters par){
        instance = par;
    }
    private int GridSize;
    private String path;
    private ArrayList<Bond> bonds = new ArrayList<Bond>();
    private ArrayList<Atom> atoms = new ArrayList<Atom>();
    private ArrayList<Electrode> electrodes = new ArrayList<Electrode>();

    private Surface surface;

    public void addAtom(Atom atom) {
        this.atoms.add(atom);
    }
    public void addElectrode(Electrode electrode){
        this.electrodes.add(electrode);
    }


    @Override
    public Parameters clone(){
        try {
            return (Parameters) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public ArrayList<Electrode> getElectrodesByAtomID(int id){
        ArrayList<Electrode> el = new ArrayList<Electrode>();
        for (Electrode e : electrodes){
            if(id == e.getAtomIndex()){
                el.add(e);
            }
        }
        return el;
    }
    @XmlElements(@XmlElement(name="Electrode"))
    public ArrayList<Electrode> getElectrodes() {
        return electrodes;
    }

    @XmlElements(@XmlElement(name="Bond"))
    public ArrayList<Bond> getBonds() {
        return bonds;
    }

    @XmlElements(@XmlElement(name="Atom"))
    public ArrayList<Atom> getAtoms() {
        return atoms;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    @XmlElement(name="Surface")
    public Surface getSurface() {
        if (surface == null){
            surface = new Surface();
        }
        return surface;
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    @XmlAttribute(name="grid_size")
    public int getGridSize() {
        return GridSize;
    }
    public void setGridSize(int gridSize) {
        GridSize = gridSize;
    }

    public Optional<Electrode> getElectrodeByAtomIndex(int id){
        return electrodes.stream().filter(electrode -> electrode.getAtomIndex() == id).findFirst();
    }

    public boolean areBond(int a1, int a2){
        for(Bond b : bonds){
            if(b.getFirst() == a1 && b.getSecond() == a2 || b.getFirst()== a2 && b.getSecond() == a1){
                return true;
            }
        }
        return false;
    }
    public boolean areBond(Atom atom1, Atom atom2){
        int a1 = atom1.getID();
        int a2 = atom2.getID();
        for(Bond b : bonds){
            if(b.getFirst() == a1 && b.getSecond() == a2 || b.getFirst() == a2 && b.getSecond() == a1){
                return true;
            }
        }
        return false;
    }
    public Bond getBond(int a1, int a2){
        int first = a1 < a2 ? a1 : a2;
        int second = a2 < a1 ? a1 : a2;
        for(Bond b : bonds){
            if(b.getFirst() == first && b.getSecond() == second){
                return b;
            }
        }
        return null;
    }

    public boolean isSurfacePresent(){
        return surface != null && surface.properties != null && !surface.properties.isEmpty();
    }

}
