package org.nanomodeller.XMLMappingFiles;

import jakarta.xml.bind.annotation.XmlAttribute;
import org.nanomodeller.Tools.StringUtils;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.stream.Collectors;

@XmlRootElement(name="Parameters")
public class Parameters {

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

    private String id;
    private String time;
    private String kFa;
    private String Number;
    private int GridSize;
    private String name;
    private String electrodesEmin;
    private String electrodesEmax;
    private String surfaceCoupling;
    private String path;
    private ArrayList<Bond> bonds = new ArrayList<Bond>();
    private ArrayList<Atom> atoms = new ArrayList<Atom>();
    private ArrayList<Electrode> electrodes = new ArrayList<Electrode>();

    public void addBound(Bond bond) {
        int count = -1;
        for (Bond other : bonds){
            count++;
            if (bond.getFirst() > other.getFirst()){}
            else if (bond.getFirst() == other.getFirst()){
                if (bond.getSecond() <= other.getSecond()){
                    this.bonds.add(count, bond);
                    return;
                }
            }
            else {
                this.bonds.add(count, bond);
                return;
            }
        }
        bonds.add(bond);

    }
    public void addAtom(Atom atom) {
        this.atoms.add(atom);
    }
    public void addElectrode(Electrode electrode){
        this.electrodes.add(electrode);
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

    @XmlAttribute(name="id")
    public String getId() {
        return id;
    }
    public void setId(String id) { this.id = id; }

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
    @XmlAttribute(name="time")
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    @XmlAttribute(name="gammaSur")
    public String getSurfaceCoupling() {
        return surfaceCoupling;
    }
    public void setSurfaceCoupling(String surfaceCoupling) {
        this.surfaceCoupling = surfaceCoupling;
    }

    @XmlAttribute(name="kFa")
    public String getkFa() {
        return kFa;
    }
    public void setkFa(String kFa) {
        this.kFa = kFa;
    }

    @XmlAttribute(name="number")
    public String getNumber() {
        return Number;
    }
    public void setNumber(String number) {
        Number = number;
    }

    @XmlAttribute(name="grid_size")
    public int getGridSize() {
        return GridSize;
    }
    public void setGridSize(int gridSize) {
        GridSize = gridSize;
    }

    @XmlAttribute(name="name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name="electrode_emin")
    public String getElectrodesEmin() {
        return electrodesEmin;
    }
    public void setElectrodesEmin(String electrodesEmin) {
        this.electrodesEmin = electrodesEmin;
    }

    @XmlAttribute(name="electrode_emax")
    public String getElectrodesEmax() {
        return electrodesEmax;
    }
    public void setElectrodesEmax(String electrodesEmax) {
        this.electrodesEmax = electrodesEmax;
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

    public Bond getBond(Atom atom1, Atom atom2){
        int a1 = atom1.getID();
        int a2 = atom2.getID();
        for(Bond b : bonds){
            if(b.getFirst() == a1 && b.getSecond() == a2 || b.getFirst() == a2 && b.getSecond() == a1){
                return b;
            }
        }
        return null;
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
        return StringUtils.isNotEmpty(surfaceCoupling) && getDoubleSurfaceCoupling() > 0
                && StringUtils.isNotEmpty(kFa) && getDoubleKFa() > 0;
    }
    public int getIntNum(){
        return Integer.parseInt(getNumber());
    }
    public double getDoubleTime(){
        return Double.parseDouble(getTime());
    }
    public double getDoubleKFa(){
        return Double.parseDouble(getkFa());
    }
    public double getDoubleSurfaceCoupling(){
        return Double.parseDouble(getSurfaceCoupling());
    }

    public ArrayList<Bond> getBoundsByAtomID(int id) {
        ArrayList<Bond> bonds = new ArrayList<>();
        for(Bond b : bonds){
            if(b.getFirst() == id || b.getSecond() == id){
                bonds.add(b);
            }
        }
        return bonds;
    }

    public ArrayList<Bond> getBondsOfAtom(int i){
        return (ArrayList<Bond>) getBonds().stream().filter(b -> b.getFirst() == i || b.getSecond() == i).collect(Collectors.toList());
    }
}
