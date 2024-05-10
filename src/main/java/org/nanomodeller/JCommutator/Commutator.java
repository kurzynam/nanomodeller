package org.nanomodeller.JCommutator;

public class Commutator {

    Expression exp1;
    Expression exp2;

    public Commutator(String expr1, String expr2){
        exp1 = new Expression("+" + expr1 + expr2);
        exp2 = new Expression("-" + expr2 + expr1);
    }
    Expression evaluate (){
        exp1.sortAll();
        exp2.sortAll();
        Expression result =  exp1.add(exp2);
        return result;
    }
}
