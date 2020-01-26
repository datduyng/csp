package csp;


import abscon.instance.components.PFunction;

import java.util.HashSet;
import java.util.List;

public class IntensionConstraint extends Constraint{

    CFunction function;
    String[] universalPostfixExpression;

    public IntensionConstraint(String name, int arity, List<Variable> scope,
                               String effectiveParametersExpression,
                               PFunction pfunction, String[] universalPostfixExpression) {
        super(name, arity, scope);
        this.function = new CFunction(pfunction, effectiveParametersExpression);
        this.universalPostfixExpression = universalPostfixExpression;
    }

    public String toFullString() {
        return "Name: " + name +
                ", variables: " + Variable.varsToString(new HashSet<>(scope)) +
                ", definition: intension " + function.toString() + "\n";
    }

}
