package org.nanomodeller.XMLMappingFiles;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement(name="Row")
public class Row {
    private int i;
    private ArrayList<RowElement> elements = new ArrayList<>();

    @XmlAttribute(name="i")
    public int getI() {return i;}
    public void setI(int i) {this.i = i;}

    @XmlElements(@XmlElement(name="Element"))
    public ArrayList<RowElement> getElements() {return elements;}
    public void setElements(ArrayList<RowElement> elements){
        this.elements = elements;
    }
    public Row(){}

    public Row (int i, ArrayList<RowElement> elements ){
        this.elements = elements;
        this.i = i;
    }
}
