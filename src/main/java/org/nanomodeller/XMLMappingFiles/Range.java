package org.nanomodeller.XMLMappingFiles;


import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Iterator;
import java.util.NoSuchElementException;

@XmlRootElement(name="Range")
public class Range implements Iterable<Float>{
    private float min, max, increment, saveEvery;

    public Range(){};

    public Range(float min, float max, float increment) {
        this.min = min;
        this.max = max;
        this.increment = increment;
    }
    @XmlElement(name="saveEvery")
    public float getSaveEvery() {
        return saveEvery;
    }

    @XmlElement(name="min")
    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }
    @XmlElement(name="max")
    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }
    @XmlElement(name="incr")
    public float getIncrement() {
        return increment;
    }

    public float getWidth(){
        return max - min;
    }

    public Integer getStepsNum(){
        return (int)(getWidth()/getIncrement());
    }

    public void setIncrement(float increment) {
        this.increment = increment;
    }

    @Override
    public Iterator<Float> iterator() {
        return new Iterator<>() {
            private float current = min;
            @Override
            public boolean hasNext() {
                return current <= max;
            }
            @Override
            public Float next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                float nextValue = current;
                current += increment;
                return nextValue;
            }
        };
    }
}
