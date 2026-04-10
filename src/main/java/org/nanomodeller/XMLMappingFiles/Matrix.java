package org.nanomodeller.XMLMappingFiles;

import net.objecthunter.exp4j.ValidationResult;
import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.nanomodeller.Globals;
import org.nanomodeller.Tools.DataAccessTools.FileOperationHelper;
import org.nanomodeller.Tools.StringUtils;

import java.io.IOException;
import static org.nanomodeller.SurfaceEffect.surfaceCoupling;

public class Matrix {

    private Object[][] reals;
    private Object[][] imags;

    public Complex_F64 getValue(int i, int j, double m, double n, double tempE) {
        int iVal = i - j == 1 || j - i == 1 ? i : 0;
        if (reals[i][j] instanceof Complex_F64) {
//            if (i <= j)
                return (Complex_F64) reals[i][j];
//            else{
//                return new Complex_F64(((Complex_F64) reals[i][j]).real, -((Complex_F64) reals[i][j]).imaginary);
//            }
        }
        else if (reals[i][j] instanceof Double) {
            double im = ((Expression)imags[i][j]).setVariable(Globals.energy, tempE).setVariable(Globals.time, n).setVariable(Globals.m, m).setVariable("i", iVal).evaluate();
//            if (i <= j)
                return new Complex_F64((Double) reals[i][j], im);
//            else
//                return new Complex_F64((Double) reals[i][j], -im);
        }
        else {
            double re = ((Expression)reals[i][j]).setVariable(Globals.energy, tempE).setVariable(Globals.time, n).setVariable(Globals.m, m).setVariable("i", iVal).evaluate();
            double im;
            if (imags[i][j] instanceof Double) {
//                if (i <= j)
                    return new Complex_F64(re, (Double) imags[i][j]);
//                else
//                    return new Complex_F64(re, (Double) imags[i][j]*(-1));
            }else{
                im = ((Expression)imags[i][j]).setVariable(Globals.energy, tempE).setVariable(Globals.time, n).setVariable(Globals.m, m).setVariable("i", iVal).evaluate();
//                if (i <= j)
                    return new Complex_F64(re, im);
//                else
//                    return new Complex_F64(re, -im);
            }
        }
    }

    public ZMatrixRMaj convertToComplexMatrix(double m, double n, double tempE) {
        int dim = reals.length;
        ZMatrixRMaj matrix = new ZMatrixRMaj(dim, dim);

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                Complex_F64 value = getValue(i, j, m, n, tempE);
                matrix.set(i, j, value.real, value.imaginary);
            }
        }
        return matrix;
    }

    public Matrix (Parameters par) throws IOException {
        int size = par.getAtoms().size();
        reals = new Object[size][size];
        imags = new Object[size][size];
        double[][] gammas = FileOperationHelper.readDoubleDataFromFile("surfaces/MDOS.txt");
        double[][] lambdas = FileOperationHelper.readDoubleDataFromFile("surfaces/MDOSh.txt");
        java.util.function.Function<Double, String> format = dbl -> dbl != 0 ? String.format("+(%f)", dbl) : "";
        Function stepFunction = new Function("step", 1) {
            @Override
            public double apply(double... args) {
                return (args[0] >= 0) ? 1 : 0;
            }
        };
        Function reluFunction = new Function("relu", 1) {
            @Override
            public double apply(double... args) {
                return Math.max(args[0],0);
            }
        };

        Function sigmoidFunction = new Function("sigmoid", 1) {
            @Override
            public double apply(double... args) {
                return 1.0 / (1.0 + Math.exp(-args[0]));
            }
        };

        Function sum = new Function("sum", 2) {
            @Override
            public double apply(double... args) {
                return args[0]+args[1];
            }
        };
        Function dosiFunction = new Function("idos", 2) {
            @Override
            public double apply(double... args) {
                if (args[1] == 0)
                    return 0.5;
                return 9.5*FileOperationHelper.findValueByEnergy(gammas,args[0],(int)args[1]);
            }
        };
        Function dosrFunction = new Function("rdos", 2) {
            @Override
            public double apply(double... args) {
                if (args[1] == 0)
                    return 0;
                return 9.5*FileOperationHelper.findValueByEnergy(lambdas,args[0],(int)args[1]);
            }
        };
        for (Atom atom : par.getAtoms()) {
            int i = par.getAtoms().indexOf(atom);
            for (Atom ato : par.getAtoms()) {
                int j = par.getAtoms().indexOf(ato);
                String re = "";
                String im = "0.000001";
                double electrodesPart = surfaceCoupling(par, ato, atom) / 2;
                im += format.apply(electrodesPart);

                if (i == j) {
                    re = "E - " + ato.getString("OnSiteEnergy");
                    if (par.getElectrodeByAtomIndex(j).isPresent()) {
                        String coupling = par.getElectrodeByAtomIndex(j).get().getString(Globals.COUPLING);
                        if (coupling.contains("dos")){
                            im += "+" + coupling.replace("dos","idos");
                            re += String.format("-(%s*0.5)",coupling.replace("dos","rdos"));
                        }
                        else {
                            im += String.format("+(%s*0.5)", coupling);
                        }
                    }
                } else {
                    if (par.areBond(i, j)) {
                        Bond bond = par.getBond(i, j);
                        re += String.format("-(%s)", bond.getString(Globals.COUPLING));
                        String imCoupling = bond.getString(Globals.IM_COUPLING);
                        if (StringUtils.isNotEmpty(imCoupling)){
                            if (bond.getFirst() == i)
                                im = String.format("-(%s)", imCoupling);
                            else
                                im = String.format("(%s)", imCoupling);
                        }

                    } else {
                        re = "0";
                    }
                }

                Expression realEx = new ExpressionBuilder(re).
                        variables(Globals.energy, Globals.time,Globals.m,"i").
                        functions(stepFunction, dosiFunction, dosrFunction, reluFunction, sigmoidFunction,sum).build();
                Expression imEx = new ExpressionBuilder(im).
                        variables(Globals.energy, Globals.time,Globals.m,"i").
                        functions(stepFunction, dosiFunction, dosrFunction, reluFunction, sigmoidFunction,sum).build();
                Double r = null;
                Double ii = null;

                ValidationResult vreal = realEx.validate();
                if(ValidationResult.SUCCESS.equals(vreal)) {
                    r = realEx.evaluate();
                }else {
                    this.reals[i][j] = realEx;
                }
                ValidationResult vimag = imEx.validate();
                if(ValidationResult.SUCCESS.equals(vimag)) {
                    ii = imEx.evaluate();
                }else {
                    this.imags[i][j] = imEx;
                }

                if (r != null) {
                    if (ii != null) {
                        this.reals[i][j] = new Complex_F64(r, ii);
                    }
                    else {
                        this.reals[i][j] = r;
                    }
                }
                if (ii != null) {
                    if (r == null) {
                        this.imags[i][j] = ii;
                    }
                }
            }
        }
    }

    public boolean isSymmetric(){
        for (int i = 0; i < this.reals.length/2 + 1; i++){
            for (int j = i + 1; j < this.reals.length/2 + 1; j++){
                if (!this.reals[i][j].equals(this.reals[j][i])){
                    return false;
                }
            }
        }
        return true;
    }

    public Complex_F64[][] inverse(Complex_F64[][] matrix) {
        Complex_F64[][] inverse = new Complex_F64[matrix.length][matrix[0].length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
//                inverse[i][j] = a(Math.min(i,j))*b(Math.max(i,j));
            }
        }
        return inverse;
    }

    private Complex_F64 b(int j) {
        return null;
    }

    private Complex_F64 a(int i) {
        return null;
    }


    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (Object[] row : reals) {
            for (Object cell : row) {
                res.append(cell).append("  ");
            }
            res.append("\n\n");
        }
        return res.toString();
    }
}
