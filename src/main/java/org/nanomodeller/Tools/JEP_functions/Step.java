package org.nanomodeller.Tools.JEP_functions;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

import java.util.Stack;

public class Step extends PostfixMathCommand {


    public Step() {
        numberOfParameters = 1;
    }

    /**
     * Returns +1 for positive numbers, 0 for negatives and +1/2 for zero
     */
    public void run(Stack var1) throws ParseException {
        this.checkStack(var1);
        Object var2 = var1.pop();
        if (var2 instanceof Number) {
            double val = ((Number)var2).doubleValue();
            if (val > 0){
                var1.push(new Double(1));
            }
            else{
                var1.push(new Double(0));
            }
        }
        else {
            throw new ParseException("Invalid parameter type");
        }
    }
}