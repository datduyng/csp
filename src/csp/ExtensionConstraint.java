package csp;

import abscon.instance.components.PConstraint;

import java.util.HashSet;
import java.util.List;

public class ExtensionConstraint extends Constraint {
    Relation relation;
    public ExtensionConstraint(PConstraint ref, String name, int arity, List<Variable> scope, Relation relation) {
        super(ref, name, arity, scope);
        this.relation = relation;
    }
    public String toFullString() {
        return "Name: " + name +
                ", variables: " + Variable.varsToString(new HashSet<>(scope)) +
                ", definition: " + relation.semantics + " " + relation.tuplesToString() + "\n";
    }
}
