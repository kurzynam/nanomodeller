package org.nanomodeller.XMLMappingFiles;


import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Iterator;
import java.util.NoSuchElementException;

@XmlRootElement(name="Range")
public class Range implements Iterable<Double>{
    private Double min, max, increment;

    public Range(){};

    public Range(Double min, Double max, Double increment) {
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    @XmlElement(name="min")
    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }
    @XmlElement(name="max")
    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }
    @XmlElement(name="incr")
    public Double getIncrement() {
        return increment;
    }

    public Double getWidth(){
        return max - min;
    }

    public Integer getStepsNum(){
        return (int)(getWidth()/getIncrement());
    }

    public void setIncrement(Double increment) {
        this.increment = increment;
    }

    @Override
    public Iterator<Double> iterator() {
        return new Iterator<Double>() {
            private Double current = min;

            @Override
            public boolean hasNext() {
                return current <= max;
            }

            @Override
            public Double next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Double nextValue = current;
                current += increment;
                return nextValue;
            }
        };
    }
}
