package org.nanomodeller.Tools.JEP_functions;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

import java.util.Stack;

public class Triangular extends PostfixMathCommand {

    /*

    parametrs:

    Triangular(argument t, minvalue t_0, maxvalue t_max)

    return:

    0  t < t_min and t > t_max
    value t_min < t < t_max

    */
    public Triangular() {
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
            double width = thirdVal - secondVal;
            if(firstVal >= secondVal && firstVal <= thirdVal){
                result = (firstVal-secondVal)/width;
            }
            else{
                result = 0;
            }
            args.push(new Double(result));
        } else {
            throw new ParseException("Invalid parameter type");
        }
    }
}