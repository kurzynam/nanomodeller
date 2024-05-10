package org.nanomodeller;

import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.Parameters;

public class SurfaceEffect {

    public static double surfaceCoupling(Parameters par, Atom first, Atom second){
        double x1 = Double.parseDouble(first.getX());
        double y1 = Double.parseDouble(first.getY());
        double x2 = Double.parseDouble(second.getX());
        double y2 = Double.parseDouble(second.getY());
        double distance = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
        double result = 0;
        if (par.isSurfacePresent() ){
            result = (par.getDoubleSurfaceCoupling() *
                    Math.sin(par.getDoubleKFa() * distance))/
                    (par.getDoubleKFa() * distance);
            if (Double.isNaN(result)){
                return par.getDoubleSurfaceCoupling();
            }
        }
        return result;
    }

}
