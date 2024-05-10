package org.nanomodeller.Tools.JEP_functions;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

import java.util.Stack;

public class NDelta extends PostfixMathCommand {


    public NDelta() {
        numberOfParameters = 1;
    }

    /**
     * Returns 0 for x = 0, 1 otherwise
     */
    public void run(Stack var1) throws ParseException {
        this.checkStack(var1);
        Object var2 = var1.pop();
        if (var2 instanceof Number) {
            double val = ((Number)var2).doubleValue();
            if (val == 0){
                var1.push(new Double(0));
            }
            else{
                var1.push(new Double(1));
            }
        }
        else {
            throw new ParseException("Invalid parameter type");
        }
    }
}