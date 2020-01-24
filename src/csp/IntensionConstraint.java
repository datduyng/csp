package csp;

import java.util.List;

public class IntensionConstraint extends Constraint{
    public IntensionConstraint(String name, int arity, List<Variable> scope) {
        super(name, arity, scope);
    }
}
