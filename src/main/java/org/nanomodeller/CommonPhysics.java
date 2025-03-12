package org.nanomodeller;

import org.jscience.mathematics.number.Complex;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.XMLMappingFiles.CommonProperties;
import org.nanomodeller.XMLMappingFiles.Range;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static org.nanomodeller.CommonMath.comp;
import static org.nanomodeller.CommonPhysics.DensType.*;
import static org.nanomodeller.VanHove.onePeak;
import static org.nanomodeller.VanHove.twoPeaks;

public class CommonPhysics {
    public static double toEnergy(int energyStep, CommonProperties gp){
        return gp.getMin("E") + (energyStep * gp.getInc("E"));
    }

    public static Complex sigma(double E, Complex[] D){
        Complex result = comp(0,0.0);
        double dE = 0.01;
        Range range = CommonProperties.getInstance().getVar("E");
        for (double e : range){
            Complex increment = (D[(int)((e-range.getMin())/dE)].divide(comp(E - e,0.05))).times(dE);
            result = result.plus(increment);
        }
        return result.times(2*PI);
    }

    public static Complex[] density(CommonProperties gp, DensType type){
        int size = (int)((gp.getMax("E") - gp.getMin("E"))/gp.getInc("E"));
        MyFileWriter writer = new MyFileWriter("C:\\Users\\lenovo\\Desktop\\van2.csv");
        Complex[] result = new Complex[size+1];
        double d = gp.getMax("E") - gp.getMin("E");
        double count = 0;
        for (int e_step = 0; e_step <= size; e_step++) {
            double energy = gp.getMin("E") + (e_step * gp.getInc("E"));
            if (vanHoveTwo.equals(type)){
                result[e_step] = twoPeaks(energy, 5);
            }else if (vanHoveOne.equals(type)){
                result[e_step] = onePeak(energy, d);
            }else if (flat.equals(type)){
                if (abs(energy) < 15)
                    result[e_step] = comp(0.5/30);
                else
                    result[e_step] = Complex.ZERO;
            }
            writer.println(energy + "," + result[e_step].getReal());
            count += result[e_step].getReal() * gp.getInc("E");
        }
        writer.close();
        System.out.println(count);
        return result;
    }

    public enum DensType{
        flat, vanHoveOne, vanHoveTwo;
    }
}
