package org.nanomodeller.XMLMappingFiles;

import org.nanomodeller.Globals;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Bound")
public class Bound {
    private int first;
    private int second;
    private String coupling;
    private String spinOrbit;
    private String type;
    private String color;
    private String correlationCoupling;
    private double parsedCoupling;
    private double parsedCorrelationCoupling;
    private String perturbation = "1";
    private double parsedPerturbation;

    public Bound(int first,int second, Bound bound){
        this.first = first;
        this.second = second;
        coupling = bound.coupling;
        spinOrbit = bound.spinOrbit;
        type = bound.type;
        color = bound.color;
        correlationCoupling = bound.correlationCoupling;
        parsedCoupling = bound.parsedCoupling;
        parsedCorrelationCoupling = bound.parsedCorrelationCoupling;
    }

    public Bound(int first,int second){
        this.first = first;
        this.second = second;
        coupling = "2";
        spinOrbit = "0";
        correlationCoupling = "0";
        color = Globals.BLACK;
    }

    public double getParsedCorrelationCoupling() {
        return parsedCorrelationCoupling;
    }

    public void setParsedCorrelationCoupling(double parsedCorrelationCoupling) {
        this.parsedCorrelationCoupling = parsedCorrelationCoupling;
    }

    public String getCorrelationCoupling() {
        return correlationCoupling;
    }

    public void setCorrelationCoupling(String correlationCoupling) {
        this.correlationCoupling = correlationCoupling;
    }

    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public double getParsedCoupling() {
        return parsedCoupling;
    }
    public void setParsedCoupling(double parsedCoupling) {
        this.parsedCoupling = parsedCoupling;
    }

    @XmlAttribute(name="first_id")
    public int getFirst() {
        return first;
    }
    public void setFirst(int first) {
        this.first = first;
    }

    @XmlAttribute(name="second_id")
    public int getSecond() {
        return second;
    }
    public void setSecond(int second) {
        this.second = second;
    }

    //@XmlAttribute(name="V")
    public String getCoupling() {
        return coupling;
    }
    public double getDoubleCoupling(){

        return Double.parseDouble(coupling);

    }
    public void setCoupling(String coupling) {
        this.coupling = coupling;
    }

    public double getParsedPerturbation() {
        return parsedPerturbation;
    }

    public void setParsedPerturbation(double parsedPerturbation) {
        this.parsedPerturbation = parsedPerturbation;
    }

    public String getPerturbation() {
        return perturbation;
    }

    public void setPerturbation(String perturbation) {
        this.perturbation = perturbation;
    }


    //@XmlAttribute(name="spin_orbit")
    public String getSpinOrbit() {return spinOrbit;}
    public void setSpinOrbit(String spinOrbit) {this.spinOrbit = spinOrbit;}

    public Bound(int first, int second, String coupling, String spinOrbit, String type) {
        this.first = first;
        this.second = second;
        this.coupling = coupling;
        this.spinOrbit = spinOrbit;
        this.type = type;
    }
    public Bound(){

    }
    public int getAnotherAtomID(int atom_id) {
        int second;
        if (atom_id == getFirst()) {
            second = getSecond();
        } else {
            second = getFirst();
        }
        return second;
    }
}
