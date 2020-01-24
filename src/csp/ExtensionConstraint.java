package csp;

import java.util.List;

public class ExtensionConstraint extends Constraint {
    Relation relation;
    public ExtensionConstraint(String name, int arity, List<Variable> scope, Relation relation) {
        super(name, arity, scope);
        this.relation = relation;
    }
}
