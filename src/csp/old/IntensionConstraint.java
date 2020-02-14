package csp.old;


import abscon.instance.components.PConstraint;
import abscon.instance.components.PFunction;

import java.util.HashSet;
import java.util.List;

public class IntensionConstraint extends Constraint{

//    CFunction function;
    PFunction function;
    String[] universalPostfixExpression;

    public IntensionConstraint(PConstraint ref, String name, int arity, List<Variable> scope,
                               String effectiveParametersExpression,
                               PFunction pfunction, String[] universalPostfixExpression) {
        super(ref, name, arity, scope);
        this.function = pfunction;
        this.function.effectiveParametersExpression = effectiveParametersExpression;
        this.universalPostfixExpression = universalPostfixExpression;
    }

    public String toFullString() {
        return "Name: " + name +
                ", variables: " + Variable.varsToString(new HashSet<>(scope)) +
                ", definition: intension " + function.toString() + "\n";
    }



}
