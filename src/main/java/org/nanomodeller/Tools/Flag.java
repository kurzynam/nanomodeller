package org.nanomodeller.Tools;

public class Flag {
    private boolean value;

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public Flag(){
        value = true;
    }
    public Flag(boolean value){
        this.value = value;
    }
    public void neg(){
        this.value = !value;
    }

}
