package org.nanomodeller.Tools.JEP_functions;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;
import org.nfunk.jep.type.Complex;

import java.util.Stack;

public class Pulse extends PostfixMathCommand {
    public Pulse() {
        super.numberOfParameters = 1;
    }

    public void run(Stack var1) throws ParseException {
        this.checkStack(var1);
        Object var2 = var1.pop();
        var1.push(this.pulse(var2));
    }

    public Object pulse(Object var1) throws ParseException {
        if (var1 instanceof Number) {
            return new Double(Math.pow(Math.cos(((Number)var1).doubleValue()*Math.PI),2000));
        } else if (var1 instanceof Complex) {
            return ((Complex) var1).mul(Math.PI).power(2000).cos();
        } else {
            throw new ParseException("Invalid parameter type");
        }
    }
}
