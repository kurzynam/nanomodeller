package org.nanomodeller.Tools.JEP_functions;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

import java.util.Stack;

public class Rectangular extends PostfixMathCommand {


    public Rectangular() {
        super.numberOfParameters = 3;
    }

    public void run(Stack args) throws ParseException {
        this.checkStack(args);
        Object thirdPar = args.pop();
        Object secondPar = args.pop();
        Object firstPar = args.pop();
        if (thirdPar instanceof Number && secondPar instanceof Number && firstPar instanceof Number) {
            double firstVal = ((Number)firstPar).doubleValue();
            double secondVal = ((Number)secondPar).doubleValue();
            double thirdVal = ((Number)thirdPar).doubleValue();
            double result;

            if(firstVal >= secondVal && firstVal <= thirdVal){
                result = 1;
            }
            else {
                result = 0;
            }
            args.push(new Double(result));
        } else {
            throw new ParseException("Invalid parameter type");
        }
    }
}