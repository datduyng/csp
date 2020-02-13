package csp;


import abscon.instance.components.PFunction;
import abscon.instance.components.PPredicate;
import abscon.instance.intension.EvaluationManager;

import java.util.HashSet;
import java.util.List;

public class IntensionConstraint extends Constraint{

//    CFunction function;
    PFunction function;
    String[] universalPostfixExpression;

    public IntensionConstraint(String name, int arity, List<Variable> scope,
                               String effectiveParametersExpression,
                               PFunction pfunction, String[] universalPostfixExpression) {
        super(name, arity, scope);
        this.function = pfunction;
        this.function.effectiveParametersExpression = effectiveParametersExpression;
        this.universalPostfixExpression = universalPostfixExpression;
    }

    public String toFullString() {
        return "Name: " + name +
                ", variables: " + Variable.varsToString(new HashSet<>(scope)) +
                ", definition: intension " + function.toString() + "\n";
    }

    @Override
    public boolean isSupportedBy(int[] vals) {
        EvaluationManager evaluationManager = new EvaluationManager(this.universalPostfixExpression);
        long result = evaluationManager.evaluate(vals);
        if (function instanceof PPredicate) {
            boolean satisfied = (result == 1);
            return satisfied;
        }
        return false;
    }



}
