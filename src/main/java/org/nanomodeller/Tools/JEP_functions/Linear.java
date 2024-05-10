package org.nanomodeller.Tools.JEP_functions;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

import java.util.Stack;

public class Linear extends PostfixMathCommand {


    public Linear() {
        super.numberOfParameters = 2;
    }

    public void run(Stack args) throws ParseException {
        this.checkStack(args);
        Object secondPar = args.pop();
        Object firstPar = args.pop();
        if (secondPar instanceof Number && firstPar instanceof Number) {
            double firstVal = ((Number)firstPar).doubleValue();
            double secondVal = ((Number)secondPar).doubleValue();
            double result;

            if(firstVal >= secondVal){
                result = firstVal-secondVal;
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