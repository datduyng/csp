package csp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static csp.Variable.varsToString;

public class Variable {
    String name;
    Domain initialDomain;
    Domain currentDomain;
    List<Constraint> constraints;
    Set<Variable> neighbors;

    public Variable(String name, Domain initialDomain, Domain currentDomain) {
        this.name = name;
        this.initialDomain = initialDomain;
        this.currentDomain = currentDomain;
        this.constraints = new ArrayList<>();
        this.neighbors = new HashSet<>();
    }

    public static String varsToString(Set<Variable> vars) {
        StringBuilder res = new StringBuilder("{");
        vars.forEach((v) -> {
            res.append(", " + v.name);
        });
        return res.append(" }").toString();
    }

    public String toString() {
        return "name: " + name +
                ", init-domain: " + initialDomain.toString() +
                ", constraints: " + constraints.toString() +
                ", neighbors: " + varsToString(neighbors);
    }
}
