package org.nanomodeller;

import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.XMLMappingFiles.GlobalChainProperties;
import org.jscience.mathematics.number.Complex;

import static org.nanomodeller.CommonMath.comp;
import static org.nanomodeller.CommonPhysics.DensType.*;
import static org.nanomodeller.VanHove.onePeak;
import static org.nanomodeller.VanHove.twoPeaks;
import static java.lang.Math.PI;
import static java.lang.Math.abs;

public class CommonPhysics {
    public static double toEnergy(int energyStep, double dE, GlobalChainProperties gp){
        return gp.getDoubleEmin() + (energyStep * dE);
    }
    public static int toEnergyStep(double E, double dE, GlobalChainProperties gp){
        double d = gp.getDoubleEmin();
        return (int)((E-d)/dE);
    }

    public static Complex sigma(double E, Complex[] D, double Emin, double Emax){
        Complex result = comp(0,0.0);
        double dE = 0.01;
        for (double e = Emin; e <= Emax; e+= dE){
            Complex increment = (D[(int)((e-Emin)/dE)].divide(comp(E - e,0.05))).times(dE);
            result = result.plus(increment);
        }
        return result.times(2*PI);
    }

    public static Complex[] density(GlobalChainProperties gp, DensType type){
        int size = gp.getNumberOfEnergySteps();
        MyFileWriter writer = new MyFileWriter("C:\\Users\\lenovo\\Desktop\\van2.csv");
        Complex[] result = new Complex[size+1];
        double d = gp.getDoubleEmax() - gp.getDoubleEmin();
        double count = 0;
        for (int e_step = 0; e_step <= size; e_step++) {
            double energy = gp.getDoubleEmin() + (e_step * gp.getdE());
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
            count += result[e_step].getReal() * gp.getdE();
        }
        writer.close();
        System.out.println(count);
        return result;
    }

    public enum DensType{
        flat, vanHoveOne, vanHoveTwo;
    }
}
