package org.nanomodeller.XMLMappingFiles;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Element")
public class RowElement{
    private int j;
    private String realValue;
    private String imaginaryValue;

    @XmlAttribute(name="j")
    public int getJ() {return j;}
    public void setJ(int j) {this.j = j;}

    @XmlAttribute(name="real_val")
    public String getRealValue() {return realValue;}
    public void setRealValue(String value) {this.realValue = value;}

    @XmlAttribute(name="im_val")
    public String getImaginaryValue() {return imaginaryValue;}
    public void setImaginaryValue(String value) {this.imaginaryValue = value;}

    public RowElement(){}
    public RowElement(int j, String real_value){this.j = j; this.realValue = real_value;}
}