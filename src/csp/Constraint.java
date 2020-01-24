package csp;

import java.util.List;

public abstract class Constraint {
    String name;
    int arity;
    List<Variable> scope;

    public Constraint(String name, int arity, List<Variable> scope) {
        this.name = name;
        this.arity = arity;
        this.scope = scope;
    }

    public String toString() {
        return name;
    }
}
