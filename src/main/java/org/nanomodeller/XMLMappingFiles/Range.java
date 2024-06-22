package org.nanomodeller.XMLMappingFiles;


import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Range")
public class Range {
    private String min, max, increment;
    @XmlElement(name="min")
    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }
    @XmlElement(name="max")
    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }
    @XmlElement(name="incr")
    public String getIncrement() {
        return increment;
    }

    public void setIncrement(String increment) {
        this.increment = increment;
    }
}
