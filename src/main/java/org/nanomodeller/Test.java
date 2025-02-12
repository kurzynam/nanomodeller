package org.nanomodeller;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Test {
    public static void runTest() {
            Expression exp = new ExpressionBuilder("hhh2")
            .build();
            System.out.println(exp.evaluate());
    }



}
