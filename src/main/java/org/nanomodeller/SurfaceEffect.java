package org.nanomodeller;

import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.Parameters;

public class SurfaceEffect {

    public static double surfaceCoupling(Parameters par, Atom first, Atom second){
        double x1 = first.getX();
        double y1 = first.getY();
        double x2 = second.getX();
        double y2 = second.getY();
        double distance = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
        double result = 0;
        if (par.isSurfacePresent() ){
            result = (par.getSurface().getDouble("Coupling") *
                    Math.sin(par.getSurface().getDouble("kFa") * distance))/
                    (par.getSurface().getDouble("kFa") * distance);

            if (first.getID() == second.getID()) {
                if(Double.isNaN(result) || result != 0){
                    return par.getSurface().getDouble("Coupling");
                }
            }
        }
        if (Double.isNaN(result)){
            result = 0;
        }
        return result;
    }

}
