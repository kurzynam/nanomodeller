package org.nanomodeller.XMLMappingFiles;

import org.nanomodeller.Tools.StringUtils;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlRootElement(name="Parameters")
public class Parameters {

    private String id;
    private String time;
    private String kFa;
    private String Number;
    private String GridSize;
    private String name;
    private String electrodesEmin;
    private String electrodesEmax;
    private boolean isActive;
    private String surfaceCoupling;
    private String path;
    private int numOfSubSteps = 1;
    private ArrayList<Bound> bounds = new ArrayList<Bound>();
    private ArrayList<Atom> atoms = new ArrayList<Atom>();
    private ArrayList<Electrode> electrodes = new ArrayList<Electrode>();

    public void addBound(Bound bound) {
        this.bounds.add(bound);
    }
    public void addAtom(Atom atom) {
        this.atoms.add(atom);
    }
    public void addElectode(Electrode electrode){
        this.electrodes.add(electrode);
    }

    public Electrode getElectrodyByID(int id){
        for (Electrode e : electrodes){
            if(id == e.getId()){
                return e;
            }
        }
        return null;
    }
    public double getElectrodeCouplingsByAtomID(int id){
        for (Electrode e : electrodes){
            if(id == e.getAtomIndex()){
                return  e.getParsedCoupling();
            }
        }
        return 0;
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

    public int getNumOfSubSteps() {
        return numOfSubSteps;
    }

    public void setNumOfSubSteps(int numOfSubSteps) {
        this.numOfSubSteps = numOfSubSteps;
    }

    @XmlElements(@XmlElement(name="Electrode"))
    public ArrayList<Electrode> getElectrodes() {
        return electrodes;
    }

    @XmlElements(@XmlElement(name="Bound"))
    public ArrayList<Bound> getBounds() {
        return bounds;
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

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
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

    @XmlAttribute(name="isActive")
    public boolean getActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        this.isActive = active;
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
    public String getGridSize() {
        return GridSize == null ? "10" : GridSize;
    }
    public void setGridSize(String gridSize) {
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

    public boolean areBound(int a1, int a2){
        for(Bound b : bounds){
            if(b.getFirst() == a1 && b.getSecond() == a2 || b.getFirst()== a2 && b.getSecond() == a1){
                return true;
            }
        }
        return false;
    }
    public boolean areBound(Atom atom1, Atom atom2){
        int a1 = atom1.getID();
        int a2 = atom2.getID();
        for(Bound b : bounds){
            if(b.getFirst() == a1 && b.getSecond() == a2 || b.getFirst() == a2 && b.getSecond() == a1){
                return true;
            }
        }
        return false;
    }

    public Bound getBound(Atom atom1, Atom atom2){
        int a1 = atom1.getID();
        int a2 = atom2.getID();
        for(Bound b : bounds){
            if(b.getFirst() == a1 && b.getSecond() == a2 || b.getFirst() == a2 && b.getSecond() == a1){
                return b;
            }
        }
        return null;
    }

    public Bound getBound(int a1, int a2){
        for(Bound b : bounds){
            if(b.getFirst() == a1 && b.getSecond() == a2 || b.getFirst() == a2 && b.getSecond() == a1){
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

    public ArrayList<Bound> getBoundsByAtomID(int id) {
        ArrayList<Bound> bounds = new ArrayList<>();
        for(Bound b : bounds){
            if(b.getFirst() == id || b.getSecond() == id){
                bounds.add(b);
            }
        }
        return bounds;
    }
}
