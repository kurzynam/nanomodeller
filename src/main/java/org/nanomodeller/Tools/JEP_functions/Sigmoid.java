package org.nanomodeller.Tools.JEP_functions;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

import java.util.Stack;

public class Sigmoid extends PostfixMathCommand {

    public Sigmoid() {
        super.numberOfParameters = 1;
    }

    public void run(Stack args) throws ParseException {
        this.checkStack(args);
        Object firstPar = args.pop();
        if (firstPar instanceof Number) {
            double x = ((Number)firstPar).doubleValue();
            double result = 1 / (1 + Math.exp(-x));
            args.push(new Double(result));
        } else {
            throw new ParseException("Invalid parameter type");
        }
    }
}