package csp.old;

import abscon.instance.components.PFunction;

import java.util.Arrays;

public class CFunction {
    String name;

    String[] formalParameters;

    String functionalExpression;

    String[] universalPostfixExpression;

    String effectiveParametersExpression;

    public CFunction(PFunction pfunction, String effectiveParametersExpression) {
        this.name = pfunction.getName();
        this.effectiveParametersExpression = effectiveParametersExpression;
        this.formalParameters = pfunction.getFormalParameters();
        this.functionalExpression = pfunction.getFunctionalExpression();
        this.universalPostfixExpression = pfunction.getUniversalPostfixExpression();
    }

    public String toString() {
        String eParamExpr = String.join(",", effectiveParametersExpression.split(" "));
        return "function: " + functionalExpression + ", " +
                "params: " + "{" + eParamExpr + "}";
    }
}
