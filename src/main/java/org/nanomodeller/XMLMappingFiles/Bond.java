package org.nanomodeller.XMLMappingFiles;

import org.nanomodeller.Calculation.CalculationBond;
import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Globals;
import org.nfunk.jep.JEP;

import javax.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Hashtable;

@XmlRootElement(name="Bond")
public class Bond extends Element{
    private int first;
    private int second;

    public Bond(int first, int second,/* int centerX, int centerY,*/ Bond bond){
        if (first > second){
            this.second = first;
            this.first = second;
        }else{
            this.first = first;
            this.second = second;
        }
//        this.setX(centerX);
//        this.setY(centerY);
        this.properties = bond.properties;
        this.color = bond.getColor();
        this.groupID = bond.groupID;
    }


    public Boolean intersects (int x, int y){
        Atom first = NanoModeler.getInstance().getAtoms().get(getFirst());
        Atom second = NanoModeler.getInstance().getAtoms().get(getSecond());
        return new Line2D.Double(first.getX(), first.getY(), second.getX(), second.getY()).intersects(x, y, 60, 60);

    }

    public Bond(int first, int second/*, int centerX, int centerY*/){
        if (first > second){
            this.second = first;
            this.first = second;
        }else{
            this.first = first;
            this.second = second;
        }
//        setY(centerY);
//        setX(centerX);
    }
    @XmlAttribute(name="first")
    public int getFirst(){
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }
    @XmlAttribute(name="second")
    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }
    public Bond(){

    }

    public static void initializeCalculationBonds(JEP parser, ArrayList<Bond> bonds, Hashtable<Integer, Hashtable<Integer,CalculationBond>> cBonds) {
        bonds.stream().forEach(
                bond -> {
                    CalculationBond cb = new CalculationBond(bond.first, bond.second);
                    fillProperties(parser, bond, cb);
                    Hashtable<Integer, CalculationBond> bondsOfFirstAtom = cBonds.get(bond.first);
                    if (bondsOfFirstAtom == null) {
                        bondsOfFirstAtom = new Hashtable<>();
                        cBonds.put(bond.first, bondsOfFirstAtom);
                    }
                    bondsOfFirstAtom.put(bond.second, cb);
                }
         );
    }


}
