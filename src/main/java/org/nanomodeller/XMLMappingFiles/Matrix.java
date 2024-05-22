package org.nanomodeller.XMLMappingFiles;


import org.nanomodeller.Globals;
import org.nanomodeller.Tools.StringUtils;
import org.jscience.mathematics.number.Complex;
import org.jscience.mathematics.vector.ComplexMatrix;
import org.nfunk.jep.JEP;
import org.nfunk.jep.function.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

import static org.nanomodeller.SurfaceEffect.surfaceCoupling;

@XmlRootElement(name="Matrix")
public class Matrix {

    private JEP parser;
    private Object[][] rows;

    @XmlElements(@XmlElement(name="Row"))

    public Object[][] getRows() {
        return rows;
    }

    public JEP getParser() {
        return parser;
    }

    public Matrix(Object[][] rows){

        parser = new JEP();
        parser.addComplex();
        parser.addFunction("cos", new Cosine());
        parser.addFunction("sin", new Sine());
        parser.addFunction("tan", new Tangent());
        parser.addFunction("abs", new Abs());
        parser.addFunction("ln", new Logarithm());
        parser.addFunction("arccos", new ArcCosine());
        parser.addFunction("arcsin", new ArcSine());
        parser.addFunction("sinh", new SineH());
        parser.addFunction("cosh", new CosineH());
        parser.addFunction("tanh", new TanH());
        parser.addFunction("arctan", new ArcTangent());
        parser.addFunction("arctanh", new ArcTanH());
        parser.addFunction("arcsinh",new ArcSineH());
        parser.addFunction("arccosh", new ArcCosineH());
        parser.addVariable("t", 1000);
        parser.addVariable("n",0);
        this.rows = rows;
    }

    public Complex getValue(int i, int j){
        if (rows[i][j] instanceof String) {
            parser.parseExpression(rows[i][j].toString());
            Complex res = Complex.valueOf(parser.getComplexValue().re(), parser.getComplexValue().im());
            return res;
        }else{
            return (Complex)rows[i][j];
        }

    }
    public boolean contains(String pattern){
        int dim = rows.length;
        for (int i = 0; i < dim; i++){
            for (int j = 0; j < dim; j++){
                if (rows[i][j].toString().contains(pattern)){
                    return true;
                }
            }
        }
        return false;

    }

    public ComplexMatrix convertToComplexMatrix(){

        int dim = rows.length;
        Complex[][] tempArray = new Complex[dim][dim];
        for (int i = 0; i < dim; i++){
            for (int j = 0; j < dim; j++){
                tempArray[i][j] = getValue(i,j);
            }
        }
        return ComplexMatrix.valueOf(tempArray);

    }
    public static Matrix readMatrixFromDataFile(Parameters par){

        int size = par.getAtoms().size();
        Object[][] rows = new Object[size][];
        for (int i = 0; i < size; i++) {
            rows[i] = new Object[size];
        }
        for(Atom atom: par.getAtoms()){
            int i = par.getAtoms().indexOf(atom);
            for(Atom ato: par.getAtoms()){
                int j = par.getAtoms().indexOf(ato);
                rows[i][j] = "";
                String electrodesPart = surfaceCoupling(par, ato, atom) + "";
                if(ato.equals(atom)){
                    String energy = "E - (" + ato.getString("OnSiteEnergy") + ")";
                    rows[i][j] = energy;
                    ArrayList<Electrode> electrodes = par.getElectrodesByAtomID(i);
                    for(int e_i = 0; e_i <  electrodes.size(); e_i++){
                        if (StringUtils.isNotEmpty(electrodesPart))
                            electrodesPart += "-";
                        electrodesPart += electrodes.get(e_i).getString("Coupling");
                    }
                    electrodesPart +=  "+i*" + Globals.ETA;

                }
                else if(par.areBond(i,j)){
                    String coupling = par.getBond(i,j).getString("Coupling");
                    rows[i][j] += "-" + coupling;
                }else{
                    rows[i][j] = "0";
                }
                if (StringUtils.isNotEmpty(electrodesPart) && !Compare.equal.equals(isZero(electrodesPart)))
                    rows[i][j] += "-"+electrodesPart+"" ;
                if(!Compare.notNumber.equals(isZero(rows[i][j].toString()))){
                    rows[i][j] = Complex.valueOf(Double.parseDouble(rows[i][j].toString()), 0);
                }
            }
        }
        Matrix result = new Matrix(rows);
        return result;
    }

    static Compare  isZero(String x){
        Compare res = Compare.notEqual;
        try {
            if (Double.parseDouble(x) == 0){
                res = Compare.equal;
            }
        }
        catch (Exception e){
            res = Compare.notNumber;
        }
        return res;
    }

    enum Compare{
        equal, notEqual, notNumber;
    }
}

