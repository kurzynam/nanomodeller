package org.nanomodeller.XMLMappingFiles;

import net.objecthunter.exp4j.ValidationResult;
import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.nanomodeller.Tools.DataAccessTools.FileOperationHelper;
import java.io.IOException;
import static org.nanomodeller.SurfaceEffect.surfaceCoupling;

public class Matrix {

    private Object[][] reals;
    private Object[][] imags;

    public Complex_F64 getValue(int i, int j, double m, double n, double tempE) {

        if (reals[i][j] instanceof Complex_F64) {
            return (Complex_F64) reals[i][j];
        }
        else if (reals[i][j] instanceof Double) {
            double im = ((Expression)imags[i][j]).setVariable("E", tempE).setVariable("n", n).setVariable("m", m).evaluate();
            return new Complex_F64((Double) reals[i][j], im);
        }
        else {
            double re = ((Expression)reals[i][j]).setVariable("E", tempE).setVariable("n", n).setVariable("m", m).evaluate();
            double im;
            if (imags[i][j] instanceof Double) {
                return new Complex_F64(re, (Double) imags[i][j]);
            }else{
                im = ((Expression)imags[i][j]).setVariable("E", tempE).setVariable("n", n).setVariable("m", m).evaluate();
                return new Complex_F64(re, im);
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
                String im = "0.001";
                double electrodesPart = surfaceCoupling(par, ato, atom) / 2;
                im += format.apply(electrodesPart);

                if (i == j) {
                    re = "E - " + ato.getString("OnSiteEnergy");
                    if (par.getElectrodeByAtomIndex(j).isPresent()) {
                        String coupling = par.getElectrodeByAtomIndex(j).get().getString("Coupling");
                        if (coupling.contains("dos")){
                            im += "+" + coupling.replace("dos","idos");
                            re += String.format("-(%s)",coupling.replace("dos","rdos"));
                        }
                        else {
                            im += "+" + coupling;
                        }
                    }
                } else {
                    if (par.areBond(i, j)) {
                        re += String.format("-(%s)", par.getBond(i, j).getString("Coupling"));
                    } else {
                        re = "0";
                    }
                }

                Expression realEx = new ExpressionBuilder(re).
                        variables("E", "n","m").
                        functions(stepFunction, dosiFunction, dosrFunction, reluFunction, sigmoidFunction,sum).build();
                Expression imEx = new ExpressionBuilder(im).
                        variables("E", "n","m").
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
