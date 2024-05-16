package org.nanomodeller.Tools;

import org.nanomodeller.Globals;

import java.util.HashMap;

public class PropertiesMap extends HashMap {
    public String getString(String p) {
        if (!containsKey(p))
            return "0";
        else
            return (String) get(p);
    }

    @Override
    public boolean containsKey(Object key) {
        return this.keySet().stream().anyMatch(k ->
                StringUtils.equals(k.toString().toUpperCase(), key.toString().toUpperCase()));
    }

    public Double getDouble(String p) {
        if (!containsKey(p)) {
            return Double.valueOf(0.0);
        } else
            return Double.parseDouble((String) get(p));
    }

    public Integer getInt(String p) {
        if (!containsKey(p)) {
            return Integer.valueOf(0);
        } else
            return Integer.parseInt((String) get(p));
    }
    public Boolean getBool(String p) {
        if (!containsKey(p)) {
            return Boolean.valueOf(false);
        } else
            return Globals.isTrue(getString(p));
    }



}
