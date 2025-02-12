package org.nanomodeller.Calculation;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.function.Function;

public class ExpressionParser {
    public static Function<Double, Double> parseExpression(String expression, String variable) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

        return (Double x) -> {
            try {
                // Replace the variable with its value
                String expr = expression.replace(variable, x.toString());
                return ((Number) engine.eval(expr)).doubleValue();
            } catch (ScriptException e) {
                throw new RuntimeException("Error evaluating expression: " + expression, e);
            }
        };
    }
}