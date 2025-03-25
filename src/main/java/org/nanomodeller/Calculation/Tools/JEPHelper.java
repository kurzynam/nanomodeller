package org.nanomodeller.Calculation.Tools;

import org.nanomodeller.Tools.JEP_functions.Sigmoid;
import org.nanomodeller.Tools.JEP_functions.Step;
import org.nanomodeller.XMLMappingFiles.CommonProperties;
import org.nfunk.jep.JEP;
import org.nfunk.jep.function.*;

public class JEPHelper {
    public static JEP createJEP(){
        JEP parser = new JEP();
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
        parser.addFunction("step", new Step());
        parser.addFunction("sigmoid", new Sigmoid());

        for (String property : CommonProperties.getInstance().getProperties().keySet()){
            parser.addVariable(property, Float.parseFloat(CommonProperties.getInstance().getString(property)));
        }
        return parser;
    }
}
