package csp.old;

import abscon.instance.components.PConstraint;

import java.util.List;

public abstract class Constraint {
    PConstraint ref;
    String name;
    int arity;
    List<Variable> scope;

    public Constraint(PConstraint ref, String name, int arity, List<Variable> scope) {
        this.ref = ref;
        this.name = name;
        this.arity = arity;
        this.scope = scope;
    }

    public String toString() {
        return name;
    }
    public boolean isSupportedBy(int[] vals) {
        long val = this.ref.computeCostOf(vals);
        return val == 0;
    }

    public abstract String toFullString();
}
