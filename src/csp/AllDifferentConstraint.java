package csp;

import abscon.instance.components.PAllDifferent;
import abscon.instance.components.PConstraint;

import java.util.List;

public class AllDifferentConstraint extends Constraint{

    public AllDifferentConstraint(PConstraint ref,
                                  String name, int arity,
                                  List<Variable> scope){
        super(ref, name, arity, scope);
    }

    @Override
    public String toFullString() {
        return null;
    }
}
