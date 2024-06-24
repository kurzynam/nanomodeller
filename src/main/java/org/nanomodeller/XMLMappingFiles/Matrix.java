package org.nanomodeller.XMLMappingFiles;


import org.jscience.mathematics.number.Complex;
import org.nanomodeller.Globals;
import org.nanomodeller.Tools.StringUtils;
import org.jscience.mathematics.vector.ComplexMatrix;
import org.nfunk.jep.JEP;
import org.nfunk.jep.function.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.function.Function;

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
        this.rows = new Object[rows.length][rows[0].length];
        for (int i = 0; i < rows.length; i++){
            for (int j = 0; j < rows[0].length; j++){
                parser.parseExpression((String)rows[i][j]);
                if (parser.getComplexValue() == null){
                    this.rows[i][j] = rows[i][j];
                }else {
                    this.rows[i][j] = Complex.valueOf(parser.getComplexValue().re(), parser.getComplexValue().im());
                }
            }
        }


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
        Function<Double, String> format = dbl -> {
            if(dbl != 0)
                return String.format("+(%f)", dbl);
            else
                return "";
            }
        ;
        for(Atom atom: par.getAtoms()){
            int i = par.getAtoms().indexOf(atom);
            for(Atom ato: par.getAtoms()){
                int j = par.getAtoms().indexOf(ato);
                String re = "";
                String im = "0.001";
                double electrodesPart = surfaceCoupling(par, ato, atom)/2;
                im += format.apply(electrodesPart);
                if(i == j){
                    re = "E - " + ato.getString("OnSiteEnergy");
                    if (Parameters.getInstance().getElectrodeByAtomIndex(j).isPresent()){
                        im += format.apply(Parameters.getInstance()
                                .getElectrodeByAtomIndex(j).get().getDouble("Coupling"));
                    }
                }
                else{
                    if(par.areBond(i,j)){
                        re += "-" + par.getBond(i,j).getString("Coupling");
                    }else{
                        re = "0";
                    }
                }
                if (StringUtils.isNotEmpty(im)){
                    rows[i][j] = re + String.format(" + i*(%s)", im);
                }else {
                    rows[i][j] = re;
                }

            }
        }
        Matrix result = new Matrix(rows);
        System.out.println(result);
        return result;
    }

    @Override
    public String toString(){
        String res = "";
        for (int i = 0; i < rows.length; i++){
            for (int j = 0; j < rows.length; j++){
                res += rows[i][j] + "  ";
            }
            res += "\n\n";
        }
        return res;
    }


    enum Compare{
        equal, notEqual, notNumber;
    }
}

